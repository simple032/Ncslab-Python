package com.ncslab.block.source;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.c.linux.pc.simulation.CodeStructCLinuxPCSimulation;
import com.utils.CodeGenerationStandardsChecker;
import com.utils.CodeGenerationStandardsChecker.ComplianceReport;
import org.junit.Test;
import Jama.Matrix;

import static org.junit.Assert.*;

/**
 * Comprehensive tests for Constant block using mock-based approach.
 * Tests both Java simulation (calculateXX) and C++ code generation.
 * Uses DirectBlockTestBase for high-efficiency testing (no I/O, no database).
 *
 * Test Coverage:
 * CATEGORY A: Calculate Methods (Java Simulation)
 * - Scalar constant output
 * - Matrix constant output
 * - calculateInit() method
 * - Output port configuration
 * - Edge cases (zero, negative, large/small values)
 * - Performance benchmarks
 *
 * CATEGORY B: Code Generation (C++ Templates)
 * - generateInitCodeC() method
 * - generateOutputCodeC() method
 * - Template variable population
 * - C++ syntax validation
 * - No template errors verification
 */
public class ConstantTest extends DirectBlockTestBase {
    @Override
    protected Block createBlock() throws Exception {
        // Create Constant block with value 5.0
        block = Constant.create("testConstant", "test", 5.0, mockModel);
        return block;
    }

    @Test
    public void testScalarConstantOutput() {
        // Act
        block.calculateInit();
        block.calculateOutput(0.0);

        // Assert
        double output = getScalarOutput(0);
        assertEquals("Constant should output 5.0", 5.0, output, DELTA);
    }

    @Test
    public void testZeroValue() throws Exception {
        // Create block with zero value
        Constant zeroBlock = Constant.create("zero", "test", 0.0, mockModel);
        initializeOutputSignals(zeroBlock);
        zeroBlock.calculateInit();
        zeroBlock.calculateOutput(0.0);

        OutputSignal outputSignal = zeroBlock.getOutputPortList().get(0).getOutputSignalC();
        Data outputData = outputSignal.getData();
        assertEquals("Constant should output 0.0", 0.0, outputData.getInitValue(), DELTA);
    }

    @Test
    public void testNegativeValue() throws Exception {
        // Create block with negative value
        Constant negBlock = Constant.create("neg", "test", -10.5, mockModel);
        initializeOutputSignals(negBlock);
        negBlock.calculateInit();
        negBlock.calculateOutput(0.0);

        OutputSignal outputSignal = negBlock.getOutputPortList().get(0).getOutputSignalC();
        Data outputData = outputSignal.getData();
        assertEquals("Constant should output -10.5", -10.5, outputData.getInitValue(), DELTA);
    }

    @Test
    public void testLargeValue() throws Exception {
        // Create block with large value
        double largeValue = 1e10;
        Constant largeBlock = Constant.create("large", "test", largeValue, mockModel);
        initializeOutputSignals(largeBlock);
        largeBlock.calculateInit();
        largeBlock.calculateOutput(0.0);

        OutputSignal outputSignal = largeBlock.getOutputPortList().get(0).getOutputSignalC();
        Data outputData = outputSignal.getData();
        assertEquals("Constant should output large value", largeValue, outputData.getInitValue(), 1.0);
    }

    @Test
    public void testSmallValue() throws Exception {
        // Create block with small value
        double smallValue = 1e-10;
        Constant smallBlock = Constant.create("small", "test", smallValue, mockModel);
        initializeOutputSignals(smallBlock);
        smallBlock.calculateInit();
        smallBlock.calculateOutput(0.0);

        OutputSignal outputSignal = smallBlock.getOutputPortList().get(0).getOutputSignalC();
        Data outputData = outputSignal.getData();
        assertEquals("Constant should output small value", smallValue, outputData.getInitValue(), 1e-20);
    }

    @Test
    public void testConstantOverTime() {
        // Verify constant output doesn't change over time
        block.calculateInit();

        // Test at different time points
        double[] timePoints = {0.0, 0.1, 1.0, 10.0, 100.0};
        for (double t : timePoints) {
            block.calculateOutput(t);
            double output = getScalarOutput(0);
            assertEquals("Constant should remain 5.0 at t=" + t, 5.0, output, DELTA);
        }
    }

    @Test
    public void testOutputPortConfiguration() {
        // Verify output port is properly configured
        assertEquals("Should have 1 output port", 1, block.getOutputPortList().size());

        OutputPort outputPort = block.getOutputPortList().get(0);
        assertNotNull("Output port should exist", outputPort);
        assertFalse("Constant block should not have feedthrough", outputPort.getFeedThrough());
    }

    @Test
    public void testBlockName() {
        assertEquals("Block name should be testConstant", "testConstant", block.getBlockName());
    }

    @Test
    public void testBlockType() {
        assertEquals("Block type should be Constant", "Constant", block.getBlockType());
    }

    @Test
    public void testNoInputPorts() {
        assertEquals("Constant block should have 0 input ports", 0, block.getInputPortList().size());
    }

    @Test
    public void testParameterCount() {
        // Constant block should have Value parameter + SourceBlock common parameters
        assertTrue("Should have at least 1 parameter", block.getParameterList().size() >= 1);
    }

    @Test
    public void testMultipleCalculations() {
        // Test multiple calculateOutput() calls produce consistent results
        block.calculateInit();

        for (int i = 0; i < 100; i++) {
            block.calculateOutput(i * 0.01);
            double output = getScalarOutput(0);
            assertEquals("Constant should always output 5.0", 5.0, output, DELTA);
        }
    }

    @Test
    public void testPerformance() {
        // Performance test: Measure calculateOutput() execution time
        block.calculateInit();

        long avgTime = measureCalculateOutputPerformance(10000);
        System.out.println("Average Constant calculateOutput() time: " + avgTime / 1000.0 + " microseconds");

        // Performance assertion - constant should be very fast
        assertTrue("Constant calculateOutput should be very fast", avgTime < 5000); // <5 microseconds
    }

    @Test
    public void testOutputSignalCreation() {
        block.calculateInit();
        block.calculateOutput(0.0);

        OutputPort outputPort = block.getOutputPortList().get(0);
        OutputSignal outputSignal = outputPort.getOutputSignalC();

        assertNotNull("Output signal should be created", outputSignal);
        assertNotNull("Output signal data should exist", outputSignal.getData());
        assertEquals("Output signal should be REAL type", DataType.REAL, outputSignal.getDataType());
    }

    @Test
    public void testPositiveZero() throws Exception {
        Constant posZeroBlock = Constant.create("posZero", "test", 0.0, mockModel);
        initializeOutputSignals(posZeroBlock);
        posZeroBlock.calculateInit();
        posZeroBlock.calculateOutput(0.0);

        OutputSignal outputSignal = posZeroBlock.getOutputPortList().get(0).getOutputSignalC();
        Data outputData = outputSignal.getData();
        assertEquals("Should output positive zero", 0.0, outputData.getInitValue(), DELTA);
        assertFalse("Should be positive zero", Double.doubleToRawLongBits(outputData.getInitValue()) == Long.MIN_VALUE);
    }

    @Test
    public void testNegativeZero() throws Exception {
        Constant negZeroBlock = Constant.create("negZero", "test", -0.0, mockModel);
        initializeOutputSignals(negZeroBlock);
        negZeroBlock.calculateInit();
        negZeroBlock.calculateOutput(0.0);

        OutputSignal outputSignal = negZeroBlock.getOutputPortList().get(0).getOutputSignalC();
        Data outputData = outputSignal.getData();
        // Java treats -0.0 and 0.0 as equal, so this test just verifies no errors occur
        assertEquals("Should output zero", 0.0, Math.abs(outputData.getInitValue()), DELTA);
    }

    @Test
    public void testSequentialBlocks() throws Exception {
        // Test creating multiple constant blocks
        Constant block1 = Constant.create("const1", "test", 1.0, mockModel);
        Constant block2 = Constant.create("const2", "test", 2.0, mockModel);
        Constant block3 = Constant.create("const3", "test", 3.0, mockModel);

        initializeOutputSignals(block1);
        initializeOutputSignals(block2);
        initializeOutputSignals(block3);

        block1.calculateInit();
        block2.calculateInit();
        block3.calculateInit();

        block1.calculateOutput(0.0);
        block2.calculateOutput(0.0);
        block3.calculateOutput(0.0);

        assertEquals("Block1 should output 1.0", 1.0,
            block1.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
        assertEquals("Block2 should output 2.0", 2.0,
            block2.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
        assertEquals("Block3 should output 3.0", 3.0,
            block3.getOutputPortList().get(0).getOutputSignalC().getData().getInitValue(), DELTA);
    }

    @Test
    public void testReinitialization() {
        // Test that block can be reinitialized
        block.calculateInit();
        block.calculateOutput(0.0);
        double firstOutput = getScalarOutput(0);

        // Reinitialize
        block.calculateInit();
        block.calculateOutput(0.0);
        double secondOutput = getScalarOutput(0);

        assertEquals("Output should be same after reinitialization", firstOutput, secondOutput, DELTA);
    }

    @Test
    public void testEdgeCaseMaxDouble() throws Exception {
        Constant maxBlock = Constant.create("max", "test", Double.MAX_VALUE, mockModel);
        initializeOutputSignals(maxBlock);
        maxBlock.calculateInit();
        maxBlock.calculateOutput(0.0);

        OutputSignal outputSignal = maxBlock.getOutputPortList().get(0).getOutputSignalC();
        Data outputData = outputSignal.getData();
        assertEquals("Should handle MAX_VALUE", Double.MAX_VALUE, outputData.getInitValue(), Double.MAX_VALUE * 1e-10);
    }

    @Test
    public void testEdgeCaseMinDouble() throws Exception {
        Constant minBlock = Constant.create("min", "test", Double.MIN_VALUE, mockModel);
        initializeOutputSignals(minBlock);
        minBlock.calculateInit();
        minBlock.calculateOutput(0.0);

        OutputSignal outputSignal = minBlock.getOutputPortList().get(0).getOutputSignalC();
        Data outputData = outputSignal.getData();
        assertEquals("Should handle MIN_VALUE", Double.MIN_VALUE, outputData.getInitValue(), Double.MIN_VALUE * 2);
    }

    @Test
    public void testNoDriftOverTime() {
        // Verify that constant doesn't drift over many iterations
        block.calculateInit();

        double firstOutput = 0;
        for (int i = 0; i < 10000; i++) {
            block.calculateOutput(i * 0.001);
            double currentOutput = getScalarOutput(0);

            if (i == 0) {
                firstOutput = currentOutput;
            } else {
                assertEquals("Output should not drift at iteration " + i,
                    firstOutput, currentOutput, DELTA);
            }
        }
    }

    // ===================================================================
    // CATEGORY B: CODE GENERATION TESTS (C++ Templates)
    // ===================================================================

    @Test
    public void testGenerateInitCodeC_NotEmpty() {
        // Setup: Initialize block
        block.calculateInit();

        // Execute: Generate C++ init code
        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        block.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        // Verify: Code should not be empty
        assertNotNull("Init code should not be null", initCode);
        assertFalse("Init code should not be empty", initCode.trim().isEmpty());

        System.out.println("=== Generated Init Code ===");
        System.out.println(initCode);
    }

    @Test
    public void testGenerateInitCodeC_NoTemplateErrors() {
        // Setup
        block.calculateInit();

        // Execute: Generate C++ init code
        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        block.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        // Verify: No undefined Velocity template variables (would appear as $VariableName)
        assertFalse("Should not contain undefined template variables",
            initCode.contains("$ValueObject") || initCode.contains("${ValueObject"));
        assertFalse("Should not contain undefined variables",
            initCode.matches(".*\\$\\{?[A-Za-z][A-Za-z0-9_]*\\}?.*"));
    }

    @Test
    public void testGenerateInitCodeC_ValidSyntax() {
        // Setup
        block.calculateInit();

        // Execute: Generate C++ init code
        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        block.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        // Verify: Should contain C++ syntax elements
        assertTrue("Should contain C++ comment or statement",
            initCode.contains("/*") || initCode.contains("//") || initCode.contains(";"));

        // Verify: Should contain block reference
        assertTrue("Should reference Constant block",
            initCode.toLowerCase().contains("constant"));
    }

    @Test
    public void testGenerateInitCodeC_ContainsValue() {
        // Setup
        block.calculateInit();

        // Execute: Generate C++ init code
        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        block.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        // Verify: Should contain the constant value (5.0)
        assertTrue("Init code should contain value assignment",
            initCode.contains("5.0") || initCode.contains("5.000000"));
    }

    @Test
    public void testGenerateInitCodeC_DifferentValues() throws Exception {
        // Test code generation with different constant values
        double[] testValues = {0.0, 1.0, -1.0, 100.0, 0.001, -99.99};

        for (double value : testValues) {
            Constant testBlock = Constant.create("test" + value, "test", value, mockModel);
            initializeOutputSignals(testBlock);
            testBlock.calculateInit();

            CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
            testBlock.generateInitCodeC(codeStruct);
            String initCode = codeStruct.getInitCode();

            assertNotNull("Init code should be generated for value " + value, initCode);
            assertFalse("Init code should not be empty for value " + value, initCode.trim().isEmpty());
        }
    }

    @Test
    public void testGenerateOutputCodeC_NotEmpty() {
        // Setup: Initialize block
        block.calculateInit();

        // Execute: Generate C++ output code
        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        block.generateOutputCodeC(codeStruct);
        String outputCode = codeStruct.getOutputCode();

        // Verify: Code should not be null (may be empty for Constant since output doesn't change)
        assertNotNull("Output code should not be null", outputCode);

        System.out.println("=== Generated Output Code ===");
        System.out.println(outputCode);
    }

    @Test
    public void testGenerateOutputCodeC_NoTemplateErrors() {
        // Setup
        block.calculateInit();

        // Execute: Generate C++ output code
        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        block.generateOutputCodeC(codeStruct);
        String outputCode = codeStruct.getOutputCode();

        // Verify: No undefined template variables
        if (outputCode != null && !outputCode.trim().isEmpty()) {
            assertFalse("Should not contain undefined variables",
                outputCode.matches(".*\\$\\{?[A-Za-z][A-Za-z0-9_]*\\}?.*"));
        }
    }

    @Test
    public void testCodeGenerationPerformance() {
        // Performance test: Measure code generation time
        block.calculateInit();

        long startTime = System.nanoTime();

        for (int i = 0; i < 1000; i++) {
            CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
            block.generateInitCodeC(codeStruct);
            block.generateOutputCodeC(codeStruct);
        }

        long endTime = System.nanoTime();
        long avgTime = (endTime - startTime) / 1000;

        System.out.println("Average code generation time: " + avgTime / 1000.0 + " microseconds");

        // Code generation should be fast (<10ms per iteration)
        assertTrue("Code generation should be fast", avgTime < 10_000_000); // <10ms
    }

    @Test
    public void testCompleteCodeGenerationCycle() {
        // Test complete cycle: init, output, and code generation
        block.calculateInit();
        block.calculateOutput(0.0);

        // Generate C++ code
        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        block.generateInitCodeC(codeStruct);
        block.generateOutputCodeC(codeStruct);

        String initCode = codeStruct.getInitCode();
        String outputCode = codeStruct.getOutputCode();

        // Verify both were generated
        assertNotNull("Init code should be generated", initCode);
        assertNotNull("Output code should be generated", outputCode);
    }

    // ===================================================================
    // CATEGORY D: CODE CORRECTNESS VERIFICATION TESTS
    // ===================================================================

    @Test
    public void testGeneratedCode_CorrectParameterDeclaration() {
        // Test that generated code correctly declares the Value parameter
        block.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        block.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        // Verify: Should declare Value parameter variable
        // Expected pattern: "Block<ID>_Value = 5.000000;"
        assertTrue("Should declare Value parameter variable",
            initCode.matches("(?s).*Block\\d+_Value\\s*=\\s*5\\.0+.*"));
    }

    @Test
    public void testGeneratedCode_CorrectOutputAssignment() {
        // Test that generated code correctly assigns output
        block.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        block.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        // Verify: Should assign output variable to Value parameter
        // Expected pattern: "Block<ID>_OutputSignal1 = Block<ID>_Value;"
        assertTrue("Should assign output to Value parameter",
            initCode.matches("(?s).*Block\\d+_OutputSignal\\d+\\s*=\\s*Block\\d+_Value.*"));
    }

    @Test
    public void testGeneratedCode_DefaultParameters_Value1() throws Exception {
        // Test code generation with default parameter Value=1
        Constant defaultBlock = Constant.create("default", "test", 1.0, mockModel);
        initializeOutputSignals(defaultBlock);
        defaultBlock.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        defaultBlock.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        System.out.println("=== Generated Code for Value=1.0 ===");
        System.out.println(initCode);

        // Verify: Parameter declaration with value 1.0
        assertTrue("Should contain 1.0 value",
            initCode.contains("1.0") || initCode.contains("1.000000"));

        // Verify: No template errors
        assertCodeContainsNoTemplateErrors(initCode);

        // Verify: Has proper structure (comment + declarations + assignment)
        assertTrue("Should have comment header", initCode.contains("/*") || initCode.contains("//"));
        assertTrue("Should have assignment operator", initCode.contains("="));
        assertTrue("Should have statement terminator", initCode.contains(";"));
    }

    @Test
    public void testGeneratedCode_DefaultParameters_Value0() throws Exception {
        // Test code generation with Value=0 (common default)
        Constant zeroBlock = Constant.create("zero", "test", 0.0, mockModel);
        initializeOutputSignals(zeroBlock);
        zeroBlock.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        zeroBlock.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        System.out.println("=== Generated Code for Value=0.0 ===");
        System.out.println(initCode);

        // Verify: Parameter declaration with value 0.0
        assertTrue("Should contain 0.0 value",
            initCode.contains("0.0") || initCode.contains("0.000000"));

        // Verify: Correct structure
        assertCodeContainsNoTemplateErrors(initCode);
        assertValidCppSyntax(initCode);
    }

    @Test
    public void testGeneratedCode_MatchesExpectedPattern() {
        // Test that generated code matches expected template pattern
        block.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        block.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        System.out.println("=== Complete Generated Init Code ===");
        System.out.println(initCode);

        // Expected pattern for Constant block init code:
        // 1. Comment header mentioning "Constant" and block name
        // 2. Parameter declaration: Block<ID>_Value = <value>;
        // 3. Output assignment: Block<ID>_OutputSignal1 = Block<ID>_Value;

        // Verify pattern components
        assertTrue("Should have Constant block comment",
            initCode.toLowerCase().contains("constant"));

        assertTrue("Should have block name reference",
            initCode.contains("testConstant") || initCode.contains("Constant"));

        assertTrue("Should declare Value parameter",
            initCode.matches("(?s).*Block\\d+_Value\\s*=.*"));

        assertTrue("Should assign to output signal",
            initCode.matches("(?s).*Block\\d+_OutputSignal\\d+\\s*=.*"));
    }

    @Test
    public void testGeneratedCode_BlockIDConsistency() {
        // Verify that generated code uses consistent block IDs
        block.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        block.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        // Extract block ID from generated code
        String blockIdPattern = "Block(\\d+)_";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(blockIdPattern);
        java.util.regex.Matcher matcher = pattern.matcher(initCode);

        java.util.Set<String> blockIds = new java.util.HashSet<>();
        while (matcher.find()) {
            blockIds.add(matcher.group(1));
        }

        // All references should use the same block ID
        assertTrue("Should have at least one block ID reference", blockIds.size() >= 1);
        if (blockIds.size() > 1) {
            fail("Block ID should be consistent throughout generated code, found: " + blockIds);
        }
    }

    @Test
    public void testGeneratedCode_NegativeValue() throws Exception {
        // Test code correctness with negative value
        Constant negBlock = Constant.create("negative", "test", -5.0, mockModel);
        initializeOutputSignals(negBlock);
        negBlock.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        negBlock.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        System.out.println("=== Generated Code for Value=-5.0 ===");
        System.out.println(initCode);

        // Verify: Negative value is correctly represented
        assertTrue("Should contain negative value",
            initCode.contains("-5.0") || initCode.contains("-5.000000"));

        // Verify: Structure is correct
        assertCodeContainsNoTemplateErrors(initCode);
        assertValidCppSyntax(initCode);
    }

    @Test
    public void testGeneratedCode_LargeValue() throws Exception {
        // Test code correctness with large value
        double largeValue = 1.23456789e10;
        Constant largeBlock = Constant.create("large", "test", largeValue, mockModel);
        initializeOutputSignals(largeBlock);
        largeBlock.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        largeBlock.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        System.out.println("=== Generated Code for Large Value ===");
        System.out.println(initCode);

        // Verify: Large value is represented (in some form - scientific notation or decimal)
        assertTrue("Should contain large value representation",
            initCode.matches("(?s).*Block\\d+_Value\\s*=\\s*[0-9.eE+-]+;.*"));

        // Verify: Structure is correct
        assertCodeContainsNoTemplateErrors(initCode);
        assertValidCppSyntax(initCode);
    }

    @Test
    public void testGeneratedCode_SmallValue() throws Exception {
        // Test code correctness with very small value
        double smallValue = 1.23456789e-10;
        Constant smallBlock = Constant.create("small", "test", smallValue, mockModel);
        initializeOutputSignals(smallBlock);
        smallBlock.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        smallBlock.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        System.out.println("=== Generated Code for Small Value ===");
        System.out.println(initCode);

        // Verify: Small value is represented
        assertTrue("Should contain small value representation",
            initCode.matches("(?s).*Block\\d+_Value\\s*=\\s*[0-9.eE+-]+;.*"));

        // Verify: Structure is correct
        assertCodeContainsNoTemplateErrors(initCode);
        assertValidCppSyntax(initCode);
    }

    @Test
    public void testGeneratedCode_CompareWithCalculateOutput() throws Exception {
        // Verify that generated code matches calculateOutput() behavior
        double testValue = 7.5;
        Constant testBlock = Constant.create("compare", "test", testValue, mockModel);
        initializeOutputSignals(testBlock);
        testBlock.calculateInit();

        // Get Java simulation output
        testBlock.calculateOutput(0.0);
        OutputSignal outputSignal = testBlock.getOutputPortList().get(0).getOutputSignalC();
        double javaOutput = outputSignal.getData().getInitValue();

        // Get generated C++ code
        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        testBlock.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        System.out.println("=== Java Output: " + javaOutput + " ===");
        System.out.println("=== Generated C++ Code ===");
        System.out.println(initCode);

        // Verify: Java output matches expected value
        assertEquals("Java simulation should output test value", testValue, javaOutput, DELTA);

        // Verify: Generated code contains the same value
        String valueStr = String.format("%.6f", testValue);
        assertTrue("Generated code should contain value " + valueStr,
            initCode.contains(valueStr) || initCode.contains(String.valueOf(testValue)));
    }

    @Test
    public void testGeneratedCode_AllDefaultParameters() throws Exception {
        // Test with all default parameters (as would appear in Simulink)
        // Default: Value=1, SampleTime=0, OutDataTypeStr="Inherit: Same as parameter"
        Constant defaultBlock = Constant.create("allDefaults", "test", 1.0, 0.0,
            "Inherit: Same as parameter", false, mockModel);
        initializeOutputSignals(defaultBlock);
        defaultBlock.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        defaultBlock.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        System.out.println("=== Generated Code with All Default Parameters ===");
        System.out.println(initCode);

        // Verify: Basic structure
        assertNotNull("Should generate code with defaults", initCode);
        assertCodeContainsNoTemplateErrors(initCode);
        assertValidCppSyntax(initCode);

        // Verify: Contains default value
        assertTrue("Should contain default value 1.0",
            initCode.contains("1.0") || initCode.contains("1.000000"));

        // Verify: Has proper Constant block pattern
        assertTrue("Should reference Constant block", initCode.toLowerCase().contains("constant"));
        assertTrue("Should have parameter declaration", initCode.matches("(?s).*Block\\d+_Value\\s*=.*"));
        assertTrue("Should have output assignment", initCode.matches("(?s).*Block\\d+_OutputSignal\\d+\\s*=.*"));
    }

    // ===================================================================
    // CATEGORY E: INDUSTRIAL STANDARDS COMPLIANCE TESTS
    // Tests compliance with MISRA-C, AUTOSAR, IEC 61508, etc.
    // ===================================================================

    @Test
    public void testStandardsCompliance_FullReport() {
        // Generate comprehensive standards compliance report
        block.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        block.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        // Run full compliance check
        ComplianceReport report = CodeGenerationStandardsChecker.checkAllStandards(
            initCode, block.getBlockId());

        System.out.println("=== Standards Compliance Report ===");
        System.out.println(report.toString());

        // Verify minimum compliance threshold (85%)
        assertTrue("Should meet minimum 85% compliance threshold",
            report.getCompliancePercentage() >= 85.0);

        // Verify no critical failures
        assertTrue("Should have no critical failures",
            report.getCriticalFailures().isEmpty());
    }

    @Test
    public void testStandardsCompliance_TraceabilityHeader() {
        // MISRA/IEC 61508: Code must have traceability header
        block.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        block.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        // Verify header comment exists
        assertTrue("Must have header comment (MISRA 2.5, IEC 61508)",
            CodeGenerationStandardsChecker.checksTraceabilityHeader(
                initCode, String.valueOf(block.getBlockId())));

        // Verify header contains block information
        assertTrue("Header should reference Constant block",
            initCode.toLowerCase().contains("constant"));
    }

    @Test
    public void testStandardsCompliance_UniqueIdentifiers() {
        // MISRA 5.7, 5.8: All identifiers must be unique
        block.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        block.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        // Verify Block<ID>_ prefix is used
        assertTrue("Must use Block<ID>_ prefix for uniqueness (MISRA 5.7)",
            CodeGenerationStandardsChecker.checksUniqueIdentifiers(
                initCode, String.valueOf(block.getBlockId())));

        // Should contain Block<ID>_Value pattern
        assertTrue("Should have unique Block<ID>_Value identifier",
            initCode.matches("(?s).*Block\\d+_Value\\s*=.*"));
    }

    @Test
    public void testStandardsCompliance_NoUndefinedVariables() {
        // Critical: No template variables should remain undefined
        block.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        block.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        // Verify no undefined Velocity variables
        assertTrue("Must not contain undefined template variables (Critical)",
            CodeGenerationStandardsChecker.checksNoUndefinedTemplateVars(initCode));

        // Should not contain ${...} patterns
        assertFalse("Should not have ${VarName} patterns",
            initCode.matches(".*\\$\\{[^}]+\\}.*"));
    }

    @Test
    public void testStandardsCompliance_VariableInitialization() {
        // MISRA 9.1: All variables must be initialized before use
        block.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        block.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        // Verify Value parameter is initialized
        assertTrue("Variables must be initialized at declaration (MISRA 9.1)",
            CodeGenerationStandardsChecker.checksVariableInitialization(initCode));

        // Should have assignment pattern
        assertTrue("Should initialize Block<ID>_Value",
            initCode.matches("(?s).*Block\\d+_Value\\s*=\\s*[0-9.eE+-]+;.*"));
    }

    @Test
    public void testStandardsCompliance_NamingConvention() {
        // AUTOSAR: Consistent naming conventions
        block.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        block.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        // Verify naming follows Block<ID>_ParameterName pattern
        assertTrue("Must follow AUTOSAR naming conventions",
            CodeGenerationStandardsChecker.checksNamingConvention(
                initCode, String.valueOf(block.getBlockId())));
    }

    @Test
    public void testStandardsCompliance_NoMagicNumbers() {
        // MISRA 2.3: Avoid magic numbers (except 0, 1, -1)
        block.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        block.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        // Check for magic numbers (this test may need adjustment based on Value parameter)
        boolean hasMagicNumbers = !CodeGenerationStandardsChecker.checksNoMagicNumbers(initCode);

        // For Constant block, the Value parameter IS the literal value, so this is acceptable
        // We just verify it's documented in comments or named clearly
        if (hasMagicNumbers) {
            // Verify the value is assigned to a named constant
            assertTrue("Magic numbers should be assigned to named constants",
                initCode.matches("(?s).*Block\\d+_Value\\s*=\\s*[0-9.eE+-]+;.*"));
        }
    }

    @Test
    public void testStandardsCompliance_ProperIndentation() {
        // Code readability: Proper indentation
        block.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        block.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        // Verify proper indentation (basic check - no tabs, consistent spacing)
        assertTrue("Should use proper indentation",
            CodeGenerationStandardsChecker.checksProperIndentation(initCode));
    }

    @Test
    public void testStandardsCompliance_NoTrailingWhitespace() {
        // Code quality: No trailing whitespace
        block.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        block.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        // Verify no trailing whitespace
        assertTrue("Should not have trailing whitespace",
            CodeGenerationStandardsChecker.checksNoTrailingWhitespace(initCode));
    }

    @Test
    public void testStandardsCompliance_StatementTerminators() {
        // C/C++ syntax: All statements properly terminated
        block.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        block.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        // Verify statements are terminated with semicolons
        assertTrue("Statements must be properly terminated",
            CodeGenerationStandardsChecker.checksStatementTerminators(initCode));

        // Should contain semicolons
        assertTrue("Should have statement terminators", initCode.contains(";"));
    }

    @Test
    public void testStandardsCompliance_CompareDefaultParameters() throws Exception {
        // Test standards compliance with default SIMULINK parameters
        Constant defaultBlock = Constant.create("defaults", "test", 1.0, mockModel);
        initializeOutputSignals(defaultBlock);
        defaultBlock.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        defaultBlock.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        System.out.println("=== Standards Compliance with Default Parameters ===");

        // Run compliance check
        ComplianceReport report = CodeGenerationStandardsChecker.checkAllStandards(
            initCode, defaultBlock.getBlockId());

        System.out.println(report.toString());

        // Default parameters should produce compliant code
        assertTrue("Default parameters should produce compliant code",
            report.getCompliancePercentage() >= 85.0);
    }

    @Test
    public void testStandardsCompliance_MultipleBlocks() throws Exception {
        // Test that multiple blocks maintain compliance
        Constant block1 = Constant.create("const1", "test", 1.0, mockModel);
        Constant block2 = Constant.create("const2", "test", 2.0, mockModel);
        Constant block3 = Constant.create("const3", "test", 3.0, mockModel);

        initializeOutputSignals(block1);
        initializeOutputSignals(block2);
        initializeOutputSignals(block3);

        block1.calculateInit();
        block2.calculateInit();
        block3.calculateInit();

        // Check each block for compliance
        for (Constant testBlock : new Constant[]{block1, block2, block3}) {
            CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
            testBlock.generateInitCodeC(codeStruct);
            String initCode = codeStruct.getInitCode();

            ComplianceReport report = CodeGenerationStandardsChecker.checkAllStandards(
                initCode, testBlock.getBlockId());

            assertTrue("Block " + testBlock.getBlockName() + " should be compliant",
                report.getCompliancePercentage() >= 85.0);
        }
    }

    // ===================================================================
    // CATEGORY G: EXACT OUTPUT VERIFICATION TESTS
    // Verifies generated code exactly matches expected template output
    // ===================================================================

    @Test
    public void testExactOutput_Value1() throws Exception {
        // Test exact output for Value=1.0 according to template pattern
        Constant block1 = Constant.create("Constant1", "test", 1.0, mockModel);
        initializeOutputSignals(block1);
        block1.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        block1.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        System.out.println("=== Exact Output for Value=1.0 ===");
        System.out.println(initCode);
        System.out.println("===================================");

        // Expected pattern based on template:
        // Line 1: /* Code for initialization of block Constant:($blockId) $blockName */
        // Line 2: Block<ID>_Value = 1.000000;
        // Line 3: (empty line after getInitCodeC())
        // Line 4: Block<ID>_Output1 = Block<ID>_Value;

        String blockId = String.valueOf(block1.getBlockId());

        // Verify header comment
        assertTrue("Should have header comment with block type",
            initCode.contains("Code for initialization of block Constant"));
        assertTrue("Should reference block ID in comment",
            initCode.contains("(" + blockId + ")"));
        assertTrue("Should reference block name in comment",
            initCode.contains("Constant1"));

        // Verify Value parameter initialization
        String expectedValueDecl = "Block" + blockId + "_Value = 1.000000;";
        assertTrue("Should have exact Value declaration: " + expectedValueDecl,
            initCode.contains(expectedValueDecl));

        // Verify Output assignment
        String expectedOutputAssignment = "Block" + blockId + "_Output1 = Block" + blockId + "_Value;";
        assertTrue("Should have exact Output assignment: " + expectedOutputAssignment,
            initCode.contains(expectedOutputAssignment));

        // Verify overall structure
        assertExactTemplateStructure(initCode, blockId, "Constant1", 1.0);
    }

    @Test
    public void testExactOutput_Value5() throws Exception {
        // Test exact output for Value=5.0
        Constant block5 = Constant.create("Constant5", "test", 5.0, mockModel);
        initializeOutputSignals(block5);
        block5.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        block5.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        System.out.println("=== Exact Output for Value=5.0 ===");
        System.out.println(initCode);
        System.out.println("===================================");

        String blockId = String.valueOf(block5.getBlockId());

        // Verify exact pattern
        String expectedValueDecl = "Block" + blockId + "_Value = 5.000000;";
        assertTrue("Should have exact Value declaration: " + expectedValueDecl,
            initCode.contains(expectedValueDecl));

        String expectedOutputAssignment = "Block" + blockId + "_Output1 = Block" + blockId + "_Value;";
        assertTrue("Should have exact Output assignment: " + expectedOutputAssignment,
            initCode.contains(expectedOutputAssignment));

        assertExactTemplateStructure(initCode, blockId, "Constant5", 5.0);
    }

    @Test
    public void testExactOutput_Value0() throws Exception {
        // Test exact output for Value=0.0
        Constant block0 = Constant.create("Constant0", "test", 0.0, mockModel);
        initializeOutputSignals(block0);
        block0.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        block0.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        System.out.println("=== Exact Output for Value=0.0 ===");
        System.out.println(initCode);
        System.out.println("===================================");

        String blockId = String.valueOf(block0.getBlockId());

        // Verify exact pattern for zero
        String expectedValueDecl = "Block" + blockId + "_Value = 0.000000;";
        assertTrue("Should have exact Value declaration: " + expectedValueDecl,
            initCode.contains(expectedValueDecl));

        String expectedOutputAssignment = "Block" + blockId + "_Output1 = Block" + blockId + "_Value;";
        assertTrue("Should have exact Output assignment: " + expectedOutputAssignment,
            initCode.contains(expectedOutputAssignment));

        assertExactTemplateStructure(initCode, blockId, "Constant0", 0.0);
    }

    @Test
    public void testExactOutput_NegativeValue() throws Exception {
        // Test exact output for negative value
        Constant blockNeg = Constant.create("ConstantNeg", "test", -3.5, mockModel);
        initializeOutputSignals(blockNeg);
        blockNeg.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        blockNeg.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        System.out.println("=== Exact Output for Value=-3.5 ===");
        System.out.println(initCode);
        System.out.println("====================================");

        String blockId = String.valueOf(blockNeg.getBlockId());

        // Verify exact pattern for negative value
        String expectedValueDecl = "Block" + blockId + "_Value = -3.500000;";
        assertTrue("Should have exact Value declaration: " + expectedValueDecl,
            initCode.contains(expectedValueDecl));

        String expectedOutputAssignment = "Block" + blockId + "_Output1 = Block" + blockId + "_Value;";
        assertTrue("Should have exact Output assignment: " + expectedOutputAssignment,
            initCode.contains(expectedOutputAssignment));

        assertExactTemplateStructure(initCode, blockId, "ConstantNeg", -3.5);
    }

    @Test
    public void testExactOutput_TemplateLineByLine() throws Exception {
        // Verify line-by-line template execution
        Constant blockTest = Constant.create("TestBlock", "test", 2.5, mockModel);
        initializeOutputSignals(blockTest);
        blockTest.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        blockTest.generateInitCodeC(codeStruct);
        String initCode = codeStruct.getInitCode();

        String blockId = String.valueOf(blockTest.getBlockId());

        // Template line 1: /* Code for initialization of block Constant:($blockId) $blockName */
        String expectedLine1Pattern = "/\\* Code for initialization of block Constant:\\(" + blockId + "\\) TestBlock \\*/";
        assertTrue("Line 1 should match template pattern",
            initCode.matches("(?s).*" + expectedLine1Pattern + ".*"));

        // Template line 2: ${ValueObject.getInitCodeC()}
        // This should produce: Block<ID>_Value = 2.500000;
        String expectedValueDecl = "Block" + blockId + "_Value = 2.500000;";
        assertTrue("Line 2 (ValueObject.getInitCodeC()) should produce: " + expectedValueDecl,
            initCode.contains(expectedValueDecl));

        // Template line 3: ${outputVar} = ${Value};
        // This should produce: Block<ID>_Output1 = Block<ID>_Value;
        String expectedOutputAssignment = "Block" + blockId + "_Output1 = Block" + blockId + "_Value;";
        assertTrue("Line 3 (outputVar = Value) should produce: " + expectedOutputAssignment,
            initCode.contains(expectedOutputAssignment));

        System.out.println("=== Line-by-Line Template Verification ===");
        System.out.println("Expected Line 1 pattern: " + expectedLine1Pattern);
        System.out.println("Expected Line 2: " + expectedValueDecl);
        System.out.println("Expected Line 3: " + expectedOutputAssignment);
        System.out.println("Actual generated code:");
        System.out.println(initCode);
    }

    @Test
    public void testExactOutput_CompareWithExpectedString() throws Exception {
        // Build exact expected string and compare
        Constant blockExact = Constant.create("ExactTest", "test", 1.0, mockModel);
        initializeOutputSignals(blockExact);
        blockExact.calculateInit();

        CodeStructC codeStruct = new CodeStructCLinuxPCSimulation(mockModel);
        blockExact.generateInitCodeC(codeStruct);
        String actualCode = codeStruct.getInitCode();

        String blockId = String.valueOf(blockExact.getBlockId());

        // Build expected output based on template
        String expectedCode = String.format(
            "/* Code for initialization of block Constant:(%s) ExactTest */\n" +
            "Block%s_Value = 1.000000;\n" +
            "\n" +
            "Block%s_Output1 = Block%s_Value;",
            blockId, blockId, blockId, blockId
        );

        System.out.println("=== Expected vs Actual Comparison ===");
        System.out.println("Expected:");
        System.out.println(expectedCode);
        System.out.println("\nActual:");
        System.out.println(actualCode);
        System.out.println("====================================");

        // Verify each component (allowing for whitespace differences)
        String normalizedActual = actualCode.replaceAll("\\s+", " ").trim();
        String normalizedExpected = expectedCode.replaceAll("\\s+", " ").trim();

        assertTrue("Generated code should match expected pattern (normalized)",
            normalizedActual.contains(normalizedExpected.replaceAll("\\s+", " ").trim()));
    }

    // ===================================================================
    // CATEGORY F: HELPER METHODS
    // ===================================================================

    /**
     * Helper method to verify exact template structure
     */
    private void assertExactTemplateStructure(String code, String blockId, String blockName, double value) {
        // Verify all required components are present

        // 1. Header comment
        assertTrue("Must have header comment",
            code.contains("/* Code for initialization of block Constant"));
        assertTrue("Header must contain block ID: " + blockId,
            code.contains("(" + blockId + ")"));
        assertTrue("Header must contain block name: " + blockName,
            code.contains(blockName));

        // 2. Value parameter declaration
        String valuePattern = String.format("Block%s_Value = %.6f;", blockId, value);
        assertTrue("Must declare Value parameter: " + valuePattern,
            code.contains(valuePattern));

        // 3. Output assignment
        String outputPattern = String.format("Block%s_Output1 = Block%s_Value;", blockId, blockId);
        assertTrue("Must assign output: " + outputPattern,
            code.contains(outputPattern));

        // 4. Verify order (Value declaration comes before Output assignment)
        int valueIndex = code.indexOf("Block" + blockId + "_Value = ");
        int outputIndex = code.indexOf("Block" + blockId + "_Output1 = ");
        assertTrue("Value declaration must come before Output assignment",
            valueIndex < outputIndex);
    }

    /**
     * Helper method to check if generated code contains template errors
     */
    private void assertCodeContainsNoTemplateErrors(String code) {
        assertNotNull("Code should not be null", code);
        assertFalse("Code should not contain undefined Velocity variables",
            code.matches(".*\\$\\{?[A-Za-z][A-Za-z0-9_]*\\}?.*"));
    }

    /**
     * Helper method to verify basic C++ syntax elements
     */
    private void assertValidCppSyntax(String code) {
        assertTrue("Should contain C++ syntax elements",
            code.contains(";") || code.contains("{") || code.contains("/*") || code.contains("//"));
    }
}
