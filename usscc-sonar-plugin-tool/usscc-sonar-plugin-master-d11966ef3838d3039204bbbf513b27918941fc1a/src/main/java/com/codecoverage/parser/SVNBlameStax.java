package com.codecoverage.parser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;

import com.codecoverage.model.BlameEntry;

/**
 * The Class SVNBlameStax to persist the Blame information of a file for SVN.
 */
public class SVNBlameStax {
	
	
	
	
	/** The line num. */
	static int lineNum = 0;

	/** The Constant logger. */
	final static Logger logger = Logger.getLogger(SVNBlameStax.class);

	
	
	     private SVNBlameStax()
	     {
	    	 
	     }
	
	
	/**
	 * This method Persist blame information of a file for svn and this  method  take's three parameter and return a list.
	 *
	 * @param file the file
	 * @param revision1 the revision 1
	 * @param fileName1 the file name 1
	 * @return the list
	 */
	static List<String> persistBlame(String file, String revision1,
			String fileName1) {

		List<BlameEntry> blameEntries = new ArrayList	();
		BlameEntry blameEntry = null;
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
					 reader.getText().trim();
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
			logger.error(e);
		} finally {
			try {
				if (fr != null)
					fr.close();
			} catch (Exception p) {
				logger.error(p);
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