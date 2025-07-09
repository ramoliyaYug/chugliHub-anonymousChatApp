package com.example.chuglihub

import android.content.Context

object SharedPrefHelper {
    fun getUsername(context: Context): String {
        val pref = context.getSharedPreferences("anon", Context.MODE_PRIVATE)
        var username = pref.getString("name", null)
        if (username == null) {
            username = "Ghost#" + (1000..9999).random()
            pref.edit().putString("name", username).apply()
        }
        return username
    }
}