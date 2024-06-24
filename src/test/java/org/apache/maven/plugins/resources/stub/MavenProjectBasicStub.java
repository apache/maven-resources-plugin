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
package org.apache.maven.plugins.resources.stub;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.maven.api.plugin.testing.MojoExtension;
import org.apache.maven.api.plugin.testing.stubs.ProjectStub;

public class MavenProjectBasicStub extends ProjectStub {
    protected String identifier;

    protected String testRootDir;

    protected Properties properties;

    protected String description;

    public MavenProjectBasicStub(String id) throws IOException {
        properties = new Properties();
        identifier = id;
        testRootDir = MojoExtension.getBasedir() + "/target/test-classes/unit/test-dir/" + identifier;
        setBasedir(Paths.get(testRootDir));

        Path path = Paths.get(testRootDir);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        setName("Test Project " + identifier);
        setGroupId("org.apache.maven.plugin.test");
        setArtifactId("maven-resource-plugin-test#" + identifier);
        setVersion(identifier);
        setPackaging("org.apache.maven.plugin.test");
        setDescription("this is a test project");
    }
}
