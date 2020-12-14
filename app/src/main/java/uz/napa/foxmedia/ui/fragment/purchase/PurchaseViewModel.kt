package uz.napa.foxmedia.ui.fragment.purchase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.request.purchase.PurchaseRequest
import uz.napa.foxmedia.response.purchase.PurchaseResponse
import uz.napa.foxmedia.response.purchase.SubscriptionDetailsResponse
import uz.napa.foxmedia.response.purchase.SubscriptionsTable
import uz.napa.foxmedia.response.user.UserInfoResponse
import uz.napa.foxmedia.util.Resource

class PurchaseViewModel(private val repository: MyRepository) : ViewModel() {

    var selectedPlanPosition = 0

    private val _subscriptionsTable = MutableLiveData<Resource<SubscriptionsTable>>()
    val subscriptionsTable: LiveData<Resource<SubscriptionsTable>> = _subscriptionsTable

    private val _purchaseDetails = MutableLiveData<Resource<SubscriptionDetailsResponse>>()
    val purchaseDetails: LiveData<Resource<SubscriptionDetailsResponse>> = _purchaseDetails

    private val _userInfo = MutableLiveData<Resource<UserInfoResponse>>()
    val userInfo: LiveData<Resource<UserInfoResponse>> = _userInfo

    private val _purchase = MutableLiveData<Resource<PurchaseResponse>>()
    val purchase: LiveData<Resource<PurchaseResponse>> = _purchase

    init {
        getSubscriptionTable()
        getUserInfo()
    }

    fun getSubscriptionTable() = viewModelScope.launch {
        repository.getSubscriptionTable()
            .onStart {
                _subscriptionsTable.postValue(Resource.Loading())
            }
            .catch { _subscriptionsTable.postValue(Resource.Error(it.message.toString())) }
            .collect {
                _subscriptionsTable.postValue(Resource.Success(it))
            }
    }

    fun getPurchaseDetails(subscriptionType: String, resourceId: Long) = viewModelScope.launch {
        repository.getPurchaseDetails(subscriptionType, resourceId)
            .onStart { _purchaseDetails.postValue(Resource.Loading()) }
            .catch { _purchaseDetails.postValue(Resource.Error(it.message.toString())) }
            .collect {
                _purchaseDetails.postValue(Resource.Success(it))
            }
    }

    fun getUserInfo() = viewModelScope.launch {
        repository.getUserInfo()
            .onStart { _userInfo.postValue(Resource.Loading()) }
            .catch { _userInfo.postValue(Resource.Error(it.message.toString())) }
            .collect {
                _userInfo.postValue(Resource.Success(it))
            }
    }

    fun purchaseSubscription(purchaseRequest: PurchaseRequest) = viewModelScope.launch {
        repository.purchaseSubscription(purchaseRequest)
            .onStart { _purchase.postValue(Resource.Loading()) }
            .catch { _purchase.postValue(Resource.Error(it.message.toString())) }
            .collect {
                _purchase.postValue(Resource.Success(it))
            }
    }

}