package uz.napa.foxmedia.response.user.transaction


import com.google.gson.annotations.SerializedName

data class Transaction(
    val id: Long,
    @SerializedName("user_id")
    val userId: Long,
    @SerializedName("payment_transaction_id")
    val paymentTransactionId: String,
    val method: String,
    val amount: Long,
    val state: Int,
    val reason: String?,
    @SerializedName("payment_time")
    val paymentTime: String,
    @SerializedName("create_time")
    val createTime: String,
    @SerializedName("perform_time")
    val performTime: String,
    @SerializedName("cancel_time")
    val cancelTime: String?,
    val receivers: String?
)