package com.impetus.codecoverage.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;


/**
 * The Class ClearcaseCodeCoverage.
 */
public class ClearcaseCodeCoverage implements CodeCoverage {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(ClearcaseCodeCoverage.class);

    /* (non-Javadoc)
     * @see com.codecoverage.parser.CodeCoverage#getFileLineNoMap(java.lang.String, java.lang.String)
     */
    @Override
    public Map<String, ArrayList<String>> getFileLineNoMap(String pathToSrc, String userStory) {
        try {
            String[] cmd1 = { "cmd.exe", "/C", "cleartool lshistory -r -fmt \"%n,%c\" |findstr /R \"" + userStory + "$\"" };

            Process p = Runtime.getRuntime().exec(cmd1, null, new File(pathToSrc));

            // For normal stream reading
            BufferedReader br1 = new BufferedReader(new InputStreamReader(p.getInputStream()));

            // For error stream reading
            BufferedReader br2 = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            Map<String, ArrayList<String>> revisionFilesMap = new ConcurrentHashMap();

            String sCurrentLine;

            // Normal reading takes place here
            while ((sCurrentLine = br1.readLine()) != null) {
                String revision = sCurrentLine.substring(sCurrentLine.indexOf("@") + 2, sCurrentLine.indexOf(","));
                String fileName = sCurrentLine.substring(0, sCurrentLine.indexOf("@"));

                if (fileName.endsWith(".txt") || fileName.endsWith(".java")) {
                    getRevisionFilesMap(revision, fileName, revisionFilesMap);
                }
            }
            // Error stream reading starts here, if any
            while ((sCurrentLine = br2.readLine()) != null) {
                LOGGER.info(sCurrentLine);
            }

            return computeFileLineNoMap(pathToSrc, revisionFilesMap);
        } catch (IOException ioe) {
            LOGGER.error("ERROR: " + ioe);
            return null;
        }
    }

    /* (non-Javadoc)
     * @see com.codecoverage.parser.CodeCoverage#computeFileLineNoMap(java.lang.String, java.util.Map)
     */
    @Override
    public Map<String, ArrayList<String>> computeFileLineNoMap(String pathToProject, Map<String, ArrayList<String>> revisionFilesMap) {
        try {
            Map<String, ArrayList<String>> fileLineNoMap = new HashMap();
            for (Map.Entry<String, ArrayList<String>> entry : revisionFilesMap.entrySet()) {

                String revision = entry.getKey();
                List<String> fileListValue = entry.getValue();

                for (String file : fileListValue) {
                    String[] cmd2 = { "cmd.exe", "/C", "cleartool annotate -out - -fmt \"%Vn |\" -nheader " + file };

                    Process p1 = Runtime.getRuntime().exec(cmd2, null, new File(pathToProject));

                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(p1.getInputStream()));

                    String s ;
                    List<String> lineNo = new ArrayList();
                    Integer countLineNumber = 0;

                    while ((s = stdInput.readLine()) != null) {
                        countLineNumber++;
                        String revisionInLog = s.substring(0, s.indexOf("|") - 1);
                        String revisionContainsDot = s.substring(s.indexOf("\\") + 4, s.indexOf("|"));
                        computeLineNo(revision, revisionInLog, revisionContainsDot, lineNo, countLineNumber);
                    }
                    List<String> lineNumber;
                    if (fileLineNoMap.get(file) != null) {
                        lineNumber = fileLineNoMap.get(file);
                        lineNumber.addAll(lineNo);
                        fileLineNoMap.put(file, (ArrayList<String>) lineNumber);
                    } else {
                        fileLineNoMap.put(file, (ArrayList<String>) lineNo);
                    }
                }

            }

            return fileLineNoMap;
        } catch (IOException ioe) {
            LOGGER.error("ERROR: " + ioe);
            return null;
        }
    }

    /**
     *This method check fileList if fileList is not empty then add the List into file and Gets the revision files map.
     *
     * @param revision the revision
     * @param fileName the file name
     * @param revisionFilesMap the revision files map
     * @return the revision files map
     */
    private static void getRevisionFilesMap(String revision, String fileName, Map<String, ArrayList<String>> revisionFilesMap) {
        List<String> fileList = new ArrayList();
        if (revisionFilesMap.isEmpty()) {
            fileList.add(fileName);
            revisionFilesMap.put(revision, (ArrayList<String>) fileList);
        } else {
            if (revisionFilesMap.containsKey(revision)) {
                fileList = revisionFilesMap.get(revision);
                fileList.add(fileName);
                revisionFilesMap.put(revision, (ArrayList<String>) fileList);
            } else {
                List<String> newFileList = new ArrayList();
                newFileList.add(fileName);
                revisionFilesMap.put(revision, (ArrayList<String>) newFileList);
            }
        }
    }

    /**
     * This method is used for Compute the line number for codeCoverage and it's  method take's a five parameter .
     *
     * @param revision the revision
     * @param revisionInLog the revision in log
     * @param revisionContainsDot the revision contains dot
     * @param lineNo the line no
     * @param countLineNumber the count line number
     */
    private static void computeLineNo(String revision, String revisionInLog, String revisionContainsDot, List<String> lineNo, Integer countLineNumber) {
        if (revisionInLog.contains(revision)) {
            lineNo.add(String.valueOf(countLineNumber));
        } else {
            if (revisionContainsDot.contains(".") ) {
                lineNo.add(String.valueOf(countLineNumber));
            }
        }
    }

}
