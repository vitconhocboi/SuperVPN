package com.common.baseui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.common.baseui.extension.PERMISSION_ALL
import com.common.baseui.extension.PERMISSION_WRITE_STORAGE
import com.common.baseui.extension.getPermissionString
import com.common.baseui.lingver.Lingver
import com.common.baseui.util.ConnectionLiveData
import com.simple.libads.AdsActivity


abstract class BaseActivity: AdsActivity() {
    private lateinit var startActivityIntent: ActivityResultLauncher<Intent>
    private var onResult: ((ActivityResult) -> Unit) ?= null

    private lateinit var mAskCheckPermissionCamera: ActivityResultLauncher<Array<String>>
    private lateinit var mAskCheckPermissionStorage: ActivityResultLauncher<Array<String>>
    private var onPermissionStorageGranted: ((isGrant: Boolean) -> Unit)? = null
    private var onPermissionCameraGranted: ((isGrant: Boolean) -> Unit)? = null

    /*private val networkChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("app", "Network connectivity change")
        }
    }

    private fun registerNetworkChangeReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, intentFilter)
    }*/
    private fun changeLanguageAppConfig() {
        if(BaseAppConfig.languageCode.isNotEmpty()) {
            Lingver.getInstance().setLocale(this, BaseAppConfig.languageCode)
        } else {
            Lingver.getInstance().setFollowSystemLocale(this)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeLanguageAppConfig()
        startActivityIntent = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            onResult?.invoke(result)
        }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )


        mAskCheckPermissionStorage =
            (this as ComponentActivity).registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                if (result.entries.firstOrNull { !it.value } == null) {
                    onPermissionStorageGranted?.invoke(true)
                } else {
                    onPermissionStorageGranted?.invoke(false)
                    if(requiredOpenSettingPermission(getPermissionString(PERMISSION_WRITE_STORAGE))) {
                        onRequiredOpenSettingPermission(PERMISSION_WRITE_STORAGE)
                    }
                }
            }

        mAskCheckPermissionCamera =
            (this as ComponentActivity).registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                if (result.entries.firstOrNull { !it.value } == null) {
                    onPermissionCameraGranted?.invoke(true)
                } else {
                    onPermissionCameraGranted?.invoke(false)
                    if(requiredOpenSettingPermission(getPermissionString(PERMISSION_ALL))) {
                        onRequiredOpenSettingPermission(PERMISSION_ALL)
                    }
                }
            }

        val connectionLiveData = ConnectionLiveData(this)
        connectionLiveData.observe(this) {
            onNetworkChange(it)
        }
    }

    // id == PERMISSION_WRITE_STORAGE || PERMISSION_CAMERA || PERMISSION_ALL
    open fun onRequiredOpenSettingPermission(id: Int) {

    }

    open fun onNetworkChange(isConnect: Boolean) {}

    private fun requiredOpenSettingPermission(permissions: Array<String>): Boolean {
        permissions.forEach { permission ->
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    permission
                )
            ) return true
        }
        return false
    }

    fun launchForResult(intent: Intent, onResult: (ActivityResult) -> Unit) {
        this.onResult = onResult
        startActivityIntent.launch(intent)
    }

    fun requestCameraPermission(onPermissionGrant: (isGrant: Boolean) -> Unit) {
        onPermissionCameraGranted = onPermissionGrant
        mAskCheckPermissionCamera.launch(
            getPermissionString(PERMISSION_ALL)
        )
    }

    fun requestStoragePermission(onPermissionGrant: (isGrant: Boolean) -> Unit) {
        onPermissionStorageGranted = onPermissionGrant
        mAskCheckPermissionStorage.launch(
            getPermissionString(PERMISSION_WRITE_STORAGE)
        )
    }

}