package uz.napa.foxmedia.response.purchase


import com.google.gson.annotations.SerializedName

data class SubscriptionDetailsResponse(
    val status: String,
    val resource: SubscriptionPlan,
    @SerializedName("subscription_type")
    val subscriptionType: String
)