import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

public class GetOverallCoverage {

	static Connection conn = null;
	static PreparedStatement pst = null;
	static ResultSet rs = null;
	static LogEntry logEntry = new LogEntry();
	static String query = "select distinct (REVISION) from coverage.svn_log where message like ? ";
	static float totalHitsCount = 0, totalLineCount = 0;
	static List<Coverage> coverageList = new ArrayList<Coverage>();

	
	
	static void getOverallCoverage(String userStory)throws SQLException, IOException{
		coverageList = getCoverageList();
		LogEntry logEntry = new LogEntry();
		conn = logEntry.connect();

		pst = conn.prepareStatement(query);
		//pst.setString(1, "US-1001%");
		pst.setString(1, userStory+"%");
		rs = pst.executeQuery();
		String revision = new String();
		double coverage = 0f;

		while (rs.next()) {

			revision = rs.getString("REVISION");
			getBlame(revision);

		}
		try {
			coverage = (float) (totalHitsCount / totalLineCount) * 100;
		} catch (Exception e) {
			totalLineCount = 1;
			coverage = (totalHitsCount / totalLineCount) * 100;
			System.out.println(e);
		}
		String finalCov = new DecimalFormat("##.#").format(coverage);
		System.out.println("Overall Coverage : " + finalCov + " %");
	}

	static void getBlame(String revison) throws SQLException {

		conn = logEntry.connect();
		ResultSet rs = null;
		pst = conn
				.prepareStatement("select * from file_blame where revision=? ");
		String fileName = new String();
		String lineNumber = new String();
		pst.setString(1, revison);
		rs = pst.executeQuery();
		int count = 0;

		while (rs.next()) {
			fileName = rs.getString("file_name");
			lineNumber = rs.getString("line_number");
			if (fileName != null & lineNumber != null) {
				getCovergae(fileName, lineNumber);
			}

		}
		/*
		 * if (totalLineCount == 0) { totalLineCount = 1; }
		 */

	}

	static void getCovergae(String fileName, String lineNumber)
			throws SQLException {

		int hits=0;
		
		for (Coverage coverage : coverageList) {
			
			try {

				fileName = fileName.replace("src\\main\\java\\", "").replace(
						"\\", ".");
				if (fileName.contains(coverage.getClassName() + ".java")) {
					if (lineNumber.equals(coverage.getLineNumber())) {
						hits = coverage.getHits();
						if (hits > 0) {
							totalHitsCount++;
						}
						totalLineCount++;
					}

				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}

	}
	
	static List<Coverage> getCoverageList(){
		
		List<Coverage> coverageList = new ArrayList<Coverage>();
		Coverage currCoverage = null;
		String tagContent = null;
		FileReader fr = null;
		Coverage coverageInn = new Coverage();
		Connection conn = coverageInn.connect();
		PreparedStatement pst = null;
		

		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			fr = new FileReader("./xml/coverage/coverage.xml");
			XMLStreamReader reader = factory.createXMLStreamReader(fr);

			String currClass = new String();
			String currMethod = new String();

			pst = conn
					.prepareStatement("insert into coverage_details (CLASS_NAME,METHOD,LINE_NUMBER,HITS) values (?,?,?,?)");

			while (reader.hasNext()) {
				int event = reader.next();

				switch (event) {
				case XMLStreamConstants.START_ELEMENT:
					if ("class".equals(reader.getLocalName())) {
						currClass = reader.getAttributeValue(0);
					}
					if ("method".equals(reader.getLocalName())) {
						currMethod = reader.getAttributeValue(0);
					}
					if ("line".equals(reader.getLocalName())) {
						currCoverage = new Coverage();
						currCoverage.setClassName(currClass);
						currCoverage.setMethod(currMethod);
						currCoverage.setLineNumber(reader.getAttributeValue(0));
						currCoverage.setHits(Integer.parseInt(reader
								.getAttributeValue(1)));
						coverageList.add(currCoverage);
					}
					break;

				case XMLStreamConstants.CHARACTERS:
					tagContent = reader.getText().trim();
					break;

				case XMLStreamConstants.END_ELEMENT:
					switch (reader.getLocalName()) {
					case "class":
						currClass = "";
						break;
					case "method":
						currMethod = "";
						break;
					default:
					}
					break;

				case XMLStreamConstants.START_DOCUMENT:
					coverageList = new ArrayList<Coverage>();
					break;
				default:
				}

			}
			return coverageList;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fr != null)
					fr.close();
			} catch (Exception p) {

			}
		}
		return coverageList;
	}
}
