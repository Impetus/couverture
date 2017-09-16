package com.impetus.codecoverage.runner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;

import com.impetus.codecoverage.model.Coverage;



/**
 * The Class ComputeSimpleUSCoverage is used by runcodeCoverage to compute the code coverage percentage specific to user story.
 */
public class ComputeSimpleUSCoverage {

	/** The query. */
	static String query = "select distinct (REVISION) from coverage.svn_log where message like ? ";
	
	/** The total line count. */
	static float totalHitsCount = 0, totalLineCount = 0;
	
	/** The coverage list. */
	static List<Coverage> coverageList = new ArrayList();
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(ComputeSimpleUSCoverage.class);
	
	/** The Constant LINE. */
	private static final String LINE = "line";
	
	/** The Constant METHOD. */
	private static final String METHOD = "method";
	
	/** The Constant CLASS. */
	private static final String CLASS = "class";
	
	/** The u SS not covered code. */
	static Map<String, String> uSSNotCoveredCode = new HashMap();
        
        /** The not coverage list. */
        static List<String> notCoverageList;

	/**
	 * Instantiates a new compute simple US coverage.
	 */
	private ComputeSimpleUSCoverage() {

	}

	/**
     * This method return the final coverage and print the total number of line and total hits and get all the overallCoverage and take a three parameter and throws IOException if something wrong.
     *
     * @param fileLineNoMap the contains file as key and line number changed for the given user story in that file as value in the map
     * @param userStory is the user story number for which we are going to calculate coverage percentage
     * @param pathToCoverageXML is a comma separated list of file path to coverage.xml
     * @return the overall coverage percentage 
     * @throws IOException Signals that an I/O exception has occurred.
     * 
     * Step1: Parse all coverage xml in project to learn for each line in project, if it is covered by Junit or not
     * Step2: Iterate over file name line no map passed as parameter and count covered lines and total lines changed 
     * to finally compute coverage percentage for given user story
     * 
     */
	static String getOverallCoverage(Map<String, ArrayList<String>> m,
			String userStory, String pathToCoverageXML)
					throws IOException {
		try{
			if (coverageList.isEmpty()) {
				if (pathToCoverageXML != null && !pathToCoverageXML.contains("coverage.xml")) {
					coverageList = getCoverageListJaCoCo(pathToCoverageXML);
				} else {
					LOGGER.info("calling getCoverageList");
					coverageList = getCoverageList(pathToCoverageXML);
				}
			}
		} catch (Exception e) {
			LOGGER.error(e);
		}

		Double coverage ;
		
		int lengthCounter = 0;

		for (Map.Entry<String, ArrayList<String>> entry : m.entrySet()) {
			String fileName = entry.getKey();
			Set<String> notCoveredList = new HashSet<String>();
			SortedSet<Integer> sortedRangeList = new TreeSet<Integer>();
			List<String> lineNoList = entry.getValue();
			for (String lineNo : lineNoList) {
				int hits = getCoverage(fileName, lineNo, pathToCoverageXML);
				if (!(hits == -1 || hits >0)) {
					//notCoveredList.add(lineNo);
					sortedRangeList.add(Integer.parseInt(lineNo));
					lengthCounter = lengthCounter + lineNo.length() + 2; // adding 2 for space and comma 
				}
			}
			lengthCounter = lengthCounter + 4; // =[];
			fileName = trimFileName(fileName);
			
			Iterator<Integer> itr = sortedRangeList.iterator();
			String start = "";
			int lastNumber = -20;
			Integer currentNumber = null;
			boolean series = false;
			boolean first = true;
			while (itr.hasNext()) {
				currentNumber = itr.next();
				if (first) {
					lastNumber = currentNumber;
				}
				if (currentNumber == lastNumber + 1) {
					if (!series) {
						series = true;
						start = start + lastNumber;
					}
				} else {
					if (series) {
						series = false;
						start = start + " to " + lastNumber + ",";
					} else {
						if (!first)
							start = start + lastNumber + ",";
					}
				}
				lastNumber = currentNumber;
				first = false;
			}
			if (series) {
				series = false;
				start = start + " to " + lastNumber;
			} else {
				start = start + lastNumber;
			}
			LOGGER.info("start : "+userStory + "," + fileName +" : "+  start);
			if (lengthCounter+(userStory + "," + fileName).length()<3900)
				uSSNotCoveredCode.put(userStory + "," + fileName, start);
			else
			{
				LOGGER.info("Total Line Count : " +totalLineCount);
				LOGGER.info("lengthCounter : " +lengthCounter);
				break;
			}
				
		}
			
		try {
			coverage = (double) (totalHitsCount / totalLineCount) * 100;
		} catch (Exception e) {
			totalLineCount = 1;
			coverage = (double) (totalHitsCount / totalLineCount) * 100;
			LOGGER.error(e);
		}
		String finalCov = new DecimalFormat("##.#").format(coverage);
		if (coverage.isNaN()) {
			finalCov = "0.0";
		}
		LOGGER.info("Total Line Count : " + totalLineCount);
		LOGGER.info("Total Hits Count : " + totalHitsCount);
		LOGGER.info("Overall Coverage for User Story : " + userStory
				+ " = " + finalCov + " %\n");
		totalHitsCount = 0;
		totalLineCount = 0;
		return finalCov;
	}
	
	/**
     *This method return a string value and trim filename for overall coverage code.
     *
     * @param fileName the file name
     * @return the string
     */
	private static String trimFileName(String fileName){
    	fileName = fileName.replace("\\", ".").replaceAll("/", ".");
    	String fileNameArray[] = fileName.split("\\.");
    	return fileNameArray[fileNameArray.length-2];
    }

	/**
	 * This method return a value which is not  covered in code.
	 *
	 * @return the not covered code map
	 */
	public static Map<String, String> getNotCoveredCodeMap() {

		return uSSNotCoveredCode;
	}

	/**
	 * This method is use to Write the numbers of line in to CSV file this method show the user story , class name and show the line number those not covered and print the comment when CSV file was created successfully
	 *
	 * @param mapToWrite the map to write
	 */
	
	public static void writeToCSV(Map<String, Set<String>> mapToWrite, String sonarHomepath) {

		String key;
		Set<String> value;
		FileWriter fileWriter = null;
		// Delimiter used in CSV file

        final String commaDelimiter = ",";

        final String newLineSeparator = "\n";

		// CSV file header
        final String fileHeader = "User Story, Class Name,Line Numbers not covered";

		try {
			if ("".equals(sonarHomepath) || null == sonarHomepath) {
				fileWriter = new FileWriter("./coverage_result.csv");
			}else {
				try {
					fileWriter = new FileWriter(sonarHomepath+"/web/coverage_result.csv");
				}catch (Exception e){
					LOGGER.info("Unable to write in Sonar Home, so writing in project home directory");
					LOGGER.info(e);
					fileWriter = new FileWriter("./coverage_result.csv");
				}
			}


			// Write the CSV file header

            fileWriter.append(fileHeader);
			// Add a new line separator after the header

            fileWriter.append(newLineSeparator);

			for (Entry<String, Set<String>> entry : mapToWrite.entrySet()) {
				key = entry.getKey();
				value = entry.getValue();
				if (value != null && !value.isEmpty()) {
					fileWriter.append(key + commaDelimiter);

					for (String temp : value) {
						fileWriter.append(temp + " ");
					}

					fileWriter.append(newLineSeparator);
				}
			}
        	LOGGER.info("CSV file was created successfully !!!");
			
		} catch (IOException e) {
            LOGGER.error(e.getMessage()+e);
            LOGGER.error(e.getStackTrace());
		} finally {

			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
                LOGGER.error("Error while flushing/closing fileWriter !!!");
                LOGGER.error(e.getStackTrace());
                LOGGER.error(e.getMessage()+e);
			}

		}
	}



	/**
	 *This method return integer value and take a three parameters fileName ,hit lineNumber and pathToCoverageXML and show the covered line and get the coverage.
	 *
	 * @param fileName the file name
	 * @param lineNumber the line number
	 * @param pathToCoverageXML the path to coverage XML
	 * @return the coverage
	 */
	static int getCoverage(String fileName, String lineNumber, String pathToCoverageXML) {

		int hits = -1;

		String fName = fileName.replace("src\\main\\java\\", "")
				.replace("src/main/java/", "").replace("\\", ".")
				.replaceAll("/", ".");
		
		for (Coverage coverage : coverageList) {
			try {
				if (fName.contains(coverage.getClassName()
						+ (pathToCoverageXML.contains("cobertura") ? ".java" : ""))
						&& isLineCovered(lineNumber, coverage)) {
					hits = coverage.getHits();
					if (hits > 0) {
						totalHitsCount++;
						totalLineCount++;
						return hits;
					}
					totalLineCount++;
					return hits;
				}
			} catch (Exception e) {
				LOGGER.error(e);
			}
		}
		return hits;
	}

	/**
     *This method return true if lineNumber is equals to the getlineNumber Checks if is line covered.
     *
     * @param lineNumber the line number
     * @param coverage the coverage
     * @return the boolean
     */
	private static Boolean isLineCovered(String lineNumber, Coverage coverage) {
		return lineNumber.equals(coverage.getLineNumber());
	}

	/**
     *this method return the the list of coverage and take a one parameters and Gets the coverage list.
     *
     * @param pathToCoverageXML the path to coverage XML
     * @return the coverage list
     */
	static List<Coverage> getCoverageList(String pathToCoverageXML) {

		List<Coverage> coverageList = new ArrayList();
		Coverage currCoverage = null;
		FileReader fr = null;

		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
            LOGGER.info("pathToProject in getCoverageList :: " + pathToCoverageXML);
            String coveragexml[] = pathToCoverageXML.split(",");
			for (int i = 0; i <= coveragexml.length - 1; i++) {
				fr = new FileReader(coveragexml[i]);
				XMLStreamReader reader = factory.createXMLStreamReader(fr);

				String currClass = new String();
				String currMethod = new String();

				while (reader.hasNext()) {
					int event = -1;
					try {
						event = reader.next();
					} catch (com.ctc.wstx.exc.WstxParsingException e) {
						LOGGER.debug("Skipping DTD event exception. " + "[" + e.getMessage() + "]", e);
						continue;
					} catch (XMLStreamException e) {
						LOGGER.debug("Skipping DTD event exception. " + "[" + e.getMessage() + "]", e);
						continue;
					}

					switch (event) {
					case XMLStreamConstants.START_ELEMENT:
						if (CLASS.equals(reader.getLocalName())) {
							currClass = reader.getAttributeValue(0);
						}
						if (METHOD.equals(reader.getLocalName())) {
							currMethod = reader.getAttributeValue(0);
						}
						if (LINE.equals(reader.getLocalName())) {
							currCoverage = new Coverage();
							currCoverage.setClassName(currClass);
							currCoverage.setMethod(currMethod);
							currCoverage.setLineNumber(reader.getAttributeValue(0));
							currCoverage.setHits(Integer.parseInt(reader.getAttributeValue(1)));
							coverageList.add(currCoverage);
						}
						break;

					case XMLStreamConstants.CHARACTERS:
						break;

					case XMLStreamConstants.END_ELEMENT:
						if (CLASS.equals(reader.getLocalName()))
							currClass = "";
						else if (METHOD.equals(reader.getLocalName()))
							currMethod = "";
						break;

					default:
					}
				}
			}
			return coverageList;
		} catch (Exception e) {
			LOGGER.error(e);
		} finally {
			try {
				if (fr != null)
					fr.close();
			} catch (Exception p) {
				LOGGER.error(p);
			}
		}
		return coverageList;
	}

	/**
     *This method the return the list of coverage and take a  one parameters and Gets the coverage list jacoco.
     *
     * @param pathToCoverageXML the path to coverage XML
     * @return the coverage list ja co co
     */
	static List<Coverage> getCoverageListJaCoCo(String pathToCoverageXML) {

		List<Coverage> coverageList = new ArrayList();
		Coverage currCoverage = null;
		FileReader fr = null;

		try {
			LOGGER.debug("pathToProject in getCoverageListJaCoCo :: " + pathToCoverageXML);
            XMLInputFactory factory = XMLInputFactory.newInstance();
            String jacocoxml[] = pathToCoverageXML.split(",");
			for (int i = 0; i <= jacocoxml.length - 1; i++) {

				fr = new FileReader(jacocoxml[i]);
				XMLStreamReader reader = factory.createXMLStreamReader(fr);
				String currClass = new String();

				while (reader.hasNext()) {

					int event = -1;

					// skip event which throws report.dtd not found exception.
					try {
						event = reader.next();
					} catch (com.ctc.wstx.exc.WstxParsingException e) {
						LOGGER.error("Skipping DTD event exception. " + "[" + e.getMessage() + "]", e);
						continue;
					} catch (XMLStreamException e) {
						LOGGER.error("Skipping DTD event exception. " + "[" + e.getMessage() + "]", e);
						continue;
					}

					switch (event) {
					case XMLStreamConstants.START_ELEMENT:
						if ("sourcefile".equals(reader.getLocalName())) {
							currClass = reader.getAttributeValue(0);
						}
						if (LINE.equals(reader.getLocalName())) {
							currCoverage = new Coverage();
							currCoverage.setClassName(currClass);
							currCoverage.setLineNumber(reader.getAttributeValue(0));
							currCoverage.setHits(Integer.parseInt(reader.getAttributeValue(2)));
							coverageList.add(currCoverage);
						}
						break;

					case XMLStreamConstants.CHARACTERS:
						 reader.getText().trim();
						break;

					case XMLStreamConstants.END_ELEMENT:
						if (CLASS.equals(reader.getLocalName()))
							currClass = "";
						break;

					default:
					}
				}
			}
			return coverageList;
		} catch (Exception e) {
			LOGGER.error("Excption occured in getCoverageListJaCoCo()"
					+ " method. [" + e.getMessage() + "]", e);
		} finally {
			try {
				if (fr != null)
					fr.close();
			} catch (Exception p) {
				LOGGER.error(p);
			}
		}
		return coverageList;
	}
}
