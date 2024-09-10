package com.ncslab;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;


/**
 * Unit test for simple App.
 */
@Category(PublicTest.class)
public class AppTest {

    @Before
    public void setUp() throws Exception {

    }

    /**
     * Rigorous Test.
     */
    @Test
    public void testApp() {
        assertEquals(1, 1);
    }
}
