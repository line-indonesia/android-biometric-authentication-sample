package com.linecorp.id.research.biometricdemo.util

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.linecorp.id.research.biometricdemo.data.EncryptedMessage

object PreferenceUtil {
    fun storeEncryptedMessage(
        context: Context,
        prefKey: String,
        encryptedMessage: EncryptedMessage
    ) {
        val json = Gson().toJson(encryptedMessage)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(prefKey, json)
            .apply()
    }

    fun getEncryptedMessage(
        context: Context,
        prefKey: String
    ): EncryptedMessage? {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val json = pref.getString(prefKey, null)
        return Gson().fromJson(json, EncryptedMessage::class.java)
    }
}

