package fr.coppernic.agridentwedgesampleup.data

import android.content.Context
import android.util.Log
import fr.coppernic.sdk.agrident.Commands
import fr.coppernic.sdk.agrident.OnDataReceivedListener
import fr.coppernic.sdk.agrident.Parameters
import fr.coppernic.sdk.agrident.Reader
import fr.coppernic.sdk.agrident.ReaderFactory
import fr.coppernic.sdk.core.Defines
import fr.coppernic.sdk.power.PowerManager
import fr.coppernic.sdk.power.api.PowerListener
import fr.coppernic.sdk.power.api.peripheral.Peripheral
import fr.coppernic.sdk.power.impl.cone.ConePeripheral
import fr.coppernic.sdk.utils.core.CpcResult
import fr.coppernic.sdk.utils.io.InstanceListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single

@Single
class ReaderManager(
    private val context: Context,
): PowerListener {

    private var isPortOpened = false
    private var reader: Reader? = null
    private var pendingOperation: (() -> Unit)? = null
    private var dataListener: OnDataReceivedListener? = null

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    // Initialize the reader
    fun initializeReader(onInitialized: (Reader) -> Unit, baudRateValue: Int) {
        // Set the reader manager to busy as initialization is starting
        _isBusy.value = true

        ReaderFactory.getInstance(context, object : InstanceListener<Reader> {
            override fun onCreated(instance: Reader?) {
                instance?.let {
                    reader = it
                    PowerManager.get().registerListener(this@ReaderManager)
                    ConePeripheral.RFID_AGRIDENT_ABR200_GPIO.on(context)

                    // Attempt to open the port and update state
                    val result = openReaderPort(it, baudRateValue)
                    isPortOpened = (result == CpcResult.RESULT.OK) // Update state based on result

                    if (result == CpcResult.RESULT.OK) {
                        Log.d("ReaderManager", "Reader port opened successfully.")
                    } else {
                        Log.e("ReaderManager", "Failed to open reader port. Error code: $result")
                    }

                    onInitialized(it) // Notify that the reader is initialized, regardless of port status
                } ?: run {
                    Log.d("ReaderManager", "Failed to create reader instance.")
                    _isBusy.value = false // Set busy to false if instance creation fails
                }
            }

            override fun onDisposed(instance: Reader?) {
                Log.d("ReaderManager", "Reader instance disposed.")
                reader = null
                isPortOpened = false // Reset the port state if the reader is disposed
                cleanup(context, withPowerOff = false) // Release resources when disposed
                _isBusy.value = false
            }
        })
    }

    // Close the reader
    fun closeReader(onClosed: () -> Unit = {}) {
        if (_isBusy.value) {
            Log.w("ReaderManager", "Cannot close the reader because it is currently busy.")
            return
        }

        _isBusy.value = true
        reader?.let {
            try {
                Log.d("ReaderManager", "Closing reader...")
                it.close()
                Log.d("ReaderManager", "Reader closed successfully.")
            } catch (e: Exception) {
                Log.e("ReaderManager", "Error closing reader: ${e.message}")
            } finally {
                isPortOpened = false // Ensure the state is updated correctly
                cleanup(context) // Release resources
                onClosed() // Notify caller that the reader is closed
            }
        } ?: run {
            Log.w("ReaderManager", "No active reader instance found to close.")
            _isBusy.value = false
            onClosed()
        }
    }

    // Fetch config
    fun fetchConfig(param: Parameters?) {
        // Check if the manager is busy or invalid states exist before proceeding
        if (!canFetchConfig(param)) return

        // Set the busy state to true before starting the operation
        _isBusy.value = true

        try {
            // Execute the configuration fetch and log the result
            when (val result = reader?.getConfig(param)) {
                CpcResult.RESULT.OK -> Log.d("ReaderManager", "Successfully fetched config for ${param?.address}.")
                else -> Log.e("ReaderManager", "Failed to fetch config: $result for ${param?.address}.")
            }
        } finally {
            // Always reset the busy state at the end of the operation
            _isBusy.value = false
        }
    }

    // Switch off/on RFID
    fun toggleRFField(enable: Boolean) {
        if (_isBusy.value) {
            Log.w("ReaderManager", "Cannot toggle RF field because the reader is currently busy.")
            return
        }

        reader?.let {
            val command = if (enable) Commands.SET_RF_ON_CMD else Commands.SET_RF_OFF_CMD

            // Mark as busy before sending command
            _isBusy.value = true
            Log.d("ReaderManager", "Sending command to toggle RF field: ${if (enable) "ON" else "OFF"}")

            // Send command to reader
            val result = it.sendCommand(command)
            when (result) {
                CpcResult.RESULT.OK -> {
                    Log.d("ReaderManager", "RF field toggled successfully: ${if (enable) "ON" else "OFF"}")
                }
                else -> {
                    Log.e("ReaderManager", "Failed to toggle RF field")
                }
            }

            // Reset busy state after command execution
            _isBusy.value = false
        } ?: run {
            Log.w("ReaderManager", "Cannot toggle RF field: Reader is not initialized.")
        }
    }

    // Set config
    fun setConfig(param: Parameters, onComplete: (CpcResult.RESULT) -> Unit) {
        if (_isBusy.value) {
            pendingOperation = { setConfig(param, onComplete) }
            return
        }

        _isBusy.value = true

        reader?.let {
            val result = it.setConfig(param)
            _isBusy.value = false
            onComplete(result)
            executePendingOperation()
        } ?: run {
            _isBusy.value = false
            onComplete(CpcResult.RESULT.ERROR)
            executePendingOperation()
        }
    }

    // Get firmware
    fun getFirmware() {
        // Check if the manager is already busy
        if (_isBusy.value) {
            Log.w("ReaderManager", "Cannot get firmware: Reader is currently busy.")
            return
        }

        reader?.let {
            // Set busy state before sending the command
            _isBusy.value = true

            try {
                // Attempt to send the firmware command
                val result = it.sendCommand(Commands.FIRMWARE_CMD)
                Log.d("ReaderManager", "Result of send command: $result, Reader state: isOpened=${it.isOpened}")

                if (result == CpcResult.RESULT.OK) {
                    Log.d("ReaderManager", "Command sent successfully. Waiting for response...")
                } else {
                    Log.e("ReaderManager", "Failed to send firmware command: $result")
                }
            } finally {
                // Always reset busy state after the operation
                _isBusy.value = false
            }
        } ?: run {
            Log.e("ReaderManager", "Reader is null. Cannot send getFirmware command.")
        }
    }

    // Get serial number
    fun getSN() {
        // Check if the manager is already busy
        if (_isBusy.value) {
            Log.w("ReaderManager", "Cannot get serial number: Reader is currently busy.")
            return
        }

        reader?.let {
            // Set the busy state before sending the command
            _isBusy.value = true

            try {
                // Attempt to send the serial number command
                val result = it.sendCommand(Commands.SNR_CMD)
                Log.d("ReaderManager", "Result of send command: $result, Reader state: isOpened=${it.isOpened}")

                if (result == CpcResult.RESULT.OK) {
                    Log.d("ReaderManager", "Serial number command sent successfully. Waiting for response...")
                } else {
                    Log.e("ReaderManager", "Failed to send serial number command: $result")
                }
            } finally {
                // Always reset the busy state after the operation
                _isBusy.value = false
            }
        } ?: run {
            Log.e("ReaderManager", "Reader is null. Cannot send serial number command.")
        }
    }

    /**
     * Get Amplitude value always returns 0.
     */
    // Get amplitude
    fun getAmplitude() {
        // Check if the manager is already busy
        if (_isBusy.value) {
            Log.w("ReaderManager", "Cannot get amplitude: Reader is currently busy.")
            return
        }

        reader?.let {
            // Set the busy state before sending the command
            _isBusy.value = true

            try {
                // Attempt to send the amplitude command
                val result = it.sendCommand(Commands.GET_AMPLITUDE_CMD)
                Log.d("ReaderManager", "Result of send command: $result, Reader state: isOpened=${it.isOpened}")

                if (result == CpcResult.RESULT.OK) {
                    Log.d("ReaderManager", "Amplitude command sent successfully. Waiting for response...")
                } else {
                    Log.e("ReaderManager", "Failed to send amplitude command: $result")
                }
            } finally {
                // Always reset the busy state after the operation
                _isBusy.value = false
            }
        } ?: run {
            Log.e("ReaderManager", "Failed to send command: Reader is null.")
        }
    }

    // Get Rssi
    fun getRSSI() {
        // Check if the manager is currently busy
        if (_isBusy.value) {
            Log.w("ReaderManager", "Cannot get RSSI: Reader is currently busy.")
            return
        }

        reader?.let {
            // Set the busy state before sending the command
            _isBusy.value = true

            try {
                // Attempt to send the RSSI command
                val result = it.sendCommand(Commands.GET_RSSI_CMD)
                Log.d("ReaderManager", "Result of send command: $result, Reader state: isOpened=${it.isOpened}")

                if (result == CpcResult.RESULT.OK) {
                    Log.d("ReaderManager", "RSSI command sent successfully. Waiting for response...")
                } else {
                    Log.e("ReaderManager", "Failed to send RSSI command: $result")
                }
            } finally {
                // Always reset the busy state after the operation
                _isBusy.value = false
            }
        } ?: run {
            Log.e("ReaderManager", "Failed to send command: Reader is null.")
        }
    }

    // Get FdxRssi
    fun getFdxRssi() {
        // Check if the manager is currently busy
        if (_isBusy.value) {
            Log.w("ReaderManager", "Cannot get FDX RSSI: Reader is currently busy.")
            return
        }

        reader?.let {
            // Set the busy state before sending the command
            _isBusy.value = true

            try {
                val result = it.sendCommand(Commands.GET_AVERAGE_FDX_RSSI_CMD)
                Log.d("ReaderManager", "Result of send command: $result, Reader state: isOpened=${it.isOpened}")

                if (result == CpcResult.RESULT.OK) {
                    Log.d("ReaderManager", "FDX RSSI command sent successfully. Waiting for response...")
                } else {
                    Log.e("ReaderManager", "Failed to send FDX RSSI command: $result")
                }
            } finally {
                // Always reset the busy state after the operation
                _isBusy.value = false
            }
        } ?: run {
            Log.e("ReaderManager", "Failed to send command: Reader is null.")
        }
    }

    // Get HdxRssi
    fun getHdxRssi() {
        // Check if the manager is currently busy
        if (_isBusy.value) {
            Log.w("ReaderManager", "Cannot get HDX RSSI: Reader is currently busy.")
            return
        }

        reader?.let {
            // Set the busy state before sending the command
            _isBusy.value = true

            try {
                val result = it.sendCommand(Commands.GET_AVERAGE_HDX_RSSI_CMD)
                Log.d("ReaderManager", "Result of send command: $result, Reader state: isOpened=${it.isOpened}")

                if (result == CpcResult.RESULT.OK) {
                    Log.d("ReaderManager", "HDX RSSI command sent successfully. Waiting for response...")
                } else {
                    Log.e("ReaderManager", "Failed to send HDX RSSI command: $result")
                }
            } finally {
                // Always reset the busy state after the operation
                _isBusy.value = false
            }
        } ?: run {
            Log.e("ReaderManager", "Failed to send command: Reader is null.")
        }
    }

    // Get HdxFreq
    fun getHdxFreq() {
        // Check if the manager is currently busy
        if (_isBusy.value) {
            Log.w("ReaderManager", "Cannot get HDX frequency: Reader is currently busy.")
            return
        }

        reader?.let {
            // Set the busy state before sending the command
            _isBusy.value = true

            try {
                val result = it.sendCommand(Commands.GET_AVERAGE_HDX_FRQ_CMD)
                Log.d("ReaderManager", "Result of send command: $result, Reader state: isOpened=${it.isOpened}")

                if (result == CpcResult.RESULT.OK) {
                    Log.d("ReaderManager", "HDX frequency command sent successfully. Waiting for response...")
                } else {
                    Log.e("ReaderManager", "Failed to send HDX frequency command: $result")
                }
            } finally {
                // Always reset the busy state after the operation
                _isBusy.value = false
            }
        } ?: run {
            Log.e("ReaderManager", "Failed to send command: Reader is null.")
        }
    }

    fun setOnDataReceivedListener(listener: OnDataReceivedListener) {
        dataListener = listener
        reader?.let {
            it.setOnDataReceivedListener(listener)
            Log.d("ReaderManager", "Data listener set: $listener")
        } ?: Log.e("ReaderManager", "Reader instance is null.")
    }

    override fun onPowerUp(res: CpcResult.RESULT?, peripheral: Peripheral?) {
        Log.d("ReaderManager", "Power up event received. Result: $res, Peripheral: $peripheral")
        _isBusy.value = false // ready for communication
        // Switch off Rfid
        toggleRFField(false)
    }

    override fun onPowerDown(res: CpcResult.RESULT?, peripheral: Peripheral?) {
        Log.d("ReaderManager", "Power down event received.")
        _isBusy.value = false // ready for starting
    }

    private fun openReaderPort(reader: Reader, baudRateValue: Int = 9600): CpcResult.RESULT {
        return try {
            val result = reader.open(Defines.SerialDefines.AGRIDENT_READER_PORT, baudRateValue)
            if (result == CpcResult.RESULT.OK) {
                Log.d("ReaderManager", "Reader port opened successfully.")
            } else {
                Log.e("ReaderManager", "Failed to open reader port. Error code: $result")
            }
            result
        } catch (e: Exception) {
            Log.e("ReaderManager", "Exception opening reader port: ${e.message}")
            CpcResult.RESULT.OPEN_FAIL // Return OPEN_FAIL if an exception occurs
        }
    }

    /**
     * Checks whether fetching the config can proceed based on current state.
     */
    private fun canFetchConfig(param: Parameters?): Boolean {
        return when {
            _isBusy.value -> {
                Log.w("ReaderManager", "Cannot fetch config: Reader is currently busy.")
                false
            }
            param == null -> {
                Log.e("ReaderManager", "Cannot fetch config: Parameter is null.")
                false
            }
            reader == null -> {
                Log.e("ReaderManager", "Cannot fetch config: Reader is null.")
                false
            }
            !isPortOpened -> {
                Log.e("ReaderManager", "Cannot fetch config: Reader port is not opened.")
                false
            }
            else -> true
        }
    }

    // Cleanup when done
    fun cleanup(context: Context, withPowerOff: Boolean = true) {
        // Unregister power management listener
        PowerManager.get().unregisterAll()
        // Release any resources
        PowerManager.get().releaseResources()
        /**
         * Powers off the AGRIDENT_ABR200 module, making it unavailable for all the system.
         * Ensure the module is idle to prevent corruption. Only power off if not needed
         * to optimize battery usage.
         */
        if (withPowerOff) {
            ConePeripheral.RFID_AGRIDENT_ABR200_GPIO.off(context)
        }
        // Change busy state
        _isBusy.value = false
        Log.d("ReaderManager", "Power resources released.")
    }

    private fun executePendingOperation() {
        pendingOperation?.let {
            pendingOperation = null
            it.invoke()
        }
    }
}