package uz.napa.foxmedia.response.video


import com.google.gson.annotations.SerializedName

data class VideoInfoResponse(
    val status: String,
    val video: Video?,
    val message: String?,
    @SerializedName("video_link")
    val videoLink: String
)