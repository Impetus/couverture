#  **There are two project here. We are giving brief introduction about both project,which is given below.**

> A . USSCC Sonar Plug-in. (folder name: usscc-sonar-pluginTool)

> B . USSCC Tool (folder name: code-coverag-tool. A command line application)


A. USSCC Sonar Plug-in.

> ## Overview-

> USSCC Sonar Plug-in Tool calculates code coverage specific to the user stories we deliver and show it over sonar dashboard.
> This gives a clear picture in terms of percentage of code coverage for each user story.
> And hence coverage result does not get diluted with usually low legacy code coverage or other user stories coverage.   

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





B. CodeCoverage Tool

## Overview-

>USSCC Tool calculates code coverage specific to the user stories we deliver.
>This gives a clear picture in terms of percentage of code coverage for each user story.
>And hence coverage result does not get diluted with usually low legacy code coverage or other user stories coverage.


>## Setup and follow below some supported step:


> For Jacoco:


> 1.Before running this tool, make sure jacoco.xml is present at the following location in your module: service/target/site/jacoco.xml If not, follow below steps :

> a. Make the necessary additions in pom.xml at service/pom.xml (you can find the changes in additions folder in tool)

> 2.CodeCoverageTool is a maven project, do a mvn install and then run it as java project.

> 3.Go to the com.codecoverage.runner package and run to the RunCodeCoverage java class.

>You have get such type of output

>a . Enter User Story Number, if multiple user Stories then please separate them       by comma (,)

>Note: Please Enter usscc number like US-1234. If you have two user story number like US-1235, Us-1234

>b.  After entered you have to give a project location  

>Like Enter SRC/Code Location

>Note: entered your location like D:\svnCheckout

> c.  After entered it will ask

> d.  Enter code coverage tool(Jococo/cobertura):

> enter 1 for Cobertura

> enter 2 for Jococo

> Note:  if you want to code coverage cobertura press one or if you want to code coverage jococo press two.

> 4.If we are getting such type of error when we run mvn sonar: sonar command. then we need to install collabnet subversion software. There is link we can download this software https://www.collab.net/downloads and set the path variable C:\Program Files\CollabNet\Subversion Client

> 5. :+1:See Coverage Result on console.