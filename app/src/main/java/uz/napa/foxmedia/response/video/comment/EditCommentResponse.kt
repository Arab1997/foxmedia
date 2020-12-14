package uz.napa.foxmedia.response.video.comment


import com.google.gson.annotations.SerializedName

data class EditCommentResponse(
    val status: String,
    val comment: Comment
)