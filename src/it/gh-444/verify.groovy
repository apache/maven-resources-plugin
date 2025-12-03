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

def propertiesFile = new File(basedir, 'target/classes/test.properties').text
assert propertiesFile.contains('project.version=1.0-SNAPSHOT')
assert propertiesFile.contains('loginSession.refNoCl_cache=cacheNameValue')

def xmlFile = new File(basedir, 'target/classes/test.xml').text
assert xmlFile.contains('<version>${project.version}</version>')

def textFile = new File(basedir, 'target/classes/test.txt')
assert !textFile.exists()
