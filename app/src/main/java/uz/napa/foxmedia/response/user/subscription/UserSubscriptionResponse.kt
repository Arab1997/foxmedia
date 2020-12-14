package uz.napa.foxmedia.response.user.subscription


import com.google.gson.annotations.SerializedName

data class UserSubscriptionResponse(
    val status: String,
    @SerializedName("user_subscriptions")
    val userSubscriptions: List<UserSubscription>
)