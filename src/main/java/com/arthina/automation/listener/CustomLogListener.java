package com.arthina.automation.listener;



import com.arthina.automation.reporter.ExtentTestManager;
import com.aventstack.extentreports.MediaEntityBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

@Slf4j
public class CustomLogListener implements ITestListener {
	private static final String FILE_SEPARATOR = System.getProperty("file.separator");
	
	@Override
	public void onFinish(ITestContext testContext) {
		log.info("-------->>>>>> Test Summary <<<<<<<----------");
		testContext.getPassedTests().getAllResults().forEach(result -> {
			log.info("==========> {} PASSED", result.getName());
		});
		log.info("-------->>>>>> ---------------------------- <<<<<<<----------");
		testContext.getFailedTests().getAllResults().forEach(result -> {
			log.info("==========> {} FAILED", result.getName());
		});
		log.info("-------->>>>>> ---------------------------- <<<<<<<----------");
		testContext.getSkippedTests().getAllResults().forEach(result -> {
			log.info("==========> {} SKIPPED", result.getName());
		});
		log.info("-------->>>>>> ---------------------------- <<<<<<<----------");		
	}

	@Override
	public void onStart(ITestContext testContext) {
		log.info("-------->>>>>> Starting the Tests...: {} on {}", testContext.getName(), testContext.getStartDate());
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult testContext) {
		log.info("*** Test failed but within percentage % {}", testContext.getMethod().getMethodName());
	}

	@Override
	public void onTestFailure(ITestResult testContext) {
		log.error("Test {} Failed due to: {}", testContext.getName(), testContext.getThrowable());
		
		WebDriver driver = (WebDriver) testContext.getTestContext().getAttribute("driver");
		final boolean imageAsBase64 = (boolean) testContext.getTestContext().getAttribute("imageAsBase64");
		String testClassName = getTestClassName(testContext.getInstanceName());
		String testMethodName = StringUtils.trimToEmpty(testContext.getName());
		String screenShotName = StringUtils.defaultIfEmpty(testMethodName, "default") + "_" + System.currentTimeMillis() + ".png";

		 if (driver != null) {
			 if (imageAsBase64)
		 		takeScreenShot(driver, screenShotName, testClassName);
			 else {
				bindScreenShotToReport(driver, screenShotName, testClassName);
				final String imagePath = String.join(FILE_SEPARATOR, System.getProperty("user.dir"), "target", "ExecutionReports", "Screenshots", testClassName, screenShotName);
				log.debug("Screenshot can be found at: {}", imagePath);
			 }
		 }
		 else {
		 	log.warn("WebDriver is null, so skipping the screen shots");
		 }
		
		ExtentTestManager.getTest().log(Status.FAIL, testContext.getThrowable());
		ExtentTestManager.getTest().log(Status.FAIL, MarkupHelper.createLabel(testContext.getName()+" FAILED ", ExtentColor.PINK));
	}

	@Override
	public void onTestSkipped(ITestResult testContext) {
		log.warn(" Test {} on {} got Skipped: {}", testContext.getName(), testContext.getTestClass().getName(), testContext.getThrowable());
		ExtentTestManager.getTest().log(Status.FAIL, testContext.getThrowable());
		ExtentTestManager.getTest().log(Status.SKIP, MarkupHelper.createLabel(testContext.getName()+" SKIPPED ", ExtentColor.ORANGE));
	}

	@Override
	public void onTestStart(ITestResult testContext) {
		log.info("-------->>>>>> Starting the Test: [{} vs {}] on {}", testContext.getName(), testContext.getMethod().getMethodName(), testContext.getStartMillis());
		ExtentTestManager.logInfo("Starting the Test: " + testContext.getName());
	}

	@Override
	public void onTestSuccess(ITestResult testContext) {
		final long timeTaken = testContext.getEndMillis() - testContext.getStartMillis();
		log.info("Test {} Succeeded and took {}", testContext.getName(), timeTaken);
		ExtentTestManager.getTest().log(Status.PASS, MarkupHelper.createLabel(testContext.getName()+" PASSED (" + timeTaken + ")", ExtentColor.GREEN));
	}

	private String getTestClassName(final String fullClassName) {
		String[] reqTestClassname = fullClassName.split("\\.");
		int i = reqTestClassname.length - 1;
		log.debug("Required Test Name : {}", reqTestClassname[i]);
		return StringUtils.trimToEmpty(reqTestClassname[i]);		
	}

	private void bindScreenShotToReport(WebDriver driver, final String screenShotName, final String testClassName) {
		log.info("Trying to take screen shot {} for {}", screenShotName, testClassName);
		final String screenshotBase64 = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
		ExtentTestManager.logFailWithScreenShot(testClassName + " failure screenshot", MediaEntityBuilder.createScreenCaptureFromBase64String(screenshotBase64, screenShotName).build());
	}

	private void takeScreenShot(WebDriver driver, final String screenShotName, final String testClassName) {
		log.info("Trying to take screen shot {} for {}", screenShotName, testClassName);
		final String imageBasePath = String.join(FILE_SEPARATOR, System.getProperty("user.dir"), "target", "ExecutionReports", "Screenshots", testClassName);
		final String imageFullName = String.join(FILE_SEPARATOR, imageBasePath, screenShotName);
		try {
			File folder = new File(imageBasePath);
			if (!folder.exists()) {
				log.debug("Screenshot Capture Folder created {}", folder);
				folder.mkdirs();
			}

			File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			File targetFile = new File(imageFullName);
			FileUtils.copyFile(screenshotFile, targetFile);
			log.info("*** Placed failedTestCase screen shot under {} ****", targetFile);
			// attach screenshots to report
			ExtentTestManager.logFailWithScreenShot(testClassName + " failure screenshot", MediaEntityBuilder.createScreenCaptureFromPath(imageFullName).build());
		} catch (FileNotFoundException e) {
			log.error("File not found exception occurred while taking screenshot: {}", e.getMessage(), e);
		} catch (IOException e) {
			log.error("An exception occurred while attaching screenshot to extentReport: {}", e.getMessage(), e);
		} catch (Exception e) {
			log.error("An exception occurred while taking screenshot: {}", e.getMessage(), e);
		}
	}
}