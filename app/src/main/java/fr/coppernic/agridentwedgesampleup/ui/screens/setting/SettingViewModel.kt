package fr.coppernic.agridentwedgesampleup.ui.screens.setting

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import fr.coppernic.agridentwedgesampleup.data.ReaderManager
import fr.coppernic.agridentwedgesampleup.ui.screens.setting.model.EditType
import fr.coppernic.agridentwedgesampleup.ui.screens.setting.model.GetDataItemClick
import fr.coppernic.agridentwedgesampleup.ui.screens.setting.model.SettingState
import fr.coppernic.agridentwedgesampleup.ui.utils.BaudRate
import fr.coppernic.agridentwedgesampleup.ui.utils.MeasurementConstants
import fr.coppernic.agridentwedgesampleup.ui.utils.MeasurementConstants.HERTZ
import fr.coppernic.agridentwedgesampleup.ui.utils.MeasurementConstants.MILLIVOLT
import fr.coppernic.agridentwedgesampleup.ui.utils.OutputFormat
import fr.coppernic.agridentwedgesampleup.ui.utils.TagTypes
import fr.coppernic.agridentwedgesampleup.ui.utils.Timing
import fr.coppernic.sdk.agrident.AgridentMessage
import fr.coppernic.sdk.agrident.Ascii
import fr.coppernic.sdk.agrident.CompactCoding
import fr.coppernic.sdk.agrident.DataBlock
import fr.coppernic.sdk.agrident.ISO11784
import fr.coppernic.sdk.agrident.MessageType
import fr.coppernic.sdk.agrident.OnDataReceivedListener
import fr.coppernic.sdk.agrident.Parameters
import fr.coppernic.sdk.agrident.ReaderInformation
import fr.coppernic.sdk.utils.core.CpcBytes
import fr.coppernic.sdk.utils.core.CpcResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.android.annotation.KoinViewModel
import java.io.IOException

@KoinViewModel
class SettingViewModel(
    private val readerManager: ReaderManager,
) : ViewModel(),
    OnDataReceivedListener {
    private var lastTagId = ""
    private var tagReadCount = 0
    private val isAsciiFormat = false
    private val isCompactCoding = false
    private val isIso11784Format = false
    private var currentParam: Parameters? = null

    // Mutable state flow ref
    // from reader manager holding reader state
    val isBusy = readerManager.isBusy

    // Mutable state list holding reader comm state
    private val _isReaderOpen = MutableStateFlow(false)
    val isReaderOpen = _isReaderOpen.asStateFlow()

    // Mutable state flow holding Rfid switch state
    private val _isRFFieldOn = MutableStateFlow(false)
    val isRFFieldOn: StateFlow<Boolean> = _isRFFieldOn.asStateFlow()

    // Mutable state flow to hold screen state
    private val _settingState = MutableStateFlow(SettingState.toEmpty())
    val settingState = _settingState.asStateFlow()

    // Mutable state list to hold log messages
    private val _logMessages = mutableStateListOf<String>()
    val logMessages: MutableList<String> = _logMessages

    // BaudRate enum assumed to be already defined elsewhere
    val baudRates = BaudRate.entries.toList().map { it.name }

    // Using MutableStateFlow for selectedBaudRate
    private val _selectedBaudRate = MutableStateFlow(baudRates[0])
    val selectedBaudRate: StateFlow<String> = _selectedBaudRate.asStateFlow()

    /*// Mutable state flow for the tag read count
    private val _tagReadCount = MutableStateFlow(0)
    val tagReadCount = _tagReadCount.asStateFlow()*/

    fun closeOpenReader() {
        if (_isReaderOpen.value) {
            close()
        } else {
            open()
        }
    }

    private fun open() {
        // Initialize the reader
        readerManager.initializeReader(
            baudRateValue = Integer.valueOf(_selectedBaudRate.value.drop(1)),
            onInitialized = { reader ->
                Log.d("SettingViewModel", "$reader reader is initialized")

                if (reader.isOpened) {
                    Log.d("SettingViewModel", "Reader is connected.")
                    readerManager.setOnDataReceivedListener(this@SettingViewModel)
                } else {
                    Log.e("SettingViewModel", "Reader is not connected.")
                }

                _isReaderOpen.value = true
                toggleRFField(false)
            },
        )
    }

    private fun close() {
        if (isBusy.value) {
            Log.w("SettingViewModel", "Reader is already busy, cannot close now.")
            return
        }

        _logMessages.add("Closing reader...")

        readerManager.closeReader {
            Log.d("SettingViewModel", "Reader closed and resources released.")
            _isRFFieldOn.value = false
            _isReaderOpen.value = false
            _logMessages.add("Reader closed successfully.")
        }
    }

    fun setConfig(
        type: EditType,
        newValue: String,
    ) {
        if (isBusy.value) {
            addLog("Reader is busy. Please wait.", false)
            return
        }

        Log.d("SettingViewModel", "type: $type, newValue: $newValue")

        getParamFromConfig(type, newValue)?.let { param ->
            readerManager.setConfig(param) { result ->
                when (result) {
                    CpcResult.RESULT.OK -> addLog("Configuration updated successfully", false)
                    else -> addLog("Failed to update configuration: $result", false)
                }
            }
        } ?: Log.d("SettingViewModel", "Parameter configuration failed for type: $type, newValue: $newValue")
    }

    private fun getParamFromConfig(
        type: EditType,
        newValue: String,
    ): Parameters? =
        try {
            when (type) {
                is EditType.BaudRate -> {
                    Parameters(Parameters.BAUDRATE).apply {
                        this.value = BaudRate.entries.find { it.name == newValue }?.byteValue
                            ?: throw IllegalArgumentException("Invalid Baudrate value: $newValue")
                    }
                }
                is EditType.OutputFormat -> {
                    Parameters(Parameters.OUTPUT_FORMAT).apply {
                        this.value = OutputFormat.entries.find { it.name == newValue }?.value
                            ?: throw IllegalArgumentException("Invalid OutputFormat value: $newValue")
                    }
                }
                is EditType.TagType -> {
                    Parameters(Parameters.TAG_TYPE).apply {
                        this.value = TagTypes.entries.find { it.name == newValue }?.value
                            ?: throw IllegalArgumentException("Invalid TagType value: $newValue")
                    }
                }
                is EditType.Timeout -> {
                    Parameters(Parameters.TIMING).apply {
                        this.value = Timing.entries.find { it.name == newValue }?.value
                            ?: throw IllegalArgumentException("Invalid TagType value: $newValue")
                    }
                }
                EditType.NotVisible -> throw IllegalArgumentException("Invalid configuration type")
            }
        } catch (e: Exception) {
            Log.e("SettingViewModel", "Error creating parameter from config: ${e.message}")
            null // Return null if there's an error
        }

    fun onBaudRateSelected(newBaudRate: String) {
        _selectedBaudRate.value = newBaudRate
    }

    fun toggleRFField(newValue: Boolean) {
        _isRFFieldOn.value = newValue
        readerManager.toggleRFField(newValue)
    }

    fun clearLogs() {
        logMessages.clear()
        lastTagId = ""
    }

    fun onDataItemClick(itemClick: GetDataItemClick) {
        when (itemClick) {
            is GetDataItemClick.Firmware -> {
                readerManager.getFirmware()
            }
            is GetDataItemClick.SerialNumber -> {
                readerManager.getSN()
            }
            is GetDataItemClick.Amplitude -> {
                readerManager.getAmplitude()
            }
            is GetDataItemClick.Rssi -> {
                readerManager.getRSSI()
            }
            is GetDataItemClick.FdxRssi -> {
                readerManager.getFdxRssi()
            }
            is GetDataItemClick.HdxRssi -> {
                readerManager.getHdxRssi()
            }
            is GetDataItemClick.HdxFreq -> {
                readerManager.getHdxFreq()
            }
            is GetDataItemClick.TagType -> {
                currentParam = Parameters(Parameters.TAG_TYPE)
                readerManager.fetchConfig(currentParam)
            }
            is GetDataItemClick.Timeout -> {
                currentParam = Parameters(Parameters.TIMING)
                readerManager.fetchConfig(currentParam)
            }
            is GetDataItemClick.BaudRate -> {
                currentParam = Parameters(Parameters.BAUDRATE)
                readerManager.fetchConfig(currentParam)
            }
            is GetDataItemClick.DelayTime -> {
                currentParam = Parameters(Parameters.DELAYTIME)
                readerManager.fetchConfig(currentParam)
            }
            is GetDataItemClick.Output -> {
                currentParam = Parameters(Parameters.OUTPUT_FORMAT)
                readerManager.fetchConfig(currentParam)
            }
        }
    }

    override fun onTagIdReceived(
        message: AgridentMessage?,
        res: CpcResult.RESULT?,
    ) {
        // Log the received message
        if (message != null) {
            Log.d("SettingViewModel", "Received Tag ID: ${message.tag}")
            processAgridentData(message)
        } else {
            Log.d("SettingViewModel", "Received message is null")
        }

        // Log the result
        if (res != null) {
            Log.d("SettingViewModel", "Result of Tag ID reception: ${res.name}")
        } else {
            Log.d("SettingViewModel", "Result is null")
        }
    }

    override fun onFirmwareReceived(
        firmware: String?,
        res: CpcResult.RESULT?,
    ) {
        // Check if firmware is null
        if (firmware != null) {
            Log.d("SettingViewModel", "Firmware received: $firmware")
            _settingState.value = _settingState.value.copy(firmware = firmware)
        } else {
            Log.d("SettingViewModel", "Firmware received: null")
        }

        // Log the result of the operation
        Log.d("SettingViewModel", "Result: $res")
    }

    override fun onSerialNumberReceived(
        sn: String?,
        res: CpcResult.RESULT?,
    ) {
        if (sn != null) {
            Log.d("SettingViewModel", "Serial Number received: $sn")
            _settingState.value = _settingState.value.copy(serialNumber = sn)
        } else {
            Log.d("SettingViewModel", "Serial Number received: null")
        }

        // Log the result of the operation
        Log.d("SettingViewModel", "Result: $res")
    }

    override fun onGetConfigReceived(
        value: Byte,
        res: CpcResult.RESULT?,
    ) {
        Log.d("ConfigReceiver", "Received value: $value, Result: ${res?.name}")

        if (res == CpcResult.RESULT.OK) {
            currentParam?.value = value
            updateParameterValue()
        } else {
            Log.e("ConfigReceiver", "Failed to receive config: ${res?.name} with value: $value")
        }
    }

    override fun onReaderInformationReceived(
        name: ReaderInformation,
        value: Int,
    ) {
        when (name.name) {
            MeasurementConstants.RSSI -> {
                _settingState.value = _settingState.value.copy(rssi = "$value $MILLIVOLT")
            }
            MeasurementConstants.AMPLITUDE -> {
                _settingState.value = _settingState.value.copy(amplitude = "$value $MILLIVOLT")
            }
            MeasurementConstants.AVERAGE_HDX_FREQ -> {
                _settingState.value = _settingState.value.copy(hdxFreq = "$value $HERTZ")
            }
            MeasurementConstants.AVERAGE_HDX_RSSI -> {
                _settingState.value = _settingState.value.copy(hdxRssi = "$value $MILLIVOLT")
            }
            MeasurementConstants.AVERAGE_FDX_RSSI -> {
                _settingState.value = _settingState.value.copy(fdxRssi = "$value $MILLIVOLT")
            }
        }
    }

    override fun onCommandAckReceived(
        name: MessageType?,
        ack: Boolean,
    ) {}

    override fun onGetConfigAllReceived(
        params: Array<out Parameters>?,
        res: CpcResult.RESULT?,
    ) {}

    private fun updateParameterValue() {
        when (currentParam?.address) {
            Parameters.BAUDRATE -> {
                val baudRates = BaudRate.entries.toTypedArray()
                for (baudRate in baudRates) {
                    if (baudRate.byteValue == currentParam?.value) {
                        _settingState.value = _settingState.value.copy(baudrate = baudRate.name)
                    }
                }
            }

            Parameters.DELAYTIME -> {
                currentParam?.value?.let { byteValue ->
                    val hexValue = Integer.toHexString(byteValue.toInt())
                    _settingState.value = _settingState.value.copy(delayTime = hexValue)
                }
            }

            Parameters.OUTPUT_FORMAT -> {
                val output = OutputFormat.entries.find { it.value == currentParam?.value }
                output?.let {
                    _settingState.value = _settingState.value.copy(output = it.toString())
                }
            }

            Parameters.TAG_TYPE -> {
                val tagType = TagTypes.entries.find { it.value == currentParam?.value }
                tagType?.let {
                    _settingState.value = _settingState.value.copy(tagType = it.toString())
                }
            }

            Parameters.TIMING -> {
                val timing = Timing.entries.find { it.value == currentParam?.value }
                timing?.let {
                    _settingState.value = _settingState.value.copy(timout = it.toString())
                }
            }

            null -> {
                Log.d("SettingViewModel", "currentParam is null")
            }
        }
    }

    /**
     * Adds a line to the log.
     *
     * @param message Text to be added
     * @param isTagCount Boolean flag indicating if the log is a Tag count incrementation
     */
    private fun addLog(
        message: String,
        isTagCount: Boolean = false,
    ) {
        val maxLogLines = 2500
        val linesToRemove = 500

        // If the log is a Tag count incrementation, modify the last line instead of adding a new one.
        if (isTagCount && _logMessages.isNotEmpty()) {
            val lastMessageIndex = _logMessages.size - 1
            val lastMessage = _logMessages[lastMessageIndex]
            // Assuming the last message is of the format "Count: X"
            if (lastMessage.startsWith("Count: ")) {
                // Update the last message with the new count
                _logMessages[lastMessageIndex] = "Count: $message"
            } else {
                // If the last message isn't a count, add the new count entry
                _logMessages.add(0, "Count: $message")
            }
        } else {
            _logMessages.add(0, message) // Add new message at the top
        }

        // Limit the number of log lines to avoid memory issues
        if (_logMessages.size > maxLogLines) {
            _logMessages.subList(maxLogLines - linesToRemove, _logMessages.size).clear()
        }
    }

    private fun processAgridentData(agridentMsg: AgridentMessage) {
        val msg = agridentMsg.messageType
        if (msg != MessageType.RFID_READ_SUCCESS) {
            lastTagId = ""
        }

        when (msg) {
            MessageType.RFID_READ_SUCCESS -> {
                val sDataRead: DataBlock? = agridentMsg.tag
                if (sDataRead != null) {
                    if (isIso11784Format) {
                        val myIso11784Data = ISO11784()
                        if (CpcResult.RESULT.OK == myIso11784Data.parse(sDataRead.tagData)) {
                            try {
                                val currentTagId = myIso11784Data.tagId.toString()
                                if (lastTagId == currentTagId) {
                                    tagReadCount += 1
                                } else {
                                    lastTagId = currentTagId
                                    tagReadCount = 1 // Reset count to 1 for the new tag
                                    addLog("", false) // Separate logs with an empty line
                                    addLog("TAG type : ${CpcBytes.byteArrayToString(sDataRead.tagType, sDataRead.tagType.size)}", false)
                                    addLog("Country : ${myIso11784Data.countryCode}", false)
                                    addLog("TAG ID : $lastTagId", false)
                                    // addLog("Count: $tagReadCount", false) // Log initial count
                                }
                                if (tagReadCount == 999) {
                                    tagReadCount = 0
                                }
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        } else {
                            addLog("Fail to parse data", false)
                        }
                    } else if (isAsciiFormat) {
                        val myAsciiData = Ascii()
                        if (CpcResult.RESULT.OK == Ascii.parse(sDataRead.tagData)) {
                            val currentTagId = String(myAsciiData.id, Charsets.UTF_8)
                            if (lastTagId == currentTagId) {
                                tagReadCount += 1
                            } else {
                                lastTagId = currentTagId
                                tagReadCount = 1 // Reset count to 1 for the new tag
                                addLog("", false) // Separate logs with an empty line
                                addLog("TAG type : ${String(sDataRead.tagType, Charsets.UTF_8)}", false)
                                addLog("Country : ${String(myAsciiData.countryCode, Charsets.UTF_8)}", false)
                                addLog("TAG ID : $lastTagId", false)
                                // addLog("Count: $tagReadCount", false) // Log initial count
                            }
                            if (tagReadCount == 999) {
                                tagReadCount = 0
                            }
                        } else {
                            addLog("Fail to parse data", false)
                        }
                    } else if (isCompactCoding) {
                        val myCompactCodeData = CompactCoding()
                        if (CpcResult.RESULT.OK == myCompactCodeData.parse(sDataRead.tagData)) {
                            val currentTagId = CpcBytes.byteArrayToString(myCompactCodeData.id, myCompactCodeData.id.size)
                            if (lastTagId == currentTagId) {
                                tagReadCount += 1
                            } else {
                                lastTagId = currentTagId
                                tagReadCount = 1 // Reset count to 1 for the new tag
                                addLog("", false) // Separate logs with an empty line
                                addLog("TAG type : ${CpcBytes.byteArrayToString(sDataRead.tagType, sDataRead.tagType.size)}", false)
                                addLog(
                                    "Country : ${CpcBytes.byteArrayToString(
                                        myCompactCodeData.countryCode,
                                        myCompactCodeData.countryCode.size,
                                    )}",
                                    false,
                                )
                                addLog("TAG ID : $lastTagId", false)
                                // addLog("Count: $tagReadCount", false) // Log initial count
                            }
                            if (tagReadCount == 999) {
                                tagReadCount = 0
                            }
                        } else {
                            addLog("Fail to parse data", false)
                        }
                    } else {
                        val currentTagId = CpcBytes.byteArrayToString(sDataRead.tagData, sDataRead.tagData.size)
                        if (lastTagId == currentTagId) {
                            tagReadCount += 1
                        } else {
                            lastTagId = currentTagId
                            tagReadCount = 1 // Reset count to 1 for the new tag
                            addLog("", false) // Separate logs with an empty line
                            addLog("TAG type : ${CpcBytes.byteArrayToString(sDataRead.tagType, sDataRead.tagType.size)}", false)
                            addLog("TAG ID : $lastTagId", false)
                            // addLog("Count: $tagReadCount", false) // Log initial count
                        }
                        if (tagReadCount == 999) {
                            tagReadCount = 0
                        }
                    }
                }
            }

            MessageType.RFID_UNKNOWN -> {
                val unknownFrame = agridentMsg.data
                addLog("UNKNOWN READER OUTPUT : ${CpcBytes.byteArrayToString(unknownFrame, unknownFrame.size)}", false)
            }

            MessageType.SWITCH_RF_ON_OFF -> // TODO check if RF on or OFF
                if (agridentMsg.isAck) {
                    addLog("RF Field Switched", false)
                } else {
                    addLog("Switch ON/OFF RF Field command failed", false)
                }

            MessageType.GET_RF_STATE_RESULT ->
                if (agridentMsg.isAck) {
                    if (1 == agridentMsg.parameterValue) {
                        _isRFFieldOn.value = true
                        // binding.swRFField.isChecked = true
                        addLog("RF is Activated", false)
                    } else {
                        _isRFFieldOn.value = false
                        // binding.swRFField.isChecked = false
                        addLog("RF is NOT Activated", false)
                    }
                } else {
                    addLog("Failed to get RSSI", false)
                }

            MessageType.SET_SETTINGS_RESULT ->
                if (agridentMsg.isAck) {
                    addLog("New Setting set Successfully", false)
                } else {
                    addLog("Fail to set new Settings", false)
                }

            MessageType.RESET_CONF_RESULT ->
                if (agridentMsg.isAck) {
                    addLog("All Settings are reset", false)
                } else {
                    addLog("Fail to reset Settings", false)
                }

            else -> {
                addLog("Message $msg not treated in handleMessage", false)
            }
        }
    }
}
