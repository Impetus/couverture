package com.impetus.codecoverage.plugin;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

/**
 * The Class USSCCMetrics.
 */
public final class USSCCMetrics implements Metrics {
	
    /** The Constant NOTCOVERED_USER_STORIES. */
    public static final Metric<String> NOTCOVERED_USER_STORIES = new Metric.Builder("notcovered_user_stories", "not covered user stories", Metric.ValueType.STRING)
            .setDescription("This is a metric to store not covered user stories").setQualitative(false)
            .setDomain(CoreMetrics.DOMAIN_GENERAL).create();
    
    /** The Constant NOTCOVERED_LINES. */
    public static final Metric<String> NOTCOVERED_LINES = new Metric.Builder("notcovered_line", "not covered lines", Metric.ValueType.DATA)
            .setDescription("This is a metric to store not covered lines of code").setQualitative(false)
            .setDomain(CoreMetrics.DOMAIN_GENERAL).create();

    /** The Constant RESULT_COVERAGE_MAP. */
    public static final Metric RESULT_COVERAGE_MAP = new Metric.Builder("coverage_map", "Coverage Map", Metric.ValueType.DATA)
            .setDescription("This stores User stories and its Coverage").setQualitative(false).setDomain(CoreMetrics.DOMAIN_GENERAL).create();

    /** The Constant PROJECT_NAME. */
    public static final Metric PROJECT_NAME = new Metric.Builder("project_name", "Project Name", Metric.ValueType.STRING)
    .setDescription("This stores Name of the project.").setQualitative(false).setDomain(CoreMetrics.DOMAIN_GENERAL).create();
    
    
    
    
   /** The Constant S3 ACCESS KEY. */
    public static final Metric S3_ACCESKEY = new Metric.Builder("s3Accesskey", "S3 AccessKey", Metric.ValueType.STRING)
    .setDescription("This stores s3 accesskey.").setQualitative(false).setDomain(CoreMetrics.DOMAIN_GENERAL).create();
   // getMetrics() method is defined in the Metrics interface and is used by
    // Sonar to retrieve the list of new metrics
    
    /* (non-Javadoc)
    * @see org.sonar.api.measures.Metrics#getMetrics()
    */
   @SuppressWarnings("rawtypes")
    @Override
    public List<Metric> getMetrics() {
        return Arrays.<Metric> asList(RESULT_COVERAGE_MAP, NOTCOVERED_USER_STORIES, NOTCOVERED_LINES, PROJECT_NAME,S3_ACCESKEY);
    }
}
