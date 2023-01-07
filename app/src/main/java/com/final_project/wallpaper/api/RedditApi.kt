package com.example.finalproject.api

import com.example.finalproject.model.Image
import com.example.finalproject.model.Subreddit


import android.util.Log
import com.example.finalproject.Utilities
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject

class RedditApi {


    companion object {
        const val HOT = 0
        const val NEW = 1
        const val TOP_WEEK = 2
        const val TOP_YEAR = 3
        const val TOP_ALL = 4

        //https://www.reddit.com/r/uwaterloo/top.json?sort=top&t=all
        @InternalCoroutinesApi
        suspend fun loadImages(query: String, sort: Int, after: String = "", onUpdate: (Image, String, Boolean) -> Unit) {
            withContext(Dispatchers.Default) {
                try {
                    var endpoint = "https://www.reddit.com/r/$query"

                    endpoint += when (sort) {
                        HOT -> "/hot.json?sort=hot"
                        NEW -> "/new.json?sort=new"
                        TOP_WEEK -> "/top.json?sort=top&t=week"
                        TOP_YEAR -> "/top.json?sort=top&t=year"
                        TOP_ALL -> "/top.json?sort=top&t=all"
                        else -> "/top.json?sort=top&t=all"
                    }

                    if (after.isNotEmpty()) {
                        endpoint += "&after=$after"
                    }

                    Log.e("ENDPOINT", endpoint)

                    val jsonString = async { Utilities.getJsonData(endpoint) }
                    var json = JSONObject(jsonString.await())
                    json = json.getJSONObject("data")
                    val nextAfter = json.getString("after")
                    val childrenArr = json.getJSONArray("children")
                    for (i in 0 until childrenArr.length()) {
                        if (!NonCancellable.isActive) {
                            break
                        }

                        val curr = childrenArr.getJSONObject(i)
                        val data = curr.getJSONObject("data")
                        if (!data.has("preview")) {
                            continue
                        }
                        val postLink = data.getString("permalink")
                        val preview = data.getJSONObject("preview")
                        val image = preview.getJSONArray("images").getJSONObject(0)
                        val source: JSONObject
                        source = image.getJSONObject("source")
                        val url = source.getString("url").replace("amp;".toRegex(), "")
                        val resolutions = image.getJSONArray("resolutions")
                        val previewUrl = resolutions.getJSONObject(0).getString("url").replace("amp;".toRegex(), "")
                        withContext(Dispatchers.Main) {
                            val currentImage = Image(imgUrl = url, postLink = "https://www.reddit.com$postLink", subName = query, previewUrl = previewUrl)
                            onUpdate(currentImage, nextAfter, false)
                        }
                    }
                    onUpdate(Image(), nextAfter, true)
                } catch (e: Exception) {
                    Log.e("Error", e.message ?: "")
                }
            }
        }
    }
}