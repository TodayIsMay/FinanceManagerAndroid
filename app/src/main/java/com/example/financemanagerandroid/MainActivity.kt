package com.example.financemanagerandroid

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.financemanagerandroid.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.BufferedOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


class MainActivity() : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var prefs: SharedPreferences
    private lateinit var textView: TextView

    var stringLogin: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("com.example.financemanagerandroid", Context.MODE_PRIVATE)
        stringLogin = prefs.getString("login", "").toString()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textView = binding.textView
        if (stringLogin.isNullOrBlank()) {
            textView.text = "You're not logged in"
        } else {
            textView.text = "You're logged in as $stringLogin"
        }

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.expenses_option, R.id.version_option
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.login_option -> {
                logIn()
                true
            }
            R.id.logout_option -> {
                logOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.dots_menu, menu)
        return true
    }

    fun logIn() {
        val loginDialog = LoginDialog()
        val manager = supportFragmentManager
        loginDialog.show(manager, "myDialog")
    }

    fun logOut() {
        prefs.edit().remove("login").commit()
        stringLogin = prefs.getString("login", "").toString()
        textView.text = "You're not logged in"
    }

    fun okClicked(
        login: String,
        password: String
    ) {//TODO: отправлять эту штуку в метод-распределитель на бэке
        val id = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val message = "{\n" +
                "    \"login\": \"$login\",\n" +
                "    \"password\": \"$password\",\n" +
                "    \"deviceId\": \"$id\"\n" +
                "}"
        sendUser("http://92.53.124.44:8080/users/insert", message)
        stringLogin = login
        prefs.edit().remove("login").commit()
        prefs.edit().putString("login", stringLogin).commit()
        textView.text = "You're logged in as $stringLogin"
    }

    override fun onStop() {
        val mEditor: SharedPreferences.Editor = prefs.edit()
        mEditor.putString("login", stringLogin).commit()
        super.onStop()
    }

    @Throws(IOException::class)
    private fun sendUser(path: String, message: String) {
        Thread {
            var conn: HttpURLConnection? = null
            var os: BufferedOutputStream? = null
            try {
                val url = URL(path)

                conn = url.openConnection() as HttpURLConnection
                conn.setReadTimeout(10000 /*milliseconds*/)
                conn.setConnectTimeout(15000 /* milliseconds */)
                conn.setRequestMethod("POST")
                conn.setDoInput(true)
                conn.setDoOutput(true)
                conn.setFixedLengthStreamingMode(message.toByteArray().size);

                //make some HTTP header nicety
                conn.setRequestProperty("Content-Type", "application/json;charset=utf-8")
                conn.setRequestProperty("X-Requested-With", "XMLHttpRequest")

                //open
                conn.connect()

                //setup send
                os = BufferedOutputStream(conn.getOutputStream())
                os.write(message.toByteArray())
                //clean up
                os.flush()

                //TODO: do somehting with response
//        is = conn.getInputStream();
//        String contentAsString = readIt(is,len);
            } finally {
                //clean up
                if (os != null) {
                    os.close()
                };
                //is.close();
                if (conn != null) {
                    conn.disconnect()
                }
            }
        }.start()
    }
}