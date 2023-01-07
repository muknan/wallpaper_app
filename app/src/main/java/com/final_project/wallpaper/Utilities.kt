package com.example.finalproject

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Utilities {
    companion object {

        public fun getDate(): String {
            return SimpleDateFormat("MM-dd-yyyy 'at' HH:mm:ss", Locale.CANADA).format(Date());
        }

        public fun saveBitmap(bitmap: Bitmap, context: Context): String {
            val fName = (0..999999999).random().toString().replace(" ", "") + ".jpg"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val relativeLocation: String = Environment.DIRECTORY_PICTURES + File.separator + "RedditWalls"

                val contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fName)
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation)

                val resolver = context.contentResolver

                var stream: OutputStream? = null
                var uri: Uri? = null

                try {
                    val contentUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    uri = resolver.insert(contentUri, contentValues)
                    if (uri == null) {
                        throw IOException("Failed to create new MediaStore record.")
                    }
                    stream = resolver.openOutputStream(uri)
                    if (stream == null) {
                        throw IOException("Failed to get output stream.")
                    }
                    if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)) {
                        throw IOException("Failed to save bitmap.")
                    }

                } catch (e: IOException) {
                    if (uri != null) {
                        resolver.delete(uri, null, null)
                    }
                    throw e
                } finally {
                    stream?.close()
                }
            } else {
                val root = Environment.getExternalStorageDirectory().toString()
                val myDir = File("$root/RedditWalls")
                myDir.mkdirs()
                val file = File(myDir, fName)
                if (file.exists())
                    file.delete()
                try {
                    val out = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    out.flush()
                    out.close()
                    MediaStore.Images.Media.insertImage(context.contentResolver, file.absolutePath, file.name, file.name)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            return fName
        }

        suspend fun getSubInfo(subName: String): JSONObject {
            var subInfo = JSONObject()
            withContext(Dispatchers.Default) {
                val endpoint = "https://www.reddit.com/r/$subName/about/.json"
                val jsonString = async { getJsonData(endpoint) }
                try {
                    subInfo = JSONObject(jsonString.await())
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            return subInfo
        }

        suspend fun getJsonData(endpoint: String): String {
            var jsonString = ""
            withContext(Dispatchers.IO) {
                var urlConnection: HttpURLConnection? = null
                var reader: BufferedReader? = null
                try {
                    val requestURL = URL(endpoint)

                    urlConnection = requestURL.openConnection() as HttpURLConnection
                    urlConnection.requestMethod = "GET"
                    urlConnection.connect()

                    val inputStream = urlConnection.inputStream
                    reader = BufferedReader(InputStreamReader(inputStream))
                    val builder = StringBuilder()

                    var line: String? = reader.readLine()
                    while (line != null) {
                        if (!isActive) {
                            break
                        }

                        builder.append(line)
                        builder.append("\n")
                        line = reader.readLine()
                    }

                    if (!isActive) {
                        jsonString = ""
                    }

                    if (builder.isEmpty()) {
                        jsonString = ""
                    }

                    jsonString = builder.toString()

                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    urlConnection?.disconnect()
                    if (reader != null) {
                        try {
                            reader.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                    }
                }
            }

            return jsonString
        }
    }

}