package io.mosip.biosdktest.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.mosip.biosdktest.config.TestResultBuilder;
import io.mosip.biosdktest.dto.SDKResult;
import io.mosip.kernel.biometrics.constant.BiometricFunction;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.QualityCheck;
import io.mosip.kernel.biometrics.model.QualityScore;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.kernel.cbeffutil.impl.CbeffImpl;
import io.mosip.kernel.core.cbeffutil.entity.BIR;
import io.mosip.kernel.core.cbeffutil.spi.CbeffUtil;
import io.mosip.kernel.core.exception.ExceptionUtils;

/**
 * @author Manoj SP
 *
 */
@Component
public class QualityCheckTest {

	private static final String QUALITY_CHECK_THRESHOLD_VALUE = ".qualitycheck.threshold.value";

	@Autowired
	private Environment env;

	@Autowired
	private TestResultBuilder builder;

	@Autowired
	private BioTestHelper helper;

	private CbeffUtil cbeffReader = new CbeffImpl();

	public void qualityCheckSuccess(String testCaseName, List<String> modalities, String probeFileName) {
		Map<String, SDKResult> results = new HashMap<>();

		List<String> expected = modalities.stream().map(modality -> modality + " -> status : 200 & qualityScore >= "
				+ env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE)).collect(Collectors.toList());

		try {
			Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

			if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
					BiometricFunction.QUALITY_CHECK)) {
				List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
						+ helper.getProvider(modality).getClass().getName()
						+ "supports Modality : true, Supports Quality Check : true, getSupportedMethods value contains BiometricType : true")
						.collect(Collectors.toList());
				builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(),
						modalities.stream()
								.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
										+ "supports Modality : " + isModalitySupported.get(modality)
										+ ", Supports Quality Check : " + isMethodSupported.get(modality)
										+ ", getSupportedMethods value contains BiometricType : "
										+ isMethodSupportsModality.get(modality))
								.collect(Collectors.toList()).toString(),
						null);
			} else {
				List<BIR> birs = cbeffReader
						.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(helper.getInputFile(probeFileName)));
				List<io.mosip.kernel.biometrics.entities.BIR> bioRecordSegments = birs.stream()
						.map(bir -> helper.convertToBiometricRecordBIR(bir)).collect(Collectors.toList());

				if (!modalities.isEmpty()) {
					modalities.forEach(modality -> {
						SDKResult sdkResult = new SDKResult();
						results.put(modality, sdkResult.setModality(modality));
					});
				} else {
					birs.forEach(bir -> {
						SDKResult sdkResult = new SDKResult();
						results.put(bir.getBdbInfo().getType().get(0).value(),
								sdkResult.setModality(bir.getBdbInfo().getType().get(0).value()));
					});
				}

				BiometricRecord biometricRecord = new BiometricRecord();
				biometricRecord.setSegments(bioRecordSegments);

				List<BiometricType> bioTypeList = modalities.stream().map(bioType -> helper.getBiometricType(bioType))
						.collect(Collectors.toList());

				qualityCheck(results, biometricRecord, bioTypeList);

				List<String> actual = results.values().stream()
						.map(sdkResult -> sdkResult.getModality() + " -> "
								+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
										: "status : " + sdkResult.getStatusCode() + " & qualityScore : "
												+ sdkResult.getScore()))
						.collect(Collectors.toList());

				builder.build(testCaseName, modalities.toString(), "", expected.toString(), actual.toString(), results
						.values().stream()
						.allMatch(sdkResult -> sdkResult.isStatus() && sdkResult.getStatusCode() >= 200
								&& sdkResult.getStatusCode() <= 299 && sdkResult.getScore() >= env.getProperty(
										sdkResult.getModality() + QUALITY_CHECK_THRESHOLD_VALUE, Float.class)));
			}
		} catch (Exception e) {
			e.printStackTrace();
			builder.build(testCaseName, modalities.toString(), "", expected.toString(), ExceptionUtils.getStackTrace(e),
					false);
		}
	}

	public void qualityCheckFail(String testCaseName, List<String> modalities, String probeFileName) {
		Map<String, SDKResult> results = new HashMap<>();

		List<String> expected = modalities.stream().map(modality -> modality + " -> status : 200 & qualityScore <= "
				+ env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE)).collect(Collectors.toList());

		try {
			Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

			if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
					BiometricFunction.QUALITY_CHECK)) {
				List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
						+ helper.getProvider(modality).getClass().getName()
						+ "supports Modality : true, Supports Quality Check : true, getSupportedMethods value contains BiometricType : true")
						.collect(Collectors.toList());
				builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(),
						modalities.stream()
								.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
										+ "supports Modality : " + isModalitySupported.get(modality)
										+ ", Supports Quality Check : " + isMethodSupported.get(modality)
										+ ", getSupportedMethods value contains BiometricType : "
										+ isMethodSupportsModality.get(modality))
								.collect(Collectors.toList()).toString(),
						null);
			} else {
				List<BIR> birs = cbeffReader
						.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(helper.getInputFile(probeFileName)));
				List<io.mosip.kernel.biometrics.entities.BIR> bioRecordSegments = birs.stream()
						.map(bir -> helper.convertToBiometricRecordBIR(bir)).collect(Collectors.toList());

				if (!modalities.isEmpty()) {
					modalities.forEach(modality -> {
						SDKResult sdkResult = new SDKResult();
						results.put(modality, sdkResult.setModality(modality));
					});
				} else {
					birs.forEach(bir -> {
						SDKResult sdkResult = new SDKResult();
						results.put(bir.getBdbInfo().getType().get(0).value(),
								sdkResult.setModality(bir.getBdbInfo().getType().get(0).value()));
					});
				}

				BiometricRecord biometricRecord = new BiometricRecord();
				biometricRecord.setSegments(bioRecordSegments);

				List<BiometricType> bioTypeList = modalities.stream().map(bioType -> helper.getBiometricType(bioType))
						.collect(Collectors.toList());

				qualityCheck(results, biometricRecord, bioTypeList);

				List<String> actual = results.values().stream()
						.map(sdkResult -> sdkResult.getModality() + " -> "
								+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
										: "status : " + sdkResult.getStatusCode() + " & qualityScore : "
												+ sdkResult.getScore()))
						.collect(Collectors.toList());

				builder.build(testCaseName, modalities.toString(), "", expected.toString(), actual.toString(), results
						.values().stream().filter(sdkResult -> modalities.contains(sdkResult.getModality()))
						.allMatch(sdkResult -> sdkResult.isStatus() && sdkResult.getStatusCode() >= 200
								&& sdkResult.getStatusCode() <= 299 && sdkResult.getScore() <= env.getProperty(
										sdkResult.getModality() + QUALITY_CHECK_THRESHOLD_VALUE, Float.class)));
			}
		} catch (Exception e) {
			e.printStackTrace();
			builder.build(testCaseName, modalities.toString(), "", expected.toString(), ExceptionUtils.getStackTrace(e),
					false);
		}
	}

	public void qualityCheckInvalidData(String testCaseName, List<String> modalities, String probeFileName) {
		Map<String, SDKResult> results = new HashMap<>();

		List<String> expected = modalities.stream().map(modality -> modality + " -> status : 401 or 403")
				.collect(Collectors.toList());

		try {
			Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

			if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
					BiometricFunction.QUALITY_CHECK)) {
				List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
						+ helper.getProvider(modality).getClass().getName()
						+ "supports Modality : true, Supports Quality Check : true, getSupportedMethods value contains BiometricType : true")
						.collect(Collectors.toList());
				builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(),
						modalities.stream()
								.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
										+ "supports Modality : " + isModalitySupported.get(modality)
										+ ", Supports Quality Check : " + isMethodSupported.get(modality)
										+ ", getSupportedMethods value contains BiometricType : "
										+ isMethodSupportsModality.get(modality))
								.collect(Collectors.toList()).toString(),
						null);
			} else {
				List<BIR> birs = cbeffReader
						.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(helper.getInputFile(probeFileName)));
				List<io.mosip.kernel.biometrics.entities.BIR> bioRecordSegments = birs.stream()
						.map(bir -> helper.convertToBiometricRecordBIR(bir)).collect(Collectors.toList());

				if (!modalities.isEmpty()) {
					modalities.forEach(modality -> {
						SDKResult sdkResult = new SDKResult();
						results.put(modality, sdkResult.setModality(modality));
					});
				} else {
					birs.forEach(bir -> {
						SDKResult sdkResult = new SDKResult();
						results.put(bir.getBdbInfo().getType().get(0).value(),
								sdkResult.setModality(bir.getBdbInfo().getType().get(0).value()));
					});
				}

				BiometricRecord biometricRecord = new BiometricRecord();
				biometricRecord.setSegments(bioRecordSegments);

				List<BiometricType> bioTypeList = modalities.stream().map(bioType -> helper.getBiometricType(bioType))
						.collect(Collectors.toList());

				qualityCheck(results, biometricRecord, bioTypeList);

				List<String> actual = results.values().stream()
						.map(sdkResult -> sdkResult.getModality() + " -> "
								+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
										: "status : " + sdkResult.getStatusCode()))
						.collect(Collectors.toList());

				builder.build(testCaseName, modalities.toString(), "", expected.toString(), actual.toString(),
						results.values().stream().filter(sdkResult -> modalities.contains(sdkResult.getModality()))
								.allMatch(sdkResult -> sdkResult.isStatus()
										&& (sdkResult.getStatusCode() == 401 || sdkResult.getStatusCode() == 403)));
			}
		} catch (Exception e) {
			e.printStackTrace();
			builder.build(testCaseName, modalities.toString(), "", expected.toString(), ExceptionUtils.getStackTrace(e),
					false);
		}
	}

	public void qualityCheckNoInputData(String testCaseName, List<String> modalities, String probeFileName) {
		Map<String, SDKResult> results = new HashMap<>();

		List<String> expected = modalities.stream().map(modality -> modality + " -> status : 402 or 403")
				.collect(Collectors.toList());

		try {
			Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

			if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
					BiometricFunction.QUALITY_CHECK)) {
				List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
						+ helper.getProvider(modality).getClass().getName()
						+ "supports Modality : true, Supports Quality Check : true, getSupportedMethods value contains BiometricType : true")
						.collect(Collectors.toList());
				builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(),
						modalities.stream()
								.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
										+ "supports Modality : " + isModalitySupported.get(modality)
										+ ", Supports Quality Check : " + isMethodSupported.get(modality)
										+ ", getSupportedMethods value contains BiometricType : "
										+ isMethodSupportsModality.get(modality))
								.collect(Collectors.toList()).toString(),
						null);
			} else {

				List<BIR> birs = cbeffReader
						.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(helper.getInputFile(probeFileName)));
				List<io.mosip.kernel.biometrics.entities.BIR> bioRecordSegments = birs.stream()
						.map(bir -> helper.convertToBiometricRecordBIR(bir)).collect(Collectors.toList());

				if (!modalities.isEmpty()) {
					modalities.forEach(modality -> {
						SDKResult sdkResult = new SDKResult();
						results.put(modality, sdkResult.setModality(modality));
					});
				} else {
					birs.forEach(bir -> {
						SDKResult sdkResult = new SDKResult();
						results.put(bir.getBdbInfo().getType().get(0).value(),
								sdkResult.setModality(bir.getBdbInfo().getType().get(0).value()));
					});
				}

				BiometricRecord biometricRecord = new BiometricRecord();
				biometricRecord.setSegments(bioRecordSegments);

				List<BiometricType> bioTypeList = modalities.stream().map(bioType -> helper.getBiometricType(bioType))
						.collect(Collectors.toList());

				qualityCheck(results, biometricRecord, bioTypeList);

				List<String> actual = results.values().stream()
						.map(sdkResult -> sdkResult.getModality() + " -> "
								+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
										: "status : " + sdkResult.getStatusCode()))
						.collect(Collectors.toList());

				builder.build(testCaseName, modalities.toString(), "", expected.toString(), actual.toString(),
						results.values().stream().filter(sdkResult -> modalities.contains(sdkResult.getModality()))
								.allMatch(sdkResult -> sdkResult.isStatus()
										&& (sdkResult.getStatusCode() == 402 || sdkResult.getStatusCode() == 403)));
			}
		} catch (Exception e) {
			e.printStackTrace();
			builder.build(testCaseName, modalities.toString(), "", expected.toString(), ExceptionUtils.getStackTrace(e),
					false);
		}
	}

	private void qualityCheck(Map<String, SDKResult> results, BiometricRecord biometricRecord,
			List<BiometricType> bioTypeList) {
		Map<String, SDKResult> qualitySDKResults = new HashMap<>();
		for (SDKResult result : results.values()) {
			if (!qualitySDKResults.containsKey(result.getModality()) && Objects.isNull(result.getStatusCode())
					&& Objects.isNull(result.getScore()) && Objects.isNull(result.getErrorStackTrace())) {
				try {
					Response<QualityCheck> qualityResult = helper.getProvider(result.getModality())
							.checkQuality(biometricRecord, bioTypeList, null);
					result.setStatusCode(qualityResult.getStatusCode()).setStatus(true);
					if (qualityResult.getStatusCode() >= 200 && qualityResult.getStatusCode() <= 299) {
						Set<Entry<BiometricType, QualityScore>> entrySet = qualityResult.getResponse().getScores()
								.entrySet();
						for (Entry<BiometricType, QualityScore> entry : entrySet) {
							qualitySDKResults.put(entry.getKey().value().toLowerCase(),
									new SDKResult(entry.getKey().value().toLowerCase(), qualityResult.getStatusCode(),
											entry.getValue().getScore(), true, null, false, null));
						}
					} else {
						qualitySDKResults.put(result.getModality(), new SDKResult(result.getModality(),
								qualityResult.getStatusCode(), null, true, null, false, null));

					}
				} catch (Exception e) {
					e.printStackTrace();
					result.setErrorStackTrace(ExceptionUtils.getStackTrace(e));
					result.setStatus(false);
				}
			}
		}
		results.putAll(qualitySDKResults);
	}

}
