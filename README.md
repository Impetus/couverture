> ## Overview-

> USSCC Tool calculates code coverage specific to the user stories we deliver.
> This gives a clear picture in terms of percentage of code coverage for each user story.
> And hence coverage result does not get diluted with usually low legacy code coverage or other user stories coverage.   

> ## The tool is available in two forms:-

> A. USSCC Sonar Plug-in. (folder name: usscc-sonar-pluginTool)

> B. USSCC Command line Tool (folder name: code-coverag-tool)


A. USSCC Sonar Plug-in.



> ## To run this tool with your Projects sonar setup, please follow the below steps:-
> 1.   Make sure your project implement Jacoco or Cobertura and create Jacoco.xml or cobertura’s coverage.xml
>      (To know how to do this, please refer to documents in USSCC_DOCUMENT/implement coverage xml)
> 2.   build your project to generate jacoco.xml or coverage.xml for your project
> 3.   Tool is available as a Plugin to SonarQube. To generate sonar report with USSC tool follow these steps :-
>       * **Place USSC-plugin jar at sonarqube-X.X.X/extensions/plugins**
>       * **Start SonarQube server**
>       * **Add it as widget to dashboard in sonar. At start of sprint, configure plugin with list of user stories in sprint scope.**
>       * **Generate sonar report for your project with follwing command :**
                     **mvn sonar:sonar**
> 4.   :+1: See Coverage Result on SonarQube Dashboard.     





B. USSCC Command line Tool

## Overview-

>USSCC Tool is a command line version of USSCC Sonar Plug-in.


>## Setup and follow below some supported step:


> For Jacoco:


> 1.   Before running this tool, make sure coverage xmls are present in the target folders like service/target/site/jacoco.xml If not, 

> 2.   To run the project, go to the com.codecoverage.runner package and run to the RunCodeCoverage java class.

> 3.   Enter information as prompted

> 4. :+1:See Coverage Result on console.