package uz.napa.foxmedia.response.course



data class TopCoursesResponse(
    val status: String,
    val courses: List<Course>
)