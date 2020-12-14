package uz.napa.foxmedia.ui.fragment.login.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import retrofit2.HttpException
import uz.napa.foxmedia.App
import uz.napa.foxmedia.R
import uz.napa.foxmedia.repository.MyRepository
import uz.napa.foxmedia.request.register.RegisterRequest
import uz.napa.foxmedia.response.regions.Province
import uz.napa.foxmedia.response.regions.Region
import uz.napa.foxmedia.response.register.RegisterResponse
import uz.napa.foxmedia.util.ProvinceData
import uz.napa.foxmedia.util.RegionData
import uz.napa.foxmedia.util.Resource
import uz.napa.foxmedia.util.handleError
import java.io.IOException

@ExperimentalCoroutinesApi
class RegisterViewModel(private val repository: MyRepository) : ViewModel() {

    private val _registerLiveData = MutableLiveData<Resource<RegisterResponse>>()
    val registerLiveData: LiveData<Resource<RegisterResponse>> = _registerLiveData

    private val _province = MutableLiveData<Resource<ProvinceData>>()
    val province: LiveData<Resource<ProvinceData>> = _province

    private val _regions = MutableLiveData<Resource<RegionData>>()
    val regions: LiveData<Resource<RegionData>> = _regions

    init {
        getProvince()
    }

    fun signUp(registerRequest: RegisterRequest) = viewModelScope.launch {
        repository.register(registerRequest)
            .onStart { _registerLiveData.postValue(Resource.Loading()) }
            .catch {
                val context = App.appInstance
                when (it) {
                    is IOException -> {
                        _registerLiveData.postValue(Resource.Error(context.getString(R.string.no_connection)))
                    }
                    is HttpException -> {
                        when {
                            it.code() == 400 -> _registerLiveData.postValue(
                                Resource.Error(
                                    context.getString(
                                        R.string.invalid_input
                                    )
                                )
                            )
                            it.code() == 500 -> _registerLiveData.postValue(
                                Resource.Error(
                                    context.getString(
                                        R.string.server_error
                                    )
                                )
                            )
                            else -> _registerLiveData.postValue(Resource.Error(context.getString(R.string.connection_error)))
                        }
                    }
                    else -> {
                        _registerLiveData.postValue(Resource.Error(context.getString(R.string.unknown_error)))
                    }
                }
            }
            .collect {
                _registerLiveData.postValue(Resource.Success(it))
            }
    }

    fun getProvince() = viewModelScope.launch {
        repository.getProvince()
            .onStart {
                _province.postValue(Resource.Loading())
            }
            .catch {
                _province.postValue(Resource.Error(handleError(it)))
            }
            .collect {
                val provinceList = arrayOfNulls<String>(it.provinces.size)
                var i = 0
                it.provinces.forEach { province ->
                    provinceList[i] = province.name
                    i++
                }
                _province.postValue(
                    Resource.Success(
                        ProvinceData(
                            it.provinces as ArrayList<Province>,
                            provinceList
                        )
                    )
                )
            }
    }

    fun getRegions(provinceId: Long) = viewModelScope.launch {
        repository.getRegions(provinceId)
            .onStart {
                _regions.postValue(Resource.Loading())
            }
            .catch {
                _regions.postValue(Resource.Error(handleError(it)))
            }
            .collect {
                val regionsList = arrayOfNulls<String>(it.regions.size)
                var i = 0
                it.regions.forEach { reg ->
                    regionsList[i] = reg.name
                    i++
                }
                _regions.postValue(
                    Resource.Success(
                        RegionData(
                            it.regions as ArrayList<Region>,
                            regionsList
                        )
                    )
                )
            }
    }
}