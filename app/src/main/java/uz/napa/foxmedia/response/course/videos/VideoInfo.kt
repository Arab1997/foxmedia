package uz.napa.foxmedia.response.course.videos


import com.google.gson.annotations.SerializedName

data class VideoInfo(
    val id: Long,
    @SerializedName("mediaid")
    val mediaId: String,
    @SerializedName("course_id")
    val courseId: Long,
    val image: String,
    val thumbnail: String,
    val name: String,
    val duration: Long,
    val type:String,
    val rating: Int,
    @SerializedName("is_trial")
    val isTrial: Boolean,
    @SerializedName("is_archived")
    val isArchived: Boolean
)