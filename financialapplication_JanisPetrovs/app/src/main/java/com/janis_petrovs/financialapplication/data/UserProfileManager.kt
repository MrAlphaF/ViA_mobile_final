package com.janis_petrovs.financialapplication.data

import android.content.Context
import android.content.SharedPreferences

data class ProfileData(
    val name: String,
    val email: String,
    val pictureUri: String?
)

class UserProfileManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_NAME = "key_name"
        const val KEY_EMAIL = "key_email"
        const val KEY_PICTURE_URI = "key_picture_uri"
    }

    fun saveProfile(name: String, email: String, pictureUri: String? = null) {
        val editor = prefs.edit()
        editor.putString(KEY_NAME, name)
        editor.putString(KEY_EMAIL, email)
        editor.putString(KEY_PICTURE_URI, pictureUri)
        editor.apply()
    }

    fun getProfile(): ProfileData {
        val name = prefs.getString(KEY_NAME, "Guest") ?: "Guest"
        val email = prefs.getString(KEY_EMAIL, "guest@email.com") ?: "guest@email.com"
        val pictureUri = prefs.getString(KEY_PICTURE_URI, null)
        return ProfileData(name, email, pictureUri)
    }
}