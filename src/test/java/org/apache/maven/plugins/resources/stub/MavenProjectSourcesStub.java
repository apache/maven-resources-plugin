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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.apache.maven.api.model.Build;
import org.apache.maven.api.model.Source;
import org.apache.maven.api.plugin.testing.MojoExtension;
import org.apache.maven.shared.filtering.Resource;

public final class MavenProjectSourcesStub extends MavenProjectBuildStub {

    public MavenProjectSourcesStub() throws Exception {
        super(MojoExtension.getTestId());
        setupSources("main");
        setupSources("test");
        deleteDirectory(outputDirectory);
        Files.createDirectories(outputDirectory);
    }

    private void setupSources(final String scope) {
        // setup default resources
        Source src = Source.newBuilder()
                .lang("resources")
                .scope(scope)
                .directory(testRootDir
                        .resolve("src")
                        .resolve(scope)
                        .resolve("resources")
                        .toString())
                .stringFiltering(false)
                .targetPath(null)
                .build();
        setModel(getModel()
                .withBuild(getModel()
                        .getBuild()
                        .withSources(concat(getModel().getBuild().getSources(), new Source[] {src}))));
    }

    public void addIncludes(String... patterns) {
        withSource(src -> src.withIncludes(concat(src.getIncludes(), patterns)));
    }

    public void addExcludes(String... patterns) {
        withSource(src -> src.withExcludes(concat(src.getExcludes(), patterns)));
    }

    public void setTargetPath(String path) {
        withSource(src -> src.withTargetPath(path));
    }

    public void setDirectory(String dir) {
        withSource(src -> src.withDirectory(dir));
    }

    public void setResourceFiltering(boolean filter) {
        withSource(r -> r.withStringFiltering(filter));
    }

    private <T> List<T> concat(Collection<T> collection, T[] item) {
        var list = new ArrayList<T>(collection);
        list.addAll(Arrays.asList(item));
        return list;
    }

    private void withSource(Function<Source, Source> mapper) {
        Build build = getModel().getBuild();
        setModel(getModel()
                .withBuild(build.withSources(
                        build.getSources().stream().map(mapper).toList())));
    }

    public List<Resource> getResources(String scope) {
        return getBuild().getSources().stream()
                .filter((source) ->
                        scope.equalsIgnoreCase(source.getScope()) && "resources".equalsIgnoreCase(source.getLang()))
                .map((source) -> {
                    Resource resource = new Resource();
                    resource.setDirectory(source.getDirectory());
                    resource.setFiltering(source.isStringFiltering());
                    resource.setExcludes(source.getExcludes());
                    resource.setIncludes(source.getIncludes());
                    resource.setTargetPath(source.getTargetPath());
                    return resource;
                })
                .toList();
    }
}
