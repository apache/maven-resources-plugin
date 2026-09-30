// Verify gracefulBinaryHandling: the binary .p12 must be copied byte-for-byte,
// and the text .properties must be filtered normally.

import java.nio.file.*

def outputDir = new File(basedir, 'target/classes')

// 1. Binary file must exist and be byte-for-byte identical to the source
def srcBinary = new File(basedir, 'src/main/resources/certs/keystore.p12')
def dstBinary = new File(outputDir, 'certs/keystore.p12')
assert dstBinary.exists() : "Binary file was not copied: ${dstBinary}"
assert Arrays.equals(srcBinary.bytes, dstBinary.bytes) : \
    "Binary file content was altered by filtering (gracefulBinaryHandling did not copy it as-is)"

// 2. Text properties file must exist and have filtered values
def props = new File(outputDir, 'config.properties')
assert props.exists() : "config.properties was not copied: ${props}"
def content = props.text
assert content.contains('MRESOURCES-graceful-binary') : \
    "config.properties does not contain filtered artifactId: ${content}"
assert content.contains('1.0') : \
    "config.properties does not contain filtered version: ${content}"
assert !content.contains('${project.') : \
    "config.properties still contains unresolved placeholders: ${content}"
