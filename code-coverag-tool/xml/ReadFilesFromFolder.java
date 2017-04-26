import java.io.File;


public class ReadFilesFromFolder {
  public static File folder = new File("./xml/");
  static String tempFileName = "";

  public static void main(String[] args) {

    System.out.println("Reading files under the folder "+ folder.getAbsolutePath());
    listFilesForFolder(folder);
  }

  public static void listFilesForFolder(final File folder) {

    for (final File fileEntry : folder.listFiles()) {
      if (fileEntry.isDirectory()) {
    	  //System.out.println("New folder Name:\n \n"+fileEntry);
    	  			listFilesForFolder(fileEntry);
      } else {
        if (fileEntry.isFile()) {
          tempFileName = folder.getPath()+"\\"+fileEntry.getName();
         
          //  System.out.println("File= " + folder.getPath()+ "\\" + fileEntry.getName());
            
            
            if(".\\xml\\blame".equalsIgnoreCase(folder.getPath())){
            	//System.out.println("HSTC:  "+folder.getPath());
            	BlameStax.persistBlame(tempFileName);
            }
            
          //  BlameStax.persistBalm();
        }

      }
    }
  }
}