package org.lastmilehealth.collect.android.parser;

import android.text.TextUtils;

import org.lastmilehealth.collect.android.application.Collect;
import org.lastmilehealth.collect.android.cases.CaseType;
import org.lastmilehealth.collect.android.cases.impl.CaseTypeImpl;
import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser focused on parsing cases.xml file.
 * <p>
 * Created by Anton Donchev on 05.05.2017.
 */

public class XmlCaseParser {
    public static final String TAG_DISPLAY_NAME = "caseDisplayName";
    public static final String TAG_PRIMARY_FORM = "casePrimaryForm";
    public static final String TAG_PRIMARY_VARIABLE = "casePrimaryVariable";
    public static final String TAG_SECONDARY_FORMS = "caseSecondaryForms";
    public static final String TAG_SECONDARY_FORM_NAME = "formName";
    public static final String TAG_ELEMENT_LIST = "caseElementList";
    public static final String TAG_ELEMENT = "caseElement";
    public static final String TAG_ELEMENT_TYPE = "elementType";
    public static final String TAG_ELEMENT_FORM = "elementForm";
    public static final String TAG_ELEMENT_DATA = "elementData";
    public static final String TAG_ELEMENT_GROUP = "caseElementGroup";
    public static final String TAG_ELEMENT_GROUP_FORM = "caseElementGroupForm";
    public static final String TAG_CASE_TYPE = "case";
    private boolean insideCaseType = false;
    private CaseTypeImpl parsedCaseType;
    private List<CaseType> caseTypes;
    private String text;
    private List<String> secondaryForms = null;

    public XmlCaseParser() {
    }

    /**
     * Parses the main cases file which should be located in the root odk folder by design.
     */
    public List<CaseType> parse() throws Exception {
        XmlPullParser parser = XmlParserUtils.getXMLFile(Collect.CASES_PATH, Collect.CASES_BAD_PATH);
        caseTypes = new ArrayList<>();

        // loop through the elements until the end of the document is reached.
        while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {

            switch (parser.getEventType()) {

                case XmlPullParser.START_TAG:
                    handleStartTag(parser);
                    break;

                case XmlPullParser.END_TAG:
                    handleEndTag(parser);
                    break;

                case XmlPullParser.TEXT:
                    text = parser.getText();

                default:
                    break;
            }


            parser.next();
        }
        return caseTypes;
    }

    private void handleStartTag(XmlPullParser parser) {
        text = null;
        if (!insideCaseType) {
            // if we are not in case type than we need to wait for a case tag in order to start parsing a case
            if (parser.getName().equals(TAG_CASE_TYPE)) {
                // We reached a case tag so we need to create a new case type in order to fill it with data
                // and mark that we are inside such tag.
                parsedCaseType = new CaseTypeImpl();
                insideCaseType = true;
            }
        } else {
            switch (parser.getName()) {
                case TAG_SECONDARY_FORMS:
                    secondaryForms = new ArrayList<>();
                    break;
            }
        }
    }

    private void handleEndTag(XmlPullParser parser) {
        switch (parser.getName()) {
            case TAG_DISPLAY_NAME:
                parsedCaseType.setDisplayName(text);
                break;

            case TAG_PRIMARY_FORM:
                parsedCaseType.setPrimaryFormName(text);
                break;

            case TAG_PRIMARY_VARIABLE:
                parsedCaseType.setPrimaryFormVariable(text);
                break;

            case TAG_CASE_TYPE:
                // a case close tag is reached
                if (insideCaseType) {
                    // if we are still inside a case tag (this should be the case)
                    // we nee to add the parsed case type to the cases list and end
                    // editing it.
                    caseTypes.add(parsedCaseType);
                }
                // reset the cases.
                parsedCaseType = null;
                insideCaseType = false;
                break;

            case TAG_SECONDARY_FORMS:
                parsedCaseType.setSecondaryFormNames(secondaryForms);
                secondaryForms = null;
                break;

            case TAG_SECONDARY_FORM_NAME:
                if (secondaryForms != null && !TextUtils.isEmpty(text)) {
                    secondaryForms.add(text);
                }
                break;

        }
    }
}
