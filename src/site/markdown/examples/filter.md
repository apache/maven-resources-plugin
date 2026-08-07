---
title: Filtering
author: 
  - Franz Allan See
date: 2008-09-05
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

# Filtering

Variables can be included in your resources. These variables, denoted by the `${...}` or `@...@` delimiters, can come from the system properties, your project properties, from your filter resources and from the command line.

For example, if we have a resource `src/main/resources/hello.txt` containing

```unknown
Hello ${name}
```

And a POM like this

```xml
<project>
  ...
  <name>My Resources Plugin Practice Project</name>
  ...
  <build>
    ...
    <resources>
      <resource>
        <directory>src/main/resources</directory>
      </resource>
      ...
    </resources>
    ...
  </build>
  ...
</project>
```

Upon calling

```shell
mvn resources:resources
```

This will create a resource output in `target/classes/hello.txt` which contains exactly the same text.

```unknown
Hello ${name}
```

However, if we add a `<filtering>` element to our POM and set it to `true` like this:

```xml
<project>
      ...
      <resource>
        <directory>src/main/resources</directory>
        <filtering>true</filtering>
      </resource>
      ...
</project>
```

Our `target/classes/hello.txt` after calling

```shell
mvn resources:resources
```

would be

```unknown
Hello My Resources Plugin Practice Project
```

That's because the name variable was replaced by the value of the project's name (which was specified in the POM).

Moreover, we can also assign values through the command line using the "-D" option. For example, to change the value for the variable `name` to "world", we can simply invoke this command:

```shell
mvn resources:resources -Dname="world"
```

And the output in `target/classes/hello.txt` would be

```unknown
Hello world
```

Furthermore, we are not limited to use pre-defined project variables. We can specify our own variables and their values in the `<properties>` element. For example, if we want to change the variable from "name" to "your.name", we can do so by adding a `<your.name>` element within the `<properties>` element.

```xml
<project>
  ...
  <properties>
    <your.name>world</your.name>
  </properties>
  ...
</project>
```

But to organize your project, you may want to put all your variables and their values in a separate file so that you will not have to rewrite your POM, or set their values all the time with every build. This can be done by adding a filter.

```xml
<project>
  ...
  <name>My Resources Plugin Practice Project</name>
  ...
  <build>
    ...
    <filters>
      <filter>[a filter property]</filter>
    </filters>
    ...
  </build>
  ...
</project>
```

For example, we can separate "your.name" from the POM by specifying a filter file `my-filter-values.properties` containing:

```properties
your.name = world
```

and adding that to our POM

```xml
<project>
    ...
    <filters>
      <filter>my-filter-values.properties</filter>
    </filters>
    ...
</project>
```

**Warning:** Do not filter files with binary content like images! This will most likely result in corrupt output.

If you have both text files and binary files as resources it is recommended to have two separated folders. One folder `src/main/resources` (default) for the resources which are not filtered and another folder `src/main/resources-filtered` for the resources which are filtered.

```xml
<project>
  ...
  <build>
    ...
    <resources>
      <resource>
        <directory>src/main/resources-filtered</directory>
        <filtering>true</filtering>
      </resource>
      ...
    </resources>
    ...
  </build>
  ...
</project>
```

Now you can put those files into `src/main/resources` which should not filtered and the other files into `src/main/resources-filtered`.

As already mentioned filtering binary files like images,pdf\`s etc. could result in corrupted output. To prevent such problems you can [configure file extensions](./binaries-filtering.html) which will not being filtered.
