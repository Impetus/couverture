import java.sql.Connection;
import java.sql.SQLException;


public class ClearcaseCodeCoverage implements CodeCoverage{

	@Override
	public void createLog(String pathToSrc) {
		
		String fileName="";
		ccSaveLogInDb(fileName);
		
	}
	
	@Override
	public void createBlame(String pathToSrc, String userStory, Connection conn)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}
	
	public void ccSaveLogInDb(String fileName)
	{
		
	}
	
}
