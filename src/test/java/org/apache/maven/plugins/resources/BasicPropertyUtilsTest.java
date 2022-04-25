package org.apache.maven.plugins.resources;

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

import java.io.File;
import java.util.Properties;

import org.apache.maven.shared.filtering.PropertyUtils;

/**
 * @author <a href="mailto:BELMOUJAHID.I@Gmail.Com>Imad BELMOUJAHID</a> @ImadBL
 */
public class BasicPropertyUtilsTest
    extends AbstractPropertyUtilsTest
{
    final static protected String validationFileName =
        "/target/test-classes/unit/propertiesutils-test/basic_validation.properties";

    final static protected String propFileName = "/target/test-classes/unit/propertiesutils-test/basic.properties";

    protected File getPropertyFile()
    {
        File propFile = new File( getBasedir(), propFileName );

        if ( !propFile.exists() )
        {
            propFile = null;
        }

        return propFile;
    }

    protected File getValidationFile()
    {
        File validationFile = new File( getBasedir(), validationFileName );

        if ( !validationFile.exists() )
        {
            validationFile = null;
        }

        return validationFile;
    }

    /**
     * load property test case can be adjusted by modifying the basic.properties and basic_validation properties
     * I added a null for this parameter (rootNode) because it is only used with json file @ImadBL
     *
     * @throws Exception
     */
    public void testBasicLoadProperty_FF()
        throws Exception
    {
        Properties prop = PropertyUtils.loadPropertyFile( propertyFile, false, false, null );

        assertNotNull( prop );
        assertTrue( validateProperties( prop ) );
    }

    /**
     * load property test case can be adjusted by modifying the basic.properties and basic_validation properties
     * I added a null for this parameter (rootNode) because it is only used with json file @ImadBL
     *
     * @throws Exception
     */
    public void testBasicLoadProperty_TF()
        throws Exception
    {
        Properties prop = PropertyUtils.loadPropertyFile( propertyFile, true, false, null );

        assertNotNull( prop );
        assertTrue( validateProperties( prop ) );
    }

    /**
     * load property test case can be adjusted by modifying the basic.properties and basic_validation properties
     * I added a null for this parameter (rootNode) because it is only used with json file @ImadBL
     *
     * @throws Exception
     */
    public void testBasicLoadProperty_TT()
        throws Exception
    {
        Properties prop = PropertyUtils.loadPropertyFile( propertyFile, true, true, null );

        validationProp.putAll( System.getProperties() );
        assertNotNull( prop );
        assertTrue( validateProperties( prop ) );
    }

    /**
     * load property test case can be adjusted by modifying the basic.properties and basic_validation properties
     * I added a null for this parameter (rootNode) because it is only used with json file @ImadBL
     *
     * @throws Exception
     */
    public void testNonExistentProperty()
        throws Exception
    {
        Properties prop = PropertyUtils.loadPropertyFile( propertyFile, true, true, null );

        validationProp.putAll( System.getProperties() );
        assertNotNull( prop );
        assertNull( prop.getProperty( "does_not_exist" ) );
    }

}
