package com.arthina.automation;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.*;

import java.lang.reflect.Method;

@Slf4j
public class TestBase {
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

        //log.info("$$$$$$$ Initializing the Extent Report as: {} ({})", testName, testDescription);
        ExtentTestManager.startTest(this.getClass().getSimpleName(), testName, testDescription);
    }

    @AfterMethod
    public void stopReport() {
        //log.info("Extent Reporter getting flushed..");
        ExtentTestManager.endTest();
    }

    @AfterClass
    public void tearDown() {

    }

    protected void assertEquals(final Object actualObj, final Object expectedObj) {
        try {
            Assert.assertEquals(actualObj, expectedObj);
            ExtentTestManager.logInfo("Validating the Actual: " + actualObj + " equals expected: " + expectedObj);
        }
        catch (AssertionError ae) {
            ExtentTestManager.logFail("Failed while Validating the Actual: " + actualObj + " equals expected: " + expectedObj);
            throw ae;
        }
    }
}
