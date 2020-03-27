# BiometricSDKTest
BiometricSDKTest kit is a java based testing kit which is used to test integration and functionality of various biometrics SDKs with MOSIP. Using this test kit, the SDK vendors can perform various check the compatibility of their SDKs with MOSIP. 

## Pre-requisites
### Java 8
As BiometricSDKTest is a completely java based application, it requires Java runtime environment to run. Java 8 needs to be setup before you use BiometricSDKTest kit. Please execute the below command in command line (for windows) / terminal ( for linux) to check if Java 8 is setup properly or not.
**Command:**
```
java -version
```
**Expected Response:**
```
java version "1.8.0_152"
Java(TM) SE Runtime Environment (build 1.8.0_152-b16)
```

### Configuration
The below configurations should be added before executing the BiometricSDKTest.jar. These configurations should be places in the below file: `config/application.properties`

**Application properties**
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

### Test case preparation
The BiometricSDKTest kit uses a list of test cases as input to execute various scenarios. Each test case contains following parameters seperated by a pipe(|). All the below parameters are mandatory to execute a test case.
* Test case name
* Biometric type
* Test function (list of scenario based test functions are provided below)
* Test data (biometric files)

After preparing the required test cases in the below format, they should be copied to a file, named "test.txt"; and should be provided as an input to the BiometricSDKTest.jar during execution.

_**Note:**_ You can choose to add comments in your "test.txt" file. Lines starting with # are considered as comments.

#### Test case format
```
# comments
testCaseName|biometricType|testFuntion|inputCbeffFiles
```

#### Test case name
Test case Name can be any name based on the user input, this is useful in identifying the test case in the logs.

#### Biometric type
BiometricSDKTest expects any one of the below biometric type for executing a test case:
* finger
* face
* iris
* composite

#### Test function
BiometricSDKTest requires any one of the below test scenario as input for a test case:
```
For Quality Check (supports finger (FIR), finger (FMR), face, iris, composite):
	qualityCheckSuccess
	qualityCheckFail
	qualityCheckInvalidData
	qualityCheckNoInputData
	
	Note: All Quality Check functions expect one input eg. probe_input_data.xml
	
For Match (supports finger (FIR), finger (FMR), face, iris, composite):
	matchSuccess
	matchFail
	matchInvalidData
	matchNoInputData
	
	Note: All Match functions expect two inputs eg. probe_input_data.xml, gallery_input_data.xml
	
For Composite Match (supports finger (FIR), face, iris, composite):
	compositeMatchSuccess
	compositeMatchFail
	compositeMatchInvalidData
	compositeMatchNoInputData
	
	Note: All Composite Match functions expect two inputs eg. probe_input_data.xml, gallery_input_data.xml
	
For Extracting FMR Template (supports finger (FIR)):
	extractTemplateAndCheckQualitySuccess
	extractTemplateAndCheckQualityFail
	extractTemplateInvalidData
	extractTemplateNoInputData
	extractAndMatchFMRSuccess
	extractAndMatchFMRFail	
	
	Note: All Extract functions expect one input eg. probe_input_data.xml
	
For Segmentation (supports finger (FIR), finger (FMR), iris):
	segment
	segmentInvalidData
	segmentNoInputData
	
	Note: All Segmentation functions expect one input eg. probe_input_data.xml
```

#### Test data
BiometricSDKTest supports biometric files which are defined only in CBEFF format as test data. After preparing the input data, place the CBEFF files along with the "BiometricSDKTest.jar" or any of the sub folders.

BiometricSDKTest has two types of Input test data:

**Probe test data: ** Biometric data considered to be captured from user end point and sent for validation against gallery data.
**Gallery test data: ** Biometric data considered to be captured using MDS and validated and stored in cbeff format. Gallery test data is expected to be always valid and will not contain any invalid data.

**_Note:_**
* All test data xml should be in valid cbeff format.
* When placed alongside BiometricSDKTest.jar, input in test case should be in below format (let's consider "inputData.xml" as our input file name).
```
testCaseName|biometricType|testFuntion|inputData.xml
```
* When placed in any of sub folder and sub folder is place alongside BiometricSDKTest.jar, input in test case should be in below format (let's consider inputData.xml as our input file name placed in sub folder called inputs).
```
testCaseName|biometricType|testFuntion|inputs/inputData.xml
```
* When there are multiple input files required, they can be provided with comma(,) seperated.
```
testCaseName|biometricType|testFuntion|inputData1.xml,inputData2.xml
```

## Executing BiometricSDKTest.jar

### Executing BiometricSDKTest.jar for all the Biometiric Modalities
Make sure that, the below files are prepared before running the "BiometricSDKTest.jar".
* Configuration file: application.properties
* List of test cases file
* Input files (if any)
* Vendor SDK(s) and dependent jars (if any)

**To run BiometricSDKTest.jar, use the below command in command prompt (for windows) / terminal (for linux)**
```
java -Dloader.path=<path to vendor SDK(s) and if any dependent jars seperated by comma(,)> -Dbiotest.fingerprint.provider=<Canonical name of fingerprint provider class> -Dbiotest.face.provider=<Canonical name of face provider class> -Dbiotest.iris.provider=<Canonical name of iris provider class> -Dbiotest.composite.provider=<Canonical name of composite provider class> -jar BiometricSDKTest.jar <Test cases file>
```

#### Example
Let's consider the following scenario,

You are a SDK vendor and have a biometric SDK named "vendorSDK.jar" but to run your SDK you need to run a dependent jar called "vendorSupportSDKTest.jar". 
* These two jars should be placed under the lib folder. This "lib" folder should be placed alongside the "BiometricSDKTest.jar". 
* Your SDK should have a biometric provider class called "com.demo.BiometricProvider" which provides support for all biometric modalities (fingerprint/iris/face/composite).
* You should update the "application.properties" with your desired configurations in the "config" folder. This config folder should also be placed alongside "BiometricSDKTest.jar".
* You should copy your list of test cases in "test.txt" file and place it alongside the "BiometricSDKTest.jar".
* You should place the required test data (CBEFF files) alongside the BiometricSDKTest.jar or a sub folder as specified in "test.txt". 

**Sample command to execute BiometricSDKTest.jar:**
```
java -Dloader.path=lib/vendorSDK.jar,lib/vendorSupportSDK.jar -Dbiotest.fingerprint.provider=com.demo.BiometricProvider -Dbiotest.face.provider=com.demo.BiometricProvider -Dbiotest.iris.provider=com.demo.BiometricProvider -Dbiotest.composite.provider=com.demo.BiometricProvider -jar BiometricSDKTest.jar test.txt
```

If there are any initializtion arguments required for biometric provider classes, then it can passed using below sample command. The arguments are expected to be only strings.

**Sample command to execute BiometricSDKTest.jar with additional arguments:**
```
java -Dloader.path=lib/vendorSDK.jar,lib/vendorSupportSDK.jar -Dbiotest.fingerprint.provider=com.demo.BiometricProvider -Dbiotest.fingerprint.provider.args=arg1,arg2 -Dbiotest.face.provider=com.demo.BiometricProvider -Dbiotest.face.provider.args=arg1,arg2 -Dbiotest.iris.provider=com.demo.BiometricProvider -Dbiotest.iris.provider.args=arg1,arg2 -Dbiotest.composite.provider=com.demo.BiometricProvider -Dbiotest.composite.provider=arg1,arg2 -jar BiometricSDKTest.jar test.txt
```

### Executing BiometricSDKTest.jar for all a Single Biometric Modality
BiometricSDKTest also supports testing of only required biometric types. This can be done by passing only the particular biometric type's class name.

#### Example
Let's consider the following scenario,

You are a SDK vendor and have a biometric SDK named "vendorSDK.jar" but to run your SDK you need to run a dependent jar called "vendorSupportSDKTest.jar". 
* These two jars should be placed under the lib folder. This "lib" folder should be placed alongside the "BiometricSDKTest.jar". 
* Your SDK should have a biometric provider class called "com.demo.FingerprintProvider" which provides support for only Fingerprint biometric type.
* You should update the "application.properties" with your desiered configurations in the "config" folder. This config folder should also be placed alongside "BiometricSDKTest.jar".
* You should copy your list of test cases in "test.txt" file and place it alongside the "BiometricSDKTest.jar".
* You should place the required test data (CBEFF files) alongside the BiometricSDKTest.jar or a sub folder as specified in "test.txt". 

**Sample command to execute BiometricSDKTest.jar:**
```
java -Dloader.path=lib/vendorSDK.jar,lib/vendorSupportSDK.jar -Dbiotest.fingerprint.provider=com.demo.FingerprintProvider -jar BiometricSDKTest.jar test.txt
```
**_Note:_** 
* If the vendorSDK.jar is located in a different location, then the complete path of the jar should be provided.
* BiometricSDKTest creates only one instance of the provided Biometric provider classes, if the class name is same for two or more biometric modalities. For example - for executing fingerprint, iris, face, composite biometric modalities, if the provided class's name is "com.demo.BiometricProvider", then instead of creating new instance of the provider class for each biometric type, only one instance will be created and the same will be used to test all the modalities.

## Test Results
Test results would be available in `test-results/result_{timestamp}.html`, which should be generated alongside the BiometricSDKTest.jar from the start of the application execution.
