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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.maven.shared.filtering.PropertyUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.apache.maven.api.plugin.testing.MojoExtension.getBasedir;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PropertyUtilsTest {

    private File propertyFile;

    private Properties validationProp;

    @BeforeEach
    void setUp() throws Exception {

        // load data

        propertyFile = new File(getBasedir(), "/target/test-classes/unit/propertiesutils-test/test.properties");
        assertNotNull(propertyFile);

        File validationFile =
                new File(getBasedir(), "/target/test-classes/unit/propertiesutils-test/test_validation.properties");
        assertNotNull(validationFile);

        loadValidationProperties(validationFile);
    }

    /**
     * load property test case can be adjusted by modifying the basic.properties and basic_validation properties
     *
     * @throws Exception
     */
    @Test
    void testBasicLoadPropertyFF() throws Exception {
        Properties prop = PropertyUtils.loadPropertyFile(propertyFile, false, false);

        assertNotNull(prop);
        validateProperties(prop);
    }

    /**
     * load property test case can be adjusted by modifying the basic.properties and basic_validation properties
     *
     * @throws Exception
     */
    @Test
    void testBasicLoadPropertyTF() throws Exception {
        Properties prop = PropertyUtils.loadPropertyFile(propertyFile, true, false);

        assertNotNull(prop);
        validateProperties(prop);
    }

    /**
     * load property test case can be adjusted by modifying the basic.properties and basic_validation properties
     *
     * @throws Exception
     */
    @Test
    void testBasicLoadPropertyTT() throws Exception {
        Properties prop = PropertyUtils.loadPropertyFile(propertyFile, true, true);

        validationProp.putAll(System.getProperties());
        assertNotNull(prop);
        validateProperties(prop);
    }

    /**
     * load property test case can be adjusted by modifying the basic.properties and basic_validation properties
     *
     * @throws Exception
     */
    @Test
    void testNonExistentProperty() throws Exception {
        Properties prop = PropertyUtils.loadPropertyFile(propertyFile, true, true);

        validationProp.putAll(System.getProperties());
        assertNotNull(prop);
        assertNull(prop.getProperty("does_not_exist"));
    }

    @Test
    void loadPropertyFileShouldFailWithFileNotFoundException() {

        assertThrows(
                FileNotFoundException.class,
                () -> PropertyUtils.loadPropertyFile(new File("NON_EXISTENT_FILE"), true, true));
    }

    private void validateProperties(Properties prop) {

        Enumeration<?> propKeys = prop.keys();
        String key;

        while (propKeys.hasMoreElements()) {
            key = (String) propKeys.nextElement();
            Assertions.assertEquals(
                    validationProp.getProperty(key), prop.getProperty(key), "Property value mismatch for key: " + key);
        }
    }

    /**
     * load the property file for cross checking the
     * values in the processed property file
     *
     * @param validationPropFile
     */
    private void loadValidationProperties(File validationPropFile) throws IOException {
        validationProp = new Properties();
        try (InputStream in = Files.newInputStream(validationPropFile.toPath())) {
            validationProp.load(in);
        }
    }
}
