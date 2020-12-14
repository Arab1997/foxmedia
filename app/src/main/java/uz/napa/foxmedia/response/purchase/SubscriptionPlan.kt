package uz.napa.foxmedia.response.purchase



data class SubscriptionPlan(
    val id: Long,
    val plan: String,
    val period: Int?,
    val price: Int,
    val saleprice: Int,
    val discount: Int
)