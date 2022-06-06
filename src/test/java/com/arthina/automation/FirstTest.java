package com.arthina.automation;

import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FirstTest {

	@BeforeSuite
	public void initSuite(ITestContext testContext) {
		ExtentTestManager.startReport(testContext.getSuite().getName());
	}

	@BeforeClass
	public void setUp(ITestContext testContext) {
        ExtentTestManager.createTest(this.getClass().getSimpleName());
    }

    @BeforeMethod
	public void initTest(ITestContext testContext, Method method, final Object[] dataDrivenTestName) {
		final String testName = dataDrivenTestName.length > 0 ? method.getName() + "_" + ObjectUtils.toString(dataDrivenTestName[1], "EMPTY") : method.getName();
		Test test = method.getAnnotation(Test.class);
		final String testDescription = test.description();
		
		log.info("$$$$$$$ Initializing the Extent Report as: {} ({})", testName, testDescription);
		ExtentTestManager.startTest(this.getClass().getSimpleName(), testName, testDescription);		
	}
	



	@AfterMethod
	public void stopReport() {
		log.info("Extent Reporter getting flushed..");
		ExtentTestManager.endTest();
	}
	
	@AfterClass
	public void tearDown() {

	}    
}
