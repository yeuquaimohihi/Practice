package com.example.sqlite_th

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    private lateinit var dbHelper: SQLiteOpenHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = SQLiteOpenHelper(this)

        val edtName = findViewById<EditText>(R.id.edt_name)
        val edtPhone = findViewById<EditText>(R.id.edt_phone)
        val btnAdd = findViewById<Button>(R.id.btn_add)
        val btnEdit = findViewById<Button>(R.id.btn_edit)
        val btnDelete = findViewById<Button>(R.id.btn_del)
        val btnLoad = findViewById<Button>(R.id.btn_load)
        val txtLoad = findViewById<TextView>(R.id.txt_load)

        btnAdd.setOnClickListener {
            val name = edtName.text.toString()
            val phone = edtPhone.text.toString()
            dbHelper.addContact(name, phone)
        }

        btnEdit.setOnClickListener {
            val cursor = dbHelper.getAllContacts()

            // Create a list of contact strings and their IDs
            val contactsList = mutableListOf<String>()
            val contactIds = mutableListOf<Int>()

            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val id = cursor.getInt(0)
                val name = cursor.getString(1)
                val phone = cursor.getString(2)

                contactsList.add("ID: $id, Name: $name, Phone: $phone")
                contactIds.add(id)
                cursor.moveToNext()
            }
            cursor.close()

            // Create and show the dialog
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select a contact to edit")

            builder.setItems(contactsList.toTypedArray()) { _, which ->
                // Get the ID of the selected contact
                val selectedId = contactIds[which]

                // Create and show the edit dialog
                val editBuilder = AlertDialog.Builder(this)
                editBuilder.setTitle("Edit Contact")
                val layout = LinearLayout(this)
                layout.orientation = LinearLayout.VERTICAL

                val editName = EditText(this)
                val editPhone = EditText(this)
                editName.hint = "Name"
                editPhone.hint = "Phone"
                layout.addView(editName)
                layout.addView(editPhone)
                editBuilder.setView(layout)

                editBuilder.setPositiveButton("Save") { _, _ ->
                    val newName = editName.text.toString()
                    val newPhone = editPhone.text.toString()
                    dbHelper.updateContact(selectedId, newName, newPhone)

                    // Refresh the displayed list
                    btnLoad.performClick()
                }

                editBuilder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }

                editBuilder.show()
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            builder.show()
        }

        btnDelete.setOnClickListener {
            val cursor = dbHelper.getAllContacts()

            // Create a list of contact strings and their IDs
            val contactsList = mutableListOf<String>()
            val contactIds = mutableListOf<Int>()

            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val id = cursor.getInt(0)
                val name = cursor.getString(1)
                val phone = cursor.getString(2)

                contactsList.add("ID: $id, Name: $name, Phone: $phone")
                contactIds.add(id)
                cursor.moveToNext()
            }
            cursor.close()

            // Create and show the dialog
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Select a contact to delete")

            builder.setItems(contactsList.toTypedArray()) { _, which ->
                // Get the ID of the selected contact
                val selectedId = contactIds[which]

                // Confirm deletion
                val confirmBuilder = android.app.AlertDialog.Builder(this)
                confirmBuilder.setTitle("Confirm Deletion")
                confirmBuilder.setMessage("Are you sure you want to delete this contact?")

                confirmBuilder.setPositiveButton("Yes") { _, _ ->
                    // Delete the contact
                    dbHelper.deleteContact(selectedId)

                    // Refresh the displayed list
                    btnLoad.performClick()
                }

                confirmBuilder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }

                confirmBuilder.show()
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            builder.show()
        }


        btnLoad.setOnClickListener {
            val cursor = dbHelper.getAllContacts()
            cursor.moveToFirst()
            val stringBuilder = StringBuilder()
            while (!cursor.isAfterLast) {
                stringBuilder.append("ID: ${cursor.getInt(0)}, Name: ${cursor.getString(1)}, Phone: ${cursor.getString(2)}\n")
                cursor.moveToNext()
            }
            txtLoad.text = stringBuilder.toString()
            cursor.close()
        }
    }
}