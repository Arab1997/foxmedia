package uz.napa.foxmedia.response.search


import com.google.gson.annotations.SerializedName
import uz.napa.foxmedia.response.course.Course

data class Results(
    @SerializedName("current_page")
    val currentPage: Int,
    val `data`: ArrayList<Course>,
    @SerializedName("first_page_url")
    val firstPageUrl: String,
    val from: Any,
    @SerializedName("last_page")
    val lastPage: Int,
    @SerializedName("last_page_url")
    val lastPageUrl: String,
    @SerializedName("next_page_url")
    val nextPageUrl: Any,
    val path: String,
    @SerializedName("per_page")
    val perPage: Int,
    @SerializedName("prev_page_url")
    val prevPageUrl: Any,
    val to: Any,
    val total: Int
)