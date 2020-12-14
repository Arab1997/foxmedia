package uz.napa.foxmedia.response.user.transaction

data class TransactionResponse(
    val status: String,
    val transactions: List<Transaction>
)