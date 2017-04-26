package com.codecoverage.runner;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.codecoverage.parser.ClearcaseCodeCoverage;
import com.codecoverage.parser.CodeCoverage;
import com.codecoverage.parser.SVNCodeCoverage;
import com.codecoverage.parser.SimpleGITCodeCoverage;
import com.codecoverage.runner.RunCodeCoverage.Finder;


// TODO: Auto-generated Javadoc
/**
 * The Class RunCodeCoverage is used for compute the code for coverage.
 */
public class RunCodeCoverage {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(RunCodeCoverage.class);
	
	/** The Constant COBERTURA. */
	private static final String COBERTURA = "Cobertura";
	
	/** The Constant JACOCO. */
	private static final String JACOCO = "Jacoco";
	
	/** The not covered user stories. */
	private static StringBuilder notCoveredUserStories = new StringBuilder();
    
    /** The Constant COVERAGEXML. */
    private static final String COVERAGEXML="target/site/cobertura/coverage.xml";
    
    /** The Constant JACOCOXML. */
    private static final String JACOCOXML="target/site/jacoco/jacoco.xml";
    
    /** The Constant SEPARATOR. */
    private static final String SEPARATOR="/";
	
	/** The mvn dir. */
	static String mvnDir = "";
	
	/** The coverage percent. */
	static String coveragePercent = "0.0";
	
	/** The file list. */
	private static ArrayList<String> fileList = null;

	
	
	 /**
 	 * Instantiates a new run code coverage.
 	 */
 	private RunCodeCoverage()
	 {
		 
	 }
	
	/**
	 * Compute code coverage.
	 *
	 * @param coverageFor the coverage for
	 * @param userStory the user story
	 * @param pathToSrc the path to src
	 * @param os the os
	 * @return the map
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static Map<String, String> computeCodeCoverage(String coverageFor,
			String userStory, String pathToSrc, String os) throws IOException {

		LOGGER.debug("start of program");
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		String pathToCoverageXML = pathToSrc;
		if (pathToCoverageXML != null) {
			if (pathToCoverageXML.endsWith(SEPARATOR) || pathToCoverageXML.endsWith("\\")) {

					pathToCoverageXML = pathToCoverageXML.concat(COVERAGEXML);
				
			} else {

					pathToCoverageXML = pathToCoverageXML.concat(SEPARATOR+COVERAGEXML);
				
			}
		}

		File f = new File(pathToCoverageXML);
		LOGGER.info("pathToProject" + f.getAbsolutePath());
		boolean flag;
		if (f.exists()) {
			LOGGER.info("There is already a coverage.xml that exists, so using this existing coverage.xml");
		}else {
			// multi module project
	        Path startingDir = Paths.get(pathToSrc);
	        Finder finder = new Finder("coverage.xml");
	        fileList = new ArrayList<>();
			try {
				Files.walkFileTree(startingDir, finder);
				pathToCoverageXML="";
			} catch (Exception e) {
				LOGGER.error(e);
			}
	        for (String s : fileList)
	        {
	        	pathToCoverageXML += s + ",";
	        }
		} 
		if(pathToCoverageXML!=null && pathToCoverageXML.length()<=0) {
			pathToCoverageXML = pathToSrc;
			if (pathToCoverageXML != null) {
				if (pathToCoverageXML.endsWith(SEPARATOR) || pathToCoverageXML.endsWith("\\")) {
					
						pathToCoverageXML = pathToCoverageXML.concat(JACOCOXML);
					
				} else {
					
						pathToCoverageXML = pathToCoverageXML.concat(SEPARATOR+JACOCOXML);
					
					
				}
			}
			f = new File(pathToCoverageXML);
			LOGGER.info("pathToProject" + f.getAbsolutePath());
			if (f.exists()) {
				LOGGER.info("There is already a jacoco.xml that exists, so using this existing jacoco.xml");

			} else {
				// multi module project
		        Path startingDir = Paths.get(pathToSrc);
		        Finder finder = new Finder("jacoco.xml");
		        fileList = new ArrayList<>();
				try {
					Files.walkFileTree(startingDir, finder);
					pathToCoverageXML="";
				} catch (Exception e) {
					LOGGER.error(e);
				}
		        for (String s : fileList)
		        {
		        	pathToCoverageXML += s + ",";
		        }
			} 
			if(pathToCoverageXML!=null && pathToCoverageXML.length()<=0) {
				LOGGER.info("There is no such jacoco.xml that exist in directory, so creating a new one.");
				mvnDir = System.getenv("MAVEN_HOME");
				if (mvnDir == null || "".equalsIgnoreCase(mvnDir)) {
					mvnDir = System.getenv("M2_HOME");
					if (mvnDir == null || "".equalsIgnoreCase(mvnDir)) {
					LOGGER.info("Please provide the maven home location on your local machine(i.e. /Users/saurabhjuneja/Documents/apache-maven-3.2.5)");
					mvnDir = scan.next();
					}
				}
				flag = MavenCoberturaRunner.run(mvnDir, pathToSrc, os,
						JACOCO);
				if (flag) {
					LOGGER.info("Jacoco plugin is executed and coverage file is created in "
							+ pathToSrc + "/target/site/jacoco-ut/jacoco.xml");
				} else {
					LOGGER.info("There is no such coverage.xml that exist in directory, so creating a new one.");
					mvnDir = System.getenv("MAVEN_HOME");
					if (mvnDir == null || "".equalsIgnoreCase(mvnDir)) {
						mvnDir = System.getenv("M2_HOME");
						if (mvnDir == null || "".equalsIgnoreCase(mvnDir)) {
							LOGGER.info("Please provide the maven home location on your local machine(i.e. /Users/saurabhjuneja/Documents/apache-maven-3.2.5)");
							mvnDir = scan.next();
						}
					}
					flag = MavenCoberturaRunner.run(mvnDir, pathToSrc,
							os, COBERTURA);
					if (flag) {
						LOGGER.info("Cobertura plugin is executed and coverage file is created in "
								+ pathToSrc
								+ "/target/site/cobertura/coverage.xml");
					}
				}
			}

		}

		String[] userStories = { userStory };
		int count = 0;
		if (userStory != null && userStory.contains(",")) {
			userStories = userStory.split(",");
		}

		while (userStories.length > count) {
			userStories[count] = userStories[count].trim();
			count++;

		}

		CodeCoverage coverage = null;

		if ("svn".equalsIgnoreCase(coverageFor)) {
			coverage = new SVNCodeCoverage();
		} else if ("git".equalsIgnoreCase(coverageFor)) {
			coverage = new SimpleGITCodeCoverage();
		} else if ("clearcase".equalsIgnoreCase(coverageFor)) {
			coverage = new ClearcaseCodeCoverage();
		}
		count = 0;

		LOGGER.debug("Going to get " + coverageFor
				+ " Files and Lines to get Coverage for User-Story: "
				+ userStory + "\n\n");

		Map<String, String> resultCoverageMap = new HashMap();
		while (userStories.length > count) {
			if(coverage!=null)
			{
			Map<String, ArrayList<String>> m = coverage.getFileLineNoMap(
					pathToSrc, userStories[count]);
			LOGGER.info("FileLineNumberMap:" + m);
			LOGGER.info("Going to get " + coverageFor
					+ " checked in files coverage for : \""
					+ userStories[count] + "\"");
			
			if (m.size()<=0) {
				notCoveredUserStories.append(userStories[count]+",");
			}else{
				coveragePercent = ComputeSimpleUSCoverage.getOverallCoverage(m,
						userStories[count], pathToCoverageXML);
			}
				resultCoverageMap.put(userStories[count], coveragePercent);
				coveragePercent ="0.0";
			count++;

		}
		}

		return resultCoverageMap;
	}
	
	/**
	 * This method will  Gets the not covered user stories.
	 *
	 * @return the not covered user stories
	 */
	public static String getNotCoveredUserStories(){
		return notCoveredUserStories.toString().replaceAll(" ,$", "").trim();
		
		
	}
	
	/**
	 * The Class Finder.
	 */
	public static class Finder extends SimpleFileVisitor<Path> {

		/** The matcher. */
		private final PathMatcher matcher;
		
		/** The num matches. */
		private int numMatches = 0;

		/**
		 * Instantiates a new finder.
		 *
		 * @param pattern the pattern
		 */
		Finder(String pattern) {
			matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
		}

		// Compares the glob pattern against
		/**
		 * This method take a path of file and find file name or directory name.
		 *
		 * @param file the file
		 */
		// the file or directory name.
		void find(Path file) {
			Path name = file.getFileName();
			if (name != null && matcher.matches(name)) {
				numMatches++;
				LOGGER.info(file.toString());
							
				fileList.add(file.toString());
			}
		}

		// Prints the total number of
		/**
		 * This method print the total number of matches line.
		 */
		// matches to standard out.
		void done() {
			LOGGER.info("Matched: " + numMatches);
		}

		// Invoke the pattern matching
		/* (non-Javadoc)
		 * @see java.nio.file.SimpleFileVisitor#visitFile(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
		 */
		// method on each file.
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
			find(file);
			return FileVisitResult.CONTINUE;
		}

		// Invoke the pattern matching
		/* (non-Javadoc)
		 * @see java.nio.file.SimpleFileVisitor#preVisitDirectory(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
		 */
		// method on each directory.
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
			find(dir);
			return FileVisitResult.CONTINUE;
		}

		/* (non-Javadoc)
		 * @see java.nio.file.SimpleFileVisitor#visitFileFailed(java.lang.Object, java.io.IOException)
		 */
		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) {
			LOGGER.error(exc);
			return FileVisitResult.CONTINUE;
		}
	}
}