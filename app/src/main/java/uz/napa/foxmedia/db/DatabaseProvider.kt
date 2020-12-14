package uz.napa.foxmedia.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import uz.napa.foxmedia.response.course.Course

@Database(
    entities = [Course::class],
    version = 1
)
abstract class DatabaseProvider: RoomDatabase(){
    abstract fun getDatabaseDao():DatabaseDao

    companion object {
        @Volatile
        private var instance: DatabaseProvider? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also { instance = it }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                DatabaseProvider::class.java,
                "fox_media_db.db"
            ).build()
    }
}