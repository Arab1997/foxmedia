package uz.napa.foxmedia.response.video


import com.google.gson.annotations.SerializedName

data class Video(
    val id: Long,
    @SerializedName("mediaid")
    val mediaId: String,
    @SerializedName("course_id")
    val courseId: Long,
    val image: String,
    val thumbnail: String,
    val name: String,
    val description: String,
    val type: String,
    val duration: Long,
    val rating: Int,
    @SerializedName("is_trial")
    val isTrial: Boolean,
    @SerializedName("is_archived")
    val isArchived: Boolean,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)