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
import java.util.Collections;
import java.util.Properties;

import org.apache.maven.api.plugin.testing.Basedir;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoParameter;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;

import static org.apache.maven.api.plugin.testing.MojoExtension.getBasedir;
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
}
