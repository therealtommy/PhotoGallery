package com.example.photogallery.database
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.photogallery.GalleryItem
import database.PhotoDao

@Database(entities = [ GalleryItem::class ], version=1)
abstract class PhotoDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
}