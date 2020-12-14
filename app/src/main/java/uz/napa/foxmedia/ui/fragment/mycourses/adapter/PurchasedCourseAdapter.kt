package uz.napa.foxmedia.ui.fragment.mycourses.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_purchased_course.view.*
import uz.napa.foxmedia.R
import uz.napa.foxmedia.response.user.subscription.SubscriptionCourse
import uz.napa.foxmedia.response.user.subscription.UserSubscription

class PurchasedCourseAdapter : RecyclerView.Adapter<PurchasedCourseAdapter.PurchasedCourseVH>() {
    private var clickCourseListener: ((UserSubscription) -> Unit)? = null

    fun setOnCourseClickListener(listener: (UserSubscription) -> Unit) {
        clickCourseListener = listener
    }


    private val differCallback = object : DiffUtil.ItemCallback<UserSubscription>() {
        override fun areItemsTheSame(
            oldItem: UserSubscription,
            newItem: UserSubscription
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: UserSubscription,
            newItem: UserSubscription
        ): Boolean {
            return oldItem.courseId == newItem.courseId
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchasedCourseVH {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_purchased_course, parent, false)
        return PurchasedCourseVH(view)
    }

    override fun onBindViewHolder(holder: PurchasedCourseVH, position: Int) {
        val course = differ.currentList[position]
        holder.itemView.apply {
            purchased_course_name.text =
                course.course?.name ?: context.getString(R.string.course_info)
            purchased_time.text = context.getString(R.string.purchased_at, course.purchasedAt)
            img_purchased_course.setImageResource(R.drawable.placeholder)
            setOnClickListener { clickCourseListener?.invoke(course) }
        }
    }

    inner class PurchasedCourseVH(containerView: View) : RecyclerView.ViewHolder(containerView)

    override fun getItemCount() = differ.currentList.size
}