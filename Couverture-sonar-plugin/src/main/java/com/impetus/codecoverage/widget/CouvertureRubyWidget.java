package com.impetus.codecoverage.widget;

import org.sonar.api.web.AbstractRubyTemplate;
import org.sonar.api.web.Description;
import org.sonar.api.web.RubyRailsWidget;
import org.sonar.api.web.UserRole;
import org.sonar.api.web.WidgetCategory;

/**
 * The Class CouvertureRubyWidget.
 */
@UserRole(UserRole.USER)
@Description("Calculates and Show User Story Specific Code Coverage")
@WidgetCategory("USSCC")
public class CouvertureRubyWidget extends AbstractRubyTemplate implements RubyRailsWidget {

    /* (non-Javadoc)
     * @see org.sonar.api.web.View#getId()
     */
    @Override
    public String getId() {
        return "USSCC";
    }

    /* (non-Javadoc)
     * @see org.sonar.api.web.View#getTitle()
     */
    @Override
    public String getTitle() {
        return "USSCC Tool";
    }

    /* (non-Javadoc)
     * @see org.sonar.api.web.AbstractRubyTemplate#getTemplatePath()
     */
    @Override
    protected String getTemplatePath() {
        return "/example/example_widget.html.erb";
    }

}
