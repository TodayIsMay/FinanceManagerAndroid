//package com.example.financemanagerandroid
//
//import java.net.http.HttpResponse
//
//import android.os.AsyncTask
//
//
//internal class RequestTask : AsyncTask<String?, String?, String?>() {
//    protected override fun doInBackground(vararg uri: String): String? {
//        val httpclient: HttpClient = DefaultHttpClient()
//        val response: java.net.http.HttpResponse
//        var responseString: String? = null
//        try {
//            response = httpclient.execute(HttpGet(uri[0]))
//            val statusLine: StatusLine = response.getStatusLine()
//            if (statusLine.getStatusCode() === HttpStatus.SC_OK) {
//                val out = ByteArrayOutputStream()
//                response.getEntity().writeTo(out)
//                responseString = out.toString()
//                out.close()
//            } else {
//                //Closes the connection.
//                response.getEntity().getContent().close()
//                throw IOException(statusLine.getReasonPhrase())
//            }
//        } catch (e: ClientProtocolException) {
//            //TODO Handle problems..
//        } catch (e: IOException) {
//            //TODO Handle problems..
//        }
//        return responseString
//    }
//
//    override fun onPostExecute(result: String?) {
//        super.onPostExecute(result)
//        //Do anything with response..
//    }
//}