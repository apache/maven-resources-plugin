---
title: Frequently Asked Questions
---

<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<a id="top"></a>

# Frequently Asked Questions

1. [What are resources?](#What_are_resources.3F)
2. [When should I use the Resources Plugin's goal outside a lifecycle?](#When_should_I_use_the_resouces_plugin.27s_goal_outside_a_lifecycle.3F)
3. [Do my main resources go to my test resources as well?](#Do_my_main_resources_go_to_my_test_resources_as_well.3F)
4. [What encoding values are allowed?](#What_encoding_values_are_allowed.3F)

### What are resources?

Resources are non-source code files used by your project. Examples of these are properties files, images and
XML files.

<a id="When_should_I_use_the_resouces_plugin.27s_goal_outside_a_lifecycle.3F"></a>

### When should I use the Resources Plugin's goal outside a lifecycle?

The Maven Resource Plugin simply copies resources from your source to your build output (with optional
filtering). So if that's the only operation you are interested in, you can skip the other phases such as
compilation and testing and simply do

```
mvn resources:resources
```

For example, if you just debugged your configuration file and you want to manually test it in your container if
it works, you can simply do

```
mvn resources:resources
```

This will produce those configuration files on your output thus skipping the other phases which may eat up a
huge amount of your time.

### Do my main resources go to my test resources as well?

No. Your main resources and your test resources are separated from each other.

Your test resources should only be used by your tests. Thus, they are separated from the main to avoid any side
effects that may occur.

### What encoding values are allowed?

The Maven Resource Plugin only allows encoding values representing the charsets supported by the Java platform,
namely `US-ASCII`, `ISO-8859-1`, `UTF-8`, `UTF-16BE`, `UTF-16LE` and `UTF-16`.
