import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

import javax.xml.stream.XMLStreamException;

public class RunCodeCoverage {

	// static final String selectQuery =
	// "select distinct(path) from svn_log where message like ?";
	static final String deleteQuerySvnLog = "truncate table coverage.svn_log";
	static final String deleteQueryFileBlame = "truncate table coverage.file_blame";
	static java.sql.Connection conn = null;
	static PreparedStatement pst = null;
	static String userStory = "";
	static String pathToSrc = "";
	static String coverageFor = "";

	public static void main(String args[]) throws XMLStreamException,
			IOException, SQLException {
		conn = new LogEntry().connect();

		Scanner scan = new Scanner(System.in);
		System.out.println("Enter the repository name like git, svn or clearcase");
		coverageFor = scan.next();
		System.out.println("Enter User Story Number");
		userStory = scan.next();
		System.out.println("Enter SRC/Code Location");
		pathToSrc = scan.next();

		// deleting old table data
		emptyLogTable();
		emptyBlameTable();

		CodeCoverage coverage = null;

		if ("svn".equalsIgnoreCase(coverageFor)) {
			coverage = new SVNCodeCoverage();
		} else if ("git".equalsIgnoreCase(coverageFor)) {
			coverage = new GITCodeCoverage();
		} else if ("clearcase".equalsIgnoreCase(coverageFor)) {
			coverage = new ClearcaseCodeCoverage();
		}

		System.out.println("Going to get " + coverageFor
				+ " logs and persist into DB");
		coverage.createLog(pathToSrc);

		System.out.println("Going to get " + coverageFor
				+ " Blamed files and persist into DB");
		coverage.createBlame(pathToSrc, userStory, conn);

		System.out.println("Going to get Coverage");
		GetOverallCoverage.getOverallCoverage(userStory);

	}

	private static void emptyBlameTable() {

		try {

			pst = conn.prepareStatement(deleteQueryFileBlame);
			int number = pst.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void emptyLogTable() {

		try {
			pst = conn.prepareStatement(deleteQuerySvnLog);
			int number = pst.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * static void createSVNLog() {
	 * 
	 * String s = null, fileName = "./xml/log/svnLog.xml";
	 * 
	 * try {
	 * 
	 * Process p = Runtime .getRuntime()
	 * .exec("svn log --xml -r BASE:HEAD --verbose "+pathToSrc);
	 * 
	 * BufferedReader stdInput = new BufferedReader(new InputStreamReader(
	 * p.getInputStream()));
	 * 
	 * BufferedReader stdError = new BufferedReader(new InputStreamReader(
	 * p.getErrorStream()));
	 * 
	 * File file = new File(fileName); FileOutputStream is = new
	 * FileOutputStream(file); OutputStreamWriter osw = new
	 * OutputStreamWriter(is); Writer w = new BufferedWriter(osw); while ((s =
	 * stdInput.readLine()) != null) { w.write(s + "\n"); } w.close();
	 * LogStax.saveLogInDB(fileName);
	 * System.out.println("Logs Saved Successfully.....\n\n"); } catch
	 * (IOException e) {
	 * System.out.println("exception happened - here's what I know: ");
	 * e.printStackTrace(); System.exit(-1); }
	 * 
	 * }
	 */
	/*
	 * static void createSvnFileBlame() throws SQLException {
	 * 
	 * String s = null; LogEntry logEntry = new LogEntry(); PreparedStatement
	 * pst = null; ResultSet rs = null; pst =
	 * conn.prepareStatement(selectQuery); pst.setString(1, userStory+"%"); rs =
	 * pst.executeQuery(); String fileName = new String(); String postFix = new
	 * String(); String fileToDB = new String(); while (rs.next()) {
	 * 
	 * fileName = rs.getString("PATH").replace("[", "").replace("]", "");
	 * postFix = fileName.substring(fileName.lastIndexOf("/") + 1,
	 * fileName.indexOf(".")); try {
	 * 
	 * Process p = Runtime.getRuntime().exec( "svn blame --xml "+pathToSrc+""+
	 * fileName);
	 * 
	 * BufferedReader stdInput = new BufferedReader( new
	 * InputStreamReader(p.getInputStream()));
	 * 
	 * BufferedReader stdError = new BufferedReader( new
	 * InputStreamReader(p.getErrorStream())); File file = new
	 * File("./xml/blame/svnBlame_" + postFix + ".xml"); fileToDB =
	 * "./xml/blame/svnBlame_" + postFix + ".xml"; FileOutputStream is = new
	 * FileOutputStream(file); OutputStreamWriter osw = new
	 * OutputStreamWriter(is); Writer w = new BufferedWriter(osw); while ((s =
	 * stdInput.readLine()) != null) { w.write(s + "\n"); } w.close();
	 * BlameStax.persistBlame(fileToDB); } catch (IOException e) {
	 * System.out.println("exception happened - here's what I know: " + e);
	 * e.printStackTrace(); System.exit(-1); }
	 * 
	 * } System.out.println("Logs Saved Successfully.....\n\n");
	 * 
	 * }
	 */
	/*
	 * public static void listFilesForFolder(final File folder) {
	 * 
	 * for (final File fileEntry : folder.listFiles()) { if
	 * (fileEntry.isDirectory()) { //
	 * System.out.println("New folder Name:\n \n"+fileEntry);
	 * listFilesForFolder(fileEntry); } else { if (fileEntry.isFile()) {
	 * tempFileName = folder.getPath() + "\\" + fileEntry.getName();
	 * 
	 * // System.out.println("File= " + folder.getPath()+ "\\" + //
	 * fileEntry.getName());
	 * 
	 * if (".\\xml\\blame".equalsIgnoreCase(folder.getPath())) { //
	 * System.out.println("HSTC:  "+folder.getPath());
	 * BlameStax.persistBlame(tempFileName); }
	 * 
	 * // BlameStax.persistBalm(); }
	 * 
	 * } } }
	 */

}