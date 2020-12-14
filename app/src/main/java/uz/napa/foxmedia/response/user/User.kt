package uz.napa.foxmedia.response.user


import com.google.gson.annotations.SerializedName

data class User(
    val id: Long,
    @SerializedName("role_id")
    val roleId: Int,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("phone_number")
    val phoneNumber: Long,
    val image: String,
    val thumbnail: String,
    @SerializedName("brief_introduction")
    val briefIntroduction: String?,
    val balance: Long,
    @SerializedName("verified_at")
    val verifiedAt: String?,
    @SerializedName("activated_at")
    val activatedAt: String?,
    @SerializedName("current_plan")
    val currentPlan: String?,
    @SerializedName("total_watch_time")
    val totalWatchTime: Long,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)