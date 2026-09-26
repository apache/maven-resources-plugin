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

// MRESOURCES-453: changeDetection=NEVER must not overwrite a destination file that already exists,
// even when the source content differs from what is currently on disk.

import java.nio.file.*

def outputDir = new File(basedir, "target/classes")
def dest = new File(outputDir, "resource.txt")

// Pre-condition: the invoker should have run process-resources once already (setup via invoker.properties).
// We verify the file exists with the pre-placed sentinel content, not with the source content.
assert dest.exists() : "Destination file must exist after process-resources"
assert dest.text == "modified by build step\n" : "changeDetection=NEVER must leave the existing file untouched; found: '${dest.text}'"

return true
