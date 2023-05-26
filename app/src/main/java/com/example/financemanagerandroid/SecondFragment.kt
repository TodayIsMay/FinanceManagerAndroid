package com.example.financemanagerandroid

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.financemanagerandroid.databinding.FragmentFirstBinding
import com.example.financemanagerandroid.databinding.FragmentSecondBinding
import org.json.JSONArray
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    private val binding get() = _binding!!

    private lateinit var getExpensesButton: Button
    private lateinit var listDB: ListView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        val root: View = binding.root

        getExpensesButton = binding.getExpensesButton
        listDB = binding.listDB

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getExpensesButton.setOnClickListener {
            Thread(Runnable {
                try {
                    val content =
                        getContent("http://92.53.124.44:8080/expenses")!!
                    activity?.runOnUiThread {
                        doSmth(content)
                    }
                } catch (ex: IOException) {
                    println(ex.message)
                    Log.e("MayApp", "There was an IO error", ex)
                }
            }).start()
        }
    }

    fun doSmth(content: String) {
        val arrayOfIds = arrayOf("1", "2")
        val listOfPersons = mutableListOf<Map<String, String>>()

        val person1 = parseJson(content)
        //val person2 = parseJson1()

        listOfPersons.add(0, person1)
        //listOfPersons.add(1, person2)


        val adapter = SimpleAdapter(
            activity, listOfPersons, R.layout.listview_item, arrayOf("amount", "comment"),
            intArrayOf(R.id.textPerson, R.id.textAchievement)
        )
        listDB.adapter = adapter
    }

    private fun parseJson(json: String): Map<String, String> {
        val jsonArray = JSONArray(json)
        val map = mutableMapOf<String, String>()
        map["amount"] = jsonArray.getJSONObject(0).getInt("amount").toString()
        map["comment"] = jsonArray.getJSONObject(0).getString("comment")
        return map
    }

    @Throws(IOException::class)
    private fun getContent(path: String): String? {
        var reader: BufferedReader? = null
        var stream: InputStream? = null
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(path)
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
}