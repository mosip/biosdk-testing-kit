package io.mosip.biosdktest.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import io.mosip.biosdktest.config.BioTestConfig;
import io.mosip.kernel.biometrics.constant.BiometricFunction;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.spi.IBioApi;
import io.mosip.kernel.core.cbeffutil.entity.BIR;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.SingleType;
import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * @author Manoj SP
 *
 */
@Component
public class BioTestHelper {

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
	private ResourceLoader resourceLoader;

	private static Map<String, byte[]> inputFiles = new HashMap<>();

	public boolean checkSDKSupport(List<String> modalities, Map<String, Boolean> isModalitySupported,
			Map<String, Boolean> isMethodSupported, Map<String, Boolean> isMethodSupportsModality,
			BiometricFunction bioFn) {
		modalities.stream().forEach(modality -> isModalitySupported.put(modality,
				BioTestConfig.sdkInfo.get(modality).getSupportedModalities().contains(getBiometricType(modality))));
		modalities.stream().forEach(modality -> isMethodSupported.put(modality,
				BioTestConfig.sdkInfo.get(modality).getSupportedMethods().containsKey(bioFn)));
		modalities.stream()
				.forEach(modality -> isMethodSupportsModality.put(modality,
						isMethodSupported.get(modality)
								? BioTestConfig.sdkInfo.get(modality).getSupportedMethods().get(bioFn)
										.contains(getBiometricType(modality))
								: false));

		return modalities.stream().allMatch(modality -> isModalitySupported.get(modality)
				&& isMethodSupported.get(modality) && isMethodSupportsModality.get(modality));
	}

	public IBioApi getProvider(String modality) {
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

	public SingleType getType(String type) {
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

	public byte[] getInputFile(String fileName) throws IOException {
		if (inputFiles.containsKey(fileName)) {
			return inputFiles.get(fileName);
		} else {
			inputFiles.put(fileName,
					IOUtils.toByteArray(resourceLoader.getResource("file:" + fileName).getInputStream()));
			return inputFiles.get(fileName);
		}
	}

	public BiometricType getBiometricType(String type) {
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

	public io.mosip.kernel.biometrics.entities.BIR convertToBiometricRecordBIR(BIR bir) {
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
