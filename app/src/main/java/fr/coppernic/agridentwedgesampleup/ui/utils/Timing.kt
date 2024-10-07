package fr.coppernic.agridentwedgesampleup.ui.utils

enum class Timing(
    val value: Byte,
) {
    T100ms(0x00),
    T50ms(0x01),
    T70ms(0x02),
    VARIABLE_TIMING(0x03),
    ;

    companion object {
        fun fromValue(value: Byte): Timing =
            entries.find { it.value == value } ?: throw IllegalArgumentException("Invalid Timing value: $value")
    }
}
