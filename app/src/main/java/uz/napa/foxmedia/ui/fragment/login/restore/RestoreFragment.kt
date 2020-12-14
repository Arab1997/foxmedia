package uz.napa.foxmedia.ui.fragment.login.restore

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_restore.*
import uz.napa.foxmedia.R
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.request.register.SendTokenRequest
import uz.napa.foxmedia.util.Resource
import uz.napa.foxmedia.util.getViewModelFactory
import uz.napa.foxmedia.util.snackbar

class RestoreFragment : Fragment(R.layout.fragment_restore) {
    private val restoreVM by viewModels<RestoreViewModel> { getViewModelFactory(MyRepository()) }
    private lateinit var phoneNum: String


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_send.isEnabled = false
        restoreVM.sendTokenResponse.observe(viewLifecycleOwner, Observer {
            when(it){
                is Resource.Loading->{
                    btn_send.startLoading()
                    isUiEnable(false)
                }
                is Resource.Error->{
                    btn_send.loadingFailed()
                    snackbar(it.message!!)
                    isUiEnable(true)
                }
                is Resource.Success->{
                    if (this::phoneNum.isInitialized){
                        btn_send.loadingSuccessful()
                        val action = RestoreFragmentDirections.actionRestoreFragmentToConfirmFragment(phoneNum.toLong(),RestoreFragment::class.simpleName!!)
                        findNavController().navigate(action)
                        isUiEnable(true)
                    }

                }
            }
        })

        et_phone.addTextChangedListener {
            it?.let {
                btn_send.isEnabled = it.length == 14
            }
        }
        btn_send.setOnClickListener {
            phoneNum = ccp_restore.selectedCountryCode + et_phone.rawText
            restoreVM.sendToken(
                SendTokenRequest(
                    phoneNum.toLong()
                )
            )
        }
    }

    private fun isUiEnable(isEnable:Boolean) {
        btn_send.isEnabled = isEnable
        et_phone.isEnabled = isEnable
        ccp_restore.isEnabled = isEnable
    }
}