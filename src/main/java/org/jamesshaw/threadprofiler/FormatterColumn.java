package org.jamesshaw.threadprofiler;

import java.util.ArrayList;
import java.util.List;

public class FormatterColumn {

    private List<String> values = new ArrayList<>();
    private int maximumWidth = 0;

    public List<String> getValues() {
        return values;
    }

    public int getMaximumWidth() {
        return maximumWidth;
    }

    public void add(String value) {
        maximumWidth = Math.max(maximumWidth, value.length());
        values.add(value);
    }
}
