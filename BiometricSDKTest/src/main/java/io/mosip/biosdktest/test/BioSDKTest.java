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
import io.mosip.kernel.core.bioapi.exception.BiometricException;
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

	private static final String MATCH_THRESHOLD_VALUE = ".match.threshold.value";

	private static final String UNKNOWN_ERROR = "KER-BIO-005";

	private static final String MATCH_FAILED = "KER-BIO-004";

	private static final String QUALITY_CHECK_FAILED = "KER-BIO-003";

	private static final String MISSING_DATA = "KER-BIO-002";

	private static final String INVALID_DATA = "KER-BIO-001";

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
				long internalScore = getProvider(modality).checkQuality(bir, null).getInternalScore();
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
						String.valueOf(internalScore),
						(internalScore >= env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE, Integer.class)));
			} catch (BiometricException e) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
						"Biometric Exception with Error code " + e.getErrorCode(), false);
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
				long internalScore = getProvider(modality).checkQuality(bir, null).getInternalScore();
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"qualityScore <= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
						String.valueOf(internalScore),
						(internalScore <= env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE, Integer.class)));
			} catch (BiometricException e) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
						"Biometric Exception with Error code " + e.getErrorCode(), false);
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
				long internalScore = getProvider(modality).checkQuality(bir, null).getInternalScore();
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Biometric Exception with Error code " + INVALID_DATA, String.valueOf(internalScore), false);
			} catch (BiometricException e) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Biometric Exception with Error code " + INVALID_DATA,
						"Biometric Exception with Error code " + e.getErrorCode(),
						(e.getClass().isAssignableFrom(BiometricException.class)
								&& e.getErrorCode().contentEquals(INVALID_DATA)));
			} catch (Exception e) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Biometric Exception with Error code " + INVALID_DATA, ExceptionUtils.getStackTrace(e), false);
			}
		}
	}

	public void qualityCheckNoInputData(String testCaseName, String modality, String probeFileName) throws Exception {
		List<BIR> birs = cbeffReader.convertBIRTypeToBIR(
				cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()));
		for (BIR bir : birs) {
			try {
				long internalScore = getProvider(modality).checkQuality(bir, null).getInternalScore();
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Biometric Exception with Error code " + MISSING_DATA, String.valueOf(internalScore), false);
			} catch (BiometricException e) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Biometric Exception with Error code " + MISSING_DATA,
						"Biometric Exception with Error code " + e.getErrorCode(),
						(e.getClass().isAssignableFrom(BiometricException.class)
								&& e.getErrorCode().contentEquals(MISSING_DATA)));
			} catch (Exception e) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Biometric Exception with Error code " + MISSING_DATA, ExceptionUtils.getStackTrace(e), false);
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
			float score = getProvider(modality).match(probeBir, galleryBir, null)[0].getScaleScore();
			builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
					"matchScore >= " + env.getProperty(modality + MATCH_THRESHOLD_VALUE), String.valueOf(score),
					(score >= env.getProperty(modality + MATCH_THRESHOLD_VALUE, Integer.class)));
		} catch (BiometricException e) {
			builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
					"matchScore >= " + env.getProperty(modality + MATCH_THRESHOLD_VALUE),
					"Biometric Exception with Error code " + e.getErrorCode(), false);
		} catch (Exception e) {
			builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
					"matchScore >= " + env.getProperty(modality + MATCH_THRESHOLD_VALUE),
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
			float score = getProvider(modality).match(probeBir, galleryBir, null)[0].getScaleScore();
			builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
					"matchScore <= " + env.getProperty(modality + MATCH_THRESHOLD_VALUE), String.valueOf(score),
					(score <= env.getProperty(modality + MATCH_THRESHOLD_VALUE, Integer.class)));
		} catch (BiometricException e) {
			builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
					"matchScore >= " + env.getProperty(modality + MATCH_THRESHOLD_VALUE),
					"Biometric Exception with Error code " + e.getErrorCode(), false);
		} catch (Exception e) {
			builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
					"matchScore >= " + env.getProperty(modality + MATCH_THRESHOLD_VALUE),
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
			float score = getProvider(modality).match(probeBir, galleryBir, null)[0].getScaleScore();
			builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
					"Biometric Exception with Error code " + INVALID_DATA, String.valueOf(score), false);
		} catch (BiometricException e) {
			builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
					"Biometric Exception with Error code " + INVALID_DATA,
					"Biometric Exception with Error code " + e.getErrorCode(),
					(e.getClass().isAssignableFrom(BiometricException.class)
							&& e.getErrorCode().contentEquals(INVALID_DATA)));
		} catch (Exception e) {
			builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
					"Biometric Exception with Error code " + INVALID_DATA, ExceptionUtils.getStackTrace(e), false);
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
			float score = getProvider(modality).match(probeBir, galleryBir, null)[0].getScaleScore();
			builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
					"Biometric Exception with Error code " + MISSING_DATA, String.valueOf(score), false);
		} catch (BiometricException e) {
			builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
					"Biometric Exception with Error code " + MISSING_DATA,
					"Biometric Exception with Error code " + e.getErrorCode(),
					(e.getClass().isAssignableFrom(BiometricException.class)
							&& e.getErrorCode().contentEquals(MISSING_DATA)));
		} catch (Exception e) {
			builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
					"Biometric Exception with Error code " + MISSING_DATA, ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void compositeMatchSuccess(String testCaseName, String modality, String probeFileName,
			String galleryFileName) throws Exception {
		BIR[] probeBirs = cbeffReader.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(getInputFile(probeFileName)))
				.toArray(new BIR[] {});
		BIR[] galleryBirs = cbeffReader
				.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(getInputFile(galleryFileName)))
				.toArray(new BIR[] {});
		try {
			float score = getProvider(modality).compositeMatch(probeBirs, galleryBirs, null).getScaledScore();
			builder.build(testCaseName, modality, null,
					"compositeMatchScore >= " + env.getProperty(modality + MATCH_THRESHOLD_VALUE),
					String.valueOf(score), (score >= env.getProperty(modality + MATCH_THRESHOLD_VALUE, Integer.class)));
		} catch (BiometricException e) {
			builder.build(testCaseName, modality, null,
					"compositeMatchScore >= " + env.getProperty(modality + MATCH_THRESHOLD_VALUE),
					"Biometric Exception with Error code " + e.getErrorCode(), false);
		} catch (Exception e) {
			builder.build(testCaseName, modality, null,
					"compositeMatchScore >= " + env.getProperty(modality + MATCH_THRESHOLD_VALUE),
					ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void compositeMatchFail(String testCaseName, String modality, String probeFileName, String galleryFileName)
			throws Exception {
		BIR[] probeBirs = cbeffReader.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(getInputFile(probeFileName)))
				.toArray(new BIR[] {});
		BIR[] galleryBirs = cbeffReader
				.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(getInputFile(galleryFileName)))
				.toArray(new BIR[] {});
		try {
			float score = getProvider(modality).compositeMatch(probeBirs, galleryBirs, null).getScaledScore();
			builder.build(testCaseName, modality, null,
					"compositeMatchScore >= " + env.getProperty(modality + MATCH_THRESHOLD_VALUE),
					String.valueOf(score), (score >= env.getProperty(modality + MATCH_THRESHOLD_VALUE, Integer.class)));
		} catch (BiometricException e) {
			builder.build(testCaseName, modality, null,
					"compositeMatchScore >= " + env.getProperty(modality + MATCH_THRESHOLD_VALUE),
					"Biometric Exception with Error code " + e.getErrorCode(), false);
		} catch (Exception e) {
			builder.build(testCaseName, modality, null,
					"compositeMatchScore >= " + env.getProperty(modality + MATCH_THRESHOLD_VALUE),
					ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void compositeMatchInvalidData(String testCaseName, String modality, String probeFileName,
			String galleryFileName) throws Exception {
		BIR[] probeBirs = cbeffReader.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(getInputFile(probeFileName)))
				.toArray(new BIR[] {});
		BIR[] galleryBirs = cbeffReader
				.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(getInputFile(galleryFileName)))
				.toArray(new BIR[] {});
		try {
			float score = getProvider(modality).compositeMatch(probeBirs, galleryBirs, null).getScaledScore();
			builder.build(testCaseName, modality, null, "Biometric Exception with Error code " + INVALID_DATA,
					String.valueOf(score), false);
		} catch (BiometricException e) {
			builder.build(testCaseName, modality, null, "Biometric Exception with Error code " + INVALID_DATA,
					"Biometric Exception with Error code " + e.getErrorCode(),
					(e.getClass().isAssignableFrom(BiometricException.class)
							&& e.getErrorCode().contentEquals(INVALID_DATA)));
		} catch (Exception e) {
			builder.build(testCaseName, modality, null, "Biometric Exception with Error code " + INVALID_DATA,
					ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void compositeMatchNoInputData(String testCaseName, String modality, String probeFileName,
			String galleryFileName) throws Exception {
		BIR[] probeBirs = cbeffReader.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(getInputFile(probeFileName)))
				.toArray(new BIR[] {});
		BIR[] galleryBirs = cbeffReader
				.convertBIRTypeToBIR(cbeffReader.getBIRDataFromXML(getInputFile(galleryFileName)))
				.toArray(new BIR[] {});
		try {
			float score = getProvider(modality).compositeMatch(probeBirs, galleryBirs, null).getScaledScore();
			builder.build(testCaseName, modality, null, "Biometric Exception with Error code " + MISSING_DATA,
					String.valueOf(score), false);
		} catch (BiometricException e) {
			builder.build(testCaseName, modality, null, "Biometric Exception with Error code " + MISSING_DATA,
					"Biometric Exception with Error code " + e.getErrorCode(),
					(e.getClass().isAssignableFrom(BiometricException.class)
							&& e.getErrorCode().contentEquals(MISSING_DATA)));
		} catch (Exception e) {
			builder.build(testCaseName, modality, null, "Biometric Exception with Error code " + MISSING_DATA,
					ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void extractTemplateAndCheckQualitySuccess(String testCaseName, String modality, String probeFileName)
			throws Exception {
		List<BIR> birs = cbeffReader.convertBIRTypeToBIR(
				cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()));
		for (BIR bir : birs) {
			try {
				BIR extractedTemplate = getProvider(modality).extractTemplate(bir, null);
				long internalScore = getProvider(modality).checkQuality(extractedTemplate, null).getInternalScore();
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
						String.valueOf(internalScore),
						(internalScore >= env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE, Integer.class)));
			} catch (BiometricException e) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
						"Biometric Exception with Error code " + e.getErrorCode(), false);
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
				BIR extractedTemplate = getProvider(modality).extractTemplate(bir, null);
				long internalScore = getProvider(modality).checkQuality(extractedTemplate, null).getInternalScore();
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"qualityScore <= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
						String.valueOf(internalScore),
						(internalScore <= env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE, Integer.class)));
			} catch (BiometricException e) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"qualityScore <= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE),
						"Biometric Exception with Error code " + e.getErrorCode(), false);
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
				BIR extractedTemplate = getProvider(modality).extractTemplate(bir, null);
				builder.build(testCaseName, modality, null,
						"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE), null, false);
			} catch (BiometricException e) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Biometric Exception with Error code " + INVALID_DATA,
						"Biometric Exception with Error code " + e.getErrorCode(),
						(e.getClass().isAssignableFrom(BiometricException.class)
								&& e.getErrorCode().contentEquals(INVALID_DATA)));
			} catch (Exception e) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Biometric Exception with Error code " + INVALID_DATA, ExceptionUtils.getStackTrace(e), false);
			}
		}
	}

	public void extractTemplateNoInputData(String testCaseName, String modality, String probeFileName)
			throws Exception {
		List<BIR> birs = cbeffReader.convertBIRTypeToBIR(
				cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()));
		for (BIR bir : birs) {
			try {
				BIR extractedTemplate = getProvider(modality).extractTemplate(bir, null);
				builder.build(testCaseName, modality, null,
						"qualityScore >= " + env.getProperty(modality + QUALITY_CHECK_THRESHOLD_VALUE), null, false);
			} catch (BiometricException e) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Biometric Exception with Error code " + MISSING_DATA,
						"Biometric Exception with Error code " + e.getErrorCode(),
						(e.getClass().isAssignableFrom(BiometricException.class)
								&& e.getErrorCode().contentEquals(MISSING_DATA)));
			} catch (Exception e) {
				builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
						"Biometric Exception with Error code " + MISSING_DATA, ExceptionUtils.getStackTrace(e), false);
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
				galleryFMRBir.add(getProvider(modality).extractTemplate(bir, null));
			}
			BIR probeFMRBir = getProvider(modality).extractTemplate(probeBir, null);
			float score = getProvider(modality).match(probeFMRBir, galleryFMRBir.toArray(new BIR[] {}), null)[0]
					.getScaleScore();
			builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
					"matchScore >= " + env.getProperty(modality + MATCH_THRESHOLD_VALUE), String.valueOf(score),
					(score >= env.getProperty(modality + MATCH_THRESHOLD_VALUE, Integer.class)));
		} catch (BiometricException e) {
			builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
					"matchScore >= " + env.getProperty(modality + MATCH_THRESHOLD_VALUE),
					"Biometric Exception with Error code " + e.getErrorCode(), false);
		} catch (Exception e) {
			builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
					"matchScore >= " + env.getProperty(modality + MATCH_THRESHOLD_VALUE),
					ExceptionUtils.getStackTrace(e), false);
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
				galleryFMRBir.add(getProvider(modality).extractTemplate(bir, null));
			}
			BIR probeFMRBir = getProvider(modality).extractTemplate(probeBir, null);
			float score = getProvider(modality).match(probeFMRBir, galleryFMRBir.toArray(new BIR[] {}), null)[0]
					.getScaleScore();
			builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
					"matchScore <= " + env.getProperty(modality + MATCH_THRESHOLD_VALUE), String.valueOf(score),
					(score <= env.getProperty(modality + MATCH_THRESHOLD_VALUE, Integer.class)));
		} catch (BiometricException e) {
			builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
					"matchScore >= " + env.getProperty(modality + MATCH_THRESHOLD_VALUE),
					"Biometric Exception with Error code " + e.getErrorCode(), false);
		} catch (Exception e) {
			builder.build(testCaseName, modality, probeBir.getBdbInfo().getSubtype().toString(),
					"matchScore >= " + env.getProperty(modality + MATCH_THRESHOLD_VALUE),
					ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void segment(String testCaseName, String modality, String probeFileName) throws Exception {
		BIR bir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()))
				.get(0);
		try {
			BIR[] segmentedData = getProvider(modality).segment(bir, null);
			Arrays.asList(segmentedData).forEach(segmentedBir -> segmentedBir.getBdbInfo().getSubtype()
					.forEach(subType -> SingleType.fromValue(subType)));
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Any value from [Left, Right, Thumb, IndexFinger, MiddleFinger, RingFinger, LittleFinger]",
					"Any value from [Left, Right, Thumb, IndexFinger, MiddleFinger, RingFinger, LittleFinger]", true);
		} catch (IllegalArgumentException e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Any value from [Left, Right, Thumb, IndexFinger, MiddleFinger, RingFinger, LittleFinger]",
					ExceptionUtils.getStackTrace(e), false);
		} catch (BiometricException e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Any value from [Left, Right, Thumb, IndexFinger, MiddleFinger, RingFinger, LittleFinger]",
					"Biometric Exception with Error code " + e.getErrorCode(), false);
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
			BIR[] segmentedData = getProvider(modality).segment(bir, null);
			Arrays.asList(segmentedData).forEach(segmentedBir -> segmentedBir.getBdbInfo().getSubtype()
					.forEach(subType -> SingleType.fromValue(subType)));
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Biometric Exception with Error code " + INVALID_DATA,
					"Any value from [Left, Right, Thumb, IndexFinger, MiddleFinger, RingFinger, LittleFinger]", false);
		} catch (IllegalArgumentException e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Biometric Exception with Error code " + INVALID_DATA, ExceptionUtils.getStackTrace(e), false);
		} catch (BiometricException e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Biometric Exception with Error code " + INVALID_DATA,
					"Biometric Exception with Error code " + e.getErrorCode(),
					(e.getClass().isAssignableFrom(BiometricException.class)
							&& e.getErrorCode().contentEquals(INVALID_DATA)));
		} catch (Exception e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Biometric Exception with Error code " + INVALID_DATA, ExceptionUtils.getStackTrace(e), false);
		}
	}

	public void segmentNoInputData(String testCaseName, String modality, String probeFileName) throws Exception {
		BIR bir = cbeffReader
				.convertBIRTypeToBIR(
						cbeffReader.getBIRDataFromXMLType(getInputFile(probeFileName), getType(modality).value()))
				.get(0);
		try {
			BIR[] segmentedData = getProvider(modality).segment(bir, null);
			Arrays.asList(segmentedData).forEach(segmentedBir -> segmentedBir.getBdbInfo().getSubtype()
					.forEach(subType -> SingleType.fromValue(subType)));
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Biometric Exception with Error code " + MISSING_DATA,
					"Any value from [Left, Right, Thumb, IndexFinger, MiddleFinger, RingFinger, LittleFinger]", false);
		} catch (IllegalArgumentException e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Biometric Exception with Error code " + MISSING_DATA, ExceptionUtils.getStackTrace(e), false);
		} catch (BiometricException e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Biometric Exception with Error code " + MISSING_DATA,
					"Biometric Exception with Error code " + e.getErrorCode(),
					(e.getClass().isAssignableFrom(BiometricException.class)
							&& e.getErrorCode().contentEquals(MISSING_DATA)));
		} catch (Exception e) {
			builder.build(testCaseName, modality, bir.getBdbInfo().getSubtype().toString(),
					"Biometric Exception with Error code " + MISSING_DATA, ExceptionUtils.getStackTrace(e), false);
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
