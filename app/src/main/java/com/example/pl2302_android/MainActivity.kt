package com.example.pl2302_android

import android.annotation.SuppressLint
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pl2302_android.databinding.ActivityMainBinding
import com.example.pl2302_android.uart.bean.O2Cmd
import com.example.pl2302_android.uart.bean.O2Data
import com.example.pl2302_android.uart.bean.O2Response
import com.example.pl2302_android.uart.bean.O2Version
import com.example.pl2302_android.uart.utils.toUInt
import com.example.pl2302_android.uart.utils.O2CRC
import com.example.pl2302_android.uart.utils.add
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * usb库使用
 * https://github.com/mik3y/usb-serial-for-android
 */
class MainActivity : AppCompatActivity(), SerialInputOutputManager.Listener {

    private var pool: ByteArray? = null
    private var port: UsbSerialPort? = null
    private lateinit var binding: ActivityMainBinding
    private var isHasWave = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.getId.setOnClickListener {
            writeToUartDevice(O2Cmd.getProductIDInfo())
        }

        binding.getVersion.setOnClickListener {//插了手指，再发版本命令才有应答
            writeToUartDevice(O2Cmd.getProductVersionInfo())
        }
        binding.getWave.setOnClickListener {
            Toast.makeText(this, "使能波形命令", Toast.LENGTH_SHORT).show()
            writeToUartDevice(O2Cmd.setEnableActivelySendInfo(1))
        }

        Thread(binding.waveView, "DrawPOD_Thread").start()
    }

    override fun onDestroy() {
        port?.close()
        binding.waveView.Stop()
        super.onDestroy()
    }


    /**
     * usb插入会自动调用onResume
     */
    public override fun onResume() {
        super.onResume()
        synchronized(this) {
            val manager = getSystemService(USB_SERVICE) as UsbManager
            val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
            if (availableDrivers.isEmpty()) {
                return
            }

            val driver = availableDrivers[0]
            val connection = manager.openDevice(driver.device) ?: return
            port = driver.ports[0]
            port?.open(connection)
            port?.setParameters(38400, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
            val usbIoManager = SerialInputOutputManager(port, this)
            usbIoManager.start()
        }
    }

    private fun writeToUartDevice(bytes: ByteArray) {
        try {
            port?.write(bytes, 1000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun handleDataPool(bytes: ByteArray?): ByteArray? {
        if (bytes == null || bytes.size < 6) {
            return bytes
        }
        loop@ for (i in 0 until bytes.size - 5) {
            if (bytes[i] != 0xAA.toByte() || bytes[i + 1] != 0x55.toByte()) {
                continue@loop
            }
            // need content length
            val len = toUInt(bytes.copyOfRange(i + 3, i + 4))
            if (i + 4 + len > bytes.size) {
                continue@loop
            }
            val temp = bytes.copyOfRange(i, i + 4 + len)
            if (temp.last() == O2CRC.calCRC8(temp)) {
                val o2Response = O2Response(temp)
                MainScope().launch {
                    onResponseReceived(o2Response)
                }
                val tempBytes = if (i + 4 + len == bytes.size) null else bytes.copyOfRange(
                    i + 4 + len,
                    bytes.size
                )
                return handleDataPool(tempBytes)
            }
        }
        return bytes
    }

    @SuppressLint("SetTextI18n")
    private fun onResponseReceived(response: O2Response) {
        when (response.token) {
            0x51 -> {
                when (response.len) {
                    0x04 -> {
                        when (response.type) {
                            0x01 -> {
                                val data = O2Version(response.content)
                                Log.e(
                                    "vaca",
                                    "response:software:${data.softVersion},hardware:${data.hardVersion}"
                                )
                                binding.info.text =
                                    "软件版本:${data.softVersion},硬件版本:${data.hardVersion}"
                            }
                        }
                    }
                }
            }

            0x53 -> {
                when (response.len) {
                    0x07 -> {
                        when (response.type) {
                            0x01 -> {
                                val data = O2Data(response.content)
                                Log.e(
                                    "vaca",
                                    "response:o2:${data.o2},pr:${data.pr} status:${data.state}  mode:${data.mode}"
                                )
                                binding.o2.text = "SpO2:${data.o2}"
                                binding.pr.text = "PR:${data.pr}"
                                binding.pi.text = "PI:${data.pi / 10f}"
                                if (data.state == 0x01) {
                                    binding.state.text = "状态:探头脱落"
                                    if (!isHasWave) {
                                        Toast.makeText(this, "使能波形命令", Toast.LENGTH_SHORT)
                                            .show()
                                        writeToUartDevice(O2Cmd.setEnableActivelySendInfo(1))
                                    }
                                    isHasWave = false
                                } else {
                                    binding.state.text = "状态:正常"
                                }
                            }
                        }
                    }
                }
            }

            0xff -> {
                when (response.type) {
                    0x01 -> {
                        val data = response.content
                        val string = String(data)
                        Log.e("vaca", "response: $string")
                        binding.info.text = "版本ID: $string"
                    }
                }
            }
            //add by frf 2023/10/19
            0x52 -> {//wave
                if (response.len == 3) {
                    isHasWave = true
                    val datas = response.content //不含crc
                    val pulseFlag = (datas[0].toInt() shr 7) and 0x01
                    /*有无搏动*/
                    binding.imageHeart.visibility = if (pulseFlag == 0 ) View.INVISIBLE else View.VISIBLE
                    val waveDate = datas[0].toInt() and 0x7f
                    binding.waveView.addData(waveDate)
                }
            }
        }
    }

    override fun onNewData(data: ByteArray) {
        pool = add(pool, data)
        pool = handleDataPool(pool)
    }

    override fun onRunError(e: Exception?) {

    }
}