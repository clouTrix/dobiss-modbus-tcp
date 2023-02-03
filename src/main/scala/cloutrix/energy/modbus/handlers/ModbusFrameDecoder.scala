package cloutrix.energy.modbus.handlers

import cloutrix.energy.modbus.Modbus
import io.netty.handler.codec.LengthFieldBasedFrameDecoder

/**
 * Modbus TCP Frame Description
 *  - max. 260 Byte (ADU = 7 Byte MBAP + 253 Byte PDU)
 *  - Length field includes Unit Identifier + PDU
 *
 * <----------------------------------------------- ADU -------------------------------------------------------->
 * <---------------------------- MBAP -----------------------------------------><------------- PDU ------------->
 * +------------------------+---------------------+----------+-----------------++---------------+---------------+
 * | Transaction Identifier | Protocol Identifier | Length   | Unit Identifier || Function Code | Data          |
 * | (2 Byte)               | (2 Byte)            | (2 Byte) | (1 Byte)        || (1 Byte)      | (1 - 252 Byte |
 * +------------------------+---------------------+----------+-----------------++---------------+---------------+
 */
class ModbusFrameDecoder extends LengthFieldBasedFrameDecoder(Modbus.ADU_MAX_LENGTH, 4, 2)
