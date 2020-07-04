package io.mosip.biosdktest.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.biosdktest.config.TestResultBuilder;
import io.mosip.biosdktest.dto.SDKResult;
import io.mosip.kernel.biometrics.constant.BiometricFunction;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.Match;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.Decision;
import io.mosip.kernel.biometrics.model.MatchDecision;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.kernel.cbeffutil.impl.CbeffImpl;
import io.mosip.kernel.core.cbeffutil.entity.BIR;
import io.mosip.kernel.core.cbeffutil.spi.CbeffUtil;
import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ExceptionUtils;

/**
 * @author Manoj SP
 *
 */
@Component
public class MatchTest {

	@Autowired
	private TestResultBuilder builder;

	@Autowired
	private BioTestHelper helper;

	private CbeffUtil cbeffReader = new CbeffImpl();

	public void matchSuccess(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) {
		Map<String, SDKResult> results = new HashMap<>();

		List<String> expected = modalities.stream()
				.map(modality -> modality + " -> status : 200 & MatchDecision: MATCHED").collect(Collectors.toList());

		try {
			Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

			if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
					BiometricFunction.MATCH)) {
				List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
						+ helper.getProvider(modality).getClass().getName()
						+ "supports Modality : true, Supports Match : true, getSupportedMethods value contains BiometricType : true")
						.collect(Collectors.toList());
				builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(),
						modalities.stream()
								.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
										+ "supports Modality : " + isModalitySupported.get(modality)
										+ ", Supports Match : " + isMethodSupported.get(modality)
										+ ", getSupportedMethods value contains BiometricType : "
										+ isMethodSupportsModality.get(modality))
								.collect(Collectors.toList()).toString(),
						null);
			} else {
				List<BIR> probeBir = cbeffReader
						.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(helper.getInputFile(probeFileName)));
				List<io.mosip.kernel.biometrics.entities.BIR> probeBirbioRecordSegments = probeBir.stream()
						.map(bir -> helper.convertToBiometricRecordBIR(bir)).collect(Collectors.toList());
				List<List<BIR>> galleryBirs = galleryFileNames.stream().map(galleryFileName -> {
					try {
						return cbeffReader.convertBIRTypeToBIR(
								cbeffReader.getBIRDataFromXML(helper.getInputFile(galleryFileName)));
					} catch (Exception e) {
						throw new BaseUncheckedException("", "", e);
					}
				}).collect(Collectors.toList());
				List<List<io.mosip.kernel.biometrics.entities.BIR>> galleryBirbioRecordSegments = galleryBirs
						.stream().map(galleryBir -> galleryBir.stream()
								.map(bir -> helper.convertToBiometricRecordBIR(bir)).collect(Collectors.toList()))
						.collect(Collectors.toList());

				BiometricRecord probeBirBiometricRecord = new BiometricRecord();
				probeBirBiometricRecord.setSegments(probeBirbioRecordSegments);

				BiometricRecord[] galleryBirBiometricRecords = galleryBirbioRecordSegments.stream().map(galleryBir -> {
					BiometricRecord galleryBirBiometricRecord = new BiometricRecord();
					galleryBirBiometricRecord.setSegments(galleryBir);
					return galleryBirBiometricRecord;
				}).collect(Collectors.toList()).toArray(new BiometricRecord[] {});

				if (!modalities.isEmpty()) {
					modalities.forEach(modality -> {
						SDKResult sdkResult = new SDKResult();
						results.put(modality, sdkResult.setModality(modality));
					});
				} else {
					probeBir.forEach(bir -> {
						SDKResult sdkResult = new SDKResult();
						results.put(bir.getBdbInfo().getType().get(0).value(),
								sdkResult.setModality(bir.getBdbInfo().getType().get(0).value()));
					});
				}

				List<BiometricType> bioTypeList = modalities.stream().map(bioType -> helper.getBiometricType(bioType))
						.collect(Collectors.toList());

				match(probeBirBiometricRecord, galleryBirBiometricRecords, results, bioTypeList);

				List<String> actual = results.values().stream()
						.map(sdkResult -> sdkResult.getModality() + " -> "
								+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
										: "status : " + sdkResult.getStatusCode() + " & MatchDecision: "
												+ sdkResult.getMatchDecision()))
						.collect(Collectors.toList());

				builder.build(testCaseName, modalities.toString(), "", expected.toString(), actual.toString(),
						results.values().stream().filter(sdkResult -> modalities.contains(sdkResult.getModality()))
								.allMatch(sdkResult -> sdkResult.isStatus() && sdkResult.getStatusCode() >= 200
										&& sdkResult.getStatusCode() <= 299
										&& sdkResult.getMatchDecision() == Match.MATCHED));

			}
		} catch (Exception e) {
			e.printStackTrace();
			builder.build(testCaseName, modalities.toString(), "", expected.toString(), ExceptionUtils.getStackTrace(e),
					false);
		}
	}

	public void matchFail(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) {
		Map<String, SDKResult> results = new HashMap<>();

		List<String> expected = modalities.stream()
				.map(modality -> modality + " -> status : 200 & MatchDecision: NOT_MATCHED")
				.collect(Collectors.toList());

		try {
			Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

			if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
					BiometricFunction.MATCH)) {
				List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
						+ helper.getProvider(modality).getClass().getName()
						+ "supports Modality : true, Supports Match : true, getSupportedMethods value contains BiometricType : true")
						.collect(Collectors.toList());
				builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(),
						modalities.stream()
								.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
										+ "supports Modality : " + isModalitySupported.get(modality)
										+ ", Supports Match : " + isMethodSupported.get(modality)
										+ ", getSupportedMethods value contains BiometricType : "
										+ isMethodSupportsModality.get(modality))
								.collect(Collectors.toList()).toString(),
						null);
			} else {
				List<BIR> probeBir = cbeffReader
						.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(helper.getInputFile(probeFileName)));
				List<io.mosip.kernel.biometrics.entities.BIR> probeBirbioRecordSegments = probeBir.stream()
						.map(bir -> helper.convertToBiometricRecordBIR(bir)).collect(Collectors.toList());
				List<List<BIR>> galleryBirs = galleryFileNames.stream().map(galleryFileName -> {
					try {
						return cbeffReader.convertBIRTypeToBIR(
								cbeffReader.getBIRDataFromXML(helper.getInputFile(galleryFileName)));
					} catch (Exception e) {
						throw new BaseUncheckedException("", "", e);
					}
				}).collect(Collectors.toList());
				List<List<io.mosip.kernel.biometrics.entities.BIR>> galleryBirbioRecordSegments = galleryBirs
						.stream().map(galleryBir -> galleryBir.stream()
								.map(bir -> helper.convertToBiometricRecordBIR(bir)).collect(Collectors.toList()))
						.collect(Collectors.toList());

				BiometricRecord probeBirBiometricRecord = new BiometricRecord();
				probeBirBiometricRecord.setSegments(probeBirbioRecordSegments);

				BiometricRecord[] galleryBirBiometricRecords = galleryBirbioRecordSegments.stream().map(galleryBir -> {
					BiometricRecord galleryBirBiometricRecord = new BiometricRecord();
					galleryBirBiometricRecord.setSegments(galleryBir);
					return galleryBirBiometricRecord;
				}).collect(Collectors.toList()).toArray(new BiometricRecord[] {});

				if (!modalities.isEmpty()) {
					modalities.forEach(modality -> {
						SDKResult sdkResult = new SDKResult();
						results.put(modality, sdkResult.setModality(modality));
					});
				} else {
					probeBir.forEach(bir -> {
						SDKResult sdkResult = new SDKResult();
						results.put(bir.getBdbInfo().getType().get(0).value(),
								sdkResult.setModality(bir.getBdbInfo().getType().get(0).value()));
					});
				}

				List<BiometricType> bioTypeList = modalities.stream().map(bioType -> helper.getBiometricType(bioType))
						.collect(Collectors.toList());

				match(probeBirBiometricRecord, galleryBirBiometricRecords, results, bioTypeList);

				List<String> actual = results.values().stream()
						.map(sdkResult -> sdkResult.getModality() + " -> "
								+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
										: "status : " + sdkResult.getStatusCode() + " & MatchDecision: "
												+ sdkResult.getMatchDecision()))
						.collect(Collectors.toList());

				builder.build(testCaseName, modalities.toString(), "", expected.toString(), actual.toString(),
						results.values().stream().filter(sdkResult -> modalities.contains(sdkResult.getModality()))
								.allMatch(sdkResult -> sdkResult.isStatus() && sdkResult.getStatusCode() >= 200
										&& sdkResult.getStatusCode() <= 299
										&& sdkResult.getMatchDecision() == Match.NOT_MATCHED));

			}
		} catch (Exception e) {
			e.printStackTrace();
			builder.build(testCaseName, modalities.toString(), "", expected.toString(), ExceptionUtils.getStackTrace(e),
					false);
		}
	}

	public void matchInvalidData(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) {
		Map<String, SDKResult> results = new HashMap<>();

		List<String> expected = modalities.stream().map(modality -> modality + " -> status : 401 or 405")
				.collect(Collectors.toList());

		try {
			Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

			if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
					BiometricFunction.MATCH)) {
				List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
						+ helper.getProvider(modality).getClass().getName()
						+ "supports Modality : true, Supports Match : true, getSupportedMethods value contains BiometricType : true")
						.collect(Collectors.toList());
				builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(),
						modalities.stream()
								.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
										+ "supports Modality : " + isModalitySupported.get(modality)
										+ ", Supports Match : " + isMethodSupported.get(modality)
										+ ", getSupportedMethods value contains BiometricType : "
										+ isMethodSupportsModality.get(modality))
								.collect(Collectors.toList()).toString(),
						null);
			} else {
				List<BIR> probeBir = cbeffReader
						.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(helper.getInputFile(probeFileName)));
				List<io.mosip.kernel.biometrics.entities.BIR> probeBirbioRecordSegments = probeBir.stream()
						.map(bir -> helper.convertToBiometricRecordBIR(bir)).collect(Collectors.toList());
				List<List<BIR>> galleryBirs = galleryFileNames.stream().map(galleryFileName -> {
					try {
						return cbeffReader.convertBIRTypeToBIR(
								cbeffReader.getBIRDataFromXML(helper.getInputFile(galleryFileName)));
					} catch (Exception e) {
						throw new BaseUncheckedException("", "", e);
					}
				}).collect(Collectors.toList());
				List<List<io.mosip.kernel.biometrics.entities.BIR>> galleryBirbioRecordSegments = galleryBirs
						.stream().map(galleryBir -> galleryBir.stream()
								.map(bir -> helper.convertToBiometricRecordBIR(bir)).collect(Collectors.toList()))
						.collect(Collectors.toList());

				BiometricRecord probeBirBiometricRecord = new BiometricRecord();
				probeBirBiometricRecord.setSegments(probeBirbioRecordSegments);

				BiometricRecord[] galleryBirBiometricRecords = galleryBirbioRecordSegments.stream().map(galleryBir -> {
					BiometricRecord galleryBirBiometricRecord = new BiometricRecord();
					galleryBirBiometricRecord.setSegments(galleryBir);
					return galleryBirBiometricRecord;
				}).collect(Collectors.toList()).toArray(new BiometricRecord[] {});

				if (!modalities.isEmpty()) {
					modalities.forEach(modality -> {
						SDKResult sdkResult = new SDKResult();
						results.put(modality, sdkResult.setModality(modality));
					});
				} else {
					probeBir.forEach(bir -> {
						SDKResult sdkResult = new SDKResult();
						results.put(bir.getBdbInfo().getType().get(0).value(),
								sdkResult.setModality(bir.getBdbInfo().getType().get(0).value()));
					});
				}

				List<BiometricType> bioTypeList = modalities.stream().map(bioType -> helper.getBiometricType(bioType))
						.collect(Collectors.toList());

				match(probeBirBiometricRecord, galleryBirBiometricRecords, results, bioTypeList);

				List<String> actual = results.values().stream()
						.map(sdkResult -> sdkResult.getModality() + " -> "
								+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
										: "status : " + sdkResult.getStatusCode()))
						.collect(Collectors.toList());

				builder.build(testCaseName, modalities.toString(), "", expected.toString(), actual.toString(),
						results.values().stream().filter(sdkResult -> modalities.contains(sdkResult.getModality()))
								.allMatch(sdkResult -> sdkResult.isStatus()
										&& (sdkResult.getStatusCode() == 401 || sdkResult.getStatusCode() == 405)));
			}
		} catch (Exception e) {
			e.printStackTrace();
			builder.build(testCaseName, modalities.toString(), "", expected.toString(), ExceptionUtils.getStackTrace(e),
					false);
		}
	}

	public void matchNoInputData(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) {
		Map<String, SDKResult> results = new HashMap<>();

		List<String> expected = modalities.stream().map(modality -> modality + " -> status : 402 or 405")
				.collect(Collectors.toList());

		try {
			Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

			if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
					BiometricFunction.MATCH)) {
				List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
						+ helper.getProvider(modality).getClass().getName()
						+ "supports Modality : true, Supports Match : true, getSupportedMethods value contains BiometricType : true")
						.collect(Collectors.toList());
				builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(),
						modalities.stream()
								.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
										+ "supports Modality : " + isModalitySupported.get(modality)
										+ ", Supports Match : " + isMethodSupported.get(modality)
										+ ", getSupportedMethods value contains BiometricType : "
										+ isMethodSupportsModality.get(modality))
								.collect(Collectors.toList()).toString(),
						false);
			} else {
				List<BIR> probeBir = cbeffReader
						.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(helper.getInputFile(probeFileName)));
				List<io.mosip.kernel.biometrics.entities.BIR> probeBirbioRecordSegments = probeBir.stream()
						.map(bir -> helper.convertToBiometricRecordBIR(bir)).collect(Collectors.toList());
				List<List<BIR>> galleryBirs = galleryFileNames.stream().map(galleryFileName -> {
					try {
						return cbeffReader.convertBIRTypeToBIR(
								cbeffReader.getBIRDataFromXML(helper.getInputFile(galleryFileName)));
					} catch (Exception e) {
						throw new BaseUncheckedException("", "", e);
					}
				}).collect(Collectors.toList());
				List<List<io.mosip.kernel.biometrics.entities.BIR>> galleryBirbioRecordSegments = galleryBirs
						.stream().map(galleryBir -> galleryBir.stream()
								.map(bir -> helper.convertToBiometricRecordBIR(bir)).collect(Collectors.toList()))
						.collect(Collectors.toList());

				BiometricRecord probeBirBiometricRecord = new BiometricRecord();
				probeBirBiometricRecord.setSegments(probeBirbioRecordSegments);

				BiometricRecord[] galleryBirBiometricRecords = galleryBirbioRecordSegments.stream().map(galleryBir -> {
					BiometricRecord galleryBirBiometricRecord = new BiometricRecord();
					galleryBirBiometricRecord.setSegments(galleryBir);
					return galleryBirBiometricRecord;
				}).collect(Collectors.toList()).toArray(new BiometricRecord[] {});

				if (!modalities.isEmpty()) {
					modalities.forEach(modality -> {
						SDKResult sdkResult = new SDKResult();
						results.put(modality, sdkResult.setModality(modality));
					});
				} else {
					probeBir.forEach(bir -> {
						SDKResult sdkResult = new SDKResult();
						results.put(bir.getBdbInfo().getType().get(0).value(),
								sdkResult.setModality(bir.getBdbInfo().getType().get(0).value()));
					});
				}

				List<BiometricType> bioTypeList = modalities.stream().map(bioType -> helper.getBiometricType(bioType))
						.collect(Collectors.toList());

				match(probeBirBiometricRecord, galleryBirBiometricRecords, results, bioTypeList);

				List<String> actual = results.values().stream()
						.map(sdkResult -> sdkResult.getModality() + " -> "
								+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
										: "status : " + sdkResult.getStatusCode()))
						.collect(Collectors.toList());

				builder.build(testCaseName, modalities.toString(), "", expected.toString(), actual.toString(),
						results.values().stream().filter(sdkResult -> modalities.contains(sdkResult.getModality()))
								.allMatch(sdkResult -> sdkResult.isStatus()
										&& (sdkResult.getStatusCode() == 402 || sdkResult.getStatusCode() == 405)));
			}
		} catch (Exception e) {
			e.printStackTrace();
			builder.build(testCaseName, modalities.toString(), "", expected.toString(), ExceptionUtils.getStackTrace(e),
					false);
		}
	}

	private void match(BiometricRecord probeBirBiometricRecord, BiometricRecord[] galleryBirBiometricRecords,
			Map<String, SDKResult> results, List<BiometricType> bioTypeList) {
		Map<String, SDKResult> matchSDKResults = new HashMap<>();
		for (SDKResult result : results.values()) {
			if (!matchSDKResults.containsKey(result.getModality()) && Objects.nonNull(result.getModality())
					&& Objects.isNull(result.getStatusCode()) && Objects.isNull(result.getMatchDecision())
					&& Objects.isNull(result.getErrorStackTrace())) {
				try {
					System.err.println(result.getModality());
					Response<MatchDecision[]> matchResult = helper.getProvider(result.getModality())
							.match(probeBirBiometricRecord, galleryBirBiometricRecords, bioTypeList, null);
					if (matchResult.getStatusCode() >= 200 && matchResult.getStatusCode() <= 299) {
						List<Map<BiometricType, Decision>> matchDecisions = Arrays.asList(matchResult.getResponse())
								.stream().map(matchDecision -> matchDecision.getDecisions())
								.collect(Collectors.toList());
						for (Map<BiometricType, Decision> matchDecision : matchDecisions) {
							for (Entry<BiometricType, Decision> entry : matchDecision.entrySet()) {
								matchSDKResults.put(entry.getKey().value().toLowerCase(),
										new SDKResult(entry.getKey().value().toLowerCase(), matchResult.getStatusCode(),
												null, true, entry.getValue().getMatch(), false, null));
							}
						}
					} else {
						matchSDKResults.put(result.getModality(), new SDKResult(result.getModality(),
								matchResult.getStatusCode(), null, true, null, false, null));
					}
				} catch (Exception e) {
					e.printStackTrace();
					matchSDKResults.put(result.getModality(),
							new SDKResult().setErrorStackTrace(ExceptionUtils.getStackTrace(e)).setStatus(false));
				}
			}
		}
		results.putAll(matchSDKResults);
	}
}
