/**
 * SPDX-FileCopyrightText: (c) 2022 Artёm IG <github.com/rtmigo>
 * SPDX-License-Identifier: MIT
 **/

package io.github.rtmigo.dec

import io.kotest.matchers.shouldBe
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import java.math.BigDecimal
import kotlin.math.round
import kotlin.random.Random
import kotlin.system.measureTimeMillis


internal class DecTest {

    @Test
    fun precision() {
        val pi = 3.141592653589793
        Dec(pi).requireEquals(pi)
        (Dec(pi) * 1000).requireEquals(3141.592653589793)
        (Dec(pi) * 1000 / 1000).requireEquals(pi)
    }

    @Test
    fun doublesAreConvertedPrecisely() {
        for (i in 1..100) {
            val x = Random.nextDouble(-100.0, 100.0)
            Dec(x).toDouble().shouldBe(x)
            x.toDec().toDouble().shouldBe(x)
        }
    }

    @Test
    fun compare() {
        assertTrue(Dec(1.0) == Dec(1.0))
        assertTrue(Dec("1.0").compareTo(Dec("1.000")) == 0)

        assertTrue(Dec("2.0") > Dec("1.55"))
        assertTrue(Dec("2.0") < Dec("3.55"))

        assertTrue(Dec("2.0") <= Dec("2.00"))
        assertTrue(Dec("2.0") >= Dec("2.00"))

        assertTrue(Dec("2.0") <= Dec(2.00))
        assertTrue(Dec("2.0") >= Dec(2.00))

        assertTrue(Dec("2.0") <= Dec(2))
        assertTrue(Dec("2.0") >= Dec(2))
    }

    @Test
    fun valueEquals() {
        assertTrue(Dec("2.0").equalsTo(Dec(2)))
        assertTrue(Dec("2.0").equalsTo(Dec(2.0)))
        assertTrue(Dec("2.0").equalsTo(Dec("2.0000")))

        assertTrue(Dec(2).equalsTo(Dec("2.0")))
        assertTrue(Dec(2.0).equalsTo(Dec("2.0")))
        assertTrue(Dec("2.0000").equalsTo(Dec("2.0")))
    }

    @Test
    fun plus() {
        assertTrue((Dec(1.0) + Dec(2.0)).equalsTo(Dec(3.0)))
        assertTrue((Dec(1.0) + 2.0).equalsTo(3))
        assertTrue((Dec(1.0) + 2).equalsTo(3.0))
    }

    @Test
    fun decBinPlus() {

        val y = 100.0.toDecBin() - 0.1.toDecBin()
        y.requireAlmostEquals(99.9)
        y.toDouble().shouldBe(99.9)
    }


    @Test
    fun minus() {
        (Dec(100.0) - Dec(0.1)).requireEquals(99.9)
        (Dec(100) - Dec(25.1)).requireEquals(74.9)
        (Dec(100) - 25.1).requireEquals(74.9)
        (Dec(100) - 25).requireEquals(75)
    }

    @Test
    fun divide() {
        (Dec(BigDecimal("125")) / 100).requireEquals(1.25)
        (Dec(125) / 100).requireEquals(1.25)
        (Dec(125) / 100L).requireEquals(1.25)
        (Dec(125) / 100.0).requireEquals(1.25)
        (Dec(125) / Dec("100")).requireEquals(1.25)

        (Dec(BigDecimal("125.0")) / Dec(BigDecimal("100.0"))).requireEquals(1.25)

        assert((BigDecimal("125.0") / BigDecimal("100.0")).compareTo(BigDecimal("1.2")) == 0)
    }

    @Test
    fun times() {
        (Dec(1.23) * 5).requireEquals(6.15)
        (Dec(1.23) * 5L).requireEquals(6.15)
        (Dec(1.23) * 5.0).requireEquals(6.15)

        (Dec(0.1) * Dec(0.02)).requireEquals(0.002)
        (Dec(BigDecimal("0.1")) * Dec(BigDecimal("0.02"))).requireEquals(0.002)
    }

    @Test
    fun unaryMinus() {
        (-Dec(1.23)).requireEquals(-1.23)
    }

    @Test
    fun powInt() {
        (Dec(3.0).pow(3)).requireEquals(27.0)
    }

    @Test
    fun powDouble() {
        (Dec(3.0).pow(1.5)).requireAlmostEquals(5.196152422706632)
    }

    @Test
    fun sqrt() {
        (Dec(4.0).sqrt()).requireEquals(2.0)
        (Dec(2.0).sqrt()).requireEquals(1.414213562373095)
    }

    @Test
    fun testToString() {
        (Dec("123.45067").toString()).shouldBe("123.45067")
        (Dec("123.0").toString()).shouldBe("123.0")
        (Dec("123").toString()).shouldBe("123.0")
        (Dec("000123.345000").toString()).shouldBe("123.345")
        (Dec(BigDecimal("000123.345000")).toString()).shouldBe("123.345")
        (Dec(BigDecimal("123")).toString()).shouldBe("123.0")
    }

    @Test
    fun serializeAsString() {
        val src = Dec(3.141592653589793)
        val json = Json.encodeToString(src)
        json.shouldBe(""""3.141592653589793"""")
        val decoded: Dec = Json.decodeFromString(json)
        decoded.requireEquals(3.141592653589793)
    }

    @Test
    fun serializePrecision() {
        for (i in 1..10000) {
            var r = Dec(Random.nextDouble(-1000000.0, 1000000.0))
            while (r > Dec.ZERO) {
                val json = Json.encodeToString(r)
                val decoded: Dec = Json.decodeFromString(json)
                decoded.requireEquals(r)
                r /= Random.nextDouble(-10.0, 10.0)
            }
        }
//        val src = Dec(3.141592653589793)
//        val json = Json.encodeToString(src)
//        json.shouldBe(""""3.141592653589793"""")
//        val decoded: Dec = Json.decodeFromString(json)
//        decoded.shouldBeEqual(3.141592653589793)
    }

    @Test
    fun deserializeFromDouble() {
        // число было ранее сериализировано как Double
        val json = "123.45"
        val decoded: Dec = Json.decodeFromString(json)
        decoded.requireEquals(123.45)
    }

    @Test
    fun deserializeFromInt() {
        // число было сериализировано как Int
        val json = "123"
        val decoded: Dec = Json.decodeFromString(json)
        decoded.requireEquals(123.0)
    }

    /*
    @Test
    fun deserializeFromDifferent() {
        // число было сериализировано как Int

        @Serializable
        data class Sample(val a: Real, val b: Real, val c: Real)

        val json = """{"a": "123.45", "b":5.67, "c":8}"""
        val decoded: Sample = Json.decodeFromString(json)
        decoded.a.shouldBeEqual(123.45)
        decoded.b.shouldBeEqual(5.67)
        decoded.c.shouldBeEqual(8)
    }*/

    @Test
    fun multiPlusPrecise() {
        var result = Dec(0.0)
        for (i in 1..2000)
            result += 0.001
        result.requireEquals(2.0)
    }

    @Test
    fun toRnum() {
        17.toDec().requireEquals(17.0)
        "17.000".toDec().requireEquals(17.0)
        17.0.toDec().requireEquals(17.0)
        17L.toDec().requireEquals(17.0)
    }

    @Test
    fun max() {
        max(Dec(4.0), Dec(7.0)).requireEquals(7.0)
        max(Dec(7.0), Dec(4.0)).requireEquals(7.0)
        max(Dec(7.0), Dec(7.0)).requireEquals(7.0)

        Dec(4.0).max(Dec(7.0)).requireEquals(7.0)
        Dec(4.0).max(7.0).requireEquals(7.0)
        Dec(4.0).max(7).requireEquals(7.0)
        Dec(4.0).max(7L).requireEquals(7.0)
        Dec(4.0).max(BigDecimal(7)).requireEquals(7.0)
    }

    @Test
    fun min() {
        min(Dec(4.0), Dec(7.0)).requireEquals(4.0)
        min(Dec(7.0), Dec(4.0)).requireEquals(4.0)
        min(Dec(7.0), Dec(7.0)).requireEquals(7.0)

        Dec(4.0).min(Dec(7.0)).requireEquals(4.0)
        Dec(4.0).min(7.0).requireEquals(4.0)
        Dec(4.0).min(7L).requireEquals(4.0)
        Dec(4.0).min(7).requireEquals(4.0)
        Dec(4.0).min(BigDecimal(7)).requireEquals(4.0)
    }

    @Test
    fun signum() {
        Dec(-123.45).signum().shouldBe(-1)
        Dec(123.45).signum().shouldBe(1)
        Dec(0.0).signum().shouldBe(0)
        Dec(-0.0).signum().shouldBe(0)
    }

    @Test
    fun sqrtAndPow() {
        val num = Dec(5)
        num.sqrt().requireAlmostEquals(num.pow(0.5))
    }

    @Disabled
    @Test
    fun draftBench() {


        for (attempt in 1..2) {
            val N = 10000000L
            measureTimeMillis {
                var sum = 0.0
                for (i in 1..N) {
                    val inc = round2(i / 100.00)
                    sum = round2(sum + inc)
                }
                //println(sum)
            }
                .let { println("double: $it") }

            measureTimeMillis {
                var sum = Dec("0.0")
                //val addend = Real("0.0001")

                val divisor = Dec("100")
                //val fact = Real("0.01")
                for (i in 1L..N) {
                    val inc = Dec(i) / divisor
                    sum = sum + inc
                }
                //println(sum)
            }.let { println("real: $it") }


            measureTimeMillis {
                var sum = BigDecimal("0.00000000000000")
                val fact = BigDecimal("0.01")
                val divisor = BigDecimal("100")
                for (i in 1L..N) {
                    val inc = BigDecimal(i) / divisor
                    sum = sum + inc
                }
                //println(sum)
            }.let { println("bigdecimal: $it") }

//            measureTimeMillis {
//                var x = BigDecimal("0.0")
//                val addend = BigDecimal("0.0001")
//                for (i in 1..N)
//                    x+=addend
//                x
//            }.let { println("bd: $it") }


        }

    }

    @Test
    fun sumOf() {
        listOf(Dec(1), Dec(2), Dec(3)).sumOf { it }.requireEquals(6)
        listOf<Dec>().sumOf { it }.requireEquals(0)
    }

}

inline fun round2(x: Double) = round(x * 100.0) / 100.00