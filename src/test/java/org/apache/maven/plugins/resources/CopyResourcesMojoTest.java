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

import javax.inject.Singleton;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import com.google.inject.Provides;
import org.apache.maven.api.Project;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.api.plugin.testing.stubs.ProjectStub;
import org.apache.maven.api.plugin.testing.stubs.SessionStub;
import org.apache.maven.internal.impl.InternalSession;
import org.apache.maven.plugins.resources.stub.MavenProjectResourcesStub;
import org.apache.maven.shared.filtering.Resource;
import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.apache.maven.api.plugin.testing.MojoExtension.setVariableValueToObject;
import static org.codehaus.plexus.testing.PlexusExtension.getBasedir;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Olivier Lamy
 * @version $Id$
 */
@MojoTest
public class CopyResourcesMojoTest {

    protected static final String defaultPomFilePath = "/target/test-classes/unit/resources-test/plugin-config.xml";

    Path outputDirectory = Paths.get(getBasedir(), "/target/copyResourcesTests");

    @BeforeEach
    protected void setUp() throws Exception {
        if (!Files.exists(outputDirectory)) {
            Files.createDirectories(outputDirectory);
        } else {
            FileUtils.cleanDirectory(outputDirectory.toFile());
        }
    }

    @Test
    @InjectMojo(goal = "resources", pom = "classpath:/unit/resources-test/plugin-config.xml")
    public void testCopyWithoutFiltering(ResourcesMojo mojo) throws Exception {
        mojo.setOutputDirectory(outputDirectory);

        Resource resource = new Resource();
        resource.setDirectory(getBasedir() + "/src/test/unit-files/copy-resources-test/no-filter");
        resource.setFiltering(false);

        mojo.setResources(Collections.singletonList(resource));

        MavenProjectResourcesStub project = new MavenProjectResourcesStub("CopyResourcesMojoTest");
        Path targetFile = Paths.get(getBasedir(), "target/copyResourcesTests");
        project.setBasedir(targetFile);
        setVariableValueToObject(mojo, "project", project);
        mojo.execute();

        assertTrue(Files.exists(targetFile.resolve("config.properties")));
    }

    private static final String LOCAL_REPO = getBasedir() + "/target/local-repo";

    @Provides
    @Singleton
    @SuppressWarnings("unused")
    private InternalSession getMockSession() {
        return SessionStub.getMockSession(LOCAL_REPO);
    }

    @Provides
    @Singleton
    @SuppressWarnings("unused")
    private Project createProject() {
        return new ProjectStub();
    }
}
