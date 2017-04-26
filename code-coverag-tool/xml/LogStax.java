import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class LogStax {

	static final String insertQuery = "insert into svn_log (AUTHOR,REVISION,MESSAGE,PATH) values (?,?,?,?)";

	public static void main(String[] args) throws XMLStreamException,
			IOException {
	}

	public static void saveLogInDB(String fileName) {

		List<LogEntry> logEntries = new ArrayList<LogEntry>();
		LogEntry currLogEntry = null;
		String tagContent = null;
		FileReader fr = null;
		LogEntry svnLogEntry = new LogEntry();
		Connection conn = svnLogEntry.connect();
		PreparedStatement pst = null;

		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			fr = new FileReader(fileName);
			XMLStreamReader reader = factory.createXMLStreamReader(fr);

			String currRevision = new String();
			String currAuthor = new String();
			String currMessage = new String();

			pst = conn.prepareStatement(insertQuery);

			ArrayList paths = new ArrayList<String>();
			while (reader.hasNext()) {
				int event = reader.next();

				switch (event) {
				case XMLStreamConstants.START_ELEMENT:
					if ("logentry".equals(reader.getLocalName())) {
						currRevision = reader.getAttributeValue(0);
					}

					if ("paths".equals(reader.getLocalName())) {
						currLogEntry = new LogEntry();
						currLogEntry.setAuthor(currAuthor);
						currLogEntry.setRevision(currRevision);

						paths = new ArrayList<String>();
					}
					break;

				case XMLStreamConstants.CHARACTERS:
					tagContent = reader.getText().trim();
					break;

				case XMLStreamConstants.END_ELEMENT:
					switch (reader.getLocalName()) {
					case "logentry":
						currRevision = "";
						currAuthor = "";
						currMessage = "";
						logEntries.add(currLogEntry);
						break;
					case "path":
						if (paths != null) {
							paths.add(tagContent);
						}
						break;
					case "author":
						currAuthor = tagContent;
						break;
					case "msg":
						currMessage = tagContent;
						currLogEntry.setMsg(currMessage);
						break;
					case "paths":
						currLogEntry.setPaths(paths);
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

		for (LogEntry logEntry : logEntries) {

			try {
				pst.setString(1, logEntry.getAuthor());
				pst.setString(2, logEntry.getRevision());
				pst.setString(3, logEntry.getMsg());
				pst.setString(4, logEntry.getPaths().toString());

				pst.execute();

			} catch (Exception e) {

				e.printStackTrace();
			}

		}

	}
}

class LogEntry {
	private String author, revision, msg;
	List paths;

	@Override
	public String toString() {
		return "LogEntry [author=" + author + ", revision=" + revision
				+ ", msg=" + msg + ", paths=" + paths + "]";
	}

	public String getAuthor() {
		return author;
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

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public List getPaths() {
		return paths;
	}

	public void setPaths(List paths) {
		this.paths = paths;
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}

}