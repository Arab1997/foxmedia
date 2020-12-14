package uz.napa.foxmedia.response.course


import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "courses")
@Parcelize
data class Course(
    @PrimaryKey
    val id: Long,
    @SerializedName("category_id")
    val categoryId: Long,
    val name: String,
    val description: String?,
    val image: String?,
    val thumbnail: String?,
    @SerializedName("total_duration")
    val totalDuration: Long,
    @SerializedName("total_rating")
    val totalRating: Int,
    @SerializedName("videos_count")
    val videosCount: Int,
    @SerializedName("is_best_seller")
    val isBestSeller: Boolean,
    @SerializedName("is_recommended")
    val isRecommended: Boolean,
    @SerializedName("subscription_type")
    val subscriptionType: String,
    @SerializedName("instructor_id")
    val instructorId:Long?,
    @SerializedName("instructor_name")
    val instructorName:String?,
    @SerializedName("short_description")
    val shortDescription:String?,
    @SerializedName("is_archived")
    val isArchived:Boolean?,
    @Embedded(prefix = "price_")
    @SerializedName("price_table")
    val priceTable: Price?
) : Parcelable