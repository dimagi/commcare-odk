package org.commcare.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.commcare.core.interfaces.HttpResponseProcessor;
import org.commcare.core.network.ModernHttpRequester;
import org.commcare.dalvik.R;
import org.commcare.modern.util.Pair;
import org.commcare.suite.model.AppAvailableForInstall;
import org.commcare.tasks.SimpleHttpTask;
import org.commcare.tasks.templates.CommCareTaskConnector;
import org.commcare.xml.AvailableAppsParser;
import org.javarosa.core.services.locale.Localization;
import org.javarosa.xml.ElementParser;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by amstone326 on 2/3/17.
 */
public class GetAvailableAppsActivity<T> extends CommCareActivity<T> implements HttpResponseProcessor {

    public static final String PROFILE_REF = "profile-ref-selected";

    private static final String REQUESTED_FROM_PROD_KEY = "have-requested-from-prod";
    private static final String REQUESTED_FROM_INDIA_KEY = "have-requested-from-india";
    private static final String ERROR_MESSAGE_KEY = "error-message-key";

    private static final String PROD_URL = "http://192.168.8.180/phone/list_apps";
    private static final String INDIA_URL = "http://192.168.8.180/phone/list_apps";
    //private static final String PROD_URL = "https://www.commcarehq.org/phone/list_apps";
    //private static final String INDIA_URL = "https://india.commcarehq.org/phone/list_apps";

    private boolean inMobileUserAuthMode;

    private boolean requestedFromProd;
    private boolean requestedFromIndia;
    private String urlCurrentlyRequestingFrom;

    private String errorMessage;
    private View authenticateView;
    private TextView errorMessageBox;
    private ListView appsList;

    private List<AppAvailableForInstall> allAvailableApps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadStateFromSavedInstance(savedInstanceState);
        setupUI();
    }

    private void setupUI() {
        setContentView(R.layout.user_get_available_apps);
        errorMessageBox = (TextView)findViewById(R.id.error_message);
        authenticateView = findViewById(R.id.authenticate_view);
        setUpGetAppsButton();
        setUpAppsList();
        setUpToggle();
    }

    private void setUpGetAppsButton() {
        Button getAppsButton = (Button)findViewById(R.id.get_apps_button);
        getAppsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputIsValid()) {
                    errorMessageBox.setVisibility(View.INVISIBLE);
                    authenticateView.setVisibility(View.GONE);
                    //requestAppList();
                    // TODO: change back to line above
                    processTestResponseAndShowResults();
                }
            }
        });
    }

    private void setUpAppsList() {
        appsList = (ListView)findViewById(R.id.apps_list_view);
        appsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < allAvailableApps.size()) {
                    AppAvailableForInstall app = allAvailableApps.get(position);
                    Intent i = new Intent(getIntent());
                    i.putExtra(PROFILE_REF, app.getMediaProfileRef());
                    setResult(RESULT_OK, i);
                    finish();
                }
            }
        });
    }

    private void setUpToggle() {
        FrameLayout toggleContainer = (FrameLayout)findViewById(R.id.toggle_button_container);
        CompoundButton userTypeToggler;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            Switch switchButton = new Switch(this);
            switchButton.setTextOff(Localization.get("toggle.web.user.mode"));
            switchButton.setTextOn(Localization.get("toggle.mobile.user.mode"));
            userTypeToggler = switchButton;
        } else {
            ToggleButton toggleButton = new ToggleButton(this);
            toggleButton.setTextOff(Localization.get("toggle.web.user.mode"));
            toggleButton.setTextOn(Localization.get("toggle.mobile.user.mode"));
            userTypeToggler = toggleButton;
        }

        final View mobileUserView = findViewById(R.id.mobile_user_view);
        final View webUserView = findViewById(R.id.web_user_view);
        userTypeToggler.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                inMobileUserAuthMode = isChecked;
                if (inMobileUserAuthMode) {
                    mobileUserView.setVisibility(View.VISIBLE);
                    webUserView.setVisibility(View.GONE);
                } else {
                    mobileUserView.setVisibility(View.GONE);
                    webUserView.setVisibility(View.VISIBLE);
                }
                errorMessageBox.setVisibility(View.INVISIBLE);
                ((EditText)findViewById(R.id.edit_password)).setText("");
            }
        });

        userTypeToggler.setChecked(true);
        toggleContainer.addView(userTypeToggler);
    }

    // TODO: REMOVE, for testing only
    private void processTestResponseAndShowResults() {
        String responseXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><apps><app profile=\"http://www.commcarehq.org/a/dimagi/apps/download/061eed551c565a2c18140a0a89fd3180/profile.xml?latest=true\" domain=\"dimagi\" name=\"Test icon uploader\" environment=\"changeme\" version=\"15\" media-profile=\"http://www.commcarehq.org/a/dimagi/apps/download/061eed551c565a2c18140a0a89fd3180/media_profile.xml?latest=true\"/><app profile=\"http://www.commcarehq.org/a/dimagi/apps/download/ef4b94438be3916d4a52eb01a726f99b/profile.xml?latest=true\" domain=\"dimagi\" name=\"Dev Recruitment Tracking\" environment=\"changeme\" version=\"130\" media-profile=\"http://www.commcarehq.org/a/dimagi/apps/download/ef4b94438be3916d4a52eb01a726f99b/media_profile.xml?latest=true\"/><app profile=\"http://www.commcarehq.org/a/dimagi/apps/download/145884de1cbbbe29619f010c56605cc8/profile.xml?latest=true\" domain=\"dimagi\" name=\"Dimagi Reports\" environment=\"changeme\" version=\"32\" media-profile=\"http://www.commcarehq.org/a/dimagi/apps/download/145884de1cbbbe29619f010c56605cc8/media_profile.xml?latest=true\"/><app profile=\"http://www.commcarehq.org/a/dimagi/apps/download/a05614e43f6b455084d5f2e158f5bc87/profile.xml?latest=true\" domain=\"dimagi\" name=\"Dimagi Device Signout\" environment=\"changeme\" version=\"3\" media-profile=\"http://www.commcarehq.org/a/dimagi/apps/download/a05614e43f6b455084d5f2e158f5bc87/media_profile.xml?latest=true\"/></apps>";
        InputStream stream = new ByteArrayInputStream(responseXml.getBytes());
        processResponseIntoAppsList(stream);
        showResults();
    }

    private boolean inputIsValid() {
        String enteredPassword = ((EditText)findViewById(R.id.edit_password)).getText().toString();
        if ("".equals(enteredPassword)) {
            enterErrorState(Localization.get("missing.fields"));
            return false;
        }

        if (inMobileUserAuthMode) {
            String enteredMobileUser = ((EditText)findViewById(R.id.edit_username)).getText().toString();
            String enteredDomain = ((EditText)findViewById(R.id.edit_domain)).getText().toString();
            if ("".equals(enteredMobileUser) || "".equals(enteredDomain)) {
                enterErrorState(Localization.get("missing.fields"));
                return false;
            }
        } else {
            String enteredEmail = ((EditText)findViewById(R.id.edit_email)).getText().toString();
            if ("".equals(enteredEmail)) {
                enterErrorState(Localization.get("missing.fields"));
                return false;
            }
            if (!enteredEmail.contains("@")) {
                enterErrorState(Localization.get("email.address.invalid"));
                return false;
            }
        }

        return true;
    }

    private void loadStateFromSavedInstance(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            requestedFromIndia = savedInstanceState.getBoolean(REQUESTED_FROM_INDIA_KEY);
            requestedFromProd = savedInstanceState.getBoolean(REQUESTED_FROM_PROD_KEY);
            errorMessage = savedInstanceState.getString(ERROR_MESSAGE_KEY);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(REQUESTED_FROM_INDIA_KEY, requestedFromIndia);
        savedInstanceState.putBoolean(REQUESTED_FROM_PROD_KEY, requestedFromProd);
        savedInstanceState.putString(ERROR_MESSAGE_KEY, errorMessage);
    }

    /**
     *
     * @return whether a request was initiated
     */
    private boolean requestAppList() {
        URL urlToTry = getURLToTry();
        if (urlToTry != null) {
            System.out.println("requesting from " + urlToTry);
            SimpleHttpTask task;
            try {
                task = new SimpleHttpTask(this, urlToTry, new HashMap<String, String>(), false,
                        new Pair<>(getUsernameForAuth(),
                                ((EditText)findViewById(R.id.edit_password)).getText().toString()));
            } catch (ModernHttpRequester.PlainTextPasswordException e) {
                enterErrorState(Localization.get("post.not.using.https", urlToTry.toString()));
                return false;
            } catch (Exception e) {
                enterErrorState(e.getMessage());
                return false;
            }

            task.connect((CommCareTaskConnector)this);
            task.executeParallel();
            task.setResponseProcessor(this);
            setAttemptedRequestFlag();
            return true;
        }
        return false;
    }

    private String getUsernameForAuth() {
        if (inMobileUserAuthMode) {
            String username =  ((EditText)findViewById(R.id.edit_username)).getText().toString();
            String domain =  ((EditText)findViewById(R.id.edit_domain)).getText().toString();
            return username + "@" + domain + ".commcarehq.org";
        } else {
            return ((EditText)findViewById(R.id.edit_email)).getText().toString();
        }
    }

    private void setAttemptedRequestFlag() {
        /*if (PROD_URL.equals(urlCurrentlyRequestingFrom)) {
            requestedFromProd = true;
        } else {
            requestedFromIndia = true;
        }*/
        requestedFromProd = true;
        requestedFromIndia = true;
    }

    private URL getURLToTry() {
        if (!requestedFromProd) {
            urlCurrentlyRequestingFrom = PROD_URL;
        } else if (!requestedFromIndia) {
            urlCurrentlyRequestingFrom = INDIA_URL;
        } else {
            return null;
        }

        try {
            return new URL(urlCurrentlyRequestingFrom);
        } catch (MalformedURLException e) {
            enterErrorState("This shouldn't be possible...");
            return null;
        }
    }

    private void enterErrorState() {
        authenticateView.setVisibility(View.VISIBLE);
        errorMessageBox.setVisibility(View.VISIBLE);
        errorMessageBox.setText(errorMessage);
    }

    private void enterErrorState(String message) {
        errorMessage = message;
        enterErrorState();
    }

    @Override
    public void processSuccess(int responseCode, InputStream responseData) {
        System.out.println("response received");
        processResponseIntoAppsList(responseData);
        repeatRequestOrShowResults();
    }

    private void processResponseIntoAppsList(InputStream responseData) {
        try {
            KXmlParser baseParser = ElementParser.instantiateParser(responseData);
            List<AppAvailableForInstall> apps = (new AvailableAppsParser(baseParser)).parse();
            allAvailableApps.addAll(apps);
            System.out.println("parsed app list response");
        } catch (IOException | InvalidStructureException | XmlPullParserException | UnfullfilledRequirementsException e) {
            // TODO: Decide how to handle this
            System.out.println("FAILED to parse app list response");
        }
    }

    @Override
    public void processRedirection(int responseCode) {
        handleRequestError(responseCode);
    }

    @Override
    public void processClientError(int responseCode) {
        handleRequestError(responseCode);
    }

    @Override
    public void processServerError(int responseCode) {
        handleRequestError(responseCode);
    }

    @Override
    public void processOther(int responseCode) {
        handleRequestError(responseCode);
    }

    @Override
    public void handleIOException(IOException exception) {
        repeatRequestOrShowResults();
    }

    private void handleRequestError(int responseCode) {
        System.out.println(responseCode);
        repeatRequestOrShowResults();
    }

    private void repeatRequestOrShowResults() {
        if (!requestAppList()) {
            // Means we've requested to both endpoints
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (allAvailableApps.size() == 0) {
                        enterErrorState("No apps found for that user");
                    } else {
                        showResults();
                    }
                }
            });
        }
    }

    private void showResults() {
        System.out.println("showing results");
        for (AppAvailableForInstall app : allAvailableApps) {
            System.out.println(app.getAppName());
        }

        appsList.setVisibility(View.VISIBLE);
        authenticateView.setVisibility(View.GONE);
        appsList.setAdapter(new ArrayAdapter<AppAvailableForInstall>(this, android.R.layout.simple_list_item_1, allAvailableApps) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView)convertView;
                Context context = GetAvailableAppsActivity.this;
                if (textView == null) {
                    //v = View.inflate(context, R.layout.single_available_app_view, null);
                    textView = new TextView(context);
                }
                AppAvailableForInstall app = this.getItem(position);
                textView.setText(app.getAppName());
                return textView;
            }
        });
    }

}