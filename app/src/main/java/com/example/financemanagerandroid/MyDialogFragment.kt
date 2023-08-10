package com.example.financemanagerandroid

import android.R
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.financemanagerandroid.databinding.DialogFragmentBinding
import com.example.financemanagerandroid.entities.Category
import org.json.JSONArray
import java.io.BufferedOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64


class MyDialogFragment : AppCompatActivity() {
    private lateinit var binding: DialogFragmentBinding

    private lateinit var expenseNameText: TextView
    private lateinit var expenseAmount: TextView

    private lateinit var insertButton: Button
    private lateinit var categorySpinner: Spinner
    private lateinit var transactionTypeSpinner: Spinner

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DialogFragmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val insertExpenseModel = ViewModelProvider(this).get(InsertExpenseViewModel::class.java)

        val observer = Observer<String> { name ->
            if (!name.isNullOrEmpty()) {
                fillCategorySpinner(parseJson(name))
            }
        }
        insertExpenseModel.data.observe(this, observer)

        val login = getSharedPreferences(
            "com.example.financemanagerandroid",
            Context.MODE_PRIVATE
        ).getString("login", "")
        val password = getSharedPreferences(
            "com.example.financemanagerandroid",
            Context.MODE_PRIVATE
        ).getString("password", "")
        expenseNameText = binding.expenseNameText
        expenseAmount = binding.amountText
        categorySpinner = binding.spinner
        transactionTypeSpinner = binding.transactionTypeSpinner

        insertButton = binding.insertButton

        insertExpenseModel.loadCategoryData()
        fillTransactionTypeSpinner()

        insertButton.setOnClickListener {
            val comment = expenseNameText.text
            val amount = expenseAmount.text
            val category = categorySpinner.selectedItem as Category
            val transactionType = transactionTypeSpinner.selectedItem as TransactionType
            val json = "{\n" +
                    "    \"transactionType\": \"${transactionType}\", \n" +
                    "    \"amount\": ${amount},\n" +
                    "    \"comment\": \"${comment}\",\n" +
                    "    \"categoryId\": ${category.id}\n" +
                    "}"
            sendJson("http://92.53.124.44:8080/transactions/$login", json, login!!, password!!)

            onBackPressed()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendJson(path: String, message: String, login: String, password: String) {
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

                val valueToEncode = "$login:$password"
                val encodedAuth =
                    "Basic" + Base64.getEncoder().encodeToString(valueToEncode.toByteArray())
                //make some HTTP header nicety
                conn.setRequestProperty("Content-Type", "application/json;charset=utf-8")
                conn.setRequestProperty("X-Requested-With", "XMLHttpRequest")
                conn.setRequestProperty("Authorization", encodedAuth)

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

    private fun fillCategorySpinner(categories: List<Category>) {
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    private fun fillTransactionTypeSpinner() {
        val types = listOf(TransactionType.EXPENSE, TransactionType.INCOME)
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        transactionTypeSpinner.adapter = adapter
    }

    private fun parseJson(json: String): List<Category> {
        val jsonArray = JSONArray(json)
        val categories = mutableListOf<Category>()
        for (i in 0 until jsonArray.length()) {
            val id = jsonArray.getJSONObject(i).getLong("id")
            val name = jsonArray.getJSONObject(i).getString("name")
            val category = Category(id, name)
            categories.add(category)
        }
        return categories
    }
}