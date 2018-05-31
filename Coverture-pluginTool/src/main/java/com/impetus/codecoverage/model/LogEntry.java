package com.impetus.codecoverage.model;

import java.util.List;

/**
 * The Class LogEntry is used for SVN to represent a SVN log entry.
 */
public class LogEntry {
    
    /** The author. */
    private String author;
    
    /** The revision. */
    private String revision;
    
    /** The msg. */
    private String msg;
    
    
    /** The paths. */
    @SuppressWarnings("rawtypes")
    List paths;

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "LogEntry [author=" + author + ", revision=" + revision + ", msg=" + msg + ", paths=" + paths + "]";
    }

    /**
     *This method will Gets the author and return a author  as type of String..
     *
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * This method will Sets the author and it will take a one parameter as type of String.
     *
     * @param author the new author
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     *This method will Gets the revision and return a value as type of String.
     *
     * @return the revision
     */
    public String getRevision() {
        return revision;
    }

    /**
     * This method will Sets the revision and it will take a one parameter as type of String.
     *
     * @param revision the new revision
     */
    public void setRevision(String revision) {
        this.revision = revision;
    }

    /**
     * This method will Gets the msg and return a value as type of String.
     *
     * @return the msg
     */
    public String getMsg() {
        return msg;
    }

    /**
     * This method will Sets the msg and it will take a one parameter as type of String.
     *
     * @param msg the new msg
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * This method will Gets the paths and return a value as type of List.
     *
     * @return the paths
     */
    @SuppressWarnings("rawtypes")
    public List getPaths() {
        return paths;
    }

    /**
     *This method will Sets the paths and it will take a one parameter as type of List.
     *
     * @param paths the new paths
     */
    @SuppressWarnings("rawtypes")
    public void setPaths(List paths) {
        this.paths = paths;
    }

}