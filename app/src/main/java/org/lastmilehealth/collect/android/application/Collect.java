/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.lastmilehealth.collect.android.application;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;

import org.lastmilehealth.collect.android.R;
import org.lastmilehealth.collect.android.database.ActivityLogger;
import org.lastmilehealth.collect.android.external.ExternalDataManager;
import org.lastmilehealth.collect.android.logic.FormController;
import org.lastmilehealth.collect.android.logic.PropertyManager;
import org.lastmilehealth.collect.android.preferences.PreferencesActivity;
import org.lastmilehealth.collect.android.utilities.AgingCredentialsProvider;
import org.opendatakit.httpclientandroidlib.client.CookieStore;
import org.opendatakit.httpclientandroidlib.client.CredentialsProvider;
import org.opendatakit.httpclientandroidlib.client.protocol.ClientContext;
import org.opendatakit.httpclientandroidlib.impl.client.BasicCookieStore;
import org.opendatakit.httpclientandroidlib.protocol.BasicHttpContext;
import org.opendatakit.httpclientandroidlib.protocol.HttpContext;

import java.io.File;
import java.util.UUID;

import static org.lastmilehealth.collect.android.R.xml.preferences;

/**
 * Extends the Application class to implement
 *
 * @author carlhartung
 */
public class Collect extends Application {

    // Storage paths
    public static final String ODK_ROOT = Environment.getExternalStorageDirectory() + File.separator + "odk";
    public static final String FORMS_PATH = ODK_ROOT + File.separator + "forms";
    public static final String INSTANCES_PATH = ODK_ROOT + File.separator + "instances";
    public static final String ARCHIVE_PATH = ODK_ROOT + File.separator + "archive";
    public static final String CACHE_PATH = ODK_ROOT + File.separator + ".cache";
    public static final String METADATA_PATH = ODK_ROOT + File.separator + "metadata";
    public static final String TMPFILE_PATH = CACHE_PATH + File.separator + "tmp.jpg";
    public static final String TMPDRAWFILE_PATH = CACHE_PATH + File.separator + "tmpDraw.jpg";
    public static final String TMPXML_PATH = CACHE_PATH + File.separator + "tmp.xml";
    public static final String LOG_PATH = ODK_ROOT + File.separator + "log";
    public static final String ROLES_PATH = ODK_ROOT + File.separator + "ROLES.xml";
    public static final String ROLES_BAD_PATH = ODK_ROOT + File.separator + "forms" + File.separator + "ROLES.xml.bad";
    public static final String CASES_PATH = FORMS_PATH + File.separator + "ODKL_cases.xml";
    public static final String CASES_BAD_PATH = FORMS_PATH + File.separator + "ODKL_cases.xml.bad";
    public static final String SUMMARY_PATH = FORMS_PATH + File.separator + "ODKL_summaries.xml";
    public static final String SUMMARY_BAD_PATH = FORMS_PATH + File.separator + "ODKL_summaries.xml.bad";

    public static final String ZIP_PATH = ODK_ROOT + File.separator + "data.zip";
    public static final String FORMS_FLAG_PATH = FORMS_PATH + File.separator + "forms.txt";
    public static final String FORMS_DB_PATH = METADATA_PATH + File.separator + "forms.db";
    public static final String INSTANCES_DB_PATH = METADATA_PATH + File.separator + "instances.db";
    public static final String INSTANCES_DB_TMP_PATH = INSTANCES_PATH + File.separator + "instances.db";
    public static final String TMP_PATH = ODK_ROOT + File.separator + "tmp";

    public static final String KEY_APP_ID = "LocalAppId";

    public static final String DEFAULT_FONTSIZE = "21";

    public static final Handler MAIN_THREAD_HANDLER = new Handler(Looper.getMainLooper());

    public static final int DEFAULT_RETENTION_TIME_EXPIRATION_TIME = 180; // days

    private String appId;

    // share all session cookies across all sessions...
    private CookieStore cookieStore = new BasicCookieStore();
    // retain credentials for 7 minutes...
    private CredentialsProvider credsProvider = new AgingCredentialsProvider(7 * 60 * 1000);
    private ActivityLogger mActivityLogger;
    private FormController mFormController = null;
    private ExternalDataManager externalDataManager;

    private static Collect singleton = null;

    public static Collect getInstance() {
        return singleton;
    }

    public ActivityLogger getActivityLogger() {
        return mActivityLogger;
    }

    public FormController getFormController() {
        return mFormController;
    }

    public void setFormController(FormController controller) {
        mFormController = controller;
    }

    public ExternalDataManager getExternalDataManager() {
        return externalDataManager;
    }

    public void setExternalDataManager(ExternalDataManager externalDataManager) {
        this.externalDataManager = externalDataManager;
    }

    public static int getQuestionFontsize() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(Collect
                .getInstance());
        String question_font = settings.getString(PreferencesActivity.KEY_FONT_SIZE,
                Collect.DEFAULT_FONTSIZE);
        int questionFontsize = Integer.valueOf(question_font);
        return questionFontsize;
    }

    public String getVersionedAppName() {
        String versionDetail = "";
        try {
            PackageInfo pinfo;
            pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            /**int versionNumber = pinfo.versionCode;
            String versionName = pinfo.versionName;
            versionDetail = " " + versionName + " (" + versionNumber + ")";*/
            String versionName = pinfo.versionName;
            versionDetail = " " + versionName + " ";

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return getString(R.string.app_name) + versionDetail;
    }

    /**
     * Creates required directories on the SDCard (or other external storage)
     *
     * @throws RuntimeException if there is no SDCard or the directory exists as a non directory
     */
    public static void createODKDirs() throws RuntimeException {
        String cardstatus = Environment.getExternalStorageState();
        if (!cardstatus.equals(Environment.MEDIA_MOUNTED)) {
            throw new RuntimeException(Collect.getInstance().getString(R.string.sdcard_unmounted, cardstatus));
        }

        String[] dirs = {
                ODK_ROOT, FORMS_PATH, INSTANCES_PATH, CACHE_PATH, METADATA_PATH, ARCHIVE_PATH
        };

        for (String dirName : dirs) {
            File dir = new File(dirName);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    RuntimeException e =
                            new RuntimeException("ODK reports :: Cannot create directory: "
                                    + dirName);
                    throw e;
                }
            } else {
                if (!dir.isDirectory()) {
                    RuntimeException e =
                            new RuntimeException("ODK reports :: " + dirName
                                    + " exists, but is not a directory");
                    throw e;
                }
            }
        }
    }

    /**
     * Predicate that tests whether a directory path might refer to an
     * ODK Tables instance data directory (e.g., for media attachments).
     *
     * @param directory
     * @return
     */
    public static boolean isODKTablesInstanceDataDirectory(File directory) {
		/**
		 * Special check to prevent deletion of files that
		 * could be in use by ODK Tables.
		 */
    	String dirPath = directory.getAbsolutePath();
    	if ( dirPath.startsWith(Collect.ODK_ROOT) ) {
    		dirPath = dirPath.substring(Collect.ODK_ROOT.length());
    		String[] parts = dirPath.split(File.separator);
    		// [appName, instances, tableId, instanceId ]
    		if ( parts.length == 4 && parts[1].equals("instances") ) {
    			return true;
    		}
    	}
    	return false;
	}

    /**
     * Construct and return a session context with shared cookieStore and credsProvider so a user
     * does not have to re-enter login information.
     *
     * @return
     */
    public synchronized HttpContext getHttpContext() {

        // context holds authentication state machine, so it cannot be
        // shared across independent activities.
        HttpContext localContext = new BasicHttpContext();

        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        localContext.setAttribute(ClientContext.CREDS_PROVIDER, credsProvider);

        return localContext;
    }

    public CredentialsProvider getCredentialsProvider() {
        return credsProvider;
    }

    public CookieStore getCookieStore() {
        return cookieStore;
    }

    @Override
    public void onCreate() {
        singleton = this;

        // // set up logging defaults for apache http component stack
        // Log log;
        // log = LogFactory.getLog("org.opendatakit.httpclientandroidlib");
        // log.enableError(true);
        // log.enableWarn(true);
        // log.enableInfo(true);
        // log.enableDebug(true);
        // log = LogFactory.getLog("org.opendatakit.httpclientandroidlib.wire");
        // log.enableError(true);
        // log.enableWarn(false);
        // log.enableInfo(false);
        // log.enableDebug(false);

        PreferenceManager.setDefaultValues(this, preferences, false);
        super.onCreate();

        PropertyManager mgr = new PropertyManager(this);

        FormController.initializeJavaRosa(mgr);

        mActivityLogger = new ActivityLogger(
                mgr.getSingularProperty(PropertyManager.DEVICE_ID_PROPERTY));
    }

    public synchronized String getAppId() {
        if (TextUtils.isEmpty(appId)) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            appId = preferences.getString(KEY_APP_ID, null);
            if (TextUtils.isEmpty(appId)) {
                appId = generateAppId();
                preferences.edit().putString(KEY_APP_ID, appId).apply();
            }
        }
        return appId;
    }

    private String generateAppId() {
        return Settings.Secure.ANDROID_ID + "." + UUID.randomUUID().toString();
    }

}
