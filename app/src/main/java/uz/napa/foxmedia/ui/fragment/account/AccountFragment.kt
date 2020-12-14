package uz.napa.foxmedia.ui.fragment.account

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.bottom_sheet_checkout.*
import kotlinx.android.synthetic.main.bottom_sheet_teacher.*
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.android.synthetic.main.fragment_home.*
import uz.napa.foxmedia.R
import uz.napa.foxmedia.api.RetrofitInstanceBearer
import uz.napa.foxmedia.db.DatabaseProvider
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.repository.TokenProvider
import uz.napa.foxmedia.response.user.User
import uz.napa.foxmedia.response.user.session.UserSession
import uz.napa.foxmedia.response.user.subscription.UserSubscription
import uz.napa.foxmedia.response.user.transaction.Transaction
import uz.napa.foxmedia.ui.activity.MainActivity
import uz.napa.foxmedia.ui.activity.RegisterActivity
import uz.napa.foxmedia.ui.activity.SplashActivity
import uz.napa.foxmedia.ui.fragment.BaseFragment
import uz.napa.foxmedia.ui.fragment.account.adapter.UserSessionsAdapter
import uz.napa.foxmedia.ui.fragment.account.adapter.UserSubscriptionsAdapter
import uz.napa.foxmedia.ui.fragment.account.adapter.UserTransactionAdapter
import uz.napa.foxmedia.ui.fragment.purchase.CLICK
import uz.napa.foxmedia.ui.fragment.purchase.PAYME
import uz.napa.foxmedia.util.*
import uz.napa.foxmedia.util.Constants.Companion.BASE_URL
import uz.napa.foxmedia.util.Constants.Companion.DESKTOP
import uz.napa.foxmedia.util.Constants.Companion.TOKEN
import java.io.File
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val IMAGE_PICK = 1001
private const val PERMISSION_REQUEST = 1000

class AccountFragment : BaseFragment(R.layout.fragment_account) {
    private val accountVM by viewModels<AccountViewModel> {
        getViewModelFactory(
            MyRepository(
                DatabaseProvider.invoke(requireContext())
            )
        )
    }
    private val subscriptionsAdapter by lazy { UserSubscriptionsAdapter() }
    private val transactionAdapter by lazy { UserTransactionAdapter() }
    private val sessionAdapter by lazy { UserSessionsAdapter() }
    private val userSubscriptions = ArrayList<UserSubscription>()
    private val userTransactions = ArrayList<Transaction>()
    private val userSessions = ArrayList<UserSession>()
    private var checkoutType = PAYME.toLowerCase()
    private var userId = -1L


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpChangeLang()
        setUpRv()
        setObservers()
        setUpListeners()
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
                            startActivity(Intent(context, MainActivity::class.java))
                            requireActivity().finish()
                        }
                    }
                    1 -> {
                        myPref.setLang("ru") {
                            startActivity(Intent(context, MainActivity::class.java))
                            requireActivity().finish()
                        }
                    }
                }
            }
            dialog.show()

        }
    }

    private fun setUpListeners() {
        btn_logout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setCancelable(true)
                .setMessage(getString(R.string.are_you_sure))
                .setNegativeButton(getString(R.string.no)) { dialogInterface, i ->
                    dialogInterface.dismiss()
                }
                .setPositiveButton(getString(R.string.yes)) { dialogInterface, i ->
                    val pref =
                        requireActivity().getSharedPreferences(
                            Constants.PREF_NAME,
                            Context.MODE_PRIVATE
                        )
                    val editor = pref.edit()
                    editor.clear()
                    editor.apply()
                    accountVM.clearDatabase()
                    TokenProvider.resetToken()
                    RetrofitInstanceBearer.instance = null
                    val intent = Intent(
                        requireContext(),
                        RegisterActivity::class.java
                    )
                    startActivity(intent)
                    requireActivity().finish()
                }.show()
        }
        img_avatar.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST
                )
            } else {
                pickImage()
            }
        }

        btn_edit_profile.setOnClickListener {
            showDialog()
        }
        val sheetBehavior = BottomSheetBehavior.from(bottom_sheet_checkout)

        btn_replenish_balance.setOnClickListener {
            if (sheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED)
                sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            else
                sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        sheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (sheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
                    bottomSheet.hideKeyboard()
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

        })

        setUpCheckout()
    }

    private fun setUpCheckout() {
        val sheetBehavior = BottomSheetBehavior.from(bottom_sheet_checkout)
        et_price.addTextChangedListener {
            btn_checkout.isEnabled =
                et_price.text.toString().isNotEmpty() && et_price.text.toString().toLong() >= 1000
        }

        btn_click.setOnClickListener {
            btn_click.setCardBackgroundColor(resources.getColor(R.color.selectedColor))
            btn_payme.setCardBackgroundColor(resources.getColor(R.color.white))
            checkoutType = CLICK.toLowerCase()
        }

        btn_payme.setOnClickListener {
            checkoutType = PAYME.toLowerCase()
            btn_payme.setCardBackgroundColor(resources.getColor(R.color.selectedColor))
            btn_click.setCardBackgroundColor(resources.getColor(R.color.white))
        }

        btn_checkout.setOnClickListener {
            accountVM.checkoutBalance(checkoutType)
        }

        accountVM.checkoutBalance.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                    showProgress()
                    et_price.isEnabled = false
                    btn_checkout.isEnabled = false
                }
                is Resource.Success -> {
                    hideProgress()
                    sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    et_price.isEnabled = true
                    btn_checkout.isEnabled = true
                    it.data?.let { response ->
                        if (checkoutType == PAYME.toLowerCase()) {
                            snackbar(PAYME)
                            val price = et_price.text.toString().toLong() * 100
                            val base64 =
                                "m=${response.merchantId};ac.id=$userId;a=${price}"
                            val data = base64.toByteArray()
                            val url = Base64.encodeToString(data, Base64.DEFAULT)
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse("${response.checkoutUrl}/$url")
                            startActivity(intent)
                        } else {
                            snackbar(CLICK)
                            val price = et_price.text.toString().toLong()
                            val url =
                                "https://my.click.uz/services/pay?service_id=${response.serviceId}&merchant_id=${response.merchantId}&amount=$price&transaction_param=$userId"
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse("${response.checkoutUrl}/$url")
                            startActivity(intent)
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgress()
                    et_price.isEnabled = true
                    btn_checkout.isEnabled = true
                    snackbar(it.message.toString())
                }
            }
        })
    }

    private fun showDialog() {
        val array =
            arrayOf(getString(R.string.change_username), getString(R.string.change_password))
        val dialog = AlertDialog.Builder(requireContext()).setCancelable(true).setItems(
            array
        ) { p0, p1 ->
            if (p1 == 0) {
                val usernameDialog = ChangeUsernameDialog(requireContext()) {
                    accountVM.changeUsername(it)
                }
                usernameDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                usernameDialog.show()

            } else if (p1 == 1) {
                val changePasswordDialog = ChangePasswordDialog(requireContext()) {
                    accountVM.changePassword(it)
                }
                changePasswordDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                changePasswordDialog.show()
            }

        }
        dialog.show()
    }

    private fun pickImage() {
        val intent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK)
    }

    private fun setObservers() {
        accountVM.userInfo.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                    showProgress()
                }
                is Resource.Success -> {
                    hideProgress()
                    it.data?.let { userInfo ->
                        val user = userInfo.user
                        userId = user.id
                        setUpViews(user)
                    }
                }
                is Resource.Error -> {
                    hideProgress()
                    snackbar(it.message.toString())
                }
            }
        })

        accountVM.changePassword.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                    showProgress()
                }
                is Resource.Success -> {
                    hideProgress()
                    it.data?.let { response ->
                        snackbar(response.message)
                    }
                }
                is Resource.Error -> {
                    hideProgress()
                    snackbar(it.message.toString())
                }
            }
        })

        accountVM.changeUsername.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                    showProgress()
                }
                is Resource.Success -> {
                    hideProgress()
                    it.data?.let { userInfo ->
                        val user = userInfo.user
                        setUpViews(user)
                        val pref = requireActivity().getSharedPreferences(
                            Constants.PREF_NAME,
                            Context.MODE_PRIVATE
                        ).edit()
                        pref.putString(Constants.FIRST_NAME, userInfo.user.firstName)
                        pref.putString(Constants.LAST_NAME, userInfo.user.lastName)
                        pref.apply()
                    }
                }
                is Resource.Error -> {
                    hideProgress()
                    snackbar(it.message.toString())
                }
            }
        })

        accountVM.userSubscriptions.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    it.data?.let { subscription ->
                        if (subscription.userSubscriptions.isNotEmpty()) {
                            val userSubscription = subscription.userSubscriptions[0]
                            userSubscriptions.addAll(subscription.userSubscriptions)
                            userSubscriptions.removeAt(0)
                            subscriptionsAdapter.differ.submitList(userSubscriptions.toList())
                            setUpSubscriptions(userSubscription)
                        } else
                            card_subscription.isVisible = false
                    }
                }
                is Resource.Error -> {
                    hideProgress()
                    snackbar(it.message.toString())
                }
            }
        })

        accountVM.userTransaction.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    it.data?.let { response ->
                        if (response.transactions.isNotEmpty()) {
                            userTransactions.addAll(response.transactions)
                            val transaction = userTransactions[0]
                            userTransactions.removeAt(0)
                            transactionAdapter.differ.submitList(userTransactions.toList())
                            setUpTransaction(transaction)
                        } else
                            card_transaction.isVisible = false
                    }
                }
                is Resource.Error -> {
                    hideProgress()
                    snackbar(it.message.toString())
                }
            }
        })

        accountVM.userSessions.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    hideProgress()
                    it.data?.let { response ->
                        if (response.userSessions.isNotEmpty()) {
                            userSessions.addAll(response.userSessions)
                            val userSession = userSessions[0]
                            userSessions.removeAt(0)
                            sessionAdapter.differ.submitList(userSessions.toList())
                            setUpSession(userSession)
                        } else
                            card_session.isVisible = false
                    }
                }
                is Resource.Error -> {
                    hideProgress()
                    snackbar(it.message.toString())
                }
            }
        })

        accountVM.changeImage.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                    showProgress()
                }
                is Resource.Success -> {
                    hideProgress()
                    it.data?.let { response ->
                        Glide.with(this).load(BASE_URL + response.image).diskCacheStrategy(
                            DiskCacheStrategy.RESOURCE
                        )
                            .placeholder(R.drawable.gerb)
                            .into(img_avatar)
                    }
                }
                is Resource.Error -> {
                    hideProgress()
                    snackbar(it.message.toString())
                }
            }
        })
    }

    private fun showProgress() {
        progress.isVisible = true
    }

    private fun hideProgress() {
        progress.isVisible = false
    }

    private fun setUpRv() {
        rv_user_subscription.apply {
            adapter = subscriptionsAdapter
            itemAnimator = DefaultItemAnimator()
        }
        rv_user_transaction.apply {
            adapter = transactionAdapter
            itemAnimator = DefaultItemAnimator()
        }
        rv_session.apply {
            adapter = sessionAdapter
            itemAnimator = DefaultItemAnimator()
        }
    }

    private fun setUpSession(userSession: UserSession) {
        if (userSession.deviceType == DESKTOP)
            img_session.setImageResource(R.drawable.ic_desktop)
        else
            img_session.setImageResource(R.drawable.ic_phone)
        tv_ip.text = userSession.ip

        tv_enter_time.text = formatDate(userSession.signedInTime)
        card_session.setOnClickListener {
            expandHideRv(rv_session, btn_more_session)
        }
        btn_more_session.setOnClickListener {
            expandHideRv(rv_session, btn_more_session)
        }
    }

    private fun setUpTransaction(transaction: Transaction) {
        transaction_method.text = transaction.method
        transaction_amount.text = getString(R.string.som, transaction.amount)
        tv_replenished_date.text = formatDate(transaction.paymentTime)
        card_transaction.setOnClickListener {
            expandHideRv(rv_user_transaction, btn_more_transaction)
        }
        btn_more_transaction.setOnClickListener {
            expandHideRv(rv_user_transaction, btn_more_transaction)
        }

    }

    private fun expandHideRv(
        rv: RecyclerView,
        btn: ImageButton
    ) {
        if (rv.isVisible) {
            rv.isVisible = false
            TransitionManager.beginDelayedTransition(layout_account, AutoTransition())
            btn.setImageResource(R.drawable.ic_expand_more)

        } else {
            rv.isVisible = true
            TransitionManager.beginDelayedTransition(layout_account, AutoTransition())
            btn.setImageResource(R.drawable.ic_hide_expand)

        }
    }

    private fun setUpSubscriptions(userSubscription: UserSubscription) {
        subscription_name.text = userSubscription.plan
        tv_expire_date.text = userSubscription.expiresAt
        subscription_cost.text = getString(R.string.som, userSubscription.total)
        btn_more_subscription.setOnClickListener {
            expandHideRv(rv_user_subscription, btn_more_subscription)
        }
        card_subscription.setOnClickListener {
            expandHideRv(rv_user_subscription, btn_more_subscription)
        }
    }

    private fun setUpViews(user: User) {
        Glide.with(this).load(BASE_URL + user.image)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .placeholder(R.drawable.gerb)
            .into(img_avatar)
        tv_username.text = getString(R.string.full_name, user.lastName, user.firstName)
        tv_number.text = user.phoneNumber.toString()
        tv_balance.text = getString(R.string.balance, formatMoney(user.balance))
        tv_number.text = getString(R.string.phone_number, user.phoneNumber)

        val seconds = user.totalWatchTime % 60
        var hour = user.totalWatchTime / 60
        val minutes = hour % 60
        hour /= 60
        tv_watch_time.text = getString(R.string.watch_time, hour, minutes, seconds)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                IMAGE_PICK -> {
                    val uri = data?.data
                    uri?.let {
                        val destination = "cropped_img_${Date().time}.jpg"
                        val options = UCrop.Options()
                        options.setCircleDimmedLayer(true)
                        options.setToolbarTitle(getString(R.string.edit))
                        UCrop.of(it, Uri.fromFile(File(requireActivity().cacheDir, destination)))
                            .withOptions(options)
                            .start(requireContext(), this)
                    }
                }
                UCrop.REQUEST_CROP -> {
                    val uri = UCrop.getOutput(data!!)
                    val path = FilePath.getPath(requireContext(), uri!!)
                    accountVM.changeImage(File(path.toString()))

                }
                UCrop.RESULT_ERROR -> {
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    pickImage()
                else
                    snackbar(getString(R.string.permission_denied))
                return
            }
            else -> snackbar(getString(R.string.error))
        }
    }
}