package org.lastmilehealth.collect.android.parser;

import android.content.Context;
import android.util.Log;

import org.lastmilehealth.collect.android.application.Collect;
import org.lastmilehealth.collect.android.utilities.Roles;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by fklymenko on 6/26/2015.
 */
public class XMLParser {

    private static final String FORM = "form";
    private static final String NAME = "name";
    private static final String SHARED_PREFS_ROLES = "shared_preferences_role";
    private static final String ROLES = "roles";
    private Context mContext;
    private Roles mRole;
    private ArrayList<String> mPermissions;

    private ArrayList<String> mNames;

    public XMLParser(Context context) {
        mContext = context;
        moveFile();
        renameFile();
        parseXML();
    }

    private void renameFile() {

        File from = new File(Collect.ODK_ROOT, "ROLES.xml.bad");
        File to = new File(Collect.ODK_ROOT, "ROLES.xml");
        from.renameTo(to);
    }

    public void parseXML() {
        List<Roles> rolesList = new ArrayList<>();
        try {
            XmlPullParser parser = getXMLFile();
            mPermissions = new ArrayList<>();
            mNames = new ArrayList<>();

            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {

                switch (parser.getEventType()) {

                    case XmlPullParser.START_TAG:
                        addRolesName(parser);
                        addPermission(parser);
                        break;

                    case XmlPullParser.END_TAG:
                        if (parser.getName().equals("permission")) {
                            mRole.setPermission(mPermissions);
                            rolesList.add(mRole);
                        }
                    default:
                        break;
                }


                parser.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!rolesList.isEmpty()) {
            saveToSharedPreferences((ArrayList<Roles>) rolesList);
        }
        Log.d("Roles", rolesList.toString());
    }

    private void saveToSharedPreferences(ArrayList<Roles> rolesList) {

        TinyDB tinydb = new TinyDB(mContext);

        tinydb.putListString(ROLES, mNames);
        for (int i = 0; i < rolesList.size(); i++) {
            com.github.snowdream.android.util.Log.d("~", "[saveToSharedPreferences] : " + rolesList.get(i).getName() + " : " + rolesList.get(i).getPermission());
            tinydb.putListString(rolesList.get(i).getName(), rolesList.get(i).getPermission());
        }
    }

    private void addPermission(XmlPullParser parser) throws IOException, XmlPullParserException {
        if (parser.getName().equals(FORM)) {
            mPermissions.add(parser.nextText());
        }
    }

    private void addRolesName(XmlPullParser parser) throws IOException, XmlPullParserException {
        if (parser.getName().equals(NAME)) {
            mRole = new Roles();
            mPermissions = new ArrayList<>();
            String name = parser.nextText();
            mRole.setName(name);
            mNames.add(name);
        }
    }

    public XmlPullParser getXMLFile() throws XmlPullParserException, FileNotFoundException {

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        // get a reference to the file.
        File file = new File(Collect.ROLES_PATH);

        // create an input stream to be read by the stream reader.
        FileInputStream fis = new FileInputStream(file);

        // set the input for the parser using an InputStreamReader
        xpp.setInput(new InputStreamReader(fis));

        return xpp;
    }


    private void moveFile() {

//        remove previous data
        new File(Collect.ODK_ROOT, "ROLES.xml").delete();

        InputStream in = null;
        OutputStream out = null;
        try {

            in = new FileInputStream(Collect.ROLES_BAD_PATH );
            out = new FileOutputStream(Collect.ODK_ROOT + "/ROLES.xml.bad");

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;
        }

        catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }

    public List<String> getmNames() {
        return mNames == null ? Collections.EMPTY_LIST : mNames;
    }
}

