package uz.napa.foxmedia.response.purchase

data class SubscriptionsTable(
    val status: String,
    val subscriptions: List<SubscriptionPlan>
)