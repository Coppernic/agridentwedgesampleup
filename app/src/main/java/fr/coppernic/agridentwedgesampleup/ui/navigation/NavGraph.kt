package fr.coppernic.agridentwedgesampleup.ui.navigation

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import fr.coppernic.agridentwedgesampleup.ui.components.DisabledScreen
import fr.coppernic.agridentwedgesampleup.ui.screens.main.MainScreen
import fr.coppernic.agridentwedgesampleup.ui.screens.main.MainViewModel
import fr.coppernic.agridentwedgesampleup.ui.screens.setting.EditDataDialog
import fr.coppernic.agridentwedgesampleup.ui.screens.setting.Setting
import fr.coppernic.agridentwedgesampleup.ui.screens.setting.SettingViewModel
import fr.coppernic.agridentwedgesampleup.ui.screens.setting.model.EditType
import org.koin.androidx.compose.koinViewModel

@Composable
fun SetupNavGraph(
    startDestination: String,
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(route = Routes.MAIN_SCREEN) {
            val context = LocalContext.current
            val viewModel: MainViewModel = koinViewModel()
            val mainState by viewModel.mainState.collectAsStateWithLifecycle()

            MainScreen(
                modifier = Modifier,
                isNewScan = mainState.isNewScan,
                dataIntentRead = mainState.intentData,
                onStartScan = { viewModel.startAgridentWedge(context) },
                navigateToSetting = {
                    navController.navigate(Routes.SETTING_SCREEN)
                }
            )

            LifecycleStartEffect(key1 = null) {
                viewModel.registerAgridentReceiver(context)

                onStopOrDispose {
                    viewModel.unregisterReceiver(context)
                }
            }
        }
        composable(route = Routes.SETTING_SCREEN) {
            val viewModel: SettingViewModel = koinViewModel()
            val logMessages = viewModel.logMessages
            val isReaderBusy by viewModel.isBusy.collectAsStateWithLifecycle()
            val isReaderOpen by viewModel.isReaderOpen.collectAsStateWithLifecycle()
            val isRfidActive by viewModel.isRFFieldOn.collectAsStateWithLifecycle()
            val settingState by viewModel.settingState.collectAsStateWithLifecycle()
            val selectedBaud by viewModel.selectedBaudRate.collectAsStateWithLifecycle()
            //val tagReadCount by viewModel.tagReadCount.collectAsStateWithLifecycle()

            var editType by remember { mutableStateOf<EditType>(EditType.NotVisible) }

            BackHandler(!isReaderBusy) {
                Log.i("Setting-BackHandler", "Clicked back")
                navController.popBackStack()
            }

            Setting(
                firmware = settingState.firmware,
                serialNumber = settingState.serialNumber,
                amplitude = settingState.amplitude,
                rssi = settingState.rssi,
                fdxRssi = settingState.fdxRssi,
                hdxRssi = settingState.hdxRssi,
                hdxFreq = settingState.hdxFreq,
                tagType = settingState.tagType,
                timeout = settingState.timout,
                baudRate = settingState.baudrate,
                delayTime = settingState.delayTime,
                output = settingState.output,
                isReaderBusy = isReaderBusy,
                isReaderOpen = isReaderOpen,
                logMessages = logMessages,
                selectedBaudRate = selectedBaud,
                //tagReadCount = tagReadCount,
                baudRates = viewModel.baudRates,
                isRFFieldOn = isRfidActive,
                onDeleteLogs = viewModel::clearLogs,
                onNavigateBack = navController::popBackStack,
                onGetDataItemClick = viewModel::onDataItemClick,
                onRFFieldSwitchChange = viewModel::toggleRFField,
                onEditDataItemClick = { editType = it },
                onCloseOpenReader = viewModel::closeOpenReader,
                onBaudRateSelected = { viewModel.onBaudRateSelected(it) }
            )

            EditDataDialog(
                isVisible = editType !is EditType.NotVisible,
                type = editType,
                onConfirm = {
                    viewModel.setConfig(editType, it)
                    editType = EditType.NotVisible
                },
                onDismiss = { editType = EditType.NotVisible }
            )

            DisabledScreen(isReaderBusy = isReaderBusy)
        }
    }
}