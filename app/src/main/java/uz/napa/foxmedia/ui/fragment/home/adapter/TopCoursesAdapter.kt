package uz.napa.foxmedia.ui.fragment.home.adapter

import android.graphics.Paint
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_top_course.view.*
import uz.napa.foxmedia.R
import uz.napa.foxmedia.response.course.Course
import uz.napa.foxmedia.util.Constants.Companion.BASE_URL
import uz.napa.foxmedia.util.getRating
import uz.napa.foxmedia.util.htmlFormat

class TopCoursesAdapter : RecyclerView.Adapter<TopCoursesAdapter.TopCourseVH>() {

    private val diffUtil = object : DiffUtil.ItemCallback<Course>() {
        override fun areItemsTheSame(oldItem: Course, newItem: Course) =
            oldItem.hashCode() == newItem.hashCode()

        override fun areContentsTheSame(oldItem: Course, newItem: Course) =
            oldItem.description == newItem.description
    }

    val differ = AsyncListDiffer(this, diffUtil)

    private var onClickListener: ((Course) -> Unit)? = null

    fun setOnClickListener(listener: (Course) -> Unit) {
        onClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopCourseVH {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_top_course, parent, false)
        return TopCourseVH(view)
    }

    override fun onBindViewHolder(holder: TopCourseVH, position: Int) {
        val course = differ.currentList[position]
        holder.itemView.apply {
            total_rating.rating = getRating(course.totalRating)
            tv_name.text = course.name
            if (course.description != null && course.description.isEmpty())
                tv_name.maxLines = 2
            tv_description.text = htmlFormat(course.description?:"")
            when {
                course.priceTable == null -> {
                    tv_price.isVisible = false
                    tv_sale_price.apply {
                        setTextColor(context.resources.getColor(R.color.colorPrimary))
                        text = context.getString(R.string.subscription)
                    }
                }
                course.priceTable.discount != null -> {
                    tv_sale_price.apply {
                        isVisible = true
                        text = context.getString(R.string.som, course.priceTable.saleprice)
                    }
                    tv_price.apply {
                        text = context.getString(R.string.som, course.priceTable.price)
                        paintFlags = tv_price.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    }
                }
                else -> {
                    tv_price.isVisible = false
                    tv_sale_price.apply {
                        setTextColor(context.resources.getColor(R.color.colorPrimary))
                        course.priceTable.price.toString()
                    }
                }
            }
            Glide.with(this).load(BASE_URL + course.thumbnail).into(img_thumbnail).clearOnDetach()
            img_thumbnail.transitionName = "image_target"
            setOnClickListener { onClickListener?.invoke(course) }
        }
    }


    inner class TopCourseVH(containerView: View) : RecyclerView.ViewHolder(containerView)

    override fun getItemCount() = differ.currentList.size
}
