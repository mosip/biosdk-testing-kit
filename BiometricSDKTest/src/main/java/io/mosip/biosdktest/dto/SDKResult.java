package io.mosip.biosdktest.dto;

import io.mosip.kernel.biometrics.constant.Match;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class SDKResult {
	private String modality;
	private Integer statusCode;
	private Float score;
	private boolean status;
	private Match matchDecision;
	private boolean isExtracted;
	private String errorStackTrace;

	public String getModality() {
		return modality;
	}

	public SDKResult setModality(String modality) {
		this.modality = modality;
		return this;
	}

	public Integer getStatusCode() {
		return statusCode;
	}

	public SDKResult setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
		return this;
	}

	public Float getScore() {
		return score;
	}

	public SDKResult setScore(Float score) {
		this.score = score;
		return this;
	}

	public boolean isStatus() {
		return status;
	}

	public SDKResult setStatus(boolean status) {
		this.status = status;
		return this;
	}

	public String getErrorStackTrace() {
		return errorStackTrace;
	}

	public SDKResult setErrorStackTrace(String errorStackTrace) {
		this.errorStackTrace = errorStackTrace;
		return this;
	}

	public Match getMatchDecision() {
		return matchDecision;
	}

	public SDKResult setMatchDecision(Match matchDecision) {
		this.matchDecision = matchDecision;
		return this;
	}

	public boolean isExtracted() {
		return isExtracted;
	}

	public SDKResult setExtracted(boolean isExtracted) {
		this.isExtracted = isExtracted;
		return this;
	}

	@Override
	public String toString() {
		return "SDKResult [modality=" + modality + ", statusCode=" + statusCode + ", score=" + score + ", status="
				+ status + ", matchDecision=" + matchDecision + ", isExtracted=" + isExtracted + ", errorStackTrace="
				+ errorStackTrace + "]";
	}

}
