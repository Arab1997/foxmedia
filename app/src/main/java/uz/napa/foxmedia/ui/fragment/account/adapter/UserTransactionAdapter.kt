package uz.napa.foxmedia.ui.fragment.account.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_user_transaction.view.*
import uz.napa.foxmedia.R
import uz.napa.foxmedia.response.user.transaction.Transaction

class UserTransactionAdapter : RecyclerView.Adapter<UserTransactionVH>() {

    private val differCallback = object : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(
            oldItem: Transaction,
            newItem: Transaction
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Transaction,
            newItem: Transaction
        ): Boolean {
            return oldItem.amount == newItem.amount
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserTransactionVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_transaction, parent, false)
        return UserTransactionVH(view)
    }

    override fun getItemCount() = differ.currentList.size
    override fun onBindViewHolder(holder: UserTransactionVH, position: Int) {
        val transaction = differ.currentList[position]
        holder.itemView.apply {
            transaction_method.text = transaction.method
            transaction_amount.text = context.getString(R.string.som, transaction.amount)
            tv_replenished_date.text = transaction.paymentTime
        }
    }

}

class UserTransactionVH(view: View) : RecyclerView.ViewHolder(view)