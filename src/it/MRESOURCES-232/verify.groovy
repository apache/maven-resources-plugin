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
// MRESOURCES-232: Verify that outputEncoding enables encoding conversion during filtering.
// The source file (message.txt) is ISO-8859-1-encoded and contains a filter token and an
// é character (0xE9 in Latin-1). After filtering with encoding=ISO-8859-1 and
// outputEncoding=UTF-8 the output file must:
//   1. Have the token expanded (${world} → World)
//   2. Encode é as UTF-8 (0xC3 0xA9)

File output = new File( basedir, 'target/classes/message.txt' );
assert output.exists() : "message.txt was not copied";

// Read as UTF-8 — if the file were still Latin-1 this would produce garbage for é
String content = output.getText( 'UTF-8' );
assert content.trim() == 'Bonjour World! caf\u00e9' : "Unexpected content: '${content.trim()}'"

// Also verify the raw bytes: é should be the two-byte UTF-8 sequence 0xC3 0xA9
byte[] bytes = output.bytes;
String hex = bytes.collect { String.format('%02x', it & 0xFF) }.join(' ');
assert hex.contains('c3 a9') : "Expected UTF-8 é (c3 a9) in output bytes, got: ${hex}"

return true;
