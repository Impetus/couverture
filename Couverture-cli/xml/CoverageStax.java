import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class CoverageStax {

	public static void main(String[] args) throws XMLStreamException, SQLException {
		List<Coverage> coverageList = new ArrayList<Coverage>();
		Coverage currCoverage = null;
		String tagContent = null;
		FileReader fr = null;
		Coverage coverageInn= new Coverage();
		Connection conn = coverageInn.connect();
		PreparedStatement pst = null;
		
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			fr = new FileReader(
					"./xml/coverage/coverage.xml");
			XMLStreamReader reader = factory.createXMLStreamReader(fr);

			String currClass = new String();
			String currMethod = new String();
			pst=conn.prepareStatement("insert into coverage_details (CLASS_NAME,METHOD,LINE_NUMBER,HITS) values (?,?,?,?)");

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
				default:
				/*
				 * case XMLStreamConstants.START_DOCUMENT: coverageList = new
				 * ArrayList<Coverage>(); break;
				 */
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fr != null)
					fr.close();
			} catch (Exception p) {

			}
		}

		// Print the employee list populated from XML
		for (Coverage coverage : coverageList) {
			//System.out.println(coverage);
			try {
				pst.setString(1, coverage.getClassName());
				pst.setString(2, coverage.getMethod());
				pst.setString(3, coverage.getLineNumber());
				pst.setInt(4, coverage.getHits());
				pst.execute();
				
				/*System.out.println(coverage.getClassName());
				//System.out.println(coverage.getClass());
				System.out.println(coverage.getHits());
				System.out.println(coverage.getLineNumber());
				System.out.println(coverage.getMethod());*/
				
				//System.out.println("Inserted....");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}

class Coverage {
	private String className, method,lineNumber;
	private int  hits;

	@Override
	public String toString() {
		return "Coverage [className=" + className + ", method=" + method
				+ ", lineNumber=" + lineNumber + ", hits=" + hits + "]";
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(String lineNumber) {
		this.lineNumber = lineNumber;
	}

	public int getHits() {
		return hits;
	}

	public void setHits(int hits) {
		this.hits = hits;
	}

	public Connection connect() {
		String url = "jdbc:mysql://localhost:3306/coverage";
		String driver = "com.mysql.jdbc.Driver";
		String userName = "root";
		String password = "root";
		Connection conn = null;
		try {
			Class.forName(driver).newInstance();
			conn = DriverManager.getConnection(url, userName, password);
			//System.out.println("connected....");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}
}