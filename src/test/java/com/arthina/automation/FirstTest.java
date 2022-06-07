package com.arthina.automation;

import org.testng.annotations.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FirstTest extends TestBase{

	@Test
	public void firstTest() {
		assertEquals("FirstTest", "FirstTest");
	}

	@Test
	public void secondTest() {
		assertEquals("SecondTest", "SecondTest");
	}

	@Test
	public void thirdTest() {
		assertEquals("ThirdTest", "ThirdTest");
	}


}
