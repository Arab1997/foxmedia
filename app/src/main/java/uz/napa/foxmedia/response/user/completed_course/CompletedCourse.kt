package uz.napa.foxmedia.response.user.completed_course


import com.google.gson.annotations.SerializedName

data class CompletedCourse(
    val id: Long,
    val name: String,
    val image: String,
    val thumbnail: String,
    @SerializedName("completed_at")
    val completedAt: String
)