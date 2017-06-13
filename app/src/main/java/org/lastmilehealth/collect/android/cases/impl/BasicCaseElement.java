package org.lastmilehealth.collect.android.cases.impl;

import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.lastmilehealth.collect.android.cases.CaseElement;
import org.lastmilehealth.collect.android.cases.CaseTextElement;
import org.lastmilehealth.collect.android.parser.InstanceElement;

import java.util.List;

/**
 * Common element functionality.
 * Created by eXirrah on 22-May-17.
 */

public abstract class BasicCaseElement implements CaseElement {
    protected List<CaseTextElement> texts = null;
    protected String formName;
    protected String type;

    public void setTexts(List<CaseTextElement> texts) {
        this.texts = texts;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public String getFormName() {
        return formName;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public void dispose() {

    }

    protected View inflate(ViewGroup context,
                           int layoutId) {
        LayoutInflater inflater = LayoutInflater.from(context.getContext());
        return inflater.inflate(layoutId, context, false);
    }

    protected CharSequence generateText(InstanceElement element) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (CaseTextElement text : texts) {
            CharSequence textFormatted = text.getValue(element);
            if (textFormatted != null) {
                builder.append(textFormatted);
            }
        }
        return builder;
    }
}

