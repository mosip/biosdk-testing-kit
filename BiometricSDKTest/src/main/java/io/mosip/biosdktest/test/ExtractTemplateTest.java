package io.mosip.biosdktest.test;

import java.util.Arrays;
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
import io.mosip.kernel.biometrics.constant.Match;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.Decision;
import io.mosip.kernel.biometrics.model.MatchDecision;
import io.mosip.kernel.biometrics.model.QualityCheck;
import io.mosip.kernel.biometrics.model.QualityScore;
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
public class ExtractTemplateTest {

	private static final String QUALITY_CHECK_THRESHOLD_VALUE = ".qualitycheck.threshold.value";

	@Autowired
	private Environment env;

	@Autowired
	private TestResultBuilder builder;

	@Autowired
	private BioTestHelper helper;

	private CbeffUtil cbeffReader = new CbeffImpl();

	public void extractTemplateAndCheckQualitySuccess(String testCaseName, List<String> modalities,
			String probeFileName) {
		Map<String, SDKResult> results = new HashMap<>();

		List<String> expected = modalities.stream()
				.map(modality -> modality + " -> status : 200 & qualityScore >= "
						+ env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE)
						+ " BdbInfo, BirInfo, Sb, SbInfo should not be null")
				.collect(Collectors.toList());

		try {
			Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

			if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
					BiometricFunction.EXTRACT)) {
				List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
						+ helper.getProvider(modality).getClass().getName()
						+ "supports Modality : true, Supports extractTemplate : true, getSupportedMethods value contains BiometricType : true")
						.collect(Collectors.toList());
				builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(),
						modalities.stream()
								.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
										+ "supports Modality : " + isModalitySupported.get(modality)
										+ ", Supports extractTemplate : " + isMethodSupported.get(modality)
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

				extractTemplate(results, biometricRecord, null, bioTypeList, false);

				List<String> actual = results.values().stream()
						.map(sdkResult -> sdkResult.getModality() + " -> "
								+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
										: "status : " + sdkResult.getStatusCode() + " & template extracted : "
												+ sdkResult.isExtracted() + "& qualityScore : " + sdkResult.getScore()))
						.collect(Collectors.toList());

				builder.build(testCaseName, modalities.toString(), "", expected.toString(), actual.toString(),
						results.values().stream().filter(sdkResult -> modalities.contains(sdkResult.getModality()))
								.allMatch(
										sdkResult -> sdkResult.isStatus() && sdkResult.isExtracted()
												&& sdkResult.getStatusCode() >= 200 && sdkResult.getStatusCode() <= 299
												&& sdkResult.getScore() >= env.getProperty(
														sdkResult.getModality() + QUALITY_CHECK_THRESHOLD_VALUE,
														Float.class)));

			}
		} catch (Exception e) {
			e.printStackTrace();
			builder.build(testCaseName, modalities.toString(), "", expected.toString(), ExceptionUtils.getStackTrace(e),
					false);
		}
	}

	public void extractTemplateAndCheckQualityFail(String testCaseName, List<String> modalities, String probeFileName) {
		Map<String, SDKResult> results = new HashMap<>();

		List<String> expected = modalities.stream()
				.map(modality -> modality + " -> status : 200 & qualityScore <= "
						+ env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE)
						+ " BdbInfo, BirInfo, Sb, SbInfo should not be null")
				.collect(Collectors.toList());

		try {
			Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

			if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
					BiometricFunction.EXTRACT)) {
				List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
						+ helper.getProvider(modality).getClass().getName()
						+ "supports Modality : true, Supports extractTemplate : true, getSupportedMethods value contains BiometricType : true")
						.collect(Collectors.toList());
				builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(),
						modalities.stream()
								.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
										+ "supports Modality : " + isModalitySupported.get(modality)
										+ ", Supports extractTemplate : " + isMethodSupported.get(modality)
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

				extractTemplate(results, biometricRecord, null, bioTypeList, false);

				List<String> actual = results.values().stream()
						.map(sdkResult -> sdkResult.getModality() + " -> "
								+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
										: "status : " + sdkResult.getStatusCode() + " & template extracted : "
												+ sdkResult.isExtracted() + "& qualityScore : " + sdkResult.getScore()))
						.collect(Collectors.toList());

				builder.build(testCaseName, modalities.toString(), "", expected.toString(), actual.toString(),
						results.values().stream().filter(sdkResult -> modalities.contains(sdkResult.getModality()))
								.allMatch(
										sdkResult -> sdkResult.isStatus() && sdkResult.isExtracted()
												&& sdkResult.getStatusCode() >= 200 && sdkResult.getStatusCode() <= 299
												&& sdkResult.getScore() <= env.getProperty(
														sdkResult.getModality() + QUALITY_CHECK_THRESHOLD_VALUE,
														Float.class)));

			}
		} catch (Exception e) {
			e.printStackTrace();
			builder.build(testCaseName, modalities.toString(), "", expected.toString(), ExceptionUtils.getStackTrace(e),
					false);
		}
	}

	public void extractTemplateInvalidData(String testCaseName, List<String> modalities, String probeFileName) {
		Map<String, SDKResult> results = new HashMap<>();

		List<String> expected = modalities.stream().map(modality -> modality + " -> status : 401 or 403")
				.collect(Collectors.toList());

		try {
			Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

			if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
					BiometricFunction.EXTRACT)) {
				List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
						+ helper.getProvider(modality).getClass().getName()
						+ "supports Modality : true, Supports extractTemplate : true, getSupportedMethods value contains BiometricType : true")
						.collect(Collectors.toList());
				builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(),
						modalities.stream()
								.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
										+ "supports Modality : " + isModalitySupported.get(modality)
										+ ", Supports extractTemplate : " + isMethodSupported.get(modality)
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

				extractTemplate(results, biometricRecord, null, bioTypeList, false);

				List<String> actual = results.values().stream()
						.map(sdkResult -> sdkResult.getModality() + " -> "
								+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
										: "status : " + sdkResult.getStatusCode() + " & template extracted : "
												+ sdkResult.isExtracted()))
						.collect(Collectors.toList());

				builder.build(testCaseName, modalities.toString(), "", expected.toString(), actual.toString(),
						results.values().stream().filter(sdkResult -> modalities.contains(sdkResult.getModality()))
								.allMatch(sdkResult -> sdkResult.isStatus() && sdkResult.isExtracted()
										&& (sdkResult.getStatusCode() == 401 || sdkResult.getStatusCode() == 403)));

			}
		} catch (Exception e) {
			e.printStackTrace();
			builder.build(testCaseName, modalities.toString(), "", expected.toString(), ExceptionUtils.getStackTrace(e),
					false);
		}
	}

	public void extractTemplateNoInputData(String testCaseName, List<String> modalities, String probeFileName) {
		Map<String, SDKResult> results = new HashMap<>();

		List<String> expected = modalities.stream().map(modality -> modality + " -> status : 402 or 403")
				.collect(Collectors.toList());

		try {
			Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

			if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
					BiometricFunction.EXTRACT)) {
				List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
						+ helper.getProvider(modality).getClass().getName()
						+ "supports Modality : true, Supports extractTemplate : true, getSupportedMethods value contains BiometricType : true")
						.collect(Collectors.toList());
				builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(),
						modalities.stream()
								.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
										+ "supports Modality : " + isModalitySupported.get(modality)
										+ ", Supports extractTemplate : " + isMethodSupported.get(modality)
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

				extractTemplate(results, biometricRecord, null, bioTypeList, false);

				List<String> actual = results.values().stream()
						.map(sdkResult -> sdkResult.getModality() + " -> "
								+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
										: "status : " + sdkResult.getStatusCode() + " & template extracted : "
												+ sdkResult.isExtracted()))
						.collect(Collectors.toList());

				builder.build(testCaseName, modalities.toString(), "", expected.toString(), actual.toString(),
						results.values().stream().filter(sdkResult -> modalities.contains(sdkResult.getModality()))
								.allMatch(sdkResult -> sdkResult.isStatus() && sdkResult.isExtracted()
										&& (sdkResult.getStatusCode() == 402 || sdkResult.getStatusCode() == 403)));

			}
		} catch (Exception e) {
			e.printStackTrace();
			builder.build(testCaseName, modalities.toString(), "", expected.toString(), ExceptionUtils.getStackTrace(e),
					false);
		}
	}

	public void extractAndMatchFMRSuccess(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) {
		Map<String, SDKResult> results = new HashMap<>();

		List<String> expected = modalities.stream().map(modality -> modality
				+ " -> status : 200 & MatchDecision: MATCHED" + " BdbInfo, BirInfo, Sb, SbInfo should not be null")
				.collect(Collectors.toList());

		try {
			Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

			if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
					BiometricFunction.EXTRACT)) {
				List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
						+ helper.getProvider(modality).getClass().getName()
						+ "supports Modality : true, Supports extractTemplate : true, getSupportedMethods value contains BiometricType : true")
						.collect(Collectors.toList());
				builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(),
						modalities.stream()
								.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
										+ "supports Modality : " + isModalitySupported.get(modality)
										+ ", Supports extractTemplate : " + isMethodSupported.get(modality)
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

				extractTemplate(results, probeBirBiometricRecord, galleryBirBiometricRecords, bioTypeList, true);

				List<String> actual = results.values().stream()
						.map(sdkResult -> sdkResult.getModality() + " -> "
								+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
										: "status : " + sdkResult.getStatusCode() + " & template extracted : "
												+ sdkResult.isExtracted() + "& MatchDecision : "
												+ sdkResult.getMatchDecision()))
						.collect(Collectors.toList());

				builder.build(testCaseName, modalities.toString(), "", expected.toString(), actual.toString(),
						results.values().stream().filter(sdkResult -> modalities.contains(sdkResult.getModality()))
								.allMatch(sdkResult -> sdkResult.isStatus() && sdkResult.isExtracted()
										&& sdkResult.getStatusCode() >= 200 && sdkResult.getStatusCode() <= 299
										&& sdkResult.getMatchDecision() == Match.MATCHED));
			}
		} catch (Exception e) {
			e.printStackTrace();
			builder.build(testCaseName, modalities.toString(), "", expected.toString(), ExceptionUtils.getStackTrace(e),
					false);
		}
	}

	public void extractAndMatchFMRFail(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) {
		Map<String, SDKResult> results = new HashMap<>();

		List<String> expected = modalities.stream().map(modality -> modality
				+ " -> status : 200 & MatchDecision: NOT_MATCHED" + " BdbInfo, BirInfo, Sb, SbInfo should not be null")
				.collect(Collectors.toList());

		try {
			Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

			if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
					BiometricFunction.EXTRACT)) {
				List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
						+ helper.getProvider(modality).getClass().getName()
						+ "supports Modality : true, Supports extractTemplate : true, getSupportedMethods value contains BiometricType : true")
						.collect(Collectors.toList());
				builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(),
						modalities.stream()
								.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
										+ "supports Modality : " + isModalitySupported.get(modality)
										+ ", Supports extractTemplate : " + isMethodSupported.get(modality)
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

				extractTemplate(results, probeBirBiometricRecord, galleryBirBiometricRecords, bioTypeList, true);

				List<String> actual = results.values().stream()
						.map(sdkResult -> sdkResult.getModality() + " -> "
								+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
										: "status : " + sdkResult.getStatusCode() + " & template extracted : "
												+ sdkResult.isExtracted() + "& MatchDecision : "
												+ sdkResult.getMatchDecision()))
						.collect(Collectors.toList());

				builder.build(testCaseName, modalities.toString(), "", expected.toString(), actual.toString(),
						results.values().stream().filter(sdkResult -> modalities.contains(sdkResult.getModality()))
								.allMatch(sdkResult -> sdkResult.isStatus() && sdkResult.isExtracted()
										&& sdkResult.getStatusCode() >= 200 && sdkResult.getStatusCode() <= 299
										&& sdkResult.getMatchDecision() == Match.NOT_MATCHED));
			}
		} catch (Exception e) {
			e.printStackTrace();
			builder.build(testCaseName, modalities.toString(), "", expected.toString(), ExceptionUtils.getStackTrace(e),
					false);
		}
	}

	private void extractTemplate(Map<String, SDKResult> results, BiometricRecord biometricRecord,
			BiometricRecord[] galleryBiometricRecords, List<BiometricType> bioTypeList, boolean isMatch) {
		Map<String, SDKResult> sdkResults = new HashMap<>();
		for (SDKResult result : results.values()) {
			if (!sdkResults.containsKey(result.getModality()) && Objects.isNull(result.getStatusCode())
					&& Objects.isNull(result.getErrorStackTrace())) {
				try {
					Response<BiometricRecord> extractResult = helper.getProvider(result.getModality())
							.extractTemplate(biometricRecord, bioTypeList, null);
					if (extractResult.getStatusCode() >= 200 && extractResult.getStatusCode() <= 299
							&& Objects.nonNull(extractResult.getResponse())) {
						extractResult.getResponse().getSegments().stream().filter(
								bir -> results.containsKey(bir.getBdbInfo().getType().get(0).value().toLowerCase()))
								.forEach(bir -> {
									boolean status = Objects.nonNull(bir.getBirInfo())
											&& Objects.nonNull(bir.getBirInfo().getPayload())
											&& Objects.nonNull(bir.getBdbInfo())
											&& Objects.nonNull(bir.getBdbInfo().getType())
											&& Objects.nonNull(bir.getBdbInfo().getSubtype())
											&& Objects.nonNull(bir.getBdbInfo().getFormat())
											&& Objects.nonNull(bir.getSb()) && Objects.nonNull(bir.getSbInfo())
											&& Objects.nonNull(bir.getSbInfo().getFormat());
									sdkResults.put(bir.getBdbInfo().getType().get(0).value().toLowerCase(),
											new SDKResult().setExtracted(true)
													.setStatusCode(extractResult.getStatusCode())
													.setErrorStackTrace(status ? null
															: "BirInfo is null : " + Objects.isNull(bir.getBirInfo())
																	+ ", BirInfo Payload is null : "
																	+ Objects.isNull(bir.getBirInfo().getPayload())
																	+ ", BdbInfo is null : "
																	+ Objects.isNull(bir.getBdbInfo())
																	+ ", BdbInfo Type is null : "
																	+ Objects.isNull(bir.getBdbInfo().getType())
																	+ ", bdbInfo Subtype is null :"
																	+ Objects.isNull(bir.getBdbInfo().getSubtype())
																	+ ", BdbInfo Format is null : "
																	+ Objects.isNull(bir.getBdbInfo().getFormat())
																	+ ", Sb is null : " + Objects.isNull(bir.getSb())
																	+ ", SbInfo is null : "
																	+ Objects.isNull(bir.getSbInfo())
																	+ ", SbInfo Format is null : "
																	+ Objects.isNull(bir.getSbInfo().getFormat()))
													.setStatus(status));
								});
						if (isMatch) {
							match(results, biometricRecord, galleryBiometricRecords, bioTypeList, result);
						} else {
							qualityCheck(results, bioTypeList, result, extractResult);
						}
					} else if (Objects.isNull(extractResult.getResponse())) {
						result.setExtracted(false).setStatusCode(extractResult.getStatusCode())
								.setErrorStackTrace("Response is null").setStatus(false);
					} else {
						result.setExtracted(false).setStatusCode(extractResult.getStatusCode()).setStatus(false);
					}
				} catch (Exception e) {
					e.printStackTrace();
					result.setErrorStackTrace(ExceptionUtils.getStackTrace(e));
					result.setStatus(false);
				}
			}
		}
		results.putAll(sdkResults);
	}

	private void match(Map<String, SDKResult> results, BiometricRecord biometricRecord,
			BiometricRecord[] galleryBiometricRecords, List<BiometricType> bioTypeList, SDKResult result) {
		Map<String, SDKResult> sdkResults = new HashMap<>();
		Response<MatchDecision[]> matchResult = helper.getProvider(result.getModality()).match(biometricRecord,
				galleryBiometricRecords, bioTypeList, null);
		result.setStatusCode(matchResult.getStatusCode()).setStatus(true);
		if (matchResult.getStatusCode() >= 200 && matchResult.getStatusCode() <= 299) {
			List<Map<BiometricType, Decision>> matchDecisions = Arrays.asList(matchResult.getResponse()).stream()
					.map(matchDecision -> matchDecision.getDecisions()).collect(Collectors.toList());
			for (Map<BiometricType, Decision> matchDecision : matchDecisions) {
				for (Entry<BiometricType, Decision> entry : matchDecision.entrySet()) {
					sdkResults.put(entry.getKey().value().toLowerCase(),
							new SDKResult().setStatusCode(matchResult.getStatusCode())
									.setMatchDecision(entry.getValue().getMatch()).setStatus(true));
				}
			}
			results.putAll(sdkResults);
		}
	}

	private void qualityCheck(Map<String, SDKResult> results, List<BiometricType> bioTypeList, SDKResult result,
			Response<BiometricRecord> extractResult) {
		Map<String, SDKResult> sdkResults = new HashMap<>();
		System.err.println("qualityCheck");
		Response<QualityCheck> qualityResult = helper.getProvider(result.getModality())
				.checkQuality(extractResult.getResponse(), bioTypeList, null);
		if (qualityResult.getStatusCode() >= 200 && qualityResult.getStatusCode() <= 299) {
			result.setStatusCode(extractResult.getStatusCode()).setStatus(true);
			Set<Entry<BiometricType, QualityScore>> entrySet = qualityResult.getResponse().getScores().entrySet();
			for (Entry<BiometricType, QualityScore> entry : entrySet) {
				sdkResults.put(entry.getKey().value().toLowerCase(),
						new SDKResult().setStatusCode(qualityResult.getStatusCode())
								.setScore(entry.getValue().getScore()).setExtracted(true).setStatus(true));
			}
			results.putAll(sdkResults);
		}
	}
}
