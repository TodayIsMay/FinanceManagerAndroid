package com.example.financemanagerandroid

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.Button
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.financemanagerandroid.databinding.FragmentExpensesBinding
import com.example.financemanagerandroid.utils.RequestsUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ExpensesFragment : Fragment() {
    private var requestUtils: RequestsUtils = RequestsUtils()

    private var _binding: FragmentExpensesBinding? = null

    private val binding get() = _binding!!

    private lateinit var getExpensesButton: Button
    private lateinit var insertExpensesButton: Button
    private lateinit var listDB: ListView
    private lateinit var balanceTextView: TextView

    @RequiresApi(Build.VERSION_CODES.O)
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
        balanceTextView = binding.balanceText
        this.registerForContextMenu(listDB)
        fillListDB(activity as MainActivity)

        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val act = activity as MainActivity
        getExpensesButton.setOnClickListener {
            if (act.stringLogin.isNullOrBlank()) {
                val toast = Toast(context)
                val toastView = layoutInflater.inflate(R.layout.login_toast_view, null)
                val text = toastView.findViewById<TextView>(R.id.login_toast_text)
                text.text = "You're not logged in!"
                toast.view = toastView
                toast.duration = Toast.LENGTH_SHORT
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
                listDB.visibility = View.INVISIBLE
            } else {
                fillListDB(act)
            }
        }

        insertExpensesButton.setOnClickListener {
            val intent = Intent(context, MyDialogFragment::class.java)
            startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        fillListDB(activity as MainActivity)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val act = activity
        val inflater: MenuInflater = act!!.menuInflater
        inflater.inflate(R.menu.context_menu, menu)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onContextItemSelected(item: MenuItem): Boolean {
        val act = activity as MainActivity
        val info = item.menuInfo as AdapterContextMenuInfo
        val transaction = listDB.getItemAtPosition(info.position)
        return when (item.itemId) {
            R.id.delete_option -> {
                transaction as LinkedHashMap<*, *>
                var transactionId = transaction["id"]
                Thread {
                    try {
                        requestUtils.sendDeleteRequest(
                            "http://92.53.124.44:8080/transactions/$transactionId",
                            act.stringLogin, act.password
                        )
                        fillListDB(act)
                    } catch (e: IOException) {
                        print("There ws an IO exception during deleting")
                    }
                }.start()
                true
            }

            else -> super.onContextItemSelected(item)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fillListDB(act: MainActivity) {
        Thread {
            try {
                val content =
                    getContent(
                        "http://92.53.124.44:8080/transactions/${act.stringLogin}",
                        act.stringLogin,
                        act.password
                    )!!
                activity?.runOnUiThread {
                    putRecordsInListView(content)
                }
                updateAvailableFundsView(act)
            } catch (ex: IOException) {
                println(ex.message)
                Log.e("MayApp", "There was an IO error", ex)
            }
        }.start()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun putRecordsInListView(content: String) {
        val listOfExpenses = parseJson(content)

        val adapter = SimpleAdapter(
            activity, listOfExpenses, R.layout.listview_item, arrayOf("amount", "category"),
            intArrayOf(R.id.textAmount, R.id.textCategory)
        )
        listDB.adapter = adapter
        listDB.visibility = View.VISIBLE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateAvailableFundsView(act: MainActivity) {
        val json = getContent(
            "http://92.53.124.44:8080/users/${act.stringLogin}",
            act.stringLogin,
            act.password
        )
        val jsonObject = JSONObject(json)
        activity?.runOnUiThread {
            balanceTextView.text = jsonObject.getDouble("availableFunds").toString();
        }
    }

    private fun parseJson(json: String): List<Map<String, String>> {
        val jsonArray = JSONArray(json)
        val mapList = mutableListOf<Map<String, String>>()

        for (i in 0 until jsonArray.length()) {
            val map = mutableMapOf<String, String>()
            map["id"] = jsonArray.getJSONObject(i).getLong("id").toString()
            map["amount"] = jsonArray.getJSONObject(i).getDouble("amount").toString()
            map["category"] = jsonArray.getJSONObject(i).getString("category")
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
            val encodedAuth =
                "Basic" + Base64.getEncoder().encodeToString(valueToEncode.toByteArray())
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
}