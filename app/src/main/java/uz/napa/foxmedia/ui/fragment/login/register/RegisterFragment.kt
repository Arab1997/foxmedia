package uz.napa.foxmedia.ui.fragment.login.register

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import uz.napa.foxmedia.R
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.request.register.RegisterRequest
import uz.napa.foxmedia.response.regions.Region
import uz.napa.foxmedia.util.Constants
import uz.napa.foxmedia.util.Resource
import uz.napa.foxmedia.util.getViewModelFactory
import uz.napa.foxmedia.util.snackbar


private const val TAG = "RegisterFragment"

@ExperimentalCoroutinesApi
class RegisterFragment : Fragment(R.layout.fragment_register) {
    private val registerVM by viewModels<RegisterViewModel> { getViewModelFactory(MyRepository()) }
    private lateinit var phoneNum: String
    private lateinit var selectedRegion: Region
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        validate()
        setUpListeners()
        setSubscribers()

    }

    private fun setSubscribers() {
        registerVM.province.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    isUiEnable(true)
                    val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                        requireContext(),
                        R.layout.drop_down_item,
                        it.data?.provinceName!!
                    )
                    spinner_province.adapter = adapter
                    getRegions(it.data.province[spinner_province.selectedItemPosition].id)
                    spinner_province.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                p0: AdapterView<*>?,
                                p1: View?,
                                pos: Int,
                                id: Long
                            ) {
                                getRegions(it.data.province[pos].id)
                            }

                            override fun onNothingSelected(p0: AdapterView<*>?) {
                                isUiEnable(false)
                            }

                        }
                }
                is Resource.Error -> {
                    isUiEnable(false)
                    snackbar(it.message.toString())
                }
                is Resource.Loading -> {
                    isUiEnable(false)
                }
            }
        })

        registerVM.regions.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    isUiEnable(true)
                    val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                        requireContext(),
                        R.layout.drop_down_item,
                        it.data?.regionName!!
                    )
                    spinner_region.adapter = adapter
                    selectedRegion = it.data.region[spinner_region.selectedItemPosition]
                    spinner_region.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                p0: AdapterView<*>?,
                                p1: View?,
                                pos: Int,
                                id: Long
                            ) {
                                selectedRegion = it.data.region[pos]

                            }

                            override fun onNothingSelected(p0: AdapterView<*>?) {
                                isUiEnable(false)
                            }

                        }
                }
                is Resource.Error -> {
                    isUiEnable(false)
                    snackbar(it.message.toString())
                }
                is Resource.Loading -> {
                    isUiEnable(false)
                }
            }
        })

        registerVM.registerLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                    btn_register.startLoading()
                    isUiEnable(false)
                }
                is Resource.Error -> {
                    btn_register.loadingFailed()
                    isUiEnable(true)
                    snackbar(it.message!!)
                }
                is Resource.Success -> {
                    btn_register.loadingSuccessful()
                    isUiEnable(true)
                    val pref =
                        requireActivity().getSharedPreferences(
                            Constants.PREF_NAME,
                            Context.MODE_PRIVATE
                        )
                            .edit()
                    pref.putLong(Constants.PHONE_NUM, phoneNum.toLong())
                    pref.putString(Constants.PASSWORD, et_password.text.toString())
                    pref.apply()
                    val action =
                        RegisterFragmentDirections.actionRegisterFragment2ToConfirmFragment(
                            phoneNum.toLong(),
                            RegisterFragment::class.simpleName!!
                        )
                    findNavController().navigate(action)
                }
            }
        })
    }

    private fun getRegions(id: Long) {
        registerVM.getRegions(id)
    }

    private fun isUiEnable(isEnabled: Boolean) {
        et_name.isEnabled = isEnabled
        et_surname.isEnabled = isEnabled
        et_password.isEnabled = isEnabled
        et_confirm_password.isEnabled = isEnabled
        et_phone.isEnabled = isEnabled
    }

    private fun setUpListeners() {
        btn_register.setOnClickListener {
            if (::selectedRegion.isInitialized) {
                phoneNum = ccp_register.selectedCountryCode + et_phone.rawText
                registerVM.signUp(
                    RegisterRequest(
                        et_name.text.toString(),
                        et_surname.text.toString(),
                        phoneNum,
                        et_password.text.toString(),
                        et_confirm_password.text.toString(),
                        selectedRegion.provinceId.toLong(),
                        selectedRegion.id
                    )
                )
            }
        }

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

        et_name.addTextChangedListener {
            it?.let {
                if (it.length > 2)
                    validate()
                else {
                    disableRequest()
                }
            }
        }

        et_surname.addTextChangedListener {
            it?.let {
                if (it.length > 2)
                    validate()
                else {
                    disableRequest()
                }
            }
        }

        et_phone.doOnTextChanged { text, start, count, after ->
            text?.let {
                if (text.length == 14)
                    validate()
                else
                    disableRequest()
            }
        }
    }

    private fun disableRequest() {
        btn_register.isEnabled = false
    }

    private fun validate() {
        btn_register.isEnabled = et_phone.text!!.length == 14 &&
                et_surname.text!!.length > 2 &&
                et_name.text!!.length > 2 &&
                et_password.text!!.length >= 6 &&
                et_confirm_password.text!!.length >= 6 &&
                et_password.text.toString() == et_confirm_password.text.toString()
    }

}