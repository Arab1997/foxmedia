package uz.napa.foxmedia.ui.activity.video.adapter

import android.content.Context
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_file.view.*
import uz.napa.foxmedia.R
import uz.napa.foxmedia.response.video.files.Reference
import java.io.File

class FilesAdapter : RecyclerView.Adapter<FilesAdapter.FilesVH>() {
    private var clickListener: ((Reference) -> Unit)? = null

    fun setOnClickListener(listener: (Reference) -> Unit) {
        clickListener = listener
    }


    private val differCallback = object : DiffUtil.ItemCallback<Reference>() {
        override fun areItemsTheSame(oldItem: Reference, newItem: Reference): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Reference,
            newItem: Reference
        ): Boolean {
            return oldItem.name == newItem.name
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    fun setProgress(progress: Int, position: Int) {
        differ.currentList[position].percentage = progress
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilesVH {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_file, parent, false)
        return FilesVH(view)
    }

    override fun onBindViewHolder(holder: FilesVH, position: Int) {
        val reference = differ.currentList[position]
        holder.itemView.apply {
            file_name.text = reference.name

            setOnClickListener {
                clickListener?.invoke(reference)
            }
        }
    }

    private fun checkIfFileExist(reference: Reference) {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            reference.name
        )
        reference.isDownloaded = file.isFile
    }

    class FilesVH(containerView: View) : RecyclerView.ViewHolder(containerView)

    override fun getItemCount() = differ.currentList.size
}