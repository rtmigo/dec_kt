/**
 * SPDX-FileCopyrightText: (c) 2022 Artsiom iG (rtmigo.github.io)
 * SPDX-License-Identifier: ISC
 **/


package io.github.rtmigo.dec

import io.kotest.matchers.shouldBe
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.random.Random

class SerializationTest {
    @Test
    fun serializeAsString() {
        val src = Dec(3.141592653589793)
        val json = Json.encodeToString(src)
        json.shouldBe(""""3.141592653589793"""")
        val decoded: Dec = Json.decodeFromString(json)
        decoded.requireEquals(3.141592653589793)
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

    @Test
    fun deserializeFromDifferent() {
        // число было сериализировано как Int

        @Serializable
        data class Sample(val a: Dec, val b: Dec, val c: Dec)

        val json = """{"a": "123.45", "b":5.67, "c":8}"""
        val decoded: Sample = Json.decodeFromString(json)
        decoded.a.requireEquals(123.45)
        decoded.b.requireEquals(5.67)
        decoded.c.requireEquals(8)
    }

    @Test
    fun serializeRandomsExact() {
        for (i in 1..10000) {
            var r = Dec(Random.nextDouble(-1000000.0, 1000000.0))
            while (r > Dec.ZERO) {
                val json = Json.encodeToString(r)
                val decoded: Dec = Json.decodeFromString(json)
                decoded.requireEquals(r)
                r /= Random.nextDouble(-10.0, 10.0)
            }
        }
    }
}