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
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.maven.api.di.Provides;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoParameter;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Developer;
import org.apache.maven.model.Organization;
import org.apache.maven.model.Resource;
import org.apache.maven.plugins.resources.stub.MavenProjectResourcesStub;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.apache.maven.api.plugin.testing.MojoExtension.setVariableValueToObject;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Filtering behaviour of the {@code resources} goal against fixture directories in
 * {@code src/test/resources/unit}. The tests were integration tests under {@code src/it} before.
 */
@MojoTest
class ResourcesMojoFilteringTest {

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

    private MavenProjectResourcesStub project() {
        return (MavenProjectResourcesStub) projectStub;
    }

    /** Points the stub's single resource entry at a fixture directory. */
    private Resource resource(String fixture, boolean filtering) throws Exception {
        Resource resource = project().getBuild().getResources().get(0);
        resource.setDirectory(fixture(fixture).toString());
        resource.setFiltering(filtering);
        project().setupBuildEnvironment();
        return resource;
    }

    static Path fixture(String name) throws URISyntaxException {
        return Paths.get(
                ResourcesMojoFilteringTest.class.getResource("/unit/" + name).toURI());
    }

    private String output(String name) throws IOException {
        return read(new File(project().getOutputDirectory(), name).toPath());
    }

    static String read(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    /** The project, profile and user properties the filter-matrix fixtures refer to. */
    private void matrixProject() {
        project().setVersion("1.0-SNAPSHOT");
        project().addProperty("projectProperty", "foo-projectProperty-bar");
        // a profile is only a property source for the filter
        project().addProperty("profileProperty", "foo-profileProperty-bar");
        mavenSession.getUserProperties().setProperty("execProps", "fromExecProps");
    }

    static List<String> filters(String... names) throws Exception {
        List<String> result = new ArrayList<>();
        for (String name : names) {
            result.add(fixture("filter-matrix/filters/" + name).toString());
        }
        return result;
    }

    static void assertContains(String content, String expected) {
        assertTrue(content.contains(expected), () -> "Expected '" + expected + "' in:\n" + content);
    }

    /** The assertions every fixture in {@code filter-matrix} shares, except the lines that need the user filter. */
    static void assertMatrixLines(String content) {
        assertContains(content, "test:direct resolution project.version=1.0-SNAPSHOT");
        assertContains(content, "test:filter resolution project.version=1.0-SNAPSHOT");
        assertContains(content, "test:filter direct projectProperty=foo-projectProperty-bar");
        assertContains(content, "test:filter resolution projectProperty=foo-projectProperty-bar");
        assertContains(content, "test:filter direct profileProperty=foo-profileProperty-bar");
        assertContains(content, "test:filter resolution profileProperty=foo-profileProperty-bar");
    }

    /** The two lines of the matrix that exercise the main filter file's own tokens and the user property. */
    static void assertMatrixUserPropertyLines(String content) {
        // a user property wins over the same key in a filter file (execProps=fromFilter)
        assertContains(content, "test:filter syspropsExecutionPropsWins=fromExecProps");
        assertContains(content, "newToken=foo");
    }

    @Test
    @InjectMojo(goal = "resources")
    @MojoParameter(name = "encoding", value = "UTF-8")
    void filterFilesChainAndUserPropertyWins(ResourcesMojo mojo) throws Exception {
        matrixProject();
        resource("filter-matrix/resources/filter", true);
        setVariableValueToObject(
                mojo, "buildFilters", filters("my.filter", "filter2.properties", "filter3.properties"));

        mojo.execute();

        String content = output("SomeResource.txt");
        assertMatrixLines(content);
        assertMatrixUserPropertyLines(content);
        // var3 -> ${var2}/Thing -> ${var1}-Bar/Thing -> Foo-Bar/Thing across three filter files
        assertContains(content, "var3=Foo-Bar/Thing");
    }

    @Test
    @InjectMojo(goal = "resources")
    @MojoParameter(name = "encoding", value = "UTF-8")
    void customDelimiterIsAddedToTheDefaults(ResourcesMojo mojo) throws Exception {
        matrixProject();
        resource("filter-matrix/resources/custom-delimiter", true);
        setVariableValueToObject(
                mojo, "buildFilters", filters("my.filter", "filter2.properties", "filter3.properties"));
        mojo.setDelimiters(new LinkedHashSet<>(Collections.singletonList(">*<")));

        mojo.execute();

        String content = output("SomeResource.txt");
        assertMatrixLines(content);
        assertMatrixUserPropertyLines(content);
        // >var3< is filtered with the custom delimiter
        assertContains(content, "var3=Foo-Bar/Thing");
    }

    @Test
    @InjectMojo(goal = "resources")
    @MojoParameter(name = "encoding", value = "UTF-8")
    @MojoParameter(name = "escapeString", value = "\\")
    void escapedExpressionIsNotInterpolated(ResourcesMojo mojo) throws Exception {
        matrixProject();
        project().setArtifactId("maven-resources-plugin-it-escapeInterpolation");
        resource("filter-matrix/resources/escape-main", true);
        setVariableValueToObject(mojo, "buildFilters", filters("my.filter"));

        mojo.execute();

        String content = output("SomeResource.txt");
        assertMatrixLines(content);
        assertMatrixUserPropertyLines(content);
        // \${project.artifactId} loses the escape and keeps the expression
        assertContains(content, "escapeString=${project.artifactId}");
    }

    /**
     * Git does not store empty directories, so the source tree is built here: the helper files named
     * {@code ._placeholder} that keep a directory alive in a checkout are default excludes ({@code **}{@code /._*}) and
     * must not reach the output.
     */
    @Test
    @InjectMojo(goal = "resources")
    @MojoParameter(name = "encoding", value = "UTF-8")
    @MojoParameter(name = "includeEmptyDirs", value = "true")
    void includeEmptyDirsKeepsDirectoriesButNotDefaultExcludedFiles(ResourcesMojo mojo) throws Exception {
        matrixProject();
        project().setupBuildEnvironment();
        project().getBuild().getResources().get(0).setFiltering(true);
        File resources = new File(project().getResourcesDirectory());
        Files.copy(fixture("include-empty-dirs/SomeResource.txt"), new File(resources, "SomeResource.txt").toPath());
        for (String dir : Arrays.asList("empty-dir", "empty-dir-child/child")) {
            File emptyDir = new File(resources, dir);
            assertTrue(emptyDir.mkdirs(), dir);
            Files.write(new File(emptyDir, "._placeholder").toPath(), new byte[0]);
        }
        setVariableValueToObject(mojo, "buildFilters", filters("my.filter"));

        mojo.execute();

        File classes = new File(project().getOutputDirectory());
        File emptyDir = new File(classes, "empty-dir");
        assertTrue(emptyDir.isDirectory(), "empty-dir");
        // the ._placeholder file that keeps the directory in git is a default exclude
        assertArrayEquals(new String[0], emptyDir.list());
        File emptyDirChild = new File(classes, "empty-dir-child");
        assertTrue(emptyDirChild.isDirectory(), "empty-dir-child");
        assertArrayEquals(new String[] {"child"}, emptyDirChild.list());
        File child = new File(emptyDirChild, "child");
        assertTrue(child.isDirectory(), "empty-dir-child/child");
        assertArrayEquals(new String[0], child.list());

        String content = output("SomeResource.txt");
        assertMatrixLines(content);
        assertMatrixUserPropertyLines(content);
    }

    @Test
    @InjectMojo(goal = "resources")
    @MojoParameter(name = "encoding", value = "UTF-8")
    void defaultDelimitersLeaveEmailAddressesAlone(ResourcesMojo mojo) throws Exception {
        project().setVersion("0.0.1-SNAPSHOT");
        resource("MRESOURCES-104", true);

        mojo.execute();

        String content = output("test.properties");
        assertContains(content, "email=foo@bar.com");
        assertContains(content, "foo=0.0.1-SNAPSHOT");
        assertContains(content, "bar=0.0.1-SNAPSHOT");
    }

    @Test
    @InjectMojo(goal = "resources")
    @MojoParameter(name = "encoding", value = "UTF-8")
    @MojoParameter(name = "escapeString", value = "!")
    void exclamationMarkAsEscapeString(ResourcesMojo mojo) throws Exception {
        project().addProperty("replaceThis", "I am the replacement");
        resource("MRESOURCES-110", true);

        mojo.execute();

        String content = output("mresources-110.xml");
        assertContains(content, "<broken-tag>Content with replacement: I am the replacement !</broken-tag>");
        assertContains(content, "<broken-tag>Content with escaped replacement: Do not ${replaceThis} !</broken-tag>");
    }

    @Test
    @InjectMojo(goal = "resources")
    @MojoParameter(name = "encoding", value = "UTF-8")
    void unresolvedExpressionIsKept(ResourcesMojo mojo) throws Exception {
        resource("MRESOURCES-20", true);

        mojo.execute();

        assertContains(output("resource.txt"), "fileValue=${foo.file}");
    }

    @Test
    @InjectMojo(goal = "resources")
    @MojoParameter(name = "encoding", value = "UTF-8")
    @MojoParameter(name = "supportMultiLineFiltering", value = "true")
    @MojoParameter(name = "escapeString", value = "\\")
    void multiLineFilteringWithBackslashEscape(ResourcesMojo mojo) throws Exception {
        project().addProperty("httpd.server.env", "sandbox");
        resource("MRESOURCES-166", true);

        mojo.execute();

        String content = output("resource.txt");
        assertContains(content, "fileValue=${foo.file}");
        assertContains(content, "export SERVER_ENV=${SERVER_ENV:sandbox}");
    }

    @Test
    @InjectMojo(goal = "resources")
    @MojoParameter(name = "encoding", value = "UTF-8")
    @MojoParameter(name = "propertiesEncoding", value = "ISO-8859-1")
    void propertiesEncodingDoesNotTouchOtherFiles(ResourcesMojo mojo) throws Exception {
        resource("MRESOURCES-171", true);

        mojo.execute();

        File classes = new File(project().getOutputDirectory());
        // the properties file is ISO-8859-1: read as UTF-8 it would be mangled
        assertArrayEquals(
                Files.readAllBytes(fixture("MRESOURCES-171/test.properties")),
                Files.readAllBytes(new File(classes, "test.properties").toPath()));
        // test.txt is UTF-8 and not a properties file, so it must come out byte for byte
        assertArrayEquals(
                Files.readAllBytes(fixture("MRESOURCES-171/test.txt")),
                Files.readAllBytes(new File(classes, "test.txt").toPath()));
    }

    @Test
    @InjectMojo(goal = "resources")
    @MojoParameter(name = "encoding", value = "UTF-8")
    void modelExpressionsOfDevelopersAndOrganization(ResourcesMojo mojo) throws Exception {
        Developer developer = new Developer();
        developer.setId("jdoe");
        developer.setName("John Doe");
        developer.setEmail("jdoe@example.com");
        project().getModel().addDeveloper(developer);
        Organization organization = new Organization();
        organization.setName("orga");
        project().getModel().setOrganization(organization);
        resource("MRESOURCES-257", true);

        mojo.execute();

        String content = output("test.txt");
        assertContains(content, "does not work: jdoe@example.com");
        assertContains(content, "works: orga");
    }

    @Test
    @InjectMojo(goal = "resources")
    @MojoParameter(name = "encoding", value = "UTF-8")
    @MojoParameter(name = "escapeString", value = "\\")
    void escapeStringOnlyAffectsFilteredResources(ResourcesMojo mojo) throws Exception {
        project().addProperty("escaped", "this is escaped");
        project().addProperty("escaped.at", "this is escaped.at");
        project().addProperty("foo", "this is foo");
        project().addProperty("bar", "this is bar");
        Resource unfiltered = resource("MRESOURCES-78/unfiltered", false);
        Resource filtered = new Resource();
        filtered.setDirectory(fixture("MRESOURCES-78/filtered").toString());
        filtered.setFiltering(true);
        project().getBuild().addResource(filtered);
        assertEquals(2, project().getBuild().getResources().size());
        assertEquals(fixture("MRESOURCES-78/unfiltered").toString(), unfiltered.getDirectory());

        mojo.execute();

        String content = output("filtered.properties");
        assertContains(content, "bar=this is bar");
        assertContains(content, "escaped=${escaped}");
        assertContains(content, "escaped.at=@escaped.at@");
        assertEquals(read(fixture("MRESOURCES-78/unfiltered/unfiltered.properties")), output("unfiltered.properties"));
    }

    @Test
    @InjectMojo(goal = "resources")
    @MojoParameter(name = "encoding", value = "UTF-8")
    void nonFilteredFileExtensionsKeepBinariesIntact(ResourcesMojo mojo) throws Exception {
        project().setVersion("1.0-SNAPSHOT");
        resource("binaries-filtering", true);
        setVariableValueToObject(mojo, "nonFilteredFileExtensions", Collections.singletonList("img"));

        mojo.execute();

        File classes = new File(project().getOutputDirectory());
        // .gif is non-filtered by default, .img through the configuration
        for (String binary : Arrays.asList("happy_duke.gif", "duke-beerjpg.img")) {
            assertArrayEquals(
                    Files.readAllBytes(fixture("binaries-filtering/" + binary)),
                    Files.readAllBytes(new File(classes, binary).toPath()),
                    binary);
        }
        // the text file next to them is filtered, so the byte comparison above is not vacuous
        assertContains(output("file.txt"), "version=1.0-SNAPSHOT");
    }

    @Test
    @InjectMojo(goal = "resources")
    @MojoParameter(name = "encoding", value = "UTF-8")
    @MojoParameter(name = "fileNameFiltering", value = "true")
    void fileNamesAreFiltered(ResourcesMojo mojo) throws Exception {
        project().setGroupId("org.apache.maven.plugins");
        project().setArtifactId("maven-resources-plugin-it-filter-filenames");
        project().setVersion("1.0-SNAPSHOT");
        resource("filter-filenames", true);

        mojo.execute();

        for (String name : Arrays.asList("1.0-SNAPSHOT.txt", "maven-resources-plugin-it-filter-filenames.txt")) {
            String content = output(name);
            assertContains(content, "org.apache.maven.plugins");
            assertContains(content, "maven-resources-plugin-it-filter-filenames");
            assertContains(content, "1.0-SNAPSHOT");
        }
    }
}
