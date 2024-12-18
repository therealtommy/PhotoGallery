package database
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.photogallery.GalleryItem

@Dao
interface PhotoDao {
    @Query("SELECT * FROM galleryitem")
    fun getPhotos(): LiveData<List<GalleryItem>>
    @Query("SELECT * FROM galleryitem WHERE id=(:id)")
    fun getPhoto(id: String): LiveData<GalleryItem?>
    @Query("DELETE FROM galleryitem")
    suspend fun delPhotos()
    @Insert
    fun addPhoto(galleryItem: GalleryItem)
}