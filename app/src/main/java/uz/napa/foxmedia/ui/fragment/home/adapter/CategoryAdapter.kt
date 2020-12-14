package uz.napa.foxmedia.ui.fragment.home.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_category.view.*
import uz.napa.foxmedia.R
import uz.napa.foxmedia.response.category.Category
import uz.napa.foxmedia.util.Constants.Companion.BASE_URL
import java.util.*

class CategoryAdapter : ListAdapter<Category, CategoryAdapter.CategoryVH>(DiffUtilCategory()) {
    private val colors = arrayListOf(R.color.cardColor1, R.color.cardColor2, R.color.cardColor3)
    private var clickListener: ((Category) -> Unit)? = null

    fun setOnClickListener(listener: (Category) -> Unit) {
        clickListener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryVH {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryVH(view)
    }

    override fun onBindViewHolder(holder: CategoryVH, position: Int) {
        val category = getItem(position)
        currentList
        holder.itemView.apply {
            category_card.setCardBackgroundColor(resources.getColor(colors[position % 3]))
            tv_category_title.text = category.name
            tv_category_description.text = category.description
            Glide.with(this)
                .load(BASE_URL + category.thumbnail)
                .placeholder(R.drawable.logo)
                .into(img_category_thumbnail)
            setOnClickListener { clickListener?.let { it.invoke(category) } }
        }
    }

    inner class CategoryVH(containerView: View) : RecyclerView.ViewHolder(containerView)

}

class DiffUtilCategory : DiffUtil.ItemCallback<Category>() {
    override fun areItemsTheSame(oldItem: Category, newItem: Category) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Category, newItem: Category) =
        oldItem.name == newItem.name

}