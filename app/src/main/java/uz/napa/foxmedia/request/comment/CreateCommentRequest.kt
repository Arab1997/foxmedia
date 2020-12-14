package uz.napa.foxmedia.request.comment

import com.google.gson.annotations.SerializedName

data class CreateCommentRequest(
    @SerializedName("video_id")
    val videoId: String,
    val rating: Int,
    val body: String
)