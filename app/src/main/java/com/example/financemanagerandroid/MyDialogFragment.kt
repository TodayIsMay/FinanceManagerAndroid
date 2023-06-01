package com.example.financemanagerandroid

import android.R
import android.R.attr.tag
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.example.financemanagerandroid.databinding.DialogFragmentBinding
import java.io.BufferedOutputStream
import java.net.HttpURLConnection
import java.net.URL


class MyDialogFragment : AppCompatActivity() {
    private lateinit var binding: DialogFragmentBinding

    private lateinit var expenseNameText: TextView
    private lateinit var expenseAmount: TextView
    private lateinit var expenseCategory: TextView
    private lateinit var insertButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DialogFragmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val login = getSharedPreferences("com.example.financemanagerandroid", Context.MODE_PRIVATE).getString("login", "")
        expenseNameText = binding.expenseNameText
        expenseAmount = binding.amountText
        expenseCategory = binding.categoryNumber

        insertButton = binding.insertButton

        insertButton.setOnClickListener {
            val comment = expenseNameText.text
            val amount = expenseAmount.text
            val category = expenseCategory.text
            val json = "{\n" +
                    "    \"amount\": ${amount},\n" +
                    "    \"comment\": \"${comment}\",\n" +
                    "    \"categoryId\": ${category}\n" +
                    "}"
            sendJson("http://92.53.124.44:8080/insert/$login", json)

            onBackPressed()
        }
    }

    private fun sendJson(path: String, message: String) {
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