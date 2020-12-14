package uz.napa.foxmedia.ui.fragment.login

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
import uz.napa.foxmedia.request.register.SignInRequest
import uz.napa.foxmedia.response.sign_in.SignInResponse
import uz.napa.foxmedia.util.Resource
import java.io.IOException

class LoginViewModel(private val repository: MyRepository) : ViewModel() {
    private val _signInLiveData = MutableLiveData<Resource<SignInResponse>>()
    val signInLiveData: LiveData<Resource<SignInResponse>> = _signInLiveData


    fun login(signInRequest: SignInRequest) = viewModelScope.launch {
        repository.login(signInRequest)
            .onStart {
                _signInLiveData.postValue(Resource.Loading())
            }
            .catch {
                val context = App.appInstance
                when (it) {
                    is IOException -> {
                        _signInLiveData.postValue(Resource.Error(context.getString(R.string.no_connection)))
                    }
                    is HttpException -> {
                        when {
                            it.code() == 400 -> {
                                _signInLiveData.postValue(Resource.Error(context.getString(R.string.invalid_user)))
                            }
                            it.code() == 500 -> {
                                _signInLiveData.postValue(Resource.Error(context.getString(R.string.server_error)))
                            }
                            else -> {
                                _signInLiveData.postValue(Resource.Error(context.getString(R.string.connection_error)))
                            }
                        }
                    }
                    else -> _signInLiveData.postValue(Resource.Error(context.getString(R.string.unknown_error)))
                }
            }
            .collect {
                _signInLiveData.postValue(Resource.Success(it))
            }
    }


}