package org.commcare.android.tests.formnav;

import android.content.Intent;
import android.os.Environment;
import android.widget.ImageButton;
import android.widget.TextView;

import org.commcare.CommCareTestApplication;
import org.commcare.activities.FormEntryActivity;
import org.commcare.activities.components.FormEntryConstants;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.commcare.android.util.ActivityLaunchUtils;
import org.commcare.android.util.TestAppInstaller;
import org.commcare.dalvik.R;
import org.commcare.suite.model.FormEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowEnvironment;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @author Phillip Mates (pmates@dimagi.com).
 */
@Config(application = CommCareTestApplication.class)
@RunWith(AndroidJUnit4.class)
public class CalendarLocaleTest {

    @Before
    public void setup() {
        TestAppInstaller.installAppAndLogin(
                "jr://resource/commcare-apps/calendar_tests/profile.ccpr",
                "test", "123");
    }

    /**
     * Test filling out and saving a form ending in a hidden repeat group. This
     * type of form exercises uncommon end-of-form code paths
     */
    @Test
    public void testNepaliEthiopianCalendar() {
        FormEntryActivity formEntryActivity = ActivityLaunchUtils.launchFormEntry("m0-f0");
        navigateCalendarForm(formEntryActivity);
    }

    private void navigateCalendarForm(FormEntryActivity formEntryActivity) {
        ImageButton nextButton = formEntryActivity.findViewById(R.id.nav_btn_next);

        // enter an answer for the question
        TextView dayText = formEntryActivity.findViewById(R.id.daytxt);
        TextView monthText = formEntryActivity.findViewById(R.id.monthtxt);
        TextView yearText = formEntryActivity.findViewById(R.id.yeartxt);

        assertEquals(monthText.getText(), "Ashadh");
        assertEquals(dayText.getText(), "19");
        assertEquals(yearText.getText(), "2073");
        assertTrue(nextButton.getTag().equals(FormEntryConstants.NAV_STATE_NEXT));

        nextButton.performClick();

        TextView ethiopianDayText = formEntryActivity.findViewById(R.id.daytxt);
        TextView ethiopianMonthText = formEntryActivity.findViewById(R.id.monthtxt);
        TextView ethiopianYearText = formEntryActivity.findViewById(R.id.yeartxt);
        assertEquals("Säne",ethiopianMonthText.getText());
        assertEquals("26", ethiopianDayText.getText());
        assertEquals("2008", ethiopianYearText.getText());
    }
}
