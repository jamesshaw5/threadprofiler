package org.jamesshaw.threadprofiler;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class Formatter {

    public final static long _nanoseconds = 1;
    public final static long _microseconds = _nanoseconds * 1000;
    public final static long _milliseconds = _microseconds * 1000;
    public final static long _seconds = _milliseconds * 1000;
    public final static long _minutes = _seconds * 60;
    public final static long _hours = _minutes * 60;
    public final static long _days = _hours * 24;

    private String indent= "  ";

    public String format(ThreadProfilerNode node) {

        StringBuilder stringBuilder = new StringBuilder();

        FormatterColumn keyColumn = new FormatterColumn();
        FormatterColumn descriptionColumn = new FormatterColumn();
        FormatterColumn threadColumn = new FormatterColumn();
        List<ThreadProfilerNode> flattenedNodes = new ArrayList<>();

        recurse(flattenedNodes, keyColumn, descriptionColumn, threadColumn, node);

        int rows = keyColumn.getValues().size();

        int keyWidth = keyColumn.getMaximumWidth();
        int descriptionWidth = descriptionColumn.getMaximumWidth();
        int threadWidth = threadColumn.getMaximumWidth();

        final String columnDivider = " | ";
        final String newline = "\n";

        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(1);

        int totalWidth = 2 + threadWidth + keyWidth + descriptionWidth + 45 + 45 + 8 + 2 + 15;
        StringBuilder div = new StringBuilder();
        for(int i = 0; i < totalWidth; i++) {
            div.append("-");
        }

        stringBuilder.append(div).append(newline);
        stringBuilder.append("| ");
        stringBuilder.append(padRight(threadWidth ,"Thread")).append(columnDivider);
        stringBuilder.append(padRight(keyWidth ,"Key")).append(columnDivider);
        stringBuilder.append(padRight(descriptionWidth ,"Details")).append(columnDivider);
        stringBuilder.append(padLeft(45 ,"Wall time")).append(columnDivider);
        stringBuilder.append(padLeft(45 ,"CPU time")).append(columnDivider);
        stringBuilder.append(padLeft(8,"CPU %"));
        stringBuilder.append(" |").append(newline);
        stringBuilder.append(div).append(newline);;

        for(int row = 0; row < rows; row++) {

            ThreadProfilerNode threadProfilerNode = flattenedNodes.get(row);

            String keyValue = keyColumn.getValues().get(row);
            String descriptionValue = descriptionColumn.getValues().get(row);
            String threadValue = threadColumn.getValues().get(row);

            String keyPadded = padRight(keyWidth, keyValue);
            String descriptionPadded = padRight(descriptionWidth, descriptionValue);
            String threadPadded = padRight(threadWidth, threadValue);

            stringBuilder.append("| ");

            stringBuilder.append(threadPadded).append(columnDivider);
            stringBuilder.append(keyPadded).append(columnDivider);
            stringBuilder.append(descriptionPadded).append(columnDivider);

            appendTime(stringBuilder, threadProfilerNode.getElapsedWallTime());
            stringBuilder.append(columnDivider);

            appendTime(stringBuilder, threadProfilerNode.getElapsedCycleTime());
            stringBuilder.append(columnDivider);

            double ratio = 100d * threadProfilerNode.getElapsedCycleTime() / (double)threadProfilerNode.getElapsedWallTime();
            if(Double.isNaN(ratio)) {
                String padded = padLeft(6, "--");
                stringBuilder.append(padded).append(" %");
            }else {
                String padded = padLeft(6, numberFormat.format(ratio));
                stringBuilder.append(padded).append(" %");
            }

            stringBuilder.append(" |").append(newline);
        }

        stringBuilder.append(div).append(newline);;

        return stringBuilder.toString();

    }

    private String padLeft(int width, String string) {
        return pad(width, true, string);
    }

    private String padRight(int width, String string) {
        return pad(width, false, string);
    }

    private String pad(int width, boolean left, String string) {
        StringBuilder stringBuilder = new StringBuilder();

        if(left) {
            for(int i = 0; i < width - string.length(); i++) {
                stringBuilder.append(" ");
            }
        }
        stringBuilder.append(string);

        if(!left) {
            for(int i = 0; i < width - string.length(); i++) {
                stringBuilder.append(" ");
            }
        }

        return stringBuilder.toString();
    }

    private void recurse(List<ThreadProfilerNode> flattenedNodes, FormatterColumn keyColumn, FormatterColumn descriptionColumn,FormatterColumn threadColumn, ThreadProfilerNode node) {

        flattenedNodes.add(node);

        keyColumn.add(formatDepth(node.getDepth(), node.getOperationKey()));
        descriptionColumn.add(node.getDescription());
        threadColumn.add(node.getThreadName());

        node.getChildren().forEach(child -> recurse(flattenedNodes, keyColumn, descriptionColumn, threadColumn, child));

    }

    private String formatDepth(int depth, String operationKey) {
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < depth; i++) {
            stringBuilder.append(indent);
        }

        stringBuilder.append(operationKey);

        return stringBuilder.toString();
    }


    private void appendTime(StringBuilder builder, long time) {

        long remainder = time;

        long days = remainder / _days;
        remainder = remainder % _days;

        long hours = remainder / _hours;
        remainder = remainder % _hours;

        long minutes = remainder / _minutes;
        remainder = remainder % _minutes;

        long seconds  = remainder / _seconds;
        remainder = remainder % _seconds;

        long milliseconds  = remainder / _milliseconds;
        remainder = remainder % _milliseconds;

        long microseconds  = remainder / _microseconds;
        remainder = remainder % _microseconds;

        long nanoseconds  = remainder;

        String gap = "    ";
        if(days > 0) {
            builder.append(padLeft(4, Long.toString(days)) + " d");
        }else{
            builder.append(padLeft(6, ""));
        }

        if(hours > 0) {
            builder.append(padLeft(4, Long.toString(hours)) + " h");
        }else{
            builder.append(padLeft(6, ""));
        }

        if(minutes > 0) {
            builder.append(padLeft(4, Long.toString(minutes)) + " m");
        }else{
            builder.append(padLeft(6, ""));
        }

        if(seconds > 0) {
            builder.append(padLeft(4, Long.toString(seconds)) + " s");
        }else{
            builder.append(padLeft(6, ""));
        }

        if(milliseconds > 0) {
            builder.append(padLeft(4, Long.toString(milliseconds)) + " ms");
        }else{
            builder.append(padLeft(7, ""));
        }

        if(microseconds > 0) {
            builder.append(padLeft(4, Long.toString(microseconds)) + " mu");
        }else{
            builder.append(padLeft(7, ""));
        }

        builder.append(padLeft(4, Long.toString(nanoseconds)) + " ns");

    }
}
