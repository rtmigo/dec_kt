/**
 * SPDX-FileCopyrightText: (c) 2022 Artsiom iG (rtmigo.github.io)
 * SPDX-License-Identifier: ISC
 **/


package io.github.rtmigo.dec

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*
import java.math.BigDecimal

internal object BdSerializer : KSerializer<BigDecimal> {
    override fun deserialize(decoder: Decoder): BigDecimal =
        if (decoder is JsonDecoder) {
            // декодируем мы менее строго, чем кодируем:
            // принимаем и строки, и double
            val element = decoder.decodeJsonElement()
            val primitive = element as JsonPrimitive
            if (primitive.isString)
                BigDecimal(primitive.content)
            else
                BigDecimal.valueOf(primitive.double)
        }
        else {
            // это гипотетически для двоичных форматов. Пока не тестируется
            decoder.decodeString().toBigDecimal()
        }

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        //val d = value.toDouble()
        //assert(d.toDec().equalsTo(value))
        encoder.encodeString(value.toString())
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)
}