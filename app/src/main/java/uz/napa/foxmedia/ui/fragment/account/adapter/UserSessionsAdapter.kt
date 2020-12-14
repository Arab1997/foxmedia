package uz.napa.foxmedia.ui.fragment.account.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_user_session.view.*
import kotlinx.android.synthetic.main.item_user_subscription.view.*
import uz.napa.foxmedia.R
import uz.napa.foxmedia.response.course.Course
import uz.napa.foxmedia.response.sign_in.Session
import uz.napa.foxmedia.response.user.session.UserSession
import uz.napa.foxmedia.response.user.subscription.UserSubscription
import uz.napa.foxmedia.ui.fragment.course.ONE_TIME
import java.text.SimpleDateFormat
import java.util.*

class UserSessionsAdapter : RecyclerView.Adapter<UserSessionsVH>() {

    private val differCallback = object : DiffUtil.ItemCallback<UserSession>() {
        override fun areItemsTheSame(
            oldItem: UserSession,
            newItem: UserSession
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: UserSession,
            newItem: UserSession
        ): Boolean {
            return oldItem.ip == newItem.ip
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserSessionsVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_session, parent, false)
        return UserSessionsVH(view)
    }

    override fun getItemCount() = differ.currentList.size
    override fun onBindViewHolder(holder: UserSessionsVH, position: Int) {
        val session = differ.currentList[position]
        holder.itemView.apply {
            if (session.deviceType == "DESKTOP")
                img_session.setImageResource(R.drawable.ic_desktop)
            else
                img_session.setImageResource(R.drawable.ic_phone)
            tv_ip.text = session.ip
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(session.signedInTime)
            tv_enter_time.text =
                SimpleDateFormat("dd.MM.YYYY", Locale.getDefault()).format(date!!)
        }
    }

}

class UserSessionsVH(view: View) : RecyclerView.ViewHolder(view) {

}
