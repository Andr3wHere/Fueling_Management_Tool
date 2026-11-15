package cz.ddt.fuelingmanagementtool

import cz.ddt.fuelingmanagementtool.feature.camera.PumpOcrParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class PumpOcrParserTest {

    private lateinit var parser: PumpOcrParser

    @Before
    fun setUp() {
        parser = PumpOcrParser()
    }

    @Test
    fun `parse ideal state with explicit decimal points`() {
        val raw = "2136.1 Kč\n45.27 L\n47.20 Kč/L"
        val result = parser.parse(raw)

        assertEquals(2136.1, result.totalPrice!!, 0.001)
        assertEquals(45.27, result.liters!!, 0.001)
        assertEquals(47.20, result.pricePerLiter!!, 0.001)
    }

    @Test
    fun `parse good reading with missing decimal points and merged unit`() {
        val raw = "21361 Kč\n4527 L\n4720 KčL"
        val result = parser.parse(raw)

        assertEquals(2136.1, result.totalPrice!!, 0.001)
        assertEquals(45.27, result.liters!!, 0.001)
        assertEquals(47.20, result.pricePerLiter!!, 0.001)
    }

    @Test
    fun `parse noisy input with MMQ 5L and TATSUNO brand`() {
        val raw = "MMQ\n5L\n21361 Kč\nTATSUNO\n4527 L\n4720 KčL"
        val result = parser.parse(raw)

        assertEquals(2136.1, result.totalPrice!!, 0.001)
        assertEquals(45.27, result.liters!!, 0.001)
        assertEquals(47.20, result.pricePerLiter!!, 0.001)
    }

    @Test
    fun `parse noisy input with space instead of decimal point and tight units`() {
        val raw = "MMQ\n5L\n2136 1Kč\nTATSUNO\n4527L\n4720 KčL"
        val result = parser.parse(raw)

        assertEquals(2136.1, result.totalPrice!!, 0.001)
        assertEquals(45.27, result.liters!!, 0.001)
        assertEquals(47.20, result.pricePerLiter!!, 0.001)
    }

    @Test
    fun `calculate missing totalPrice when only liters and pricePerLiter are read`() {
        val raw = "4527 L\n4720 KčL"
        val result = parser.parse(raw)

        assertEquals(45.27, result.liters!!, 0.001)
        assertEquals(47.20, result.pricePerLiter!!, 0.001)
        assertEquals(2136.74, result.totalPrice!!, 0.01)
    }

    @Test
    fun `return nulls on empty or completely invalid input`() {
        val result = parser.parse("TATSUNO\nMMQ\n5L")

        assertNull(result.liters)
        assertNull(result.totalPrice)
        assertNull(result.pricePerLiter)
    }
}
