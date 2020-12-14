package uz.napa.foxmedia.ui.activity

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import uz.napa.foxmedia.R
import uz.napa.foxmedia.receiver.MessageListener
import uz.napa.foxmedia.util.MyContextWrapper
import uz.napa.foxmedia.util.MyPreference

class RegisterActivity:AppCompatActivity(){
    private lateinit var myPref: MyPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
    }

    override fun attachBaseContext(newBase: Context?) {
        myPref = MyPreference(newBase!!)
        val lang = myPref.getLang()
        super.attachBaseContext(MyContextWrapper.wrap(newBase, lang))
    }
}