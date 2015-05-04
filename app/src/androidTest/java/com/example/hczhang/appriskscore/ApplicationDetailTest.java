package com.example.hczhang.appriskscore;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by hczhang on 04/05/15.
 */
public class ApplicationDetailTest extends TestCase {


    private ApplicationDetail myTest;

    @Before
    public void setUp() throws Exception {
        myTest = new ApplicationDetail();

    }

    @Test
    public void testGetAppCategory() throws Exception {
        assertEquals("equals","Music & Audio","com.tencent.qqmusic");

    }
}