package fr.coppernic.agridentwedgesampleup.ui.screens.setting.model

sealed class EditType {
    data object NotVisible: EditType()
    data class TagType(val value: Byte): EditType()
    data class Timeout(val value: Byte): EditType()
    data class BaudRate(val value: Byte): EditType()
    data class OutputFormat(val value: Byte): EditType()
}