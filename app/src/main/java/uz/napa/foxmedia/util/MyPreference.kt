package uz.napa.foxmedia.util

import android.content.Context

const val PREFERENCE_NAME = "FOXMEDIA_PREFERENCE"
const val PREFERENCE_LANGUAGE = "Language"

class MyPreference(context: Context) {


    private val preference = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    fun getLang(): String {
        return preference.getString(PREFERENCE_LANGUAGE, "oz")!!
    }

    fun setLang(Language: String, listener: () -> Unit) {
        val editor = preference.edit()
        editor.putString(PREFERENCE_LANGUAGE, Language)
        editor.apply()
        listener.invoke()
    }

}