package fr.coppernic.agridentwedgesampleup.ui.screens.setting.model

sealed class GetDataItemClick {
    data object Firmware : GetDataItemClick()
    data object SerialNumber : GetDataItemClick()
    data object Amplitude : GetDataItemClick()
    data object Rssi : GetDataItemClick()
    data object FdxRssi : GetDataItemClick()
    data object HdxRssi : GetDataItemClick()
    data object HdxFreq : GetDataItemClick()
    data object TagType : GetDataItemClick()
    data object Timeout : GetDataItemClick()
    data object BaudRate : GetDataItemClick()
    data object DelayTime : GetDataItemClick()
    data object Output : GetDataItemClick()
}