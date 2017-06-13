package org.lastmilehealth.collect.android.cases.impl;

import android.text.Html;
import android.text.TextUtils;

import org.lastmilehealth.collect.android.cases.CaseTextElement;
import org.lastmilehealth.collect.android.parser.InstanceElement;
import org.lastmilehealth.collect.android.summary.SummaryTextElement;

import java.util.Collection;

/**
 * Created by Anton Donchev on 19.05.2017.
 */

public class CaseElementStringText implements CaseTextElement, SummaryTextElement {
    public static final String STYLE_BOLD = "bold";
    public static final String STYLE_ITALIC = "italics";
    public static final String STYLE_UNDERLINE = "underlined";
    private final String text;
    private final String style;

    public CaseElementStringText(String text,
                                 String style) {
        this.text = text;
        this.style = style;
    }

    @Override
    public CharSequence getValue(InstanceElement instanceElement) {
        if (TextUtils.isEmpty(style)) {
            return getText(instanceElement);
        }
        return formatText(getText(instanceElement));
    }

    @Override
    public String getRawText() {
        return text;
    }

    @Override
    public CharSequence generateText(Collection<InstanceElement> instances) {
        return getValue(null);
    }

    protected CharSequence formatText(CharSequence text) {
        if (STYLE_BOLD.equalsIgnoreCase(style)) {
            return Html.fromHtml("<b>" + text + "</b>");
        } else if (STYLE_ITALIC.equalsIgnoreCase(style)) {
            return Html.fromHtml("<i>" + text + "</i>");
        } else if (STYLE_UNDERLINE.equalsIgnoreCase(style)) {
            return Html.fromHtml("<u>" + text + "</u>");
        } else {
            return text;
        }
    }

    public String getText(InstanceElement instanceElement) {
        return text;
    }
}
