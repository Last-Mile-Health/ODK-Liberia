package org.lastmilehealth.collect.android.parser;

import org.xmlpull.v1.XmlPullParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Parser focused on parsing cases.xml file.
 * <p>
 * Created by Anton Donchev on 05.05.2017.
 */

public class XmlInstanceParser {
    private static final Map<String, InstanceElement> INSTANCE_ELEMENTS_CACHE = new HashMap<>();
    private final String instancePath;
    private InstanceElement currentElement = null;
    private InstanceElement rootElement;

    public XmlInstanceParser(String instancePath) {
        this.instancePath = instancePath;
    }

    public static void resetCache() {
        INSTANCE_ELEMENTS_CACHE.clear();
    }

    /**
     * Parses the main cases file which should be located in the root odk folder by design.
     */
    public InstanceElement parse() throws Exception {
        InstanceElement cachedElement = INSTANCE_ELEMENTS_CACHE.get(instancePath);
        if (cachedElement != null) {
            return cachedElement;
        }

        XmlParserUtils.XmlPullParserHolder parserHolder = null;
        try {
            parserHolder = XmlParserUtils.getXMLFile(instancePath);
            XmlPullParser parser = parserHolder.parser;

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
                        handlerText(parser);

                    default:
                        break;
                }

                parser.next();
            }
            INSTANCE_ELEMENTS_CACHE.put(instancePath, rootElement);
        } catch (Exception e) {
            throw e;
        } finally {
            if (parserHolder != null) {
                parserHolder.dispose();
            }
        }
        return rootElement;
    }

    private void handlerText(XmlPullParser parser) {
        currentElement.setValue(parser.getText());
    }

    private void handleStartTag(XmlPullParser parser) {
        if (currentElement == null) {
            currentElement = new InstanceElement();
            rootElement = currentElement;
        } else {
            InstanceElement newElement = new InstanceElement();
            currentElement.getChildren().add(newElement);
            newElement.setParent(currentElement);
            currentElement = newElement;
        }

        currentElement.setName(parser.getName());
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            currentElement.getAttributes().put(parser.getAttributeName(i), parser.getAttributeValue(i));
        }
    }

    private void handleEndTag(XmlPullParser parser) {
        currentElement = currentElement.getParent();
    }
}
