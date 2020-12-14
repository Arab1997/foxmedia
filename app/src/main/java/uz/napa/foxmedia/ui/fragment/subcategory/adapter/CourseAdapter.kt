package uz.napa.foxmedia.ui.fragment.subcategory.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.item_category_course.view.img_thumbnail
import kotlinx.android.synthetic.main.item_category_course.view.total_rating_course
import kotlinx.android.synthetic.main.item_category_course.view.tv_course_type
import kotlinx.android.synthetic.main.item_category_course.view.tv_name
import kotlinx.android.synthetic.main.item_course.view.*
import uz.napa.foxmedia.R
import uz.napa.foxmedia.response.course.Course
import uz.napa.foxmedia.ui.fragment.course.FUNDED
import uz.napa.foxmedia.util.Constants
import uz.napa.foxmedia.util.getRating

class CourseAdapter : RecyclerView.Adapter<CourseAdapter.CourseVH>() {
    private var clickCourseListener: ((Course) -> Unit)? = null

    fun setOnCourseClickListener(listener: (Course) -> Unit) {
        clickCourseListener = listener
    }

    private val differCallback = object : DiffUtil.ItemCallback<Course>() {
        override fun areItemsTheSame(oldItem: Course, newItem: Course): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Course, newItem: Course): Boolean {
            return oldItem.name == newItem.name
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseVH {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_course, parent, false)
        return CourseVH(view)
    }

    override fun onBindViewHolder(holder: CourseVH, position: Int) {
        val course = differ.currentList[position]
        holder.itemView.apply {
            tv_name.text = course.name
            total_rating_course.rating = getRating(course.totalRating)
            if (course.subscriptionType == FUNDED)
                tv_course_type.text = context.getString(R.string.subscription)
            else
                tv_course_type.text = context.getString(R.string.one_time_payment)

            Glide.with(this).load(Constants.BASE_URL + course.image)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .placeholder(R.drawable.placeholder)
                .into(img_thumbnail)
            setOnClickListener { clickCourseListener?.invoke(course) }
        }
    }

    inner class CourseVH(containerView: View) : RecyclerView.ViewHolder(containerView)

    override fun getItemCount() = differ.currentList.size
}