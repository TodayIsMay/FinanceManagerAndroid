package com.example.financemanagerandroid.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64

class RequestsUtils {
    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(IOException::class)
    fun getContent(path: String, username: String, password: String): String? {
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendDeleteRequest(path: String, login: String, password: String) {
        var conn: HttpURLConnection? = null
        var os: BufferedOutputStream? = null
        try {
            val url = URL(path)

            conn = url.openConnection() as HttpURLConnection
            conn.setReadTimeout(10000 /*milliseconds*/)
            conn.setConnectTimeout(15000 /* milliseconds */)
            conn.setRequestMethod("DELETE")
            conn.setDoInput(true)
            conn.setDoOutput(true)
            //conn.setFixedLengthStreamingMode(message.toByteArray().size);

            val valueToEncode = "$login:$password"
            val encodedAuth =
                "Basic" + Base64.getEncoder().encodeToString(valueToEncode.toByteArray())
            //make some HTTP header nicety
            //conn.setRequestProperty("Content-Type", "application/json;charset=utf-8")
            conn.setRequestProperty("X-Requested-With", "XMLHttpRequest")
            conn.setRequestProperty("Authorization", encodedAuth)

            //open
            conn.connect()

            //setup send
            os = BufferedOutputStream(conn.outputStream)
            //os.write(message.toByteArray())
            //clean up
            os.flush()

            val inputs = conn.inputStream
            val newLine = System.getProperty("line.separator")
            val reader = BufferedReader(
                InputStreamReader(inputs)
            )
            val result = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (result.length > 0) {
                    result.append(newLine)
                }
                result.append(line)
            }
            println(result)

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
    }
}