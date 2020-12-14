package uz.napa.foxmedia.ui.fragment.login.confirm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_confirm.*
import uz.napa.foxmedia.ui.activity.MainActivity
import uz.napa.foxmedia.R
import uz.napa.foxmedia.receiver.MessageListener
import uz.napa.foxmedia.receiver.MessageReceiver
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.request.register.ConfirmRequest
import uz.napa.foxmedia.ui.fragment.login.register.RegisterFragment
import uz.napa.foxmedia.ui.fragment.login.restore.RestoreFragment
import uz.napa.foxmedia.util.Constants.Companion.PREF_NAME
import uz.napa.foxmedia.util.Constants.Companion.USER_ID
import uz.napa.foxmedia.util.Resource
import uz.napa.foxmedia.util.getViewModelFactory
import uz.napa.foxmedia.util.snackbar

class ConfirmFragment : Fragment(R.layout.fragment_confirm) {
    private val args by navArgs<ConfirmFragmentArgs>()
    private val confirmViewModel by viewModels<ConfirmViewModel> { getViewModelFactory(MyRepository()) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        confirmViewModel.confirmLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                    progress_bar.isVisible = true
                }
                is Resource.Error -> {
                    progress_bar.isVisible = false
                    snackbar(it.message!!)
                }
                is Resource.Success -> {
                    progress_bar.isVisible = false
                    val intent = Intent(
                        requireContext(),
                        MainActivity::class.java
                    )
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
        })

        confirmViewModel.verifyLiveData.observe(this as LifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                    progress_bar.isVisible = true
                }
                is Resource.Error -> {
                    progress_bar.isVisible = false
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Success -> {
                    val response = it.data!!
                    progress_bar.isVisible = false
                    val pref = requireActivity().getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE).edit()
                    pref.putLong(USER_ID, response.userId)
                    pref.apply()
                    val action =
                        ConfirmFragmentDirections.actionConfirmFragmentToResetPasswordFragment(
                            response.tempPassword
                        )
                    findNavController().navigate(action)

                }
            }
        })
        tv_phone_num.text = args.phoneNum.toString()
        btn_back.setOnClickListener {
            findNavController().popBackStack()
        }

        et_confirm_code.doOnTextChanged { text, start, count, after ->
            if (text?.length == 6) {
                if (args.fragmentName == RegisterFragment::class.simpleName) {
                    confirmViewModel.confirmNumber(
                        ConfirmRequest(
                            args.phoneNum,
                            text.toString().toInt()
                        )
                    )
                } else if (args.fragmentName == RestoreFragment::class.simpleName) {
                    confirmViewModel.verifyToken(
                        ConfirmRequest(
                            args.phoneNum,
                            text.toString().toInt()
                        )
                    )
                }

            }
        }
    }

}