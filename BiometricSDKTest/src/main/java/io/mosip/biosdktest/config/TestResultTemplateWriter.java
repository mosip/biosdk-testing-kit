package io.mosip.biosdktest.config;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;

/**
 * @author Manoj SP
 *
 */
@Component
public class TestResultTemplateWriter {

	private static String reportFileName = "result_" + DateUtils.getCurrentDateTimeString().replaceAll("T", "")
			.replaceAll(":", "").replaceAll("\\+", "").replaceAll("\\.", "").replaceAll("-", "");
	
	private String report;

	@PostConstruct
	public void prepareReportTemplate() throws IOException {
		report = new String(CryptoUtil.decodeBase64(
				IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("index_part1"), "UTF-8")));
		report += reportFileName + ".js";
		report += new String(CryptoUtil.decodeBase64(
				IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("index_part2"), "UTF-8")));
		FileUtils.write(new File("test-results/" + reportFileName + ".html"), report, "UTF-8");
		System.out.println("#----------------------------------------------------------------------------------------------------#");
		System.out.println("Test execution started. Test results are available in test-results/" + reportFileName + ".html");
		System.out.println("#----------------------------------------------------------------------------------------------------#");
	}

	public static String getReportFileName() {
		return reportFileName;
	}
	
	@PreDestroy
	public void destroy() throws IOException {
		FileUtils.write(new File("test-results/" + reportFileName + ".html"), report, "UTF-8");
		System.out.println("#----------------------------------------------------------------------------------------------------#");
		System.out.println("Test execution completed. Test results are available in test-results/" + reportFileName + ".html");
		System.out.println("#----------------------------------------------------------------------------------------------------#");
	}

}
