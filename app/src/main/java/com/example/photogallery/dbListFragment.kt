package com.example.photogallery

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
private const val TAG = "dbListFragment"
class dbListFragment : Fragment() {
    private lateinit var thumbnailDownloader : ThumbnailDownloader<PhotoHolder>
    private lateinit var photoRecyclerView: RecyclerView
    private var adapter: PhotoAdapter? = PhotoAdapter(emptyList())
    private val photoListViewModel: dbListViewModel by lazy {
        ViewModelProvider(this).get(dbListViewModel::class.java)
    }
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        val responseHandler = Handler()
        thumbnailDownloader =
            ThumbnailDownloader(responseHandler) { photoHolder, bitmap ->
                val drawable = BitmapDrawable(resources, bitmap)
                photoHolder.bindDrawable(drawable)
            }
        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_photo_list, container, false)
        photoRecyclerView = view.findViewById(R.id.photo_recycler_view) as RecyclerView
        photoRecyclerView.layoutManager = LinearLayoutManager(context)
        photoRecyclerView.adapter = adapter
        viewLifecycleOwner.lifecycle.addObserver(thumbnailDownloader.viewLifecycleObserver)
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoListViewModel.photoListLiveData.observe(
            viewLifecycleOwner,
            Observer { galleryItems ->
                galleryItems?.let {
                    Log.i(TAG, "Got photos ${galleryItems.size}")
                    updateUI(galleryItems)
                }
            })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        thumbnailDownloader.clearQueue()
        viewLifecycleOwner.lifecycle.removeObserver(thumbnailDownloader.viewLifecycleObserver)
    }
    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(thumbnailDownloader.fragmentLifecycleObserver)
    }
    private fun updateUI(galleryItems: List<GalleryItem>) {
        adapter = PhotoAdapter(galleryItems)
        photoRecyclerView.adapter = adapter
    }
    private inner class PhotoHolder(view: View)
        : RecyclerView.ViewHolder(view) {
        private lateinit var galleryItem: GalleryItem
        val idTextView: TextView = itemView.findViewById(R.id.photo_id)
        val titleTextView: TextView = itemView.findViewById(R.id.photo_title)
        val urlTextView: TextView = itemView.findViewById(R.id.photo_url)
        val bindDrawable: (Drawable) -> Unit = itemView.findViewById<ImageView>(R.id.photo_drawable)::setImageDrawable
        fun bind(galleryItem: GalleryItem) {
            this.galleryItem = galleryItem
            idTextView.text = this.galleryItem.id
            titleTextView.text = this.galleryItem.title
            urlTextView.text = this.galleryItem.url
        }
    }
    private inner class PhotoAdapter(var galleryItems: List<GalleryItem>)
        : RecyclerView.Adapter<PhotoHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
            val view = layoutInflater.inflate(R.layout.list_item_photo, parent, false)
            return PhotoHolder(view)
        }
        override fun getItemCount() = galleryItems.size
        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            val galleryItem = galleryItems[position]
            holder.apply {
                holder.bind(galleryItem)
            }
            val placeholder: Drawable = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.bill_up_close
            ) ?: ColorDrawable()
            holder.bindDrawable(placeholder)
            thumbnailDownloader.queueThumbnail(holder, galleryItem.url)
        }
    }
    companion object {
        fun newInstance(): dbListFragment {
            return dbListFragment()
        }
    }
}