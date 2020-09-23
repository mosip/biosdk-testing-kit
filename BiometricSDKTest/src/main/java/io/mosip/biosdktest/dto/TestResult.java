package io.mosip.biosdktest.dto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonFormat;

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

	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS")
	private LocalDateTime testStartTime = DateUtils.getUTCCurrentDateTime();

	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS")
	private LocalDateTime testEndTime;

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
			Duration duration = Duration.between(getTestStartTime(), getTestEndTime());
			long millis = duration.toMillis();

			return String.format("%02d:%02d:%02d.%02d", duration.toHours(), duration.toMinutes(),
					TimeUnit.MILLISECONDS.toSeconds(millis), duration.toMillis());
		}
		return null;
	}
}
