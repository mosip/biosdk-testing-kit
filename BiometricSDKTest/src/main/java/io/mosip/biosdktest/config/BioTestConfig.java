package io.mosip.biosdktest.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;

import io.mosip.kernel.biometrics.model.SDKInfo;
import io.mosip.kernel.biometrics.spi.IBioApi;
import io.mosip.kernel.core.bioapi.exception.BiometricException;

/**
 * @author Manoj SP
 *
 */
@Configuration
@ConfigurationProperties(prefix = "biotest.sdk.provider")
public class BioTestConfig {

	private static final String BIOTEST_IRIS_PROVIDER = "biotest.iris.provider";

	private static final String BIOTEST_FACE_PROVIDER = "biotest.face.provider";

	private static final String BIOTEST_FINGERPRINT_PROVIDER = "biotest.fingerprint.provider";

	private Map<String, String> finger = new HashMap<>();

	private Map<String, String> iris = new HashMap<>();

	private Map<String, String> face = new HashMap<>();

	@Autowired
	private Environment env;

	public static Map<String, SDKInfo> sdkInfo = new HashMap<>();

	@PostConstruct
	public void init() throws BiometricException {
		if (StringUtils.isAllBlank(env.getProperty(BIOTEST_FINGERPRINT_PROVIDER),
				env.getProperty(BIOTEST_FACE_PROVIDER), env.getProperty(BIOTEST_IRIS_PROVIDER))) {
			throw new BiometricException("", "Unable to find any biometric providers");
		}
	}

	@Bean("finger")
	public IBioApi fingerProvider() throws BiometricException {
		if (StringUtils.isNotBlank(env.getProperty(BIOTEST_FINGERPRINT_PROVIDER))) {
			try {
				IBioApi newInstance = (IBioApi) Class.forName(env.getProperty(BIOTEST_FINGERPRINT_PROVIDER))
						.newInstance();
				sdkInfo.put("finger", newInstance.init(finger));
				return newInstance;
			} catch (Exception e) {
				throw new BiometricException("", "Unable to load fingerprint provider", e);
			}
		}
		return null;

	}

	@Bean("face")
	@DependsOn("finger")
	public IBioApi faceProvider() throws BiometricException {
		if (StringUtils.isNotBlank(env.getProperty(BIOTEST_FACE_PROVIDER))) {
			if (StringUtils.equals(env.getProperty(BIOTEST_FACE_PROVIDER),
					env.getProperty(BIOTEST_FINGERPRINT_PROVIDER))) {
				sdkInfo.put("face", sdkInfo.get("finger"));
				return fingerProvider();
			} else {
				try {
					IBioApi newInstance = (IBioApi) Class.forName(env.getProperty(BIOTEST_FACE_PROVIDER)).newInstance();
					sdkInfo.put("face", newInstance.init(face));
					return newInstance;
				} catch (Exception e) {
					throw new BiometricException("", "Unable to load fingerprint provider", e);
				}
			}
		}
		return null;
	}

	@Bean("iris")
	@DependsOn({"finger", "face"})
	public IBioApi irisProvider() throws BiometricException {
		if (StringUtils.isNotBlank(env.getProperty(BIOTEST_IRIS_PROVIDER))) {
			if (StringUtils.equals(env.getProperty(BIOTEST_IRIS_PROVIDER),
					env.getProperty(BIOTEST_FINGERPRINT_PROVIDER))) {
				sdkInfo.put("iris", sdkInfo.get("finger"));
				return fingerProvider();
			} else if (StringUtils.equals(env.getProperty(BIOTEST_IRIS_PROVIDER),
					env.getProperty(BIOTEST_FACE_PROVIDER))) {
				return faceProvider();
			} else {
				try {
					IBioApi newInstance = (IBioApi) Class.forName(env.getProperty(BIOTEST_IRIS_PROVIDER)).newInstance();
					sdkInfo.put("iris", newInstance.init(iris));
					return newInstance;
				} catch (Exception e) {
					throw new BiometricException("", "Unable to load fingerprint provider", e);
				}
			}
		}
		return null;
	}

	public Map<String, String> getFinger() {
		return finger;
	}

	public void setFinger(Map<String, String> finger) {
		this.finger = finger;
	}

	public Map<String, String> getIris() {
		return iris;
	}

	public void setIris(Map<String, String> iris) {
		this.iris = iris;
	}

	public Map<String, String> getFace() {
		return face;
	}

	public void setFace(Map<String, String> face) {
		this.face = face;
	}
}
