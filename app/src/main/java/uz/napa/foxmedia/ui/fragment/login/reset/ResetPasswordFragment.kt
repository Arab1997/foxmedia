package uz.napa.foxmedia.ui.fragment.login.reset

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_reset_password.*
import kotlinx.android.synthetic.main.fragment_reset_password.et_confirm_password
import kotlinx.android.synthetic.main.fragment_reset_password.et_password
import kotlinx.android.synthetic.main.fragment_reset_password.til_confirm_password
import kotlinx.android.synthetic.main.fragment_reset_password.til_password
import uz.napa.foxmedia.R
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.request.register.ResetPasswordRequest
import uz.napa.foxmedia.util.Constants.Companion.USER_ID
import uz.napa.foxmedia.util.Resource
import uz.napa.foxmedia.util.getViewModelFactory
import uz.napa.foxmedia.util.snackbar

class ResetPasswordFragment : Fragment(R.layout.fragment_reset_password) {

    private val resetVM by viewModels<ResetPasswordVM> { getViewModelFactory(MyRepository()) }
    private val args by navArgs<ResetPasswordFragmentArgs>()
    private lateinit var tempPassword: String
    private var userId: Long = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        validate()
        val pref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        userId = pref.getLong(USER_ID, 0)
        tempPassword = args.tempPassword

        resetVM.resetPasswordLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                    btn_reset.startLoading()
                }
                is Resource.Error -> {
                    btn_reset.loadingFailed()
                    snackbar(it.message!!)
                }
                is Resource.Success -> {
                    btn_reset.loadingSuccessful()
                    findNavController().navigate(R.id.action_resetPasswordFragment_to_registerFragment)
                }
            }
        })

        setUpListeners()

        btn_reset.setOnClickListener {
            resetVM.resetPassword(
                ResetPasswordRequest(
                    userId,
                    tempPassword,
                    et_password.text.toString(),
                    et_confirm_password.text.toString()
                )
            )
        }
    }

    private fun setUpListeners() {
        et_password.addTextChangedListener {
            if (et_confirm_password.text!!.isNotEmpty()) {
                it?.let {
                    if (it.length >= 6 && it.toString() == et_confirm_password.text.toString()) {
                        validate()
                        til_password.error = null
                        til_confirm_password.error = null
                    } else
                        til_password.error = "Passwords don't match"
                }
            }
        }

        et_confirm_password.addTextChangedListener {
            it?.let {
                if (it.toString() == et_password.text.toString() && it.length >= 6) {
                    validate()
                    til_password.error = null
                    til_confirm_password.error = null
                } else
                    til_confirm_password.error = "Passwords don't match"
            }
        }
    }

    private fun validate() {
        btn_reset.isEnabled =
            et_confirm_password.text.toString() == et_password.text.toString()
                    && et_confirm_password.length() >= 6
                    && et_password.length() >= 6
    }
}