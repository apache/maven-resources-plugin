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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Properties;

import org.apache.maven.api.di.Provides;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoParameter;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugins.resources.stub.MavenProjectResourcesStub;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.apache.maven.api.plugin.testing.MojoExtension.setVariableValueToObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MojoTest
class ResourcesMojoTest {

    @TempDir
    private File testRootDir;

    @Inject
    private MavenProject projectStub;

    @Inject
    private MavenSession mavenSession;

    @Provides
    @SuppressWarnings("unused")
    private MavenProject projectStub() {
        return new MavenProjectResourcesStub(testRootDir.getAbsolutePath());
    }

    /**
     * test mojo lookup, test harness should be working fine
     */
    @Test
    @InjectMojo(goal = "resources")
    void testHarnessEnvironment(ResourcesMojo mojo) {
        assertNotNull(mojo);
    }

    @Test
    @InjectMojo(goal = "resources")
    void testResourceDirectoryStructure(ResourcesMojo mojo) throws Exception {
        MavenProjectResourcesStub project = (MavenProjectResourcesStub) this.projectStub;

        assertNotNull(mojo);

        project.addFile("file4.txt");
        project.addFile("package/file3.nottest");
        project.addFile("notpackage/file1.include");
        project.addFile("package/test/file1.txt");
        project.addFile("notpackage/test/file2.txt");
        project.setupBuildEnvironment();

        mojo.execute();

        String resourcesDir = project.getOutputDirectory();

        assertTrue(FileUtils.fileExists(resourcesDir + "/file4.txt"));
        assertTrue(FileUtils.fileExists(resourcesDir + "/package/file3.nottest"));
        assertTrue(FileUtils.fileExists(resourcesDir + "/notpackage/file1.include"));
        assertTrue(FileUtils.fileExists(resourcesDir + "/package/test"));
        assertTrue(FileUtils.fileExists(resourcesDir + "/notpackage/test"));
    }

    @Test
    @InjectMojo(goal = "resources")
    @MojoParameter(name = "outputDirectory", value = "../relative_dir")
    void testResourceDirectoryStructureRelativePath(ResourcesMojo mojo) throws Exception {
        MavenProjectResourcesStub project = (MavenProjectResourcesStub) this.projectStub;

        assertNotNull(mojo);

        project.addFile("file4.txt");
        project.addFile("package/file3.nottest");
        project.addFile("notpackage/file1.include");
        project.addFile("package/test/file1.txt");
        project.addFile("notpackage/test/file2.txt");
        project.setupBuildEnvironment();

        mojo.execute();

        String resourcesDir = mojo.getOutputDirectory().getAbsolutePath();

        assertTrue(FileUtils.fileExists(resourcesDir + "/file4.txt"));
        assertTrue(FileUtils.fileExists(resourcesDir + "/package/file3.nottest"));
        assertTrue(FileUtils.fileExists(resourcesDir + "/notpackage/file1.include"));
        assertTrue(FileUtils.fileExists(resourcesDir + "/package/test"));
        assertTrue(FileUtils.fileExists(resourcesDir + "/notpackage/test"));
    }

    @Test
    @InjectMojo(goal = "resources")
    @MojoParameter(name = "encoding", value = "UTF-8")
    void testResourceEncoding(ResourcesMojo mojo) throws Exception {
        MavenProjectResourcesStub project = (MavenProjectResourcesStub) this.projectStub;

        assertNotNull(mojo);

        project.addFile("file4.txt");
        project.setResourceFiltering(0, true);
        project.setupBuildEnvironment();

        mojo.execute();

        String resourcesDir = project.getOutputDirectory();

        assertTrue(FileUtils.fileExists(resourcesDir + "/file4.txt"));
    }

    @Test
    @InjectMojo(goal = "resources")
    void testResourceInclude(ResourcesMojo mojo) throws Exception {
        MavenProjectResourcesStub project = (MavenProjectResourcesStub) this.projectStub;

        assertNotNull(mojo);

        project.addFile("file1.include");
        project.addFile("file2.exclude");
        project.addFile("file3.nottest");
        project.addFile("file4.txt");
        project.addFile("package/file1.include");
        project.addFile("package/file2.exclude");
        project.addFile("package/file3.nottest");
        project.addFile("package/file4.txt");
        project.addFile("notpackage/file1.include");
        project.addFile("notpackage/file2.exclude");
        project.addFile("notpackage/file3.nottest");
        project.addFile("notpackage/file4.txt");
        project.addFile("package/test/file1.txt");
        project.addFile("package/nottest/file2.txt");
        project.addFile("notpackage/test/file1.txt");
        project.addFile("notpackage/nottest/file.txt");
        project.setupBuildEnvironment();

        project.addInclude("*.include");
        project.addInclude("**/test");
        project.addInclude("**/test/file*");
        project.addInclude("**/package/*.include");

        mojo.execute();

        String resourcesDir = project.getOutputDirectory();

        assertTrue(FileUtils.fileExists(resourcesDir + "/package/test"));
        assertTrue(FileUtils.fileExists(resourcesDir + "/file1.include"));
        assertTrue(FileUtils.fileExists(resourcesDir + "/package/file1.include"));
        assertFalse(FileUtils.fileExists(resourcesDir + "/notpackage/file1.include"));
        assertFalse(FileUtils.fileExists(resourcesDir + "/notpackage/nottest/file.txt"));
    }

    @Test
    @InjectMojo(goal = "resources")
    void testResourceExclude(ResourcesMojo mojo) throws Exception {
        MavenProjectResourcesStub project = (MavenProjectResourcesStub) this.projectStub;

        assertNotNull(mojo);

        project.addFile("file1.include");
        project.addFile("file2.exclude");
        project.addFile("file3.nottest");
        project.addFile("file4.txt");
        project.addFile("package/file1.include");
        project.addFile("package/file2.exclude");
        project.addFile("package/file3.nottest");
        project.addFile("package/file4.txt");
        project.addFile("notpackage/file1.include");
        project.addFile("notpackage/file2.exclude");
        project.addFile("notpackage/file3.nottest");
        project.addFile("notpackage/file4.txt");
        project.addFile("package/test/file1.txt");
        project.addFile("package/nottest/file2.txt");
        project.addFile("notpackage/test/file1.txt");
        project.addFile("notpackage/nottest/file.txt");
        project.setupBuildEnvironment();

        project.addExclude("**/*.exclude");
        project.addExclude("**/nottest*");
        project.addExclude("**/notest");
        project.addExclude("**/notpackage*");
        project.addExclude("**/notpackage*/**");

        mojo.execute();

        String resourcesDir = project.getOutputDirectory();

        assertTrue(FileUtils.fileExists(resourcesDir + "/package/test"));
        assertTrue(FileUtils.fileExists(resourcesDir + "/file1.include"));
        assertTrue(FileUtils.fileExists(resourcesDir + "/package/file1.include"));
        assertFalse(FileUtils.fileExists(resourcesDir + "/notpackage/file1.include"));
        assertFalse(FileUtils.fileExists(resourcesDir + "/notpackage/nottest/file.txt"));
    }

    @Test
    @InjectMojo(goal = "resources")
    void testResourceTargetPath(ResourcesMojo mojo) throws Exception {
        MavenProjectResourcesStub project = (MavenProjectResourcesStub) this.projectStub;

        assertNotNull(mojo);

        project.setTargetPath("org/apache/maven/plugin/test");

        project.addFile("file4.txt");
        project.addFile("package/file3.nottest");
        project.addFile("notpackage/file1.include");
        project.addFile("package/test/file1.txt");
        project.addFile("notpackage/test/file2.txt");
        project.setupBuildEnvironment();

        mojo.execute();

        String resourcesDir = project.getOutputDirectory();

        assertTrue(FileUtils.fileExists(resourcesDir + "/org/apache/maven/plugin/test/file4.txt"));
        assertTrue(FileUtils.fileExists(resourcesDir + "/org/apache/maven/plugin/test/package/file3.nottest"));
        assertTrue(FileUtils.fileExists(resourcesDir + "/org/apache/maven/plugin/test/notpackage/file1.include"));
        assertTrue(FileUtils.fileExists(resourcesDir + "/org/apache/maven/plugin/test/package/test"));
        assertTrue(FileUtils.fileExists(resourcesDir + "/org/apache/maven/plugin/test/notpackage/test"));
    }

    @Test
    @InjectMojo(goal = "resources")
    void testResourceSystemPropertiesFiltering(ResourcesMojo mojo) throws Exception {
        MavenProjectResourcesStub project = (MavenProjectResourcesStub) this.projectStub;

        assertNotNull(mojo);

        project.addFile("file4.txt", "current-working-directory = ${user.dir}");
        project.setResourceFiltering(0, true);
        project.setupBuildEnvironment();

        MavenExecutionRequest request = new DefaultMavenExecutionRequest();
        request.setSystemProperties(System.getProperties());

        mavenSession.getSystemProperties().putAll(System.getProperties());

        mojo.execute();

        String resourcesDir = project.getOutputDirectory();

        File userDir = new File(System.getProperty("user.dir"));
        assertTrue(userDir.exists());

        Properties props = new Properties();
        try (FileInputStream inStream = new FileInputStream(new File(resourcesDir, "file4.txt"))) {
            props.load(inStream);
        }
        File fileFromFiltering = new File(props.getProperty("current-working-directory"));

        assertTrue(fileFromFiltering.exists(), fileFromFiltering.getAbsolutePath() + " does not exist.");
        assertEquals(userDir.getAbsolutePath(), fileFromFiltering.getAbsolutePath());
    }

    @Test
    @InjectMojo(goal = "resources")
    void testResourceProjectPropertiesFiltering(ResourcesMojo mojo) throws Exception {
        MavenProjectResourcesStub project = (MavenProjectResourcesStub) this.projectStub;

        assertNotNull(mojo);

        project.addFile("file4.txt", "current working directory = ${user.dir}");
        project.setResourceFiltering(0, true);
        project.addProperty("user.dir", "FPJ kami!!!");
        project.setupBuildEnvironment();

        mojo.execute();

        String resourcesDir = project.getOutputDirectory();
        String checkString = "current working directory = FPJ kami!!!";

        assertContent(resourcesDir + "/file4.txt", checkString);
    }

    @Test
    @InjectMojo(goal = "resources")
    void testProjectPropertyFilteringPropertyDestination(ResourcesMojo mojo) throws Exception {
        MavenProjectResourcesStub project = (MavenProjectResourcesStub) this.projectStub;

        assertNotNull(mojo);

        project.addFile("file4.properties", "current working directory=${description}");
        project.setResourceFiltering(0, true);
        project.setupBuildEnvironment();

        // setup dummy property
        project.setDescription("c:\\\\org\\apache\\test");

        mojo.execute();

        String resourcesDir = project.getOutputDirectory();
        String checkString = "current working directory=c:\\\\org\\\\apache\\\\test";

        assertContent(resourcesDir + "/file4.properties", checkString);
    }

    @Test
    @InjectMojo(goal = "resources")
    void testPropertyFilesFiltering(ResourcesMojo mojo) throws Exception {
        MavenProjectResourcesStub project = (MavenProjectResourcesStub) this.projectStub;
        LinkedList<String> filterList = new LinkedList<>();

        assertNotNull(mojo);

        project.addFile("file4.properties", "current working directory=${dir}");
        project.addFile("filter.properties", "dir:testdir");
        project.setResourceFiltering(0, true);
        project.setupBuildEnvironment();
        filterList.add(project.getResourcesDirectory() + "filter.properties");

        setVariableValueToObject(mojo, "buildFilters", filterList);

        mojo.execute();

        String resourcesDir = project.getOutputDirectory();
        String checkString = "current working directory=testdir";

        assertContent(resourcesDir + "/file4.properties", checkString);
    }

    @Test
    @InjectMojo(goal = "resources")
    void testPropertyFilesExtra(ResourcesMojo mojo) throws Exception {
        MavenProjectResourcesStub project = (MavenProjectResourcesStub) this.projectStub;
        LinkedList<String> filterList = new LinkedList<>();

        assertNotNull(mojo);

        project.addFile("extra.properties", "current working directory=${dir}");
        project.addFile("filter.properties", "dir:testdir");
        project.setResourceFiltering(0, true);
        project.setupBuildEnvironment();
        filterList.add(project.getResourcesDirectory() + "filter.properties");

        setVariableValueToObject(mojo, "filters", filterList);

        mojo.execute();

        String resourcesDir = project.getOutputDirectory();
        String checkString = "current working directory=testdir";

        assertContent(resourcesDir + "/extra.properties", checkString);
    }

    @Test
    @InjectMojo(goal = "resources")
    void testPropertyFilesMainAndExtra(ResourcesMojo mojo) throws Exception {
        MavenProjectResourcesStub project = (MavenProjectResourcesStub) this.projectStub;
        LinkedList<String> filterList = new LinkedList<>();
        LinkedList<String> extraFilterList = new LinkedList<>();

        assertNotNull(mojo);

        project.addFile("main-extra.properties", "current working directory=${dir}; old working directory=${dir2}");
        project.addFile("filter.properties", "dir:testdir");
        project.addFile("extra-filter.properties", "dir2:testdir2");
        project.setResourceFiltering(0, true);

        project.setupBuildEnvironment();

        filterList.add(project.getResourcesDirectory() + "filter.properties");
        extraFilterList.add(project.getResourcesDirectory() + "extra-filter.properties");

        setVariableValueToObject(mojo, "buildFilters", filterList);
        setVariableValueToObject(mojo, "filters", extraFilterList);

        mojo.execute();

        String resourcesDir = project.getOutputDirectory();
        String checkString = "current working directory=testdir; old working directory=testdir2";

        File file = new File(resourcesDir, "main-extra.properties");
        assertContent(file.getAbsolutePath(), checkString);
    }

    /**
     * Validates that a Filter token containing a project property will be resolved before the Filter is applied to the
     * resources.
     */
    @Test
    @InjectMojo(goal = "resources")
    void testPropertyFilesFilteringTokensInFilters(ResourcesMojo mojo) throws Exception {
        final MavenProjectResourcesStub project = (MavenProjectResourcesStub) this.projectStub;
        final LinkedList<String> filterList = new LinkedList<>();

        assertNotNull(mojo);

        project.addFile("file4.properties", "current working directory=${filter.token}");
        project.addFile("filter.properties", "filter.token=${pom-property}");
        project.setResourceFiltering(0, true);
        project.addProperty("pom-property", "foobar");
        project.setupBuildEnvironment();
        filterList.add(project.getResourcesDirectory() + "filter.properties");

        setVariableValueToObject(mojo, "buildFilters", filterList);

        mojo.execute();
        final String resourcesDir = project.getOutputDirectory();

        final String checkString = "current working directory=foobar";

        assertContent(resourcesDir + "/file4.properties", checkString);
    }

    @Test
    @InjectMojo(goal = "resources")
    @MojoParameter(name = "escapeWindowsPaths", value = "false")
    void testWindowsPathEscapingDisabled(ResourcesMojo mojo) throws Exception {
        MavenProjectResourcesStub project = (MavenProjectResourcesStub) this.projectStub;

        assertNotNull(mojo);

        project.getProperties().setProperty("basePath", "C:\\Users\\Administrator");
        project.getProperties().setProperty("docsPath", "${basePath}\\Documents");

        project.addFile("path-listing.txt", "base path is ${basePath}\ndocuments path is ${docsPath}");
        project.setResourceFiltering(0, true);

        project.setupBuildEnvironment();

        mojo.execute();

        String resourcesDir = project.getOutputDirectory();

        assertTrue(FileUtils.fileExists(new File(resourcesDir, "path-listing.txt").getAbsolutePath()));

        assertEquals(
                "base path is C:\\Users\\Administrator\ndocuments path is C:\\Users\\Administrator\\Documents",
                FileUtils.fileRead(new File(resourcesDir, "path-listing.txt")));
    }

    @Test
    @InjectMojo(goal = "resources")
    @MojoParameter(name = "escapeWindowsPaths", value = "true")
    void testWindowsPathEscapingEnabled(ResourcesMojo mojo) throws Exception {
        MavenProjectResourcesStub project = (MavenProjectResourcesStub) this.projectStub;

        assertNotNull(mojo);

        project.getProperties().setProperty("basePath", "C:\\Users\\Administrator");
        project.getProperties().setProperty("docsPath", "${basePath}\\Documents");

        project.addFile("path-listing.txt", "base path is ${basePath}\ndocuments path is ${docsPath}");
        project.setResourceFiltering(0, true);

        project.setupBuildEnvironment();

        mojo.execute();

        String resourcesDir = project.getOutputDirectory();

        assertTrue(FileUtils.fileExists(new File(resourcesDir, "path-listing.txt").getAbsolutePath()));

        assertEquals(
                "base path is C:\\\\Users\\\\Administrator\ndocuments path is C:\\\\Users\\\\Administrator\\\\Documents",
                FileUtils.fileRead(new File(resourcesDir, "path-listing.txt")));
    }

    /**
     * Ensures the file exists and its first line equals the given data.
     */
    private void assertContent(String fileName, String data) throws IOException {
        assertTrue(FileUtils.fileExists(fileName));
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            assertEquals(data, reader.readLine());
        }
    }
}
