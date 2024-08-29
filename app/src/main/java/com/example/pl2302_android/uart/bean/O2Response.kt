package com.example.pl2302_android.uart.bean

import com.example.pl2302_android.uart.utils.unsigned

class O2Response(bytes: ByteArray) {
    var token: Int = bytes[2].unsigned()
    var len: Int = bytes[3].unsigned()
    var type: Int = bytes[4].unsigned()
    var content: ByteArray = bytes.copyOfRange(5, 5 + len - 2)
}