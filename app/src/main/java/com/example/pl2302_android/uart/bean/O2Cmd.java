package com.example.pl2302_android.uart.bean;


import static com.example.pl2302_android.uart.utils.O2CRC.calCRC8;

public class O2Cmd {

    public static int O2_CMD_GET_PRODUCT_ID_INFO = 0x01;
    public static int O2_CMD_GET_PRODUCT_VERSION_INFO = 0x01;
    public static int O2_CMD_GET_STATUS_INFO = 0x02;

    /**
     * 获取产品ID信息
     *
     * @return 数据为长度（n）不超过 30 的字符串（ASCII 码），如“SpO2_LFC_HC_Module” 代表家庭保健类光
     * 频血氧模块，“SpO2_LFC_FC_Module”代表智能指夹式血氧探头产品，“SpO2_LFC_BT_Module”代
     * 表带蓝牙数据传输的光频血氧模块
     */
    public static byte[] getProductIDInfo() {
        int len = 2;
        byte[] cmd = new byte[4 + len];
        cmd[0] = (byte) 0xAA;
        cmd[1] = (byte) 0x55;
        cmd[2] = (byte) 0xFF;
        cmd[3] = (byte) len;
        cmd[4] = (byte) O2_CMD_GET_PRODUCT_ID_INFO;
        cmd[5] = calCRC8(cmd);
        return cmd;
    }


    /**
     * 获取产品版本信息
     *
     * @return 数据 0 – 软件版本 (Ver x.y x -- bit7-bit4 y –bit3-bit0)
     * 数据 1 – 硬件版本 (Ver x.y x — bit7-bit4 y –bit3-bit0)
     */

    public static byte[] getProductVersionInfo() {
        int len = 2;
        byte[] cmd = new byte[4 + len];
        cmd[0] = (byte) 0xAA;
        cmd[1] = (byte) 0x55;
        cmd[2] = (byte) 0x51;
        cmd[3] = (byte) len;
        cmd[4] = (byte) O2_CMD_GET_PRODUCT_VERSION_INFO;
        cmd[5] = calCRC8(cmd);
        return cmd;
    }

    /**
     * 获取状态信息
     * 注：在待机状态下，此报文间隔 2 秒主动上传一次
     *
     * @return 数据
     * D7D6: 00 成人/儿童模式, 01 新生儿模式, 10, 11 预留。
     * D5: 1 上行主动发送允许状态，0 上行主动发送禁止状态
     * D4: 1 Probe disconnected 探头未连接， 0 Probe connected 探头已连接（reserved）
     * D3： 1 Probe off 探头脱落 （手指未插入）
     * D2: 1 Check probe 检查探头 (探头故障或使用不当)
     * D1-D0: 预留（默认置零）
     */
    public static byte[] getStatusInfo() {
        int len = 2;
        byte[] cmd = new byte[4 + len];
        cmd[0] = (byte) 0xAA;
        cmd[1] = (byte) 0x55;
        cmd[2] = (byte) 0x51;
        cmd[3] = (byte) len;
        cmd[4] = (byte) O2_CMD_GET_STATUS_INFO;
        cmd[5] = calCRC8(cmd);
        return cmd;
    }

    /**
     * 设置工作模式
     *
     * @param mode 0x00 为成人模式，0x01 为新生儿模式，0x02-0x03 预留。
     * @return
     */
    public static byte[] setWorkMode(int mode) {
        int len = 3;
        byte[] cmd = new byte[4 + len];
        cmd[0] = (byte) 0xAA;
        cmd[1] = (byte) 0x55;
        cmd[2] = (byte) 0x50;
        cmd[3] = (byte) len;
        cmd[4] = (byte) 0x01;
        cmd[5] = (byte) mode;
        cmd[6] = calCRC8(cmd);
        return cmd;
    }

    /**
     * Master 使能/禁止 Slave 主动发送数据 上电默认为不主动发送数据。
     *
     * @param mode 数据 0x00 禁止 Slave 主动发送数据，0x01,0x02 允许 slave 按照指定的类型主动发送数据。
     * @return
     */
    public static byte[] setEnableActivelySendInfo(int mode) {
        int len = 3;
        byte[] cmd = new byte[4 + len];
        cmd[0] = (byte) 0xAA;
        cmd[1] = (byte) 0x55;
        cmd[2] = (byte) 0x50;
        cmd[3] = (byte) len;
        cmd[4] = (byte) 0x02;
        cmd[5] = (byte) mode;
        cmd[6] = calCRC8(cmd); //(byte) 0x27;
        return cmd;
    }

    /**
     * Master 命令 Slave 进入休眠模式
     *
     * @return
     */

    public static byte[] setSleep() {
        int len = 2;
        byte[] cmd = new byte[4 + len];
        cmd[0] = (byte) 0xAA;
        cmd[1] = (byte) 0x55;
        cmd[2] = (byte) 0x50;
        cmd[3] = (byte) len;
        cmd[4] = (byte) 0x03;
        cmd[5] = calCRC8(cmd);
        return cmd;
    }
}
