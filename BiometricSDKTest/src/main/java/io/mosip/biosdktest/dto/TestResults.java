package io.mosip.biosdktest.dto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.mosip.kernel.core.util.DateUtils;

/**
 * @author Manoj SP
 *
 */
public class TestResults {

	private Integer testCaseCount = 0;

	private Integer passedTestCaseCount = 0;

	private Integer failedTestCaseCount = 0;

	private Integer skippedTestCaseCount = 0;

	private String totalExecutionTime;

	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS")
	private LocalDateTime startTime = DateUtils.getUTCCurrentDateTime();

	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS")
	private LocalDateTime endTime;

	private SetValuedMap<String, TestResult> passedTestCases = new HashSetValuedHashMap<>();

	private SetValuedMap<String, TestResult> failedTestCases = new HashSetValuedHashMap<>();

	private SetValuedMap<String, TestResult> skippedTestCases = new HashSetValuedHashMap<>();

	public void addPassed(TestResult result) {
		passedTestCases.put(result.getTestCaseName(), result);
	}

	public void addFailed(TestResult result) {
		failedTestCases.put(result.getTestCaseName(), result);
	}

	public void addSkipped(TestResult result) {
		skippedTestCases.put(result.getTestCaseName(), result);
	}

	public void removeSkipped(TestResult result) {
		skippedTestCases.remove(result.getTestCaseName());
	}

	public void addTestCount() {
		testCaseCount++;
	}

	public Integer getTestCaseCount() {
		return testCaseCount;
	}

	public void setTestCaseCount(Integer testCaseCount) {
		this.testCaseCount = testCaseCount;
	}

	public String getTotalExecutionTime() {
		if (Objects.nonNull(getEndTime())) {
			Duration duration = Duration.between(
					getStartTime(),
					getEndTime());
			long millis = duration.toMillis();

			return String.format("%02d:%02d:%02d.%02d", duration.toHours(), duration.toMinutes(),
					TimeUnit.MILLISECONDS.toSeconds(millis), duration.toMillis());
		}
		return null;
	}

	public void setTotalExecutionTime(String totalExecutionTime) {
		this.totalExecutionTime = totalExecutionTime;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public Collection<TestResult> getPassedTestCases() {
		return passedTestCases.values();
	}

	public void setPassedTestCases(SetValuedMap<String, TestResult> passedTestCases) {
		this.passedTestCases = passedTestCases;
	}

	public Collection<TestResult> getFailedTestCases() {
		return failedTestCases.values();
	}

	public void setFailedTestCases(SetValuedMap<String, TestResult> failedTestCases) {
		this.failedTestCases = failedTestCases;
	}

	public Collection<TestResult> getSkippedTestCases() {
		return skippedTestCases.values();
	}

	public void setSkippedTestCases(SetValuedMap<String, TestResult> skippedTestCases) {
		this.skippedTestCases = skippedTestCases;
	}

	public Integer getPassedTestCaseCount() {
		return passedTestCases.keySet().size();
	}

	public void setPassedTestCaseCount(Integer passedTestCaseCount) {
		this.passedTestCaseCount = passedTestCaseCount;
	}

	public Integer getFailedTestCaseCount() {
		return failedTestCases.keySet().size();
	}

	public void setFailedTestCaseCount(Integer failedTestCaseCount) {
		this.failedTestCaseCount = failedTestCaseCount;
	}

	public Integer getSkippedTestCaseCount() {
		return skippedTestCases.keySet().size();
	}

	public void setSkippedTestCaseCount(Integer skippedTestCaseCount) {
		this.skippedTestCaseCount = skippedTestCaseCount;
	}

}
