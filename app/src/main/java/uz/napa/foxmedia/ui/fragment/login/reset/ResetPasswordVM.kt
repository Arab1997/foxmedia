package uz.napa.foxmedia.ui.fragment.login.reset

import android.util.Log
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
import uz.napa.foxmedia.request.register.ResetPasswordRequest
import uz.napa.foxmedia.response.register.ConfirmResponse
import uz.napa.foxmedia.util.Resource
import java.io.IOException

class ResetPasswordVM(private val repository: MyRepository) : ViewModel() {

    private val _resetPassLiveData = MutableLiveData<Resource<ConfirmResponse>>()
    val resetPasswordLiveData: LiveData<Resource<ConfirmResponse>> = _resetPassLiveData

    fun resetPassword(resetPasswordRequest: ResetPasswordRequest) = viewModelScope.launch {
        repository.resetPassword(resetPasswordRequest)
            .onStart { _resetPassLiveData.postValue(Resource.Loading()) }
            .catch {
                val context = App.appInstance
                when (it) {
                    is IOException -> {
                        _resetPassLiveData.postValue(Resource.Error(context.getString(R.string.no_connection)))
                    }
                    is HttpException -> {
                        when {
                            it.code() == 400 -> _resetPassLiveData.postValue(Resource.Error(context.getString(R.string.invalid_password)))
                            it.code() == 500 -> _resetPassLiveData.postValue(Resource.Error(context.getString(R.string.server_error)))
                            else -> _resetPassLiveData.postValue(Resource.Error(context.getString(R.string.connection_error)))
                        }
                    }
                    else -> _resetPassLiveData.postValue(Resource.Error(context.getString(R.string.unknown_error)))
                }
            }
            .collect { _resetPassLiveData.postValue(Resource.Success(it)) }
    }
}