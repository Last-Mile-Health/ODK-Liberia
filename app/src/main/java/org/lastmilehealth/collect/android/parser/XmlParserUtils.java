package org.lastmilehealth.collect.android.parser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Anton Donchev on 05.05.2017.
 */

public class XmlParserUtils {

    /**
     * This method returns an xml parser fed by the first existing file found from the path send as parameters.
     */
    public static XmlPullParserHolder getXMLFile(String... filenames) throws XmlPullParserException, FileNotFoundException {
        if (filenames != null) {
            for (String filename : filenames) {
                // get a reference to the file.
                File file = new File(filename);
                if (file.exists()) {
                    // if the file exists than we create the parser and return the instance
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    XmlPullParser xpp = factory.newPullParser();

                    // create an input stream to be read by the stream reader.
                    FileInputStream fis = new FileInputStream(file);

                    // set the input for the parser using an InputStreamReader
                    xpp.setInput(new InputStreamReader(fis));
                    return new XmlPullParserHolder(xpp, fis);
                }
                // If the file was not found than continue to iterate until all paths are checked
            }
        }

        // If there is no file found then throw an exception
        throw new FileNotFoundException("No XML file was found to load");
    }

    public static final class XmlPullParserHolder {
        public final XmlPullParser parser;
        public final InputStream stream;

        public XmlPullParserHolder(XmlPullParser parser,
                                   InputStream stream) {
            this.parser = parser;
            this.stream = stream;
        }

        public void dispose() {
            try {
                stream.close();
            } catch (Exception e) {
                // nothing
            }
        }
    }

}
