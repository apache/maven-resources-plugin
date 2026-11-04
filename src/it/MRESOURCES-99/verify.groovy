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

def resource = new File(basedir, 'target/classes/SomeResource.txt')
assert resource.isFile() : 'target/classes/SomeResource.txt is missing'

def props = new Properties()
resource.withReader('ISO-8859-1') { reader -> props.load(reader) }

// each of these keys has to be present in the filtered file
def keysWhichHaveToExist = [
    'timestamp', 'at.timestamp', 'build.timestamp', 'project.build.timestamp',
    'baseUri', 'project.baseUri', 'pom.baseUri', 'at.baseUri',
    'groupId', 'basedir', 'project.basedir',
]
for (key in keysWhichHaveToExist) {
    assert props.containsKey(key) : "Missing the key '${key}'"
}

// ${maven.build.timestamp} and @maven.build.timestamp@ are replaced by the plugin
def timestampFormat = /\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z/
assert props['timestamp'] ==~ timestampFormat : "\${maven.build.timestamp} was not replaced: ${props['timestamp']}"
assert props['at.timestamp'] == props['timestamp'] : "@maven.build.timestamp@ was not replaced the same way: ${props['at.timestamp']}"

// only these expressions are replaced
assert props['project.baseUri'] == basedir.getAbsoluteFile().toURI().toString()
assert props['groupId'] == 'org.apache.maven.plugins'
assert props['basedir'] == basedir.getAbsolutePath()
assert props['project.basedir'] == basedir.getAbsolutePath()

// these have no value in the filtering context and stay as they are
assert props['build.timestamp'] == '${build.timestamp}'
assert props['project.build.timestamp'] == '${project.build.timestamp}'
assert props['baseUri'] == '${baseUri}'
assert props['pom.baseUri'] == '${pom.baseUri}'
assert props['at.baseUri'] == '@baseUri@'
