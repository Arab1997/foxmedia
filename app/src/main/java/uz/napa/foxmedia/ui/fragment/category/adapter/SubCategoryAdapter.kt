package uz.napa.foxmedia.ui.fragment.category.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.recyclerview.widget.*
import kotlinx.android.synthetic.main.list_subcategory.view.*
import uz.napa.foxmedia.R
import uz.napa.foxmedia.ui.fragment.category.CategoryFragmentDirections

class SubCategoryAdapter :
    RecyclerView.Adapter<SubCategoryAdapter.SubcategoryVH>() {

    private val differCallback = object : DiffUtil.ItemCallback<SubCategoryTopCoursesModel>() {
        override fun areItemsTheSame(
            oldItem: SubCategoryTopCoursesModel,
            newItem: SubCategoryTopCoursesModel
        ): Boolean {
            return oldItem.subCategory.id == newItem.subCategory.id
        }

        override fun areContentsTheSame(
            oldItem: SubCategoryTopCoursesModel,
            newItem: SubCategoryTopCoursesModel
        ): Boolean {
            return oldItem.subCategory.name == newItem.subCategory.name
        }
    }

    val differ = AsyncListDiffer(this, differCallback)
    private var scrollPosition = 0
    private var viewPagerPosition = 0

    fun scrollPosition(position: Int, vpPosition: Int) {
        scrollPosition = position
        viewPagerPosition = vpPosition
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubcategoryVH {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_subcategory, parent, false)
        return SubcategoryVH(view)
    }

    override fun onBindViewHolder(holder: SubcategoryVH, position: Int) {
        val course = differ.currentList[position].courses
        val action = CategoryFragmentDirections.actionCategoryFragmentToSubcategoryFragment(
            differ.currentList[position].subCategory
        )
        holder.itemView.apply {
            if (course.isEmpty()) {
                rv_top_courses.isVisible = false
                layout_no_content.isVisible = true
                btn_see_all.setOnClickListener { findNavController().navigate(action) }
            } else {
                rv_top_courses.isVisible = true
                layout_no_content.isVisible = false
                val adapter = CategoryCourseAdapter()
                adapter.btnMoreClickListener { findNavController().navigate(action) }
                adapter.setOnCourseClickListener {
                    val direction =
                        CategoryFragmentDirections.actionCategoryFragmentToCourseFragment(it)
                    findNavController().navigate(direction)
                }
                adapter.differ.submitList(course)
                rv_top_courses.itemAnimator = DefaultItemAnimator()
                rv_top_courses.adapter = adapter
                if (viewPagerPosition == position)
                    (rv_top_courses.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                        scrollPosition,
                        0
                    )
            }

        }
    }

    inner class SubcategoryVH(containerView: View) : RecyclerView.ViewHolder(containerView)

    override fun getItemCount() = differ.currentList.size
}

class CatDiffUtilCall : DiffUtil.ItemCallback<SubCategoryTopCoursesModel>() {
    override fun areItemsTheSame(
        oldItem: SubCategoryTopCoursesModel,
        newItem: SubCategoryTopCoursesModel
    ) =
        oldItem.subCategory.id == newItem.subCategory.id

    override fun areContentsTheSame(
        oldItem: SubCategoryTopCoursesModel,
        newItem: SubCategoryTopCoursesModel
    ) =
        oldItem.subCategory.name == newItem.subCategory.name
}