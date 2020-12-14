package uz.napa.foxmedia.response.course


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Price(
    val id: Long,
    @SerializedName("course_id")
    val courseId: Long,
    val price: Long,
    val saleprice: Long,
    val discount: Long?
):Parcelable