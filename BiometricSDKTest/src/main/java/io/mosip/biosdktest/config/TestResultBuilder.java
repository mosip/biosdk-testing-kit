package io.mosip.biosdktest.config;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.biosdktest.dto.TestResult;
import io.mosip.biosdktest.dto.TestResults;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.StringUtils;

/**
 * @author Manoj SP
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TestResultBuilder {

	public static TestResults results = new TestResults();

	@Autowired
	private ObjectMapper mapper;

	public void build(String testCaseName, String modality, String subType, String expected, String actual,
			Boolean result) {
		TestResult testResult = new TestResult();
		testResult.setTestCaseName(testCaseName);
		if (StringUtils.isNotBlank(subType)) {
			testResult.setMetaData("Test results for biometric type " + modality + " with subType " + subType);
		} else {
			testResult.setMetaData("Test results for biometric type " + modality);
		}
		testResult.setExpectedResponse(expected);
		testResult.setActualResponse(actual);
		testResult.setTestEndTime(DateUtils.getUTCCurrentDateTime());
		if (Objects.isNull(result)) {
			testResult.setResult("SKIPPED");
			results.removeSkipped(testResult);
			results.addSkipped(testResult);
		} else if (result) {
			testResult.setResult("PASSED");
			results.addPassed(testResult);
			results.removeSkipped(testResult);
		} else {
			testResult.setResult("FAILED");
			results.addFailed(testResult);
			results.removeSkipped(testResult);
		}
		results.setEndTime(DateUtils.getUTCCurrentDateTime());
		try {
			FileUtils.write(new File("test-results/" + TestResultTemplateWriter.getReportFileName()  + ".js"),
					"var results = " + JSONObject.quote(mapper.writeValueAsString(results)), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
