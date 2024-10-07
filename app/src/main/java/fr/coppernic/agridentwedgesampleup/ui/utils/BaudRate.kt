package fr.coppernic.agridentwedgesampleup.ui.utils

/**
 * Enum class representing standard baud rates and their byte values.
 */
enum class BaudRate(
    val intValue: Int,
    val byteValue: Byte,
) {
    B9600(9600, 0x00),
    B19200(19200, 0x01),
    B38400(38400, 0x02),
    B57600(57600, 0x03),
    B115200(115200, 0x04),
    ;

    companion object {
        /**
         * Retrieves the BaudRate enum associated with the given integer value.
         *
         * @param value the integer value to look up.
         * @return the corresponding BaudRate enum.
         * @throws IllegalArgumentException if the value does not correspond to a valid BaudRate.
         */
        fun fromValue(value: Int): BaudRate =
            entries.find { it.intValue == value }
                ?: throw IllegalArgumentException("Invalid baud rate value: $value")
    }
}
