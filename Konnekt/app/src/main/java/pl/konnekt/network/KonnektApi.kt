package pl.konnekt.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://192.168.70.67:8000"

private val okHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient().newBuilder()
    .addInterceptor { chain ->
        val original = chain.request()
        val request = original.newBuilder()
            .header("Content-Type", "application/json")
            .method(original.method, original.body)
            .build()
        chain.proceed(request)
    }
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .client(okHttpClient)
    .build()

object KonnektApi {
    val retrofitService: KonnektApiService by lazy {
        retrofit.create(KonnektApiService::class.java)
    }
}
