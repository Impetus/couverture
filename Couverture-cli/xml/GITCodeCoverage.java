import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;


public class GITCodeCoverage implements CodeCoverage{
	
	public static void main(String args[]){
		String pathToSrc="D:/code_coverage/";
		String commandToBeExecuted="git  --git-dir "+pathToSrc+".git log --pretty=format:\"\"<logentry revision=\"\"%h\"\">%n  <author>%an</author>%n  <date>%cd</date>%n  <msg>%s</msg>%n</entry>\"> manish.xml";
		
		System.out.println(commandToBeExecuted);
	}

	@Override
	public void createBlame(String pathToSrc, String userStory, Connection conn)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void createLog(String pathToSrc) {
		

		// TODO Auto-generated method stub
		String s = null, fileName = "./xml/log/gitLog.xml";
		if(pathToSrc!=null && !(pathToSrc.endsWith("/") || pathToSrc.endsWith("\\"))){
			pathToSrc=pathToSrc.concat("/");
		}
		String commandToBeExecuted="git  --git-dir "+pathToSrc+".git log --pretty=format:\"<logentry revision=\"\"%h\"\">%n  <author>%an</author>%n  <date>%cd</date>%n  <msg>%s</msg>%n</entry>\"> manish.xml";
		try {

			Process p = Runtime.getRuntime()
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
}
