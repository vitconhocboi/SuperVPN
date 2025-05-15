package com.akmobile.supervpn.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("assign-proxy")
    suspend fun assignProxy(@Body request: ProxyRequest): Response<AssignProxyResponse>

    @POST("disconnect")
    suspend fun disconnect(@Body request: DisconnectRequest): Response<DisconnectResponse>

    @GET("get-countries")
    suspend fun getCountries(): Response<CountriesResponse>
}

data class ProxyRequest(
    val user_id: String,
    val country: String
)

data class AssignProxyResponse(
    val proxy: String,
    val error: String?
)

data class DisconnectRequest(
    val user_id: String
)

data class DisconnectResponse(
    val message: String
)

data class CountriesResponse(
    val countries: List<String>,
    val error: String?
)