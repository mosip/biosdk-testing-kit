## BiometricSDKTest

BiometricSDKTest is used to test Integration and functionality of Biometric SDKs with MOSIP. BiometricSDKTest can be used by vendors to perform tests with their Biometric provider SDKs.

# Pre-requisites:

* **Java 8** - BiometricSDKTest is completely java based application and it requires Java runtime environment to run. Java 8 needs to be setup before using BiometricSDKTest. Please use below command in Command Line (Windows) / Terminal (Linux) to check whether Java 8 is setup properly or not.

**Command:**

```
java -version

```

**Expected Response:**

```
java version "1.8.0_152"
Java(TM) SE Runtime Environment (build 1.8.0_152-b16)

```

* **BiometricSDKTest** - Jar file required to test Integration and run tests.


# Configuration:

The following configuration should be done before executing BiometricSDKTest. These configurations should be provided in the below path:

* Path: config/application.properties

**Application Properties**

```
# Threshold value against which the quality check score value will be evaluated for Fingerprint biometric type.
finger.qualitycheck.threshold.value=<Threshold value>

# Threshold value against which the match score value will be evaluated for Fingerprint biometric type.
finger.match.threshold.value=<Threshold value>

# Threshold value against which the quality check score value will be evaluated for Face biometric type.
face.qualitycheck.threshold.value=<Threshold value>

# Threshold value against which the match score value will be evaluated for Face biometric type.
face.match.threshold.value=<Threshold value>

# Threshold value against which the quality check score value will be evaluated for Iris biometric type.
iris.qualitycheck.threshold.value=<Threshold value>

# Threshold value against which the match score value will be evaluated for Iris biometric type.
iris.match.threshold.value=<Threshold value>

# Threshold value against which the quality check score value will be evaluated for Composite matching.
composite.qualitycheck.threshold.value=<Threshold value>

# Threshold value against which the match score value will be evaluated for Composite matching.
composite.match.threshold.value=<Threshold value>

# Thread pool size required for multithreaded testing.
threadpool.size=<Number of threads>
```

# Test case preparation:

BiometricSDKTest uses list of test cases as Input and provides results based on the test case. Each Test case contains following parameters seperated by pipe (|). All the below parameters are mandatory for a test case.

1. Test Case Name
2. Biometric Type
3. Test Function (List of test scenarios are provided below).
4. Input Files 

After preparing the required test cases in the below format, they should be saved in a file (eg. test.txt), which needs to be provided as input to BiometricSDKTest in later steps.
Test case comments are also supported using #. Lines starting with # are considered as comments.

**Test case Format:**

```
# comments
testCaseName|biometricType|testFuntion|inputCbeffFiles
```

**Test Case Name:**
	Test case Name can be any name based on the user input.
	
**Biometric Type:**
	BiometricSDKTest expects any one of the following biometric type for a single test case.
* finger
* face
* iris
* composite

**Test Function:**
	BiometricSDKTest requires any one of the following test scenario as input for a single test case:

```
For Quality Check (supports finger (FIR), finger (FMR), face, iris, composite):
	qualityCheckSuccess
	qualityCheckFail
	qualityCheckInvalidData
	qualityCheckNoInputData
	
For Matching (supports finger (FIR), finger (FMR), face, iris, composite):
	matchSuccess
	matchFail
	matchInvalidData
	matchNoInputData
	
For Composite match (supports finger (FIR), face, iris, composite):
	compositeMatchSuccess
	compositeMatchFail
	compositeMatchInvalidData
	compositeMatchNoInputData
	
For Extracting FMR Template (supports finger (FIR)):
	extractTemplateAndCheckQualitySuccess
	extractTemplateAndCheckQualityFail
	extractTemplateInvalidData
	extractTemplateNoInputData
	extractAndMatchFMRSuccess
	extractAndMatchFMRFail
	
```

**Input Files:**
	BiometricSDKTest supports input files which are defined only in CBEFF format. After preparing the input data, place the required input files along with BiometricSDKTest.jar or any of the sub folders.

When placed alongside BiometricSDKTest.jar, input in test case should be in below format. Let's consider inputData.xml as our input file name.

```
testCaseName|biometricType|testFuntion|inputData.xml
```

When placed in any of sub folder and sub folder is place alongside BiometricSDKTest.jar, input in test case should be in below format. Let's consider inputData.xml as our input file name placed in sub folder called inputs.

```
testCaseName|biometricType|testFuntion|inputs/inputData.xml
```

When there are multiple input files required, they can be provided with comma(,) seperated.

```
testCaseName|biometricType|testFuntion|inputs/inputData1.xml,inputData2.xml
```

# Usage:
The following files should be prepared before running tests using BiometricSDKTest.

1. application.properties
2. List of test cases file
3. Input files (if any)
4. Vendor SDK(s) and if any dependent jars

To run BiometricSDKTest, use the following command in Command Prompt (Windows) / Terminal (Linux).

```
java -Dloader.path=<path to vendor SDK(s) and if any dependent jars seperated by comma(,)> -Dbiotest.fingerprint.provider=<Canonical name of fingerprint provider class> -Dbiotest.face.provider=<Canonical name of face provider class> -Dbiotest.iris.provider=<Canonical name of iris provider class> -Dbiotest.composite.provider=<Canonical name of composite provider class> -jar BiometricSDKTest.jar <Test cases file>
```

Example:
Let's consider the following:

1. vendorSDK.jar is the vendorSDK and it has a dependent jar as vendorSupportSDK.jar. These 2 jars are placed under lib folder. This lib folder is placed alongside BiometricSDKTest.jar. vendorSDK has a biometric provider class called com.demo.BiometricProvider which provides support for all biometric types. (fingerprint/iris/face/composite).
2. application.properties is updated with required inputs and placed under config folder. This config folder is placed alongside BiometricSDKTest.jar.
3. List of test cases are placed under test.txt file, which is placed alongside BiometricSDKTest.jar.
4. Required input files are placed 

Sample command to execute:

```
java -Dloader.path=lib/vendorSDK.jar,lib/vendorSupportSDK.jar -Dbiotest.fingerprint.provider=com.demo.BiometricProvider -Dbiotest.face.provider=com.demo.BiometricProvider -Dbiotest.iris.provider=com.demo.BiometricProvider -Dbiotest.composite.provider=com.demo.BiometricProvider -jar BiometricSDKTest.jar test.txt
```

For the above example, If there are any initializtion arguments required for Biometric provider classes, It can passed using below sample command. The arguments are expected to be only Strings.

Sample command to with Biometric provider arguments:

```
java -Dloader.path=lib/vendorSDK.jar,lib/vendorSupportSDK.jar -Dbiotest.fingerprint.provider=com.demo.BiometricProvider -Dbiotest.fingerprint.provider.args=arg1,arg2 -Dbiotest.face.provider=com.demo.BiometricProvider -Dbiotest.face.provider.args=arg1,arg2 -Dbiotest.iris.provider=com.demo.BiometricProvider -Dbiotest.iris.provider.args=arg1,arg2 -Dbiotest.composite.provider=com.demo.BiometricProvider -Dbiotest.composite.provider=arg1,arg2 -jar BiometricSDKTest.jar test.txt
```

BiometricSDKTest supports testing of only required biometric types. This can be done by passing only the particular biometric type's class name.

Example:
Let's consider the following:

1. vendorSDK.jar is the vendorSDK and it has a dependent jar as vendorSupportSDK.jar. These 2 jars are placed under lib folder. This lib folder is placed alongside BiometricSDKTest.jar. vendorSDK has a biometric provider class called com.demo.FingerprintProvider which provides support for only Fingerprint biometric type.
2. application.properties is updated with required inputs and placed under config folder. This config folder is placed alongside BiometricSDKTest.jar.
3. List of test cases are placed under test.txt file, which is placed alongside BiometricSDKTest.jar.
4. Required input files are placed 

Sample command to execute:

```
java -Dloader.path=lib/vendorSDK.jar,lib/vendorSupportSDK.jar -Dbiotest.fingerprint.provider=com.demo.FingerprintProvider -jar BiometricSDKTest.jar test.txt
```

If the vendorSDK.jar is located in a different location, then complete path of the jar can be provided.

**NOTE:**
```
BiometricSDKTest creates only one instance of the provided Biometric provider classes, if the class name is same for 2 or more biometric types.

Example:
If fingerprint, iris, face, composite biometric type provided class's name is com.demo.BiometricProvider, instead of creating new instance of the provider class for each biometric type, only one instance will be created and the same will be used to test all the modalities.
```

# Test Results:
Test results are available in **test-results/result_{timestamp}.html**, which will be generated alongside BiometricSDKTest.jar from the start of the application execution.