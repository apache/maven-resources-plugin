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
package org.apache.maven.plugins.resources;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import org.apache.maven.api.Project;
import org.apache.maven.api.di.Provides;
import org.apache.maven.api.di.Singleton;
import org.apache.maven.api.plugin.testing.Basedir;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.api.plugin.testing.stubs.SessionMock;
import org.apache.maven.impl.InternalSession;
import org.apache.maven.plugins.resources.stub.MavenProjectSourcesStub;
import org.apache.maven.shared.filtering.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.apache.maven.api.plugin.testing.MojoExtension.getBasedir;
import static org.apache.maven.api.plugin.testing.MojoExtension.getPluginBasedir;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Olivier Lamy
 */
@MojoTest
public class CopyResourcesMojoTest {

    @Test
    @InjectMojo(goal = "resources", pom = "classpath:/unit/resources-test/plugin-config.xml")
    @Basedir
    public void testCopyWithoutFiltering(ResourcesMojo mojo) throws Exception {
        Resource resource = new Resource();
        resource.setDirectory(getPluginBasedir() + "/src/test/unit-files/copy-resources-test/no-filter");
        resource.setFiltering(false);
        mojo.setResources(Collections.singletonList(resource));

        mojo.execute();

        Path result = mojo.outputDirectory.resolve("config.properties");
        assertTrue(Files.exists(result), result + " does not exist");
    }

    private static final String LOCAL_REPO = "/target/local-repo";

    @Provides
    @Singleton
    @SuppressWarnings("unused")
    private static InternalSession getMockSession() {
        return SessionMock.getMockSession(getBasedir() + LOCAL_REPO);
    }

    @Provides
    @Singleton
    @SuppressWarnings("unused")
    private static Project createProject(ExtensionContext context) throws Exception {
        return new MavenProjectSourcesStub();
    }
}
