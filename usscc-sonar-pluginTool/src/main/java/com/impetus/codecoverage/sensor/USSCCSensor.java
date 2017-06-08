package com.impetus.codecoverage.sensor;

import java.io.File;
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

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.impetus.codecoverage.plugin.USSCCMetrics;
import com.impetus.codecoverage.plugin.USSCCPlugin;
import com.impetus.codecoverage.runner.ComputeSimpleUSCoverage;
import com.impetus.codecoverage.runner.RunCodeCoverage;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;

/**
 * The Class USSCCSensor is used for get userstory code coverage jira.
 */
public class USSCCSensor implements Sensor {

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory
			.getLogger(USSCCSensor.class);

	/** The scan. */
	Scanner scan = new Scanner(System.in);
	
	/** The settings. */
	private Settings settings;
	
	/** The path to src. */
	static String pathToSrc = "";
	
	/** The mvn dir. */
	static String mvnDir = "";
	
	/** The coverage. */
	PropertiesBuilder<String, String> coverage = new PropertiesBuilder();
	
	/** The not covered lines. */
	PropertiesBuilder<String, Set<String>> notCoveredLines = new PropertiesBuilder<>();
	
	/** The u SS coverage. */
	Map<String, String> uSSCoverage = new HashMap();

	/**
	 * Use of IoC to get Settings and FileSystem.
	 *
	 * @param settings the settings
	 */
	public USSCCSensor(Settings settings) {
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
		String userStory = settings.getString(USSCCPlugin.USER_STORY);
		String rallyKey = settings.getString(USSCCPlugin.RALLY_KEY);
		String JIRAURL = settings.getString(USSCCPlugin.JIRA_URL);
		String JiraLogin = settings.getString(USSCCPlugin.JIRA_LOGIN);
		String JIRAPassword = settings.getString(USSCCPlugin.JIRA_PASSWORD);
		String JIRAJQL = settings.getString(USSCCPlugin.JIRA_JQL);
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
		if(rallyKey != null && rallyKey.length()>0){
			userStoryFromRally = getUserStoryFromRall(rallyKey);
			if (userStoryFromRally!=null && userStoryFromRally.length()>0){
				addCoverage(getDirec, coverageFor, userStoryFromRally , os);
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
					
		LOG.info("coverage.buildData() =" + coverage.buildData());
		sensorContext.saveMeasure(new Measure(
				USSCCMetrics.RESULT_COVERAGE_MAP, coverage.buildData()));
		
		LOG.info("Not covered stories "+RunCodeCoverage.getNotCoveredUserStories());
		sensorContext.saveMeasure(new Measure(
				USSCCMetrics.NOTCOVERED_USER_STORIES, RunCodeCoverage.getNotCoveredUserStories()));
		
		LOG.info("notCoveredLines.buildData() =" + notCoveredLines.buildData());
		
		sensorContext.saveMeasure(new Measure(
				USSCCMetrics.NOTCOVERED_LINES, notCoveredLines.buildData()));
		
		String projectName = getProjectName(getDirec);
		
		LOG.info("Project name =" + projectName);
		
		if (projectName ==null || projectName.length() <= 0)
			projectName = UUID.randomUUID().toString();
        
        sensorContext.saveMeasure(new Measure(
                USSCCMetrics.PROJECT_NAME, projectName));
        
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
	private String getUserStoryFromRall(String rallyKey){
		RallyRestApi restApi = null;
		String USNumbers = "";
		try {
			restApi = new RallyRestApi(new URI("https://rally1.rallydev.com"), rallyKey);
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			LOG.error("error", e1);
		}
        
		QueryRequest defectCount = new QueryRequest("hierarchicalrequirement");
		defectCount.setQueryFilter(new QueryFilter("Iteration.StartDate", "<=", "today").and(new QueryFilter("Iteration.EndDate", ">=", "today")));
        defectCount.setFetch(new Fetch("FormattedID"));

		defectCount.setPageSize(1);
        defectCount.setLimit(500);
        int total = 0;
		QueryResponse defectCountResponse = null;
		try {
			if(restApi!=null)
			{
			defectCountResponse = restApi.query(defectCount);
			LOG.info("defectCountResponse :"+defectCountResponse);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOG.error("error", e);
		}
		if(defectCountResponse!=null)
		{
		 total = defectCountResponse.getTotalResultCount();
		LOG.info("total : "+total);
		}
		if(total > 0 && defectCountResponse!=null){
			for (JsonElement result : defectCountResponse.getResults()) {
                JsonObject defect = result.getAsJsonObject();
                LOG.info("FormattedID: " + defect.get("FormattedID"));
                if (defect.get("FormattedID")!=null && defect.get("FormattedID").toString().length()>0)
                	USNumbers = USNumbers + defect.get("FormattedID").toString().substring(1,defect.get("FormattedID").toString().length()-1) + ",";
            }
		}
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
			for (Map.Entry<String, Set<String>> a : ComputeSimpleUSCoverage.getNotCoveredCodeMap().entrySet()) {
				notCoveredLines.add(a.getKey(), a.getValue());
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
