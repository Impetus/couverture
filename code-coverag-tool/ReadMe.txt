Overview: 
         USSCC Tool calculates code coverage specific to the user stories we deliver.
         This gives a clear picture in terms of percentage of code coverage for each user story.
         And hence coverage result does not get diluted with usually low legacy code coverage or other user stories coverage.   

Setup and follow below some supported  step:

For Jacoco :
1) Before running this tool, make sure jacoco.xml is present at the following location in your module: service/target/site/jacoco.xml
	If not, follow below steps : 
	a. Make the necessary additions in pom.xml at service/pom.xml (you can find the changes in additions folder in tool)
	b. To generate jacoco.xml, run the below commands in your module(EDS,DF,etc):
		mvn clean compile -DskipTests=true
		mvn compiler:testCompile resources:testResources surefire:test org.jacoco:jacoco-maven-plugin:report
		
2) CodeCoverageTool is a maven project, do a mvn install and then run it as java project

3) If you are run code coverage tool project. This is a standalone project.in this project we have entry point class. This is called  RunCodeCoverage.java class.
   we will be following some steps.
       1.Go to the com. codecoverage.runner package and  run  to the RunCodeCoverage  java class.
         You have get such type of output
   Enter User Story Number, if multiple user Stories then please separate them       by comma (,)
        Note: Please Enter usscc number like US-1234. If you have two user story number like US-1235, Us-1234
       2. After entered you have to give a project location  
          Like Enter SRC/Code Location
          Note: entered your location like D:\svnCheckout
       3. After entered give your it will ask
         Enter code coverage tool(Jococo/cobertura):
          enter 1 for Cobertura
          enter 2 for Jococo
          Note: if you want  code coverage cobertura press one or if you want  code coverage jococo press two.

        4.If we are getting such type of error when we run mvn sonar: sonar command. then we need to install collabnet subversion software. There is link we can download this software 
                  https://www.collab.net/downloads
            and set the path variable C:\Program Files\CollabNet\Subversion Client


        5. See Coverage Result on console.


