package uz.napa.foxmedia.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import uz.napa.foxmedia.util.Constants.Companion.PREF_NAME
import uz.napa.foxmedia.util.Constants.Companion.SESSION_ID
class SplashActivity:Activity() {
    private lateinit var pref: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pref = getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE)
        check()
    }

    private fun check() {
        val  sessionId = pref.getString(SESSION_ID,null)
        if (sessionId == null){
            val intent = Intent(this,
                RegisterActivity::class.java)
            startActivity(intent)
        }else{
            val intent = Intent(this,
                MainActivity::class.java)
            startActivity(intent)
        }
        finish()
    }
}