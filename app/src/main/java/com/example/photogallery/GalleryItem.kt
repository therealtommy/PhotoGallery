package com.example.photogallery

import com.google.gson.annotations.SerializedName
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class GalleryItem(
    var title: String = "",
    @PrimaryKey var id: String = "",
    @SerializedName("url_s") var url: String = ""
)

