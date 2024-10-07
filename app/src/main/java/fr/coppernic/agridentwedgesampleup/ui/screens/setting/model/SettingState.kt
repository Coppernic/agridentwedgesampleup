package fr.coppernic.agridentwedgesampleup.ui.screens.setting.model

data class SettingState(
    val firmware: String,
    val serialNumber: String,
    val amplitude: String,
    val rssi: String,
    val fdxRssi: String,
    val hdxRssi: String,
    val hdxFreq: String,
    val output: String,
    val tagType: String,
    val timout: String,
    val baudrate: String,
    val delayTime: String,
) {
    companion object {
        fun toEmpty() = SettingState(
            firmware = "N/A",
            serialNumber = "N/A",
            amplitude = "N/A",
            rssi = "N/A",
            fdxRssi = "N/A",
            hdxRssi = "N/A",
            hdxFreq = "N/A",
            output = "N/A",
            tagType = "N/A",
            timout = "N/A",
            baudrate = "N/A",
            delayTime = "N/A",
        )
    }
}
