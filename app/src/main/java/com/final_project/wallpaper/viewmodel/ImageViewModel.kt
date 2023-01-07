package com.example.finalproject.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.api.RedditApi
import com.example.finalproject.model.Image
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch

class ImageViewModel : ViewModel() {
    var images = MutableLiveData<ArrayList<Image>>()
    var isLoading = MutableLiveData<Boolean>()
    var currentSort = 0
    var query = ""
    var after = ""

    init {
        images.value = ArrayList()
        isLoading.value = false
    }

    @InternalCoroutinesApi
    fun getNextImages() {
        isLoading.value = true
        images.value = images.value

        viewModelScope.launch {
            RedditApi.loadImages(query, currentSort, after) { image, nextAfter, isLast ->
                if (isLast) {
                    isLoading.postValue(false)
                    images.postValue(images.value)
                    if (image.imgUrl.isEmpty()) {
                        return@loadImages
                    }
                }
                Log.d("tung",image.imgUrl)
                images.value?.add(image)
                images.postValue(images.value)
                after = nextAfter
            }
        }
    }

    fun clear() {
        images.value?.clear()
        images.value = images.value
        isLoading.value = false
    }

    @InternalCoroutinesApi
    fun setup(query: String, sort: Int) {
        this.query = query
        currentSort = sort
        images.value?.clear()
        images.value = images.value
        after = ""
        getNextImages()
    }
}
