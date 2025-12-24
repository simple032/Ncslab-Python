package com.ncslab.block.math;

import com.ncslab.block.DirectBlockTestBase;
import com.ncslab.block.Block;
import org.junit.Test;

import static org.junit.Assert.*;

public class DebugMathFunctionTest extends DirectBlockTestBase {

    @Override
    protected Block createBlock() throws Exception {
        return MathFunction.create("testMathFunction", "test", "exp", mockModel);
    }

    @Test
    public void testPowBlockInputPorts() throws Exception {
        Block powBlock = MathFunction.create("testPow", "test", "pow", mockModel);
        System.out.println("Pow block created. Input ports: " + powBlock.getInputPortList().size());

        assertEquals("pow block should have 2 input ports", 2, powBlock.getInputPortList().size());
    }

    @Test
    public void testExpBlockInputPorts() throws Exception {
        Block expBlock = MathFunction.create("testExp", "test", "exp", mockModel);
        System.out.println("Exp block created. Input ports: " + expBlock.getInputPortList().size());

        assertEquals("exp block should have 1 input port", 1, expBlock.getInputPortList().size());
    }
}
