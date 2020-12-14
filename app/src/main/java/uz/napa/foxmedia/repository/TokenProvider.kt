package uz.napa.foxmedia.repository

import android.content.Context
import uz.napa.foxmedia.util.Constants.Companion.PREF_NAME
import uz.napa.foxmedia.util.Constants.Companion.TOKEN

class TokenProvider {
    companion object {
        private var token: String? = null
        fun getToken(context: Context): String {
            if (token == null) {
                token =
                    context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(TOKEN,"")
            }
            return token!!
        }

        fun resetToken() {
            token = null
        }
    }
}