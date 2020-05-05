package io.mosip.biosdktest.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import io.mosip.biosdktest.config.TestResultBuilder;
import io.mosip.kernel.cbeffutil.impl.CbeffImpl;
import io.mosip.kernel.core.bioapi.model.MatchDecision;
import io.mosip.kernel.core.bioapi.model.QualityScore;
import io.mosip.kernel.core.bioapi.model.Response;
import io.mosip.kernel.core.bioapi.spi.IBioApi;
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
		for (BIR bir : birs) {
			try {
				Response<QualityScore> quality = getProvider(modality).checkQuality(bir, null);
				if (quality.getStatusCode() >= 200 && quality.getStatusCode() <= 299) {
					float internalScore = quality.getResponse().getScore();
					builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
							"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
							String.valueOf(internalScore), (internalScore >= env
									.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE, Integer.class)));
				} else {
					builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
							"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
							"Failed with status code : " + quality.getStatusCode(), false);
				}
			} catch (Exception e) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
						ExceptionUtils.getStackTrace(e), false);
			}
		}
	}

	public void qualityCheckFail(String testCaseName, String modality, String probeFileName) throws Exception {
		List<BIR> birs = cbeffReader.convertBIRTypeToBIR(
				cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()));
		for (BIR bir : birs) {
			try {
				Response<QualityScore> quality = getProvider(modality).checkQuality(bir, null);
				if (quality.getStatusCode() >= 200 && quality.getStatusCode() <= 299) {
					float internalScore = quality.getResponse().getScore();
					builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
							"qualityScore <= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
							String.valueOf(internalScore), (internalScore <= env
									.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE, Integer.class)));
				} else {
					builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
							"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
							"Failed with status code : " + quality.getStatusCode(), false);

				}
			} catch (Exception e) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
						ExceptionUtils.getStackTrace(e), false);
			}
		}
	}

	public void qualityCheckInvalidData(String testCaseName, String modality, String probeFileName) throws Exception {
		List<BIR> birs = cbeffReader.convertBIRTypeToBIR(
				cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()));
		for (BIR bir : birs) {
			try {
				Response<QualityScore> quality = getProvider(modality).checkQuality(bir, null);
				if (quality.getStatusCode() == 401 || quality.getStatusCode() == 403) {
					builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
							"Failed with status code : 401 or 403",
							"Failed with status code : " + quality.getStatusCode(), true);
				} else {
					builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
							"Failed with status code : 401 or 403", "status code : " + quality.getStatusCode(), false);
				}
			} catch (Exception e) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Failed with status code : 401 or 403", ExceptionUtils.getStackTrace(e), false);
			}
		}
	}

	public void qualityCheckNoInputData(String testCaseName, String modality, String probeFileName) throws Exception {
		List<BIR> birs = cbeffReader.convertBIRTypeToBIR(
				cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()));
		for (BIR bir : birs) {
			try {
				Response<QualityScore> quality = getProvider(modality).checkQuality(bir, null);
				if (quality.getStatusCode() == 402 || quality.getStatusCode() == 403) {
					builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
							"Failed with status code : 402 or 403",
							"Failed with status code : " + quality.getStatusCode(), true);
				} else {
					builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
							"Failed with status code : 402 or 403", "status code : " + quality.getStatusCode(), false);
				}
			} catch (Exception e) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Failed with status code : 402 or 403", ExceptionUtils.getStackTrace(e), false);
			}
		}
	}

	public void matchSuccess(String testCaseName, String modality, String probeFileName, String galleryFileName)
			throws Exception {
		BIR probeBir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()))
				.get(0);
		BIR[] galleryBir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(galleryFileName), getType(modality).value()))
				.toArray(new BIR[] {});
		try {
			Response<MatchDecision[]> match = getProvider(modality).match(probeBir, galleryBir, null);
			if (match.getStatusCode() >= 200 && match.getStatusCode() <= 299
					&& Arrays.asList(match.getResponse()).stream().anyMatch(MatchDecision::isMatch)) {
				builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
						"match is successful", "match is successful", true);
			} else {
				builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
						"Match is successful", "Failed with status code : " + match.getStatusCode(), false);
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
		BIR[] galleryBir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(galleryFileName), getType(modality).value()))
				.toArray(new BIR[] {});
		try {
			Response<MatchDecision[]> match = getProvider(modality).match(probeBir, galleryBir, null);
			if ((match.getStatusCode() >= 200 && match.getStatusCode() <= 299 || match.getStatusCode() == 405)
					&& !Arrays.asList(match.getResponse()).stream().anyMatch(MatchDecision::isMatch)) {
				builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
						"Match failed - No true returned in MatchDecision",
						"Match failed - No true returned in MatchDecision", true);
			} else {
				builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
						"Match failed - No true returned in MatchDecision or status code: 405",
						"Match failed - true returned in MatchDecision or Failed with status code : "
								+ match.getStatusCode(),
						false);
			}
		} catch (Exception e) {
			builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
					"Match failed - No true returned in MatchDecision or status code: 405",
					ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void matchInvalidData(String testCaseName, String modality, String probeFileName, String galleryFileName)
			throws Exception {
		BIR probeBir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()))
				.get(0);
		BIR[] galleryBir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(galleryFileName), getType(modality).value()))
				.toArray(new BIR[] {});
		try {
			Response<MatchDecision[]> match = getProvider(modality).match(probeBir, galleryBir, null);
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
		BIR[] galleryBir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(galleryFileName), getType(modality).value()))
				.toArray(new BIR[] {});
		try {
			Response<MatchDecision[]> match = getProvider(modality).match(probeBir, galleryBir, null);
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
		for (BIR bir : birs) {
			try {
				Response<BIR> extractedTemplate = getProvider(modality).extractTemplate(bir, null);
				if (!(extractedTemplate.getStatusCode() >= 200 && extractedTemplate.getStatusCode() <= 299)) {
					builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
							"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
							"ExtractedTemplate Failed with status code : " + extractedTemplate.getStatusCode(), false);
				} else {
					Response<QualityScore> quality = getProvider(modality).checkQuality(extractedTemplate.getResponse(),
							null);
					if (quality.getStatusCode() >= 200 && quality.getStatusCode() <= 299) {
						float internalScore = quality.getResponse().getScore();
						builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
								"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
								String.valueOf(internalScore), (internalScore >= env
										.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE, Integer.class)));
					} else {
						builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
								"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
								"Failed with status code : " + quality.getStatusCode(), false);
					}
				}
			} catch (Exception e) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
						ExceptionUtils.getStackTrace(e), false);
			}
		}
	}

	public void extractTemplateAndCheckQualityFail(String testCaseName, String modality, String probeFileName)
			throws Exception {
		List<BIR> birs = cbeffReader.convertBIRTypeToBIR(
				cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()));
		for (BIR bir : birs) {
			try {
				Response<BIR> extractedTemplate = getProvider(modality).extractTemplate(bir, null);
				if (!(extractedTemplate.getStatusCode() >= 200 && extractedTemplate.getStatusCode() <= 299)) {
					builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
							"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
							"ExtractedTemplate Failed with status code : " + extractedTemplate.getStatusCode(), false);
				} else {
					Response<QualityScore> quality = getProvider(modality).checkQuality(extractedTemplate.getResponse(),
							null);
					if (quality.getStatusCode() >= 200 && quality.getStatusCode() <= 299) {
						float internalScore = quality.getResponse().getScore();
						builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
								"qualityScore <= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
								String.valueOf(internalScore), (internalScore <= env
										.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE, Integer.class)));
					} else {
						builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
								"qualityScore <= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
								"Failed with status code : " + quality.getStatusCode(), false);
					}
				}
			} catch (Exception e) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"qualityScore <= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
						ExceptionUtils.getStackTrace(e), false);
			}
		}
	}

	public void extractTemplateInvalidData(String testCaseName, String modality, String probeFileName)
			throws Exception {
		List<BIR> birs = cbeffReader.convertBIRTypeToBIR(
				cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()));
		for (BIR bir : birs) {
			try {
				Response<BIR> extractedTemplate = getProvider(modality).extractTemplate(bir, null);
				if (extractedTemplate.getStatusCode() == 401) {
					builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
							"Failed with status code : 401",
							"Failed with status code : " + extractedTemplate.getStatusCode(), true);
				} else {
					builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
							"Failed with status code : 401", "status code : " + extractedTemplate.getStatusCode(),
							false);
				}
			} catch (Exception e) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Failed with status code : 401", ExceptionUtils.getStackTrace(e), false);
			}
		}
	}

	public void extractTemplateNoInputData(String testCaseName, String modality, String probeFileName)
			throws Exception {
		List<BIR> birs = cbeffReader.convertBIRTypeToBIR(
				cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()));
		for (BIR bir : birs) {
			try {
				Response<BIR> extractedTemplate = getProvider(modality).extractTemplate(bir, null);
				if (extractedTemplate.getStatusCode() == 402) {
					builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
							"Failed with status code : 402",
							"Failed with status code : " + extractedTemplate.getStatusCode(), true);
				} else {
					builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
							"Failed with status code : 402", "status code : " + extractedTemplate.getStatusCode(),
							false);
				}
			} catch (Exception e) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Failed with status code : 402", ExceptionUtils.getStackTrace(e), false);
			}
		}
	}

	public void extractAndMatchFMRSuccess(String testCaseName, String modality, String probeFileName,
			String galleryFileName) throws Exception {
		BIR probeBir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()))
				.get(0);
		List<BIR> galleryBir = cbeffReader.convertBIRTypeToBIR(
				cbeffReader.getBIRDataFromXMLType(getInputFile(galleryFileName), getType(modality).value()));
		List<BIR> galleryFMRBir = new ArrayList<>();
		try {
			for (BIR bir : galleryBir) {
				Response<BIR> extractTemplate = getProvider(modality).extractTemplate(bir, null);
				if (extractTemplate.getStatusCode() >= 200 && extractTemplate.getStatusCode() <= 299) {
					galleryFMRBir.add(extractTemplate.getResponse());
				} else {
					builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
							"Extraction is successful",
							"Extraction failed with status code " + extractTemplate.getStatusCode(), false);
				}
			}
			Response<BIR> probeFMRBirResponse = getProvider(modality).extractTemplate(probeBir, null);
			if (probeFMRBirResponse.getStatusCode() >= 200 && probeFMRBirResponse.getStatusCode() <= 299) {
				Response<MatchDecision[]> match = getProvider(modality).match(probeFMRBirResponse.getResponse(),
						galleryFMRBir.toArray(new BIR[] {}), null);
				if (match.getStatusCode() >= 200 && match.getStatusCode() <= 299
						&& Arrays.asList(match.getResponse()).stream().anyMatch(MatchDecision::isMatch)) {
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
		List<BIR> galleryBir = cbeffReader.convertBIRTypeToBIR(
				cbeffReader.getBIRDataFromXMLType(getInputFile(galleryFileName), getType(modality).value()));
		List<BIR> galleryFMRBir = new ArrayList<>();
		try {
			for (BIR bir : galleryBir) {
				Response<BIR> extractTemplate = getProvider(modality).extractTemplate(bir, null);
				if (extractTemplate.getStatusCode() >= 200 && extractTemplate.getStatusCode() <= 299) {
					galleryFMRBir.add(extractTemplate.getResponse());
				} else {
					builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
							"Extraction is successful",
							"Extraction failed with status code " + extractTemplate.getStatusCode(), false);
				}
			}
			Response<BIR> probeFMRBirResponse = getProvider(modality).extractTemplate(probeBir, null);
			if (probeFMRBirResponse.getStatusCode() >= 200 && probeFMRBirResponse.getStatusCode() <= 299) {
				Response<MatchDecision[]> match = getProvider(modality).match(probeFMRBirResponse.getResponse(),
						galleryFMRBir.toArray(new BIR[] {}), null);
				if (match.getStatusCode() >= 200 && match.getStatusCode() <= 299
						&& !Arrays.asList(match.getResponse()).stream().anyMatch(MatchDecision::isMatch)) {
					builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
							"Match failed - No true returned in MatchDecision",
							"Match failed - No true returned in MatchDecision", true);
				} else {
					builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
							"Match failed - No true returned in MatchDecision",
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
		try {
			Response<BIR[]> segmentedDataResponse = getProvider(modality).segment(bir, null);
			if (segmentedDataResponse.getStatusCode() >= 200 && segmentedDataResponse.getStatusCode() <= 299) {
				Arrays.asList(segmentedDataResponse.getResponse()).forEach(segmentedBir -> segmentedBir.getBdbInfo()
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
		try {
			Response<BIR[]> segmentedDataResponse = getProvider(modality).segment(bir, null);
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
		try {
			Response<BIR[]> segmentedDataResponse = getProvider(modality).segment(bir, null);
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
}
