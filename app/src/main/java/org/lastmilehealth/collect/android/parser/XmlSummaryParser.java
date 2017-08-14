package org.lastmilehealth.collect.android.parser;

import android.text.TextUtils;

import org.lastmilehealth.collect.android.application.Collect;
import org.lastmilehealth.collect.android.cases.impl.CaseElementStringText;
import org.lastmilehealth.collect.android.summary.BasicIndicator;
import org.lastmilehealth.collect.android.summary.BasicSummary;
import org.lastmilehealth.collect.android.summary.BasicSummaryCollection;
import org.lastmilehealth.collect.android.summary.BasicSummaryTextElementCollection;
import org.lastmilehealth.collect.android.summary.DisplayIndicator;
import org.lastmilehealth.collect.android.summary.Indicator;
import org.lastmilehealth.collect.android.summary.SpecialIndicator;
import org.lastmilehealth.collect.android.summary.SummaryCollection;
import org.lastmilehealth.collect.android.summary.SummaryTextExpressionElement;
import org.xmlpull.v1.XmlPullParser;

import static org.lastmilehealth.collect.android.parser.XmlCaseParser.ATTR_CASE_ELEMENT_DATA_STYLE;

/**
 * Created by Anton Donchev on 13.06.2017.
 */

public class XmlSummaryParser {
    public static final String TAG_SUMMARY = "summary";
    public static final String TAG_SUMMARY_DISPLAY_NAME = "summaryDisplayName";
    public static final String TAG_INDICATOR = "indicator";
    public static final String TAG_INDICATOR_TYPE = "indicatorType";
    public static final String TAG_INDICATOR_FORM = "indicatorForm";
    public static final String TAG_INDICATOR_ELEMENTS = "indicatorElements";
    public static final String TAG_INDICATOR_ELEMENT_DATUM = "datum";

    private BasicSummaryCollection summariesCollection;
    private BasicSummary currentSummary;
    private IndicatorValues currentIndicator;
    private String text;
    private String style;
    private boolean insideIndicatorElements = false;


    public SummaryCollection parse() throws Exception {
        XmlParserUtils.XmlPullParserHolder parserHolder = null;
        try {
            parserHolder = XmlParserUtils.getXMLFile(Collect.SUMMARY_PATH, Collect.SUMMARY_BAD_PATH);
            XmlPullParser parser = parserHolder.parser;

            summariesCollection = new BasicSummaryCollection();

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
            if (parserHolder != null) {
                parserHolder.dispose();
            }
        }


        return summariesCollection;
    }

    private void handleStartTag(XmlPullParser parser) {
        switch (parser.getName()) {
            case TAG_SUMMARY:
                currentSummary = new BasicSummary();
                break;

            case TAG_INDICATOR:
                currentIndicator = new IndicatorValues();
                break;

            case TAG_INDICATOR_ELEMENTS:
                insideIndicatorElements = true;
                break;

            case TAG_INDICATOR_ELEMENT_DATUM:
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

    private void handleEndTag(XmlPullParser parser) {
        switch (parser.getName()) {
            case TAG_SUMMARY:
                if (currentSummary != null) {
                    summariesCollection.add(currentSummary);
                    currentSummary = null;
                    insideIndicatorElements = false;
                }
                break;

            case TAG_SUMMARY_DISPLAY_NAME:
                if (currentSummary != null && !TextUtils.isEmpty(text)) {
                    currentSummary.setDisplayName(text);
                    insideIndicatorElements = false;
                }
                break;

            case TAG_INDICATOR:
                if (currentSummary != null && currentIndicator != null) {
                    Indicator indicator = currentIndicator.createIndicator();
                    if (indicator != null) {
                        currentSummary.getIndicators().add(indicator);
                    }
                    currentIndicator = null;
                    insideIndicatorElements = false;
                }
                break;

            case TAG_INDICATOR_TYPE:
                if (currentIndicator != null) {
                    currentIndicator.type = text;
                    insideIndicatorElements = false;
                }
                break;

            case TAG_INDICATOR_FORM:
                if (currentIndicator != null) {
                    currentIndicator.formName = text;
                    insideIndicatorElements = false;
                }
                break;

            case TAG_INDICATOR_ELEMENTS:
                insideIndicatorElements = false;
                break;

            case TAG_INDICATOR_ELEMENT_DATUM:
                if (insideIndicatorElements && currentIndicator != null) {
                    currentIndicator.addTextElement(text, style);
                }
                break;
        }
    }

    private static final class IndicatorValues {
        private final BasicSummaryTextElementCollection textCollections = new BasicSummaryTextElementCollection();
        private String formName;
        private String type;

        public Indicator createIndicator() {
            BasicIndicator indicator = null;
            switch (type) {
                case IndicatorType.DISPLAY:
                    indicator = new DisplayIndicator();
                    break;

                case IndicatorType.SPECIAL:
                    indicator = new SpecialIndicator();
                    break;

            }
            if (indicator != null) {
                indicator.setFormName(formName);
                indicator.setType(type);
                indicator.getTextElements().addAll(textCollections);
            }
            return indicator;
        }

        public void addTextElement(String text,
                                   String style) {
            if (!TextUtils.isEmpty(text)) {

                if (text.startsWith("\"") && text.endsWith("\"")) {
                    // This by specs is plain text
                    CaseElementStringText elementText = new CaseElementStringText(text.substring(1, text.length() - 1), style);
                    textCollections.add(elementText);
                } else {
                    SummaryTextExpressionElement elementText = new SummaryTextExpressionElement(text, style);
                    textCollections.add(elementText);
                }
            }
        }


    }
}
