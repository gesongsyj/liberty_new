package com.liberty.system.web;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CurrencyControllerTest {
    private CurrencyController controller;

    @Before
    public void setUp() throws Exception {
        controller =  new CurrencyController();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void calibrate() {
        controller.calibrate();
    }

    @Test
    public void execute() {
    }
}
