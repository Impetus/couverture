package com.codecoverage.plugin;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.SonarPlugin;

import com.codecoverage.sensor.USSCCSensor;
import com.codecoverage.widget.USSCCFooter;
import com.codecoverage.widget.USSCCRubyWidget;

/**
 *  This class is the entry point for all extensions.
 */
@Properties({
		@Property(key = USSCCPlugin.USER_STORY, name = "User Story", description = "User Story Number", defaultValue = "US-1234"),
		@Property(key = USSCCPlugin.RALLY_KEY, name = "Rally Login Key", description = "Rally Login Key"),
		@Property(key = USSCCPlugin.JIRA_URL, name = "Jira URL", description = "JIRA URL"),
		@Property(key = USSCCPlugin.JIRA_LOGIN, name = "Jira Login ID", description = "JIRA Login ID"),
		@Property(key = USSCCPlugin.JIRA_PASSWORD, name = "Jira Login Password", description = "JIRA Login Password"),
		@Property(key = USSCCPlugin.JIRA_JQL, name = "Jira JQL", description = "JIRA JQL", defaultValue = "sprint in openSprints() and sprint not in futureSprints() and issuetype in (Bug, Story)")
		})

public final class USSCCPlugin extends SonarPlugin {

	/** The Constant USER_STORY. */
	public static final String USER_STORY = "sonar.coverage.userstory";
	
	/** The Constant RALLY_KEY. */
	public static final String RALLY_KEY = "sonar.coverage.rallykey";
	
	/** The Constant JIRA_URL. */
	public static final String JIRA_URL = "sonar.jira.url";
	
	/** The Constant JIRA_LOGIN. */
	public static final String JIRA_LOGIN = "sonar.jira.login";
	
	/** The Constant JIRA_PASSWORD. */
	public static final String JIRA_PASSWORD = "sonar.jira.password";
	
	/** The Constant JIRA_JQL. */
	public static final String JIRA_JQL = "sonar.jira.jql";
	
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
				USSCCMetrics.class,

				// Batch
				USSCCSensor.class,

				// UI
				USSCCFooter.class, USSCCRubyWidget.class);
	}
}
