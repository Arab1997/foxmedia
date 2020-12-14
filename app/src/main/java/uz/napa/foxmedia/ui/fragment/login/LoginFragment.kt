package uz.napa.foxmedia.ui.fragment.login

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.change_lang
import uz.napa.foxmedia.R
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.request.register.SignInRequest
import uz.napa.foxmedia.ui.activity.MainActivity
import uz.napa.foxmedia.ui.activity.RegisterActivity
import uz.napa.foxmedia.util.*
import uz.napa.foxmedia.util.Constants.Companion.PASSWORD
import uz.napa.foxmedia.util.Constants.Companion.PHONE_NUM
import uz.napa.foxmedia.util.Constants.Companion.PREF_NAME
import uz.napa.foxmedia.util.Constants.Companion.SESSION_ID
import uz.napa.foxmedia.util.Constants.Companion.TOKEN
import uz.napa.foxmedia.util.Constants.Companion.USER_ID

const val ANDROID = "Android"

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val loginVM by viewModels<LoginViewModel> { getViewModelFactory(MyRepository()) }
    private lateinit var deviceType: String
    private var sessionId: String? = null


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUpChangeLang()
        setUpVariables()
        setUpListeners()

        loginVM.signInLiveData.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Loading -> {
                    btn_login.startLoading()
                    isUiEnable(false)
                }
                is Resource.Error -> {
                    btn_login.loadingFailed()
                    isUiEnable(true)
                    snackbar(response.message.toString())
                }
                is Resource.Success -> {
                    val data = response.data!!
                    val phoneNum = ccp.selectedCountryCode + et_phone.rawText
                    val pref =
                        requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                            .edit()
                    pref.putString(SESSION_ID, data.session.id)
                    pref.putLong(USER_ID, data.session.userId)
                    pref.putString(TOKEN, data.token)
                    pref.putLong(PHONE_NUM, phoneNum.toLong())
                    pref.putString(PASSWORD, et_password.text.toString())
                    pref.apply()
                    btn_login.loadingSuccessful()
                    btn_login.animationEndAction = {
                        toNextPage()
                    }
                }
            }
        })
    }

    private fun setUpChangeLang() {
        val myPref = MyPreference(requireContext());
        change_lang.setOnClickListener {
            val array =
                arrayOf(
                    getString(R.string.uzbek_ru),
                    getString(R.string.russian)
                )
            val dialog = AlertDialog.Builder(requireContext()).setCancelable(true).setItems(
                array
            ) { p0, p1 ->
                when (p1) {
                    0 -> {
                        myPref.setLang("oz") {
                            startActivity(Intent(context, RegisterActivity::class.java))
                            requireActivity().finish()
                        }
                    }
                    1 -> {
                        myPref.setLang("ru") {
                            startActivity(Intent(context, RegisterActivity::class.java))
                            requireActivity().finish()
                        }
                    }
                }

            }
            dialog.show()

        }
    }

    private fun setUpListeners() {
        et_phone.doOnTextChanged { text, start, count, after ->
            et_password.text?.let {
                btn_login.isEnabled = text!!.length == 14 && it.length >= 6
            }
        }

        et_password.doOnTextChanged { text, start, before, count ->
            et_phone.text?.let {
                btn_login.isEnabled = it.length == 14 && text!!.length >= 6
            }
        }

        btn_login.setOnClickListener {
            it.hideKeyboard()
            val phoneNum = ccp.selectedCountryCode + et_phone.rawText
            loginVM.login(
                SignInRequest(
                    phoneNum,
                    et_password.text.toString(),
                    sessionId,
                    Build.VERSION.RELEASE,
                    deviceType,
                    ANDROID,
                    Build.MODEL
                )
            )
        }

        btn_sign_up.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.registerFragment)
                findNavController().navigate(R.id.action_registerFragment_to_registerFragment2)
        }

        btn_forgot_password.setOnClickListener {
            if (findNavController().currentDestination?.id == R.id.registerFragment)
                findNavController().navigate(R.id.action_registerFragment_to_restoreFragment)
        }
    }

    private fun setUpVariables() {
        btn_login.isEnabled = false
        deviceType = if (resources.getBoolean(R.bool.isTablet)) "TABLET" else "MOBILE"
        val pref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        pref.getString(SESSION_ID, "")?.let {
            sessionId = it
        }
    }

    private fun isUiEnable(isEnabled: Boolean) {
        btn_login.isEnabled = isEnabled
        til_password.isEnabled = isEnabled
        til_phone.isEnabled = isEnabled
    }

    private fun toNextPage() {

        val cx = (btn_login.left + btn_login.right) / 2
        val cy = (btn_login.top + btn_login.bottom) / 2

        val animator = ViewAnimationUtils.createCircularReveal(
            animate_view,
            cx,
            cy,
            0f,
            resources.displayMetrics.heightPixels * 1.2f
        )
        animator.duration = 300
        animator.interpolator = AccelerateDecelerateInterpolator()
        animate_view.visibility = View.VISIBLE
        animator.start()

        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                btn_login.postDelayed({
                    btn_login.reset()
                    animate_view.visibility = View.INVISIBLE
                }, 200)
            }

            override fun onAnimationEnd(animation: Animator) {
                val intent = Intent(
                    requireContext(),
                    MainActivity::class.java
                )
                startActivity(intent)
                requireActivity().finish()
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })

    }
}