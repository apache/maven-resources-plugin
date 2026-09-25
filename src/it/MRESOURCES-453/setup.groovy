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

// Pre-populate the output directory with content that differs from the source,
// simulating a later build step that modified the copied resource.
// With changeDetection=NEVER the plugin must leave this file untouched.

import java.nio.file.*

def outputDir = new File(basedir, "target/classes")
outputDir.mkdirs()

def dest = new File(outputDir, "resource.txt")
dest.text = "modified by build step\n"

return true
