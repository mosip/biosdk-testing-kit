package io.mosip.biosdktest.runner;

import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.test.util.ReflectionTestUtils;
import org.vishag.async.AsyncTask;

import io.mosip.biosdktest.config.TestResultBuilder;
import io.mosip.biosdktest.dto.TestResult;
import io.mosip.biosdktest.test.BioSDKTest;
import io.mosip.kernel.core.exception.ExceptionUtils;

/**
 * The Class BioSDKTestRunner.
 * 
 * @author Manoj SP
 */
@Component
public class BioSDKTestRunner implements CommandLineRunner {

	private static final String THREADPOOL_SIZE = "threadpool.size";

	private static final String QUALITY_CHECK_THRESHOLD_VALUE = "qualitycheck.threshold.value";

	private static final String MATCH_THRESHOLD_VALUE = "match.threshold.value";

	@Autowired
	private BioSDKTest test;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private Environment env;

	@Autowired
	private TestResultBuilder logger;

	@Override
	public void run(String... args) throws Exception {
		File testCaseFile = resourceLoader.getResource("file:" + args[0]).getFile();
		List<String> testsToExecute = FileUtils.readLines(testCaseFile, "UTF-8");
		List<Runnable> testCases = new ArrayList<>();
		testsToExecute.stream().filter(testCase -> StringUtils.isNotBlank(testCase) && !testCase.startsWith("#"))
				.forEach(testCase -> TestResultBuilder.results.addTestCount());
		Map<String, TestResult> allTestCases = testsToExecute.stream()
				.filter(testCase -> StringUtils.isNotBlank(testCase) && !testCase.startsWith("#"))
				.map(testCase -> new TestResult(testCase.split("\\|")[0], "SKIPPED"))
				.collect(Collectors.toMap(TestResult::getTestCaseName, testResult -> testResult));
		TestResultBuilder.results.setSkippedTestCases(new HashSetValuedHashMap<>(allTestCases));
		for (String testCase : testsToExecute) {
			if (StringUtils.isNotBlank(testCase) && !testCase.startsWith("#")
					&& validateInputs(testCase.split("\\|")[0], testCase.split("\\|")[1], testCase.split("\\|")[2],
							Arrays.asList(testCase.split("\\|")[3].split(",")))
					&& checkForProperty(testCase.split("\\|")[0], testCase.split("\\|")[1].toLowerCase())) {
				String modality = testCase.split("\\|")[1].toLowerCase();
				String testMethod = testCase.split("\\|")[2];
				List<String> inputs = Arrays.asList(testCase.split("\\|")[3].split(","));
				List<String> arguments = new ArrayList<>();
				arguments.add(testCase.split("\\|")[0]);
				arguments.add(modality);
				arguments.addAll(inputs);
				testCases.add(() -> executeTest(testCase.split("\\|")[0], testMethod, inputs, arguments));
			}
		}
		executeTestInThreadPool(testCases);
	}

	private void executeTestInThreadPool(List<Runnable> testCases) {
		ExecutorService threadPool = Executors.newFixedThreadPool(env.getProperty(THREADPOOL_SIZE, Integer.class));
		AsyncTask.of(threadPool).submitTasksAndWait(testCases.toArray(new Runnable[] {}));
		threadPool.shutdown();
	}

	private void executeTest(String testCaseName, String testMethod, List<String> inputs, List<String> arguments) {
		try {
			ReflectionTestUtils.invokeMethod(test, testMethod, arguments.toArray());
		} catch (UndeclaredThrowableException e) {
			e.printStackTrace();
			if (e.getCause().getClass().isAssignableFrom(NoSuchMethodError.class)) {
				logger.build(testCaseName, arguments.get(0), null, null,
						"Exception: Test method " + testMethod + " not found with input size: " + inputs.size(), null);
			} else {
				logger.build(testCaseName, arguments.get(0), null, null, ExceptionUtils.getStackTrace(e.getCause()),
						null);
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
			logger.build(testCaseName, arguments.get(0), null, null,
					"Exception: Test method " + testMethod + " not found with input size: " + inputs.size(), null);
		}
	}

	private boolean validateInputs(String testCaseName, String modality, String testMethod, List<String> inputs) {
		if (StringUtils.isBlank(testCaseName)) {
			logger.build(testCaseName, modality, null, null, "Exception: Test Case Name should not be empty", null);
			return false;
		}
		if (StringUtils.isBlank(modality)) {
			logger.build(testCaseName, modality, null, null, "Exception: Biometric Type should not be empty", null);
			return false;
		}
		if (StringUtils.isBlank(testMethod)) {
			logger.build(testCaseName, modality, null, null, "Exception: Test Function should not be empty", null);
			return false;
		}
		if (inputs.isEmpty()) {
			logger.build(testCaseName, modality, null, null, "Exception: Input File Name should not be empty", null);
			return false;
		}
		return true;
	}

	private boolean checkForProperty(String testCaseName, String modality) {
		String qualityCheckKey = modality + "." + QUALITY_CHECK_THRESHOLD_VALUE;
		String matchKey = modality + "." + MATCH_THRESHOLD_VALUE;
		if (StringUtils.isAllBlank(env.getProperty(THREADPOOL_SIZE))) {
			logger.build(testCaseName, modality, null, null, THREADPOOL_SIZE + " property not configured", null);
			return false;
		}
		if (StringUtils.isAllBlank(env.getProperty(qualityCheckKey))) {
			logger.build(testCaseName, modality, null, null, qualityCheckKey + " property not configured", null);
			return false;
		}

		if (StringUtils.isAllBlank(env.getProperty(matchKey))) {
			logger.build(testCaseName, modality, null, null, matchKey + " property not configured", null);
			return false;
		}
		return true;
	}
}
