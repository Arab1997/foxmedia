package uz.napa.foxmedia.util

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import kotlinx.android.synthetic.main.dialog_password.*
import uz.napa.foxmedia.R
import uz.napa.foxmedia.request.user.ChangePasswordRequest
import uz.napa.foxmedia.response.user.ChangePasswordResponse

class ChangePasswordDialog(
    context: Context,
    private val callback: ((ChangePasswordRequest) -> Unit)
) : AlertDialog(context) {

    init {
        setView(layoutInflater.inflate(R.layout.dialog_password, null, false))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        btn_save.isEnabled = false
        et_current_password.addTextChangedListener {
            btn_save.isEnabled = isPasswordValid()
        }
        et_new_password.addTextChangedListener {
            btn_save.isEnabled = isPasswordValid()
        }
        et_confirm_password.addTextChangedListener {
            btn_save.isEnabled = isPasswordValid()
        }
        btn_save.setOnClickListener {
            callback.invoke(
                ChangePasswordRequest(
                    et_current_password.text.toString(),
                    et_new_password.text.toString(),
                    et_confirm_password.text.toString()
                )
            )
            it.hideKeyboard()
            dismiss()
        }
    }

    private fun isPasswordValid() =
        et_current_password.text.toString().length >= 6 &&
                et_new_password.text.toString().length >= 6 &&
                et_confirm_password.text.toString().length >= 6 &&
                et_new_password.text.toString() == et_confirm_password.text.toString()
}