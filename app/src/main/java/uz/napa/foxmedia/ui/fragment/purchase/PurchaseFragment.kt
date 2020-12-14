package uz.napa.foxmedia.ui.fragment.purchase

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_purchase.*
import uz.napa.foxmedia.R
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.request.purchase.PurchaseRequest
import uz.napa.foxmedia.response.purchase.SubscriptionPlan
import uz.napa.foxmedia.ui.fragment.BaseFragment
import uz.napa.foxmedia.ui.fragment.course.FUNDED
import uz.napa.foxmedia.ui.fragment.course.ONE_TIME
import uz.napa.foxmedia.util.Resource
import uz.napa.foxmedia.util.formatMoney
import uz.napa.foxmedia.util.getViewModelFactory
import uz.napa.foxmedia.util.snackbar
import java.text.DecimalFormat
import java.text.NumberFormat

const val BALANCE = "BALANCE"
const val CLICK = "CLICK"
const val PAYME = "PAYME"

class PurchaseFragment : BaseFragment(R.layout.fragment_purchase, isBottomNavVisible = false) {
    private val args by navArgs<PurchaseFragmentArgs>()
    private val purchaseVM by viewModels<PurchaseViewModel> { getViewModelFactory(MyRepository()) }
    private val subscriptionsTable = ArrayList<SubscriptionPlan>()
    private var resourceId = -1L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpSpinner()
        setUpListeners()
        purchaseVM.userInfo.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                    showProgress()
                }
                is Resource.Error -> {
                    hideProgress()
                    snackbar(it.message.toString())
                }
                is Resource.Success -> {
                    hideProgress()
                    it.data?.let { res ->
                        btn_balance.isEnabled = res.user.balance >= 30_000
                        tv_user_balance.text =
                            requireContext().getString(
                                R.string.balance,
                                formatMoney(res.user.balance)
                            )
                    }
                }
            }
        })
        purchaseVM.subscriptionsTable.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                    showProgress()
                }
                is Resource.Error -> {
                    hideProgress()
                    snackbar(it.message.toString())
                }
                is Resource.Success -> {
                    hideProgress()
                    it.data?.let { res ->
                        val array = ArrayList<String>()
                        subscriptionsTable.clear()
                        subscriptionsTable.addAll(res.subscriptions)
                        res.subscriptions.forEach { sub ->
                            array.add(
                                "${sub.plan} - ${
                                    requireContext().getString(
                                        R.string.som_formatted,
                                        formatMoney(sub.price)
                                    )
                                }"
                            )
                        }
                        val adapter = ArrayAdapter<String>(
                            requireContext(),
                            android.R.layout.simple_spinner_dropdown_item, array
                        )
                        spinner_subscription.adapter = adapter
                        spinner_subscription.setSelection(purchaseVM.selectedPlanPosition)
                        resourceId = if (args.course.subscriptionType == ONE_TIME) {
                            purchaseVM.getPurchaseDetails(
                                args.course.subscriptionType,
                                args.course.id
                            )
                            args.course.id
                        } else {
                            purchaseVM.getPurchaseDetails(
                                args.course.subscriptionType,
                                subscriptionsTable[0].id
                            )
                            subscriptionsTable[0].id
                        }

                    }
                }
            }
        })

        purchaseVM.purchaseDetails.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                    showProgress()
                }
                is Resource.Error -> {
                    hideProgress()
                    snackbar(it.message.toString())
                }
                is Resource.Success -> {
                    hideProgress()
                    it.data?.let { res ->
                        val resource = res.resource
                        course_name.text = resource.plan
                        resource.period?.let { period ->
                            course_limit.text = requireContext().getString(R.string.months, period)
                        } ?: kotlin.run {
                            course_limit.text = requireContext().getString(R.string.unlimited)
                        }
                        course_sale_price.text = requireContext().getString(
                            R.string.som_formatted,
                            formatMoney(resource.price)
                        )
                        course_discount.text =
                            requireContext().getString(R.string.percentage, resource.discount)
                        course_price.text = requireContext().getString(
                            R.string.som_formatted,
                            formatMoney(resource.saleprice)
                        )
                    }
                }
            }
        })

        purchaseVM.purchase.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Loading -> {
                    disableScreen()
                }
                is Resource.Error -> {
                    enableScreen()
                    snackbar(it.message.toString())
                }
                is Resource.Success -> {
                    enableScreen()
                    it.data?.let { res ->
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(res.checkoutLink)
                        startActivity(intent)
                        purchaseVM.getUserInfo()
                    }
                }
            }
        })
    }

    private fun disableScreen() {
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun enableScreen() {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private fun setUpListeners() {
        btn_balance.setOnClickListener {
            if (resourceId != -1L) {
                purchaseVM.purchaseSubscription(
                    PurchaseRequest(
                        resourceId,
                        args.course.subscriptionType,
                        BALANCE
                    )
                )
            }

        }
        btn_payme.setOnClickListener {
            if (resourceId != -1L) {
                purchaseVM.purchaseSubscription(
                    PurchaseRequest(
                        resourceId,
                        args.course.subscriptionType,
                        PAYME
                    )
                )
            }
        }
        btn_click.setOnClickListener {
            if (resourceId != -1L) {
                purchaseVM.purchaseSubscription(
                    PurchaseRequest(
                        resourceId,
                        args.course.subscriptionType,
                        CLICK
                    )
                )
            }
        }
    }


    private fun setUpSpinner() {
        spinner_subscription.isVisible = args.course.subscriptionType == FUNDED
        spinner_subscription.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                purchaseVM.getPurchaseDetails(
                    args.course.subscriptionType,
                    subscriptionsTable[position].id
                )
                purchaseVM.selectedPlanPosition = position
                resourceId = subscriptionsTable[position].id
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }
    }

    private fun showProgress() {
        progress_purchase.isVisible = true
    }

    private fun hideProgress() {
        progress_purchase.isVisible = false
    }
}