package com.akmobile.supervpn.settings.dns

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DNSViewModel : ViewModel() {
    val allDNS = MutableLiveData<List<DnsUI>>()

    fun getAllProxy(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            FirebaseApp.initializeApp(context)

            val listDNS = ArrayList<DnsUI>()
            listDNS.add(
                DnsUI(
                    id = "1", name = "Google", server = "8.8.8.8", icon = "", active = false
                )
            )
            listDNS.add(
                DnsUI(
                    id = "2", name = "Cloudflare", server = "1.1.1.1", icon = "", active = false
                )
            )

            allDNS.postValue(listDNS)
        }
    }
}