package com.impetus.codecoverage.sensor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.PropertiesBuilder;
import org.sonar.api.resources.Project;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.impetus.codecoverage.plugin.CouvertureMetrics;
import com.impetus.codecoverage.plugin.CouverturePlugin;
import com.impetus.codecoverage.runner.ComputeSimpleUSCoverage;
import com.impetus.codecoverage.runner.RunCodeCoverage;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;

/**
 * The Class CouvertureSensor is used for get userstory code coverage jira.
 */
public class CouvertureSensor implements Sensor {

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory
			.getLogger(CouvertureSensor.class);

	/** The scan. */
	Scanner scan = new Scanner(System.in);
	
	/** The settings. */
	private static Settings settings;
	
	/** The path to src. */
	static String pathToSrc = "";
	
	/** The mvn dir. */
	static String mvnDir = "";
	
	/** The coverage. */
	PropertiesBuilder<String, String> coverage = new PropertiesBuilder();
	
	/** The not covered lines. */
	PropertiesBuilder<String, String> notCoveredLines = new PropertiesBuilder<>();
	
	/** The u SS coverage. */
	Map<String, String> uSSCoverage = new HashMap();

	/**
	 * Use of IoC to get Settings and FileSystem.
	 *
	 * @param settings the settings
	 */
	public CouvertureSensor(Settings settings) {
		this.settings = settings;
	}

	/* (non-Javadoc)
	 * @see org.sonar.api.batch.CheckProject#shouldExecuteOnProject(org.sonar.api.resources.Project)
	 */
	@Override
	public boolean shouldExecuteOnProject(Project project) {
		// This sensor is executed only when there are Java files
		return project.isRoot();
	}

	/* (non-Javadoc)
	 * @see org.sonar.api.batch.Sensor#analyse(org.sonar.api.resources.Project, org.sonar.api.batch.SensorContext)
	 */
	@Override
	public void analyse(Project project, SensorContext sensorContext) {
		// This sensor create a measure for metric MESSAGE on each Java file
		String getDirec = settings.getString("sonar.projectBaseDir");
		LOG.info("Comment below line and uncomment String coverageFor = null... in case there is already git in sonar and project is SVN");
		String coverageFor = settings.getString("sonar.scm.provider");
		String userStory = settings.getString(CouverturePlugin.USER_STORY);
		String rallyKey = settings.getString(CouverturePlugin.RALLY_KEY);
		String JIRAURL = settings.getString(CouverturePlugin.JIRA_URL);
		String releaseId = settings.getString(CouverturePlugin.RALLY_RELEASE);
		String JiraLogin = settings.getString(CouverturePlugin.JIRA_LOGIN);
		String JIRAPassword = settings.getString(CouverturePlugin.JIRA_PASSWORD);
		String JIRAJQL = settings.getString(CouverturePlugin.JIRA_JQL);
		String s3AccessKey=settings.getString(CouverturePlugin.S3_ACCESSKEY);
		String s3SecretKey=settings.getString(CouverturePlugin.S3_SECRETKEY);
		
		
		String os = settings.getString("sun.desktop");
		LOG.info(" sonar.working.directory	"+ settings.getString("sonar.working.directory"));
		LOG.info("Coverage for User Stroy = " + userStory);
		LOG.info("Coverage Project Type =" + coverageFor);
		LOG.info("Coverage Project Path =" + getDirec);
		LOG.info("os =" + os);
		LOG.info("sonar.path.data  =" + settings.getString("sonar.path.data"));
		    
		if (coverageFor == null) {
			File f = new File(getDirec + "/.git");
			if (f.exists()) {
				coverageFor = "git";
				LOG.info("Coverage Project Type came =" + coverageFor);
			} else {
				f = new File(getDirec + "/.svn");
				if (f.exists()) {
					coverageFor = "svn";
					LOG.info("Coverage Project Type came =" + coverageFor);
				}
			}
		}
		
		LOG.info("Coverage Project Type =" + coverageFor);
		if (coverageFor==null)
			coverageFor = "svn";
		String userStoryFromRally = "";
	
		if (rallyKey != null && rallyKey.length() > 0 && releaseId != null) {
			try {
				userStoryFromRally = getUserStroyFromRelease(rallyKey,
						releaseId);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (userStoryFromRally != null && userStoryFromRally.length() > 0
					&& releaseId != null) {
				addCoverage(getDirec, coverageFor, userStoryFromRally, os);
			}
		}

		if (rallyKey != null && rallyKey.length() > 0
				&& userStoryFromRally.length() <= 0) {
			try {
				userStoryFromRally = getUserStoryFromRally(rallyKey);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (userStoryFromRally != null && userStoryFromRally.length() > 0) {
				addCoverage(getDirec, coverageFor, userStoryFromRally, os);
			}
		}
		
		String userStoryFromJIRA = "";
		if ("".equals(userStoryFromRally)&& JiraLogin != null && JiraLogin.length()>0){
				userStoryFromJIRA = getUserStoryFromJIRA(JIRAURL, JiraLogin, JIRAPassword, JIRAJQL);
				if (userStoryFromJIRA!=null && userStoryFromJIRA.length()>0){
					addCoverage(getDirec, coverageFor, userStoryFromJIRA , os);
			}
		}
		
		if ("".equals(userStoryFromRally) && "".equals(userStoryFromJIRA) ){
			addCoverage(getDirec, coverageFor, userStory, os);
		}
					
		LOG.info("coverage.buildData() =" +coverage.buildData());
		sensorContext.saveMeasure(new Measure(
				CouvertureMetrics.RESULT_COVERAGE_MAP, coverage.buildData()));
		
		LOG.info("Not covered stories "+RunCodeCoverage.getNotCoveredUserStories());
		sensorContext.saveMeasure(new Measure(
				CouvertureMetrics.NOTCOVERED_USER_STORIES, RunCodeCoverage.getNotCoveredUserStories()));
		
		LOG.info("notCoveredLines.buildData() =" + notCoveredLines.buildData());
		sensorContext.saveMeasure(new Measure(
				CouvertureMetrics.NOTCOVERED_LINES, notCoveredLines.buildData()));
		
		String projectName = getProjectName(getDirec);
		
		LOG.info("Project name =" + projectName);
		
		if (projectName ==null || projectName.length() <= 0)
			projectName = UUID.randomUUID().toString();
        
        sensorContext.saveMeasure(new Measure(
                CouvertureMetrics.PROJECT_NAME, projectName));
    
   
        if (s3AccessKey == null ||s3AccessKey.isEmpty() )
        {
        	s3AccessKey="0";
        	sensorContext.saveMeasure(new Measure(
                		CouvertureMetrics.S3_ACCESKEY, s3AccessKey));
        	LOG.info("s3 access key$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ =" +s3AccessKey);
        }
        	else
        	{
        	sensorContext.saveMeasure(new Measure(
            		CouvertureMetrics.S3_ACCESKEY, s3AccessKey));
     	
        	LOG.info("s3 access key$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ =" +s3AccessKey);
        	}
        
		 
        if(s3AccessKey!=null && s3SecretKey!=null)
        {
           Map<String, String> mapToWrite = ComputeSimpleUSCoverage.getNotCoveredCodeMap();
          
                String sonarHomePath=null;
                ComputeSimpleUSCoverage.writeToCSV(mapToWrite, sonarHomePath, projectName,s3AccessKey,s3SecretKey);
        } 
                
    
	
	}

	
	
	
	
	/**
	 * This method is used for Gets the user story in Rally from release and return a value as a string.
	 *
	 * @param rallyKey the rally key
	 * @param releaseId the release Id
	 * @return the user story from release in rally
	 */
	private String getUserStroyFromRelease(String rallyKey, String releaseId)
			throws IOException {

		String Wspace_ref = null;
		RallyRestApi restApi = null;
		String USNumbers = "";
		try {
			restApi = new RallyRestApi(new URI("https://rally1.rallydev.com"),
					rallyKey);
		} catch (Exception e1) {
			LOG.error("error", e1);
		}
		// Read Subscriptions
		QueryRequest subscriptionRequest = new QueryRequest("Subscriptions");
		QueryResponse subscriptonResponse = restApi.query(subscriptionRequest);
		// Release
		QueryRequest defectCount = new QueryRequest("hierarchicalrequirement");
		// grab workspace collection
		QueryRequest workSpaceRequest = new QueryRequest(subscriptonResponse
				.getResults().get(0).getAsJsonObject()
				.getAsJsonObject("Workspaces"));
		workSpaceRequest.setFetch(new Fetch("Name", "_ref"));
		JsonArray myWorkSpaces = restApi.query(workSpaceRequest).getResults();
		// iterate through the workSpace to find the correct one
		String workSpaceName = "";
		for (int i = 0; i < myWorkSpaces.size(); i++) {
			workSpaceName = myWorkSpaces.get(i).getAsJsonObject().get("Name")
					.getAsString();
			Wspace_ref = myWorkSpaces.get(i).getAsJsonObject().get("_ref")
					.getAsString();
			LOG.info("work space name =" + workSpaceName);
			Wspace_ref = Wspace_ref.substring(Wspace_ref.lastIndexOf("0/") + 1);
			Wspace_ref = Wspace_ref.replace("\"", "");
			LOG.info("ready workspace =" + Wspace_ref);
			defectCount.setWorkspace(Wspace_ref);
			String[] release_key = { releaseId };
			int count = 0;
			if (releaseId != null && releaseId.contains(",")) {
				release_key = releaseId.split(",");
			}
			for (String release : release_key) {
				defectCount.setQueryFilter(new QueryFilter("Release.Name", "=",
						release));
				defectCount.setFetch(new Fetch(new String[] { "FormattedID" }));
				defectCount.setPageSize(1);
				defectCount.setLimit(500);
				int total = 0;
				QueryResponse userStoryNumbers = null;
				try {
					if (restApi != null) {
						userStoryNumbers = restApi.query(defectCount);
						LOG.info("defectCountResponse :" + userStoryNumbers);
					}
				} catch (IOException e) {
					e.printStackTrace();
					LOG.error("error", e);
				}
				if (userStoryNumbers != null) {
					total = userStoryNumbers.getTotalResultCount();
					LOG.info("total :" + total + "defectCountResponse : "
							+ userStoryNumbers.getResults());
				}
				if (total > 0 && userStoryNumbers != null) {
					for (JsonElement userStory : userStoryNumbers.getResults()) {
						JsonObject defect1 = userStory.getAsJsonObject();
						LOG.info("FormattedID: " + defect1.get("FormattedID"));
						if (defect1.get("FormattedID") != null
								&& defect1.get("FormattedID").toString()
										.length() > 0)
							USNumbers = USNumbers
									+ defect1
											.get("FormattedID")
											.toString()
											.substring(
													1,
													defect1.get("FormattedID")
															.toString()
															.length() - 1)
									+ ",";
					}
				}

			}

		}

		return USNumbers;
	}
	
	/**
	 * This method is used for  Gets the user story from JIRA and return a value as a string.
	 *
	 * @param JIRAURL the jiraurl
	 * @param JIRALogin the JIRA login
	 * @param JIRAPassword the JIRA password
	 * @param JIRAJQL the jirajql
	 * @return the user story from JIRA
	 */
	private String getUserStoryFromJIRA(String JIRAURL, String JIRALogin, String JIRAPassword, String JIRAJQL){
		String USNumbers = "";
		
		final AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
		URI jiraServerUri = null;
		try{
			jiraServerUri = new URI(JIRAURL);
		}catch(Exception u){
			LOG.error("error", u);
		}
		final JiraRestClient restClient = factory
				.createWithBasicHttpAuthentication(jiraServerUri, JIRALogin,
						JIRAPassword);
		final Promise<SearchResult> searchJqlPromise = restClient.getSearchClient().searchJql(JIRAJQL, 100, 0, null); 

		for (Issue issue : searchJqlPromise.claim().getIssues()) {
			LOG.debug(issue.getKey());
            USNumbers = USNumbers + issue.getKey() + ",";
        }
		
		return USNumbers;
	}
	
	
	
	/**
	 * This method is used for Gets the user story from Rall and return a value as a string.
	 *
	 * @param rallyKey the rally key
	 * @return the user story from rall
	 */
	private String getUserStoryFromRally(String rallyKey) throws IOException {
		RallyRestApi restApi = null;
		String USNumbers = "";
		String workspaceName = "";
		String Wspace_ref = null;
		try {
			restApi = new RallyRestApi(new URI("https://rally1.rallydev.com"),
					rallyKey);
		} catch (Exception e1) {
			LOG.error("error", e1);
		}
		QueryRequest subscriptionRequest = new QueryRequest("Subscriptions");
		QueryResponse subscriptionQueryResponse = restApi
				.query(subscriptionRequest);
		QueryRequest defectCount = new QueryRequest("hierarchicalrequirement");
		QueryRequest projectRequest = new QueryRequest("Project");
		defectCount.setQueryFilter(new QueryFilter("Iteration.StartDate", "<=","today").and(new QueryFilter("Iteration.EndDate", ">=", "today")));
		projectRequest.setFetch(new Fetch("Projects"));
		QueryRequest workspaceRequest = new QueryRequest(
				subscriptionQueryResponse.getResults().get(0).getAsJsonObject()
						.getAsJsonObject("Workspaces"));
		workspaceRequest.setFetch(new Fetch("Name", "_ref"));
		JsonArray myWorkspaces = restApi.query(workspaceRequest).getResults();
		for (int i = 0; i < myWorkspaces.size(); i++) {
			Wspace_ref = myWorkspaces.get(i).getAsJsonObject().get("_ref")
					.getAsString();
			Wspace_ref = Wspace_ref.substring(Wspace_ref.lastIndexOf("0/") + 1);
			Wspace_ref = Wspace_ref.replace("\"", "");
			LOG.info("ready workspace :" + Wspace_ref);
			projectRequest.setWorkspace(Wspace_ref);
			defectCount.setFetch(new Fetch(new String[] { "FormattedID" }));
			QueryResponse projectQueryResponse = restApi.query(projectRequest);
			defectCount.setPageSize(1);
			defectCount.setLimit(500);
			int count = projectQueryResponse.getResults().size();
			if (projectQueryResponse != null) {
				count = projectQueryResponse.getTotalResultCount();
			}
			if (count > 0 && projectQueryResponse != null) {
				for (JsonElement result : projectQueryResponse.getResults()) {
					JsonObject defect = result.getAsJsonObject();

					if (defect.get("_ref") != null
							&& defect.get("_ref").toString().length() > 0) {

						String ProjectRf = defect.get("_ref").toString();
						ProjectRf = ProjectRf.substring(ProjectRf
								.lastIndexOf("0/") + 1);
						ProjectRf = ProjectRf.replace("\"", "");
						defectCount.setProject(ProjectRf);
						LOG.info("ProjectRf :" + ProjectRf);
						int total = 0;
						QueryResponse defectCountResponse = null;
						try {
							if (restApi != null) {
								defectCountResponse = restApi
										.query(defectCount);
								LOG.info("defectCountResponse :"
										+ defectCountResponse);
							}
						} catch (IOException e) {
							e.printStackTrace();
							LOG.error("error", e);
						}
						if (defectCountResponse != null) {
							total = defectCountResponse.getTotalResultCount();
							LOG.info("total : " + total+ " defectCountResponse : "+ defectCountResponse.getResults());
							}
						if (total > 0 && defectCountResponse != null) {
							for (JsonElement result1 : defectCountResponse
									.getResults()) {
								JsonObject defect1 = result1.getAsJsonObject();
								LOG.info("FormattedID: "
										+ defect1.get("FormattedID"));
								if (defect1.get("FormattedID") != null
										&& defect1.get("FormattedID")
												.toString().length() > 0)
									USNumbers = USNumbers
											+ defect1
													.get("FormattedID")
													.toString()
													.substring(
															1,
															defect1.get(
																	"FormattedID")
																	.toString()
																	.length() - 1)
											+ ",";
							}
						}

					}

				}
			}
		}
		LOG.info("User specific code coverage:-- " + USNumbers);  
		return USNumbers;
	}

    /**
     * This method is used for check directory and userstory is not null or null and then add the coverage.
     *
     * @param getDirec the get direc
     * @param coverageFor the coverage for
     * @param userStory the user story
     * @param os the os
     */
    private void addCoverage(String getDirec, String coverageFor,
            String userStory, 
            String os) {
        boolean flag = false;
        if (getDirec != null && userStory != null && coverageFor != null){
        	flag = true;
        }
        try {
        	if (flag && (("git").equalsIgnoreCase(coverageFor)
        			|| ("svn").equalsIgnoreCase(coverageFor) || ("clearcase")
        			.equalsIgnoreCase(coverageFor))){
        		uSSCoverage = RunCodeCoverage.computeCodeCoverage(coverageFor,
        				userStory, getDirec, os);
        	}
        	checkAndAddCoverage(getDirec, coverageFor, userStory);
        } catch (IOException e) {
		    LOG.error("error", e);
		}
    }

    /**
     * This method will check which type of coverage we will   add means git or svn.
     *
     * @param getDirec the get direc
     * @param coverageFor the coverage for
     * @param userStory the user story
     */
    private void checkAndAddCoverage(String getDirec, String coverageFor,
            String userStory) {
			if (userStory == null)
				LOG.info("Coverage for User Stroy is null");
			if (getDirec == null)
				LOG.info("Coverage Project Path is null");
			if (!(("git").equalsIgnoreCase(coverageFor)
					|| ("svn").equalsIgnoreCase(coverageFor) || ("clearcase")
						.equalsIgnoreCase(coverageFor)))
				LOG.info("Coverage Project Type can be for git,svn or clearcase");
			for (Map.Entry<String, String> a : uSSCoverage.entrySet()) {
				coverage.add(a.getKey(), a.getValue());
			}
			for (Map.Entry<String, String> a :ComputeSimpleUSCoverage.getNotCoveredCodeMap().entrySet()) {
				notCoveredLines.add(a.getKey(), a.getValue());
				LOG.info("comptuer*#####################"+a.getKey(), a.getValue());

			}
			
	}
    
    /** This method will generate gets the name of the project from the baseDir path.
     * 
     * @param baseDir
     *            Base directory of the project.
     * @return name of  project*/
    public static String getProjectName(String baseDir) {
        String projectName = "";
        if (!"".equals(baseDir)) {
            int lastSlashIndex = baseDir.lastIndexOf('/');
            if (lastSlashIndex == -1) {
                lastSlashIndex = baseDir.lastIndexOf('\\');
            }
            projectName = baseDir.substring(lastSlashIndex + 1);
            if(projectName!=null){
                projectName = projectName.replace(".","");
            }
        }
        return projectName;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
