package fr.coppernic.agridentwedgesampleup.ui.screens.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.ViewModel
import fr.coppernic.agridentwedgesampleup.ui.utils.Coppernic.ACTION_AGRIDENT_ERROR
import fr.coppernic.agridentwedgesampleup.ui.utils.Coppernic.ACTION_AGRIDENT_SUCCESS
import fr.coppernic.agridentwedgesampleup.ui.utils.Coppernic.AGRIDENT_WEDGE_Service
import fr.coppernic.agridentwedgesampleup.ui.utils.Coppernic.KEY_BARCODE_DATA
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class MainViewModel: ViewModel() {

    private val _mainState = MutableStateFlow(MainState.noReadState())
    val mainState: StateFlow<MainState> = _mainState.asStateFlow()

    fun startAgridentWedge(context: Context) {
        _mainState.value = _mainState.value.copy(
            isNewScan = true,
            intentData = ""
        )

        if (!isAppInstalled(context)) {
            Log.d("MainViewModel", "Error: Agrident Wedge is not installed")
            return
        }

        // Starts Agrident wedge
        val launchIntent = context.packageManager.getLaunchIntentForPackage(AGRIDENT_WEDGE_Service)
        launchIntent?.let { context.startActivity(it) }
    }

    private val agridentReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // reset state after each reception
            _mainState.value = MainState.noReadState()

            if (intent.action == ACTION_AGRIDENT_SUCCESS) {
                // Displays data read in the intent edit text
                val dataRead = intent.getStringExtra(KEY_BARCODE_DATA)
                _mainState.value = _mainState.value.copy(
                    isNewScan = false,
                    intentData = dataRead ?: ""
                )
                _mainState.value = _mainState.value.copy(isNewScan = false)
            } else if (intent.action == ACTION_AGRIDENT_ERROR) {
                // Displays no data read in intent edit text
                _mainState.value = _mainState.value.copy(
                    isNewScan = false,
                    intentData = "error reading",
                )
            }
        }
    }

    fun registerAgridentReceiver(context: Context) {
        // Registers agrident wedge intent receiver
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_AGRIDENT_SUCCESS)
        intentFilter.addAction(ACTION_AGRIDENT_ERROR)
        context.registerReceiver(agridentReceiver, intentFilter)
    }

    fun unregisterReceiver(context: Context) {
        context.unregisterReceiver(agridentReceiver)
    }

    /**
     * Checks if an application is installed on the device
     *
     * @param context     A context
     * @param packageName Application Id
     * @return True if application is installed
     * */
    private fun isAppInstalled(context: Context, packageName: String = AGRIDENT_WEDGE_Service): Boolean {
        return try {
            context.packageManager.getApplicationInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

}