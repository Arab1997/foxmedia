package uz.napa.foxmedia

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import uz.napa.foxmedia.util.MyContextWrapper
import uz.napa.foxmedia.util.MyPreference

class MainActivity : AppCompatActivity() {
    private lateinit var myPref: MyPreference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)
    }

    override fun attachBaseContext(newBase: Context?) {
        myPref = MyPreference(newBase!!)
        val lang = myPref.getLang()
        super.attachBaseContext(MyContextWrapper.wrap(newBase, lang))
    }



}
