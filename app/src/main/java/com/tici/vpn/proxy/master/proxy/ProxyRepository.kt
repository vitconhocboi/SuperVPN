package com.tici.vpn.proxy.master.proxy

import com.tici.vpn.proxy.master.db.VpnDatabase
import com.tici.vpn.proxy.master.utils.Constant
import com.common.baseui.ResultData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ProxyRepository @Inject constructor(private val database: VpnDatabase) : ProxyInterface {
    var list = mutableListOf(
        ProxyUI(
            "1",
            Constant.PROXY_HTTP,
            "United States",
            "5.181.164.131",
            "56789",
            "US443764",
            "oQFW9r2p",
            "us",
            true,
        ),
        ProxyUI(
            "2",
            Constant.PROXY_HTTP,
            "United States",
            "193.39.184.34",
            "56789",
            "US391942",
            "gXnU4s4k",
            "us",
        ),
        ProxyUI(
            "3",
            Constant.PROXY_HTTP,
            "United States",
            "185.202.174.179",
            "56789",
            "US178623",
            "pMKZ8q6b",
            "us",
        ),
        ProxyUI(
            "4",
            Constant.PROXY_SOCKS5,
            "United States",
            "193.39.184.60",
            "55555",
            "US126173",
            "dSBH7c4I",
            "us",
        ),
        ProxyUI(
            "5",
            Constant.PROXY_SOCKS5,
            "United States",
            "193.39.184.80",
            "55555",
            "US264150",
            "eTwU4h4p",
            "us",
        ),
        ProxyUI(
            "6",
            Constant.PROXY_SOCKS5,
            "Privoxy",
            "192.168.1.215",
            "1080",
            "test",
            "test",
            "vn",
        ),
    )

//    override fun getAllProxy(): Flow<ResultData<List<ProxyGroupUI>>> = flow {
//        var cached = database.proxyDAO().getAll()
//        if (cached.isNullOrEmpty()) {
//            database.proxyDAO().insertAll(list.map { it.mapToDB() })
//            cached = database.proxyDAO().getAll()
//        }
//        val group = cached.groupBy { it.country }.map {
//            ProxyGroupUI(it.key, it.value.find { it.active } != null, it.value.map { it.mapToUI() })
//        }
//
//        emit(ResultData.success(group))
//    }


    override fun getActiveProxy(): Flow<ResultData<ProxyUI?>> = flow {
        emit(ResultData.success(database.proxyDAO().findActiveProxy()?.mapToUI()))
    }


    override fun setActiveProxy(id: String): Flow<ResultData<Boolean>> {
        database.proxyDAO().updateActiveProxy(id)
        return flow {
            emit(ResultData.success(true))
        }
    }
}