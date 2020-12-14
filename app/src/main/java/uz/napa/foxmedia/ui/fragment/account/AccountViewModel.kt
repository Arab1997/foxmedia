package uz.napa.foxmedia.ui.fragment.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import uz.napa.foxmedia.App
import uz.napa.foxmedia.R
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.request.user.ChangePasswordRequest
import uz.napa.foxmedia.request.user.UpdateUsernameRequest
import uz.napa.foxmedia.response.purchase.CheckoutResponse
import uz.napa.foxmedia.response.user.ChangePasswordResponse
import uz.napa.foxmedia.response.user.UserInfoResponse
import uz.napa.foxmedia.response.user.image.ChangeImageResponse
import uz.napa.foxmedia.response.user.session.SessionsResponse
import uz.napa.foxmedia.response.user.subscription.UserSubscriptionResponse
import uz.napa.foxmedia.response.user.transaction.TransactionResponse
import uz.napa.foxmedia.ui.fragment.course.FUNDED
import uz.napa.foxmedia.ui.fragment.course.ONE_TIME
import uz.napa.foxmedia.util.Resource
import java.io.File
import java.io.IOException

class AccountViewModel(private val repository: MyRepository) : ViewModel() {

    private val _userInfo = MutableLiveData<Resource<UserInfoResponse>>()
    val userInfo: LiveData<Resource<UserInfoResponse>> = _userInfo

    private val _userSubscriptions = MutableLiveData<Resource<UserSubscriptionResponse>>()
    val userSubscriptions: LiveData<Resource<UserSubscriptionResponse>> = _userSubscriptions

    private val _userTransaction = MutableLiveData<Resource<TransactionResponse>>()
    val userTransaction: LiveData<Resource<TransactionResponse>> = _userTransaction

    private val _userSessions = MutableLiveData<Resource<SessionsResponse>>()
    val userSessions: LiveData<Resource<SessionsResponse>> = _userSessions

    private val _changeImage = MutableLiveData<Resource<ChangeImageResponse>>()
    val changeImage: LiveData<Resource<ChangeImageResponse>> = _changeImage

    private val _changeUsername = MutableLiveData<Resource<UserInfoResponse>>()
    val changeUsername: LiveData<Resource<UserInfoResponse>> = _changeUsername

    private val _changePassword = MutableLiveData<Resource<ChangePasswordResponse>>()
    val changePassword: LiveData<Resource<ChangePasswordResponse>> = _changePassword

    private val _checkoutBalance = MutableLiveData<Resource<CheckoutResponse>>()
    val checkoutBalance: LiveData<Resource<CheckoutResponse>> = _checkoutBalance

    init {
        getUserInfo()
        getUserSubscriptions(FUNDED)
        getUserTransactions()
        getUserSessions()
    }

    fun checkoutBalance(checkoutType:String) = viewModelScope.launch {
        repository.checkoutBalance(checkoutType)
            .onStart { _checkoutBalance.postValue(Resource.Loading()) }
            .catch {
                _checkoutBalance.postValue(Resource.Error(handleError(it)))
            }
            .collect {
                _checkoutBalance.postValue(Resource.Success(it))
            }
    }

    fun getUserInfo() = viewModelScope.launch {
        repository.getUserInfo()
            .onStart { _userInfo.postValue(Resource.Loading()) }
            .catch {
                _userInfo.postValue(Resource.Error(handleError(it)))
            }
            .collect {
                _userInfo.postValue(Resource.Success(it))
            }
    }


    fun getUserSubscriptions(subscriptionType: String) = viewModelScope.launch {
        repository.getUserSubscriptions(subscriptionType)
            .onStart { _userSubscriptions.postValue(Resource.Loading()) }
            .catch { _userSubscriptions.postValue(Resource.Error(handleError(it))) }
            .collect {
                _userSubscriptions.postValue(Resource.Success(it))
            }
    }

    fun getUserTransactions() = viewModelScope.launch {
        repository.getUserTransaction()
            .onStart { _userTransaction.postValue(Resource.Loading()) }
            .catch { _userTransaction.postValue(Resource.Error(handleError(it))) }
            .collect {
                _userTransaction.postValue(Resource.Success(it))
            }
    }

    fun getUserSessions() = viewModelScope.launch {
        repository.getUserSessions()
            .onStart { _userSessions.postValue(Resource.Loading()) }
            .catch { _userSessions.postValue(Resource.Error(handleError(it))) }
            .collect {
                _userSessions.postValue(Resource.Success(it))
            }
    }


    fun changeImage(image: File) = viewModelScope.launch {
        val reqBody = image.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", image.name, reqBody)
        repository.changeUserImage(body)
            .onStart { _changeImage.postValue(Resource.Loading()) }
            .catch { _changeImage.postValue(Resource.Error(handleError(it))) }
            .collect {
                _changeImage.postValue(Resource.Success(it))
            }
    }

    fun changeUsername(updateUsernameRequest: UpdateUsernameRequest) = viewModelScope.launch {
        repository.changeUsername(updateUsernameRequest)
            .onStart { _changeUsername.postValue(Resource.Loading()) }
            .catch { _changeUsername.postValue(Resource.Error(handleError(it))) }
            .collect {
                _changeUsername.postValue(Resource.Success(it))
            }
    }

    fun changePassword(updatePasswordRequest: ChangePasswordRequest) = viewModelScope.launch {
        repository.changePassword(updatePasswordRequest)
            .onStart { _changePassword.postValue(Resource.Loading()) }
            .catch { _changePassword.postValue(Resource.Error(handleError(it))) }
            .collect {
                _changePassword.postValue(Resource.Success(it))
            }
    }


    private fun handleError(error: Throwable): String {
        val context = App.appInstance
        return when (error) {
            is IOException -> {
                context.getString(R.string.no_connection)
            }
            is HttpException -> {
                when {
                    error.code() == 500 -> context.getString(R.string.server_error)
                    error.code() == 401 -> context.getString(R.string.log_out)
                    else -> context.getString(R.string.connection_error)
                }
            }
            else -> {
                context.getString(R.string.unknown_error)
            }
        }
    }

    fun clearDatabase() = viewModelScope.launch {
        repository.clearDatabase()
    }
}