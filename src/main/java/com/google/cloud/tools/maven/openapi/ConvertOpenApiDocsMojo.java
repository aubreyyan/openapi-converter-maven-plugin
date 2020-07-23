/*
 * Copyright (c) 2020 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.google.cloud.tools.maven.openapi;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/** Goal which regenerates openapi docs. */
@Mojo(
        name = "convertOpenApiDocs",
        defaultPhase = LifecyclePhase.INTEGRATION_TEST
)
public class ConvertOpenApiDocsMojo extends AbstractMojo {

    /** Input+Output directory for openapi docs. */
    @Parameter(defaultValue = "${project.basedir}", required = true)
    File openApiDocDir;

    @Parameter(defaultValue = "openapi.yaml")
    String inputFileName;

    @Parameter(required = true)
    String inputSpecification;

    @Parameter(required = true)
    String outputSpecification;

    private static final String OPENAPI_301 = "openapi: \"3.0.1\"";

    private static final String SWAGGER_20 = "swagger: \"2.0\"";

    @Override
    public void execute() throws MojoExecutionException {
        try {
            if(Arrays.stream(Objects.requireNonNull(openApiDocDir.listFiles())).map(File::getName).noneMatch(inputFileName::equals) && !openApiDocDir.mkdirs()) {
                throw new MojoExecutionException(String.format("%s does not exist in directory: %s", inputFileName, openApiDocDir.getAbsolutePath()));
            }
            else if(!inputSpecification.equals(OPENAPI_301)) {
                throw new MojoExecutionException(String.format("%s input spec is not supported at this time", inputSpecification));
            }
            else if(!outputSpecification.equals(SWAGGER_20)) {
                throw new MojoExecutionException(String.format("%s output spec is not supported at this time", outputSpecification));
            }

            String pathToOpenApi = String.format("%s/%s", openApiDocDir.getAbsolutePath(), inputFileName);

            List<String> openApi = this.readFile(pathToOpenApi);
            new File(pathToOpenApi).delete();

            List<String> transformedOpenApi;

            //Can add support for additional conversions in the future via else if
            if(inputSpecification.equals(OPENAPI_301) && outputSpecification.equals(SWAGGER_20)) {
                transformedOpenApi = this.transformOpenApi301ToSwagger20(openApi, outputSpecification);
            }
            else {
                throw new MojoExecutionException(String.format("Unsupported conversion between %s and %s", inputSpecification, outputSpecification));
            }

            File outputOpenApi = new File(pathToOpenApi);

            FileWriter fileWriter = new FileWriter(outputOpenApi);
            for(String ln: transformedOpenApi) {
                fileWriter.write(ln + "\n");
            }
            fileWriter.close();
        }
        catch (Exception e) {
            throw new MojoExecutionException("OpenApi Plugin Error", e);
        }
    }

    /**
     * Open and read a file, and return the lines in the file as a list
     * of Strings.
     */
    private List<String> readFile(String filename)
    {
        List<String> records = new ArrayList<>();
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null)
            {
                records.add(line);
            }
            reader.close();
            return records;
        }
        catch (Exception e)
        {
            System.err.format("Exception occurred trying to read '%s'.", filename);
            e.printStackTrace();
            return null;
        }
    }

    public List<String> transformOpenApi301ToSwagger20(List<String> openApi, String expectedSpec) {
        //First line defines new specification
        openApi.set(0, expectedSpec);

        //Edge case for description field, which is valid for info but must be removed in servers
        boolean isServers = false;
        for(Iterator<String> iterator = openApi.iterator(); iterator.hasNext();) {
            String ln = iterator.next();
            if(ln.startsWith("servers:")) {
                isServers = true;
            }
            if(isServers && ln.startsWith("  description:")) {
                iterator.remove();
                break;
            }
        }

        //Remove Swagger 2.0 invalid fields
        openApi.removeIf(ln -> ln.startsWith("servers:") || ln.startsWith("- url:") || ln.startsWith("        schema:") ||
                ln.startsWith("          format:") || ln.startsWith("          content:") || ln.startsWith("            '*/*':") ||
                ln.startsWith("              schema:") || ln.startsWith("                type:") || ln.startsWith("components:"));

        //Logically interpolate modified fields
        for(int i = 0; i < openApi.size(); i++) {
            if(openApi.get(i).startsWith("          type:")) {
                //Remove 2 spaces for Swagger 2.0 indentation
                openApi.set(i, openApi.get(i).substring(2));
            }
        }

        return openApi;
    }
}
