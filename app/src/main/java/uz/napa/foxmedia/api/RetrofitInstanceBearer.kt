package uz.napa.foxmedia.api

import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import uz.napa.foxmedia.App
import uz.napa.foxmedia.util.Constants.Companion.BASE_URL
import uz.napa.foxmedia.util.MyPreference
import java.util.concurrent.TimeUnit

class RetrofitInstanceBearer {
    companion object {
        var instance: BearerApi? = null
        fun instance(token: String): BearerApi {
            if (instance == null) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(getHttpClient(token))
                    .addConverterFactory(
                        GsonConverterFactory.create(
                            GsonBuilder().serializeNulls().create()
                        )
                    )
                    .build()
                instance = retrofit.create()
            }
            return instance!!
        }


        private fun getHttpClient(token: String): OkHttpClient {
            val headers = Interceptor { chain ->
                chain.run {
                    proceed(
                        request()
                            .newBuilder()
                            .url(
                                request().url.newBuilder().addQueryParameter("_lang", getLang())
                                    .build()
                            )
                            .addHeader("Authorization", "Bearer $token")
                            .build()
                    )
                }
            }

            return OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(headers)
//                .addInterceptor(ChuckerInterceptor(App.appInstance))
                .build()
        }

        private fun getLang(): String {
            var lang = MyPreference(App.appInstance).getLang()
            if (lang == "uz_") lang = "uz"
            if (lang == "uz") lang = "oz"
            return lang
        }
    }


}