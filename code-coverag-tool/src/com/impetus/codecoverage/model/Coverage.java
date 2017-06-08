package com.impetus.codecoverage.model;

/**
 * The Class Coverage is used in computeSimpleUSCoverage for user specific code coverage.
 *
 * @author saurabh.juneja
 */
public class Coverage {
    
    /** The class name. */
    private String className;
    
    /** The method. */
    private String method;
    
    /** The line number. */
    private String lineNumber;
    
    /** The hits. */
    private int hits;

    /* 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Coverage [className=" + className + ", method=" + method + ", lineNumber=" + lineNumber + ", hits=" + hits + "]";
    }
   
     /**
      * This method will Gets the class name and return a class name as type of String.
      *
      * @return the class name
      */
     public String getClassName() {
        return className;
    }

     /**
      * This method will Sets the class name and it will take a one parameter as type of String.
      *
      * @param className the new class name
      */
     public void setClassName(String className) {
        this.className = className;
    }

     /**
      * This method will Gets the method and return a value  as type of String.
      *
      * @return the method
      */
     public String getMethod() {
        return method;
    }
     
     /**
      * This method will Sets the method and it will take a one parameter as type of String.
      *
      * @param method the new method
      */
     public void setMethod(String method) {
        this.method = method;
    }

     /**
      * This method will Gets the line number and return a line number as type of String.
      *
      * @return the line number
      */
     public String getLineNumber() {
        return lineNumber;
    }
     
     /**
      * This method will Sets the line number and it will take a one parameter as type of String.
      *
      * @param lineNumber the new line number
      */
     public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

     /**
      * This method will Gets the hits and return a hits  as type of int.
      *
      * @return the hits
      */
     public int getHits() {
        return hits;
    }

     /**
      * This method will Sets the hits and it will take a one parameter as type of int.
      *
      * @param hits the new hits
      */
     public void setHits(int hits) {
        this.hits = hits;
    }
}
