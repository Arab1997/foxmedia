package uz.napa.foxmedia.response.user.subscription


import com.google.gson.annotations.SerializedName

data class UserSubscription(
    val id: Long,
    @SerializedName("user_id")
    val userId: Long,
    @SerializedName("course_id")
    val courseId: Long?,
    @SerializedName("subscription_id")
    val subscriptionId: Int,
    @SerializedName("subscription_type")
    val subscriptionType: String,
    val plan: String?,
    val subtotal: Long,
    val total: Long,
    val discount: Int,
    @SerializedName("purchased_at")
    val purchasedAt: String,
    @SerializedName("expires_at")
    val expiresAt: String,
    @SerializedName("renewed_at")
    val renewedAt: String?,
    val course: SubscriptionCourse?
)