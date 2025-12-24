package com.ncslab.code.c;

import com.ncslab.block.Block;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.GlobalVariable;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.ncslablink.NCSLabModel;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CodeStructCTest {

//    @Test
//    public void testPreventDuplicateParameters() {
//        // Create a mock CodeModelC
//        CodeModelC mockModel = mock(CodeModelC.class);
//
//        // Create a test implementation of CodeStructC
//        CodeStructC codeStruct = new CodeStructC(mockModel) {
//            @Override
//            public void writeCCodeFiles() {}
//        };
//
//        // Create mock blocks with same parameter name
//        Block mockBlock1 = mock(Block.class);
//        when(mockBlock1.getBlockId()).thenReturn(1);
//        when(mockBlock1.getBlockUUID()).thenReturn("uuid1");
//
//        Block mockBlock2 = mock(Block.class);
//        when(mockBlock2.getBlockId()).thenReturn(2);
//        when(mockBlock2.getBlockUUID()).thenReturn("uuid2");
//
//        // Create parameters with same name (simulating the bug)
//        Parameter param1 = new Parameter(mockBlock1, 1, "No", "1.0");
//        Parameter param2 = new Parameter(mockBlock2, 1, "No", "2.0");
//
//        // Both parameters would have the same name if blocks have null UUID
//        when(mockBlock1.getBlockUUID()).thenReturn(null);
//        when(mockBlock2.getBlockUUID()).thenReturn(null);
//        param1 = new Parameter(mockBlock1, 1, "No", "1.0");
//        param2 = new Parameter(mockBlock1, 1, "No", "2.0"); // Same block ID
//
//        // Add parameters multiple times
//        codeStruct.addParameter(param1);
//        codeStruct.addParameter(param2);
//        codeStruct.addParameter(param1); // Try to add duplicate
//
//        // Verify only one parameter was added
//        assertEquals("Should only have one parameter with the same name",
//                     1, codeStruct.getParameterList().size());
//    }
//
//    @Test
//    public void testPreventDuplicateStates() {
//        // Create a mock CodeModelC
//        CodeModelC mockModel = mock(CodeModelC.class);
//
//        // Create a test implementation of CodeStructC
//        CodeStructC codeStruct = new CodeStructC(mockModel) {
//            @Override
//            public void writeCCodeFiles() {}
//        };
//
//        // Create mock block
//        Block mockBlock = mock(Block.class);
//        when(mockBlock.getBlockId()).thenReturn(1);
//
//        // Create states with same name
//        State state1 = new State(mockBlock, 1, "state1", "0.0");
//        State state2 = new State(mockBlock, 1, "state1", "1.0");
//
//        // Add states multiple times
//        codeStruct.addState(state1);
//        codeStruct.addState(state2); // Same name, should be rejected
//        codeStruct.addState(state1); // Duplicate, should be rejected
//
//        // Verify only one state was added
//        assertEquals("Should only have one state with the same name",
//                     1, codeStruct.getStateList().size());
//    }
}
