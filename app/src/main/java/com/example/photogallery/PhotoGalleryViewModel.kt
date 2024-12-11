package com.example.photogallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.photogallery.api.FlickrFetchr


class PhotoGalleryViewModel(private val app: Application) : AndroidViewModel(app) {
    val galleryItemLiveData : LiveData<List<GalleryItem>>
    private val flickrFetchr = FlickrFetchr()
    private val mutableSearchTerm = MutableLiveData<String>()

    val searchTerm: String get() = mutableSearchTerm.value ?: ""


    init {
        mutableSearchTerm.value = QueryPreferences.getStoredQuery(app)
        galleryItemLiveData = mutableSearchTerm.switchMap() { searchTerm ->
            if (searchTerm.isBlank()) {
                flickrFetchr.fetchPhotos()
            } else {
                flickrFetchr.searchPhotos(searchTerm)
            }
        }
    }

    fun fetchPhotos(query: String = "") {
        QueryPreferences.setStoredQuery(app, query)
        mutableSearchTerm.value = query
    }
}