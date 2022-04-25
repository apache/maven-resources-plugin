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
import java.io.FileNotFoundException;

import org.apache.maven.shared.filtering.PropertyUtils;
import org.junit.Test;

/**
 * @author <a href="mailto:BELMOUJAHID.I@Gmail.Com>Imad BELMOUJAHID</a> @ImadBL
 */
public class PropertyUtilsExceptionTest
{

    /**
     * load property test case can be adjusted by modifying the basic.properties and basic_validation properties
     * I added a null for this parameter (rootNode) because it is only used with json file @ImadBL
     *
     * @throws Exception
     */
    @Test( expected = FileNotFoundException.class )
    public void loadPropertyFileShouldFailWithFileNotFoundException()
        throws Exception
    {
        PropertyUtils.loadPropertyFile( new File( "NON_EXISTENT_FILE" ), true, true, null );
    }
}
