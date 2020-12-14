package uz.napa.foxmedia.response.user.completed_course


import com.google.gson.annotations.SerializedName

data class CompletedCourseResponse(
    val status: String,
    @SerializedName("completed_courses")
    val completedCourses: List<CompletedCourse>
)