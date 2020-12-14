package uz.napa.foxmedia.receiver

interface MessageListener {
    /**
     * To call this method when new message received and send back
     * @param message Message
     */
    fun messageReceived(message: String?)
}