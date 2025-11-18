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

// Verify that the build succeeded and resources were processed
File target = new File(basedir, "target/classes")
assert target.exists() : "target/classes directory should exist"

File testProps = new File(target, "test.properties")
assert testProps.exists() : "test.properties should be copied to target/classes"

// Verify filtering worked (project.name should be replaced)
String content = testProps.text
assert content.contains("GH-312") : "Filtering should have replaced project.name"
assert content.contains("1.0-SNAPSHOT") : "Filtering should have replaced project.version"

println "✓ Maven 4 compatibility test passed - resources processed successfully (GH-312)"
return true
