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

import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;

import com.codecoverage.parser.ClearcaseCodeCoverage;
import com.codecoverage.parser.CodeCoverage;
import com.codecoverage.parser.SVNCodeCoverage;
import com.codecoverage.parser.SimpleGITCodeCoverage;

/*
 * 
 * Author: Saurabh Juneja
 * Step0: For each user story, do step 1 and 2.
 * Step1: call getFileLineNoMap to get map of (file, list of lines changed for user story)
 * Step2: find for each file the percent of lines covered.
 */

/**
 * The Class RunCodeCoverage is the main class this is used for starting the execution for getting code coverage specific to user story.
 *
 * @author saurabh.juneja
 */

public class RunCodeCoverage {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(RunCodeCoverage.class);
    
    /** The Constant COBERTURA. */
    private static final String COBERTURA = "Cobertura";
    
    /** The Constant JACOCO. */
    private static final String JACOCO = "Jacoco";
    
    /** The Constant ONE. */
    private static final String ONE = "1";
    
    /** The Constant TWO. */
    private static final String TWO = "2";
    

	/** The user story. */
    static String userStory = "";
	
	/** The path to src. */
	static String pathToSrc = "";
	
	/** The coverage for. */
	static String coverageFor = null;
	
	/** The mvn dir. */
	static String mvnDir = "";
	
	/** The coverage tool option. */
	static String coverageToolOption=null;
	
	/** The file list. */
	private static ArrayList<String> fileList = null;
	
	/**
	 * The main method is a entry point method, this method is used for starting the execution.
	 * It prompts for User STory Numbers, Project root directory Path with those user story checked in
	 * It auto detects if the target project is using git or svn
	 *
	 * @param args the arguments
	 * @throws XMLStreamException the XML stream exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * 
	 * Take User story list, target Project location as input
	 * Step1: Determine if target project is using git or svn
	 * Step2: Based on code repository find filenames and there line number changed for list of user stories entered
	 * Step3: Read coverage xmls and know which lines in which class are junit covered
	 * Step4: Based on Step 2 and 3 knowledge, computes the code coverage percent of each user story
	 * Step5: Write Not covered lines of code for each class for each user story to a CSV file
	 * 
	 */
	public static void main(String[] args) throws XMLStreamException,
			IOException {

		LOGGER.debug("USSCC: User story specific code coverage tool");
		
		System.out.println("***** USSCC: User story specific code coverage tool *****");

		Scanner scan = new Scanner(System.in);

		System.out
				.println("Enter User Story Number, if multiple userStories then please seperate them by comma(,)");
		userStory = scan.next();
		LOGGER.debug("userStory:" + userStory);

		System.out.println("Enter SRC/Code Location");
		pathToSrc = scan.next();
		LOGGER.debug("pathToSrc:" + pathToSrc);

		File f1 = new File(pathToSrc + "/.git");
		if (f1.exists()) {
			coverageFor = "git";
		} else {
			f1 = new File(pathToSrc + "/.svn");
			if (f1.exists()) {
				coverageFor = "svn";
			}
		}

		LOGGER.info("Coverage Project Type =" + coverageFor);
		if (coverageFor != null) {
			System.out.println("repository name auto detected as :: "
					+ coverageFor);
		} else {
			System.out
					.println("Enter the repository name like git, svn or clearcase");
			coverageFor = scan.next();
			LOGGER.debug("coverageFor:" + coverageFor);
		}

		String pathToCoverageXML = readCoverageXML(pathToSrc);

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

		String coveragePercent ;
		Map<String, String> resultCoverageMap = new HashMap();

		while (userStories.length > count) {
			if(coverage!=null){
				Map<String, ArrayList<String>> fileLineNoMap = coverage.getFileLineNoMap(
						pathToSrc, userStories[count]);
				LOGGER.debug("fileLineNoMap" + fileLineNoMap);
				System.out.println("Going to get " + coverageFor
						+ " checked in files coverage for : \""
						+ userStories[count] + "\"");
				coveragePercent = ComputeSimpleUSCoverage.getOverallCoverage(fileLineNoMap,
						userStories[count], pathToCoverageXML);
				resultCoverageMap.put(userStories[count], coveragePercent);
				count++;
			}
		}
		ComputeSimpleUSCoverage.writeToCSV(ComputeSimpleUSCoverage.getNotCoveredCodeMap());
	}
	
	/**
	 * 
	 * Read coverage report XML from covertura or jacoco and return a path(s) to xml file as string.
	 *
	 * @param pathToProject the path to project
	 * @return a string of comma separated list of path to each coverage xmls within the project root. 
	 *  
	 * 
	 */
	public static String readCoverageXML(String pathToProject){
		String pathToCoverageXML = pathToProject;
		Scanner scan = new Scanner(System.in);
		if (pathToCoverageXML != null) {
			System.out.println("Enter code coverage tool(Jococo/cobertura):");
			System.out.println("enter 1 for Cobertura");
			System.out.println("enter 2 for Jococo");
			coverageToolOption = scan.next();
			LOGGER.debug("coverageTool Option:" + coverageToolOption);
			boolean flag = false;
			if(ONE.equals(coverageToolOption)){
				if (pathToCoverageXML.endsWith("/") || pathToCoverageXML.endsWith("\\")) {
					pathToCoverageXML = pathToCoverageXML
							.concat("target/site/cobertura/coverage.xml");
				} else {
					pathToCoverageXML = pathToCoverageXML
							.concat("/target/site/cobertura/coverage.xml");
				}

				File f = new File(pathToCoverageXML);

				if (f.exists()) {
					System.out
							.println("There is already a coverage.xml that exists, so using this existing coverage.xml");

				} else {
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
					System.out
							.println("There is no such coverage.xml that exist in directory, so creating a new one.");
					mvnDir = System.getenv("MAVEN_HOME");
					if (mvnDir == null || "".equalsIgnoreCase(mvnDir)) {
						mvnDir = System.getenv("M2_HOME");
						if (mvnDir == null || "".equalsIgnoreCase(mvnDir)) {
							System.out
									.println("Please provide the maven home location on your local machine(i.e. /Users/saurabhjuneja/Documents/apache-maven-3.2.5)");
							mvnDir = scan.next();
						}
					}
					flag = MavenCoberturaRunner.run(mvnDir, pathToSrc, COBERTURA);
				}

				if (flag) {
					System.out
							.println("Cobertura plugin is executed and coverage file is created in "
									+ pathToSrc + "/target/site/cobertura/coverage.xml");
				}


			}else if(TWO.equals(coverageToolOption)){
				if (pathToCoverageXML.endsWith("/") || pathToCoverageXML.endsWith("\\")) {
					pathToCoverageXML = pathToCoverageXML
							.concat("target/site/jacoco/jacoco.xml");
				} else {
					pathToCoverageXML = pathToCoverageXML
							.concat("/target/site/jacoco/jacoco.xml");
				}
				File f = new File(pathToCoverageXML);
				if (f.exists()) {
					System.out
							.println("There is already a jacoco.xml that exists, so using this existing jacoco.xml");

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
					System.out
							.println("There is no such jacoco.xml that exist in directory, so creating a new one.");
					mvnDir = System.getenv("MAVEN_HOME");
					if (mvnDir == null || "".equalsIgnoreCase(mvnDir)) {
						mvnDir = System.getenv("M2_HOME");
						if (mvnDir == null || "".equalsIgnoreCase(mvnDir)) {
							System.out
									.println("Please provide the maven home location on your local machine(i.e. /Users/saurabhjuneja/Documents/apache-maven-3.2.5)");
							mvnDir = scan.next();
						}
					}
					flag = MavenCoberturaRunner.run(mvnDir, pathToSrc, JACOCO);
				}

				if (flag) {
					System.out
							.println("Jacoco plugin is executed and coverage file is created in "
									+ pathToSrc + "/target/site/jacoco-ut/jacoco.xml");
				}
			}



		}
		return pathToCoverageXML;
	}

	/**
	 * The Class Finder is a utility class to find a pattern of file in directory and sub directories
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
			
			//.newWatchService()
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
				System.out.println(file.toString());
				fileList.add(file.toString());
			}
		}

		// Prints the total number of
		/**
		 * This method print the total number of matches line.
		 */
		// matches to standard out.
		void done() {
			System.out.println("Matched: " + numMatches);
		}

		// Invoke the pattern matching
		/* 
		 * this method take a path and basicFileAttributes parameter and this method invoke the pattern matching for each file and return a filevisitResult.
		 * @see java.nio.file.SimpleFileVisitor#visitFile(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
		 */
		// method on each file.
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
			find(file);
			return FileVisitResult.CONTINUE;
		}

		// Invoke the pattern matching
		
		/* 
		 * this method take a path and basicFileAttributes parameter and this method invoke the pattern matching for each directory and return a filevisitResult.
		 * @see java.nio.file.SimpleFileVisitor#preVisitDirectory(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
		 */
		// method on each directory.
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
			find(dir);
			return FileVisitResult.CONTINUE;
		}

		/* 
		 *  this method take a path and IOException parameter and this method print the exception to related with IOException and return the fileVisitResult.
		 * @see java.nio.file.SimpleFileVisitor#visitFileFailed(java.lang.Object, java.io.IOException)
		 */
		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) {
			System.err.println(exc);
			return FileVisitResult.CONTINUE;
		}
	}
}