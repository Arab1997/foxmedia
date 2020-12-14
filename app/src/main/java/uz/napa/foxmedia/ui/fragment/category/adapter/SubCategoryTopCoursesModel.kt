package uz.napa.foxmedia.ui.fragment.category.adapter

import uz.napa.foxmedia.response.category.Subcategory
import uz.napa.foxmedia.response.course.Course

data class SubCategoryTopCoursesModel(
    val subCategory:Subcategory,
    val courses:ArrayList<Course>
)