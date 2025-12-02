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

import javax.inject.Inject;

import java.io.File;

import org.apache.maven.api.di.Provides;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.plugins.resources.stub.MavenProjectResourcesStub;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MojoTest
class TestResourcesTest {

    @TempDir
    private File testRootDir;

    @Inject
    private MavenProject projectStub;

    @Provides
    @SuppressWarnings("unused")
    private MavenProject projectStub() throws Exception {
        return new MavenProjectResourcesStub(testRootDir.getAbsolutePath());
    }

    /**
     * test mojo lookup, test harness should be working fine
     */
    @Test
    @InjectMojo(goal = "testResources")
    void testHarnessEnvironment(TestResourcesMojo mojo) {
        assertNotNull(mojo);
    }

    @Test
    @InjectMojo(goal = "testResources")
    void testTestResourceDirectoryCreation(TestResourcesMojo mojo) throws Exception {
        MavenProjectResourcesStub project = (MavenProjectResourcesStub) this.projectStub;

        assertNotNull(mojo);

        project.addFile("file4.txt");
        project.addFile("package/file3.nottest");
        project.addFile("notpackage/file1.include");
        project.addFile("package/test/file1.txt");
        project.addFile("notpackage/test/file2.txt");
        project.setupBuildEnvironment();

        mojo.execute();

        String resourcesDir = project.getTestOutputDirectory();

        assertTrue(FileUtils.fileExists(resourcesDir + "/file4.txt"));
        assertTrue(FileUtils.fileExists(resourcesDir + "/package/file3.nottest"));
        assertTrue(FileUtils.fileExists(resourcesDir + "/notpackage/file1.include"));
        assertTrue(FileUtils.fileExists(resourcesDir + "/package/test"));
        assertTrue(FileUtils.fileExists(resourcesDir + "/notpackage/test"));
    }
}
