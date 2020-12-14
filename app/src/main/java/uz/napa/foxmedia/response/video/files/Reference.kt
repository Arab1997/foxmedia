package uz.napa.foxmedia.response.video.files


import com.google.gson.annotations.SerializedName

data class Reference(
    val id: Long,
    @SerializedName("video_id")
    val videoId: Long,
    @SerializedName("instructor_id")
    val instructorId: Long,
    val name: String,
    val notes: String?,
    val filename: String,
    val thumbnail: String?,
    val type: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    var isDownloaded:Boolean = false,
    var percentage:Int = 0
)