package uz.napa.foxmedia.response.purchase


import com.google.gson.annotations.SerializedName

data class PurchaseResponse(
    val status: String,
    val complete: Int,
    @SerializedName("checkout_link")
    val checkoutLink: String
)