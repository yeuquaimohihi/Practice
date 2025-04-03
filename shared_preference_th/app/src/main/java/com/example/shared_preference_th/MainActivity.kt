package com.example.shared_preference_th
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    private lateinit var btnSave: Button
    private lateinit var btnDel: Button
    private lateinit var btnView: Button
    private lateinit var btnDelItem: Button
    private lateinit var edtTk: EditText
    private lateinit var editMk: EditText
    private lateinit var txtInfo: TextView
    private lateinit var preferenceHelper: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnSave = findViewById(R.id.btn_save)
        btnDel = findViewById(R.id.btn_del)
        btnView = findViewById(R.id.btn_view)
        btnDelItem = findViewById(R.id.btn_del_item)
        edtTk = findViewById(R.id.edt_tk)
        editMk = findViewById(R.id.edt_mk)
        txtInfo = findViewById(R.id.txt_view)

        preferenceHelper = PreferenceHelper(this)

        btnSave.setOnClickListener {
            val tkList = preferenceHelper.getList("tk")?.toMutableList() ?: mutableListOf()
            val mkList = preferenceHelper.getList("mk")?.toMutableList() ?: mutableListOf()
            tkList.add(edtTk.text.toString())
            mkList.add(editMk.text.toString())
            preferenceHelper.saveList("tk", tkList)
            preferenceHelper.saveList("mk", mkList)
            Toast.makeText(this, "Lưu thành công", Toast.LENGTH_SHORT).show()
        }
        btnView.setOnClickListener {
            val tkList = preferenceHelper.getList("tk")
            val mkList = preferenceHelper.getList("mk")
            if (tkList != null && mkList != null) {
                val info = StringBuilder()
                for (i in tkList.indices) {
                    info.append("Tài khoản: ${tkList[i]}\nMật khẩu: ${mkList[i]}\n\n")
                }
                txtInfo.text = info.toString()
            } else {
                txtInfo.text = "No data found"
            }
        }
        btnDel.setOnClickListener {
            preferenceHelper.removeData("tk")
            preferenceHelper.removeData("mk")
            Toast.makeText(this, "Xóa thành công", Toast.LENGTH_SHORT).show()
        }
        btnDelItem.setOnClickListener {
            val tk = edtTk.text.toString()
            val mk = editMk.text.toString()
            preferenceHelper.removeItemFromList("tk", tk)
            preferenceHelper.removeItemFromList("mk", mk)
            Toast.makeText(this, "Xóa phần tử thành công", Toast.LENGTH_SHORT).show()
        }
    }
}