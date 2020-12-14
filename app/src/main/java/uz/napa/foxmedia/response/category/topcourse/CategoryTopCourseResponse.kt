package uz.napa.foxmedia.response.category.topcourse


import com.google.gson.annotations.SerializedName
import uz.napa.foxmedia.response.category.Category

data class CategoryTopCourseResponse(
    val status: String,
    val categories: List<Category>
)