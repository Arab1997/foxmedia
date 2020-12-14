package uz.napa.foxmedia.ui.fragment.account.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_user_subscription.view.*
import uz.napa.foxmedia.R
import uz.napa.foxmedia.response.course.Course
import uz.napa.foxmedia.response.user.subscription.UserSubscription
import uz.napa.foxmedia.ui.fragment.course.ONE_TIME

class UserSubscriptionsAdapter : RecyclerView.Adapter<UserSubscriptionVH>() {

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
            return oldItem.plan == newItem.plan
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserSubscriptionVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_subscription, parent, false)
        return UserSubscriptionVH(view)
    }

    override fun getItemCount() = differ.currentList.size
    override fun onBindViewHolder(holder: UserSubscriptionVH, position: Int) {
        val subscription = differ.currentList[position]
        holder.itemView.apply {
            tv_subscription_name.text = subscription.plan
            tv_expire_date.text = subscription.expiresAt
            tv_subscription_price.text = context.getString(R.string.som, subscription.total)
        }
    }

}

class UserSubscriptionVH(view: View) : RecyclerView.ViewHolder(view) {

}
