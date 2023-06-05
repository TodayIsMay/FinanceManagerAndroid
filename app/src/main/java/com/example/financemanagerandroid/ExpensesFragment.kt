package com.example.financemanagerandroid

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.financemanagerandroid.databinding.FragmentExpensesBinding
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.sql.Connection
import java.util.Base64

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ExpensesFragment : Fragment() {

    private var _binding: FragmentExpensesBinding? = null

    private val binding get() = _binding!!

    private lateinit var getExpensesButton: Button
    private lateinit var insertExpensesButton: Button
    private lateinit var listDB: ListView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        getExpensesButton = binding.getExpensesButton
        insertExpensesButton = binding.insertExpenseButton
        listDB = binding.listDB

        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val act = activity as MainActivity
        getExpensesButton.setOnClickListener {
            if (act.stringLogin.isNullOrBlank()) {
                val toast = Toast.makeText(activity, "You're not logged in!", Toast.LENGTH_LONG)
                toast.setGravity(Gravity.TOP, 0, 0)
                toast.show()
                listDB.visibility = View.INVISIBLE
            } else {
                Thread {
                    try {
                        val content =
                            getContent(
                                "http://92.53.124.44:8080/expenses/user/${act.stringLogin}",
                                act.stringLogin,
                                act.password
                            )!!
                        activity?.runOnUiThread {
                            putRecordsInListView(content)
                        }
                    } catch (ex: IOException) {
                        println(ex.message)
                        Log.e("MayApp", "There was an IO error", ex)
                    }
                }.start()
            }
        }

        insertExpensesButton.setOnClickListener {
            val intent = Intent(context, MyDialogFragment::class.java)
            startActivity(intent)
        }
    }

    private fun putRecordsInListView(content: String) {
        val listOfExpenses = parseJson(content)

        val adapter = SimpleAdapter(
            activity, listOfExpenses, R.layout.listview_item, arrayOf("amount", "comment"),
            intArrayOf(R.id.textPerson, R.id.textAchievement)
        )
        listDB.adapter = adapter
        listDB.visibility = View.VISIBLE
    }

    private fun parseJson(json: String): List<Map<String, String>> {
        val jsonArray = JSONArray(json)
        val mapList = mutableListOf<Map<String, String>>()
        for (i in 0 until jsonArray.length()) {
            val map = mutableMapOf<String, String>()
            map["amount"] = jsonArray.getJSONObject(i).getDouble("amount").toString()
            map["comment"] = jsonArray.getJSONObject(i).getString("comment")
            mapList.add(map)
        }
        return mapList
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(IOException::class)
    private fun getContent(path: String, username: String, password: String): String? {
        var reader: BufferedReader? = null
        var stream: InputStream? = null
        var connection: HttpURLConnection? = null
        return try {
            val valueToEncode = "$username:$password"
            val encodedAuth = "Basic" + Base64.getEncoder().encodeToString(valueToEncode.toByteArray())
            val url = URL(path)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.readTimeout = 10000
            connection.setRequestProperty("Authorization", encodedAuth)
            connection.connect()

            stream = connection.inputStream
            reader = BufferedReader(InputStreamReader(stream))
            val buf = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                buf.append(line).append("\n")
            }
            buf.toString()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun showToast(response: String) {
        Thread {
            val toast = Toast.makeText(activity, response, Toast.LENGTH_LONG)
            toast.setGravity(Gravity.TOP, 0, 0)
            toast.show()
        }
    }
}