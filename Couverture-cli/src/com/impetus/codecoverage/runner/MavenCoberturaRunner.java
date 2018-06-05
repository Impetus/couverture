package com.impetus.codecoverage.runner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.log4j.Logger;


/**
 *  The Class MavenCoberturaRunner is used for run the command and copy one file into other file.
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
     *FUNCTIONS: This method return true or false and take a three parameters mvnDir,pathToSrc and codeCoverageTool and cover the current line of code and clean the mvn package and also covered the current line code .
     *INPUTS:
     * @param mvnDir the mvn dir
     * @param pathToSrc the path to src
     * @param codeCoverageTool the code coverage tool
     *OUTPUTS: 
     * @return true, if successful
     * @throws InterruptedException 
     */
    public static boolean run(String mvnDir, String pathToSrc, String codeCoverageTool) throws InterruptedException {
        Process p ;
        boolean flag = false;
        String sCurrentLine ;
        String commandToExecute = "";
        String line;
        String dir ;
        if (mvnDir != null && pathToSrc != null) {
            dir = mvnDir + "/bin/mvn.bat ";
            LOGGER.info("mvnDir=" + dir);

            if(codeCoverageTool.equals(JACOCO)){
                commandToExecute = dir + "mvn clean package";
            } else {
                commandToExecute = dir + " -f " + pathToSrc + "/pom.xml cobertura:cobertura -D cobertura.report.format=xml";
            }

        }
        try { 
              p = Runtime.getRuntime().exec(commandToExecute);
             int i= p.waitFor();
            System.out.println("exit value "+i);
             BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            line=stdInput.readLine();   
            if (line != null) {
                flag = true;
            }
            while ((sCurrentLine = stdError.readLine()) != null) {
                LOGGER.info("stdError" + sCurrentLine);
            }

        } catch (IOException e) {
            LOGGER.error(e);
        }
        return flag;

    }

    /**
     *FUNCTIONS: This method is used for read the contain from other and copy into the other and throws IOException went something happened wrong.
     *INPUTS:
     * @param in the in
     * @param out the out
     * @throws IOException Signals that an I/O exception has occurred.
     *OUTPUTS:
     * void
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