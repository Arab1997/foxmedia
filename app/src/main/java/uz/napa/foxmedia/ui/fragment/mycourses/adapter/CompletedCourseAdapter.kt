package uz.napa.foxmedia.ui.fragment.mycourses.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_purchased_course.view.*
import uz.napa.foxmedia.R
import uz.napa.foxmedia.response.user.completed_course.CompletedCourse
import uz.napa.foxmedia.util.Constants.Companion.BASE_URL

class CompletedCourseAdapter : RecyclerView.Adapter<CompletedCourseAdapter.CompletedCourseVH>() {
    private var clickCourseListener: ((CompletedCourse) -> Unit)? = null

    fun setOnCourseClickListener(listener: (CompletedCourse) -> Unit) {
        clickCourseListener = listener
    }


    private val differCallback = object : DiffUtil.ItemCallback<CompletedCourse>() {
        override fun areItemsTheSame(oldItem: CompletedCourse, newItem: CompletedCourse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: CompletedCourse,
            newItem: CompletedCourse
        ): Boolean {
            return oldItem.name == newItem.name
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompletedCourseVH {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_purchased_course, parent, false)
        return CompletedCourseVH(view)
    }

    override fun onBindViewHolder(holder: CompletedCourseVH, position: Int) {
        val course = differ.currentList[position]
        holder.itemView.apply {
            purchased_course_name.text = course.name
            purchased_time.text = context.getString(R.string.finished, course.completedAt)
            Glide.with(this).load(BASE_URL + course.thumbnail).placeholder(R.drawable.placeholder)
                .into(img_purchased_course)
            setOnClickListener { clickCourseListener?.invoke(course) }
        }
    }

    inner class CompletedCourseVH(containerView: View) : RecyclerView.ViewHolder(containerView)

    override fun getItemCount() = differ.currentList.size
}
