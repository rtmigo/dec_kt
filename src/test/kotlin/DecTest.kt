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
import kotlin.random.Random


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


    @Test
    fun sumOf() {
        listOf(Dec(1), Dec(2), Dec(3)).sumOf { it }.requireEquals(6)
        listOf<Dec>().sumOf { it }.requireEquals(0)
    }
}
