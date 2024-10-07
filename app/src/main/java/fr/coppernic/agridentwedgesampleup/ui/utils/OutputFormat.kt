package fr.coppernic.agridentwedgesampleup.ui.utils

enum class OutputFormat(val value: Byte) {
    ASCII(0x01),
    BYTE_STRUCTURE(0x02),
    COMPACT_CODING(0x03),
    ISO_24631(0x04),
    RAW_DATA(0x06),
    SHORT_ASCII_15(0x07),
    NLIS(0x08),
    CUSTOM_OUTPUT_FORMAT(0x09),
    SHORT_ASCII_16(0x17),
    SCP_FORMAT(0x21);

    companion object {
        fun fromValue(value: Byte): OutputFormat {
            return entries.find { it.value == value } ?: throw IllegalArgumentException("Invalid OutputFormat value: $value")
        }
    }
}