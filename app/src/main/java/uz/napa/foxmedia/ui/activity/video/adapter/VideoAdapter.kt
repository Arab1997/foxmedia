package uz.napa.foxmedia.ui.activity.video.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_video.view.*
import uz.napa.foxmedia.R
import uz.napa.foxmedia.response.course.videos.VideoInfo
import uz.napa.foxmedia.util.Constants
import uz.napa.foxmedia.util.formatDuration


class VideoAdapter : RecyclerView.Adapter<VideoAdapter.VideoVH>() {
    private var clickListener: ((VideoInfo) -> Unit)? = null
    private var selectedVideo = 0

    fun setOnClickListener(listener: (VideoInfo) -> Unit) {
        clickListener = listener
    }


    private val differCallback = object : DiffUtil.ItemCallback<VideoInfo>() {
        override fun areItemsTheSame(oldItem: VideoInfo, newItem: VideoInfo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: VideoInfo,
            newItem: VideoInfo
        ): Boolean {
            return oldItem.name == newItem.name
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoVH {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_video, parent, false)
        return VideoVH(view)
    }

    override fun onBindViewHolder(holder: VideoVH, position: Int) {
        val video = differ.currentList[position]
        holder.itemView.apply {
            if (position == selectedVideo) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    item_video_background.setBackgroundColor(context.getColor(R.color.selectedColor))
                } else
                    item_video_background.setBackgroundColor(context.resources.getColor(R.color.selectedColor))
            } else {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    item_video_background.setBackgroundColor(context.getColor(R.color.white))
                } else
                    item_video_background.setBackgroundColor(context.resources.getColor(R.color.white))
            }
            video_name.text = video.name
//            tv_is_free.isVisible = video.isTrial
            video_rating.rating = (video.rating / 20).toFloat()
            tv_video_duration.text = formatDuration(video.duration)
            Glide.with(this).load(Constants.BASE_URL + video.thumbnail)
                .placeholder(R.drawable.placeholder)
                .into(video_thumbnail)
            setOnClickListener {
                selectedVideo = position
                clickListener?.invoke(video)
                notifyDataSetChanged()
            }
        }
    }

    class VideoVH(containerView: View) : RecyclerView.ViewHolder(containerView)

    override fun getItemCount() = differ.currentList.size
}