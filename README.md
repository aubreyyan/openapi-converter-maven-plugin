[![Build Status](https://travis-ci.com/aubrey-y/openapi-converter-maven-plugin.svg?branch=master)](https://travis-ci.com/aubrey-y/openapi-converter-maven-plugin)

# OpenApi Converter Maven Plugin
This maven plugin converts between different OpenApi specification types.

# Requirements
Maven is required to build the plugin. To download Maven, follow the [instructions](http://maven.apache.org/).

# How to use
In your Maven App Engine Java app, add the following plugin to your pom.xml:

```xml
            <plugin>
                <groupId>com.google.cloud.tools</groupId>
                <artifactId>openapi-converter-maven-plugin</artifactId>
                <version>1.0.0-SNAPSHOT</version>
                <configuration>
                    <inputSpecification>openapi: "3.0.1"</inputSpecification>
                    <outputSpecification>swagger: "2.0"</outputSpecification>
                </configuration>
            </plugin>
```

All goals are prefixed with `com.google.cloud.tools:openapi-converter-maven-plugin:1.0.0-SNAPSHOT`.

## Client
The plugin exposes the following client side goals

- `convertOpenApiDocs`

The plugin exposes the following parameters for client side goals

- `openApiDocDir` - The input (and output) directory for editing Open Api documents (default is `${project.basedir}`)
- `inputFileName` - The input openapi (yaml) filename (default is `openapi.yaml`)
- `inputSpecification` - The input specification type to be modified (like `openapi: "3.0.1"`)
- `outputSpecification` - The output specification type (like `swagger: "2.0"`)

Note: the only available input+output combination right now that is supported is `openapi: "3.0.1"` -> `swagger: "2.0"`.

### Usage
The parameters `inputSpecification` and `outputSpecification` are required for the plugin goal to succeed. Exceptions
will be thrown if they are not provided, or invalid specifications and/or combinations are used.

Furthermore, for usage with [springdoc-openapi-maven-plugin](https://github.com/springdoc/springdoc-openapi-maven-plugin),
please include this plugin definition sequentially after springdoc's plugin and, because they both occur in the same
LifecyclePhase, will automatically generate your `openapi.yaml` with your desired specification.

You will have to add `convertOpenApiDocs` as an execution goal to the plugin definition, in line with
springdoc's.

Example:

```xml
            <plugin>
                <groupId>org.springdoc</groupId>
                <artifactId>springdoc-openapi-maven-plugin</artifactId>
                <version>1.0</version>
                <executions>
                    <execution>
                        <id>integration-test</id>
                        <goals>
                            <goal>generate</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <apiDocsUrl>http://localhost:8080/v3/api-docs.yaml</apiDocsUrl>
                    <outputFileName>openapi.yaml</outputFileName>
                    <outputDir>${project.basedir}</outputDir>
                </configuration>
            </plugin>
            <plugin>
                <groupId>com.google.cloud.tools</groupId>
                <artifactId>openapi-converter-maven-plugin</artifactId>
                <version>1.0.0-SNAPSHOT</version>
                <executions>
                    <execution>
                        <id>integration-test</id>
                        <goals>
                            <goal>convertOpenApiDocs</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <inputSpecification>openapi: "3.0.1"</inputSpecification>
                    <outputSpecification>swagger: "2.0"</outputSpecification>
                </configuration>
            </plugin>
```

Your openapi definition file will be automatically generated, then, whenever the Integration Test Lifecycle Phase happens,
which includes popular commands like `mvn clean install` and `mvn verify`.
