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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.apache.maven.api.model.Build;
import org.apache.maven.api.model.Resource;
import org.apache.maven.api.plugin.testing.MojoExtension;
import org.codehaus.plexus.util.FileUtils;

public class MavenProjectResourcesStub extends MavenProjectBuildStub {

    public MavenProjectResourcesStub() throws Exception {
        super(MojoExtension.getTestId());
        setupResources();
        setupTestResources();
        Path outputDirectory = Paths.get(getOutputDirectory());
        FileUtils.deleteDirectory(outputDirectory.toFile());
        Files.createDirectories(outputDirectory);
    }

    public void addInclude(String pattern) {
        withResource(r -> r.withIncludes(concat(r.getIncludes(), pattern)));
    }

    public void addExclude(String pattern) {
        withResource(r -> r.withExcludes(concat(r.getExcludes(), pattern)));
    }

    public void addTestInclude(String pattern) {
        withTestResource(r -> r.withIncludes(concat(r.getIncludes(), pattern)));
    }

    public void addTestExclude(String pattern) {
        withTestResource(r -> r.withExcludes(concat(r.getExcludes(), pattern)));
    }

    public void setTargetPath(String path) {
        withResource(r -> r.withTargetPath(path));
    }

    public void setTestTargetPath(String path) {
        withTestResource(r -> r.withTargetPath(path));
    }

    public void setDirectory(String dir) {
        withResource(r -> r.withDirectory(dir));
    }

    public void setTestDirectory(String dir) {
        withTestResource(r -> r.withDirectory(dir));
    }

    public void setResourceFiltering(boolean filter) {
        withResource(r -> r.withFiltering(Boolean.toString(filter)));
    }

    private <T> List<T> concat(Collection<T> collection, T item) {
        List<T> list = new ArrayList<>(collection);
        list.add(item);
        return list;
    }

    private void withResource(
            Function<org.apache.maven.api.model.Resource, org.apache.maven.api.model.Resource> mapper) {
        Build build = getModel().getBuild();
        setModel(getModel()
                .withBuild(build.withResources(Collections.singletonList(
                        mapper.apply(build.getResources().get(0))))));
    }

    private void withTestResource(
            Function<org.apache.maven.api.model.Resource, org.apache.maven.api.model.Resource> mapper) {
        Build build = getModel().getBuild();
        setModel(getModel()
                .withBuild(build.withTestResources(Collections.singletonList(
                        mapper.apply(build.getTestResources().get(0))))));
    }

    private void setupResources() {
        // see MavenProjectBasicStub for details
        // of getBasedir

        // setup default resources
        Resource resource = Resource.newBuilder()
                .directory(testRootDir + "/src/main/resources")
                .filtering(Boolean.toString(false))
                .targetPath(null)
                .build();
        setModel(getModel()
                .withBuild(getModel()
                        .getBuild()
                        .withResources(concat(getModel().getBuild().getResources(), resource))));
    }

    private void setupTestResources() {
        // see MavenProjectBasicStub for details
        // of getBasedir

        // setup default test resources
        Resource resource = Resource.newBuilder()
                .directory(testRootDir + "/src/test/resources")
                .filtering(Boolean.toString(false))
                .targetPath(null)
                .build();
        setModel(getModel()
                .withBuild(getModel()
                        .getBuild()
                        .withTestResources(concat(getModel().getBuild().getTestResources(), resource))));
    }

    @Override
    public String getOutputDirectory() {
        return getBuild().getOutputDirectory();
    }

    @Override
    public String getTestOutputDirectory() {
        return getBuild().getTestOutputDirectory();
    }
}
