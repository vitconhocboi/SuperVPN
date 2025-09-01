package com.tici.vpn.proxy.master.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("assign-proxy")
    suspend fun assignProxy(@Body request: ProxyRequest): Response<AssignProxyResponse>

    @POST("disconnect")
    suspend fun disconnect(@Body request: DisconnectRequest): Response<DisconnectResponse>

    @POST("connect")
    suspend fun connect(@Body user: Users): Response<DisconnectResponse>

    @POST("subscription")
    suspend fun subscription(@Body sub: Subscription): Response<DisconnectResponse>

    @GET("get-countries")
    suspend fun getCountries(
        @Query("type") type: String
    ): Response<CountriesResponse>

    @GET("get-countries-v2")
    suspend fun getCountriesV2(): Response<CountriesResponseV2>

    @GET("get-free-countries-v2")
    suspend fun getFreeCountry(): Response<FreeCountryResponse>

    @GET("get-dns")
    suspend fun getDns(): Response<DnsResponse>
}

data class Subscription(
    @SerializedName("user_id") val user_id: String,
    @SerializedName("pack") val pack: String
)

data class Users(
    @SerializedName("user_id") val user_id: String,
    @SerializedName("ip_address") val ip_address: String,
    @SerializedName("type") val type: String
)

data class ProxyRequest(
    @SerializedName("user_id") val user_id: String,
    @SerializedName("country") val country: String,
    @SerializedName("type") val type: String
)

data class AssignProxyResponse(
    @SerializedName("proxy") val proxy: String,
    @SerializedName("error") val error: String?
)

data class DisconnectRequest(
    @SerializedName("user_id") val user_id: String
)

data class DisconnectResponse(
    @SerializedName("message") val message: String
)

data class CountriesResponse(
    @SerializedName("countries") val countries: List<String>,
    @SerializedName("error") val error: String?
)

data class CountriesResponseV2(
    @SerializedName("countries") val countries: List<Country>,
    @SerializedName("error") val error: String?
)

data class FreeCountryResponse(
    @SerializedName("country") val country: String,
    @SerializedName("type") val type: String
)

data class Country(
    @SerializedName("country") val name: String,
    @SerializedName("is_quick_access") val is_quick_access: Int,
    @SerializedName("type") val type: String,
)

data class DnsDB(
    @SerializedName("name") val name: String,
    @SerializedName("ip_address") val ip_address: String
)

data class DnsResponse(
    @SerializedName("dns") val dns: List<DnsDB>,
    @SerializedName("error") val error: String?
)