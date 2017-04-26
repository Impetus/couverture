package com.codecoverage.runner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.log4j.Logger;


/**
 * The Class MavenCoberturaRunner is used for run the command and copy one file into other file.
 */
public class MavenCoberturaRunner {
    
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(MavenCoberturaRunner.class);
    
    /** The Constant JACOCO. */
    private static final String JACOCO = "Jacoco";

    /**
     * Instantiates a new maven cobertura runner.
     */
    private MavenCoberturaRunner() {
        // private constructor
    }

    /**
     *  This method return true or false and take a three parameters mvnDir,pathToSrc and codeCoverageTool and cover the current line of code and clean the mvn package and also covered the current line code .
     *
     * @param mvnDir the mvn dir
     * @param pathToSrc the path to src
     * @param os the os
     * @param codeCoverageTool the code coverage tool
     * @return true, if successful
     */
    public static boolean run(String mvnDir, String pathToSrc, String os, String codeCoverageTool) {
        Process p ;
        boolean flag = false;
        String sCurrentLine ;
        String commandToExecute = "";
        String dir ;
        if (mvnDir != null && pathToSrc != null) {

            if ("windows".equalsIgnoreCase(os)) {
                dir = mvnDir + "/bin/mvn.cmd ";
            } else {
                dir = mvnDir + "/bin/mvn ";
            }
            LOGGER.info("mvnDir=" + dir);

            if(codeCoverageTool.equals(JACOCO)){
                commandToExecute = dir + " clean package";
            } else {
                commandToExecute = dir + " -f " + pathToSrc + "/pom.xml cobertura:cobertura -Dcobertura.report.format=xml";
            }

        }
        try {
            p = Runtime.getRuntime().exec(commandToExecute);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            flag=true;
            while ((sCurrentLine = stdInput.readLine()) != null) {
                LOGGER.info("stdInput" + sCurrentLine);
            }
            while ((sCurrentLine = stdError.readLine()) != null) {
                LOGGER.info("stdError" + sCurrentLine);
                flag=false;
            }

        } catch (IOException e) {
            LOGGER.error(e);
        }
        return flag;

    }

    /**
     * This method is used for read the contain from other and copy into the other and throws IOException went something happened wrong.
     *
     * @param in the in
     * @param out the out
     * @throws IOException Signals that an I/O exception has occurred.
     */
    static void copy(InputStream in, OutputStream out) throws IOException {
        while (true) {
            int c = in.read();
            if (c == -1)
                break;
            out.write((char) c);
        }
    }
}