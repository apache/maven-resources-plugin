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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.shared.filtering.PropertyUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.apache.maven.api.plugin.testing.MojoExtension.getBasedir;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TODO: do we still need this test? {@code PropertyUtils} is used nowhere else.
 */
@MojoTest
public class BasicPropertyUtilsTest {
    private static final String VALIDATION_FILE_NAME =
            "target/test-classes/unit/propertiesutils-test/basic_validation.properties";

    private static final String PROP_FILE_NAME = "target/test-classes/unit/propertiesutils-test/basic.properties";

    private Path propertyFile;

    private Properties validationProp;

    @BeforeEach
    protected void setUp() throws Exception {
        // load data
        propertyFile = Path.of(getBasedir(), PROP_FILE_NAME);
        assertTrue(Files.exists(propertyFile));

        Path validationFile = Path.of(getBasedir(), VALIDATION_FILE_NAME);
        assertTrue(Files.exists(validationFile));

        validationProp = new Properties();
        try (InputStream in = Files.newInputStream(validationFile)) {
            validationProp.load(in);
        }
    }

    private boolean validateProperties(Properties prop) {
        if (prop.isEmpty()) {
            return false;
        }
        Enumeration<?> propKeys = prop.keys();
        while (propKeys.hasMoreElements()) {
            String key = (String) propKeys.nextElement();
            if (!prop.getProperty(key).equals(validationProp.getProperty(key))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Loads property test case can be adjusted by modifying the basic.properties and basic_validation properties.
     */
    @Test
    public void testBasicLoadPropertyFF() throws Exception {
        Properties prop = PropertyUtils.loadPropertyFile(propertyFile, false, false);
        assertNotNull(prop);
        assertTrue(validateProperties(prop));
    }

    /**
     * Loads property test case can be adjusted by modifying the basic.properties and basic_validation properties.
     */
    @Test
    public void testBasicLoadPropertyTF() throws Exception {
        Properties prop = PropertyUtils.loadPropertyFile(propertyFile, true, false);
        assertNotNull(prop);
        assertTrue(validateProperties(prop));
    }

    /**
     * Loads property test case can be adjusted by modifying the basic.properties and basic_validation properties.
     */
    @Test
    public void testBasicLoadPropertyTT() throws Exception {
        Properties prop = PropertyUtils.loadPropertyFile(propertyFile, true, true);
        validationProp.putAll(System.getProperties());
        assertNotNull(prop);
        assertTrue(validateProperties(prop));
    }

    /**
     * Loads property test case can be adjusted by modifying the basic.properties and basic_validation properties.
     */
    @Test
    public void testNonExistentProperty() throws Exception {
        Properties prop = PropertyUtils.loadPropertyFile(propertyFile, true, true);
        validationProp.putAll(System.getProperties());
        assertNotNull(prop);
        assertNull(prop.getProperty("does_not_exist"));
    }

    /**
     * Loads property test case can be adjusted by modifying the basic.properties and basic_validation properties.
     */
    @Test
    public void loadPropertyFileShouldFailWithFileNotFoundException() {
        assertThrows(
                FileNotFoundException.class,
                () -> PropertyUtils.loadPropertyFile(Path.of("NON_EXISTENT_FILE"), true, true));
    }
}
