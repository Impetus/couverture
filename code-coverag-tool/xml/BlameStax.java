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

public class BlameStax {
	static int lineNum = 0;
	static int totalCount = 0;
	static BlameEntry svnLogEntry = new BlameEntry();
	static Connection conn = svnLogEntry.connect();
	static final String insertQuery="insert into file_blame (line_number,author,revision,date,file_name) values (?,?,?,?,?)";
	
	public static void main(String[] args) throws XMLStreamException,
			SQLException {

		persistBlame("");

	}

	static void persistBlame(String file) {

		List<BlameEntry> blameEntries = new ArrayList<BlameEntry>();
		BlameEntry blameEntry = null;
		BlameStax blamestax = new BlameStax();
		String tagContent = null;
		FileReader fr = null;

		PreparedStatement pst = null;
		String fileName = new String();

		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			fr = new FileReader(file);
			XMLStreamReader reader = factory.createXMLStreamReader(fr);

			String currRevision = new String();
			String lineNum = new String();
			String currAuthor = new String();
			String currMessage = new String();

			pst = conn
					.prepareStatement(insertQuery);

			ArrayList paths = new ArrayList<String>();

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
						String revision = reader.getAttributeValue(0);
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
					case "author":
						currAuthor = tagContent;
						blameEntry.setAuthor(currAuthor);

						break;
					case "date":
						currMessage = tagContent;
						blameEntry.setDate(currMessage);
						break;
					default:
					}
					break;
				default:
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

		for (BlameEntry blamed : blameEntries) {

			totalCount = 0;
			lineNum = 0;

			if (blamed != null) {
				try {
					pst.setString(1, blamed.getLine());

					pst.setString(2, blamed.getAuthor());
					pst.setString(3, blamed.getRevision());
					pst.setString(4, blamed.getDate());
					pst.setString(5, blamed.getFileName());

					pst.execute();
				} catch (SQLException e) {
					System.out
							.println("Exception while inserting the blame records into DB");
					e.printStackTrace();
				}
			}
		}
	}

}

class BlameEntry {
	private String author, revision, fileName, line, date;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	List paths;

	@Override
	public String toString() {
		return "BlameEntry [author=" + author + ", FileName " + fileName
				+ ", revision=" + revision + ", line = " + line + ", date = "
				+ date + "]";
	}

	public String getAuthor() {
		return author;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
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
			System.out.println("connected....");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}

}