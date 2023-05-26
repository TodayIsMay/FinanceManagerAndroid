package com.example.financemanagerandroid

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.financemanagerandroid.databinding.FragmentFirstBinding
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class VersionFragment : Fragment() {
    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private lateinit var helloText: TextView
    private lateinit var startButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        val root: View = binding.root

        helloText = binding.helloText

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        startButton = view.findViewById(R.id.startButton)
        startButton.setOnClickListener {
            if (helloText.visibility == View.VISIBLE) {
                helloText.visibility = View.INVISIBLE
            } else {
                Thread {
                    try {
                        val content =
                            getContent("http://92.53.124.44:8080/version");
                        activity?.runOnUiThread {
                            helloText.text = content
                            helloText.visibility = View.VISIBLE
                        }
                    } catch (ex: IOException) {
                        println(ex.message)
                        Log.e("MayApp", "There was an IO error", ex)
                    }
                }.start()
            }
        }
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