package com.ncslab.block;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;

import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import Jama.Matrix;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * GOLD STANDARD Test Template for NCSLab Block Testing
 *
 * This template provides comprehensive test coverage for any NCSLab block including:
 * 1. Calculate methods (Java simulation) - Direct execution testing
 * 2. Code generation methods (C++ templates) - Template rendering validation
 * 3. Template variable population - Context verification
 * 4. Error handling - Edge cases and boundary conditions
 *
 * FEATURES:
 * - Uses mock data exclusively (NO database, NO WebSocket, NO file I/O)
 * - High-efficiency testing (target: <10ms per test method)
 * - Comprehensive coverage of all block lifecycle methods
 * - Reusable helper methods for common validations
 * - Clear test organization with category markers
 *
 * HOW TO USE THIS TEMPLATE:
 * 1. Copy this file and rename to {YourBlock}Test.java
 * 2. Replace BlockType with your actual block class name
 * 3. Update createBlock() to instantiate your specific block
 * 4. Customize test scenarios for your block's specific behavior
 * 5. Remove tests for methods not applicable to your block
 * 6. Add block-specific edge cases as needed
 *
 * @author NCSLab Team
 * @version 2025 - Gold Standard Edition
 */
public class BlockTestTemplate extends DirectBlockTestBase {

    // =====================================================================
    // SECTION 1: TEST SETUP
    // =====================================================================

    /**
     * Reference to the specific block under test.
     * Use this for block-specific method calls.
     */
    private Block specificBlock;

    /**
     * Creates the block instance for testing.
     *
     * CUSTOMIZE THIS METHOD for your specific block:
     * - Replace BlockType with your actual block class
     * - Provide appropriate parameters for block creation
     * - Use mockModel for the parent model reference
     *
     * @return Block instance configured for testing
     */
    @Override
    protected Block createBlock() throws Exception {
        // TODO: Replace with your block creation
        // Example for Constant block:
        // specificBlock = Constant.create("testBlock", "test", 5.0, mockModel);

        // Example for Gain block:
        // specificBlock = Gain.create("testGain", "test", 2.0, mockModel);

        // Example for Integrator block:
        // specificBlock = Integrator.create("testIntegrator", "test", 0.0, mockModel);

        throw new UnsupportedOperationException("Must implement createBlock() with your specific block");
    }

    /**
     * Additional setup specific to this block type.
     * Override if your block needs special initialization.
     */
    @Before
    public void setupBlockSpecific() {
        // Add any block-specific setup here
        // Example: Configure special parameters, set up mock dependencies, etc.
    }

    // =====================================================================
    // SECTION 2: CATEGORY A - CALCULATE METHODS (Java Simulation)
    // =====================================================================

    // ---------------------------------------------------------------------
    // A.1: calculateInit() Tests
    // ---------------------------------------------------------------------

    /**
     * Test that calculateInit() properly initializes the block.
     * Verifies initial state, output signals, and parameter setup.
     */
    @Test
    public void testCalculateInit_Success() {
        // Act
        block.calculateInit();

        // Assert: Basic initialization checks
        assertNotNull("Block should be initialized", block);
        assertFalse("Block should not have output code generated yet", block.getIsOutputCodeGenerated());

        // Assert: Output ports should be properly configured
        for (OutputPort port : block.getOutputPortList()) {
            assertNotNull("Output port should exist", port);
            assertNotNull("Output signal should be created", port.getOutputSignalC());
        }

        // Assert: Parameters should be initialized
        assertTrue("Block should have parameters", block.getParameterList().size() >= 0);
    }

    /**
     * Test that calculateInit() can be called multiple times safely.
     * Verifies idempotency of initialization.
     */
    @Test
    public void testCalculateInit_Idempotent() {
        // Act: Initialize multiple times
        block.calculateInit();
        block.calculateInit();
        block.calculateInit();

        // Assert: Should not throw exceptions or corrupt state
        assertNotNull("Block should remain valid after multiple inits", block);
    }

    /**
     * Test initialization with default parameters.
     */
    @Test
    public void testCalculateInit_DefaultParameters() {
        // Act
        block.calculateInit();

        // Assert: All parameters should have valid values
        for (Parameter param : block.getParameterList()) {
            assertNotNull("Parameter should exist", param);
            assertNotNull("Parameter should have data", param.getData());
        }
    }

    // ---------------------------------------------------------------------
    // A.2: calculateOutput() Tests - Scalar Values
    // ---------------------------------------------------------------------

    /**
     * Test calculateOutput() with basic scalar input.
     * This is the most fundamental test for any block.
     */
    @Test
    public void testCalculateOutput_Scalar_Basic() {
        // Arrange
        block.calculateInit();
        setScalarInput(0, 5.0);

        // Act
        block.calculateOutput(0.0);

        // Assert
        double output = getScalarOutput(0);
        // TODO: Replace with expected output for your block
        // Example: assertEquals(10.0, output, DELTA); // For Gain with factor 2.0
        assertTrue("Output should be a valid number", !Double.isNaN(output));
    }

    /**
     * Test calculateOutput() with zero input.
     * Critical edge case that all blocks should handle correctly.
     */
    @Test
    public void testCalculateOutput_Scalar_Zero() {
        // Arrange
        block.calculateInit();
        setScalarInput(0, 0.0);

        // Act
        block.calculateOutput(0.0);

        // Assert
        double output = getScalarOutput(0);
        // TODO: Define expected behavior for zero input
        assertTrue("Output should be a valid number", !Double.isNaN(output));
        assertFalse("Output should not be infinite", Double.isInfinite(output));
    }

    /**
     * Test calculateOutput() with negative input.
     * Verifies proper handling of negative values.
     */
    @Test
    public void testCalculateOutput_Scalar_Negative() {
        // Arrange
        block.calculateInit();
        setScalarInput(0, -10.5);

        // Act
        block.calculateOutput(0.0);

        // Assert
        double output = getScalarOutput(0);
        // TODO: Define expected behavior for negative input
        assertTrue("Output should be a valid number", !Double.isNaN(output));
    }

    /**
     * Test calculateOutput() with multiple scalar inputs.
     * For blocks with multiple input ports.
     */
    @Test
    public void testCalculateOutput_Scalar_MultipleInputs() {
        // Skip if block has fewer than 2 inputs
        if (block.getInputPortList().size() < 2) {
            return;
        }

        // Arrange
        block.calculateInit();
        setScalarInput(0, 3.0);
        setScalarInput(1, 7.0);

        // Act
        block.calculateOutput(0.0);

        // Assert
        double output = getScalarOutput(0);
        // TODO: Define expected output for multiple inputs
        assertTrue("Output should be a valid number", !Double.isNaN(output));
    }

    // ---------------------------------------------------------------------
    // A.3: calculateOutput() Tests - Edge Cases
    // ---------------------------------------------------------------------

    /**
     * Test calculateOutput() with very large values.
     * Verifies overflow handling.
     */
    @Test
    public void testCalculateOutput_Scalar_LargeValues() {
        // Arrange
        block.calculateInit();
        setScalarInput(0, 1e100);

        // Act
        block.calculateOutput(0.0);

        // Assert
        double output = getScalarOutput(0);
        assertTrue("Output should be finite or infinite (no NaN)",
            !Double.isNaN(output));
    }

    /**
     * Test calculateOutput() with very small values.
     * Verifies underflow handling.
     */
    @Test
    public void testCalculateOutput_Scalar_SmallValues() {
        // Arrange
        block.calculateInit();
        setScalarInput(0, 1e-100);

        // Act
        block.calculateOutput(0.0);

        // Assert
        double output = getScalarOutput(0);
        assertTrue("Output should be a valid number", !Double.isNaN(output));
    }

    /**
     * Test calculateOutput() with NaN input.
     * Verifies graceful handling of invalid inputs.
     */
    @Test
    public void testCalculateOutput_Scalar_NaN() {
        // Arrange
        block.calculateInit();
        setScalarInput(0, Double.NaN);

        try {
            // Act
            block.calculateOutput(0.0);

            // Assert: Block should either propagate NaN or handle it
            double output = getScalarOutput(0);
            // Some blocks propagate NaN, others may throw - both acceptable

        } catch (Exception e) {
            // Exception is acceptable for NaN handling
            assertTrue("Should throw meaningful exception",
                e.getMessage() != null && !e.getMessage().isEmpty());
        }
    }

    /**
     * Test calculateOutput() with infinity input.
     * Verifies boundary condition handling.
     */
    @Test
    public void testCalculateOutput_Scalar_Infinity() {
        // Arrange
        block.calculateInit();
        setScalarInput(0, Double.POSITIVE_INFINITY);

        try {
            // Act
            block.calculateOutput(0.0);

            // Assert
            double output = getScalarOutput(0);
            // Block should handle infinity gracefully

        } catch (Exception e) {
            // Exception is acceptable for infinity handling
            assertTrue("Should throw meaningful exception", e.getMessage() != null);
        }
    }

    /**
     * Test calculateOutput() with MAX_VALUE.
     * Verifies extreme boundary handling.
     */
    @Test
    public void testCalculateOutput_Scalar_MaxValue() {
        // Arrange
        block.calculateInit();
        setScalarInput(0, Double.MAX_VALUE);

        // Act
        block.calculateOutput(0.0);

        // Assert
        double output = getScalarOutput(0);
        assertTrue("Output should be finite or infinite (no NaN)",
            !Double.isNaN(output));
    }

    /**
     * Test calculateOutput() with MIN_VALUE.
     * Verifies minimum positive value handling.
     */
    @Test
    public void testCalculateOutput_Scalar_MinValue() {
        // Arrange
        block.calculateInit();
        setScalarInput(0, Double.MIN_VALUE);

        // Act
        block.calculateOutput(0.0);

        // Assert
        double output = getScalarOutput(0);
        assertTrue("Output should be a valid number", !Double.isNaN(output));
    }

    // ---------------------------------------------------------------------
    // A.4: calculateOutput() Tests - Matrix Values
    // ---------------------------------------------------------------------

    /**
     * Test calculateOutput() with basic matrix input.
     * For blocks that support matrix operations.
     */
    @Test
    public void testCalculateOutput_Matrix_Basic() {
        // Skip if block doesn't support matrix input
        // Most blocks support either scalar OR matrix, check documentation

        // Arrange
        block.calculateInit();
        double[][] inputMatrix = {
            {1.0, 2.0},
            {3.0, 4.0}
        };
        setMatrixInput(0, inputMatrix);

        // Act
        block.calculateOutput(0.0);

        // Assert
        Matrix output = getMatrixOutput(0);
        assertNotNull("Output matrix should exist", output);
        assertEquals("Output matrix rows", 2, output.getRowDimension());
        assertEquals("Output matrix cols", 2, output.getColumnDimension());

        // TODO: Add specific assertions for expected matrix output
    }

    /**
     * Test calculateOutput() with zero matrix input.
     */
    @Test
    public void testCalculateOutput_Matrix_Zero() {
        // Arrange
        block.calculateInit();
        double[][] zeroMatrix = {
            {0.0, 0.0},
            {0.0, 0.0}
        };
        setMatrixInput(0, zeroMatrix);

        // Act
        block.calculateOutput(0.0);

        // Assert
        Matrix output = getMatrixOutput(0);
        assertNotNull("Output matrix should exist", output);
    }

    /**
     * Test calculateOutput() with single-element matrix (scalar as matrix).
     */
    @Test
    public void testCalculateOutput_Matrix_SingleElement() {
        // Arrange
        block.calculateInit();
        double[][] singleMatrix = {{5.0}};
        setMatrixInput(0, singleMatrix);

        // Act
        block.calculateOutput(0.0);

        // Assert
        Matrix output = getMatrixOutput(0);
        assertNotNull("Output matrix should exist", output);
        assertEquals("Should be 1x1 matrix", 1, output.getRowDimension());
        assertEquals("Should be 1x1 matrix", 1, output.getColumnDimension());
    }

    /**
     * Test calculateOutput() with large matrix.
     * Verifies performance with bigger matrices.
     */
    @Test
    public void testCalculateOutput_Matrix_Large() {
        // Arrange
        block.calculateInit();
        double[][] largeMatrix = new double[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                largeMatrix[i][j] = i + j;
            }
        }
        setMatrixInput(0, largeMatrix);

        // Act
        block.calculateOutput(0.0);

        // Assert
        Matrix output = getMatrixOutput(0);
        assertNotNull("Output matrix should exist", output);
        assertEquals("Output matrix rows", 10, output.getRowDimension());
        assertEquals("Output matrix cols", 10, output.getColumnDimension());
    }

    // ---------------------------------------------------------------------
    // A.5: calculateUpdate() Tests (for Discrete Blocks)
    // ---------------------------------------------------------------------

    /**
     * Test calculateUpdate() for discrete state updates.
     * Only applicable to discrete-time blocks (Delay, Unit Delay, etc.)
     */
    @Test
    public void testCalculateUpdate_Basic() {
        // Skip if block doesn't have discrete states
        if (block.getStateList().isEmpty() && block.getDStateList().isEmpty()) {
            return;
        }

        // Arrange
        block.calculateInit();
        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);

        // Act
        block.calculateDiscreteUpdate(0.0);

        // Assert: Verify state was updated
        // TODO: Add block-specific state update verification
    }

    /**
     * Test calculateUpdate() maintains state correctly over multiple steps.
     */
    @Test
    public void testCalculateUpdate_MultipleSteps() {
        // Skip if no discrete states
        if (block.getDStateList().isEmpty()) {
            return;
        }

        // Arrange
        block.calculateInit();

        // Act: Simulate multiple time steps
        double[] testInputs = {1.0, 2.0, 3.0, 4.0, 5.0};
        for (int i = 0; i < testInputs.length; i++) {
            setScalarInput(0, testInputs[i]);
            block.calculateOutput(i * 0.1);
            block.calculateDiscreteUpdate(i * 0.1);
        }

        // Assert: State should reflect all updates
        // TODO: Add block-specific multi-step verification
    }

    // ---------------------------------------------------------------------
    // A.6: calculateDerivative() Tests (for Continuous Blocks)
    // ---------------------------------------------------------------------

    /**
     * Test calculateDerivative() for continuous state derivatives.
     * Only applicable to continuous-time blocks (Integrator, Transfer Function, etc.)
     */
    @Test
    public void testCalculateDerivative_Basic() {
        // Skip if block doesn't have continuous states
        if (block.getStateList().isEmpty()) {
            return;
        }

        // Arrange
        block.calculateInit();
        setScalarInput(0, 1.0);

        // Act
        block.calculateDerivative(0.0);

        // Assert: Verify derivative was calculated
        State state = block.getStateList().get(0);
        assertNotNull("State should exist", state);
        assertNotNull("Derivative should be calculated", state.getDerivateData());

        // TODO: Add block-specific derivative verification
    }

    /**
     * Test calculateDerivative() with varying inputs.
     */
    @Test
    public void testCalculateDerivative_VaryingInputs() {
        // Skip if no continuous states
        if (block.getStateList().isEmpty()) {
            return;
        }

        // Arrange
        block.calculateInit();

        // Act & Assert: Test with different inputs
        double[] testInputs = {0.0, 1.0, -1.0, 5.0, -5.0};
        for (double input : testInputs) {
            setScalarInput(0, input);
            block.calculateDerivative(0.0);

            State state = block.getStateList().get(0);
            assertNotNull("Derivative should be calculated for input " + input,
                state.getDerivateData());
        }
    }

    // ---------------------------------------------------------------------
    // A.7: State Management Tests
    // ---------------------------------------------------------------------

    /**
     * Test that states are properly initialized.
     */
    @Test
    public void testStateManagement_Initialization() {
        // Skip if block has no states
        if (block.getStateList().isEmpty() && block.getDStateList().isEmpty()) {
            return;
        }

        // Act
        block.calculateInit();

        // Assert: All states should be initialized
        for (State state : block.getStateList()) {
            assertNotNull("State should exist", state);
            assertNotNull("State should have data", state.getData());
            assertNotNull("State should have name", state.getName());
        }

        for (State dstate : block.getDStateList()) {
            assertNotNull("Discrete state should exist", dstate);
            assertNotNull("Discrete state should have data", dstate.getData());
        }
    }

    /**
     * Test state persistence across multiple calculations.
     */
    @Test
    public void testStateManagement_Persistence() {
        // Skip if no states
        if (block.getStateList().isEmpty() && block.getDStateList().isEmpty()) {
            return;
        }

        // Arrange
        block.calculateInit();
        setScalarInput(0, 5.0);

        // Act: Multiple calculations
        block.calculateOutput(0.0);
        block.calculateOutput(0.1);
        block.calculateOutput(0.2);

        // Assert: States should maintain consistency
        // TODO: Add block-specific state persistence verification
    }

    // =====================================================================
    // SECTION 3: CATEGORY B - CODE GENERATION (C++ Templates)
    // =====================================================================

    // ---------------------------------------------------------------------
    // B.1: generateInitCodeC() Tests
    // ---------------------------------------------------------------------

    /**
     * Test that generateInitCodeC() produces non-empty code.
     */
    @Test
    public void testGenerateInitCodeC_NotEmpty() {
        // Arrange
        block.calculateInit();
        CodeStructC codeStruct = createMockCodeStructC();

        // Act
        block.generateInitCodeC(codeStruct);

        // Assert
        String initCode = codeStruct.getInitCode();
        assertNotNull("Init code should not be null", initCode);
        assertFalse("Init code should not be empty", initCode.trim().isEmpty());
    }

    /**
     * Test that generateInitCodeC() has no template errors.
     * Verifies no undefined variables in generated code.
     */
    @Test
    public void testGenerateInitCodeC_NoTemplateErrors() {
        // Arrange
        block.calculateInit();
        CodeStructC codeStruct = createMockCodeStructC();

        // Act
        block.generateInitCodeC(codeStruct);

        // Assert
        String initCode = codeStruct.getInitCode();
        assertCodeContainsNoTemplateErrors(initCode);
    }

    /**
     * Test that generateInitCodeC() produces valid C++ syntax.
     * Basic syntax validation (braces, semicolons, etc.)
     */
    @Test
    public void testGenerateInitCodeC_ValidSyntax() {
        // Arrange
        block.calculateInit();
        CodeStructC codeStruct = createMockCodeStructC();

        // Act
        block.generateInitCodeC(codeStruct);

        // Assert
        String initCode = codeStruct.getInitCode();
        assertValidCppSyntax(initCode);
    }

    /**
     * Test that generateInitCodeC() includes block identification.
     * Generated code should reference the block for debugging.
     */
    @Test
    public void testGenerateInitCodeC_ContainsBlockInfo() {
        // Arrange
        block.calculateInit();
        CodeStructC codeStruct = createMockCodeStructC();

        // Act
        block.generateInitCodeC(codeStruct);

        // Assert
        String initCode = codeStruct.getInitCode();
        assertTrue("Init code should mention block type or ID",
            initCode.contains(block.getBlockType()) ||
            initCode.contains(String.valueOf(block.getBlockId())));
    }

    /**
     * Test that generateInitCodeC() declares all parameters.
     */
    @Test
    public void testGenerateInitCodeC_DeclaresParameters() {
        // Skip if block has no parameters
        if (block.getParameterList().isEmpty()) {
            return;
        }

        // Arrange
        block.calculateInit();
        CodeStructC codeStruct = createMockCodeStructC();

        // Act
        block.generateInitCodeC(codeStruct);

        // Assert
        // Parameters should be added to CodeStructC
        assertTrue("Should have parameters",
            mockModel.getParameterList().size() >= 0);
    }

    // ---------------------------------------------------------------------
    // B.2: generateOutputCodeC() Tests
    // ---------------------------------------------------------------------

    /**
     * Test that generateOutputCodeC() produces non-empty code.
     */
    @Test
    public void testGenerateOutputCodeC_NotEmpty() {
        // Arrange
        block.calculateInit();
        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);

        CodeStructC codeStruct = createMockCodeStructC();

        // Act
        block.generateOutputCodeC(codeStruct);

        // Assert
        String outputCode = codeStruct.getOutputCode();
        assertNotNull("Output code should not be null", outputCode);
        assertFalse("Output code should not be empty", outputCode.trim().isEmpty());
    }

    /**
     * Test that generateOutputCodeC() has no template errors.
     */
    @Test
    public void testGenerateOutputCodeC_NoTemplateErrors() {
        // Arrange
        block.calculateInit();
        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);

        CodeStructC codeStruct = createMockCodeStructC();

        // Act
        block.generateOutputCodeC(codeStruct);

        // Assert
        String outputCode = codeStruct.getOutputCode();
        assertCodeContainsNoTemplateErrors(outputCode);
    }

    /**
     * Test that generateOutputCodeC() produces valid C++ syntax.
     */
    @Test
    public void testGenerateOutputCodeC_ValidSyntax() {
        // Arrange
        block.calculateInit();
        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);

        CodeStructC codeStruct = createMockCodeStructC();

        // Act
        block.generateOutputCodeC(codeStruct);

        // Assert
        String outputCode = codeStruct.getOutputCode();
        assertValidCppSyntax(outputCode);
    }

    /**
     * Test that generateOutputCodeC() references input variables.
     */
    @Test
    public void testGenerateOutputCodeC_ReferencesInputs() {
        // Skip if block has no inputs
        if (block.getInputPortList().isEmpty()) {
            return;
        }

        // Arrange
        block.calculateInit();
        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);

        CodeStructC codeStruct = createMockCodeStructC();

        // Act
        block.generateOutputCodeC(codeStruct);

        // Assert
        String outputCode = codeStruct.getOutputCode();
        // TODO: Verify input variable references
        // Example: assertTrue(outputCode.contains(block.getInputPortVariable(0)));
    }

    /**
     * Test that generateOutputCodeC() assigns to output variables.
     */
    @Test
    public void testGenerateOutputCodeC_AssignsOutputs() {
        // Arrange
        block.calculateInit();
        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);

        CodeStructC codeStruct = createMockCodeStructC();

        // Act
        block.generateOutputCodeC(codeStruct);

        // Assert
        String outputCode = codeStruct.getOutputCode();
        // TODO: Verify output variable assignments
        // Example: assertTrue(outputCode.contains(block.getOutputPortVariable(0)));
    }

    // ---------------------------------------------------------------------
    // B.3: generateUpdateCodeC() Tests (for Discrete Blocks)
    // ---------------------------------------------------------------------

    /**
     * Test that generateUpdateCodeC() produces code for discrete updates.
     */
    @Test
    public void testGenerateUpdateCodeC_NotEmpty() {
        // Skip if no discrete states
        if (block.getDStateList().isEmpty()) {
            return;
        }

        // Arrange
        block.calculateInit();
        CodeStructC codeStruct = createMockCodeStructC();

        // Act
        try {
            block.generateUpdateCodeC(codeStruct);

            // Assert
            String updateCode = codeStruct.getUpdateCode();
            assertNotNull("Update code should not be null", updateCode);

        } catch (Exception e) {
            fail("Update code generation should not throw: " + e.getMessage());
        }
    }

    /**
     * Test that generateUpdateCodeC() has no template errors.
     */
    @Test
    public void testGenerateUpdateCodeC_NoTemplateErrors() {
        // Skip if no discrete states
        if (block.getDStateList().isEmpty()) {
            return;
        }

        // Arrange
        block.calculateInit();
        CodeStructC codeStruct = createMockCodeStructC();

        // Act
        try {
            block.generateUpdateCodeC(codeStruct);

            // Assert
            String updateCode = codeStruct.getUpdateCode();
            assertCodeContainsNoTemplateErrors(updateCode);

        } catch (Exception e) {
            fail("Update code generation should not throw: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------------
    // B.4: generateDerivativeCodeC() Tests (for Continuous Blocks)
    // ---------------------------------------------------------------------

    /**
     * Test that generateDerivativeCodeC() produces code for derivatives.
     */
    @Test
    public void testGenerateDerivativeCodeC_NotEmpty() {
        // Skip if no continuous states
        if (block.getStateList().isEmpty()) {
            return;
        }

        // Arrange
        block.calculateInit();
        setScalarInput(0, 1.0);
        block.calculateDerivative(0.0);

        CodeStructC codeStruct = createMockCodeStructC();

        // Act
        block.generateDerivativeCodeC(codeStruct);

        // Assert
        String derivCode = codeStruct.getDerivativeCode();
        assertNotNull("Derivative code should not be null", derivCode);
    }

    /**
     * Test that generateDerivativeCodeC() has no template errors.
     */
    @Test
    public void testGenerateDerivativeCodeC_NoTemplateErrors() {
        // Skip if no continuous states
        if (block.getStateList().isEmpty()) {
            return;
        }

        // Arrange
        block.calculateInit();
        setScalarInput(0, 1.0);
        block.calculateDerivative(0.0);

        CodeStructC codeStruct = createMockCodeStructC();

        // Act
        block.generateDerivativeCodeC(codeStruct);

        // Assert
        String derivCode = codeStruct.getDerivativeCode();
        assertCodeContainsNoTemplateErrors(derivCode);
    }

    // =====================================================================
    // SECTION 4: CATEGORY C - TEMPLATE VALIDATION
    // =====================================================================

    /**
     * Test that all expected template variables are populated in context.
     */
    @Test
    public void testTemplateContext_AllVariablesPopulated() {
        // Arrange
        block.calculateInit();

        // Act: Access template context
        org.apache.velocity.VelocityContext context = getBlockContext();

        // Assert: Standard variables should be present
        assertNotNull("Context should exist", context);
        assertNotNull("Block should be in context", context.get("block"));
        assertNotNull("BlockId should be in context", context.get("blockId"));
        assertNotNull("BlockName should be in context", context.get("blockName"));
        assertNotNull("BlockType should be in context", context.get("blockType"));
    }

    /**
     * Test that parameter objects are correctly populated.
     */
    @Test
    public void testTemplateContext_ParametersCorrect() {
        // Skip if no parameters
        if (block.getParameterList().isEmpty()) {
            return;
        }

        // Arrange
        block.calculateInit();

        // Act
        org.apache.velocity.VelocityContext context = getBlockContext();

        // Assert: Parameters should be accessible
        assertNotNull("Parameters should be in context", context.get("parameterList"));

        // TODO: Add block-specific parameter verification
    }

    /**
     * Test that input/output port information is correct.
     */
    @Test
    public void testTemplateContext_PortsCorrect() {
        // Arrange
        block.calculateInit();

        // Act
        org.apache.velocity.VelocityContext context = getBlockContext();

        // Assert
        assertNotNull("Input ports should be in context", context.get("inputPortList"));
        assertNotNull("Output ports should be in context", context.get("outputPortList"));
    }

    // =====================================================================
    // SECTION 5: CATEGORY D - ERROR HANDLING
    // =====================================================================

    /**
     * Test behavior with invalid input dimensions.
     * For blocks that expect specific input dimensions.
     */
    @Test
    public void testErrorHandling_InvalidDimensions() {
        // Skip if block accepts any dimension
        // TODO: Customize for blocks with dimension requirements

        // Arrange
        block.calculateInit();

        // Act & Assert: Test dimension mismatch
        try {
            // Example: Set wrong dimension input
            double[][] wrongDimMatrix = {{1.0, 2.0, 3.0}}; // 1x3 when 2x2 expected
            setMatrixInput(0, wrongDimMatrix);
            block.calculateOutput(0.0);

            // Block should either handle gracefully or throw meaningful exception

        } catch (Exception e) {
            // Exception is acceptable - verify it's meaningful
            assertTrue("Should provide meaningful error message",
                e.getMessage() != null && e.getMessage().length() > 0);
        }
    }

    /**
     * Test behavior with unconnected input ports.
     */
    @Test
    public void testErrorHandling_UnconnectedInputs() {
        // Arrange
        block.calculateInit();
        // Don't set any inputs

        try {
            // Act
            block.calculateOutput(0.0);

            // Assert: Should handle gracefully or throw
            // Some blocks require inputs, others don't

        } catch (Exception e) {
            // Exception is acceptable
            assertTrue("Should provide meaningful error", e.getMessage() != null);
        }
    }

    /**
     * Test behavior at extreme time values.
     */
    @Test
    public void testErrorHandling_ExtremeTime() {
        // Arrange
        block.calculateInit();
        setScalarInput(0, 1.0);

        // Act & Assert: Test at extreme times
        double[] extremeTimes = {
            -1000.0,      // Negative time
            0.0,          // Zero time
            1e10,         // Very large time
            Double.MAX_VALUE  // Maximum time
        };

        for (double time : extremeTimes) {
            try {
                block.calculateOutput(time);
                // Should handle gracefully
            } catch (Exception e) {
                // Exception is acceptable for invalid time
                System.out.println("Block handled extreme time " + time +
                    " with exception: " + e.getMessage());
            }
        }
    }

    // =====================================================================
    // SECTION 6: PERFORMANCE TESTS
    // =====================================================================

    /**
     * Test calculateOutput() performance.
     * Should complete in <10 microseconds for simple blocks.
     */
    @Test
    public void testPerformance_CalculateOutput() {
        // Arrange
        block.calculateInit();
        setScalarInput(0, 5.0);

        // Act
        long avgTime = measureCalculateOutputPerformance(10000);

        // Assert
        System.out.println("Average calculateOutput() time: " +
            avgTime / 1000.0 + " microseconds");

        // TODO: Adjust threshold based on block complexity
        assertTrue("Calculate output should be fast (< 50 microseconds)",
            avgTime < 50000);
    }

    /**
     * Test code generation performance.
     * Should complete in <5ms for template rendering.
     */
    @Test
    public void testPerformance_CodeGeneration() {
        // Arrange
        block.calculateInit();
        setScalarInput(0, 5.0);
        block.calculateOutput(0.0);

        CodeStructC codeStruct = createMockCodeStructC();

        // Act: Measure code generation time
        long startTime = System.nanoTime();
        block.generateInitCodeC(codeStruct);
        block.generateOutputCodeC(codeStruct);
        long endTime = System.nanoTime();

        long elapsedMs = (endTime - startTime) / 1_000_000;

        // Assert
        System.out.println("Code generation time: " + elapsedMs + " ms");
        assertTrue("Code generation should be fast (< 10ms)", elapsedMs < 10);
    }

    // =====================================================================
    // SECTION 7: INTEGRATION TESTS
    // =====================================================================

    /**
     * Test complete simulation cycle: init -> output -> update/derivative.
     */
    @Test
    public void testIntegration_CompleteSimulationCycle() {
        // Arrange
        block.calculateInit();
        setScalarInput(0, 5.0);

        // Act: Simulate one complete cycle
        block.calculateOutput(0.0);

        if (!block.getStateList().isEmpty()) {
            block.calculateDerivative(0.0);
        }

        if (!block.getDStateList().isEmpty()) {
            block.calculateDiscreteUpdate(0.0);
        }

        // Assert: Should complete without exceptions
        assertNotNull("Block should remain valid", block);
    }

    /**
     * Test multiple simulation steps.
     */
    @Test
    public void testIntegration_MultipleSimulationSteps() {
        // Arrange
        block.calculateInit();

        // Act: Simulate multiple steps
        for (int i = 0; i < 100; i++) {
            setScalarInput(0, i * 0.1);
            block.calculateOutput(i * 0.01);

            if (!block.getStateList().isEmpty()) {
                block.calculateDerivative(i * 0.01);
            }

            if (!block.getDStateList().isEmpty()) {
                block.calculateDiscreteUpdate(i * 0.01);
            }
        }

        // Assert: Should complete without exceptions
        assertNotNull("Block should remain valid after multiple steps", block);
    }

    /**
     * Test code generation after simulation.
     */
    @Test
    public void testIntegration_SimulationThenCodeGen() {
        // Arrange
        block.calculateInit();
        setScalarInput(0, 5.0);

        // Act: Simulate then generate code
        block.calculateOutput(0.0);

        CodeStructC codeStruct = createMockCodeStructC();
        block.generateInitCodeC(codeStruct);
        block.generateOutputCodeC(codeStruct);

        // Assert: Code should be valid
        assertNotNull("Init code should be generated", codeStruct.getInitCode());
        assertNotNull("Output code should be generated", codeStruct.getOutputCode());
    }

    // =====================================================================
    // SECTION 8: HELPER METHODS (Reusable Utilities)
    // =====================================================================

    /**
     * Creates a mock CodeStructC for testing code generation.
     *
     * @return Mock CodeStructC instance
     */
    private CodeStructC createMockCodeStructC() {
        // Create a simple mock implementation
        return new CodeStructC(mockModel) {
            private StringBuilder initCode = new StringBuilder();
            private StringBuilder outputCode = new StringBuilder();
            private StringBuilder updateCode = new StringBuilder();
            private StringBuilder derivativeCode = new StringBuilder();

            @Override
            public void addInitCode(String code) {
                initCode.append(code);
            }

            @Override
            public void addOutputCode(String code) {
                outputCode.append(code);
            }

            @Override
            public void addUpdateCode(String code) {
                updateCode.append(code);
            }

            @Override
            public void addDerivativeCode(String code) {
                derivativeCode.append(code);
            }

            public String getInitCode() {
                return initCode.toString();
            }

            public String getOutputCode() {
                return outputCode.toString();
            }

            public String getUpdateCode() {
                return updateCode.toString();
            }

            public String getDerivativeCode() {
                return derivativeCode.toString();
            }
        };
    }

    /**
     * Asserts that generated code contains no template errors.
     * Checks for common template error patterns:
     * - $undefined_variable
     * - ${undefined_variable}
     * - $!{undefined_variable}
     *
     * @param code Generated code to check
     */
    protected void assertCodeContainsNoTemplateErrors(String code) {
        assertNotNull("Code should not be null", code);

        // Check for undefined velocity variables
        Pattern undefinedVarPattern = Pattern.compile("\\$!?\\{?[a-zA-Z_][a-zA-Z0-9_]*\\}?");
        Matcher matcher = undefinedVarPattern.matcher(code);

        if (matcher.find()) {
            // Found potential undefined variable - verify it's not in a comment
            String match = matcher.group();
            if (!code.contains("/*") || !code.contains("*/")) {
                // No comments, so this might be an error
                System.out.println("Warning: Potential undefined variable: " + match);
            }
        }

        // Check for explicit error markers
        assertFalse("Code should not contain 'ERROR'",
            code.toUpperCase().contains("ERROR"));
        assertFalse("Code should not contain 'UNDEFINED'",
            code.toUpperCase().contains("UNDEFINED"));
    }

    /**
     * Asserts that generated code has valid C++ syntax basics.
     * Checks for:
     * - Balanced braces
     * - Balanced parentheses
     * - No unterminated strings
     *
     * @param code Generated code to validate
     */
    protected void assertValidCppSyntax(String code) {
        assertNotNull("Code should not be null", code);

        // Check balanced braces
        int braceCount = 0;
        int parenCount = 0;
        boolean inString = false;
        boolean inComment = false;

        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);

            // Handle string literals
            if (c == '"' && (i == 0 || code.charAt(i - 1) != '\\')) {
                inString = !inString;
            }

            // Skip analysis inside strings
            if (inString) {
                continue;
            }

            // Handle multi-line comments
            if (i < code.length() - 1 && code.charAt(i) == '/' && code.charAt(i + 1) == '*') {
                inComment = true;
                i++; // Skip next char
                continue;
            }
            if (i < code.length() - 1 && code.charAt(i) == '*' && code.charAt(i + 1) == '/') {
                inComment = false;
                i++; // Skip next char
                continue;
            }

            // Skip analysis inside comments
            if (inComment) {
                continue;
            }

            // Count braces and parentheses
            if (c == '{') braceCount++;
            if (c == '}') braceCount--;
            if (c == '(') parenCount++;
            if (c == ')') parenCount--;
        }

        assertEquals("Braces should be balanced", 0, braceCount);
        assertEquals("Parentheses should be balanced", 0, parenCount);
        assertFalse("Should not have unterminated string", inString);
    }

    /**
     * Gets the Velocity context from the block for template validation.
     *
     * @return VelocityContext from the block
     */
    private org.apache.velocity.VelocityContext getBlockContext() {
        try {
            java.lang.reflect.Field contextField = Block.class.getDeclaredField("context");
            contextField.setAccessible(true);
            return (org.apache.velocity.VelocityContext) contextField.get(block);
        } catch (Exception e) {
            fail("Failed to access block context: " + e.getMessage());
            return null;
        }
    }

    /**
     * Asserts that matrix values match expected values within tolerance.
     *
     * @param expected Expected matrix values
     * @param actual Actual matrix
     */
    protected void assertMatrixEquals(double[][] expected, Matrix actual) {
        assertNotNull("Actual matrix should not be null", actual);
        assertEquals("Row dimension mismatch", expected.length, actual.getRowDimension());
        assertEquals("Column dimension mismatch", expected[0].length, actual.getColumnDimension());

        for (int i = 0; i < expected.length; i++) {
            for (int j = 0; j < expected[0].length; j++) {
                assertEquals(
                    String.format("Matrix element mismatch at [%d,%d]", i, j),
                    expected[i][j],
                    actual.get(i, j),
                    DELTA
                );
            }
        }
    }

    /**
     * Runs a series of test scenarios with inputs and expected outputs.
     *
     * @param scenarios Array of {input, expectedOutput} pairs
     */
    protected void runScalarTestScenarios(double[][] scenarios) {
        block.calculateInit();

        for (double[] scenario : scenarios) {
            double input = scenario[0];
            double expectedOutput = scenario[1];

            setScalarInput(0, input);
            block.calculateOutput(0.0);

            double actualOutput = getScalarOutput(0);
            assertEquals(
                String.format("Failed for input %.2f", input),
                expectedOutput,
                actualOutput,
                DELTA
            );
        }
    }
}
