package com.example.financemanagerandroid

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity() : AppCompatActivity() {
    private lateinit var helloText: TextView
    private lateinit var startButton: ImageButton
    private lateinit var listDB: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        helloText = findViewById(R.id.helloText)
        startButton = findViewById(R.id.startButton)
        listDB = findViewById(R.id.listDB)
    }

    fun onGetExpensesButtonClick(view: View) {
        val arrayOfIds = arrayOf("1", "2")
        val listOfPersons = mutableListOf<Map<String, String>>()
        val person1 = parseJson()
        val person2 = parseJson1()

        listOfPersons.add(0, person1)
        listOfPersons.add(1, person2)


        val adapter = SimpleAdapter(this, listOfPersons, R.layout.listview_item, arrayOf("amount", "comment"),
            intArrayOf(R.id.textPerson, R.id.textAchievement)
        )
        listDB.adapter = adapter
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

    private fun parseJson(): Map<String, String> {
        val json =
            "{\"amount\": 11.1,\"comment\": \"insert through Postman\",\"categoryId\": 1,\"date\": \"2023-05-21T18:15:21.53769\" }"
        val jsonObject = JSONObject(json)
        val map = mutableMapOf<String, String>()
        map["amount"] = jsonObject.getInt("amount").toString()
        map["comment"] = jsonObject.getString("comment")
        return map
    }

    private fun parseJson1(): Map<String, String> {
        val json =
            "{\"amount\": 12.0,\"comment\": \"testComment\",\"categoryId\": 1,\"date\": \"2023-05-21T18:15:21.53769\" }"
        val jsonObject = JSONObject(json)
        val map = mutableMapOf<String, String>()
        map["amount"] = jsonObject.getInt("amount").toString()
        map["comment"] = jsonObject.getString("comment")
        return map
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