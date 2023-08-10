package com.example.financemanagerandroid

import android.R.attr.duration
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
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
    var password: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("com.example.financemanagerandroid", Context.MODE_PRIVATE)
        stringLogin = prefs.getString("login", "").toString()
        password = prefs.getString("password", "").toString()

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
                "    \"username\": \"$login\",\n" +
                "    \"password\": \"$password\"\n" +
                "}"
        sendUser("http://92.53.124.44:8080/registration", message, login, password)
        stringLogin = login
        this.password = password
        prefs.edit().remove("login").commit()
        prefs.edit().putString("login", stringLogin).commit()
        prefs.edit().remove("password").commit()
        prefs.edit().putString("password", this.password).commit()
        textView.text = "You're logged in as $stringLogin"
    }

    override fun onStop() {
        val mEditor: SharedPreferences.Editor = prefs.edit()
        mEditor.putString("login", stringLogin).commit()
        mEditor.putString("password", this.password).commit()
        super.onStop()
    }

    @Throws(IOException::class)
    private fun sendUser(path: String, message: String, login: String, password: String) {
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
                val ins = conn.inputStream;
                val inputAsString = ins.bufferedReader().use {it.readText() }
                showToast(inputAsString)
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

    fun showToast(response: String) {
        runOnUiThread {
            val toast = Toast(applicationContext)
            val toastView = layoutInflater.inflate(R.layout.login_toast_view, null)
            toast.view = toastView
            toast.duration = Toast.LENGTH_SHORT
            toast.setGravity(Gravity.TOP, 0, 0)
            toast.show()
        }
    }
}