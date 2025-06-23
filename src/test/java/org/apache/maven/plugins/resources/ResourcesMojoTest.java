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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

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

import static org.apache.maven.api.plugin.testing.MojoExtension.getBasedir;
import static org.apache.maven.api.plugin.testing.MojoExtension.setVariableValueToObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MojoTest
public class ResourcesMojoTest {

    private static final String CONFIG_XML = "classpath:/unit/resources-test/plugin-config.xml";

    /**
     * Tests mojo lookup, test harness should be working fine.
     *
     * @param  mojo  the <abbr>MOJO</abbr> to test
     */
    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testHarnessEnvironment(ResourcesMojo mojo) {
        assertNotNull(mojo);
    }

    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testResourceDirectoryStructure(ResourcesMojo mojo) throws Exception {
        testResourceDirectoryStructure(mojo, false);
    }

    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testResourceDirectoryStructureRelativePath(ResourcesMojo mojo) throws Exception {
        testResourceDirectoryStructure(mojo, true);
    }

    /**
     * Code shared by {@code testResourceDirectoryStructure(…)}
     * and {@code testResourceDirectoryStructureRelativePath(…)}.
     * Those two tests are almost identical.
     */
    private static void testResourceDirectoryStructure(ResourcesMojo mojo, boolean relative) throws Exception {
        assertNotNull(mojo);
        final var project = (MavenProjectSourcesStub) mojo.project;
        if (relative) {
            project.setOutputDirectory("../relative_dir");
        }
        Path packageDir = Path.of("package");
        Path notpackageDir = Path.of("notpackage");
        project.addFile(Path.of("file4.txt"));
        project.addFile(packageDir.resolve("file3.nottest"));
        project.addFile(notpackageDir.resolve("file1.include"));
        project.addFile(packageDir.resolve("test").resolve("file1.txt"));
        project.addFile(notpackageDir.resolve("test").resolve("file2.txt"));
        project.setupBuildEnvironment();

        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "outputDirectory", project.getOutputDirectory());
        setVariableValueToObject(mojo, "buildFilters", Collections.emptyList());
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        mojo.execute();

        Path outputDirectory = project.getOutputDirectory();
        packageDir = outputDirectory.resolve("package");
        notpackageDir = outputDirectory.resolve("notpackage");
        assertTrue(Files.exists(outputDirectory.resolve("file4.txt")));
        assertTrue(Files.exists(packageDir.resolve("file3.nottest")));
        assertTrue(Files.exists(notpackageDir.resolve("file1.include")));
        assertTrue(Files.exists(packageDir.resolve("test")));
        assertTrue(Files.exists(notpackageDir.resolve("test")));
    }

    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testResourceEncoding(ResourcesMojo mojo) throws Exception {
        assertNotNull(mojo);
        final var project = (MavenProjectSourcesStub) mojo.project;
        project.addFile(Path.of("file4.txt"));
        project.setResourceFiltering(true);
        project.setupBuildEnvironment();

        setVariableValueToObject(mojo, "encoding", "UTF-8");
        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "outputDirectory", project.getOutputDirectory());
        setVariableValueToObject(mojo, "buildFilters", Collections.emptyList());
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        mojo.execute();

        Path outputDirectory = project.getOutputDirectory();
        assertTrue(Files.exists(outputDirectory.resolve("file4.txt")));
    }

    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testResourceInclude(ResourcesMojo mojo) throws Exception {
        testResourceIncludeOrExclude(mojo, false);
    }

    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testResourceExclude(ResourcesMojo mojo) throws Exception {
        testResourceIncludeOrExclude(mojo, true);
    }

    /**
     * Code shared by {@code testResourceInclude(…)} and {@code testResourceExclude(…)}.
     * Those two tests are almost identical.
     */
    private static void testResourceIncludeOrExclude(ResourcesMojo mojo, boolean exclude) throws Exception {
        assertNotNull(mojo);
        final var project = (MavenProjectSourcesStub) mojo.project;
        Path packageDir = Path.of("package");
        Path notpackageDir = Path.of("notpackage");
        project.addFile(Path.of("file1.include"));
        project.addFile(Path.of("file2.exclude"));
        project.addFile(Path.of("file3.nottest"));
        project.addFile(Path.of("file4.txt"));
        project.addFile(packageDir.resolve("file1.include"));
        project.addFile(packageDir.resolve("file2.exclude"));
        project.addFile(packageDir.resolve("file3.nottest"));
        project.addFile(packageDir.resolve("file4.txt"));
        project.addFile(notpackageDir.resolve("file1.include"));
        project.addFile(notpackageDir.resolve("file2.exclude"));
        project.addFile(notpackageDir.resolve("file3.nottest"));
        project.addFile(notpackageDir.resolve("file4.txt"));
        project.addFile(packageDir.resolve("test").resolve("file1.txt"));
        project.addFile(packageDir.resolve("nottest").resolve("file2.txt"));
        project.addFile(notpackageDir.resolve("test").resolve("file1.txt"));
        project.addFile(notpackageDir.resolve("nottest").resolve("file.txt"));
        project.setupBuildEnvironment();
        if (exclude) {
            project.addExcludes("**/*.exclude", "**/nottest*", "**/notest", "**/notpackage*", "**/notpackage*/**");
        } else {
            project.addIncludes("*.include", "**/test", "**/test/file*", "**/package/*.include");
        }
        setVariableValueToObject(mojo, "outputDirectory", project.getOutputDirectory());
        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "buildFilters", Collections.emptyList());
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        mojo.execute();

        Path outputDirectory = project.getOutputDirectory();
        packageDir = outputDirectory.resolve("package");
        notpackageDir = outputDirectory.resolve("notpackage");
        assertTrue(Files.exists(packageDir.resolve("test")));
        assertTrue(Files.exists(outputDirectory.resolve("file1.include")));
        assertTrue(Files.exists(packageDir.resolve("file1.include")));
        assertFalse(Files.exists(notpackageDir.resolve("file1.include")));
        assertFalse(Files.exists(notpackageDir.resolve("nottest").resolve("file.txt")));
    }

    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testResourceTargetPath(ResourcesMojo mojo) throws Exception {
        assertNotNull(mojo);
        final var project = (MavenProjectSourcesStub) mojo.project;
        project.setTargetPath("org/apache/maven/plugin/test");
        project.addFile(Path.of("file4.txt"));
        project.addFile(Path.of("package", "file3.nottest"));
        project.addFile(Path.of("notpackage", "file1.include"));
        project.addFile(Path.of("package", "test", "file1.txt"));
        project.addFile(Path.of("notpackage", "test", "file2.txt"));
        project.setupBuildEnvironment();

        setVariableValueToObject(mojo, "outputDirectory", project.getOutputDirectory());
        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "buildFilters", Collections.emptyList());
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        mojo.execute();

        Path outputDirectory = project.getOutputDirectory();
        assertTrue(Files.exists(outputDirectory.resolve("org/apache/maven/plugin/test/file4.txt")));
        assertTrue(Files.exists(outputDirectory.resolve("org/apache/maven/plugin/test/package/file3.nottest")));
        assertTrue(Files.exists(outputDirectory.resolve("org/apache/maven/plugin/test/notpackage/file1.include")));
        assertTrue(Files.exists(outputDirectory.resolve("org/apache/maven/plugin/test/package/test")));
        assertTrue(Files.exists(outputDirectory.resolve("org/apache/maven/plugin/test/notpackage/test")));
    }

    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testResourceSystemPropertiesFiltering(ResourcesMojo mojo) throws Exception {
        assertNotNull(mojo);
        final var project = (MavenProjectSourcesStub) mojo.project;
        project.addFile(Path.of("file4.txt"), "current-working-directory = ${user.dir}");
        project.setResourceFiltering(true);
        project.setupBuildEnvironment();

        // setVariableValueToObject(mojo,"encoding","UTF-8");
        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "outputDirectory", project.getOutputDirectory());
        setVariableValueToObject(mojo, "buildFilters", Collections.emptyList());
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        setVariableValueToObject(mojo, "escapeWindowsPaths", Boolean.TRUE);

        mojo.session.getSystemProperties().put("user.dir", System.getProperty("user.dir"));
        mojo.execute();

        Path outputDirectory = project.getOutputDirectory();
        Path userDir = Path.of(System.getProperty("user.dir"));
        assertTrue(Files.exists(userDir));

        Properties props = new Properties();
        try (InputStream inStream = Files.newInputStream(outputDirectory.resolve("file4.txt"))) {
            props.load(inStream);
        }
        Path fileFromFiltering = Path.of(props.getProperty("current-working-directory"));
        assertTrue(Files.exists(fileFromFiltering), fileFromFiltering.toAbsolutePath() + " does not exist.");
        assertEquals(userDir.toAbsolutePath(), fileFromFiltering.toAbsolutePath());
    }

    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testResourceProjectPropertiesFiltering(ResourcesMojo mojo) throws Exception {
        assertNotNull(mojo);
        final var project = (MavenProjectSourcesStub) mojo.project;
        project.addFile(Path.of("file4.txt"), "current working directory = ${user.dir}");
        project.setResourceFiltering(true);
        project.addProperty("user.dir", "FPJ kami!!!");
        project.setupBuildEnvironment();

        // setVariableValueToObject(mojo,"encoding","UTF-8");
        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "outputDirectory", project.getOutputDirectory());
        setVariableValueToObject(mojo, "buildFilters", Collections.emptyList());
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        mojo.execute();

        Path outputDirectory = project.getOutputDirectory();
        String checkString = "current working directory = FPJ kami!!!";
        assertContent(outputDirectory.resolve("file4.txt"), checkString);
    }

    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testProjectPropertyFilteringPropertyDestination(ResourcesMojo mojo) throws Exception {
        assertNotNull(mojo);
        final var project = (MavenProjectSourcesStub) mojo.project;
        project.addFile(Path.of("file4.properties"), "current working directory=${description}");
        project.setResourceFiltering(true);
        project.setupBuildEnvironment();

        // setup dummy property
        project.setDescription("c:\\\\org\\apache\\test");

        // setVariableValueToObject(mojo,"encoding","UTF-8");
        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "outputDirectory", project.getOutputDirectory());
        setVariableValueToObject(mojo, "buildFilters", Collections.emptyList());
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        setVariableValueToObject(mojo, "escapeWindowsPaths", Boolean.TRUE);
        mojo.execute();

        Path outputDirectory = project.getOutputDirectory();
        String checkString = "current working directory=c:\\\\org\\\\apache\\\\test";
        assertContent(outputDirectory.resolve("file4.properties"), checkString);
    }

    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testPropertyFilesFiltering(ResourcesMojo mojo) throws Exception {
        assertNotNull(mojo);
        final var project = (MavenProjectSourcesStub) mojo.project;
        final var filterList = new ArrayList<String>();

        project.addFile(Path.of("file4.properties"), "current working directory=${dir}");
        project.addFile(Path.of("filter.properties"), "dir:testdir");
        project.setResourceFiltering(true);
        project.setupBuildEnvironment();
        filterList.add(
                project.getResourcesDirectory().resolve("filter.properties").toString());

        // setVariableValueToObject(mojo,"encoding","UTF-8");
        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "outputDirectory", project.getOutputDirectory());
        setVariableValueToObject(mojo, "buildFilters", filterList);
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        mojo.execute();

        Path outputDirectory = project.getOutputDirectory();
        String checkString = "current working directory=testdir";
        assertContent(outputDirectory.resolve("file4.properties"), checkString);
    }

    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testPropertyFilesExtra(ResourcesMojo mojo) throws Exception {
        assertNotNull(mojo);
        final var project = (MavenProjectSourcesStub) mojo.project;
        final var filterList = new ArrayList<String>();

        project.addFile(Path.of("extra.properties"), "current working directory=${dir}");
        project.addFile(Path.of("filter.properties"), "dir:testdir");
        project.setResourceFiltering(true);
        project.setupBuildEnvironment();
        filterList.add(
                project.getResourcesDirectory().resolve("filter.properties").toString());

        // setVariableValueToObject(mojo,"encoding","UTF-8");
        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "outputDirectory", project.getOutputDirectory());
        setVariableValueToObject(mojo, "filters", filterList);
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        mojo.execute();

        Path outputDirectory = project.getOutputDirectory();
        String checkString = "current working directory=testdir";
        assertContent(outputDirectory.resolve("extra.properties"), checkString);
    }

    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testPropertyFilesMainAndExtra(ResourcesMojo mojo) throws Exception {
        assertNotNull(mojo);
        final var project = (MavenProjectSourcesStub) mojo.project;
        final var filterList = new ArrayList<String>();
        final var extraFilterList = new ArrayList<String>();

        project.addFile(
                Path.of("main-extra.properties"), "current working directory=${dir}; old working directory=${dir2}");
        project.addFile(Path.of("filter.properties"), "dir:testdir");
        project.addFile(Path.of("extra-filter.properties"), "dir2:testdir2");
        project.setResourceFiltering(true);

        project.cleanBuildEnvironment();
        project.setupBuildEnvironment();

        filterList.add(
                project.getResourcesDirectory().resolve("filter.properties").toString());
        extraFilterList.add(project.getResourcesDirectory()
                .resolve("extra-filter.properties")
                .toString());

        // setVariableValueToObject(mojo,"encoding","UTF-8");
        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "outputDirectory", project.getOutputDirectory());
        setVariableValueToObject(mojo, "buildFilters", filterList);
        setVariableValueToObject(mojo, "filters", extraFilterList);
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        mojo.execute();

        Path outputDirectory = project.getOutputDirectory();
        String checkString = "current working directory=testdir; old working directory=testdir2";
        Path file = outputDirectory.resolve("main-extra.properties");
        assertContent(file, checkString);
    }

    /**
     * Validates that a Filter token containing a project property will be resolved before the Filter is applied to the
     * resources.
     */
    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testPropertyFilesFilteringTokensInFilters(ResourcesMojo mojo) throws Exception {
        assertNotNull(mojo);
        final var project = (MavenProjectSourcesStub) mojo.project;
        final var filterList = new ArrayList<String>();

        project.addFile(Path.of("file4.properties"), "current working directory=${filter.token}");
        project.addFile(Path.of("filter.properties"), "filter.token=${pom-property}");
        project.setResourceFiltering(true);
        project.addProperty("pom-property", "foobar");
        project.setupBuildEnvironment();
        filterList.add(
                project.getResourcesDirectory().resolve("filter.properties").toString());

        // setVariableValueToObject(mojo,"encoding","UTF-8");
        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "outputDirectory", project.getOutputDirectory());
        setVariableValueToObject(mojo, "buildFilters", filterList);
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        mojo.execute();

        final Path outputDirectory = project.getOutputDirectory();
        final String checkString = "current working directory=foobar";
        assertContent(outputDirectory.resolve("file4.properties"), checkString);
    }

    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testWindowsPathEscapingDisabled(ResourcesMojo mojo) throws Exception {
        assertNotNull(mojo);
        final var project = (MavenProjectSourcesStub) mojo.project;
        project.addProperty("basePath", "C:\\Users\\Administrator");
        project.addProperty("docsPath", "${basePath}\\Documents");
        project.addFile(Path.of("path-listing.txt"), "base path is ${basePath}\ndocuments path is ${docsPath}");
        project.setResourceFiltering(true);
        project.cleanBuildEnvironment();
        project.setupBuildEnvironment();

        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "outputDirectory", project.getOutputDirectory());
        setVariableValueToObject(mojo, "buildFilters", Collections.emptyList());
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        setVariableValueToObject(mojo, "escapeWindowsPaths", Boolean.FALSE);

        mojo.execute();
        Path outputDirectory = project.getOutputDirectory();
        assertTrue(Files.exists(outputDirectory.resolve("path-listing.txt").toAbsolutePath()));
        assertEquals(
                "base path is C:\\Users\\Administrator\ndocuments path is C:\\Users\\Administrator\\Documents",
                new String(Files.readAllBytes(outputDirectory.resolve("path-listing.txt"))));
    }

    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testWindowsPathEscapingEnabled(ResourcesMojo mojo) throws Exception {
        assertNotNull(mojo);
        final var project = (MavenProjectSourcesStub) mojo.project;
        project.addProperty("basePath", "C:\\Users\\Administrator");
        project.addProperty("docsPath", "${basePath}\\Documents");
        project.addFile(Path.of("path-listing.txt"), "base path is ${basePath}\ndocuments path is ${docsPath}");
        project.setResourceFiltering(true);
        project.cleanBuildEnvironment();
        project.setupBuildEnvironment();

        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "outputDirectory", project.getOutputDirectory());
        setVariableValueToObject(mojo, "buildFilters", Collections.emptyList());
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        setVariableValueToObject(mojo, "escapeWindowsPaths", Boolean.TRUE);
        mojo.execute();

        Path outputDirectory = project.getOutputDirectory();
        assertTrue(Files.exists(outputDirectory.resolve("path-listing.txt").toAbsolutePath()));
        assertEquals(
                "base path is C:\\\\Users\\\\Administrator\ndocuments path is C:\\\\Users\\\\Administrator\\\\Documents",
                new String(Files.readAllBytes(outputDirectory.resolve("path-listing.txt"))));
    }

    /**
     * Ensures the file exists and its first line equals the given data.
     */
    private void assertContent(Path fileName, String data) throws IOException {
        assertTrue(Files.exists(fileName));
        try (BufferedReader reader = Files.newBufferedReader(fileName)) {
            assertEquals(data, reader.readLine());
        }
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
    private static Project createProject() throws Exception {
        return new MavenProjectSourcesStub();
    }

    private static List<Resource> getResources(MavenProjectSourcesStub project) {
        return project.getResources("main");
    }
}
