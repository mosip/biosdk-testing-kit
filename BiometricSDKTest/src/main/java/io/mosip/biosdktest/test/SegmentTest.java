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
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.kernel.cbeffutil.impl.CbeffImpl;
import io.mosip.kernel.core.cbeffutil.entity.BIR;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.SingleAnySubtypeType;
import io.mosip.kernel.core.cbeffutil.spi.CbeffUtil;
import io.mosip.kernel.core.exception.ExceptionUtils;

/**
 * @author Manoj SP
 *
 */
@Component
public class SegmentTest {

	@Autowired
	private TestResultBuilder builder;

	@Autowired
	private BioTestHelper helper;

	private CbeffUtil cbeffReader = new CbeffImpl();

	public void segment(String testCaseName, List<String> modalities, String probeFileName) {
		Map<String, SDKResult> results = new HashMap<>();

		List<String> expected = modalities.stream()
				.map(modality -> modality + " -> status : 200" + " BdbInfo, BirInfo, Sb, SbInfo should not be null")
				.collect(Collectors.toList());
		try {
			Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

			if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
					BiometricFunction.SEGMENT)) {
				List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
						+ helper.getProvider(modality).getClass().getName()
						+ "supports Modality : true, Supports Segment : true, getSupportedMethods value contains BiometricType : true")
						.collect(Collectors.toList());
				builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(),
						modalities.stream()
								.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
										+ "supports Modality : " + isModalitySupported.get(modality)
										+ ", Supports Segment : " + isMethodSupported.get(modality)
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

				List<BiometricType> bioTypeList = modalities.stream().map(bioType -> helper.getBiometricType(bioType))
						.collect(Collectors.toList());

				List<List<String>> segmentedSubTypes = new ArrayList<>();

				segment(bioRecordSegments, results, bioTypeList, segmentedSubTypes);

				List<String> actual = results.values().stream()
						.map(sdkResult -> sdkResult.getModality() + " -> "
								+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
										: "status : " + sdkResult.getStatusCode() + " & segmentedSubTypes : "
												+ segmentedSubTypes.toString()))
						.collect(Collectors.toList());

				builder.build(testCaseName, modalities.toString(), "", expected.toString(), actual.toString(),
						results.values().stream().filter(sdkResult -> modalities.contains(sdkResult.getModality()))
								.allMatch(sdkResult -> sdkResult.isStatus() && sdkResult.getStatusCode() >= 200
										&& sdkResult.getStatusCode() <= 299));
			}
		} catch (Exception e) {
			e.printStackTrace();
			builder.build(testCaseName, modalities.toString(), "", expected.toString(), ExceptionUtils.getStackTrace(e),
					false);
		}
	}

	public void segmentInvalidData(String testCaseName, List<String> modalities, String probeFileName) {
		Map<String, SDKResult> results = new HashMap<>();

		List<String> expected = modalities.stream().map(modality -> modality + " -> status : 401 or 405")
				.collect(Collectors.toList());

		try {
			Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

			if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
					BiometricFunction.SEGMENT)) {
				List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
						+ helper.getProvider(modality).getClass().getName()
						+ "supports Modality : true, Supports Segment : true, getSupportedMethods value contains BiometricType : true")
						.collect(Collectors.toList());
				builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(),
						modalities.stream()
								.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
										+ "supports Modality : " + isModalitySupported.get(modality)
										+ ", Supports Segment : " + isMethodSupported.get(modality)
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

				List<BiometricType> bioTypeList = modalities.stream().map(bioType -> helper.getBiometricType(bioType))
						.collect(Collectors.toList());

				List<List<String>> segmentedSubTypes = new ArrayList<>();

				segment(bioRecordSegments, results, bioTypeList, segmentedSubTypes);

				List<String> actual = results.values().stream()
						.map(sdkResult -> sdkResult.getModality() + " -> "
								+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
										: "status : " + sdkResult.getStatusCode() + " & segmentedSubTypes : "
												+ segmentedSubTypes.toString()))
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

	public void segmentNoInputData(String testCaseName, List<String> modalities, String probeFileName) {
		Map<String, SDKResult> results = new HashMap<>();

		List<String> expected = modalities.stream().map(modality -> modality + " -> status : 402 or 405")
				.collect(Collectors.toList());

		try {
			Map<String, Boolean> isModalitySupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupported = new HashMap<>(modalities.size());
			Map<String, Boolean> isMethodSupportsModality = new HashMap<>(modalities.size());

			if (!helper.checkSDKSupport(modalities, isModalitySupported, isMethodSupported, isMethodSupportsModality,
					BiometricFunction.SEGMENT)) {
				List<String> checkSDKSupportExpected = modalities.stream().map(modality -> modality + " -> "
						+ helper.getProvider(modality).getClass().getName()
						+ "supports Modality : true, Supports Segment : true, getSupportedMethods value contains BiometricType : true")
						.collect(Collectors.toList());
				builder.build(testCaseName, modalities.toString(), "", checkSDKSupportExpected.toString(),
						modalities.stream()
								.map(modality -> modality + " -> " + helper.getProvider(modality).getClass().getName()
										+ "supports Modality : " + isModalitySupported.get(modality)
										+ ", Supports Segment : " + isMethodSupported.get(modality)
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

				List<BiometricType> bioTypeList = modalities.stream().map(bioType -> helper.getBiometricType(bioType))
						.collect(Collectors.toList());

				List<List<String>> segmentedSubTypes = new ArrayList<>();

				segment(bioRecordSegments, results, bioTypeList, segmentedSubTypes);

				List<String> actual = results.values().stream()
						.map(sdkResult -> sdkResult.getModality() + " -> "
								+ (Objects.nonNull(sdkResult.getErrorStackTrace()) ? sdkResult.getErrorStackTrace()
										: "status : " + sdkResult.getStatusCode() + " & segmentedSubTypes : "
												+ segmentedSubTypes.toString()))
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

	private void segment(List<io.mosip.kernel.biometrics.entities.BIR> bioRecordSegments,
			Map<String, SDKResult> results, List<BiometricType> bioTypeList, List<List<String>> segmentedSubTypes) {
		Map<String, SDKResult> sdkResults = new HashMap<>();
		for (SDKResult result : results.values()) {
			if (!sdkResults.containsKey(result.getModality()) && Objects.isNull(result.getStatusCode())
					&& Objects.isNull(result.getErrorStackTrace())) {
				try {
					Response<BiometricRecord> segmentedDataResponse = helper.getProvider(result.getModality())
							.segment(bioRecordSegments.get(0), bioTypeList, null);
					if (segmentedDataResponse.getStatusCode() >= 200 && segmentedDataResponse.getStatusCode() <= 299
							&& Objects.nonNull(segmentedDataResponse.getResponse())
							&& Objects.nonNull(segmentedDataResponse.getResponse().getSegments())) {
						segmentedDataResponse.getResponse().getSegments().forEach(segmentedBir -> segmentedBir
								.getBdbInfo().getSubtype().forEach(subType -> SingleAnySubtypeType.fromValue(subType)));
						segmentedDataResponse.getResponse().getSegments()
								.forEach(bir -> segmentedSubTypes.add(bir.getBdbInfo().getSubtype()));
						segmentedDataResponse.getResponse().getSegments()
								.forEach(bir -> bir.getBdbInfo().getType().stream().forEach(type -> {
									boolean status = Objects.nonNull(bir.getBirInfo())
											&& Objects.nonNull(bir.getBirInfo().getPayload())
											&& Objects.nonNull(bir.getBdbInfo())
											&& Objects.nonNull(bir.getBdbInfo().getType())
											&& Objects.nonNull(bir.getBdbInfo().getSubtype())
											&& Objects.nonNull(bir.getBdbInfo().getFormat())
											&& Objects.nonNull(bir.getSb()) && Objects.nonNull(bir.getSbInfo())
											&& Objects.nonNull(bir.getSbInfo().getFormat());
									sdkResults.put(type.value().toLowerCase(), new SDKResult()
											.setModality(result.getModality())
											.setStatusCode(segmentedDataResponse.getStatusCode())
											.setErrorStackTrace(status ? null
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
																	|| (Objects.nonNull(bir.getSbInfo()) && Objects
																			.isNull(bir.getSbInfo().getFormat())))
															+ " & status: " + segmentedDataResponse.getStatusCode())
											.setStatus(status));
								}));
					} else if (Objects.isNull(segmentedDataResponse.getResponse())) {
						sdkResults.put(result.getModality(), new SDKResult().setModality(result.getModality())
								.setStatusCode(segmentedDataResponse.getStatusCode())
								.setErrorStackTrace(
										"Response is null & status code : " + segmentedDataResponse.getStatusCode())
								.setStatus(false));
					} else if (Objects.isNull(segmentedDataResponse.getResponse().getSegments())) {
						sdkResults.put(result.getModality(),
								new SDKResult().setModality(result.getModality())
										.setStatusCode(segmentedDataResponse.getStatusCode())
										.setErrorStackTrace("No segments returned in response & status code : "
												+ segmentedDataResponse.getStatusCode())
										.setStatus(false));
					} else {
						sdkResults.put(result.getModality(),
								new SDKResult().setModality(result.getModality()).setExtracted(false)
										.setStatusCode(segmentedDataResponse.getStatusCode()).setStatus(false));
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

}
