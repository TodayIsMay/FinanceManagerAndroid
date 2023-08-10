package com.example.financemanagerandroid

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class InsertExpenseViewModel: ViewModel() {

    val data: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun loadCategoryData() {
        Thread {
            data.postValue(getContent("http://92.53.124.44:8080/categories"))
        }.start()
    }

    private fun getContent(
        path: String
    ): String {
        val url = URL(path)
        (url.openConnection() as? HttpURLConnection)?.run {
            requestMethod = "GET"
            setRequestProperty("Content-Type", "application/json; utf-8")
            setRequestProperty("Accept", "application/json")
            return parseStream(inputStream)
        }
        return ""
    }

    fun parseStream(stream: InputStream):String {
        val reader = BufferedReader(InputStreamReader(stream))
        val buf = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            buf.append(line).append("\n")
        }
        return buf.toString()
    }
}