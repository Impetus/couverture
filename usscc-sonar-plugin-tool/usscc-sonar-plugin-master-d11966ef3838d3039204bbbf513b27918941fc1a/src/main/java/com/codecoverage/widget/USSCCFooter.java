package com.codecoverage.widget;

import org.sonar.api.web.Footer;

// TODO: Auto-generated Javadoc
/**
 * The Class USSCCFooter.
 */
public final class USSCCFooter implements Footer {

    /* (non-Javadoc)
     * @see org.sonar.api.web.Footer#getHtml()
     */
    @Override
    public String getHtml() {
        return "<p>Footer Example - <em>This is static HTML</em></p>";
    }
}
