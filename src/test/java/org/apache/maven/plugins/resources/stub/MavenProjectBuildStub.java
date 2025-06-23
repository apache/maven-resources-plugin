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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.maven.api.model.Build;
import org.apache.maven.api.plugin.testing.MojoExtension;
import org.apache.maven.api.plugin.testing.stubs.ProjectStub;

public class MavenProjectBuildStub extends ProjectStub {
    protected final Path testRootDir;

    private final Path buildDirectory;

    protected Path outputDirectory;

    private final Path testOutputDirectory;

    private final Path resourcesDirectory;

    private final Path testResourcesDirectory;

    private final ArrayList<Path> fileList;

    private final ArrayList<Path> directoryList;

    private final HashMap<Path, String> dataMap;

    protected MavenProjectBuildStub(final String identifier) throws Exception {
        testRootDir = Path.of(MojoExtension.getBasedir(), "target", "test-classes", "unit", "test-dir", identifier);
        final Path srcDirectory = testRootDir.resolve("src");
        buildDirectory = testRootDir.resolve("target");
        outputDirectory = buildDirectory.resolve("classes");
        testOutputDirectory = buildDirectory.resolve("test-classes");
        resourcesDirectory = srcDirectory.resolve("main").resolve("resources");
        testResourcesDirectory = srcDirectory.resolve("test").resolve("resources");
        fileList = new ArrayList<>();
        directoryList = new ArrayList<>();
        dataMap = new HashMap<>();

        setBasedir(testRootDir);
        Files.createDirectories(testRootDir);
        setName("Test Project " + identifier);
        setGroupId("org.apache.maven.plugin.test");
        setArtifactId("maven-resource-plugin-test#" + identifier);
        setVersion(identifier);
        setPackaging("org.apache.maven.plugin.test");
        setDescription("this is a test project");
        setModel(getModel()
                .withBuild(Build.newBuilder()
                        .directory(buildDirectory.toString())
                        .outputDirectory(outputDirectory.toString())
                        .testOutputDirectory(testOutputDirectory.toString())
                        .build()));
    }

    public void setOutputDirectory(String dir) {
        outputDirectory = buildDirectory.resolve(dir);
        setModel(getModel().withBuild(getModel().getBuild().withOutputDirectory(outputDirectory.toString())));
    }

    public void addFile(Path name) {
        if (isValidPath(name.toString())) {
            fileList.add(name);
        }
    }

    public void addFile(Path name, String data) {
        addFile(name);
        dataMap.put(name.getFileName(), data);
    }

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    public Path getTestOutputDirectory() {
        return testOutputDirectory;
    }

    public Path getResourcesDirectory() {
        return resourcesDirectory;
    }

    public Path getTestResourcesDirectory() {
        return testResourcesDirectory;
    }

    /**
     * {@return true if the path is relative and false if absolute}.
     * Also returns false if it is relative to the parent.
     */
    private boolean isValidPath(String path) {
        return !(path.startsWith("c:") || path.startsWith("..") || path.startsWith("/") || path.startsWith("\\"));
    }

    public void cleanBuildEnvironment() throws Exception {
        deleteDirectory(resourcesDirectory);
        deleteDirectory(testResourcesDirectory);
        deleteDirectory(outputDirectory);
        deleteDirectory(testOutputDirectory);
    }

    protected static void deleteDirectory(final Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc != null) {
                        throw exc;
                    }
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    /**
     * Populates dummy resources and dummy test resources.
     *
     * @throws IOException if a directory or a file cannot be created.
     */
    public void setupBuildEnvironment() throws IOException {
        // setup src dir
        for (Path directory : directoryList) {
            Files.createDirectories(resourcesDirectory.resolve(directory));
            Files.createDirectories(testResourcesDirectory.resolve(directory));
        }
        for (Path file : fileList) {
            populateFile(resourcesDirectory.resolve(file));
            populateFile(testResourcesDirectory.resolve(file));
        }

        // setup target dir
        Files.createDirectories(outputDirectory);
        Files.createDirectories(testOutputDirectory);
    }

    private void populateFile(Path file) throws IOException {
        Files.createDirectories(file.getParent());
        String data = dataMap.get(file.getFileName());
        try (OutputStream outputStream = Files.newOutputStream(file)) {
            if (data != null) {
                outputStream.write(data.getBytes());
            }
        }
        if (!Files.exists(file)) {
            throw new IOException("Unable to create file: " + file);
        }
    }
}
