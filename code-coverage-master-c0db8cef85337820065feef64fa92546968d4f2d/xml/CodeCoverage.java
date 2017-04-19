import java.sql.Connection;
import java.sql.SQLException;




public interface CodeCoverage {

    void createLog(String pathToSrc);
    void createBlame(String pathToSrc,String userStory,Connection conn) throws SQLException;
 }
