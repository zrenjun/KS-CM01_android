package com.example.pl2302_android.uart.bean

import com.example.pl2302_android.uart.utils.toUInt
import com.example.pl2302_android.uart.utils.unsigned

/**
 * state 0 戴着，1 脱落
 *mode 00 成人模式，01 新生儿模式，10 动物模式 (reserved)
 */
class O2Data(bytes: ByteArray) {
    var o2: Int = bytes[0].unsigned()
    var pr: Int = toUInt(bytes.copyOfRange(1, 3))
    var pi: Int = bytes[3].unsigned()
    var state: Int = bytes[4].unsigned().and(2).shr(1)
    var mode: Int = bytes[4].unsigned().and(128 + 64).shr(6)
}