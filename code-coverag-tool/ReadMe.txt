CodeCoverageTool is a maven project
Setup
-----

For Jacoco :
1) Before running this tool, make sure jacoco.xml is present at the following location in your module: service/target/site/jacoco.xml
	If not, follow below steps : 
	a. Make the necessary additions in pom.xml at service/pom.xml (you can find the changes in additions folder in tool)
	b. To generate jacoco.xml, run the below commands in your module(EDS,DF,etc):
		mvn clean compile -DskipTests=true
		mvn compiler:testCompile resources:testResources surefire:test org.jacoco:jacoco-maven-plugin:report
		
2) CodeCoverageTool is a maven project, do a mvn install and then run it as java project

