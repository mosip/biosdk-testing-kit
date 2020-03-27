package io.mosip.biosdktest.dto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.mosip.kernel.core.util.DateUtils;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

/**
 * @author Manoj SP
 *
 */
@Data
public class TestResult {

	private String testCaseName;

	private String metaData;

	private String testStartTime = DateUtils.getCurrentDateTimeString();

	private String testEndTime;

	private String expectedResponse;

	private String actualResponse;

	private String result;

	@Getter(AccessLevel.NONE)
	private String testExecutionTime;

	public TestResult(String testCaseName, String result) {
		this.testCaseName = testCaseName;
		this.result = result;
	}

	public TestResult() {
	}

	public String getTestExecutionTime() {
		if (Objects.nonNull(getTestEndTime())) {
			LocalDateTime startTime = LocalDateTime.parse(getTestStartTime(),
					DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ"));
			LocalDateTime endTime = LocalDateTime.parse(getTestEndTime(),
					DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ"));
			Duration duration = Duration.between(startTime, endTime);
			long millis = duration.toMillis();

			return String.format("%02d:%02d:%02d.%02d", duration.toHours(), duration.toMinutes(),
					TimeUnit.MILLISECONDS.toSeconds(millis), duration.toMillis());
		}
		return null;
	}
}
