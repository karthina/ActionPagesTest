package com.arthina.automation;

import com.arthina.automation.reporter.ExtentTestManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.*;

import java.io.File;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TestBase {
    private WebDriver driver;
    private boolean isWebTesting = Boolean.TRUE;
    private String browserType;
    protected String downloadDir;
    private boolean headlessOption = Boolean.TRUE;
    private boolean imagesAsBase64Option = Boolean.TRUE;

    public WebDriver getDriver() {
        return this.driver;
    }

    @Parameters({"isWebAutomation", "browser", "isHeadless", "imageAsBase64", "downloadDir"})
    @BeforeSuite
    public void initSuite(ITestContext testContext,
                          @Optional("true") String isWebAutomation,
                          @Optional("Chrome") final String browser,
                          @Optional("true") final String headlessOption,
                          @Optional("true") final String imageAsBase64,
                          @Optional("./src/test/resources/ToDownload") final String downloadDir) {
        this.isWebTesting = Boolean.valueOf(isWebAutomation);
        this.headlessOption = Boolean.valueOf(headlessOption);
        this.imagesAsBase64Option = Boolean.valueOf(imageAsBase64);
        this.browserType = browser;
        final String otherHeader = isWebTesting ? "-" + browserType : "";

        this.downloadDir = downloadDir;
        if (isWebTesting) {
            try {
                setDriver(this.browserType, this.headlessOption, downloadDir);
                testContext.setAttribute("driver", this.driver);
                testContext.setAttribute("imageAsBase64", imagesAsBase64Option);
            } catch (Exception e) {
                log.error("Error: {}", e.getMessage(), e);
            }
        }

        ExtentTestManager.startReport(testContext.getSuite().getName() + otherHeader);
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

    @AfterSuite
    public void tearDown() {
        if (isWebTesting) {
            log.info("Web Driver getting cleaned up..");
            this.driver.quit();
            log.info("Web Driver cleaned up successfully.");
        }
    }


    private void setDriver(String browserType, final boolean headlessOption, String downloadDir) {
        switch (browserType) {
            case "Chrome":
                driver = initChromeDriver(headlessOption, downloadDir);
                break;
            case "Firefox":
                driver = initFirefoxDriver(headlessOption);
                break;
            case "Safari":
                driver = initSafariDriver();
                break;
            case "InternetExplorer":
                WebDriverManager.iedriver().setup();
                driver = new InternetExplorerDriver();
                break;
            case "Opera":
                WebDriverManager.operadriver().setup();
                driver = new OperaDriver();
                //TO-DO
                break;
            case "Edge":
                WebDriverManager.edgedriver().setup();
                driver = new EdgeDriver();
                //TO-DO
                break;
            default:
                log.info("given browser:{} is invalid, Launching Chrome as browser of choice..", browserType);
                driver = initChromeDriver(headlessOption, downloadDir);
        }
    }

    private WebDriver initChromeDriver(final boolean headlessOption, final String downloadDir) {
        ChromeOptions options =  new ChromeOptions();
        options.setHeadless(headlessOption);
        if (StringUtils.isNotBlank(downloadDir)) {
            log.info("Launching google chrome with download Dir: {}", downloadDir);
            File folder = new File(downloadDir);
            Map<String, Object> prefs = new HashMap<String, Object>();
            prefs.put("profile.default_content_settings.popups", 0);
            prefs.put("download.default_directory", folder.getAbsolutePath());
            options.setExperimentalOption("prefs", prefs);
        }

        log.info("Launching google chrome with new profile..");
        WebDriverManager.chromedriver().setup();

        WebDriver driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        return driver;
    }

    private WebDriver initFirefoxDriver(final boolean headlessOption) {
        log.info("Launching Firefox browser..");
        FirefoxOptions options = new FirefoxOptions();
        options.setHeadless(headlessOption);
        options.setAcceptInsecureCerts(true);
        options.setCapability("marionette", true);
        WebDriverManager.firefoxdriver().setup();
        WebDriver driver = new FirefoxDriver(options);
        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        return driver;
    }

    private WebDriver initSafariDriver() {
        log.info("Launching apple safari with new profile..");

        WebDriver driver = new SafariDriver();
        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        return driver;
    }

    protected void assertContains(final String actualObj, final String expectedObj) {
        ExtentTestManager.logInfo("Validating the Actual: " + actualObj + " contains Expected: " + expectedObj);
        Assert.assertTrue(actualObj.contains(expectedObj));
    }

    protected void assertContainsAll(final String actualObj, final String... expectedObj) {
        ExtentTestManager.logInfo("Validating the Actual: " + actualObj + " contains Expected: " + expectedObj);
        for(int i=0; i< expectedObj.length; i++) {
            Assert.assertTrue(actualObj.contains(expectedObj[i]), expectedObj[i]);
        }
    }

    protected void assertEquals(final Object actualObj, final Object expectedObj) {
        try {
            Assert.assertEquals(actualObj, expectedObj);
            ExtentTestManager.logInfo("Validated the Actual: " + actualObj + " equals Expected: " + expectedObj);
        }
        catch(AssertionError ae) {
            ExtentTestManager.logFail("Failed on Validating [Actual: " + actualObj + " equals Expected: " + expectedObj + "], Failure: " + ae.getMessage());
            throw ae;
        }
    }

    protected void assertNotNull(Object actualValue) {
        ExtentTestManager.logInfo("Validating the " + actualValue + "is NOT NULL");
        Assert.assertNotNull(actualValue);
    }
}
