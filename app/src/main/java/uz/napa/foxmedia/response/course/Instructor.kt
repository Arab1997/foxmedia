package uz.napa.foxmedia.response.course

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Instructor(
    val id: Long,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    val image: String,
    @SerializedName("brief_introduction")
    val briefIntroduction: String
) : Parcelable