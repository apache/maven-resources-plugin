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

// MRESOURCES-286: Verify per-resource changeDetection override.
//
// Two resources are configured with different per-resource changeDetection strategies:
//   1. resources-never/never.txt   → changeDetection=NEVER
//   2. resources-always/always.txt → changeDetection=ALWAYS
//
// Before the build, setup.groovy pre-populated target/classes/never.txt with sentinel content
// that differs from the source.  After process-resources:
//   - never.txt must still contain the sentinel (NEVER = do not overwrite existing dest file)
//   - always.txt must contain the source content (ALWAYS = unconditionally copy)

def outputDir = new File(basedir, "target/classes")

// 1) NEVER — the pre-existing file must be preserved
def neverDest = new File(outputDir, "never.txt")
assert neverDest.exists() : "never.txt must exist after process-resources"
assert neverDest.text.normalize().trim() == "sentinel \u2014 do not overwrite" :
        "changeDetection=NEVER must leave the existing file untouched; found: '${neverDest.text}'"

// 2) ALWAYS — the source content must be copied
def alwaysDest = new File(outputDir, "always.txt")
assert alwaysDest.exists() : "always.txt must exist after process-resources"
assert alwaysDest.text.normalize().trim() == "always-overwrite me" :
        "changeDetection=ALWAYS must copy the source content; found: '${alwaysDest.text}'"

return true
