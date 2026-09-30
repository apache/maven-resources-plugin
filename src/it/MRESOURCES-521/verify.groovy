/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

// MRESOURCES-521: per-resource <nonFilteredFiles> glob patterns

import java.io.*
import java.nio.file.*

// --- 1) Verify the binary .p12 file was copied byte-for-byte ---

File src    = new File( basedir, "src/main/resources/certs/keystore.p12" )
File target = new File( basedir, "target/classes/certs/keystore.p12" )

if ( !target.exists() ) {
    System.err.println( "MRESOURCES-521: target/classes/certs/keystore.p12 does not exist" )
    return false
}

byte[] expectedBytes = Files.readAllBytes( src.toPath() )
byte[] actualBytes   = Files.readAllBytes( target.toPath() )

if ( !Arrays.equals( expectedBytes, actualBytes ) ) {
    System.err.println( "MRESOURCES-521: keystore.p12 was modified during filtering — content is not identical to source" )
    System.err.println( "  source size: " + expectedBytes.length + ", target size: " + actualBytes.length )
    return false
}

System.out.println( "MRESOURCES-521: keystore.p12 was binary-copied correctly (nonFilteredFiles pattern matched)" )

// --- 2) Verify that text files NOT matched by the pattern were still filtered ---

File propsTarget = new File( basedir, "target/classes/config.properties" )

if ( !propsTarget.exists() ) {
    System.err.println( "MRESOURCES-521: target/classes/config.properties does not exist" )
    return false
}

String propsContent = propsTarget.text
if ( propsContent.contains( '${project.version}' ) ) {
    System.err.println( "MRESOURCES-521: config.properties was NOT filtered — still contains \${project.version}" )
    return false
}
if ( !propsContent.contains( "1.0-SNAPSHOT" ) ) {
    System.err.println( "MRESOURCES-521: config.properties does not contain the resolved version 1.0-SNAPSHOT" )
    return false
}

System.out.println( "MRESOURCES-521: config.properties was correctly filtered (nonFilteredFiles did not suppress it)" )

return true
