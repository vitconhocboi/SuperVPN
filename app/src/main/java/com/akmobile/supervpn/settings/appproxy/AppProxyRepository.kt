package com.akmobile.supervpn.settings.appproxy

import com.akmobile.supervpn.db.VpnAppItemDB
import com.akmobile.supervpn.db.VpnDatabase
import com.common.baseui.ResultData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AppProxyRepository @Inject constructor(private val database: VpnDatabase) :
    AppProxyInterface {
    override fun getAllowApp(): Flow<ResultData<List<VpnAppItemDB>>> = flow {
        val allowApp = database.appVpnDao().getAll()
        emit(ResultData.success(allowApp))
    }

    override fun setAllowApp(item: AppProxyUI): Flow<ResultData<Boolean>> {
        if (item.allowed) {
            database.appVpnDao().insert(item.mapToDB())
        } else {
            database.appVpnDao().delete(item.packageName)
        }
        return flow {
            emit(ResultData.success(true))
        }
    }

    override fun setAllowApps(items: ArrayList<AppProxyUI>): Flow<ResultData<Boolean>> {
        val disallowApps = items.filter { !it.allowed }
        if (disallowApps.isNotEmpty()) {
            database.appVpnDao().deleteAll()
            val listDb = disallowApps.map {
                VpnAppItemDB(it.packageName, it.packageName)
            }
            database.appVpnDao().insertAll(listDb)
        } else {
            database.appVpnDao().deleteAll()
        }
        return flow {
            emit(ResultData.success(true))
        }
    }
}