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

// MRESOURCES-301: binary .jar files must be binary-copied by default (no nonFilteredFileExtensions config needed)

import java.io.*;
import java.nio.file.*;

boolean filesAreIdentical( File expected, File current ) throws IOException {
    if ( expected.length() != current.length() ) {
        return false;
    }
    byte[] expectedBytes = Files.readAllBytes( expected.toPath() );
    byte[] currentBytes  = Files.readAllBytes( current.toPath() );
    return Arrays.equals( expectedBytes, currentBytes );
}

File src    = new File( basedir, "src/main/resources/lib/binary.jar" );
File target = new File( basedir, "target/classes/lib/binary.jar" );

if ( !target.exists() ) {
    System.err.println( "MRESOURCES-301: target/classes/lib/binary.jar does not exist" );
    return false;
}

if ( !filesAreIdentical( src, target ) ) {
    System.err.println( "MRESOURCES-301: binary.jar was modified during filtering — content is not identical to source" );
    return false;
}

System.out.println( "MRESOURCES-301: binary.jar was binary-copied correctly without nonFilteredFileExtensions config" );
return true;
