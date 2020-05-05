package io.mosip.biosdktest.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import io.mosip.kernel.core.bioapi.exception.BiometricException;
import io.mosip.kernel.core.bioapi.spi.IBioApi;

/**
 * @author Manoj SP
 *
 */
@Configuration
public class BioTestConfig {

	private static final String BIOTEST_IRIS_PROVIDER = "biotest.iris.provider";

	private static final String BIOTEST_FACE_PROVIDER = "biotest.face.provider";

	private static final String BIOTEST_FINGERPRINT_PROVIDER = "biotest.fingerprint.provider";

	private List<String> fingerArgs = Collections.emptyList();

	private List<String> irisArgs = Collections.emptyList();

	private List<String> faceArgs = Collections.emptyList();

	@Autowired
	private Environment env;

	@PostConstruct
	public void init() throws BiometricException {
		if (Objects.nonNull(env.getProperty("biotest.fingerprint.provider.args"))) {
			fingerArgs = Arrays.asList(env.getProperty("biotest.fingerprint.provider.args").split(","));
		}
		if (Objects.nonNull(env.getProperty("biotest.face.provider.args"))) {
			faceArgs = Arrays.asList(env.getProperty("biotest.face.provider.args").split(","));
		}
		if (Objects.nonNull(env.getProperty("biotest.iris.provider.args"))) {
			irisArgs = Arrays.asList(env.getProperty("biotest.iris.provider.args").split(","));
		}
		if (StringUtils.isAllBlank(env.getProperty(BIOTEST_FINGERPRINT_PROVIDER),
				env.getProperty(BIOTEST_FACE_PROVIDER), env.getProperty(BIOTEST_IRIS_PROVIDER))) {
			throw new BiometricException("", "Unable to find any biometric providers");
		}
	}

	@Bean("finger")
	public IBioApi fingerProvider() throws BiometricException, InvocationTargetException {
		if (StringUtils.isNotBlank(env.getProperty(BIOTEST_FINGERPRINT_PROVIDER))) {
			try {
				System.err.println(env.getProperty(BIOTEST_FINGERPRINT_PROVIDER));
				Optional<Constructor<?>> constructor = getConstructor(BIOTEST_FINGERPRINT_PROVIDER, fingerArgs);
				if (constructor.isPresent()) {
					return (IBioApi) constructor.get().newInstance(fingerArgs.toArray());
				} else {
					throw new BiometricException("", "Unable to initialize finger provider/Argsuments not matching");
				}
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				throw new BiometricException("", "Unable to load fingerprint provider", e);
			}
		}
		return null;
	}

	@Bean("face")
	public IBioApi faceProvider() throws BiometricException, InvocationTargetException {
		if (StringUtils.isNotBlank(env.getProperty(BIOTEST_FACE_PROVIDER))) {
			if (StringUtils.equals(env.getProperty(BIOTEST_FACE_PROVIDER),
					env.getProperty(BIOTEST_FINGERPRINT_PROVIDER))) {
				return fingerProvider();
			} else {
				try {
					System.err.println(env.getProperty(BIOTEST_FACE_PROVIDER));
					Optional<Constructor<?>> constructor = getConstructor(BIOTEST_FACE_PROVIDER, faceArgs);
					if (constructor.isPresent()) {
						return (IBioApi) constructor.get().newInstance(faceArgs.toArray());
					} else {
						throw new BiometricException("", "Unable to initialize face provider/Argsuments not matching");
					}
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
					throw new BiometricException("", "Unable to load face provider", e);
				}
			}
		}
		return null;
	}

	@Bean("iris")
	public IBioApi irisProvider() throws BiometricException, InvocationTargetException {
		if (StringUtils.isNotBlank(env.getProperty(BIOTEST_IRIS_PROVIDER))) {
			if (StringUtils.equals(env.getProperty(BIOTEST_IRIS_PROVIDER),
					env.getProperty(BIOTEST_FINGERPRINT_PROVIDER))) {
				return fingerProvider();
			} else if (StringUtils.equals(env.getProperty(BIOTEST_IRIS_PROVIDER),
					env.getProperty(BIOTEST_FACE_PROVIDER))) {
				return faceProvider();
			} else {
				try {
					System.err.println(env.getProperty(BIOTEST_IRIS_PROVIDER));
					Optional<Constructor<?>> constructor = getConstructor(BIOTEST_IRIS_PROVIDER, irisArgs);
					if (constructor.isPresent()) {
						return (IBioApi) constructor.get().newInstance(irisArgs.toArray());
					} else {
						throw new BiometricException("", "Unable to initialize iris provider/Argsuments not matching");
					}
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
					throw new BiometricException("", "Unable to load iris provider", e);
				}
			}
		}
		return null;
	}

	private Optional<Constructor<?>> getConstructor(String provider, List<String> args) throws ClassNotFoundException {
		return Arrays.asList(Class.forName(env.getProperty(provider)).getDeclaredConstructors()).stream()
				.filter(cons -> Objects.nonNull(args) && cons.getParameterCount() == args.size())
				.peek(cons -> cons.setAccessible(true)).findFirst();
	}
}
