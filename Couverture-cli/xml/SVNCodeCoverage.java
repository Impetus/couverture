import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;




public class SVNCodeCoverage implements CodeCoverage{
	
	String selectQuery = "select distinct(path) from svn_log where message like ?";
	
	@Override
	public void createLog(String pathToSrc) {
		// TODO Auto-generated method stub
		String s = null, fileName = "./xml/log/svnLog.xml";

		try {

			Process p = Runtime
					.getRuntime()
					.exec("svn log --xml -r BASE:HEAD --verbose "+pathToSrc);

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));

			File file = new File(fileName);
			FileOutputStream is = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(is);
			Writer w = new BufferedWriter(osw);
			while ((s = stdInput.readLine()) != null) {
				w.write(s + "\n");
			}
			w.close();
			LogStax.saveLogInDB(fileName);
			System.out.println("Logs Saved Successfully.....\n\n");
		} catch (IOException e) {
			System.out.println("exception happened - here's what I know: ");
			e.printStackTrace();
			System.exit(-1);
		}

	}
	
	@Override
	public void createBlame(String pathToSrc, String userStory, Connection conn) throws SQLException {
		String s = null;
		LogEntry logEntry = new LogEntry();
		PreparedStatement pst = null;
		ResultSet rs = null;
		pst = conn.prepareStatement(selectQuery);
		pst.setString(1, userStory+"%");
		rs = pst.executeQuery();
		String fileName = new String();
		String postFix = new String();
		String fileToDB = new String();
		while (rs.next()) {

			fileName = rs.getString("PATH").replace("[", "").replace("]", "");
			postFix = fileName.substring(fileName.lastIndexOf("/") + 1,
					fileName.indexOf("."));
			try {

				Process p = Runtime.getRuntime().exec(
						"svn blame --xml "+pathToSrc+""+ fileName);

				BufferedReader stdInput = new BufferedReader(
						new InputStreamReader(p.getInputStream()));

				BufferedReader stdError = new BufferedReader(
						new InputStreamReader(p.getErrorStream()));
				File file = new File("./xml/blame/svnBlame_" + postFix + ".xml");
				fileToDB = "./xml/blame/svnBlame_" + postFix + ".xml";
				FileOutputStream is = new FileOutputStream(file);
				OutputStreamWriter osw = new OutputStreamWriter(is);
				Writer w = new BufferedWriter(osw);
				while ((s = stdInput.readLine()) != null) {
					w.write(s + "\n");
				}
				w.close();
				BlameStax.persistBlame(fileToDB);
			} catch (IOException e) {
				System.out.println("exception happened - here's what I know: "
						+ e);
				e.printStackTrace();
				System.exit(-1);
			}

		}
		System.out.println("Logs Saved Successfully.....\n\n");
		
	}
	

}
