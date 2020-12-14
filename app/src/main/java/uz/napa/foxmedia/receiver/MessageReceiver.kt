package uz.napa.foxmedia.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log


class MessageReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("MyLog","Broadcast receiver")
        val data = intent.extras
        val pdus = data!!["pdus"] as Array<Any>?
        for (i in pdus!!.indices) {
            val smsMessage =
                SmsMessage.createFromPdu(pdus[i] as ByteArray)
            val message = ("Sender : " + smsMessage.displayOriginatingAddress
                    + "Email From: " + smsMessage.emailFrom
                    + "Emal Body: " + smsMessage.emailBody
                    + "Display message body: " + smsMessage.displayMessageBody
                    + "Time in millisecond: " + smsMessage.timestampMillis
                    + "Message: " + smsMessage.messageBody)
//            Log.d("MyLog","Broadcast receiver for $mListener $message")
            mListener.messageReceived(smsMessage.displayMessageBody)
        }
    }

    companion object {
        private lateinit var mListener: MessageListener
        fun bindListener(listener: MessageListener) {
            mListener = listener
            Log.d("MyLog","bind listener $listener $mListener")
        }

        fun getListener() = mListener
    }
}