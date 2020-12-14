package uz.napa.foxmedia.response.course.subscription


import com.google.gson.annotations.SerializedName

data class CourseSubscriptionResponse(
    val status: String,
    val resource: Resource?,
    @SerializedName("subscription_type")
    val subscriptionType: String
)