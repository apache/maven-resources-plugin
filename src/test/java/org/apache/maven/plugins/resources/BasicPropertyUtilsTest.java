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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.shared.filtering.PropertyUtils;
import org.junit.jupiter.api.Test;

import static org.apache.maven.api.plugin.testing.MojoExtension.getBasedir;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MojoTest
public class BasicPropertyUtilsTest extends AbstractPropertyUtilsTest {
    protected static final String VALIDATION_FILE_NAME =
            "target/test-classes/unit/propertiesutils-test/basic_validation.properties";

    protected static final String PROP_FILE_NAME = "target/test-classes/unit/propertiesutils-test/basic.properties";

    protected Path getPropertyFile() {
        Path propFile = Paths.get(getBasedir(), PROP_FILE_NAME);

        if (!Files.exists(propFile)) {
            propFile = null;
        }

        return propFile;
    }

    protected Path getValidationFile() {
        Path validationFile = Paths.get(getBasedir(), VALIDATION_FILE_NAME);

        if (!Files.exists(validationFile)) {
            validationFile = null;
        }

        return validationFile;
    }

    /**
     * load property test case can be adjusted by modifying the basic.properties and basic_validation properties
     *
     * @throws Exception
     */
    @Test
    public void testBasicLoadPropertyFF() throws Exception {
        Properties prop = PropertyUtils.loadPropertyFile(propertyFile, false, false);

        assertNotNull(prop);
        assertTrue(validateProperties(prop));
    }

    /**
     * load property test case can be adjusted by modifying the basic.properties and basic_validation properties
     *
     * @throws Exception
     */
    @Test
    public void testBasicLoadPropertyTF() throws Exception {
        Properties prop = PropertyUtils.loadPropertyFile(propertyFile, true, false);

        assertNotNull(prop);
        assertTrue(validateProperties(prop));
    }

    /**
     * load property test case can be adjusted by modifying the basic.properties and basic_validation properties
     *
     * @throws Exception
     */
    @Test
    public void testBasicLoadPropertyTT() throws Exception {
        Properties prop = PropertyUtils.loadPropertyFile(propertyFile, true, true);

        validationProp.putAll(System.getProperties());
        assertNotNull(prop);
        assertTrue(validateProperties(prop));
    }

    /**
     * load property test case can be adjusted by modifying the basic.properties and basic_validation properties
     *
     * @throws Exception
     */
    @Test
    public void testNonExistentProperty() throws Exception {
        Properties prop = PropertyUtils.loadPropertyFile(propertyFile, true, true);

        validationProp.putAll(System.getProperties());
        assertNotNull(prop);
        assertNull(prop.getProperty("does_not_exist"));
    }
}
