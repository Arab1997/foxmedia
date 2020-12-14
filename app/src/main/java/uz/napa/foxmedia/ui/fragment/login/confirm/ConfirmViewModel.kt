package uz.napa.foxmedia.ui.fragment.login.confirm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import retrofit2.HttpException
import uz.napa.foxmedia.App
import uz.napa.foxmedia.R
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.request.register.ConfirmRequest
import uz.napa.foxmedia.response.register.ConfirmResponse
import uz.napa.foxmedia.response.register.VerifyTokenResponse
import uz.napa.foxmedia.util.Resource
import java.io.IOException

class ConfirmViewModel(private val repository: MyRepository) : ViewModel() {
    private val _confirmLiveData = MutableLiveData<Resource<ConfirmResponse>>()
    val confirmLiveData: LiveData<Resource<ConfirmResponse>> = _confirmLiveData

    private val _verifyLiveData = MutableLiveData<Resource<VerifyTokenResponse>>()
    val verifyLiveData: LiveData<Resource<VerifyTokenResponse>> = _verifyLiveData

    fun confirmNumber(confirmRequest: ConfirmRequest) = viewModelScope.launch {
        repository.confirm(confirmRequest)
            .onStart { _confirmLiveData.postValue(Resource.Loading()) }
            .catch {
                when (it.cause) {
                    is IOException -> {
                        _confirmLiveData.postValue(Resource.Error(App.appInstance.getString(R.string.no_connection)))
                    }
                    is HttpException -> {
                        val error = it as HttpException
                        when {
                            error.code() == 400 -> _confirmLiveData.postValue(Resource.Error(App.appInstance.getString(R.string.invalid_code)))
                            error.code() == 500 -> _confirmLiveData.postValue(Resource.Error(App.appInstance.getString(R.string.server_error)))
                            else -> _confirmLiveData.postValue(Resource.Error(App.appInstance.getString(R.string.connection_error)))
                        }
                    }
                    else -> _confirmLiveData.postValue(Resource.Error(App.appInstance.getString(R.string.unknown_error)))
                }
            }
            .collect { _confirmLiveData.postValue(Resource.Success(it)) }
    }

    fun verifyToken(confirmRequest: ConfirmRequest) = viewModelScope.launch {
        repository.verifyToken(confirmRequest)
            .onStart { _verifyLiveData.postValue(Resource.Loading()) }
            .catch {
                when (it) {
                    is IOException -> {
                        _verifyLiveData.postValue(Resource.Error(App.appInstance.getString(R.string.no_connection)))
                    }
                    is HttpException -> {
                        if (it.code() == 400)
                            _verifyLiveData.postValue(Resource.Error(App.appInstance.getString(R.string.invalid_code)))
                        if (it.code() == 500)
                            _verifyLiveData.postValue(Resource.Error(App.appInstance.getString(R.string.server_error)))
                    }
                    else -> _verifyLiveData.postValue(Resource.Error(App.appInstance.getString(R.string.unknown_error)))
                }

            }
            .collect { _verifyLiveData.postValue(Resource.Success(it)) }
    }
}