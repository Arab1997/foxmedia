package uz.napa.foxmedia.response.sign_in


import com.google.gson.annotations.SerializedName
import uz.napa.foxmedia.response.sign_in.Role

data class User(
    val id: Int,
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
    val briefIntroduction: String,
    val balance: Int,
    @SerializedName("verified_at")
    val verifiedAt: String,
    @SerializedName("activated_at")
    val activatedAt: Any,
    @SerializedName("current_plan")
    val currentPlan: Any,
    @SerializedName("total_watch_time")
    val totalWatchTime: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    val role: Role
)