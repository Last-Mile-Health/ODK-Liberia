package org.lastmilehealth.collect.android.parser;

import android.text.TextUtils;

import org.lastmilehealth.collect.android.application.Collect;
import org.lastmilehealth.collect.android.cases.CaseElement;
import org.lastmilehealth.collect.android.cases.CaseTextElement;
import org.lastmilehealth.collect.android.cases.CaseType;
import org.lastmilehealth.collect.android.cases.impl.BasicCaseElement;
import org.lastmilehealth.collect.android.cases.impl.CaseElementButton;
import org.lastmilehealth.collect.android.cases.impl.CaseElementDisplay;
import org.lastmilehealth.collect.android.cases.impl.CaseElementGroup;
import org.lastmilehealth.collect.android.cases.impl.CaseElementRoot;
import org.lastmilehealth.collect.android.cases.impl.CaseElementSpecial;
import org.lastmilehealth.collect.android.cases.impl.CaseElementStringText;
import org.lastmilehealth.collect.android.cases.impl.CaseElementVariableText;
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
    public static final String TAG_ELEMENT_DATA = "datum";
    public static final String TAG_ELEMENT_DATA_GROUP = "elementData";
    public static final String TAG_ELEMENT_GROUP = "caseElementGroup";
    public static final String TAG_ELEMENT_GROUP_FORM = "caseElementGroupForm";
    public static final String TAG_CASE_TYPE = "case";
    public static final String ATTR_CASE_ELEMENT_DATA_STYLE = "style";

    public static final String ELEMENT_TYPE_DISPLAY = "Display";
    public static final String ELEMENT_TYPE_BUTTON = "Button";
    public static final String ELEMENT_TYPE_SPECIAL = "Special";

    private boolean insideCaseType = false;
    private CaseTypeImpl parsedCaseType;
    private List<CaseType> caseTypes;
    private String text;
    private List<String> secondaryForms = null;
    private boolean inElementList = false;
    private boolean inElementDataGroup = false;
    private CaseElementGroup caseElementRoot;
    private CaseElementGroup currentGroup;
    private ElementHolder currentElement;
    private String style;


    public XmlCaseParser() {
    }

    /**
     * Parses the main cases file which should be located in the root odk folder by design.
     */
    public List<CaseType> parse() throws Exception {
        XmlParserUtils.XmlPullParserHolder parserHolder = null;
        try {
            parserHolder = XmlParserUtils.getXMLFile(Collect.CASES_PATH, Collect.CASES_BAD_PATH);

            XmlPullParser parser = parserHolder.parser;

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
        } catch (Exception e) {
            throw e;
        } finally {
            parsedCaseType = null;
            secondaryForms = null;
            caseElementRoot = null;
            currentGroup = null;
            currentElement = null;
            if (parserHolder != null) {
                parserHolder.dispose();
            }
        }
        return caseTypes;
    }

    private void handleStartTag(XmlPullParser parser) {
        text = null;
        style = null;
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

                case TAG_ELEMENT_LIST:
                    caseElementRoot = new CaseElementRoot();
                    currentGroup = caseElementRoot;
                    break;

                case TAG_ELEMENT_GROUP:
                    CaseElementGroup group = new CaseElementGroup(currentGroup);
                    currentGroup.getCaseElements().add(group);
                    currentGroup = group;
                    break;

                case TAG_ELEMENT:
                    currentElement = new ElementHolder();
                    break;

                case TAG_ELEMENT_DATA_GROUP:
                    inElementDataGroup = true;
                    break;

                case TAG_ELEMENT_DATA:
                    int count = parser.getAttributeCount();
                    for (int i = 0; i < count; i++) {
                        if (ATTR_CASE_ELEMENT_DATA_STYLE.equalsIgnoreCase(parser.getAttributeName(i))) {
                            style = parser.getAttributeValue(i);
                            break;
                        }
                    }
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

            case TAG_ELEMENT_LIST:
                parsedCaseType.setCaseElement(caseElementRoot);
                break;

            case TAG_ELEMENT_GROUP:
                currentGroup = currentGroup.getParent();
                break;

            case TAG_ELEMENT_GROUP_FORM:
                currentGroup.setCaseElementsGroupForm(text);
                break;

            case TAG_ELEMENT_TYPE:
                if (currentElement != null) {
                    currentElement.type = text;
                }
                break;

            case TAG_ELEMENT_FORM:
                if (currentElement != null) {
                    currentElement.formName = text;
                }
                break;

            case TAG_ELEMENT_DATA:
                if (inElementDataGroup && !TextUtils.isEmpty(text)) {
                    if (text.startsWith("\"") && text.endsWith("\"")) {
                        // This by specs is plain text
                        CaseElementStringText elementText = new CaseElementStringText(text.substring(1, text.length() - 1), style);
                        currentElement.text.add(elementText);
                    } else {
                        CaseElementVariableText elementText = new CaseElementVariableText(text, style);
                        currentElement.text.add(elementText);
                    }
                }
                break;

            case TAG_ELEMENT_DATA_GROUP:
                inElementDataGroup = false;
                currentGroup.getCaseElements().add(currentElement.createElement());
                break;

        }
    }

    private class ElementHolder {
        private final List<CaseTextElement> text = new ArrayList<>();
        private String type;
        private String formName;

        private CaseElement createElement() {
            BasicCaseElement element = null;
            if (type == null) {
                return null;
            } else if (type.equalsIgnoreCase(ELEMENT_TYPE_BUTTON)) {
                CaseElementButton button = new CaseElementButton();
                element = button;
            } else if (type.equalsIgnoreCase(ELEMENT_TYPE_DISPLAY)) {
                element = new CaseElementDisplay();
            } else if (type.equalsIgnoreCase(ELEMENT_TYPE_SPECIAL)) {
                element = new CaseElementSpecial();
            }
            if (element != null) {
                element.setTexts(text);
                element.setFormName(formName);
                element.setType(type);
            }
            return element;
        }
    }
}
