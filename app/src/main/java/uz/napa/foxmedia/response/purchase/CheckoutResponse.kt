package uz.napa.foxmedia.response.purchase


import com.google.gson.annotations.SerializedName

data class CheckoutResponse(
    @SerializedName("checkout_url")
    val checkoutUrl: String,
    @SerializedName("merchant_id")
    val merchantId: String,
    val callbackUrl: String?,
    @SerializedName("service_id")
    val serviceId: Long?,
    val description: String?,
    @SerializedName("return_url")
    val returnUrl: String?,
    @SerializedName("min_amount")
    val minAmount: Int
)