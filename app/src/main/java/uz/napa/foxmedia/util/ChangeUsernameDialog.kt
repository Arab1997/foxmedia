package uz.napa.foxmedia.util

import android.content.Context
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import kotlinx.android.synthetic.main.dialog_username.*
import uz.napa.foxmedia.R
import uz.napa.foxmedia.request.user.UpdateUsernameRequest

class ChangeUsernameDialog(context: Context, private val func: ((UpdateUsernameRequest) -> Unit)) :
    AlertDialog(context) {

    init {
        setView(layoutInflater.inflate(R.layout.dialog_username, null, false))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        btn_save.isEnabled = false
        et_first_name.addTextChangedListener {
            it?.let { editable ->
                btn_save.isEnabled = isValid()
            }
        }
        et_last_name.addTextChangedListener {
            btn_save.isEnabled = isValid()
        }

        btn_save.setOnClickListener {
            func.invoke(
                UpdateUsernameRequest(
                    et_first_name.text.toString(),
                    et_last_name.text.toString()
                )
            )
            it.hideKeyboard()
            dismiss()
        }
    }

    private fun isValid() =
        et_first_name.text.toString().length >= 2 && et_last_name.text.toString().length >= 2
}