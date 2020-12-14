package uz.napa.foxmedia.db

import androidx.room.*
import uz.napa.foxmedia.response.course.Course

@Dao
interface DatabaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course): Long

    @Query("SELECT * FROM courses")
    suspend fun getAllCourses(): List<Course>

    @Delete
    suspend fun deleteCourse(course: Course)

    @Query("DELETE FROM courses")
    suspend fun nukeTable()
}