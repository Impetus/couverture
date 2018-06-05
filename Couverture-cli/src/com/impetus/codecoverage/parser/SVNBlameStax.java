package com.impetus.codecoverage.parser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;

import com.impetus.codecoverage.model.BlameEntry;


/**
 * The Class SVNBlameStax to read Blame information of a file to determine what all lines changed in a give revision.
 */
public class SVNBlameStax {
	
	/** The line num. */
	static int lineNum = 0;

	/** The Constant LOGGER. */
	private final static Logger LOGGER = Logger.getLogger(SVNBlameStax.class);

	/**
	 * This method Persist blame information of a file for svn and this method take's three parameter and return a list.
	 * This method read a file with blame information in xml format and create a list of BlameEntry object
	 * Then, iterate the list of BlameEntry to check if there is a blame entry of given revision and file name, 
	 * to capture the line number of this blame entry, 
	 * as it is the line number in file for which changes are done for given revision/user story
	 *
	 * @param file the file
	 * @param revision1 the revision 1
	 * @param fileName1 the file name 1
	 * @return the list
	 */
	static List<String> persistBlame(String file, String revision1,
			String fileName1) {

		List<BlameEntry> blameEntries = new ArrayList();
		BlameEntry blameEntry = null;
		String tagContent ;
		FileReader fr = null;

		String fileName = new String();

		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			fr = new FileReader(file);
			XMLStreamReader reader = factory.createXMLStreamReader(fr);

			String lineNum = new String();
			
			String revision = "";
			while (reader.hasNext()) {
				int event = reader.next();

				switch (event) {
				case XMLStreamConstants.START_ELEMENT:
					if ("entry".equals(reader.getLocalName())) {
						lineNum = reader.getAttributeValue(0);
					}
					if ("target".equals(reader.getLocalName())) {
						fileName = reader.getAttributeValue(0);
					}

					if ("commit".equals(reader.getLocalName())) {
						revision = reader.getAttributeValue(0);
						blameEntry = new BlameEntry();
						blameEntry.setLine(lineNum);

						blameEntry.setRevision(revision);
						blameEntry.setFileName(fileName);
					}

					break;

				case XMLStreamConstants.CHARACTERS:
					tagContent = reader.getText().trim();
					break;

				case XMLStreamConstants.END_ELEMENT:
					switch (reader.getLocalName()) {
					case "entry":
						blameEntries.add(blameEntry);
						break;
					default:
					}
					break;
				default:
				}

			}
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
		ArrayList<String> lines = new ArrayList();

		for (BlameEntry blamed : blameEntries) {

			if (revision1 != null
					&& revision1.equalsIgnoreCase(blamed.getRevision())
					&& blamed.getFileName().contains(
							fileName1.substring(fileName1.lastIndexOf("/") + 1,
									fileName1.length()))) {
				lines.add(blamed.getLine());
			}

		}

		return lines;
	}
}