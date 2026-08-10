package com.andyl.iris

import com.andyl.iris.domain.model.GeoPlace
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class GeoPlaceSerializationTest {
    private val json = Json { encodeDefaults = true }

    @Test
    fun placesRoundTrip() {
        val places = listOf(
            GeoPlace("1", "Home", -34.0, -58.0, 2000.0, "pack_x", invert = true)
        )
        val encoded = json.encodeToString(places)
        val decoded = json.decodeFromString<List<GeoPlace>>(encoded)
        assertEquals(places, decoded)
        assertEquals(true, decoded.first().invert)
        println("OK: $encoded")
    }

    @Test
    fun placesDecodeLegacyWithoutInvert() {
        val legacy = """[{"id":"1","name":"Home","latitude":-34.0,"longitude":-58.0,"radiusMeters":2000.0,"packId":"pack_x"}]"""
        val decoded = json.decodeFromString<List<GeoPlace>>(legacy)
        assertEquals(2000.0, decoded.first().radiusMeters, 0.0001)
        assertEquals(false, decoded.first().invert)
        println("OK legacy: $decoded")
    }
}
