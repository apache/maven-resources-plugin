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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import org.apache.maven.api.plugin.testing.Basedir;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoParameter;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;

import static org.apache.maven.api.plugin.testing.MojoExtension.getBasedir;
import static org.apache.maven.api.plugin.testing.MojoExtension.setVariableValueToObject;
import static org.apache.maven.plugins.resources.ResourcesMojoFilteringTest.assertContains;
import static org.apache.maven.plugins.resources.ResourcesMojoFilteringTest.assertMatrixLines;
import static org.apache.maven.plugins.resources.ResourcesMojoFilteringTest.assertMatrixUserPropertyLines;
import static org.apache.maven.plugins.resources.ResourcesMojoFilteringTest.filters;
import static org.apache.maven.plugins.resources.ResourcesMojoFilteringTest.fixture;
import static org.apache.maven.plugins.resources.ResourcesMojoFilteringTest.read;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author Olivier Lamy
 * @version $Id$
 */
@MojoTest
class CopyResourcesMojoTest {

    @Inject
    private MavenProject project;

    @Inject
    private MavenSession session;

    @Test
    @InjectMojo(goal = "copy-resources")
    @Basedir("/unit/copy-resources-test")
    @MojoParameter(name = "outputDirectory", value = "${basedir}/filtered-resources")
    void copyWithoutFiltering(CopyResourcesMojo mojo) throws Exception {
        addResources(mojo, false);

        mojo.execute();

        Properties properties = getResultProperties();

        assertEquals("zorglub", properties.getProperty("config"));
        assertEquals("${project.version}", properties.getProperty("project.version"));
    }

    @Test
    @InjectMojo(goal = "copy-resources")
    @Basedir("/unit/copy-resources-test")
    @MojoParameter(name = "outputDirectory", value = "${basedir}/filtered-resources")
    void copyWithFiltering(CopyResourcesMojo mojo) throws Exception {

        addResources(mojo, true);
        when(project.getVersion()).thenReturn("1.2.3-SNAPSHOT");

        mojo.execute();

        Properties properties = getResultProperties();

        assertEquals("zorglub", properties.getProperty("config"));
        assertEquals("1.2.3-SNAPSHOT", properties.getProperty("project.version"));
    }

    private Properties getResultProperties() throws IOException {
        Properties properties = new Properties();
        Path result = new File(getBasedir(), "filtered-resources/config.properties").toPath();
        try (InputStream in = Files.newInputStream(result)) {
            properties.load(in);
        }
        return properties;
    }

    private void addResources(CopyResourcesMojo mojo, boolean filtered) {
        Resource resource = new Resource();
        resource.setDirectory(getBasedir() + "/resources");
        resource.setFiltering(filtered);
        mojo.setResources(Collections.singletonList(resource));
    }

    @Test
    @InjectMojo(goal = "copy-resources")
    @Basedir("/unit/copy-resources-test")
    @MojoParameter(name = "outputDirectory", value = "${basedir}/legacy-pom-prefix")
    @MojoParameter(name = "encoding", value = "UTF-8")
    void copyWithFilteringThroughTheLegacyPomPrefix(CopyResourcesMojo mojo) throws Exception {
        Resource resource = new Resource();
        resource.setDirectory(fixture("copy-resources-it").toString());
        resource.setFiltering(true);
        mojo.setResources(Collections.singletonList(resource));
        when(project.getArtifactId()).thenReturn("maven-resources-plugin-copy-resources-it");
        when(project.getVersion()).thenReturn("1.0.1-SNAPSHOT");

        mojo.execute();

        String content = read(new File(getBasedir(), "legacy-pom-prefix/configuration.properties").toPath());
        assertContains(content, "artifactId=maven-resources-plugin-copy-resources-it");
        assertContains(content, "version=1.0.1-SNAPSHOT");
    }

    @Test
    @InjectMojo(goal = "copy-resources")
    @Basedir("/unit/copy-resources-test")
    @MojoParameter(name = "outputDirectory", value = "${basedir}/own-filters")
    @MojoParameter(name = "encoding", value = "UTF-8")
    void copyWithOwnFiltersOnTopOfTheBuildFilters(CopyResourcesMojo mojo) throws Exception {
        Resource resource = new Resource();
        resource.setDirectory(fixture("filter-matrix/resources/MRESOURCES-77").toString());
        resource.setFiltering(true);
        mojo.setResources(Collections.singletonList(resource));
        when(project.getVersion()).thenReturn("1.0-SNAPSHOT");
        project.getProperties().setProperty("projectProperty", "foo-projectProperty-bar");
        project.getProperties().setProperty("profileProperty", "foo-profileProperty-bar");
        session.getUserProperties().setProperty("execProps", "fromExecProps");
        setVariableValueToObject(
                mojo, "buildFilters", filters("my.filter", "filter2.properties", "filter3.properties"));
        mojo.setFilters(Arrays.asList(filters("custom-filters.properties").get(0)));

        mojo.execute();

        String content = read(new File(getBasedir(), "own-filters/SomeResource.txt").toPath());
        assertMatrixLines(content);
        assertMatrixUserPropertyLines(content);
        assertContains(content, "var3=Foo-Bar/Stuff");
        assertContains(content, "custom=my-custom-filter-value");
    }
}
