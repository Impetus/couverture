package com.impetus.codecoverage.parser;

import java.util.ArrayList;
import java.util.Map;


/**
 * The Interface CodeCoverage.
 * It has two methods, namely getFileLineNoMap and computeFileLineNoMap.
 */
public interface CodeCoverage {

    /**
     * To get map of file name as key and list of lines changed for user story provided as input parameter
     * It takes two inputs, 1. Path to target project source folder and 2. The user story number to process.
     * @param pathToSrc the path to src
     * @param userStory the user story
     * @return the file line no map
     */
    public Map<String, ArrayList<String>> getFileLineNoMap(String pathToSrc, String userStory);

    /**
     * This method is use for Compute file line no map and it will take a two parameter.
     *
     * @param pathToProject the path to project
     * @param revisionFilesMap the revision files map
     * @return the map
     */
    public Map<String, ArrayList<String>> computeFileLineNoMap(String pathToProject, Map<String, ArrayList<String>> revisionFilesMap);
}
