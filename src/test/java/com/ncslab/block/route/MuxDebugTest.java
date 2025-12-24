package com.ncslab.block.route;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import org.junit.Test;

import static org.junit.Assert.*;

public class MuxDebugTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        return Mux.create("debugMux", "test", 2, mockModel);
    }

    @Test
    public void testBlockCreation() {
        System.out.println("Block type: " + block.getBlockType());
        System.out.println("Block name: " + block.getBlockName());
        System.out.println("Input ports: " + block.getInputPortList().size());
        System.out.println("Output ports: " + block.getOutputPortList().size());

        // Check field 'num' using reflection
        try {
            java.lang.reflect.Field numField = Mux.class.getDeclaredField("num");
            numField.setAccessible(true);
            int num = (int) numField.get(block);
            System.out.println("Field 'num': " + num);
        } catch (Exception e) {
            System.out.println("Could not access 'num' field: " + e.getMessage());
        }

        assertEquals("Should have 2 input ports", 2, block.getInputPortList().size());
    }

    @Test
    public void testSimpleCalculateOutput() {
        System.out.println("=== Before setScalarInput ===");
        System.out.println("Input ports: " + block.getInputPortList().size());
        System.out.println("Output port height: " + block.getOutputPortList().get(0).getHeight());
        System.out.println("Output port width: " + block.getOutputPortList().get(0).getWidth());

        System.out.println("=== Setting input 0 ===");
        setScalarInput(0, 5.0);
        System.out.println("Input 0 set successfully");
        System.out.println("Input 0 height: " + block.getInputPortList().get(0).getHeight());
        System.out.println("Input 0 width: " + block.getInputPortList().get(0).getWidth());

        System.out.println("=== Setting input 1 ===");
        setScalarInput(1, 3.0);
        System.out.println("Input 1 set successfully");
        System.out.println("Input 1 height: " + block.getInputPortList().get(1).getHeight());
        System.out.println("Input 1 width: " + block.getInputPortList().get(1).getWidth());

        System.out.println("=== Calling updateDimension ===");
        try {
            block.updateDimension();
            System.out.println("updateDimension completed");
            System.out.println("Output port height after update: " + block.getOutputPortList().get(0).getHeight());
            System.out.println("Output port width after update: " + block.getOutputPortList().get(0).getWidth());
        } catch (Exception e) {
            System.out.println("updateDimension threw exception: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== Calling calculateOutput ===");
        try {
            block.calculateOutput(0.0);
            System.out.println("calculateOutput completed successfully");
        } catch (Exception e) {
            System.out.println("calculateOutput threw exception: " + e.getClass().getName());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
