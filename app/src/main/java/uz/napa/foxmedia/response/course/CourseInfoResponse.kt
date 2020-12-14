package uz.napa.foxmedia.response.course

data class CourseInfoResponse(
    val status:String,
    val course:Course,
    val instructor: Instructor
)