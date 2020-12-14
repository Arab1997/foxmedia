package uz.napa.foxmedia.response.course.videos


import com.google.gson.annotations.SerializedName

data class Videos(
    @SerializedName("current_page")
    val currentPage: Int,
    val `data`: List<VideoInfo>,
    @SerializedName("first_page_url")
    val firstPageUrl: String,
    val from: Int,
    @SerializedName("last_page")
    val lastPage: Int,
    @SerializedName("last_page_url")
    val lastPageUrl: String,
    @SerializedName("next_page_url")
    val nextPageUrl: String?,
    val path: String,
    @SerializedName("per_page")
    val perPage: Int,
    @SerializedName("prev_page_url")
    val prevPageUrl: String?,
    val to: Int,
    val total: Int
)