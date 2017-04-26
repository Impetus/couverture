package com.codecoverage.parser;

import java.util.ArrayList;
import java.util.Map;


/**
 * The Interface CodeCoverage.
 */
public interface CodeCoverage {

    /**
     * This method is use for Gets the file line no map it will take two parameter.
     *
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
