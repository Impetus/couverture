package com.impetus.codecoverage.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * The Class SimpleGITCodeCoverage is used for get the git code coverage.
 */
/*
 * Author: Saurabh Juneja
 * Entry Method: getFileLineNoMap
 * 		Step 1: git log and grep the user story number to find revision list
 * 		output: revision list
 * 		Step 2: call getRevisionFileMap(revision list)
 * 			Step 2.1: git show command for each revision, to get List of file names changed in this revision
 * 			Output: list(revision, list of file names changed in this revision)
 * 		Step 3: call computeFileLineNoMap(list(revision, list of file names changed in this revision))
 * 			Step 3.1: git blame each file, create list of line no having us changes.
 * 			Output: list(filename, list of line numbers)
 * 			returned as m
 */
public class SimpleGITCodeCoverage implements CodeCoverage {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(SimpleGITCodeCoverage.class);
    
    /** The Constant GIT_DIR. */
    private static final String GIT_DIR = "git --git-dir ";

    /**
     * This method is used to Get a map with revision as key and names of the file that were modified in that revision as map 
     * It will take a two parameter and return the value as map and throw a sqlException.
     * it uses command git show --pretty=format: --name-only <revision number> as command as raw data to get the required information
     * it only process the .java file whose name does not end with Test
     * 
     * @param pathToProject the path to project
     * @param revisionList the revision list
     * @return the revision file map
     * @throws SQLException the SQL exception
     */
    public Map<String, ArrayList<String>> getRevisionFileMap(String pathToProject, List<String> revisionList) throws SQLException {
        Map<String, ArrayList<String>> revisionFileMap = new HashMap	();
        String commandToBeExecuted ;
        BufferedReader stdInput ;
        Process p = null;
        for (String revision : revisionList) {
            LOGGER.info("revision: " + revision);
            commandToBeExecuted = GIT_DIR + pathToProject + "/.git --work-tree=" + pathToProject + " show --pretty=format: --name-only "
                    + revision.replaceAll("\"", "");

            try {
                p = Runtime.getRuntime().exec(commandToBeExecuted);
            } catch (IOException e) {
                LOGGER.error(e);
            }
                        
             stdInput = new BufferedReader(new InputStreamReader((InputStream) ((p!=null)?p.getInputStream():"")));

            String s = null;
            List<String> files = new ArrayList();
            try {
                while ((s = stdInput.readLine()) != null) {
                    if (s.length() > 0 && s.endsWith(".java")  && !s.endsWith("Test.java")) {
                        files.add(s);
                    } 
                    //Saurabh: comment this to include non java and test files also.
                }
            } catch (IOException e) {
                LOGGER.error(e);
            }
            revisionFileMap.put(revision, (ArrayList<String>) files);
            LOGGER.info("RevisionFileMap: " + revisionFileMap);
        }
        return revisionFileMap;
    }

    /* 
     * @see com.codecoverage.parser.CodeCoverage#computeFileLineNoMap(java.lang.String, java.util.Map)
     * It returns a map with file name and its line numbers changed in the given revision
     * it takes a map with key as revision and file names as value, as an important input to process
     * it blames each file and grep revision number over the blame result to get line number that were changed in given revision
     * git blame --porcelain <file>
     */
    @Override
    public Map<String, ArrayList<String>> computeFileLineNoMap(String pathToProject, Map<String, ArrayList<String>> revisionFileNameMap) {
        Map<String, ArrayList<String>> fileLineNoMap = new HashMap();
        for (Map.Entry<String, ArrayList<String>> entry : revisionFileNameMap.entrySet()) {
            String revision = entry.getKey();
            List<String> fileList = entry.getValue();
            String commandToBeExecuted ;
            BufferedReader stdInput ;
            Process p ;
            for (String file : fileList) {
            	//--line-porcelain 
                commandToBeExecuted = GIT_DIR + pathToProject + "/.git " + "--work-tree=" + pathToProject + " blame -w --porcelain " + pathToProject + "/" + file;
                LOGGER.info("commandToBeExecuted=" + commandToBeExecuted);
                try {
                    p = Runtime.getRuntime().exec(commandToBeExecuted);
                    stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    String s ;

                    List<String> lineNo = new ArrayList();
                    while ((s = stdInput.readLine()) != null) {
                        if (s.length() > 0 && s.startsWith(revision.replaceAll("\"", ""))) {
                            String[] a = s.split("\\s+");
                            LOGGER.info("a=" + a[2]);
                            lineNo.add(a[2]);
                        }
                    }

                    while ((s = stdError.readLine()) != null) {
                        LOGGER.info("stdError" + s);
                    }

                    if (!lineNo.isEmpty()) {
                        Set<String> lines = new HashSet<>(lineNo);
                        if (fileLineNoMap.containsKey(file)) {
                            List<String> lineArr = fileLineNoMap.get(file);
                            lineArr.addAll(lineNo);
                            lines.addAll(lineArr);
                        }
                        fileLineNoMap.put(file, new ArrayList<>(lines));
                    }
                } catch (IOException e) {
                    LOGGER.error(e);
                }
            }
        }
        return fileLineNoMap;
    }

    /* 
     * @see com.codecoverage.parser.CodeCoverage#getFileLineNoMap(java.lang.String, java.lang.String)
     * To get map of file name as key and list of lines changed for user story provided as input parameter
     * It takes two inputs, 1. Path to target project source folder and 2. The user story number to process
     * to get the information, it does the following steps
     * 1. generate git log for the project root directory
     * 2. grep user story in the git log output
     * 3. prepare a revision number list of all the checkins for given user story 
     * 4. call getRevisionFileMap passing revision number list 
     * 5. getRevisionFileMap returns revisionFileNameMap: a map with revision as key and list of files as value changed for that revision
     * 6. finally we call computeFileLineNoMap passing revisionFileNameMap 
     * 7. and return file name and its line no map back for further coverage calculation to the calling method.
     */
    @Override
    public Map<String, ArrayList<String>> getFileLineNoMap(String pathToSrc, String userStory) {

        String sCurrentLine = null;

        String userStoryMessage = userStory;

        Map<String, ArrayList<String>> fileLineNoMap = null;

        Map<String, ArrayList<String>> revisionFileNameMap = null;

        String commandToBeExecuted = GIT_DIR + pathToSrc
                + "/.git log --pretty=format:\"revision=\"%H\",%nauthor=\"%an\",%nmessage=\"%s\",%ndate=\"%cd\",\" --grep=" + userStoryMessage + " ";
        try {

            LOGGER.info("commandToBeExecuted:: " + commandToBeExecuted);
            Process p = Runtime.getRuntime().exec(commandToBeExecuted);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            List<String> revisionList = new ArrayList();

            while ((sCurrentLine = stdInput.readLine()) != null) {
                if (sCurrentLine.startsWith("\"revision=") || sCurrentLine.startsWith("revision=")) {
                    revisionList.add(sCurrentLine.substring(sCurrentLine.indexOf("=") + 1, sCurrentLine.indexOf(",")));
                }
            }

            while ((sCurrentLine = stdError.readLine()) != null) {
                LOGGER.info("stdError" + sCurrentLine);
            }

            revisionFileNameMap = getRevisionFileMap(pathToSrc, revisionList);

            LOGGER.info("Logs Saved Successfully....." + revisionFileNameMap);

            return computeFileLineNoMap(pathToSrc, revisionFileNameMap);

        } catch (Exception e) {
            LOGGER.error(e);
        }
        return fileLineNoMap;
    }

}
