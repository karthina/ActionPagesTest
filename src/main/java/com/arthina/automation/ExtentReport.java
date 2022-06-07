package com.arthina.automation;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aventstack.extentreports.AnalysisStrategy;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.kstruct.gethostname4j.Hostname;

public class ExtentReport {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtentReport.class);
	private static final String FILE_SEPERATOR = System.getProperty("file.separator");
    private static String DOCUMENT_TITLE = "Arthina Automation Report";
    private static String REPORT_NAME = "Test Execution Report - Arthina Merchant Solutions";
//    private static String REPORT_FILE_NAME = "Test-Execution-Report.html";
    
	private static ExtentReports extent;
   
    public static ExtentReports getInstance(final String reportName) {
        if (extent == null)
            createInstance(reportName);
        return extent;
    }
 
    //Create an extent report instance
    public static ExtentReports createInstance(final String reportName) {
        //String fileName = String.join(FILE_SEPERATOR, getReportPath(), reportName + ".html");
        final String fileName = String.join(FILE_SEPERATOR, getReportPath(), "index.html");
       
        ExtentSparkReporter htmlReporter = new ExtentSparkReporter(fileName);
        
        htmlReporter.config().setTheme(Theme.STANDARD);
        htmlReporter.config().setDocumentTitle(DOCUMENT_TITLE + " :: " + REPORT_NAME);
        htmlReporter.config().setEncoding("UTF-8");
        htmlReporter.config().setReportName(REPORT_NAME);
        htmlReporter.config().setTimeStampFormat("EEEE, MMMM dd, yyyy, hh:mm a '('zzz')'");
 
        extent = new ExtentReports();
        //Set environment details
        extent.setSystemInfo("REPORT_DIR", fileName);
        extent.setSystemInfo("Browser", "Chrome");
		extent.setSystemInfo("OS", System.getProperty("os.name"));
		extent.setSystemInfo("Owner", "Arthina Kumar");
		extent.setSystemInfo("HostName", Hostname.getHostname());
		extent.setAnalysisStrategy(AnalysisStrategy.TEST);
        extent.attachReporter(htmlReporter);
		
        return extent;
    }
     
    //Create the report path
    private static String getReportPath() {
    	//String path = null;
    	String workingDir = System.getProperty("user.dir");
    	LOGGER.debug("Working Directory: {}", workingDir);
//        if (System.getProperty("os.name").toLowerCase().contains("win")) {
//        	path = workingDir + "\\test-output\\ExecutionReports\\";
//        	System.out.println("Formed Report Path(Win): " + path);
//        }
//        else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
//        	path = workingDir + "/test-output/ExecutionReports/";
//        	System.out.println("Formed Report Path(Mac): " + path);
//        }

        final String reportBasePath = String.join(FILE_SEPERATOR, workingDir, "target", "ExecutionReports");

    	File testDirectory = new File(reportBasePath);
        if (!testDirectory.exists()) {
        	if (testDirectory.mkdirs()) {
        		LOGGER.info("Directory: {} is created!", reportBasePath);
                return reportBasePath;
            } else {
            	LOGGER.error("Failed to create directory: {}", reportBasePath);
                return System.getProperty("user.dir");
            }
        } else {
        	LOGGER.debug("Directory already exists: {}", reportBasePath);
        }
		return reportBasePath;
    }    
}