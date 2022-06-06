package com.arthina.automation;



import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;

public class CustomLogListener implements ITestListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomLogListener.class);
	private static final String FILE_SEPARATOR = System.getProperty("file.separator");
	
	@Override
	public void onFinish(ITestContext testContext) {
		LOGGER.info("-------->>>>>> Test Summary <<<<<<<----------");
		testContext.getPassedTests().getAllResults().forEach(result -> {
			LOGGER.info("==========> {} PASSED", result.getName());
		});
		LOGGER.info("-------->>>>>> ---------------------------- <<<<<<<----------");
		testContext.getFailedTests().getAllResults().forEach(result -> {
			LOGGER.info("==========> {} FAILED", result.getName());
		});
		LOGGER.info("-------->>>>>> ---------------------------- <<<<<<<----------");
		testContext.getSkippedTests().getAllResults().forEach(result -> {
			LOGGER.info("==========> {} SKIPPED", result.getName());
		});
		LOGGER.info("-------->>>>>> ---------------------------- <<<<<<<----------");		
	}

	@Override
	public void onStart(ITestContext testContext) {
		LOGGER.info("-------->>>>>> Starting the Tests...: {} on {}", testContext.getName(), testContext.getStartDate());
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult testContext) {
		LOGGER.info("*** Test failed but within percentage % {}", testContext.getMethod().getMethodName());
	}

	@Override
	public void onTestFailure(ITestResult testContext) {
		LOGGER.error("Test {} Failed due to: {}", testContext.getName(), testContext.getThrowable());
		
		//WebDriver driver = (WebDriver) testContext.getTestContext().getAttribute("driver");
		String testClassName = getTestClassName(testContext.getInstanceName());
		String testMethodName = StringUtils.trimToEmpty(testContext.getName());
		String screenShotName = StringUtils.defaultIfEmpty(testMethodName, "default") + "_" + System.currentTimeMillis() + ".png";

		// if (driver != null) {
		// 	takeScreenShot(driver, screenShotName, testClassName);
		// 	String imagePath = String.join(FILE_SEPARATOR, System.getProperty("user.dir"), "test-output", "Screenshots", testClassName, screenShotName);
		// 	LOGGER.debug("Screenshot can be found at: {}", imagePath);
		// }
		// else {
		// 	LOGGER.warn("WebDriver is null, so skipping the screen shots");
		// }
		
		ExtentTestManager.getTest().log(Status.FAIL, testContext.getThrowable());
		ExtentTestManager.getTest().log(Status.FAIL, MarkupHelper.createLabel(testContext.getName()+" FAILED ", ExtentColor.PINK));
	}

	@Override
	public void onTestSkipped(ITestResult testContext) {
		LOGGER.warn(" Test {} on {} got Skipped: {}", testContext.getName(), testContext.getTestClass().getName(), testContext.getThrowable());
		ExtentTestManager.getTest().log(Status.FAIL, testContext.getThrowable());
		ExtentTestManager.getTest().log(Status.SKIP, MarkupHelper.createLabel(testContext.getName()+" SKIPPED ", ExtentColor.ORANGE));
	}

	@Override
	public void onTestStart(ITestResult testContext) {
		LOGGER.info("-------->>>>>> Starting the Test: [{} vs {}] on {}", testContext.getName(), testContext.getMethod().getMethodName(), testContext.getStartMillis());
		ExtentTestManager.logInfo("Starting the Test: " + testContext.getName());
	}

	@Override
	public void onTestSuccess(ITestResult testContext) {
		final long timeTaken = testContext.getEndMillis() - testContext.getStartMillis();
		LOGGER.info("Test {} Succeeded and took {}", testContext.getName(), timeTaken);
		ExtentTestManager.getTest().log(Status.PASS, MarkupHelper.createLabel(testContext.getName()+" PASSED (" + timeTaken + ")", ExtentColor.GREEN));
	}

	private String getTestClassName(final String fullClassName) {
		String[] reqTestClassname = fullClassName.split("\\.");
		int i = reqTestClassname.length - 1;
		LOGGER.debug("Required Test Name : {}", reqTestClassname[i]);
		return StringUtils.trimToEmpty(reqTestClassname[i]);		
	}
	
}