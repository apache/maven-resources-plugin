
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

File target = new File(basedir, "target")
assert target.isDirectory() : "target file is missing or a directory."

File someResource = new File( target, "/classes/filter.properties" )
assert someResource.isFile() : "file.txt is missing or not a file."

Properties p = new Properties()
someResource.withInputStream { p.load( it ) }

File f = new File( p.getProperty( "location" ) )
assert f.exists() : "Property was not correctly filtered: " + f + "\n" + f.getAbsolutePath()

File filteredBasedir = new File( p.getProperty( "basedir" ) )
assert filteredBasedir.exists() : "Property was not correctly filtered: " + filteredBasedir + "\n" + filteredBasedir.getAbsolutePath()

File expectedLocation = new File( filteredBasedir, "src/main/resources/file.txt" )
assert expectedLocation.equals( f ) : "Files doesn't match:\n" + filteredBasedir + "\n" + expectedLocation
