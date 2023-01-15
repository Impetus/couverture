## Overview-

Couverture calculates code coverage specific to the user stories we deliver.
This gives a clear picture in terms of percentage of code coverage for each user story.
And hence coverage result does not get diluted with usually low legacy code coverage or other user stories coverage.   

## Couverture is available in two options:-

* Sonar Plug-in. (folder name: Coverture-pluginTool)
* Command line Tool (folder name: code-coverag-tool)

## **Couverture Plug-in:-** 
To run this tool with your Projects sonar setup, please follow the below steps:-
1.   Make sure your project implement Jacoco or Cobertura and create Jacoco.xml or cobertura’s coverage.xml
  (To know how to do this, please refer to documents in Coverture_DOCUMENT/implement coverage xml)
2.   build your project to generate jacoco.xml or coverage.xml for your project
3.   Tool is available as a Plugin to SonarQube. To generate sonar report with USSC tool follow these steps :-
   * **Place Coverture-plugin jar at sonarqube-X.X.X/extensions/plugins**
   * **Start SonarQube server**
   * **Add it as widget to dashboard in sonar. At start of sprint, configure plugin with list of user stories in sprint scope.**
   * **Generate sonar report for your project with follwing command :**
				 **mvn sonar:sonar**
4.   :+1: See Coverage Result on SonarQube Dashboard.     

## **Couverture Command line Tool**
Couverture Command Line Tool is a command line version of Couverture Sonar Plug-in.

## Setup and follow below some supported step:
1.   Before running this tool, make sure coverage xmls are present in the target folders like service/target/site/jacoco.xml If not, 

2.   To run the project, go to the com.codecoverage.runner package and run to the RunCodeCoverage java class.

3.   Enter information as prompted

4. :+1:See Coverage Result on console.

For more details please go through presentation in Coverture_DOCUMENT\presentations
