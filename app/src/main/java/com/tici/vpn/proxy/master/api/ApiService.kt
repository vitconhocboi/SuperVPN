package com.tici.vpn.proxy.master.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    /** Premium purchase reporting — see PremiumViewModel.subscribeSafe. */
    @POST("subscription")
    suspend fun subscription(@Body sub: Subscription): Response<DisconnectResponse>

    /** DNS server list for the DNS settings screen — see DNSViewModel. */
    @GET("get-dns")
    suspend fun getDns(): Response<DnsResponse>
}

data class Subscription(
    @SerializedName("user_id") val user_id: String,
    @SerializedName("pack") val pack: String
)

/** Generic server acknowledgement. Named after its original `disconnect` caller. */
data class DisconnectResponse(
    @SerializedName("message") val message: String
)

data class DnsDB(
    @SerializedName("name") val name: String,
    @SerializedName("ip_address") val ip_address: String
)

data class DnsResponse(
    @SerializedName("dns") val dns: List<DnsDB>,
    @SerializedName("error") val error: String?
)
