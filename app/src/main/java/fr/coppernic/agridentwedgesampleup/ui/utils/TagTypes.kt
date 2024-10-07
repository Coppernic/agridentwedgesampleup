package fr.coppernic.agridentwedgesampleup.ui.utils

enum class TagTypes(val value: Byte) {
    NONE(0x00),
    FDX_B(0x02),
    HDX(0x04),
    FDX_B_HDX(0x06),
    H4002(0x08),
    FDX_B_H4002(0x0A),
    HDX_H4002(0x0C),
    FDX_B_HDX_H4002(0x0E);

    companion object {
        fun fromValue(value: Byte): TagTypes {
            return entries.find { it.value == value } ?: throw IllegalArgumentException("Invalid TagType value: $value")
        }
    }
}