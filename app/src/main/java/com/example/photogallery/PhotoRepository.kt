package com.example.photogallery

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.example.photogallery.database.PhotoDatabase
import java.util.concurrent.Executors
private const val DATABASE_NAME = "photo-database"
class PhotoRepository private constructor(context: Context) {
    private val executor = Executors.newSingleThreadExecutor()
    private val database : PhotoDatabase = Room.databaseBuilder(
        context.applicationContext,
        PhotoDatabase::class.java,
        DATABASE_NAME
    ).build()
    private val photoDao = database.photoDao()
    fun getPhotos(): LiveData<List<GalleryItem>> = photoDao.getPhotos()
    fun getPhoto(id: String): LiveData<GalleryItem?> = photoDao.getPhoto(id)
    suspend fun delPhotos() {
        photoDao.delPhotos()
    }
    fun addPhoto(galleryItem: GalleryItem) {
        executor.execute {
            photoDao.addPhoto(galleryItem)
        }
    }
    companion object {
        private var INSTANCE: PhotoRepository? = null
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = PhotoRepository(context)
            }
        }
        fun get(): PhotoRepository {
            return INSTANCE ?: throw
            IllegalStateException("CrimeRepository must be initialized")
        }
    }
}