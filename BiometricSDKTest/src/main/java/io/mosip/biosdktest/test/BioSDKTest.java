package io.mosip.biosdktest.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import io.mosip.biosdktest.config.TestResultBuilder;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.Match;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.MatchDecision;
import io.mosip.kernel.biometrics.model.QualityCheck;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.kernel.biometrics.spi.IBioApi;
import io.mosip.kernel.cbeffutil.impl.CbeffImpl;
import io.mosip.kernel.core.cbeffutil.entity.BIR;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.SingleType;
import io.mosip.kernel.core.cbeffutil.spi.CbeffUtil;
import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ExceptionUtils;

/**
 * @author Manoj SP
 *
 */
@Component
public class BioSDKTest {

	private static final String QUALITY_CHECK_THRESHOLD_VALUE = ".qualitycheck.threshold.value";

	@Autowired(required = false)
	@Qualifier("face")
	private IBioApi face;

	@Autowired(required = false)
	@Qualifier("iris")
	private IBioApi iris;

	@Autowired(required = false)
	@Qualifier("finger")
	private IBioApi finger;

	@Autowired(required = false)
	@Qualifier("composite")
	private IBioApi composite;

	@Autowired
	private Environment env;

	@Autowired
	private TestResultBuilder builder;

	@Autowired
	private ResourceLoader resourceLoader;

	private static Map<String, byte[]> inputFiles = new HashMap<>();

	private CbeffUtil cbeffReader = new CbeffImpl();

	public void qualityCheckSuccess(String testCaseName, String modality, String probeFileName) throws Exception {
		List<BIR> birs = cbeffReader.convertBIRTypeToBIR(
				cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()));
		List<io.mosip.kernel.biometrics.entities.BIR> bioRecordSegments = birs.stream()
				.map(bir -> convertToBiometricRecordBIR(bir)).collect(Collectors.toList());

		try {
			BiometricRecord biometricRecord = new BiometricRecord();
			biometricRecord.setSegments(bioRecordSegments);
			Response<QualityCheck> quality = getProvider(modality).checkQuality(biometricRecord,
					Collections.singletonList(getBiometricType(modality)), null);
			if (quality.getStatusCode() >= 200 && quality.getStatusCode() <= 299) {
				float score = quality.getResponse().getScores().get(getBiometricType(modality)).getScore();
				builder.build(testCaseName, modality, "",
						"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
						String.valueOf(score),
						(score >= env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE, Integer.class)));
			} else {
				builder.build(testCaseName, modality, "",
						"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
						"Failed with status code : " + quality.getStatusCode(), false);
			}
		} catch (Exception e) {
			builder.build(testCaseName, modality, "",
					"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
					ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void qualityCheckFail(String testCaseName, String modality, String probeFileName) throws Exception {
		List<BIR> birs = cbeffReader.convertBIRTypeToBIR(
				cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()));
		List<io.mosip.kernel.biometrics.entities.BIR> bioRecordSegments = birs.stream()
				.map(bir -> convertToBiometricRecordBIR(bir)).collect(Collectors.toList());
		try {
			BiometricRecord biometricRecord = new BiometricRecord();
			biometricRecord.setSegments(bioRecordSegments);
			Response<QualityCheck> quality = getProvider(modality).checkQuality(biometricRecord,
					Collections.singletonList(getBiometricType(modality)), null);
			if (quality.getStatusCode() >= 200 && quality.getStatusCode() <= 299) {
				float score = quality.getResponse().getScores().get(getBiometricType(modality)).getScore();
				builder.build(testCaseName, modality, "",
						"qualityScore <= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
						String.valueOf(score),
						(score <= env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE, Integer.class)));
			} else {
				builder.build(testCaseName, modality, "",
						"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
						"Failed with status code : " + quality.getStatusCode(), false);

			}
		} catch (Exception e) {
			builder.build(testCaseName, modality, "",
					"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
					ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void qualityCheckInvalidData(String testCaseName, String modality, String probeFileName) throws Exception {
		List<BIR> birs = cbeffReader.convertBIRTypeToBIR(
				cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()));
		List<io.mosip.kernel.biometrics.entities.BIR> bioRecordSegments = birs.stream()
				.map(bir -> convertToBiometricRecordBIR(bir)).collect(Collectors.toList());
		try {
			BiometricRecord biometricRecord = new BiometricRecord();
			biometricRecord.setSegments(bioRecordSegments);
			Response<QualityCheck> quality = getProvider(modality).checkQuality(biometricRecord,
					Collections.singletonList(getBiometricType(modality)), null);
			if (quality.getStatusCode() == 401 || quality.getStatusCode() == 403) {
				builder.build(testCaseName, modality, "", "Failed with status code : 401 or 403",
						"Failed with status code : " + quality.getStatusCode(), true);
			} else {
				builder.build(testCaseName, modality, "", "Failed with status code : 401 or 403",
						"status code : " + quality.getStatusCode(), false);
			}
		} catch (Exception e) {
			builder.build(testCaseName, modality, "", "Failed with status code : 401 or 403",
					ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void qualityCheckNoInputData(String testCaseName, String modality, String probeFileName) throws Exception {
		List<BIR> birs = cbeffReader.convertBIRTypeToBIR(
				cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()));
		List<io.mosip.kernel.biometrics.entities.BIR> bioRecordSegments = birs.stream()
				.map(bir -> convertToBiometricRecordBIR(bir)).collect(Collectors.toList());
		try {
			BiometricRecord biometricRecord = new BiometricRecord();
			biometricRecord.setSegments(bioRecordSegments);
			Response<QualityCheck> quality = getProvider(modality).checkQuality(biometricRecord,
					Collections.singletonList(getBiometricType(modality)), null);
			if (quality.getStatusCode() == 402 || quality.getStatusCode() == 403) {
				builder.build(testCaseName, modality, "", "Failed with status code : 402 or 403",
						"Failed with status code : " + quality.getStatusCode(), true);
			} else {
				builder.build(testCaseName, modality, "", "Failed with status code : 402 or 403",
						"status code : " + quality.getStatusCode(), false);
			}
		} catch (Exception e) {
			builder.build(testCaseName, modality, "", "Failed with status code : 402 or 403",
					ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void matchSuccess(String testCaseName, String modality, String probeFileName, String galleryFileName)
			throws Exception {
		BIR probeBir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()))
				.get(0);
		io.mosip.kernel.biometrics.entities.BIR probeBirbioRecordSegments = convertToBiometricRecordBIR(probeBir);
		List<BIR> galleryBir = cbeffReader.convertBIRTypeToBIR(
				cbeffReader.getBIRDataFromXMLType(getInputFile(galleryFileName), getType(modality).value()));
		List<io.mosip.kernel.biometrics.entities.BIR> galleryBirbioRecordSegments = galleryBir.stream()
				.map(bir -> convertToBiometricRecordBIR(bir)).collect(Collectors.toList());

		try {
			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(Collections.singletonList(probeBirbioRecordSegments));
			BiometricRecord galleryBirBiometricRecord = new BiometricRecord();
			galleryBirBiometricRecord.setSegments(galleryBirbioRecordSegments);
			Response<MatchDecision[]> match = getProvider(modality).match(probeBirBiometricRecord,
					new BiometricRecord[] { galleryBirBiometricRecord },
					Collections.singletonList(getBiometricType(modality)), null);
			if (match.getStatusCode() >= 200 && match.getStatusCode() <= 299
					&& Arrays.asList(match.getResponse()).stream().anyMatch(matchDecision -> matchDecision
							.getDecisions().get(getBiometricType(modality)).getMatch() == Match.MATCHED)) {
				builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
						"match is successful with status 2XX",
						"match is successful with status " + match.getStatusCode(), true);
			} else {
				builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
						"match is successful with status 2XX",
						"Failed with status code : " + match.getStatusCode() + " and MatchDecision Match.MATCHED "
								+ (Arrays.asList(match.getResponse()).stream()
										.anyMatch(matchDecision -> matchDecision.getDecisions()
												.get(getBiometricType(modality)).getMatch() == Match.MATCHED)
														? " returned"
														: " not returned"),
						false);
			}
		} catch (Exception e) {
			builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(), "Match is successful",
					ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void matchFail(String testCaseName, String modality, String probeFileName, String galleryFileName)
			throws Exception {
		BIR probeBir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()))
				.get(0);
		io.mosip.kernel.biometrics.entities.BIR probeBirbioRecordSegments = convertToBiometricRecordBIR(probeBir);
		List<BIR> galleryBir = cbeffReader.convertBIRTypeToBIR(
				cbeffReader.getBIRDataFromXMLType(getInputFile(galleryFileName), getType(modality).value()));
		List<io.mosip.kernel.biometrics.entities.BIR> galleryBirbioRecordSegments = galleryBir.stream()
				.map(bir -> convertToBiometricRecordBIR(bir)).collect(Collectors.toList());

		try {
			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(Collections.singletonList(probeBirbioRecordSegments));
			BiometricRecord galleryBirBiometricRecord = new BiometricRecord();
			galleryBirBiometricRecord.setSegments(galleryBirbioRecordSegments);
			Response<MatchDecision[]> match = getProvider(modality).match(probeBirBiometricRecord,
					new BiometricRecord[] { galleryBirBiometricRecord },
					Collections.singletonList(getBiometricType(modality)), null);
			if ((match.getStatusCode() >= 200 && match.getStatusCode() <= 299 || match.getStatusCode() == 405)
					&& !Arrays.asList(match.getResponse()).stream().anyMatch(matchDecision -> matchDecision
							.getDecisions().get(getBiometricType(modality)).getMatch() == Match.MATCHED)) {
				builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
						"Match failed - Match.MATCHED should not be returned in MatchDecision or status code: 405",
						"Match failed - Match.MATCHED not returned in MatchDecision and status code is : "
								+ match.getStatusCode(),
						true);
			} else {
				builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
						"Match failed - Match.MATCHED should not be returned in MatchDecision or status code: 405",
						"Match failed - Match.MATCHED "
								+ (Arrays.asList(match.getResponse()).stream()
										.anyMatch(matchDecision -> matchDecision.getDecisions()
												.get(getBiometricType(modality)).getMatch() == Match.MATCHED) ? ""
														: "not")
								+ " returned in MatchDecision and Failed with status code : " + match.getStatusCode(),
						false);
			}
		} catch (Exception e) {
			builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
					"Match failed - Match.MATCHED should not be returned in MatchDecision or status code: 405",
					ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void matchInvalidData(String testCaseName, String modality, String probeFileName, String galleryFileName)
			throws Exception {
		BIR probeBir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()))
				.get(0);
		io.mosip.kernel.biometrics.entities.BIR probeBirbioRecordSegments = convertToBiometricRecordBIR(probeBir);
		List<BIR> galleryBir = cbeffReader.convertBIRTypeToBIR(
				cbeffReader.getBIRDataFromXMLType(getInputFile(galleryFileName), getType(modality).value()));
		List<io.mosip.kernel.biometrics.entities.BIR> galleryBirbioRecordSegments = galleryBir.stream()
				.map(bir -> convertToBiometricRecordBIR(bir)).collect(Collectors.toList());

		try {
			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(Collections.singletonList(probeBirbioRecordSegments));
			BiometricRecord galleryBirBiometricRecord = new BiometricRecord();
			galleryBirBiometricRecord.setSegments(galleryBirbioRecordSegments);
			Response<MatchDecision[]> match = getProvider(modality).match(probeBirBiometricRecord,
					new BiometricRecord[] { galleryBirBiometricRecord },
					Collections.singletonList(getBiometricType(modality)), null);
			if (match.getStatusCode() == 401 || match.getStatusCode() == 405) {
				builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
						"Match failed with status code: 401 or 405",
						"Match failed with status code: " + match.getStatusCode(), true);
			} else {
				builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
						"Match failed with status code: 401 or 405",
						"Match failed with status code : " + match.getStatusCode(), false);
			}
		} catch (Exception e) {
			builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
					"Match failed with status code: 401 or 405", ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void matchNoInputData(String testCaseName, String modality, String probeFileName, String galleryFileName)
			throws Exception {
		BIR probeBir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()))
				.get(0);
		io.mosip.kernel.biometrics.entities.BIR probeBirbioRecordSegments = convertToBiometricRecordBIR(probeBir);
		List<BIR> galleryBir = cbeffReader.convertBIRTypeToBIR(
				cbeffReader.getBIRDataFromXMLType(getInputFile(galleryFileName), getType(modality).value()));
		List<io.mosip.kernel.biometrics.entities.BIR> galleryBirbioRecordSegments = galleryBir.stream()
				.map(bir -> convertToBiometricRecordBIR(bir)).collect(Collectors.toList());

		try {
			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(Collections.singletonList(probeBirbioRecordSegments));
			BiometricRecord galleryBirBiometricRecord = new BiometricRecord();
			galleryBirBiometricRecord.setSegments(galleryBirbioRecordSegments);
			Response<MatchDecision[]> match = getProvider(modality).match(probeBirBiometricRecord,
					new BiometricRecord[] { galleryBirBiometricRecord },
					Collections.singletonList(getBiometricType(modality)), null);
			if (match.getStatusCode() == 402 || match.getStatusCode() == 405) {
				builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
						"Match failed with status code: 402 or 405",
						"Match failed with status code: " + match.getStatusCode(), true);
			} else {
				builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
						"Match failed with status code: 402 or 405",
						"Match failed with status code : " + match.getStatusCode(), false);
			}
		} catch (Exception e) {
			builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
					"Match failed with status code: 402 or 405", ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void extractTemplateAndCheckQualitySuccess(String testCaseName, String modality, String probeFileName)
			throws Exception {
		List<BIR> birs = cbeffReader.convertBIRTypeToBIR(
				cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()));
		List<io.mosip.kernel.biometrics.entities.BIR> bioRecordSegments = birs.stream()
				.map(bir -> convertToBiometricRecordBIR(bir)).collect(Collectors.toList());
		try {
			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(bioRecordSegments);
			Response<BiometricRecord> extractedTemplate = getProvider(modality).extractTemplate(probeBirBiometricRecord,
					Collections.singletonList(getBiometricType(modality)), null);
			if (!(extractedTemplate.getStatusCode() >= 200 && extractedTemplate.getStatusCode() <= 299)) {
				builder.build(testCaseName, modality, "",
						"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
						"ExtractedTemplate Failed with status code : " + extractedTemplate.getStatusCode(), false);
			} else {
				BiometricRecord extractedBioRecord = new BiometricRecord();
				probeBirBiometricRecord.setSegments(extractedTemplate.getResponse().getSegments());
				Response<QualityCheck> quality = getProvider(modality).checkQuality(extractedBioRecord,
						Collections.singletonList(getBiometricType(modality)), null);
				if (quality.getStatusCode() >= 200 && quality.getStatusCode() <= 299) {
					float score = quality.getResponse().getScores().get(getBiometricType(modality)).getScore();
					builder.build(testCaseName, modality, "",
							"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
							String.valueOf(score),
							(score >= env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE, Integer.class)));
				} else {
					builder.build(testCaseName, modality, "",
							"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
							"Failed with status code : " + quality.getStatusCode(), false);
				}
			}
		} catch (Exception e) {
			builder.build(testCaseName, modality, "",
					"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
					ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void extractTemplateAndCheckQualityFail(String testCaseName, String modality, String probeFileName)
			throws Exception {
		List<BIR> birs = cbeffReader.convertBIRTypeToBIR(
				cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()));
		List<io.mosip.kernel.biometrics.entities.BIR> bioRecordSegments = birs.stream()
				.map(bir -> convertToBiometricRecordBIR(bir)).collect(Collectors.toList());
		try {
			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(bioRecordSegments);
			Response<BiometricRecord> extractedTemplate = getProvider(modality).extractTemplate(probeBirBiometricRecord,
					Collections.singletonList(getBiometricType(modality)), null);
			if (!(extractedTemplate.getStatusCode() >= 200 && extractedTemplate.getStatusCode() <= 299)) {
				builder.build(testCaseName, modality, "",
						"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
						"ExtractedTemplate Failed with status code : " + extractedTemplate.getStatusCode(), false);
			} else {
				BiometricRecord extractedBioRecord = new BiometricRecord();
				probeBirBiometricRecord.setSegments(extractedTemplate.getResponse().getSegments());
				Response<QualityCheck> quality = getProvider(modality).checkQuality(extractedBioRecord,
						Collections.singletonList(getBiometricType(modality)), null);
				if (quality.getStatusCode() >= 200 && quality.getStatusCode() <= 299) {
					float score = quality.getResponse().getScores().get(getBiometricType(modality)).getScore();
					builder.build(testCaseName, modality, "",
							"qualityScore <= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
							String.valueOf(score),
							(score <= env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE, Integer.class)));
				} else {
					builder.build(testCaseName, modality, "",
							"qualityScore <= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
							"Failed with status code : " + quality.getStatusCode(), false);
				}
			}
		} catch (Exception e) {
			builder.build(testCaseName, modality, "",
					"qualityScore <= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
					ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void extractTemplateInvalidData(String testCaseName, String modality, String probeFileName)
			throws Exception {
		List<BIR> birs = cbeffReader.convertBIRTypeToBIR(
				cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()));
		List<io.mosip.kernel.biometrics.entities.BIR> bioRecordSegments = birs.stream()
				.map(bir -> convertToBiometricRecordBIR(bir)).collect(Collectors.toList());
		try {
			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(bioRecordSegments);
			Response<BiometricRecord> extractedTemplate = getProvider(modality).extractTemplate(probeBirBiometricRecord,
					Collections.singletonList(getBiometricType(modality)), null);
			if (extractedTemplate.getStatusCode() == 401) {
				builder.build(testCaseName, modality, "", "Failed with status code : 401",
						"Failed with status code : " + extractedTemplate.getStatusCode(), true);
			} else {
				builder.build(testCaseName, modality, "", "Failed with status code : 401",
						"status code : " + extractedTemplate.getStatusCode(), false);
			}
		} catch (Exception e) {
			builder.build(testCaseName, modality, "", "Failed with status code : 401", ExceptionUtils.getStackTrace(e),
					false);
		}
	}

	public void extractTemplateNoInputData(String testCaseName, String modality, String probeFileName)
			throws Exception {
		List<BIR> birs = cbeffReader.convertBIRTypeToBIR(
				cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()));
		List<io.mosip.kernel.biometrics.entities.BIR> bioRecordSegments = birs.stream()
				.map(bir -> convertToBiometricRecordBIR(bir)).collect(Collectors.toList());
		try {
			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(bioRecordSegments);
			Response<BiometricRecord> extractedTemplate = getProvider(modality).extractTemplate(probeBirBiometricRecord,
					Collections.singletonList(getBiometricType(modality)), null);
			if (extractedTemplate.getStatusCode() == 402) {
				builder.build(testCaseName, modality, "", "Failed with status code : 402",
						"Failed with status code : " + extractedTemplate.getStatusCode(), true);
			} else {
				builder.build(testCaseName, modality, "", "Failed with status code : 402",
						"status code : " + extractedTemplate.getStatusCode(), false);
			}
		} catch (Exception e) {
			builder.build(testCaseName, modality, "", "Failed with status code : 402", ExceptionUtils.getStackTrace(e),
					false);
		}
	}

	public void extractAndMatchFMRSuccess(String testCaseName, String modality, String probeFileName,
			String galleryFileName) throws Exception {
		BIR probeBir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()))
				.get(0);
		io.mosip.kernel.biometrics.entities.BIR probeBirbioRecordSegments = convertToBiometricRecordBIR(probeBir);
		List<BIR> galleryBir = cbeffReader.convertBIRTypeToBIR(
				cbeffReader.getBIRDataFromXMLType(getInputFile(galleryFileName), getType(modality).value()));
		List<io.mosip.kernel.biometrics.entities.BIR> galleryBirbioRecordSegments = galleryBir.stream()
				.map(bir -> convertToBiometricRecordBIR(bir)).collect(Collectors.toList());
		BiometricRecord galleryFMRBioRecord = new BiometricRecord();
		try {
			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(Collections.singletonList(probeBirbioRecordSegments));
			BiometricRecord galleryBirBiometricRecord = new BiometricRecord();
			galleryBirBiometricRecord.setSegments(galleryBirbioRecordSegments);

			Response<BiometricRecord> galleryFMRResponse = getProvider(modality).extractTemplate(
					galleryBirBiometricRecord, Collections.singletonList(getBiometricType(modality)), null);
			if (galleryFMRResponse.getStatusCode() >= 200 && galleryFMRResponse.getStatusCode() <= 299) {
				galleryFMRBioRecord = galleryFMRResponse.getResponse();
			} else {
				builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
						"Extraction is successful",
						"Extraction failed with status code " + galleryFMRResponse.getStatusCode(), false);
			}

			Response<BiometricRecord> probeFMRBirResponse = getProvider(modality).extractTemplate(
					probeBirBiometricRecord, Collections.singletonList(getBiometricType(modality)), null);
			if (probeFMRBirResponse.getStatusCode() >= 200 && probeFMRBirResponse.getStatusCode() <= 299) {
				BiometricRecord probeFMRBioRecord = probeFMRBirResponse.getResponse();
				Response<MatchDecision[]> match = getProvider(modality).match(probeFMRBioRecord,
						new BiometricRecord[] { galleryFMRBioRecord },
						Collections.singletonList(getBiometricType(modality)), null);
				if (match.getStatusCode() >= 200 && match.getStatusCode() <= 299
						&& Arrays.asList(match.getResponse()).stream().anyMatch(matchDecision -> matchDecision
								.getDecisions().get(getBiometricType(modality)).getMatch() == Match.MATCHED)) {
					builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
							"match is successful", "match is successful", true);
				} else {
					builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
							"Match is successful", "Failed with status code : " + match.getStatusCode(), false);
				}
			} else {
				builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
						"Extraction is successful",
						"Extraction failed with status code " + probeFMRBirResponse.getStatusCode(), false);
			}
		} catch (Exception e) {
			builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
					"Extraction is successful", ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void extractAndMatchFMRFail(String testCaseName, String modality, String probeFileName,
			String galleryFileName) throws Exception {
		BIR probeBir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()))
				.get(0);
		io.mosip.kernel.biometrics.entities.BIR probeBirbioRecordSegments = convertToBiometricRecordBIR(probeBir);
		List<BIR> galleryBir = cbeffReader.convertBIRTypeToBIR(
				cbeffReader.getBIRDataFromXMLType(getInputFile(galleryFileName), getType(modality).value()));
		List<io.mosip.kernel.biometrics.entities.BIR> galleryBirbioRecordSegments = galleryBir.stream()
				.map(bir -> convertToBiometricRecordBIR(bir)).collect(Collectors.toList());
		BiometricRecord galleryFMRBioRecord = new BiometricRecord();
		try {
			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(Collections.singletonList(probeBirbioRecordSegments));
			BiometricRecord galleryBirBiometricRecord = new BiometricRecord();
			galleryBirBiometricRecord.setSegments(galleryBirbioRecordSegments);

			Response<BiometricRecord> galleryFMRResponse = getProvider(modality).extractTemplate(
					galleryBirBiometricRecord, Collections.singletonList(getBiometricType(modality)), null);
			if (galleryFMRResponse.getStatusCode() >= 200 && galleryFMRResponse.getStatusCode() <= 299) {
				galleryFMRBioRecord = galleryFMRResponse.getResponse();
			} else {
				builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
						"Extraction is successful",
						"Extraction failed with status code " + galleryFMRResponse.getStatusCode(), false);
			}
			Response<BiometricRecord> probeFMRBirResponse = getProvider(modality).extractTemplate(
					probeBirBiometricRecord, Collections.singletonList(getBiometricType(modality)), null);
			if (probeFMRBirResponse.getStatusCode() >= 200 && probeFMRBirResponse.getStatusCode() <= 299) {
				Response<MatchDecision[]> match = getProvider(modality).match(probeFMRBirResponse.getResponse(),
						new BiometricRecord[] { galleryFMRBioRecord },
						Collections.singletonList(getBiometricType(modality)), null);
				if (match.getStatusCode() >= 200 && match.getStatusCode() <= 299
						&& !Arrays.asList(match.getResponse()).stream().anyMatch(matchDecision -> matchDecision
								.getDecisions().get(getBiometricType(modality)).getMatch() == Match.MATCHED)) {
					builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
							"Match failed - Match.MATCHED is not returned as MatchDecision",
							"Match failed - Match.MATCHED is not returned as MatchDecision", true);
				} else {
					builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
							"Match.MATCHED is not returned as MatchDecision",
							"Failed with status code : " + match.getStatusCode(), false);
				}
			} else {
				builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
						"Extraction is successful",
						"Extraction failed with status code " + probeFMRBirResponse.getStatusCode(), false);
			}
		} catch (Exception e) {
			builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
					"Extract and Match is successful", ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void segment(String testCaseName, String modality, String probeFileName) throws Exception {
		BIR bir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()))
				.get(0);
		io.mosip.kernel.biometrics.entities.BIR probeBirbioRecordSegments = convertToBiometricRecordBIR(bir);
		try {
			Response<BiometricRecord> segmentedDataResponse = getProvider(modality).segment(probeBirbioRecordSegments,
					Collections.singletonList(getBiometricType(modality)), null);
			if (segmentedDataResponse.getStatusCode() >= 200 && segmentedDataResponse.getStatusCode() <= 299) {
				segmentedDataResponse.getResponse().getSegments().forEach(segmentedBir -> segmentedBir.getBdbInfo()
						.getSubtype().forEach(subType -> SingleType.fromValue(subType)));
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Any value from [Left, Right, Thumb, IndexFinger, MiddleFinger, RingFinger, LittleFinger]",
						"Any value from [Left, Right, Thumb, IndexFinger, MiddleFinger, RingFinger, LittleFinger]",
						true);
			} else {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Any value from [Left, Right, Thumb, IndexFinger, MiddleFinger, RingFinger, LittleFinger]",
						"Segment failed with status code " + segmentedDataResponse.getStatusCode(), false);
			}
		} catch (IllegalArgumentException e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Any value from [Left, Right, Thumb, IndexFinger, MiddleFinger, RingFinger, LittleFinger]",
					ExceptionUtils.getStackTrace(e), false);
		} catch (Exception e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Any value from [Left, Right, Thumb, IndexFinger, MiddleFinger, RingFinger, LittleFinger]",
					ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void segmentInvalidData(String testCaseName, String modality, String probeFileName) throws Exception {
		BIR bir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()))
				.get(0);
		io.mosip.kernel.biometrics.entities.BIR probeBirbioRecordSegments = convertToBiometricRecordBIR(bir);
		try {
			Response<BiometricRecord> segmentedDataResponse = getProvider(modality).segment(probeBirbioRecordSegments,
					Collections.singletonList(getBiometricType(modality)), null);
			if (segmentedDataResponse.getStatusCode() == 401) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Segment failed with status code : 401",
						"Segment failed with status code : " + segmentedDataResponse.getStatusCode(), true);
			} else {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Segment failed with status code : 401",
						"Segment failed with status code : " + segmentedDataResponse.getStatusCode(), false);
			}
		} catch (IllegalArgumentException e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Segment failed with status code : 401", ExceptionUtils.getStackTrace(e), false);
		} catch (Exception e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Segment failed with status code : 401", ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void segmentNoInputData(String testCaseName, String modality, String probeFileName) throws Exception {
		BIR bir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()))
				.get(0);
		io.mosip.kernel.biometrics.entities.BIR probeBirbioRecordSegments = convertToBiometricRecordBIR(bir);
		try {
			Response<BiometricRecord> segmentedDataResponse = getProvider(modality).segment(probeBirbioRecordSegments,
					Collections.singletonList(getBiometricType(modality)), null);
			if (segmentedDataResponse.getStatusCode() == 402) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Segment failed with status code : 402",
						"Segment failed with status code : " + segmentedDataResponse.getStatusCode(), true);
			} else {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Segment failed with status code : 402",
						"Segment failed with status code : " + segmentedDataResponse.getStatusCode(), false);
			}
		} catch (IllegalArgumentException e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Segment failed with status code : 402", ExceptionUtils.getStackTrace(e), false);
		} catch (Exception e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Segment failed with status code : 402", ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void convertFormatJPEGToJPEG(String testCaseName, String modality, String probeFileName) throws Exception {
		BIR bir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()))
				.get(0);
		io.mosip.kernel.biometrics.entities.BIR probeBirbioRecordSegments = convertToBiometricRecordBIR(bir);
		try {
			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(Collections.singletonList(probeBirbioRecordSegments));
			BiometricRecord response = getProvider(modality).convertFormat(probeBirBiometricRecord, "JPEG", "JPEG",
					null, null, Collections.singletonList(getBiometricType(modality)));
			if (response.getSegments().stream().anyMatch(segmentedBir -> segmentedBir.getBdbInfo().getType().stream()
					.allMatch(type -> BiometricType.fromValue(type.value()) == getBiometricType(modality)))) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Any value from BiometricType", "Any value from BiometricType", true);

			} else {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Any value from BiometricType", "BiometricType in response is not matching", true);
			}
		} catch (IllegalArgumentException e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Any value from BiometricType", ExceptionUtils.getStackTrace(e), false);
		} catch (Exception e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Any value from BiometricType", ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void convertFormatJPEGToBMP(String testCaseName, String modality, String probeFileName) throws Exception {
		BIR bir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()))
				.get(0);
		io.mosip.kernel.biometrics.entities.BIR probeBirbioRecordSegments = convertToBiometricRecordBIR(bir);
		try {
			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(Collections.singletonList(probeBirbioRecordSegments));
			BiometricRecord response = getProvider(modality).convertFormat(probeBirBiometricRecord, "JPEG", "BMP", null,
					null, Collections.singletonList(getBiometricType(modality)));
			if (response.getSegments().stream().anyMatch(segmentedBir -> segmentedBir.getBdbInfo().getType().stream()
					.allMatch(type -> BiometricType.fromValue(type.value()) == getBiometricType(modality)))) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Any value from BiometricType", "Any value from BiometricType", true);

			} else {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Any value from BiometricType", "BiometricType in response is not matching", true);
			}
		} catch (IllegalArgumentException e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Any value from BiometricType", ExceptionUtils.getStackTrace(e), false);
		} catch (Exception e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Any value from BiometricType", ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void convertFormatJPEGToWSQ(String testCaseName, String modality, String probeFileName) throws Exception {
		BIR bir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()))
				.get(0);
		io.mosip.kernel.biometrics.entities.BIR probeBirbioRecordSegments = convertToBiometricRecordBIR(bir);
		try {
			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(Collections.singletonList(probeBirbioRecordSegments));
			BiometricRecord response = getProvider(modality).convertFormat(probeBirBiometricRecord, "JPEG", "WSQ", null,
					null, Collections.singletonList(getBiometricType(modality)));
			if (response.getSegments().stream().anyMatch(segmentedBir -> segmentedBir.getBdbInfo().getType().stream()
					.allMatch(type -> BiometricType.fromValue(type.value()) == getBiometricType(modality)))) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Any value from BiometricType", "Any value from BiometricType", true);

			} else {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Any value from BiometricType", "BiometricType in response is not matching", true);
			}
		} catch (IllegalArgumentException e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Any value from BiometricType", ExceptionUtils.getStackTrace(e), false);
		} catch (Exception e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Any value from BiometricType", ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void convertFormatBMPToJPEG(String testCaseName, String modality, String probeFileName) throws Exception {
		BIR bir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()))
				.get(0);
		io.mosip.kernel.biometrics.entities.BIR probeBirbioRecordSegments = convertToBiometricRecordBIR(bir);
		try {
			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(Collections.singletonList(probeBirbioRecordSegments));
			BiometricRecord response = getProvider(modality).convertFormat(probeBirBiometricRecord, "JPEG", "JPEG",
					null, null, Collections.singletonList(getBiometricType(modality)));
			if (response.getSegments().stream().anyMatch(segmentedBir -> segmentedBir.getBdbInfo().getType().stream()
					.allMatch(type -> BiometricType.fromValue(type.value()) == getBiometricType(modality)))) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Any value from BiometricType", "Any value from BiometricType", true);

			} else {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Any value from BiometricType", "BiometricType in response is not matching", true);
			}
		} catch (IllegalArgumentException e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Any value from BiometricType", ExceptionUtils.getStackTrace(e), false);
		} catch (Exception e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Any value from BiometricType", ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void convertFormatBMPToBMP(String testCaseName, String modality, String probeFileName) throws Exception {
		BIR bir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()))
				.get(0);
		io.mosip.kernel.biometrics.entities.BIR probeBirbioRecordSegments = convertToBiometricRecordBIR(bir);
		try {
			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(Collections.singletonList(probeBirbioRecordSegments));
			BiometricRecord response = getProvider(modality).convertFormat(probeBirBiometricRecord, "JPEG", "BMP", null,
					null, Collections.singletonList(getBiometricType(modality)));
			if (response.getSegments().stream().anyMatch(segmentedBir -> segmentedBir.getBdbInfo().getType().stream()
					.allMatch(type -> BiometricType.fromValue(type.value()) == getBiometricType(modality)))) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Any value from BiometricType", "Any value from BiometricType", true);

			} else {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Any value from BiometricType", "BiometricType in response is not matching", true);
			}
		} catch (IllegalArgumentException e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Any value from BiometricType", ExceptionUtils.getStackTrace(e), false);
		} catch (Exception e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Any value from BiometricType", ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void convertFormatBMPToWSQ(String testCaseName, String modality, String probeFileName) throws Exception {
		BIR bir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()))
				.get(0);
		io.mosip.kernel.biometrics.entities.BIR probeBirbioRecordSegments = convertToBiometricRecordBIR(bir);
		try {
			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(Collections.singletonList(probeBirbioRecordSegments));
			BiometricRecord response = getProvider(modality).convertFormat(probeBirBiometricRecord, "JPEG", "WSQ", null,
					null, Collections.singletonList(getBiometricType(modality)));
			if (response.getSegments().stream().anyMatch(segmentedBir -> segmentedBir.getBdbInfo().getType().stream()
					.allMatch(type -> BiometricType.fromValue(type.value()) == getBiometricType(modality)))) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Any value from BiometricType", "Any value from BiometricType", true);

			} else {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Any value from BiometricType", "BiometricType in response is not matching", true);
			}
		} catch (IllegalArgumentException e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Any value from BiometricType", ExceptionUtils.getStackTrace(e), false);
		} catch (Exception e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Any value from BiometricType", ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void convertFormatWSQToJPEG(String testCaseName, String modality, String probeFileName) throws Exception {
		BIR bir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()))
				.get(0);
		io.mosip.kernel.biometrics.entities.BIR probeBirbioRecordSegments = convertToBiometricRecordBIR(bir);
		try {
			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(Collections.singletonList(probeBirbioRecordSegments));
			BiometricRecord response = getProvider(modality).convertFormat(probeBirBiometricRecord, "JPEG", "JPEG",
					null, null, Collections.singletonList(getBiometricType(modality)));
			if (response.getSegments().stream().anyMatch(segmentedBir -> segmentedBir.getBdbInfo().getType().stream()
					.allMatch(type -> BiometricType.fromValue(type.value()) == getBiometricType(modality)))) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Any value from BiometricType", "Any value from BiometricType", true);

			} else {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Any value from BiometricType", "BiometricType in response is not matching", true);
			}
		} catch (IllegalArgumentException e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Any value from BiometricType", ExceptionUtils.getStackTrace(e), false);
		} catch (Exception e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Any value from BiometricType", ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void convertFormatWSQToBMP(String testCaseName, String modality, String probeFileName) throws Exception {
		BIR bir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()))
				.get(0);
		io.mosip.kernel.biometrics.entities.BIR probeBirbioRecordSegments = convertToBiometricRecordBIR(bir);
		try {
			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(Collections.singletonList(probeBirbioRecordSegments));
			BiometricRecord response = getProvider(modality).convertFormat(probeBirBiometricRecord, "JPEG", "BMP", null,
					null, Collections.singletonList(getBiometricType(modality)));
			if (response.getSegments().stream().anyMatch(segmentedBir -> segmentedBir.getBdbInfo().getType().stream()
					.allMatch(type -> BiometricType.fromValue(type.value()) == getBiometricType(modality)))) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Any value from BiometricType", "Any value from BiometricType", true);

			} else {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Any value from BiometricType", "BiometricType in response is not matching", true);
			}
		} catch (IllegalArgumentException e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Any value from BiometricType", ExceptionUtils.getStackTrace(e), false);
		} catch (Exception e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Any value from BiometricType", ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void convertFormatWSQToWSQ(String testCaseName, String modality, String probeFileName) throws Exception {
		BIR bir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()))
				.get(0);
		io.mosip.kernel.biometrics.entities.BIR probeBirbioRecordSegments = convertToBiometricRecordBIR(bir);
		try {
			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(Collections.singletonList(probeBirbioRecordSegments));
			BiometricRecord response = getProvider(modality).convertFormat(probeBirBiometricRecord, "JPEG", "WSQ", null,
					null, Collections.singletonList(getBiometricType(modality)));
			if (response.getSegments().stream().anyMatch(segmentedBir -> segmentedBir.getBdbInfo().getType().stream()
					.allMatch(type -> BiometricType.fromValue(type.value()) == getBiometricType(modality)))) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Any value from BiometricType", "Any value from BiometricType", true);

			} else {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Any value from BiometricType", "BiometricType in response is not matching", true);
			}
		} catch (IllegalArgumentException e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Any value from BiometricType", ExceptionUtils.getStackTrace(e), false);
		} catch (Exception e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Any value from BiometricType", ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void convertFormatInvalidData(String testCaseName, String modality, String probeFileName) throws Exception {
		BIR bir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()))
				.get(0);
		io.mosip.kernel.biometrics.entities.BIR probeBirbioRecordSegments = convertToBiometricRecordBIR(bir);
		try {
			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(Collections.singletonList(probeBirbioRecordSegments));
			getProvider(modality).convertFormat(probeBirBiometricRecord, "JPEG", "WSQ", null, null,
					Collections.singletonList(getBiometricType(modality)));
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Unsupported biometric type/Unsupported image format/Processing error",
					"A value from BiometricType", false);
		} catch (Exception e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Unsupported biometric type/Unsupported image format/Processing error",
					ExceptionUtils.getStackTrace(e), e.getMessage() == "Unsupported biometric type"
							|| e.getMessage() == "Unsupported image format" || e.getMessage() == "Processing error");
		}
	}

	public void convertFormatNoData(String testCaseName, String modality, String probeFileName) throws Exception {
		BIR bir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()))
				.get(0);
		io.mosip.kernel.biometrics.entities.BIR probeBirbioRecordSegments = convertToBiometricRecordBIR(bir);
		try {
			BiometricRecord probeBirBiometricRecord = new BiometricRecord();
			probeBirBiometricRecord.setSegments(Collections.singletonList(probeBirbioRecordSegments));
			getProvider(modality).convertFormat(probeBirBiometricRecord, "JPEG", "WSQ", null, null,
					Collections.singletonList(getBiometricType(modality)));
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Unsupported biometric type/Unsupported image format/Processing error",
					"A value from BiometricType", false);
		} catch (Exception e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Unsupported biometric type/Unsupported image format/Processing error",
					ExceptionUtils.getStackTrace(e), e.getMessage() == "Unsupported biometric type"
							|| e.getMessage() == "Unsupported image format" || e.getMessage() == "Processing error");
		}
	}

	private IBioApi getProvider(String modality) {
		if (modality.equalsIgnoreCase("finger")) {
			if (Objects.isNull(finger)) {
				throw new BaseUncheckedException("Biometric provider for " + modality + " not found.");
			}
			return finger;
		} else if (modality.equalsIgnoreCase("face")) {
			if (Objects.isNull(face)) {
				throw new BaseUncheckedException("Biometric provider for " + modality + " not found.");
			}
			return face;
		} else if (modality.equalsIgnoreCase("iris")) {
			if (Objects.isNull(iris)) {
				throw new BaseUncheckedException("Biometric provider for " + modality + " not found.");
			}
			return iris;
		} else if (modality.equalsIgnoreCase("composite")) {
			if (Objects.isNull(composite)) {
				throw new BaseUncheckedException("Biometric provider for " + modality + " not found.");
			}
			return composite;
		} else {
			throw new BaseUncheckedException("Invalid biometric modality : " + modality);
		}
	}

	private SingleType getType(String type) {
		if (type.equalsIgnoreCase("finger")) {
			return SingleType.FINGER;
		} else if (type.equalsIgnoreCase("face")) {
			return SingleType.FACE;
		} else if (type.equalsIgnoreCase("iris")) {
			return SingleType.IRIS;
		} else {
			throw new BaseUncheckedException("Invalid biometric type : " + type);
		}
	}

	private byte[] getInputFile(String fileName) throws IOException {
		if (inputFiles.containsKey(fileName)) {
			return inputFiles.get(fileName);
		} else {
			inputFiles.put(fileName,
					IOUtils.toByteArray(resourceLoader.getResource("file:" + fileName).getInputStream()));
			return inputFiles.get(fileName);
		}
	}

	private BiometricType getBiometricType(String type) {
		if (type.equalsIgnoreCase("finger")) {
			return BiometricType.FINGER;
		} else if (type.equalsIgnoreCase("face")) {
			return BiometricType.FACE;
		} else if (type.equalsIgnoreCase("iris")) {
			return BiometricType.IRIS;
		} else {
			throw new BaseUncheckedException("Invalid biometric type : " + type);
		}

	}

	private io.mosip.kernel.biometrics.entities.BIR convertToBiometricRecordBIR(BIR bir) {
		List<BiometricType> bioTypes = new ArrayList<>();
		for (SingleType type : bir.getBdbInfo().getType()) {
			bioTypes.add(BiometricType.fromValue(type.value()));
		}

		io.mosip.kernel.biometrics.entities.RegistryIDType format = new io.mosip.kernel.biometrics.entities.RegistryIDType(
				bir.getBdbInfo().getFormat().getOrganization(), bir.getBdbInfo().getFormat().getType());

		io.mosip.kernel.biometrics.constant.QualityType qualityType;

		if (Objects.nonNull(bir.getBdbInfo().getQuality())) {
			io.mosip.kernel.biometrics.entities.RegistryIDType birAlgorithm = new io.mosip.kernel.biometrics.entities.RegistryIDType(
					bir.getBdbInfo().getQuality().getAlgorithm().getOrganization(),
					bir.getBdbInfo().getQuality().getAlgorithm().getType());

			qualityType = new io.mosip.kernel.biometrics.constant.QualityType();
			qualityType.setAlgorithm(birAlgorithm);
			qualityType.setQualityCalculationFailed(bir.getBdbInfo().getQuality().getQualityCalculationFailed());
			qualityType.setScore(bir.getBdbInfo().getQuality().getScore());

		} else {
			qualityType = null;
		}

		io.mosip.kernel.biometrics.entities.VersionType version;
		if (Objects.nonNull(bir.getVersion())) {
			version = new io.mosip.kernel.biometrics.entities.VersionType(bir.getVersion().getMajor(),
					bir.getVersion().getMinor());
		} else {
			version = null;
		}

		io.mosip.kernel.biometrics.entities.VersionType cbeffversion;
		if (Objects.nonNull(bir.getCbeffversion())) {
			cbeffversion = new io.mosip.kernel.biometrics.entities.VersionType(bir.getCbeffversion().getMajor(),
					bir.getCbeffversion().getMinor());
		} else {
			cbeffversion = null;
		}

		io.mosip.kernel.biometrics.constant.PurposeType purposeType;
		if (Objects.nonNull(bir.getBdbInfo().getPurpose())) {
			purposeType = io.mosip.kernel.biometrics.constant.PurposeType
					.fromValue(bir.getBdbInfo().getPurpose().name());
		} else {
			purposeType = null;
		}

		io.mosip.kernel.biometrics.constant.ProcessedLevelType processedLevelType;
		if (Objects.nonNull(bir.getBdbInfo().getLevel())) {
			processedLevelType = io.mosip.kernel.biometrics.constant.ProcessedLevelType
					.fromValue(bir.getBdbInfo().getLevel().name());
		} else {
			processedLevelType = null;
		}

		return new io.mosip.kernel.biometrics.entities.BIR.BIRBuilder().withBdb(bir.getBdb()).withVersion(version)
				.withCbeffversion(cbeffversion)
				.withBirInfo(
						new io.mosip.kernel.biometrics.entities.BIRInfo.BIRInfoBuilder().withIntegrity(true).build())
				.withBdbInfo(new io.mosip.kernel.biometrics.entities.BDBInfo.BDBInfoBuilder().withFormat(format)
						.withType(bioTypes).withQuality(qualityType)
						.withCreationDate(bir.getBdbInfo().getCreationDate()).withIndex(bir.getBdbInfo().getIndex())
						.withPurpose(purposeType).withLevel(processedLevelType)
						.withSubtype(bir.getBdbInfo().getSubtype()).build())
				.build();
	}
}
