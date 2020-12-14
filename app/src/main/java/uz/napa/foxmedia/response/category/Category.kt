package uz.napa.foxmedia.response.category


import com.google.gson.annotations.SerializedName
import uz.napa.foxmedia.response.course.Course

data class Category(
    val id: Long,
    @SerializedName("parent_id")
    val parentId: Int,
    val name: String,
    val description: String,
    @SerializedName("subscription_type")
    val subscriptionType: String,
    val image: String,
    val thumbnail: String,
    val subcategories: List<Subcategory>?,
    val courses: List<Course>?
)