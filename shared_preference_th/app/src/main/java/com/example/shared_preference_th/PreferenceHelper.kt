package com.example.shared_preference_th
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferenceHelper(context: Context) {
    private val sharedPref: SharedPreferences = context.getSharedPreferences("yeuquaimo", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveData(key: String, value: String) {
        val editor = sharedPref.edit()
        editor.putString(key, value)
        editor.apply()
    }
    fun getData(key: String): String? {
        return sharedPref.getString(key, null)
    }
    fun removeData(key: String) {
        val editor = sharedPref.edit()
        editor.remove(key)
        editor.apply()
    }
    fun saveList(key: String, list: List<String>) {
        val json = gson.toJson(list)
        val editor = sharedPref.edit()
        editor.putString(key, json)
        editor.apply()
    }
    fun getList(key: String): List<String>? {
        val json = sharedPref.getString(key, null) ?: return null
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }
    fun removeItemFromList(key: String, item: String) {
        val list = getList(key)?.toMutableList() ?: return
        list.remove(item)
        saveList(key, list)
    }
}