package com.codecoverage.runner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;

import com.codecoverage.model.Coverage;


/**
 *  The Class ComputeSimpleUSCoverage is used for  runcodeCoverage this class compute the  specific line for codeCoverage.
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
	static Map<String, Set<String>> uSSNotCoveredCode = new HashMap();
        
        /** The not coverage list. */
        static List<String> notCoverageList;

	/**
	 * Instantiates a new compute simple US coverage.
	 */
	private ComputeSimpleUSCoverage() {

	}

	/**
	 *  This method return the final coverage and print the total number of line and total hits and get all the overallCoverage and take a three parameter and throws IOException if something wrong.
	 *
	 * @param m the m
	 * @param userStory the user story
	 * @param pathToCoverageXML the path to coverage XML
	 * @return the overall coverage
	 * @throws IOException Signals that an I/O exception has occurred.
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

		for (Map.Entry<String, ArrayList<String>> entry : m.entrySet()) {
			String fileName = entry.getKey();
			Set<String> notCoveredList = new HashSet();
			List<String> lineNoList = entry.getValue();
			for (String lineNo : lineNoList) {
				int hits = getCoverage(fileName, lineNo, pathToCoverageXML);
				if (!(hits == -1 || hits >0)) {
					notCoveredList.add(lineNo);
				}
			}
			fileName = trimFileName(fileName);
			uSSNotCoveredCode.put(userStory + "," + fileName, notCoveredList);
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
	 * This method return a string value and trim filename for overall coverage code.
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
	 * this method return a value which is not  covered in code.
	 *
	 * @return the not covered code map
	 */
	public static Map<String, Set<String>> getNotCoveredCodeMap() {

		return uSSNotCoveredCode;
	}

	/**
	 * this method is use to Write the numbers of line in to CSV file this method show the user story , class name and show the line number those not covered and print the comment when CSV file was created successfully
	 *
	 * @param mapToWrite the map to write
	 * @param sonarHomepath the sonar homepath
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
	 * This method return integer value and take a three parameters fileName ,hit lineNumber and pathToCoverageXML and show the covered line and get the coverage.
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
	 * This method return true if lineNumber is equals to the getlineNumber Checks if is line covered.
	 *
	 * @param lineNumber the line number
	 * @param coverage the coverage
	 * @return the boolean
	 */
	private static Boolean isLineCovered(String lineNumber, Coverage coverage) {
		return lineNumber.equals(coverage.getLineNumber());
	}

	/**
	 * This method return the the list of coverage and take a one parameters and Gets the coverage list.
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
