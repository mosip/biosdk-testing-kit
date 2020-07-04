package io.mosip.biosdktest.test;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Manoj SP
 *
 */
@Component
public class BioSDKTest {

	@Autowired
	private QualityCheckTest qualityCheckTest;

	@Autowired
	private MatchTest matchTest;

	@Autowired
	private ExtractTemplateTest extractTemplateTest;

	@Autowired
	private SegmentTest segmentTest;

	@Autowired
	private ConvertTest convertTest;

	public void qualityCheckSuccess(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		qualityCheckTest.qualityCheckSuccess(testCaseName, modalities, probeFileName);
	}

	public void qualityCheckFail(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		qualityCheckTest.qualityCheckFail(testCaseName, modalities, probeFileName);
	}

	public void qualityCheckInvalidData(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		qualityCheckTest.qualityCheckInvalidData(testCaseName, modalities, probeFileName);
	}

	public void qualityCheckNoInputData(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		qualityCheckTest.qualityCheckNoInputData(testCaseName, modalities, probeFileName);
	}

	public void matchSuccess(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		matchTest.matchSuccess(testCaseName, modalities, probeFileName, galleryFileNames);
	}

	public void matchFail(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		matchTest.matchFail(testCaseName, modalities, probeFileName, galleryFileNames);
	}

	public void matchInvalidData(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		matchTest.matchInvalidData(testCaseName, modalities, probeFileName, galleryFileNames);
	}

	public void matchNoInputData(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		matchTest.matchNoInputData(testCaseName, modalities, probeFileName, galleryFileNames);
	}

	public void extractTemplateAndCheckQualitySuccess(String testCaseName, List<String> modalities,
			String probeFileName, List<String> galleryFileNames) throws Exception {
		extractTemplateTest.extractTemplateAndCheckQualitySuccess(testCaseName, modalities, probeFileName);
	}

	public void extractTemplateAndCheckQualityFail(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		extractTemplateTest.extractTemplateAndCheckQualityFail(testCaseName, modalities, probeFileName);
	}

	public void extractTemplateInvalidData(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		extractTemplateTest.extractTemplateInvalidData(testCaseName, modalities, probeFileName);
	}

	public void extractTemplateNoInputData(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		extractTemplateTest.extractTemplateNoInputData(testCaseName, modalities, probeFileName);
	}

	public void extractAndMatchFMRSuccess(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		extractTemplateTest.extractAndMatchFMRSuccess(testCaseName, modalities, probeFileName, galleryFileNames);
	}

	public void extractAndMatchFMRFail(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		extractTemplateTest.extractAndMatchFMRFail(testCaseName, modalities, probeFileName, galleryFileNames);
	}

	public void segment(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		segmentTest.segment(testCaseName, modalities, probeFileName);
	}

	public void segmentInvalidData(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		segmentTest.segmentInvalidData(testCaseName, modalities, probeFileName);
	}

	public void segmentNoInputData(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		segmentTest.segmentNoInputData(testCaseName, modalities, probeFileName);
	}

	public void convertFormatJPEGToJPEG(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		convertTest.convertFormatJPEGToJPEG(testCaseName, modalities, probeFileName);
	}

	public void convertFormatJPEGToBMP(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		convertTest.convertFormatJPEGToBMP(testCaseName, modalities, probeFileName);
	}

	public void convertFormatJPEGToWSQ(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		convertTest.convertFormatJPEGToWSQ(testCaseName, modalities, probeFileName);
	}

	public void convertFormatBMPToJPEG(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		convertTest.convertFormatBMPToJPEG(testCaseName, modalities, probeFileName);
	}

	public void convertFormatBMPToBMP(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		convertTest.convertFormatBMPToBMP(testCaseName, modalities, probeFileName);
	}

	public void convertFormatBMPToWSQ(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		convertTest.convertFormatBMPToWSQ(testCaseName, modalities, probeFileName);
	}

	public void convertFormatWSQToJPEG(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		convertTest.convertFormatWSQToJPEG(testCaseName, modalities, probeFileName);
	}

	public void convertFormatWSQToBMP(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		convertTest.convertFormatWSQToBMP(testCaseName, modalities, probeFileName);
	}

	public void convertFormatWSQToWSQ(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		convertTest.convertFormatWSQToWSQ(testCaseName, modalities, probeFileName);
	}

	public void convertFormatInvalidData(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		convertTest.convertFormatInvalidData(testCaseName, modalities, probeFileName);
	}

	public void convertFormatNoData(String testCaseName, List<String> modalities, String probeFileName,
			List<String> galleryFileNames) throws Exception {
		convertTest.convertFormatNoData(testCaseName, modalities, probeFileName);
	}
}
