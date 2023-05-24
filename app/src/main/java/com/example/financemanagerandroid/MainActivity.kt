package com.example.financemanagerandroid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity() : AppCompatActivity() {
    private lateinit var helloText: TextView
    private lateinit var startButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        helloText = findViewById(R.id.helloText)
        startButton = findViewById(R.id.startButton)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.expenses_option) {
            startActivity(Intent(this, Expenses::class.java))
            return true
        }
        return true
    }


    fun onStartButtonClick(view: View) {
        Thread(Runnable {
            try {
                val content =
                    getContent("http://92.53.124.44:8080/version");
                runOnUiThread {
                    helloText.text = content
                    helloText.visibility = View.VISIBLE
                }
            } catch (ex: IOException) {
                println(ex.message)
                Log.e("MayApp", "There was an IO error", ex)
            }
        }).start()
    }


    @Throws(IOException::class)
    private fun getContent(path: String): String? {
        var reader: BufferedReader? = null
        var stream: InputStream? = null
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(path)
            //connection = url.openConnection() as HttpsURLConnection
            connection = url.openConnection() as HttpURLConnection
            connection.setRequestMethod("GET")
            connection.setReadTimeout(10000)
            connection.connect()
            stream = connection.getInputStream()
            reader = BufferedReader(InputStreamReader(stream))
            val buf = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                buf.append(line).append("\n")
            }
            return buf.toString()
        } finally {
            if (reader != null) {
                reader.close()
            }
            if (stream != null) {
                stream.close()
            }
            if (connection != null) {
                connection.disconnect()
            }
        }
    }
}