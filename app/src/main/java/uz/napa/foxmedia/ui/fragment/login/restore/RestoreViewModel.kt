package uz.napa.foxmedia.ui.fragment.login.restore

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
import uz.napa.foxmedia.request.register.SendTokenRequest
import uz.napa.foxmedia.response.register.SendTokenResponse
import uz.napa.foxmedia.util.Resource
import java.io.IOException

class RestoreViewModel(private val repository: MyRepository) : ViewModel() {

    private val _sendTokenLiveData = MutableLiveData<Resource<SendTokenResponse>>()
    val sendTokenResponse: LiveData<Resource<SendTokenResponse>> = _sendTokenLiveData

    fun sendToken(sendTokenRequest: SendTokenRequest) = viewModelScope.launch {
        repository.sendToken(sendTokenRequest)
            .onStart {
                _sendTokenLiveData.postValue(Resource.Loading())
            }
            .catch {
                when (it) {
                    is IOException -> {
                        _sendTokenLiveData.postValue(Resource.Error(App.appInstance.getString(R.string.no_connection)))
                    }
                    is HttpException -> {
                        when {
                            it.code() == 400 -> _sendTokenLiveData.postValue(Resource.Error(App.appInstance.getString(R.string.invalid_number)))
                            it.code() == 500 -> _sendTokenLiveData.postValue(Resource.Error(App.appInstance.getString(R.string.server_error)))
                            else -> _sendTokenLiveData.postValue(Resource.Error(App.appInstance.getString(R.string.connection_error)))
                        }
                    }
                    else -> _sendTokenLiveData.postValue(Resource.Error(App.appInstance.getString(R.string.unknown_error)))
                }
            }
            .collect {
                _sendTokenLiveData.postValue(Resource.Success(it))
            }
    }
}