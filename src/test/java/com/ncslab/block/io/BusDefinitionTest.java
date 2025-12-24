package com.ncslab.block.io;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Comprehensive test suite for BusDefinition class.
 *
 * Tests cover:
 * - Builder pattern usage
 * - Validation (name, elements, scope, circular references)
 * - Element access methods
 * - Dimension calculations
 * - Compatibility checking
 * - Virtual vs non-virtual buses
 * - Nested bus structures
 *
 * @author NCSLab
 * @since Phase 2 - Bus Architecture Implementation
 */
public class BusDefinitionTest {

    // === Basic Builder Pattern Tests ===

    @Test
    public void testSimpleBusDefinition() {
        BusDefinition sensorBus = BusDefinition.builder()
            .name("SensorBus")
            .description("Temperature and pressure sensors")
            .isVirtual(true)
            .element(BusElementDefinition.scalar("temperature", "double"))
            .element(BusElementDefinition.scalar("pressure", "double"))
            .build();

        assertNotNull("Bus definition should not be null", sensorBus);
        assertEquals("Bus name should match", "SensorBus", sensorBus.getName());
        assertEquals("Should have 2 elements", 2, sensorBus.getElementCount());
        assertTrue("Should be virtual", sensorBus.isVirtual());
        assertEquals("Scope should be Global", "Global", sensorBus.getScope());
    }

    @Test
    public void testNonVirtualBus() {
        BusDefinition canBus = BusDefinition.builder()
            .name("CANBus")
            .description("CAN bus message structure")
            .isVirtual(false)
            .scope("Global")
            .element(BusElementDefinition.scalar("id", "uint32"))
            .element(BusElementDefinition.vector("data", "uint8", 8))
            .build();

        assertFalse("Should be non-virtual", canBus.isVirtual());
        assertEquals("Total width should be 9 (1 scalar + 8 vector)", 9, canBus.getTotalWidth());
    }

    @Test
    public void testLocalScopeBus() {
        BusDefinition localBus = BusDefinition.builder()
            .name("LocalBus")
            .scope("Local")
            .element(BusElementDefinition.scalar("value", "double"))
            .build();

        assertEquals("Scope should be Local", "Local", localBus.getScope());
    }

    // === Element Access Tests ===

    @Test
    public void testGetElement() {
        BusDefinition bus = BusDefinition.builder()
            .name("TestBus")
            .element(BusElementDefinition.scalar("temp", "double"))
            .element(BusElementDefinition.scalar("pressure", "double"))
            .build();

        bus.validate(); // Initialize element maps

        BusElementDefinition tempElement = bus.getElement("temp");
        assertNotNull("Temperature element should exist", tempElement);
        assertEquals("Element name should match", "temp", tempElement.getName());

        BusElementDefinition notExist = bus.getElement("nonexistent");
        assertNull("Nonexistent element should be null", notExist);
    }

    @Test
    public void testGetElementIndex() {
        BusDefinition bus = BusDefinition.builder()
            .name("TestBus")
            .element(BusElementDefinition.scalar("first", "double"))
            .element(BusElementDefinition.scalar("second", "double"))
            .element(BusElementDefinition.scalar("third", "double"))
            .build();

        bus.validate();

        assertEquals("First element index should be 0", 0, bus.getElementIndex("first"));
        assertEquals("Second element index should be 1", 1, bus.getElementIndex("second"));
        assertEquals("Third element index should be 2", 2, bus.getElementIndex("third"));
        assertEquals("Nonexistent element index should be -1", -1, bus.getElementIndex("nonexistent"));
    }

    @Test
    public void testHasElement() {
        BusDefinition bus = BusDefinition.builder()
            .name("TestBus")
            .element(BusElementDefinition.scalar("temp", "double"))
            .build();

        bus.validate();

        assertTrue("Should have temp element", bus.hasElement("temp"));
        assertFalse("Should not have nonexistent element", bus.hasElement("nonexistent"));
    }

    @Test
    public void testGetElementNames() {
        BusDefinition bus = BusDefinition.builder()
            .name("TestBus")
            .element(BusElementDefinition.scalar("first", "double"))
            .element(BusElementDefinition.scalar("second", "double"))
            .element(BusElementDefinition.scalar("third", "double"))
            .build();

        bus.validate();

        assertEquals("Should have 3 element names", 3, bus.getElementNames().size());
        assertTrue("Should contain 'first'", bus.getElementNames().contains("first"));
        assertTrue("Should contain 'second'", bus.getElementNames().contains("second"));
        assertTrue("Should contain 'third'", bus.getElementNames().contains("third"));
    }

    // === Dimension Calculation Tests ===

    @Test
    public void testTotalWidthScalars() {
        BusDefinition bus = BusDefinition.builder()
            .name("ScalarBus")
            .element(BusElementDefinition.scalar("a", "double"))
            .element(BusElementDefinition.scalar("b", "double"))
            .element(BusElementDefinition.scalar("c", "double"))
            .build();

        assertEquals("Total width should be 3 scalars", 3, bus.getTotalWidth());
    }

    @Test
    public void testTotalWidthVectors() {
        BusDefinition bus = BusDefinition.builder()
            .name("VectorBus")
            .element(BusElementDefinition.scalar("scalar", "double"))
            .element(BusElementDefinition.vector("vec4", "double", 4))
            .element(BusElementDefinition.vector("vec8", "double", 8))
            .build();

        assertEquals("Total width should be 1 + 4 + 8 = 13", 13, bus.getTotalWidth());
    }

    @Test
    public void testTotalWidthMatrices() {
        BusDefinition bus = BusDefinition.builder()
            .name("MatrixBus")
            .element(BusElementDefinition.scalar("scalar", "double"))
            .element(BusElementDefinition.matrix("mat3x3", "double", 3, 3))
            .element(BusElementDefinition.matrix("mat2x4", "double", 2, 4))
            .build();

        assertEquals("Total width should be 1 + 9 + 8 = 18", 18, bus.getTotalWidth());
    }

    @Test
    public void testTotalWidthNestedBus() {
        BusDefinition sensorBus = BusDefinition.builder()
            .name("SensorBus")
            .element(BusElementDefinition.scalar("temp", "double"))
            .element(BusElementDefinition.scalar("pressure", "double"))
            .build();

        BusDefinition vehicleBus = BusDefinition.builder()
            .name("VehicleBus")
            .element(BusElementDefinition.scalar("speed", "double"))
            .element(BusElementDefinition.vector("position", "double", 3))
            .element(BusElementDefinition.nestedBus("sensors", sensorBus))
            .build();

        assertEquals("Sensor bus total width should be 2", 2, sensorBus.getTotalWidth());
        assertEquals("Vehicle bus total width should be 1 + 3 + 2 = 6", 6, vehicleBus.getTotalWidth());
    }

    // === Validation Tests ===

    @Test
    public void testValidBusDefinition() {
        BusDefinition bus = BusDefinition.builder()
            .name("ValidBus")
            .element(BusElementDefinition.scalar("temp", "double"))
            .build();

        // Should not throw exception
        bus.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void testValidationNullName() {
        BusDefinition bus = BusDefinition.builder()
            .name(null)
            .element(BusElementDefinition.scalar("temp", "double"))
            .build();

        bus.validate(); // Should throw
    }

    @Test(expected = IllegalStateException.class)
    public void testValidationEmptyName() {
        BusDefinition bus = BusDefinition.builder()
            .name("")
            .element(BusElementDefinition.scalar("temp", "double"))
            .build();

        bus.validate(); // Should throw
    }

    @Test(expected = IllegalStateException.class)
    public void testValidationInvalidNameFormat() {
        BusDefinition bus = BusDefinition.builder()
            .name("123InvalidName") // Starts with number
            .element(BusElementDefinition.scalar("temp", "double"))
            .build();

        bus.validate(); // Should throw
    }

    @Test(expected = IllegalStateException.class)
    public void testValidationNoElements() {
        BusDefinition bus = BusDefinition.builder()
            .name("EmptyBus")
            .build();

        bus.validate(); // Should throw
    }

    @Test(expected = IllegalStateException.class)
    public void testValidationInvalidScope() {
        BusDefinition bus = BusDefinition.builder()
            .name("TestBus")
            .scope("InvalidScope")
            .element(BusElementDefinition.scalar("temp", "double"))
            .build();

        bus.validate(); // Should throw
    }

    @Test(expected = IllegalStateException.class)
    public void testValidationDuplicateElementNames() {
        BusDefinition bus = BusDefinition.builder()
            .name("DuplicateBus")
            .element(BusElementDefinition.scalar("temp", "double"))
            .element(BusElementDefinition.scalar("temp", "double")) // Duplicate name
            .build();

        bus.validate(); // Should throw
    }

    @Test(expected = IllegalStateException.class)
    public void testValidationCircularReference() {
        // Create a bus that references itself (circular reference)
        BusDefinition.BusDefinitionBuilder builder = BusDefinition.builder()
            .name("CircularBus")
            .element(BusElementDefinition.scalar("value", "double"));

        BusDefinition bus = builder.build();

        // Try to add the bus as a nested element to itself (this creates circular reference)
        BusDefinition circularBus = BusDefinition.builder()
            .name("CircularBus")
            .element(BusElementDefinition.scalar("value", "double"))
            .element(BusElementDefinition.nestedBus("self", bus))
            .build();

        circularBus.validate(); // Should throw
    }

    // === Compatibility Tests ===

    @Test
    public void testCompatibilityIdentical() {
        BusDefinition bus1 = BusDefinition.builder()
            .name("Bus1")
            .element(BusElementDefinition.scalar("temp", "double"))
            .element(BusElementDefinition.scalar("pressure", "double"))
            .build();

        BusDefinition bus2 = BusDefinition.builder()
            .name("Bus2")
            .element(BusElementDefinition.scalar("temp", "double"))
            .element(BusElementDefinition.scalar("pressure", "double"))
            .build();

        assertTrue("Identical structure should be compatible", bus1.isCompatibleWith(bus2));
    }

    @Test
    public void testCompatibilityDifferentElementCount() {
        BusDefinition bus1 = BusDefinition.builder()
            .name("Bus1")
            .element(BusElementDefinition.scalar("temp", "double"))
            .build();

        BusDefinition bus2 = BusDefinition.builder()
            .name("Bus2")
            .element(BusElementDefinition.scalar("temp", "double"))
            .element(BusElementDefinition.scalar("pressure", "double"))
            .build();

        assertFalse("Different element counts should be incompatible", bus1.isCompatibleWith(bus2));
    }

    @Test
    public void testCompatibilityDifferentElementNames() {
        BusDefinition bus1 = BusDefinition.builder()
            .name("Bus1")
            .element(BusElementDefinition.scalar("temp", "double"))
            .build();

        BusDefinition bus2 = BusDefinition.builder()
            .name("Bus2")
            .element(BusElementDefinition.scalar("temperature", "double"))
            .build();

        assertFalse("Different element names should be incompatible", bus1.isCompatibleWith(bus2));
    }

    @Test
    public void testCompatibilityDifferentDataTypes() {
        BusDefinition bus1 = BusDefinition.builder()
            .name("Bus1")
            .element(BusElementDefinition.scalar("value", "double"))
            .build();

        BusDefinition bus2 = BusDefinition.builder()
            .name("Bus2")
            .element(BusElementDefinition.scalar("value", "int32"))
            .build();

        assertFalse("Different data types should be incompatible", bus1.isCompatibleWith(bus2));
    }

    @Test
    public void testCompatibilityDifferentDimensions() {
        BusDefinition bus1 = BusDefinition.builder()
            .name("Bus1")
            .element(BusElementDefinition.vector("vec", "double", 4))
            .build();

        BusDefinition bus2 = BusDefinition.builder()
            .name("Bus2")
            .element(BusElementDefinition.vector("vec", "double", 8))
            .build();

        assertFalse("Different dimensions should be incompatible", bus1.isCompatibleWith(bus2));
    }

    @Test
    public void testCompatibilityNullOther() {
        BusDefinition bus = BusDefinition.builder()
            .name("Bus")
            .element(BusElementDefinition.scalar("temp", "double"))
            .build();

        assertFalse("Null bus should be incompatible", bus.isCompatibleWith(null));
    }

    // === Nested Bus Tests ===

    @Test
    public void testNestedBusValidation() {
        BusDefinition innerBus = BusDefinition.builder()
            .name("InnerBus")
            .element(BusElementDefinition.scalar("value", "double"))
            .build();

        BusDefinition outerBus = BusDefinition.builder()
            .name("OuterBus")
            .element(BusElementDefinition.scalar("id", "uint32"))
            .element(BusElementDefinition.nestedBus("inner", innerBus))
            .build();

        // Should not throw exception
        outerBus.validate();

        assertEquals("Outer bus total width should be 1 + 1 = 2", 2, outerBus.getTotalWidth());
    }

    @Test
    public void testDeepNestedBus() {
        BusDefinition level3 = BusDefinition.builder()
            .name("Level3")
            .element(BusElementDefinition.scalar("value", "double"))
            .build();

        BusDefinition level2 = BusDefinition.builder()
            .name("Level2")
            .element(BusElementDefinition.scalar("id", "uint32"))
            .element(BusElementDefinition.nestedBus("level3", level3))
            .build();

        BusDefinition level1 = BusDefinition.builder()
            .name("Level1")
            .element(BusElementDefinition.scalar("timestamp", "uint64"))
            .element(BusElementDefinition.nestedBus("level2", level2))
            .build();

        level1.validate();

        assertEquals("Level 3 total width should be 1", 1, level3.getTotalWidth());
        assertEquals("Level 2 total width should be 1 + 1 = 2", 2, level2.getTotalWidth());
        assertEquals("Level 1 total width should be 1 + 2 = 3", 3, level1.getTotalWidth());
    }

    // === Utility Method Tests ===

    @Test
    public void testToString() {
        BusDefinition bus = BusDefinition.builder()
            .name("TestBus")
            .scope("Global")
            .isVirtual(true)
            .element(BusElementDefinition.scalar("temp", "double"))
            .element(BusElementDefinition.scalar("pressure", "double"))
            .build();

        String str = bus.toString();
        assertNotNull("toString should not be null", str);
        assertTrue("toString should contain bus name", str.contains("TestBus"));
        assertTrue("toString should contain scope", str.contains("Global"));
        assertTrue("toString should contain virtual flag", str.contains("virtual=true"));
    }

    @Test
    public void testToStructureString() {
        BusDefinition sensorBus = BusDefinition.builder()
            .name("SensorBus")
            .description("Sensor data")
            .element(BusElementDefinition.scalar("temp", "double"))
            .element(BusElementDefinition.vector("readings", "double", 4))
            .build();

        String structure = sensorBus.toStructureString();
        assertNotNull("Structure string should not be null", structure);
        assertTrue("Structure should contain bus name", structure.contains("SensorBus"));
        assertTrue("Structure should contain description", structure.contains("Sensor data"));
        assertTrue("Structure should contain element names", structure.contains("temp"));
        assertTrue("Structure should contain element names", structure.contains("readings"));
    }

    @Test
    public void testEqualsAndHashCode() {
        BusDefinition bus1 = BusDefinition.builder()
            .name("Bus")
            .scope("Global")
            .isVirtual(true)
            .element(BusElementDefinition.scalar("temp", "double"))
            .build();

        BusDefinition bus2 = BusDefinition.builder()
            .name("Bus")
            .scope("Global")
            .isVirtual(true)
            .element(BusElementDefinition.scalar("temp", "double"))
            .build();

        BusDefinition bus3 = BusDefinition.builder()
            .name("DifferentBus")
            .scope("Global")
            .isVirtual(true)
            .element(BusElementDefinition.scalar("temp", "double"))
            .build();

        assertEquals("Equal buses should have same hash code", bus1.hashCode(), bus2.hashCode());
        assertEquals("Buses with same structure should be equal", bus1, bus2);
        assertNotEquals("Buses with different names should not be equal", bus1, bus3);
    }

    // === Complex Integration Tests ===

    @Test
    public void testComplexVehicleBusExample() {
        // Create sensor bus definition
        BusDefinition sensorBus = BusDefinition.builder()
            .name("SensorBus")
            .description("Temperature and pressure sensors")
            .isVirtual(true)
            .element(BusElementDefinition.scalar("temperature", "double"))
            .element(BusElementDefinition.scalar("pressure", "double"))
            .element(BusElementDefinition.scalar("humidity", "double"))
            .build();

        // Create motor control bus definition
        BusDefinition motorBus = BusDefinition.builder()
            .name("MotorBus")
            .description("Motor control signals")
            .isVirtual(false)
            .element(BusElementDefinition.scalar("pwm", "double"))
            .element(BusElementDefinition.scalar("current", "double"))
            .element(BusElementDefinition.scalar("voltage", "double"))
            .build();

        // Create vehicle bus with nested buses
        BusDefinition vehicleBus = BusDefinition.builder()
            .name("VehicleBus")
            .description("Complete vehicle data")
            .isVirtual(true)
            .element(BusElementDefinition.scalar("speed", "double"))
            .element(BusElementDefinition.vector("position", "double", 3))
            .element(BusElementDefinition.matrix("rotation", "double", 3, 3))
            .element(BusElementDefinition.nestedBus("sensors", sensorBus))
            .element(BusElementDefinition.nestedBus("motor", motorBus))
            .build();

        // Validate all definitions
        sensorBus.validate();
        motorBus.validate();
        vehicleBus.validate();

        // Verify structure
        assertEquals("Sensor bus should have 3 elements", 3, sensorBus.getElementCount());
        assertEquals("Motor bus should have 3 elements", 3, motorBus.getElementCount());
        assertEquals("Vehicle bus should have 5 elements", 5, vehicleBus.getElementCount());

        // Verify total widths
        assertEquals("Sensor bus total width should be 3", 3, sensorBus.getTotalWidth());
        assertEquals("Motor bus total width should be 3", 3, motorBus.getTotalWidth());
        assertEquals("Vehicle bus total width should be 1 + 3 + 9 + 3 + 3 = 19", 19, vehicleBus.getTotalWidth());

        // Verify virtual flags
        assertTrue("Sensor bus should be virtual", sensorBus.isVirtual());
        assertFalse("Motor bus should be non-virtual", motorBus.isVirtual());
        assertTrue("Vehicle bus should be virtual", vehicleBus.isVirtual());
    }
}
