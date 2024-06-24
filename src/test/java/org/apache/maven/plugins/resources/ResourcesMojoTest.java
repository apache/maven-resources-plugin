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
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.maven.api.Project;
import org.apache.maven.api.di.Provides;
import org.apache.maven.api.di.Singleton;
import org.apache.maven.api.plugin.testing.Basedir;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.api.plugin.testing.stubs.SessionMock;
import org.apache.maven.internal.impl.InternalSession;
import org.apache.maven.plugins.resources.stub.MavenProjectResourcesStub;
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
     * test mojo lookup, test harness should be working fine
     */
    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testHarnessEnvironment(ResourcesMojo mojo) {
        assertNotNull(mojo);
    }

    /**
     */
    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testResourceDirectoryStructure(ResourcesMojo mojo) throws Exception {
        assertNotNull(mojo);

        MavenProjectResourcesStub project = (MavenProjectResourcesStub) mojo.project;

        project.addFile("file4.txt");
        project.addFile("package/file3.nottest");
        project.addFile("notpackage/file1.include");
        project.addFile("package/test/file1.txt");
        project.addFile("notpackage/test/file2.txt");
        project.setupBuildEnvironment();

        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "outputDirectory", Paths.get(project.getOutputDirectory()));
        setVariableValueToObject(mojo, "buildFilters", Collections.emptyList());
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        mojo.execute();

        String resourcesDir = project.getOutputDirectory();

        assertTrue(Files.exists(Paths.get(resourcesDir + "/file4.txt")));
        assertTrue(Files.exists(Paths.get(resourcesDir + "/package/file3.nottest")));
        assertTrue(Files.exists(Paths.get(resourcesDir + "/notpackage/file1.include")));
        assertTrue(Files.exists(Paths.get(resourcesDir + "/package/test")));
        assertTrue(Files.exists(Paths.get(resourcesDir + "/notpackage/test")));
    }

    /**
     */
    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testResourceDirectoryStructure_RelativePath(ResourcesMojo mojo) throws Exception {
        assertNotNull(mojo);

        MavenProjectResourcesStub project = (MavenProjectResourcesStub) mojo.project;

        project.setOutputDirectory("../relative_dir");
        project.addFile("file4.txt");
        project.addFile("package/file3.nottest");
        project.addFile("notpackage/file1.include");
        project.addFile("package/test/file1.txt");
        project.addFile("notpackage/test/file2.txt");
        project.setupBuildEnvironment();

        setVariableValueToObject(mojo, "outputDirectory", Paths.get(project.getOutputDirectory()));
        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "buildFilters", Collections.emptyList());
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        mojo.execute();

        String resourcesDir = project.getOutputDirectory();

        assertTrue(Files.exists(Paths.get(resourcesDir + "/file4.txt")));
        assertTrue(Files.exists(Paths.get(resourcesDir + "/package/file3.nottest")));
        assertTrue(Files.exists(Paths.get(resourcesDir + "/notpackage/file1.include")));
        assertTrue(Files.exists(Paths.get(resourcesDir + "/package/test")));
        assertTrue(Files.exists(Paths.get(resourcesDir + "/notpackage/test")));
    }

    /**
     */
    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testResourceEncoding(ResourcesMojo mojo) throws Exception {
        assertNotNull(mojo);

        MavenProjectResourcesStub project = (MavenProjectResourcesStub) mojo.project;

        project.addFile("file4.txt");
        project.setResourceFiltering(true);
        project.setupBuildEnvironment();

        setVariableValueToObject(mojo, "encoding", "UTF-8");
        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "outputDirectory", Paths.get(project.getOutputDirectory()));
        setVariableValueToObject(mojo, "buildFilters", Collections.emptyList());
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        mojo.execute();

        String resourcesDir = project.getOutputDirectory();

        assertTrue(Files.exists(Paths.get(resourcesDir + "/file4.txt")));
    }

    /**
     */
    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testResourceInclude(ResourcesMojo mojo) throws Exception {
        assertNotNull(mojo);

        MavenProjectResourcesStub project = (MavenProjectResourcesStub) mojo.project;

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

        setVariableValueToObject(mojo, "outputDirectory", Paths.get(project.getOutputDirectory()));
        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "buildFilters", Collections.emptyList());
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        mojo.execute();

        String resourcesDir = project.getOutputDirectory();

        assertTrue(Files.exists(Paths.get(resourcesDir + "/package/test")));
        assertTrue(Files.exists(Paths.get(resourcesDir + "/file1.include")));
        assertTrue(Files.exists(Paths.get(resourcesDir + "/package/file1.include")));
        assertFalse(Files.exists(Paths.get(resourcesDir + "/notpackage/file1.include")));
        assertFalse(Files.exists(Paths.get(resourcesDir + "/notpackage/nottest/file.txt")));
    }

    /**
     */
    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testResourceExclude(ResourcesMojo mojo) throws Exception {
        assertNotNull(mojo);

        MavenProjectResourcesStub project = (MavenProjectResourcesStub) mojo.project;

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

        setVariableValueToObject(mojo, "outputDirectory", Paths.get(project.getOutputDirectory()));
        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "buildFilters", Collections.emptyList());
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        mojo.execute();

        String resourcesDir = project.getOutputDirectory();

        assertTrue(Files.exists(Paths.get(resourcesDir + "/package/test")));
        assertTrue(Files.exists(Paths.get(resourcesDir + "/file1.include")));
        assertTrue(Files.exists(Paths.get(resourcesDir + "/package/file1.include")));
        assertFalse(Files.exists(Paths.get(resourcesDir + "/notpackage/file1.include")));
        assertFalse(Files.exists(Paths.get(resourcesDir + "/notpackage/nottest/file.txt")));
    }

    /**
     */
    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testResourceTargetPath(ResourcesMojo mojo) throws Exception {
        assertNotNull(mojo);

        MavenProjectResourcesStub project = (MavenProjectResourcesStub) mojo.project;

        project.setTargetPath("org/apache/maven/plugin/test");

        project.addFile("file4.txt");
        project.addFile("package/file3.nottest");
        project.addFile("notpackage/file1.include");
        project.addFile("package/test/file1.txt");
        project.addFile("notpackage/test/file2.txt");
        project.setupBuildEnvironment();

        setVariableValueToObject(mojo, "outputDirectory", Paths.get(project.getOutputDirectory()));
        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "buildFilters", Collections.emptyList());
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        mojo.execute();

        String resourcesDir = project.getOutputDirectory();

        assertTrue(Files.exists(Paths.get(resourcesDir + "/org/apache/maven/plugin/test/file4.txt")));
        assertTrue(Files.exists(Paths.get(resourcesDir + "/org/apache/maven/plugin/test/package/file3.nottest")));
        assertTrue(Files.exists(Paths.get(resourcesDir + "/org/apache/maven/plugin/test/notpackage/file1.include")));
        assertTrue(Files.exists(Paths.get(resourcesDir + "/org/apache/maven/plugin/test/package/test")));
        assertTrue(Files.exists(Paths.get(resourcesDir + "/org/apache/maven/plugin/test/notpackage/test")));
    }

    /**
     */
    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testResourceSystemProperties_Filtering(ResourcesMojo mojo) throws Exception {
        assertNotNull(mojo);

        MavenProjectResourcesStub project = (MavenProjectResourcesStub) mojo.project;

        project.addFile("file4.txt", "current-working-directory = ${user.dir}");
        project.setResourceFiltering(true);
        project.setupBuildEnvironment();

        // setVariableValueToObject(mojo,"encoding","UTF-8");
        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "outputDirectory", Paths.get(project.getOutputDirectory()));
        setVariableValueToObject(mojo, "buildFilters", Collections.emptyList());
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        setVariableValueToObject(mojo, "escapeWindowsPaths", Boolean.TRUE);

        mojo.session.getSystemProperties().put("user.dir", System.getProperty("user.dir"));

        mojo.execute();

        String resourcesDir = project.getOutputDirectory();

        Path userDir = Paths.get(System.getProperty("user.dir"));
        assertTrue(Files.exists(userDir));

        Properties props = new Properties();
        try (InputStream inStream = Files.newInputStream(Paths.get(resourcesDir, "file4.txt"))) {
            props.load(inStream);
        }
        Path fileFromFiltering = Paths.get(props.getProperty("current-working-directory"));

        assertTrue(Files.exists(fileFromFiltering), fileFromFiltering.toAbsolutePath() + " does not exist.");
        assertEquals(userDir.toAbsolutePath(), fileFromFiltering.toAbsolutePath());
    }

    /**
     */
    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testResourceProjectProperties_Filtering(ResourcesMojo mojo) throws Exception {
        assertNotNull(mojo);

        MavenProjectResourcesStub project = (MavenProjectResourcesStub) mojo.project;

        project.addFile("file4.txt", "current working directory = ${user.dir}");
        project.setResourceFiltering(true);
        project.addProperty("user.dir", "FPJ kami!!!");
        project.setupBuildEnvironment();

        // setVariableValueToObject(mojo,"encoding","UTF-8");
        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "outputDirectory", Paths.get(project.getOutputDirectory()));
        setVariableValueToObject(mojo, "buildFilters", Collections.emptyList());
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        mojo.execute();

        String resourcesDir = project.getOutputDirectory();
        String checkString = "current working directory = FPJ kami!!!";

        assertContent(resourcesDir + "/file4.txt", checkString);
    }

    /**
     */
    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testProjectProperty_Filtering_PropertyDestination(ResourcesMojo mojo) throws Exception {
        assertNotNull(mojo);

        MavenProjectResourcesStub project = (MavenProjectResourcesStub) mojo.project;

        project.addFile("file4.properties", "current working directory=${description}");
        project.setResourceFiltering(true);
        project.setupBuildEnvironment();

        // setup dummy property
        project.setDescription("c:\\\\org\\apache\\test");

        // setVariableValueToObject(mojo,"encoding","UTF-8");
        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "outputDirectory", Paths.get(project.getOutputDirectory()));
        setVariableValueToObject(mojo, "buildFilters", Collections.emptyList());
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        setVariableValueToObject(mojo, "escapeWindowsPaths", Boolean.TRUE);
        mojo.execute();

        String resourcesDir = project.getOutputDirectory();
        String checkString = "current working directory=c:\\\\org\\\\apache\\\\test";

        assertContent(resourcesDir + "/file4.properties", checkString);
    }

    /**
     */
    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testPropertyFiles_Filtering(ResourcesMojo mojo) throws Exception {
        assertNotNull(mojo);

        MavenProjectResourcesStub project = (MavenProjectResourcesStub) mojo.project;

        LinkedList<String> filterList = new LinkedList<>();

        project.addFile("file4.properties", "current working directory=${dir}");
        project.addFile("filter.properties", "dir:testdir");
        project.setResourceFiltering(true);
        project.setupBuildEnvironment();
        filterList.add(project.getResourcesDirectory() + "filter.properties");

        // setVariableValueToObject(mojo,"encoding","UTF-8");
        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "outputDirectory", Paths.get(project.getOutputDirectory()));
        setVariableValueToObject(mojo, "buildFilters", filterList);
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        mojo.execute();

        String resourcesDir = project.getOutputDirectory();
        String checkString = "current working directory=testdir";

        assertContent(resourcesDir + "/file4.properties", checkString);
    }

    /**
     */
    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testPropertyFiles_Extra(ResourcesMojo mojo) throws Exception {
        assertNotNull(mojo);

        MavenProjectResourcesStub project = (MavenProjectResourcesStub) mojo.project;

        LinkedList<String> filterList = new LinkedList<>();

        project.addFile("extra.properties", "current working directory=${dir}");
        project.addFile("filter.properties", "dir:testdir");
        project.setResourceFiltering(true);
        project.setupBuildEnvironment();
        filterList.add(project.getResourcesDirectory() + "filter.properties");

        // setVariableValueToObject(mojo,"encoding","UTF-8");
        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "outputDirectory", Paths.get(project.getOutputDirectory()));
        setVariableValueToObject(mojo, "filters", filterList);
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        mojo.execute();

        String resourcesDir = project.getOutputDirectory();
        String checkString = "current working directory=testdir";

        assertContent(resourcesDir + "/extra.properties", checkString);
    }

    /**
     */
    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testPropertyFiles_MainAndExtra(ResourcesMojo mojo) throws Exception {
        assertNotNull(mojo);

        MavenProjectResourcesStub project = (MavenProjectResourcesStub) mojo.project;

        LinkedList<String> filterList = new LinkedList<>();
        LinkedList<String> extraFilterList = new LinkedList<>();

        project.addFile("main-extra.properties", "current working directory=${dir}; old working directory=${dir2}");
        project.addFile("filter.properties", "dir:testdir");
        project.addFile("extra-filter.properties", "dir2:testdir2");
        project.setResourceFiltering(true);

        project.cleanBuildEnvironment();
        project.setupBuildEnvironment();

        filterList.add(project.getResourcesDirectory() + "filter.properties");
        extraFilterList.add(project.getResourcesDirectory() + "extra-filter.properties");

        // setVariableValueToObject(mojo,"encoding","UTF-8");
        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "outputDirectory", Paths.get(project.getOutputDirectory()));
        setVariableValueToObject(mojo, "buildFilters", filterList);
        setVariableValueToObject(mojo, "filters", extraFilterList);
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        mojo.execute();

        String resourcesDir = project.getOutputDirectory();
        String checkString = "current working directory=testdir; old working directory=testdir2";

        Path file = Paths.get(resourcesDir, "main-extra.properties");
        assertContent(file.toAbsolutePath().toString(), checkString);
    }

    /**
     * Validates that a Filter token containing a project property will be resolved before the Filter is applied to the
     * resources.
     *
     */
    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testPropertyFiles_Filtering_TokensInFilters(ResourcesMojo mojo) throws Exception {
        assertNotNull(mojo);

        MavenProjectResourcesStub project = (MavenProjectResourcesStub) mojo.project;

        LinkedList<String> filterList = new LinkedList<>();

        project.addFile("file4.properties", "current working directory=${filter.token}");
        project.addFile("filter.properties", "filter.token=${pom-property}");
        project.setResourceFiltering(true);
        project.addProperty("pom-property", "foobar");
        project.setupBuildEnvironment();
        filterList.add(project.getResourcesDirectory() + "filter.properties");

        // setVariableValueToObject(mojo,"encoding","UTF-8");
        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "outputDirectory", Paths.get(project.getOutputDirectory()));
        setVariableValueToObject(mojo, "buildFilters", filterList);
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        mojo.execute();
        final String resourcesDir = project.getOutputDirectory();

        final String checkString = "current working directory=foobar";

        assertContent(resourcesDir + "/file4.properties", checkString);
    }

    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testWindowsPathEscapingDisabled(ResourcesMojo mojo) throws Exception {
        assertNotNull(mojo);

        MavenProjectResourcesStub project = (MavenProjectResourcesStub) mojo.project;

        project.addProperty("basePath", "C:\\Users\\Administrator");
        project.addProperty("docsPath", "${basePath}\\Documents");

        project.addFile("path-listing.txt", "base path is ${basePath}\ndocuments path is ${docsPath}");
        project.setResourceFiltering(true);

        project.cleanBuildEnvironment();
        project.setupBuildEnvironment();

        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "outputDirectory", Paths.get(project.getOutputDirectory()));
        setVariableValueToObject(mojo, "buildFilters", Collections.emptyList());
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);
        setVariableValueToObject(mojo, "escapeWindowsPaths", Boolean.FALSE);

        mojo.execute();

        String resourcesDir = project.getOutputDirectory();

        assertTrue(Files.exists(Paths.get(resourcesDir, "path-listing.txt").toAbsolutePath()));

        assertEquals(
                "base path is C:\\Users\\Administrator\ndocuments path is C:\\Users\\Administrator\\Documents",
                new String(Files.readAllBytes(Paths.get(resourcesDir, "path-listing.txt"))));
    }

    @Test
    @InjectMojo(goal = "resources", pom = CONFIG_XML)
    @Basedir
    public void testWindowsPathEscapingEnabled(ResourcesMojo mojo) throws Exception {
        assertNotNull(mojo);

        MavenProjectResourcesStub project = (MavenProjectResourcesStub) mojo.project;

        project.addProperty("basePath", "C:\\Users\\Administrator");
        project.addProperty("docsPath", "${basePath}\\Documents");

        project.addFile("path-listing.txt", "base path is ${basePath}\ndocuments path is ${docsPath}");
        project.setResourceFiltering(true);

        project.cleanBuildEnvironment();
        project.setupBuildEnvironment();

        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "resources", getResources(project));
        setVariableValueToObject(mojo, "outputDirectory", Paths.get(project.getOutputDirectory()));
        setVariableValueToObject(mojo, "buildFilters", Collections.emptyList());
        setVariableValueToObject(mojo, "useBuildFilters", Boolean.TRUE);

        setVariableValueToObject(mojo, "escapeWindowsPaths", Boolean.TRUE);

        mojo.execute();

        String resourcesDir = project.getOutputDirectory();

        assertTrue(Files.exists(Paths.get(resourcesDir, "path-listing.txt").toAbsolutePath()));

        assertEquals(
                "base path is C:\\\\Users\\\\Administrator\ndocuments path is C:\\\\Users\\\\Administrator\\\\Documents",
                new String(Files.readAllBytes(Paths.get(resourcesDir, "path-listing.txt"))));
    }

    /**
     * Ensures the file exists and its first line equals the given data.
     */
    private void assertContent(String fileName, String data) throws IOException {
        assertTrue(Files.exists(Paths.get(fileName)));
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(fileName))) {
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
        return new MavenProjectResourcesStub();
    }

    private List<Resource> getResources(MavenProjectResourcesStub project) {
        return project.getBuild().getResources().stream()
                .map(ResourceUtils::newResource)
                .collect(Collectors.toList());
    }
}
