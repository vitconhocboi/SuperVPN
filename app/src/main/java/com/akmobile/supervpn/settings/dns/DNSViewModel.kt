package com.akmobile.supervpn.settings.dns

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.common.baseui.BaseAppConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DNSViewModel : ViewModel() {
    val allDNS = MutableLiveData<List<DnsUI>>()

    fun getAllProxy(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            FirebaseApp.initializeApp(context)

            val listDNS = ArrayList<DnsUI>()
            val db = FirebaseFirestore.getInstance()
            val result = db.collection("dns")
                .get()
                .await()
            val dnsActive = BaseAppConfig.dnsServer.split(",")
            for (document in result) {
                for ((field, value) in document.data) {
                    val dnsUI = DnsUI(
                        id = document.id,
                        name = field,
                        server = value.toString(),
                        icon = "",
                        active = dnsActive.contains(value.toString()),
                    )
                    listDNS.add(dnsUI)
                }
            }

            allDNS.postValue(listDNS)
        }
    }
}