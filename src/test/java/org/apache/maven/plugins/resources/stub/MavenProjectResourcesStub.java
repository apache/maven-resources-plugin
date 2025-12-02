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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.maven.model.Build;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

public class MavenProjectResourcesStub extends MavenProject {
    private String testRootDir;

    private Properties properties;

    private String description;

    private Build build;

    private String srcDirectory;

    private String buildDirectory;

    private String outputDirectory;

    private String testOutputDirectory;

    private String resourcesDirectory;

    private String testResourcesDirectory;

    private ArrayList<String> fileList;

    private ArrayList<String> directoryList;

    private HashMap<String, String> dataMap;

    public MavenProjectResourcesStub(String testRootDir) {
        this.testRootDir = testRootDir;
        properties = new Properties();
        build = new Build();
        fileList = new ArrayList<>();
        directoryList = new ArrayList<>();
        dataMap = new HashMap<>();

        if (!FileUtils.fileExists(testRootDir)) {
            FileUtils.mkdir(testRootDir);
        }

        setupBuild();

        setupResources();
        setupTestResources();
    }

    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    public void addFile(String name) {
        if (isValidPath(name)) {
            fileList.add(name);
        }
    }

    public void addFile(String name, String data) {
        File fileName = new File(name);

        addFile(name);
        dataMap.put(fileName.getName(), data);
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public String getTestOutputDirectory() {
        return testOutputDirectory;
    }

    public String getResourcesDirectory() {
        return resourcesDirectory;
    }

    /**
     * returns true if the path is relative
     * and false if absolute
     * also returns false if it is relative to
     * the parent
     */
    private boolean isValidPath(String path) {
        return !path.startsWith("c:") && !path.startsWith("..") && !path.startsWith("/") && !path.startsWith("\\");
    }

    private void setupBuild() {
        // check getBasedir method for the exact path
        // we need to recreate the dir structure in
        // an isolated environment
        srcDirectory = testRootDir + "/src";
        buildDirectory = testRootDir + "/target";
        outputDirectory = buildDirectory + "/classes";
        testOutputDirectory = buildDirectory + "/test-classes";
        resourcesDirectory = srcDirectory + "/main/resources/";
        testResourcesDirectory = srcDirectory + "/test/resources/";

        build.setDirectory(buildDirectory);
        build.setOutputDirectory(outputDirectory);
        build.setTestOutputDirectory(testOutputDirectory);

        properties.setProperty("project.build.sourceEncoding", "UTF-8");
    }

    public void setupBuildEnvironment() throws Exception {
        // populate dummy resources and dummy test resources

        // setup src dir
        if (!FileUtils.fileExists(resourcesDirectory)) {
            FileUtils.mkdir(resourcesDirectory);
        }

        if (!FileUtils.fileExists(testResourcesDirectory)) {
            FileUtils.mkdir(testResourcesDirectory);
        }

        createDirectories(resourcesDirectory, testResourcesDirectory);
        createFiles(resourcesDirectory, testResourcesDirectory);

        // setup target dir
        if (!FileUtils.fileExists(outputDirectory)) {
            FileUtils.mkdir(outputDirectory);
        }

        if (!FileUtils.fileExists(testOutputDirectory)) {
            FileUtils.mkdir(testOutputDirectory);
        }
    }

    public void addInclude(String pattern) {
        build.getResources().get(0).addInclude(pattern);
    }

    public void addExclude(String pattern) {
        build.getResources().get(0).addExclude(pattern);
    }

    public void setTargetPath(String path) {
        build.getResources().get(0).setTargetPath(path);
    }

    public void setResourceFiltering(int nIndex, boolean filter) {
        if (build.getResources().size() > nIndex) {
            build.getResources().get(nIndex).setFiltering(filter);
        }
    }

    private void createDirectories(String parent, String testparent) {
        File currentDirectory;

        for (String directory : directoryList) {
            currentDirectory = new File(parent, "/" + directory);

            if (!currentDirectory.exists()) {
                currentDirectory.mkdirs();
            }

            // duplicate dir structure in test resources
            currentDirectory = new File(testparent, "/" + directory);

            if (!currentDirectory.exists()) {
                currentDirectory.mkdirs();
            }
        }
    }

    private void createFiles(String parent, String testparent) throws IOException {
        File currentFile;

        for (String file : fileList) {
            currentFile = new File(parent, file);

            // create the necessary parent directories
            // before we create the files
            if (!currentFile.getParentFile().exists()) {
                currentFile.getParentFile().mkdirs();
            }

            if (!currentFile.exists()) {
                currentFile.createNewFile();
                populateFile(currentFile);
            }

            // duplicate file in test resources
            currentFile = new File(testparent, file);

            if (!currentFile.getParentFile().exists()) {
                currentFile.getParentFile().mkdirs();
            }

            if (!currentFile.exists()) {
                currentFile.createNewFile();
                populateFile(currentFile);
            }
        }
    }

    private void populateFile(File file) throws IOException {
        String data = dataMap.get(file.getName());

        if ((data != null) && file.exists()) {
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                outputStream.write(data.getBytes());
            }
        }
    }

    private void setupResources() {
        Resource resource = new Resource();

        // setup default resources
        resource.setDirectory(getBasedir().getPath() + "/src/main/resources");
        resource.setFiltering(false);
        resource.setTargetPath(null);
        build.addResource(resource);
    }

    private void setupTestResources() {
        Resource resource = new Resource();

        // setup default test resources
        resource.setDirectory(getBasedir().getPath() + "/src/test/resources");
        resource.setFiltering(false);
        resource.setTargetPath(null);
        build.addTestResource(resource);
    }

    @Override
    public File getBasedir() {
        return new File(testRootDir);
    }

    @Override
    public Build getBuild() {
        return build;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public void setDescription(String desc) {
        description = desc;
    }

    @Override
    public String getDescription() {
        if (description == null) {
            return "this is a test project";
        } else {
            return description;
        }
    }

    @Override
    public List<Resource> getTestResources() {
        return build.getTestResources();
    }

    @Override
    public List<Resource> getResources() {
        return build.getResources();
    }
}
