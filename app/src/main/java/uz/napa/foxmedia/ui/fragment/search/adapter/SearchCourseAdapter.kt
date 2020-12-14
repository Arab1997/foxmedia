package uz.napa.foxmedia.ui.fragment.search.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_category_course.view.*
import kotlinx.android.synthetic.main.item_search_course.view.*
import kotlinx.android.synthetic.main.item_search_course.view.img_thumbnail
import kotlinx.android.synthetic.main.item_search_course.view.total_rating_course
import kotlinx.android.synthetic.main.item_search_course.view.tv_course_type
import kotlinx.android.synthetic.main.item_search_course.view.tv_name
import uz.napa.foxmedia.R
import uz.napa.foxmedia.response.course.Course
import uz.napa.foxmedia.util.Constants.Companion.BASE_URL
import uz.napa.foxmedia.util.getRating

class SearchCourseAdapter : RecyclerView.Adapter<SearchCourseAdapter.SearchCourseVH>() {
    private var clickCourseListener: ((Course) -> Unit)? = null

    fun setOnCourseClickListener(listener: (Course) -> Unit) {
        clickCourseListener = listener
    }


    private val differCallback = object : DiffUtil.ItemCallback<Course>() {
        override fun areItemsTheSame(oldItem: Course, newItem: Course): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Course,
            newItem: Course
        ): Boolean {
            return oldItem.name == newItem.name
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchCourseVH {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_search_course, parent, false)
        return SearchCourseVH(view)
    }

    override fun onBindViewHolder(holder: SearchCourseVH, position: Int) {
        val course = differ.currentList[position]
        holder.itemView.apply {
            tv_name.text = course.name
            total_rating_course.rating = getRating(course.totalRating)
            if (course.subscriptionType == "FUNDED")
                tv_course_type.text = context.getString(R.string.subscription)
            else
                tv_course_type.text = context.getString(R.string.one_time_payment)
            Glide.with(this).load(BASE_URL + course.thumbnail).placeholder(R.drawable.placeholder)
                .into(img_thumbnail)
            setOnClickListener { clickCourseListener?.invoke(course) }
        }
    }

    inner class SearchCourseVH(containerView: View) : RecyclerView.ViewHolder(containerView)

    override fun getItemCount() = differ.currentList.size
}
