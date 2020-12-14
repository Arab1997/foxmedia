package uz.napa.foxmedia.response.video.comment


import com.google.gson.annotations.SerializedName

data class Comment(
    val id: Long,
    @SerializedName("user_id")
    val userId: Long,
    @SerializedName("video_id")
    val videoId: Long?,
    val rating: Int,
    val body: String,
    @SerializedName("is_approved")
    val isApproved: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    val user: User?,
    val replies: List<Any>?,
    var isChecked: Boolean = false
)