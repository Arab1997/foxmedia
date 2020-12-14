package uz.napa.foxmedia.response.category


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Subcategory(
    val id: Long,
    @SerializedName("parent_id")
    val parentId: Long,
    val name: String,
    val description: String?,
    @SerializedName("subscription_type")
    val subscriptionType: String,
    val image: String?,
    val thumbnail: String?
):Parcelable