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

    @Test
    fun `parse emulator OCR confusion where I replaces 1 before Kc`() {
        val raw = "MMQ\n5L\n2136 IKč\nTATSUNO\n4527L\n4720 KčL"
        val result = parser.parse(raw)

        assertEquals(2136.1, result.totalPrice!!, 0.001)
        assertEquals(45.27, result.liters!!, 0.001)
        assertEquals(47.20, result.pricePerLiter!!, 0.001)
    }

    @Test
    fun `parse OCR confusion where lowercase l replaces 1 before Kc`() {
        val raw = "2136 lKč\n4527L\n4720 KčL"
        val result = parser.parse(raw)

        assertEquals(2136.1, result.totalPrice!!, 0.001)
        assertEquals(45.27, result.liters!!, 0.001)
        assertEquals(47.20, result.pricePerLiter!!, 0.001)
    }

    @Test
    fun `parse OCR confusion with tight I before Kc without space`() {
        val raw = "2136IKč\n4527L\n4720 KčL"
        val result = parser.parse(raw)

        assertEquals(2136.1, result.totalPrice!!, 0.001)
        assertEquals(45.27, result.liters!!, 0.001)
        assertEquals(47.20, result.pricePerLiter!!, 0.001)
    }

    @Test
    fun `parse explicit decimal with I before Kc while preserving lowercase l units`() {
        val raw = "2136.I Kč\n45.27 l\n47.20 Kč/l"
        val result = parser.parse(raw)

        assertEquals(2136.1, result.totalPrice!!, 0.001)
        assertEquals(45.27, result.liters!!, 0.001)
        assertEquals(47.20, result.pricePerLiter!!, 0.001)
    }

    @Test
    fun `parse emulator Logcat output with thousands space and KcIL slash misread`() {
        val raw = "2 136 Kč\n4527L\n420 KčIL"
        val result = parser.parse(raw)

        assertEquals(2136.0, result.totalPrice!!, 0.001)
        assertEquals(45.27, result.liters!!, 0.001)
        assertEquals(42.0, result.pricePerLiter!!, 0.001)
    }

    @Test
    fun `parse thousands space with decimal point and KcIL`() {
        val raw = "2 136.1 Kč\n45.27 L\n4720 KčIL"
        val result = parser.parse(raw)

        assertEquals(2136.1, result.totalPrice!!, 0.001)
        assertEquals(45.27, result.liters!!, 0.001)
        assertEquals(47.20, result.pricePerLiter!!, 0.001)
    }

    @Test
    fun `parse thousands space with I before Kc and Kc per 1 misread`() {
        val raw = "2 136 IKč\n4527L\n47.20 Kč/1"
        val result = parser.parse(raw)

        assertEquals(2136.1, result.totalPrice!!, 0.001)
        assertEquals(45.27, result.liters!!, 0.001)
        assertEquals(47.20, result.pricePerLiter!!, 0.001)
    }

    @Test
    fun `calculate missing pricePerLiter when only totalPrice and liters are read`() {
        val raw = "2 136 Kč\n4527L"
        val result = parser.parse(raw)

        assertEquals(2136.0, result.totalPrice!!, 0.001)
        assertEquals(45.27, result.liters!!, 0.001)
        assertEquals(47.18, result.pricePerLiter!!, 0.01)
    }
}
