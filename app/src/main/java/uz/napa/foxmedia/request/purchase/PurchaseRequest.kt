package uz.napa.foxmedia.request.purchase

import com.google.gson.annotations.SerializedName

data class PurchaseRequest(
    @SerializedName("resource_id")
    val resourceId:Long,
    @SerializedName("subscription_type")
    val subscriptionType:String,
    @SerializedName("payment_method")
    val paymentMethod:String
)