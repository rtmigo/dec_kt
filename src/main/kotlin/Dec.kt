/**
 * SPDX-FileCopyrightText: (c) 2022 Artёm IG <github.com/rtmigo>
 * SPDX-License-Identifier: MIT
 **/

package io.github.rtmigo.dec

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*
import java.math.*
import kotlin.math.pow

/** Wrapper for [BigDecimal] that keeps results of all operations in the same [MathContext]. */
@Serializable
@JvmInline
value class Dec(
    @Serializable(with = BigDecimalDecSerializer::class)
    val decimal: BigDecimal,
): Comparable<Dec> {
    companion object {
        val context: MathContext = MathContext.DECIMAL64
        val ZERO = Dec(0.0)
        val ONE = Dec(1.0)
    }

    // in Java there are no constructors like BigDecimal(Short) or BigDecimal(Float).
    // So we only implement constructors and other implicitly converting operators for the types,
    // that is easily converted to BigDecimal:
    constructor(num: Int) : this(BigDecimal(num))
    constructor(num: Long) : this(BigDecimal(num))
    constructor(num: Double) : this(BigDecimal.valueOf(num)) // this is kinda slow, but precise
    constructor(num: String) : this(BigDecimal(num))

    ////////////////////////////////////////////////////////////////////////////////////////////////

    override operator fun compareTo(other: Dec) = this.decimal.compareTo(other.decimal)
    operator fun compareTo(other: Double) = this.compareTo(Dec(other))
    operator fun compareTo(other: Int) = this.compareTo(Dec(other))
    operator fun compareTo(other: Long) = this.compareTo(Dec(other))
    operator fun compareTo(other: BigDecimal) = this.compareTo(Dec(other))

    ////////////////////////////////////////////////////////////////////////////////////////////////

    operator fun plus(other: Dec): Dec = Dec(this.decimal.add(other.decimal))
    operator fun plus(other: Int): Dec = this.plus(Dec(other))
    operator fun plus(other: Long): Dec = this.plus(Dec(other))
    operator fun plus(other: Double): Dec = this.plus(Dec(other))
    operator fun plus(other: BigDecimal): Dec = this.plus(Dec(other))

    ////////////////////////////////////////////////////////////////////////////////////////////////

    operator fun minus(other: Dec): Dec =
        Dec(this.decimal.subtract(other.decimal))

    operator fun minus(other: Int): Dec = this.minus(Dec(other))
    operator fun minus(other: Long): Dec = this.minus(Dec(other))
    operator fun minus(other: Double): Dec = this.minus(Dec(other))
    operator fun minus(other: BigDecimal): Dec = this.minus(Dec(other))

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * We generally avoid specifying [MathContext], as extra precision can hurt performance. But
     * here it is just necessary that `125.0 / 100.0` be equal to `1.25`, not `1.2`.
     **/
    operator fun div(other: Dec): Dec = Dec(this.decimal.divide(other.decimal, context))
    operator fun div(other: Int): Dec = this.div(Dec(other))
    operator fun div(other: Long): Dec = this.div(Dec(other))
    operator fun div(other: Double): Dec = this.div(Dec(other))
    operator fun div(other: BigDecimal): Dec = this.div(Dec(other))

    ////////////////////////////////////////////////////////////////////////////////////////////////

    operator fun times(other: Dec): Dec =
        Dec(this.decimal.multiply(other.decimal))

    operator fun times(other: Int): Dec = this.times(Dec(other))
    operator fun times(other: Long): Dec = this.times(Dec(other))
    operator fun times(other: Double): Dec = this.times(Dec(other))
    operator fun times(other: BigDecimal): Dec = this.times(Dec(other))

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Kotlin 1.6 не позволяет перегружать метод equals для value-классов

    fun equalsTo(other: Dec) = compareTo(other) == 0

    fun equalsTo(other: Int) = compareTo(Dec(other)) == 0
    fun equalsTo(other: Long) = compareTo(Dec(other)) == 0
    fun equalsTo(other: Double) = compareTo(Dec(other)) == 0
    fun equalsTo(other: BigDecimal) = compareTo(Dec(other)) == 0

    ////////////////////////////////////////////////////////////////////////////////////////////////

    override fun toString(): String {
        return if (decimal.scale() == 0)
            decimal.toPlainString() + ".0"
        else {
            var result = decimal.toPlainString().dropLastWhile { it == '0' }
            if (result.endsWith('.'))
                result += "0"
            result
        }
    }

    fun toDouble() = this.decimal.toDouble()

    ////////////////////////////////////////////////////////////////////////////////////////////////

    operator fun unaryMinus(): Dec = Dec(decimal.negate())

    /**
     * This method uses [BigDecimal.sqrt], that is available since Java 9. In Java 8 you can use
     * workaround:
     *
     *      real.toDouble().sqrt().toReal()
     **/
    fun sqrt(): Dec = Dec(decimal.sqrt(context))

    fun pow(n: Int): Dec = Dec(decimal.pow(n, context))
    fun pow(n: Double): Dec = decimal.toDouble().pow(n).toDecBin()
    fun abs(): Dec = Dec(this.decimal.abs())
    fun signum(): Int = this.decimal.signum()

    ////////////////////////////////////////////////////////////////////////////////////////////////

    fun max(other: Dec) = max(this, other)
    fun max(other: Double) = max(this, Dec(other))
    fun max(other: Long) = max(this, Dec(other))
    fun max(other: Int) = max(this, Dec(other))
    fun max(other: BigDecimal) = max(this, Dec(other))

    ////////////////////////////////////////////////////////////////////////////////////////////////

    fun min(other: Dec) = min(this, other)
    fun min(other: Double) = min(this, Dec(other))
    fun min(other: Long) = min(this, Dec(other))
    fun min(other: Int) = min(this, Dec(other))
    fun min(other: BigDecimal) = min(this, Dec(other))
    operator fun rangeTo(other: Dec) = DecRange(this, other)
}

data class DecRange(
    override val start: Dec,
    override val endInclusive: Dec
): ClosedRange<Dec> {}

fun Int.toDec() = Dec(this)
fun Long.toDec() = Dec(this)
fun Double.toDec() = Dec(this)
fun Double.toDecBin() = Dec(BigDecimal(this, Dec.context))
fun BigDecimal.toDec() = Dec(this)
fun String.toDec() = Dec(this)

class RequirementFailedException(s: String): Exception(s)

fun Dec.requireEquals(other: Dec) {
    if (!this.equalsTo(other))
        throw RequirementFailedException("$this is not equal to $other")
}

fun Dec.requireEquals(other: Int) = this.requireEquals(Dec(other))
fun Dec.requireEquals(other: Long) = this.requireEquals(Dec(other))
fun Dec.requireEquals(other: Double) = this.requireEquals(Dec(other))

const val DEFAULT_TOLERANCE = 1E-15

fun Dec.isAlmostEquals(other: Dec, tolerance: Double = DEFAULT_TOLERANCE): Boolean {
    return (this/other-1.0).abs() <= tolerance
}

fun Dec.requireAlmostEquals(other: Dec, tolerance: Double = DEFAULT_TOLERANCE) {
    if (!this.isAlmostEquals(other, tolerance))
        throw RequirementFailedException("$this is not close to $other")
}

fun Dec.requireAlmostEquals(other: Double, tolerance: Double = DEFAULT_TOLERANCE) =
    this.requireAlmostEquals(Dec(other), tolerance)

fun Dec.requireAlmostEquals(other: BigDecimal, tolerance: Double = DEFAULT_TOLERANCE) =
    this.requireAlmostEquals(Dec(other), tolerance)

fun Dec.requireAlmostEquals(other: Int, tolerance: Double = DEFAULT_TOLERANCE) =
    this.requireAlmostEquals(Dec(other), tolerance)

fun Dec.requireAlmostEquals(other: Long, tolerance: Double = DEFAULT_TOLERANCE) =
    this.requireAlmostEquals(Dec(other), tolerance)


internal object BigDecimalDecSerializer : KSerializer<BigDecimal> {
    override fun deserialize(decoder: Decoder): BigDecimal =
        if (decoder is JsonDecoder) {
            // декодируем мы менее строго, чем кодируем:
            // принимаем и строки, и double
            val element = decoder.decodeJsonElement()
            val primitive = element as JsonPrimitive
            if (primitive.isString)
                primitive.content.toBigDecimal()
            else
                primitive.double.toBigDecimal()
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

fun max(a: Dec, b: Dec): Dec = if (a > b) a else b
fun min(a: Dec, b: Dec): Dec = if (a < b) a else b

inline fun <T> Iterable<T>.sumOf(selector: (T) -> Dec): Dec {
    var sum: Dec = Dec.ZERO
    for (element in this) {
        sum += selector(element)
    }
    return sum
}