package com.example.pl2302_android.uart.bean

import com.example.pl2302_android.uart.utils.unsigned

/**
 * state 0 戴着，1 脱落
 *mode 00 成人模式，01 新生儿模式，10 动物模式 (reserved)
 */
class O2Version(bytes: ByteArray) {
    private val sof1 = bytes[0].unsigned().and(128 + 64 + 32 + 16).shr(4)
    private val sof2 = bytes[0].unsigned().and(8 + 4 + 2 + 1)
    val softVersion = "$sof1.$sof2"

    private val hard1 = bytes[1].unsigned().and(128 + 64 + 32 + 16).shr(4)
    private val hard2 = bytes[1].unsigned().and(8 + 4 + 2 + 1)
    val hardVersion = "$hard1.$hard2"
}