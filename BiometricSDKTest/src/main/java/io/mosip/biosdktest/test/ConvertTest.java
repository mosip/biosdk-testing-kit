package io.mosip.biosdktest.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.biosdktest.config.TestResultBuilder;
import io.mosip.biosdktest.dto.SDKResult;
import io.mosip.kernel.biometrics.constant.BiometricFunction;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.cbeffutil.impl.CbeffImpl;
import io.mosip.kernel.core.cbeffutil.entity.BIR;
import io.mosip.kernel.core.cbeffutil.spi.CbeffUtil;
import io.mosip.kernel.core.exception.ExceptionUtils;

/**
 * @author Manoj SP
 *
 */
@Component
public class ConvertTest {

	@Autowired
	private TestResultBuilder builder;

	@Autowired
	private BioTestHelper helper;

	private CbeffUtil cbeffReader = new CbeffImpl();

	public void convertFormatJPEGToJPEG(String testCaseName, List<String> modalities, String probeFileName)
			throws Exception {
		Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
		Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
		Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

		if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
				BiometricFunction.CONVERT_FORMAT)) {
			List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
					+ helper.getProvider(modality).getClass().getName()
					+ "supports Modality : true, Supports convert : true, getSupportedMethods value contains BiometricType : true")
					.collect(Collectors.toList());
			builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(), modalities
					.stream()
					.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
							+ "supports Modality : " + isModalitySupported.get(modality) + ", Supports convert : "
							+ isMethodSupported.get(modality) + ", getSupportedMethods value contains BiometricType : "
							+ isMethodSupportsModality.get(modality))
					.collect(Collectors.toList()).toString(), null);
		} else {
			List<String> expected = modalities.stream().map(modality -> modality
					+ " -> Response BiometricRecord should have BiometricType from " + modalities.toString()
					+ " & BirInfo, BirInfo Payload, BdbInfo, BdbInfo Type, bdbInfo Subtype, BdbInfo Format, sb, sbInfo, sbInfo Format should not be null & response Bdbinfo Type should match all requested modalities")
					.collect(Collectors.toList());

			List<BIR> birs = cbeffReader
					.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(helper.getInputFile(probeFileName)));
			List<io.mosip.kernel.biometrics.entities.BIR> bioRecordSegments = birs.stream()
					.map(bir -> helper.convertToBiometricRecordBIR(bir)).collect(Collectors.toList());

			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(bioRecordSegments);

			Map<String, SDKResult> results = new HashMap<>();

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

			List<BiometricType> bioTypeList = modalities.stream().map(bioType -> helper.getBiometricType(bioType))
					.collect(Collectors.toList());

			List<List<BiometricType>> responseBioTypes = new ArrayList<>();

			convert(probeBirBiometricRecord, results, bioTypeList, responseBioTypes, "JPEG", "JPEG");

			List<String> actual = results.values().stream()
					.map(sdkResult -> sdkResult.getModality() + " -> "
							+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
									: "response BiometricTypes : " + responseBioTypes.toString()))
					.collect(Collectors.toList());

			builder.build(testCaseName, modalities.toString(), "", expected.toString(), actual.toString(),
					results.values().stream().filter(sdkResult -> modalities.contains(sdkResult.getModality()))
							.allMatch(sdkResult -> sdkResult.isStatus()));
		}
	}

	public void convertFormatJPEGToBMP(String testCaseName, List<String> modalities, String probeFileName)
			throws Exception {
		Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
		Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
		Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

		if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
				BiometricFunction.CONVERT_FORMAT)) {
			List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
					+ helper.getProvider(modality).getClass().getName()
					+ "supports Modality : true, Supports convert : true, getSupportedMethods value contains BiometricType : true")
					.collect(Collectors.toList());
			builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(), modalities
					.stream()
					.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
							+ "supports Modality : " + isModalitySupported.get(modality) + ", Supports convert : "
							+ isMethodSupported.get(modality) + ", getSupportedMethods value contains BiometricType : "
							+ isMethodSupportsModality.get(modality))
					.collect(Collectors.toList()).toString(), null);
		} else {
			List<String> expected = modalities.stream().map(modality -> modality
					+ " -> Response BiometricRecord should have BiometricType from " + modalities.toString()
					+ " & BirInfo, BirInfo Payload, BdbInfo, BdbInfo Type, bdbInfo Subtype, BdbInfo Format, sb, sbInfo, sbInfo Format should not be null & response Bdbinfo Type should match all requested modalities")
					.collect(Collectors.toList());

			List<BIR> birs = cbeffReader
					.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(helper.getInputFile(probeFileName)));
			List<io.mosip.kernel.biometrics.entities.BIR> bioRecordSegments = birs.stream()
					.map(bir -> helper.convertToBiometricRecordBIR(bir)).collect(Collectors.toList());

			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(bioRecordSegments);

			Map<String, SDKResult> results = new HashMap<>();

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

			List<BiometricType> bioTypeList = modalities.stream().map(bioType -> helper.getBiometricType(bioType))
					.collect(Collectors.toList());

			List<List<BiometricType>> responseBioTypes = new ArrayList<>();

			convert(probeBirBiometricRecord, results, bioTypeList, responseBioTypes, "JPEG", "BMP");

			List<String> actual = results.values().stream()
					.map(sdkResult -> sdkResult.getModality() + " -> "
							+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
									: "response BiometricTypes : " + responseBioTypes.toString()))
					.collect(Collectors.toList());

			builder.build(testCaseName, modalities.toString(), "", expected.toString(), actual.toString(),
					results.values().stream().filter(sdkResult -> modalities.contains(sdkResult.getModality()))
							.allMatch(sdkResult -> sdkResult.isStatus()));
		}
	}

	public void convertFormatJPEGToWSQ(String testCaseName, List<String> modalities, String probeFileName)
			throws Exception {
		Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
		Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
		Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

		if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
				BiometricFunction.CONVERT_FORMAT)) {
			List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
					+ helper.getProvider(modality).getClass().getName()
					+ "supports Modality : true, Supports convert : true, getSupportedMethods value contains BiometricType : true")
					.collect(Collectors.toList());
			builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(), modalities
					.stream()
					.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
							+ "supports Modality : " + isModalitySupported.get(modality) + ", Supports convert : "
							+ isMethodSupported.get(modality) + ", getSupportedMethods value contains BiometricType : "
							+ isMethodSupportsModality.get(modality))
					.collect(Collectors.toList()).toString(), null);
		} else {
			List<String> expected = modalities.stream().map(modality -> modality
					+ " -> Response BiometricRecord should have BiometricType from " + modalities.toString()
					+ " & BirInfo, BirInfo Payload, BdbInfo, BdbInfo Type, bdbInfo Subtype, BdbInfo Format, sb, sbInfo, sbInfo Format should not be null & response Bdbinfo Type should match all requested modalities")
					.collect(Collectors.toList());

			List<BIR> birs = cbeffReader
					.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(helper.getInputFile(probeFileName)));
			List<io.mosip.kernel.biometrics.entities.BIR> bioRecordSegments = birs.stream()
					.map(bir -> helper.convertToBiometricRecordBIR(bir)).collect(Collectors.toList());

			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(bioRecordSegments);

			Map<String, SDKResult> results = new HashMap<>();

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

			List<BiometricType> bioTypeList = modalities.stream().map(bioType -> helper.getBiometricType(bioType))
					.collect(Collectors.toList());

			List<List<BiometricType>> responseBioTypes = new ArrayList<>();

			convert(probeBirBiometricRecord, results, bioTypeList, responseBioTypes, "JPEG", "WSQ");

			List<String> actual = results.values().stream()
					.map(sdkResult -> sdkResult.getModality() + " -> "
							+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
									: "response BiometricTypes : " + responseBioTypes.toString()))
					.collect(Collectors.toList());

			builder.build(testCaseName, modalities.toString(), "", expected.toString(), actual.toString(),
					results.values().stream().filter(sdkResult -> modalities.contains(sdkResult.getModality()))
							.allMatch(sdkResult -> sdkResult.isStatus()));
		}
	}

	public void convertFormatBMPToJPEG(String testCaseName, List<String> modalities, String probeFileName)
			throws Exception {
		Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
		Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
		Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

		if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
				BiometricFunction.CONVERT_FORMAT)) {
			List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
					+ helper.getProvider(modality).getClass().getName()
					+ "supports Modality : true, Supports convert : true, getSupportedMethods value contains BiometricType : true")
					.collect(Collectors.toList());
			builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(), modalities
					.stream()
					.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
							+ "supports Modality : " + isModalitySupported.get(modality) + ", Supports convert : "
							+ isMethodSupported.get(modality) + ", getSupportedMethods value contains BiometricType : "
							+ isMethodSupportsModality.get(modality))
					.collect(Collectors.toList()).toString(), null);
		} else {
			List<String> expected = modalities.stream().map(modality -> modality
					+ " -> Response BiometricRecord should have BiometricType from " + modalities.toString()
					+ " & BirInfo, BirInfo Payload, BdbInfo, BdbInfo Type, bdbInfo Subtype, BdbInfo Format, sb, sbInfo, sbInfo Format should not be null & response Bdbinfo Type should match all requested modalities")
					.collect(Collectors.toList());

			List<BIR> birs = cbeffReader
					.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(helper.getInputFile(probeFileName)));
			List<io.mosip.kernel.biometrics.entities.BIR> bioRecordSegments = birs.stream()
					.map(bir -> helper.convertToBiometricRecordBIR(bir)).collect(Collectors.toList());

			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(bioRecordSegments);

			Map<String, SDKResult> results = new HashMap<>();

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

			List<BiometricType> bioTypeList = modalities.stream().map(bioType -> helper.getBiometricType(bioType))
					.collect(Collectors.toList());

			List<List<BiometricType>> responseBioTypes = new ArrayList<>();

			convert(probeBirBiometricRecord, results, bioTypeList, responseBioTypes, "BMP", "JPEG");

			List<String> actual = results.values().stream()
					.map(sdkResult -> sdkResult.getModality() + " -> "
							+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
									: "response BiometricTypes : " + responseBioTypes.toString()))
					.collect(Collectors.toList());

			builder.build(testCaseName, modalities.toString(), "", expected.toString(), actual.toString(),
					results.values().stream().filter(sdkResult -> modalities.contains(sdkResult.getModality()))
							.allMatch(sdkResult -> sdkResult.isStatus()));
		}
	}

	public void convertFormatBMPToBMP(String testCaseName, List<String> modalities, String probeFileName)
			throws Exception {
		Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
		Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
		Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

		if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
				BiometricFunction.CONVERT_FORMAT)) {
			List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
					+ helper.getProvider(modality).getClass().getName()
					+ "supports Modality : true, Supports convert : true, getSupportedMethods value contains BiometricType : true")
					.collect(Collectors.toList());
			builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(), modalities
					.stream()
					.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
							+ "supports Modality : " + isModalitySupported.get(modality) + ", Supports convert : "
							+ isMethodSupported.get(modality) + ", getSupportedMethods value contains BiometricType : "
							+ isMethodSupportsModality.get(modality))
					.collect(Collectors.toList()).toString(), null);
		} else {
			List<String> expected = modalities.stream().map(modality -> modality
					+ " -> Response BiometricRecord should have BiometricType from " + modalities.toString()
					+ " & BirInfo, BirInfo Payload, BdbInfo, BdbInfo Type, bdbInfo Subtype, BdbInfo Format, sb, sbInfo, sbInfo Format should not be null & response Bdbinfo Type should match all requested modalities")
					.collect(Collectors.toList());

			List<BIR> birs = cbeffReader
					.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(helper.getInputFile(probeFileName)));
			List<io.mosip.kernel.biometrics.entities.BIR> bioRecordSegments = birs.stream()
					.map(bir -> helper.convertToBiometricRecordBIR(bir)).collect(Collectors.toList());

			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(bioRecordSegments);

			Map<String, SDKResult> results = new HashMap<>();

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

			List<BiometricType> bioTypeList = modalities.stream().map(bioType -> helper.getBiometricType(bioType))
					.collect(Collectors.toList());

			List<List<BiometricType>> responseBioTypes = new ArrayList<>();

			convert(probeBirBiometricRecord, results, bioTypeList, responseBioTypes, "BMP", "BMP");

			List<String> actual = results.values().stream()
					.map(sdkResult -> sdkResult.getModality() + " -> "
							+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
									: "response BiometricTypes : " + responseBioTypes.toString()))
					.collect(Collectors.toList());

			builder.build(testCaseName, modalities.toString(), "", expected.toString(), actual.toString(),
					results.values().stream().filter(sdkResult -> modalities.contains(sdkResult.getModality()))
							.allMatch(sdkResult -> sdkResult.isStatus()));
		}
	}

	public void convertFormatBMPToWSQ(String testCaseName, List<String> modalities, String probeFileName)
			throws Exception {
		Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
		Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
		Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

		if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
				BiometricFunction.CONVERT_FORMAT)) {
			List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
					+ helper.getProvider(modality).getClass().getName()
					+ "supports Modality : true, Supports convert : true, getSupportedMethods value contains BiometricType : true")
					.collect(Collectors.toList());
			builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(), modalities
					.stream()
					.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
							+ "supports Modality : " + isModalitySupported.get(modality) + ", Supports convert : "
							+ isMethodSupported.get(modality) + ", getSupportedMethods value contains BiometricType : "
							+ isMethodSupportsModality.get(modality))
					.collect(Collectors.toList()).toString(), null);
		} else {
			List<String> expected = modalities.stream().map(modality -> modality
					+ " -> Response BiometricRecord should have BiometricType from " + modalities.toString()
					+ " & BirInfo, BirInfo Payload, BdbInfo, BdbInfo Type, bdbInfo Subtype, BdbInfo Format, sb, sbInfo, sbInfo Format should not be null & response Bdbinfo Type should match all requested modalities")
					.collect(Collectors.toList());

			List<BIR> birs = cbeffReader
					.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(helper.getInputFile(probeFileName)));
			List<io.mosip.kernel.biometrics.entities.BIR> bioRecordSegments = birs.stream()
					.map(bir -> helper.convertToBiometricRecordBIR(bir)).collect(Collectors.toList());

			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(bioRecordSegments);

			Map<String, SDKResult> results = new HashMap<>();

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

			List<BiometricType> bioTypeList = modalities.stream().map(bioType -> helper.getBiometricType(bioType))
					.collect(Collectors.toList());

			List<List<BiometricType>> responseBioTypes = new ArrayList<>();

			convert(probeBirBiometricRecord, results, bioTypeList, responseBioTypes, "BMP", "WSQ");

			List<String> actual = results.values().stream()
					.map(sdkResult -> sdkResult.getModality() + " -> "
							+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
									: "response BiometricTypes : " + responseBioTypes.toString()))
					.collect(Collectors.toList());

			builder.build(testCaseName, modalities.toString(), "", expected.toString(), actual.toString(),
					results.values().stream().filter(sdkResult -> modalities.contains(sdkResult.getModality()))
							.allMatch(sdkResult -> sdkResult.isStatus()));
		}
	}

	public void convertFormatWSQToJPEG(String testCaseName, List<String> modalities, String probeFileName)
			throws Exception {
		Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
		Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
		Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

		if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
				BiometricFunction.CONVERT_FORMAT)) {
			List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
					+ helper.getProvider(modality).getClass().getName()
					+ "supports Modality : true, Supports convert : true, getSupportedMethods value contains BiometricType : true")
					.collect(Collectors.toList());
			builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(), modalities
					.stream()
					.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
							+ "supports Modality : " + isModalitySupported.get(modality) + ", Supports convert : "
							+ isMethodSupported.get(modality) + ", getSupportedMethods value contains BiometricType : "
							+ isMethodSupportsModality.get(modality))
					.collect(Collectors.toList()).toString(), null);
		} else {
			List<String> expected = modalities.stream().map(modality -> modality
					+ " -> Response BiometricRecord should have BiometricType from " + modalities.toString()
					+ " & BirInfo, BirInfo Payload, BdbInfo, BdbInfo Type, bdbInfo Subtype, BdbInfo Format, sb, sbInfo, sbInfo Format should not be null & response Bdbinfo Type should match all requested modalities")
					.collect(Collectors.toList());

			List<BIR> birs = cbeffReader
					.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(helper.getInputFile(probeFileName)));
			List<io.mosip.kernel.biometrics.entities.BIR> bioRecordSegments = birs.stream()
					.map(bir -> helper.convertToBiometricRecordBIR(bir)).collect(Collectors.toList());

			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(bioRecordSegments);

			Map<String, SDKResult> results = new HashMap<>();

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

			List<BiometricType> bioTypeList = modalities.stream().map(bioType -> helper.getBiometricType(bioType))
					.collect(Collectors.toList());

			List<List<BiometricType>> responseBioTypes = new ArrayList<>();

			convert(probeBirBiometricRecord, results, bioTypeList, responseBioTypes, "WSQ", "JPEG");

			List<String> actual = results.values().stream()
					.map(sdkResult -> sdkResult.getModality() + " -> "
							+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
									: "response BiometricTypes : " + responseBioTypes.toString()))
					.collect(Collectors.toList());

			builder.build(testCaseName, modalities.toString(), "", expected.toString(), actual.toString(),
					results.values().stream().filter(sdkResult -> modalities.contains(sdkResult.getModality()))
							.allMatch(sdkResult -> sdkResult.isStatus()));
		}
	}

	public void convertFormatWSQToBMP(String testCaseName, List<String> modalities, String probeFileName)
			throws Exception {
		Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
		Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
		Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

		if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
				BiometricFunction.CONVERT_FORMAT)) {
			List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
					+ helper.getProvider(modality).getClass().getName()
					+ "supports Modality : true, Supports convert : true, getSupportedMethods value contains BiometricType : true")
					.collect(Collectors.toList());
			builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(), modalities
					.stream()
					.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
							+ "supports Modality : " + isModalitySupported.get(modality) + ", Supports convert : "
							+ isMethodSupported.get(modality) + ", getSupportedMethods value contains BiometricType : "
							+ isMethodSupportsModality.get(modality))
					.collect(Collectors.toList()).toString(), null);
		} else {
			List<String> expected = modalities.stream().map(modality -> modality
					+ " -> Response BiometricRecord should have BiometricType from " + modalities.toString()
					+ " & BirInfo, BirInfo Payload, BdbInfo, BdbInfo Type, bdbInfo Subtype, BdbInfo Format, sb, sbInfo, sbInfo Format should not be null & response Bdbinfo Type should match all requested modalities")
					.collect(Collectors.toList());

			List<BIR> birs = cbeffReader
					.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(helper.getInputFile(probeFileName)));
			List<io.mosip.kernel.biometrics.entities.BIR> bioRecordSegments = birs.stream()
					.map(bir -> helper.convertToBiometricRecordBIR(bir)).collect(Collectors.toList());

			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(bioRecordSegments);

			Map<String, SDKResult> results = new HashMap<>();

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

			List<BiometricType> bioTypeList = modalities.stream().map(bioType -> helper.getBiometricType(bioType))
					.collect(Collectors.toList());

			List<List<BiometricType>> responseBioTypes = new ArrayList<>();

			convert(probeBirBiometricRecord, results, bioTypeList, responseBioTypes, "WSQ", "BMP");

			List<String> actual = results.values().stream()
					.map(sdkResult -> sdkResult.getModality() + " -> "
							+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
									: "response BiometricTypes : " + responseBioTypes.toString()))
					.collect(Collectors.toList());

			builder.build(testCaseName, modalities.toString(), "", expected.toString(), actual.toString(),
					results.values().stream().filter(sdkResult -> modalities.contains(sdkResult.getModality()))
							.allMatch(sdkResult -> sdkResult.isStatus()));
		}
	}

	public void convertFormatWSQToWSQ(String testCaseName, List<String> modalities, String probeFileName)
			throws Exception {
		Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
		Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
		Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

		if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
				BiometricFunction.CONVERT_FORMAT)) {
			List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
					+ helper.getProvider(modality).getClass().getName()
					+ "supports Modality : true, Supports convert : true, getSupportedMethods value contains BiometricType : true")
					.collect(Collectors.toList());
			builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(), modalities
					.stream()
					.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
							+ "supports Modality : " + isModalitySupported.get(modality) + ", Supports convert : "
							+ isMethodSupported.get(modality) + ", getSupportedMethods value contains BiometricType : "
							+ isMethodSupportsModality.get(modality))
					.collect(Collectors.toList()).toString(), null);
		} else {
			List<String> expected = modalities.stream().map(modality -> modality
					+ " -> Response BiometricRecord should have BiometricType from " + modalities.toString()
					+ " & BirInfo, BirInfo Payload, BdbInfo, BdbInfo Type, bdbInfo Subtype, BdbInfo Format, sb, sbInfo, sbInfo Format should not be null & response Bdbinfo Type should match all requested modalities")
					.collect(Collectors.toList());

			List<BIR> birs = cbeffReader
					.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(helper.getInputFile(probeFileName)));
			List<io.mosip.kernel.biometrics.entities.BIR> bioRecordSegments = birs.stream()
					.map(bir -> helper.convertToBiometricRecordBIR(bir)).collect(Collectors.toList());

			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(bioRecordSegments);

			Map<String, SDKResult> results = new HashMap<>();

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

			List<BiometricType> bioTypeList = modalities.stream().map(bioType -> helper.getBiometricType(bioType))
					.collect(Collectors.toList());

			List<List<BiometricType>> responseBioTypes = new ArrayList<>();

			convert(probeBirBiometricRecord, results, bioTypeList, responseBioTypes, "WSQ", "WSQ");

			List<String> actual = results.values().stream()
					.map(sdkResult -> sdkResult.getModality() + " -> "
							+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
									: "response BiometricTypes : " + responseBioTypes.toString()))
					.collect(Collectors.toList());

			builder.build(testCaseName, modalities.toString(), "", expected.toString(), actual.toString(),
					results.values().stream().filter(sdkResult -> modalities.contains(sdkResult.getModality()))
							.allMatch(sdkResult -> sdkResult.isStatus()));
		}
	}

	public void convertFormatInvalidData(String testCaseName, List<String> modalities, String probeFileName)
			throws Exception {
		Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
		Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
		Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

		if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
				BiometricFunction.CONVERT_FORMAT)) {
			List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
					+ helper.getProvider(modality).getClass().getName()
					+ "supports Modality : true, Supports convert : true, getSupportedMethods value contains BiometricType : true")
					.collect(Collectors.toList());
			builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(), modalities
					.stream()
					.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
							+ "supports Modality : " + isModalitySupported.get(modality) + ", Supports convert : "
							+ isMethodSupported.get(modality) + ", getSupportedMethods value contains BiometricType : "
							+ isMethodSupportsModality.get(modality))
					.collect(Collectors.toList()).toString(), null);
		} else {
			List<String> expected = modalities.stream().map(modality -> modality + " -> Exception to be thrown")
					.collect(Collectors.toList());

			List<BIR> birs = cbeffReader
					.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(helper.getInputFile(probeFileName)));
			List<io.mosip.kernel.biometrics.entities.BIR> bioRecordSegments = birs.stream()
					.map(bir -> helper.convertToBiometricRecordBIR(bir)).collect(Collectors.toList());

			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(bioRecordSegments);

			Map<String, SDKResult> results = new HashMap<>();

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

			List<BiometricType> bioTypeList = modalities.stream().map(bioType -> helper.getBiometricType(bioType))
					.collect(Collectors.toList());

			List<List<BiometricType>> responseBioTypes = new ArrayList<>();

			convert(probeBirBiometricRecord, results, bioTypeList, responseBioTypes, "JPEG", "BMP");

			List<String> actual = results.values().stream()
					.map(sdkResult -> sdkResult.getModality() + " -> "
							+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
									: "response BiometricTypes : " + responseBioTypes.toString()))
					.collect(Collectors.toList());

			builder.build(testCaseName, modalities.toString(), "", expected.toString(), actual.toString(),
					results.values().stream().filter(sdkResult -> modalities.contains(sdkResult.getModality()))
							.allMatch(sdkResult -> Objects.nonNull(sdkResult.getErrorStackTrace())));
		}
	}

	public void convertFormatNoData(String testCaseName, List<String> modalities, String probeFileName)
			throws Exception {
		Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
		Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
		Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

		if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
				BiometricFunction.CONVERT_FORMAT)) {
			List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
					+ helper.getProvider(modality).getClass().getName()
					+ "supports Modality : true, Supports convert : true, getSupportedMethods value contains BiometricType : true")
					.collect(Collectors.toList());
			builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(), modalities
					.stream()
					.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
							+ "supports Modality : " + isModalitySupported.get(modality) + ", Supports convert : "
							+ isMethodSupported.get(modality) + ", getSupportedMethods value contains BiometricType : "
							+ isMethodSupportsModality.get(modality))
					.collect(Collectors.toList()).toString(), null);
		} else {
			List<String> expected = modalities.stream().map(modality -> modality + " -> Exception to be thrown")
					.collect(Collectors.toList());

			List<BIR> birs = cbeffReader
					.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(helper.getInputFile(probeFileName)));
			List<io.mosip.kernel.biometrics.entities.BIR> bioRecordSegments = birs.stream()
					.map(bir -> helper.convertToBiometricRecordBIR(bir)).collect(Collectors.toList());

			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(bioRecordSegments);

			Map<String, SDKResult> results = new HashMap<>();

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

			List<BiometricType> bioTypeList = modalities.stream().map(bioType -> helper.getBiometricType(bioType))
					.collect(Collectors.toList());

			List<List<BiometricType>> responseBioTypes = new ArrayList<>();

			convert(probeBirBiometricRecord, results, bioTypeList, responseBioTypes, "JPEG", "BMP");

			List<String> actual = results.values().stream()
					.map(sdkResult -> sdkResult.getModality() + " -> "
							+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
									: "response BiometricTypes : " + responseBioTypes.toString()))
					.collect(Collectors.toList());

			builder.build(testCaseName, modalities.toString(), "", expected.toString(), actual.toString(),
					results.values().stream().filter(sdkResult -> modalities.contains(sdkResult.getModality()))
							.allMatch(sdkResult -> Objects.nonNull(sdkResult.getErrorStackTrace())));
		}
	}

	private void convert(BiometricRecord probeBirBiometricRecord, Map<String, SDKResult> results,
			List<BiometricType> bioTypeList, List<List<BiometricType>> responseBioTypes, String from, String to) {
		Map<String, SDKResult> sdkResults = new HashMap<>();
		for (SDKResult result : results.values()) {
			if (!sdkResults.containsKey(result.getModality()) && Objects.isNull(result.getStatusCode())
					&& Objects.isNull(result.getErrorStackTrace())) {
				try {
					BiometricRecord response = helper.getProvider(result.getModality())
							.convertFormat(probeBirBiometricRecord, from, to, null, null, bioTypeList);
					boolean status = response.getSegments().stream()
							.allMatch(bir -> Objects.nonNull(bir.getBirInfo())
									&& Objects.nonNull(bir.getBirInfo().getPayload())
									&& Objects.nonNull(bir.getBdbInfo()) && Objects.nonNull(bir.getBdbInfo().getType())
									&& Objects.nonNull(bir.getBdbInfo().getSubtype())
									&& Objects.nonNull(bir.getBdbInfo().getFormat()) && Objects.nonNull(bir.getSb())
									&& Objects.nonNull(bir.getSbInfo()) && Objects.nonNull(bir.getSbInfo().getFormat())
									&& bir.getBdbInfo().getType().stream().allMatch(type -> BiometricType
											.fromValue(type.value()) == helper.getBiometricType(result.getModality())));
					response.getSegments().stream().filter(
							bir -> Objects.nonNull(bir.getBdbInfo()) && Objects.nonNull(bir.getBdbInfo().getType()))
							.forEach(segmentedBir -> responseBioTypes.add(segmentedBir.getBdbInfo().getType().stream()
									.map(type -> helper.getBiometricType(type.value())).collect(Collectors.toList())));
					response.getSegments().stream().forEach(bir -> bir.getBdbInfo().getType().stream()
							.forEach(type -> sdkResults.put(type.value().toLowerCase(),
									new SDKResult().setModality(result.getModality()).setErrorStackTrace(status ? null
											: "BirInfo is null : " + Objects.isNull(bir.getBirInfo())
													+ ", BirInfo Payload is null : "
													+ (Objects.nonNull(bir.getBirInfo())
															&& Objects.isNull(bir.getBirInfo().getPayload()))
													+ ", BdbInfo is null : " + Objects.isNull(bir.getBdbInfo())
													+ ", BdbInfo Type is null : "
													+ (Objects.nonNull(bir.getBdbInfo())
															&& Objects.isNull(bir.getBdbInfo().getType()))
													+ ", bdbInfo Subtype is null :"
													+ (Objects.nonNull(bir.getBdbInfo())
															&& Objects.isNull(bir.getBdbInfo().getSubtype()))
													+ ", BdbInfo Format is null : "
													+ (Objects.nonNull(bir.getBdbInfo())
															&& Objects.isNull(bir.getBdbInfo().getFormat()))
													+ ", Sb is null : " + Objects.isNull(bir.getSb())
													+ ", SbInfo is null : " + Objects.isNull(bir.getSbInfo())
													+ ", SbInfo Format is null : "
													+ (Objects.isNull(bir.getSbInfo())
															|| (Objects.nonNull(bir.getSbInfo())
																	&& Objects.isNull(bir.getSbInfo().getFormat())))
													+ ", response Bdbinfo Type matches all requested modalities : "
													+ bir.getBdbInfo().getType().stream()
															.allMatch(bioType -> BiometricType
																	.fromValue(bioType.value()) == helper
																			.getBiometricType(result.getModality())))
											.setStatus(status))));
				} catch (Exception e) {
					sdkResults.put(result.getModality(), new SDKResult().setModality(result.getModality())
							.setErrorStackTrace(ExceptionUtils.getStackTrace(e)).setStatus(false));
				}
			}
		}
		results.putAll(sdkResults);
	}

}
