package com.impetus.codecoverage.plugin;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.SonarPlugin;

import com.impetus.codecoverage.sensor.CouvertureSensor;
import com.impetus.codecoverage.widget.CouvertureFooter;
import com.impetus.codecoverage.widget.CouvertureRubyWidget;

/**
 *  This class is the entry point for all extensions.
 */
@Properties({
		@Property(key = CouverturePlugin.USER_STORY, name = "User Story", description = "User Story Number", defaultValue = "US-1234"),
		@Property(key = CouverturePlugin.RALLY_KEY, name = "Rally Login Key", description = "Rally Login Key"),
		@Property(key = CouverturePlugin.JIRA_URL, name = "Jira URL", description = "JIRA URL"),
		@Property(key = CouverturePlugin.RALLY_RELEASE, name = "Rally Release ID", description = "Rally Release No"),
		@Property(key = CouverturePlugin.JIRA_LOGIN, name = "Jira Login ID", description = "JIRA Login ID"),
		@Property(key = CouverturePlugin.JIRA_PASSWORD, name = "Jira Login Password", description = "JIRA Login Password"),
		@Property(key = CouverturePlugin.JIRA_JQL, name = "Jira JQL", description = "JIRA JQL", defaultValue = "sprint in openSprints() and sprint not in futureSprints() and issuetype in (Bug, Story)"),
		@Property(key = CouverturePlugin.S3_ACCESSKEY, name = "S3 Access Key", description = "S3 Access Key"),
		@Property(key = CouverturePlugin.S3_SECRETKEY, name = "S3 SecretKey", description = "S3 SecretKey")
		
})

public final class CouverturePlugin extends SonarPlugin {

	/** The Constant USER_STORY. */
	public static final String USER_STORY = "sonar.coverage.userstory";
	
	/** The Constant RALLY_KEY. */
	public static final String RALLY_KEY = "sonar.coverage.rallykey";
	
	/** The Constant JIRA_URL. */
	public static final String JIRA_URL = "sonar.jira.url";
	
	/** The Constant RALLY_RELEASE. */
	public static final String RALLY_RELEASE = "sonar.rally.release";
	
	/** The Constant JIRA_LOGIN. */
	public static final String JIRA_LOGIN = "sonar.jira.login";
	
	/** The Constant JIRA_PASSWORD. */
	public static final String JIRA_PASSWORD = "sonar.jira.password";
	
	/** The Constant JIRA_JQL. */
	public static final String JIRA_JQL = "sonar.jira.jql";
	
	
	/** The Constant S3 ACCESSKEY. */
	public static final String S3_ACCESSKEY = "sonar.s3.accesskey";
	/** The Constant S3 SECREATKEY. */
	public static final String  S3_SECRETKEY  = "sonar.s3.secretkey";
	
	
	
	
	
	/* 
	 * this method is used for get the extension and return a list.
	 * @see org.sonar.api.Plugin#getExtensions()
	 */
	// This is where you're going to declare all your SonarQube extensions
	@SuppressWarnings("rawtypes")
	@Override
	public List getExtensions() {
		return Arrays.asList(
		// Definitions
				CouvertureMetrics.class,

				// Batch
				CouvertureSensor.class,

				// UI
				CouvertureFooter.class, CouvertureRubyWidget.class);
	}
}
