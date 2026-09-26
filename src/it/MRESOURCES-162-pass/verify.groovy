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

// MRESOURCES-162: when failOnMissingFilterValue is not set (defaults to false),
// the build succeeds and the unresolved placeholder is passed through as-is.
import org.codehaus.plexus.util.FileUtils

def resource = new File( basedir, "target/classes/resource.txt" )
assert resource.exists()

def content = FileUtils.fileRead( resource )
// defined.property must be resolved
assert content.contains( "defined=hello" )
// missing.property must be passed through literally
assert content.contains( 'missing=${missing.property}' )
return true
