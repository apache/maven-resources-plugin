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

def lines = new File(basedir, 'target/classes/test.properties').readLines()

// the first line has to survive: it is the reason for the bug report
assert lines.contains('#init test value (true|false) LINE IS essential for this bug report.#')
// $toto.value$ is replaced
assert lines.contains('toto.value=foo')
// #test.value# and @test.value@ are replaced
assert lines.findAll { it.startsWith('test.value=') }.take(2) == ['test.value=true', 'test.value=true']
