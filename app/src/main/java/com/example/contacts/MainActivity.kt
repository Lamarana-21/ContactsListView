package com.example.contacts

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    // 1) Les données : une liste MODIFIABLE (mutableListOf) de chaînes simples
    private val contacts = mutableListOf(
        "Ahmed Ben Ali",
        "Sarra Trabelsi",
        "Mohamed Gharbi",
        "Ines Jaziri",
        "Youssef Mansouri",
        "Amira Chaabane",
        "Karim Bouazizi",
        "Nour Hammami",
    )

    // 2) L'adaptateur fait le lien entre les données et la ListView
    private lateinit var adapter: ArrayAdapter<String>

    // 3) Les vues
    private lateinit var etContact: EditText
    private lateinit var btnAjouter: Button
    private lateinit var btnModifier: Button
    private lateinit var lvContacts: ListView

    // Position de l'élément sélectionné (-1 = aucune sélection)
    private var positionSelectionnee = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Récupération des vues déclarées dans activity_main.xml
        etContact = findViewById(R.id.etContact)
        btnAjouter = findViewById(R.id.btnAjouter)
        btnModifier = findViewById(R.id.btnModifier)
        lvContacts = findViewById(R.id.lvContacts)

        // Tri initial de la liste par ordre alphabétique
        contacts.sort()

        // Création de l'adaptateur avec un layout Android prédéfini
        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_activated_1,
            contacts,
        )
        lvContacts.adapter = adapter

        // Clic sur un élément de la liste -> on copie son texte dans l'EditText
        lvContacts.setOnItemClickListener { _, _, position, _ ->
            positionSelectionnee = position
            lvContacts.setItemChecked(position, true)
            etContact.error = null
            etContact.setText(contacts[position])
            etContact.setSelection(etContact.text.length) // curseur à la fin
            etContact.requestFocus()
        }

        // Appui long sur un élément de la liste -> suppression après confirmation
        lvContacts.setOnItemLongClickListener { _, _, position, _ ->
            supprimerContact(position)
            true
        }

        // Clic sur le bouton "Ajouter"
        btnAjouter.setOnClickListener { ajouterContact() }

        // Clic sur le bouton "Modifier"
        btnModifier.setOnClickListener { modifierContact() }

        // Touche "Terminé" du clavier valide la modification ou l'ajout
        etContact.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (positionSelectionnee == -1) {
                    ajouterContact()
                } else {
                    modifierContact()
                }
                true
            } else {
                false
            }
        }
    }

    private fun ajouterContact() {
        etContact.error = null
        val nouveauNom = etContact.text.toString().trim()

        // Vérification 1 : le texte ne doit pas être vide
        if (nouveauNom.isEmpty()) {
            etContact.error = getString(R.string.msg_nom_vide)
            return
        }

        // Vérification 2 : interdiction des doublons
        if (contacts.any { it.equals(nouveauNom, ignoreCase = true) }) {
            etContact.error = getString(R.string.msg_doublon)
            return
        }

        // Ajout du contact...
        contacts.add(nouveauNom)

        // Tri alphabétique
        contacts.sort()

        // Mise à jour de l'affichage
        adapter.notifyDataSetChanged()

        // Sélection du nouveau contact trié
        positionSelectionnee = contacts.indexOf(nouveauNom)
        if (positionSelectionnee != -1) {
            lvContacts.setItemChecked(positionSelectionnee, true)
        }

        Toast.makeText(this, getString(R.string.msg_contact_ajoute, nouveauNom), Toast.LENGTH_SHORT).show()
    }

    private fun modifierContact() {
        etContact.error = null

        // Vérification 1 : un élément doit être sélectionné
        if (positionSelectionnee == -1) {
            Toast.makeText(this, R.string.msg_aucune_selection, Toast.LENGTH_SHORT).show()
            return
        }

        // Vérification 2 : le texte ne doit pas être vide
        val nouveauNom = etContact.text.toString().trim()
        if (nouveauNom.isEmpty()) {
            etContact.error = getString(R.string.msg_nom_vide)
            return
        }

        // Vérification 3 : interdiction des doublons (sauf pour le contact en cours de modification)
        val doublonExiste = contacts.indices.any { i ->
            (i != positionSelectionnee) && contacts[i].equals(nouveauNom, ignoreCase = true)
        }
        if (doublonExiste) {
            etContact.error = getString(R.string.msg_doublon)
            return
        }

        // Mise à jour de la donnée...
        contacts[positionSelectionnee] = nouveauNom

        // Tri alphabétique
        contacts.sort()

        // Mise à jour de la nouvelle position après le tri
        positionSelectionnee = contacts.indexOf(nouveauNom)

        // Redessiner la liste
        adapter.notifyDataSetChanged()

        if (positionSelectionnee != -1) {
            lvContacts.setItemChecked(positionSelectionnee, true)
        }

        Toast.makeText(this, getString(R.string.msg_contact_modifie, nouveauNom), Toast.LENGTH_SHORT).show()
    }

    private fun supprimerContact(position: Int) {
        val contactASupprimer = contacts[position]

        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_supprimer_titre)
            .setMessage(getString(R.string.dialog_supprimer_msg, contactASupprimer))
            .setPositiveButton(R.string.btn_oui) { _, _ ->
                contacts.removeAt(position)
                contacts.sort()
                adapter.notifyDataSetChanged()

                // Réinitialisation ou ajustement de la sélection si nécessaire
                if (positionSelectionnee == position) {
                    positionSelectionnee = -1
                    etContact.text.clear()
                    etContact.error = null
                    lvContacts.clearChoices()
                } else if (positionSelectionnee > position) {
                    positionSelectionnee--
                    lvContacts.setItemChecked(positionSelectionnee, true)
                }

                Toast.makeText(this, getString(R.string.msg_contact_supprime, contactASupprimer), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.btn_non, null)
            .show()
    }
}