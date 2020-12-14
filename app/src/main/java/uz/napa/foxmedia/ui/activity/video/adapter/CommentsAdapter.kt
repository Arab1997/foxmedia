package uz.napa.foxmedia.ui.activity.video.adapter


import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_comment.view.*
import uz.napa.foxmedia.R
import uz.napa.foxmedia.response.video.comment.Comment
import uz.napa.foxmedia.util.Constants
import uz.napa.foxmedia.util.Constants.Companion.FIRST_NAME
import uz.napa.foxmedia.util.Constants.Companion.LAST_NAME
import uz.napa.foxmedia.util.Constants.Companion.USER_ID
import uz.napa.foxmedia.util.formatDate
import uz.napa.foxmedia.util.getRating


class CommentsAdapter : RecyclerView.Adapter<CommentsAdapter.CommentsVH>() {
    private var clickListener: ((Comment, Int) -> Unit)? = null
    private var longPressListener: ((Comment) -> Unit)? = null
    var selectedVideo = -1

    fun setOnClickListener(listener: (Comment, Int) -> Unit) {
        clickListener = listener
    }

    fun setOnLongClickListener(listener: (Comment) -> Unit) {
        longPressListener = listener
    }

    private val differCallback = object : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Comment,
            newItem: Comment
        ): Boolean {
            return oldItem.userId == newItem.userId
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    fun submitList(list: List<Comment>) {
        differ.submitList(null)
        differ.submitList(list.toList())
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsVH {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_comment, parent, false)
        return CommentsVH(view)
    }

    override fun onBindViewHolder(holder: CommentsVH, position: Int) {
        val comment = differ.currentList[position]
        holder.itemView.apply {
            if (selectedVideo == position) {
                comment_container.setCardBackgroundColor(context.resources.getColor(R.color.lightGrey))
            } else
                comment_container.setCardBackgroundColor(context.resources.getColor(R.color.milkColor))
            val pref = context.getSharedPreferences(
                Constants.PREF_NAME,
                Context.MODE_PRIVATE
            )
            val userId = pref.getLong(USER_ID, 0)
            if (comment.userId == userId) {
                tv_comment_user.setTextColor(resources.getColor(R.color.cardColor1))
            } else
                tv_comment_user.setTextColor(resources.getColor(R.color.colorPrimary))

            setOnClickListener {
                if (comment.userId == userId) {
                    if (comment.isChecked) {
                        selectedVideo = -1
                        comment.isChecked = false
                    } else {
                        if (selectedVideo != -1)
                            differ.currentList[selectedVideo].isChecked = false
                        selectedVideo = position
                        comment.isChecked = true
                    }
                    notifyDataSetChanged()
                    clickListener?.invoke(comment, position)
                } else
                    comment_container.isClickable = false
            }
            comment.user?.let {
                tv_comment_user.text =
                    context.getString(R.string.full_name, it.lastName, it.firstName)
            } ?: kotlin.run {
                if (comment.userId == userId) {
                    val firstName = pref.getString(FIRST_NAME, "")
                    val lastName = pref.getString(LAST_NAME, "")
                    tv_comment_user.text =
                        context.getString(R.string.full_name, lastName, firstName)
                }
            }
            tv_comment_time.text = formatDate(comment.createdAt)
            tv_comment_body.text = comment.body
            comment_rating.rating = getRating(comment.rating)
        }
    }


    class CommentsVH(containerView: View) : RecyclerView.ViewHolder(containerView)

    override fun getItemCount() = differ.currentList.size

}
