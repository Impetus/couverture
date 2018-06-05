package com.impetus.codecoverage.model;


/**
 * 
 * The Class BlameEntry is used to persist the Blame information of a file for SVN
 *
 * @author saurabh.juneja
 */
public class BlameEntry {
	
	
	/** The author. */
	private String author;
	
	/** The revision. */
	private String  revision;
	
	/** The file name. */
	private String  fileName;
	
	/** The line. */
	private String line;
	
	/** The date. */
	private String date;
	
		/**
		 *This method will Gets the file name and return a file name as type of String.
		 *
		 * @return the file name
		 */
		public String getFileName() {
		return fileName;
	}
	
	/**
	 * This method will Sets the file name and it will take a one parameter as type of String.
	 *
	 * @param fileName the new file name
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/* 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BlameEntry [author=" + author + ", FileName " + fileName
				+ ", revision=" + revision + ", line = " + line + ", date = "
				+ date + "]";
	}

	
	
	/**
	 *This method will Gets the author and return a author as type of String.
	 *
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 *This method will Gets the date and return a date as type of String..
	 *
	 * @return the date
	 */
	public String getDate() {
		return date;
	}

	/**
	 *This method will Sets the date and it will take a one parameter as type of String.
	 *
	 * @param date the new date
	 */
	public void setDate(String date) {
		this.date = date;
	}

	/**
	 *This method will Sets the author and it will take a one parameter as type of String.
	 *
	 * @param author the new author
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	
	/**
	 *This method will Gets the revision and return a value  as type of String..
	 *
	 * @return the revision
	 */
	public String getRevision() {
		return revision;
	}

	/**
	 *This method will Sets the revision and it will take a one parameter as type of String.
	 *
	 * @param revision the new revision
	 */
	public void setRevision(String revision) {
		this.revision = revision;
	}

	/**
	 *This method will Gets the line and return line as type of String.
	 * @return the line
	 */
	public String getLine() {
		return line;
	}

	/**
	 *This method will Sets the line and it will take a one parameter as type of String.
	 *
	 * @param line the new line
	 */
	public void setLine(String line) {
		this.line = line;
	}

}