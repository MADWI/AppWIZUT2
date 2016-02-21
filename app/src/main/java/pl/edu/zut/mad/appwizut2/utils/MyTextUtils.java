package pl.edu.zut.mad.appwizut2.utils;

import android.graphics.Typeface;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities for text and Spannable formatting
 *
 * Name with "My" prefix to avoid confusion with {@link android.text.TextUtils}
 */
public class MyTextUtils {

    private static final Pattern CONSECUTIVE_WHITESPACE = Pattern.compile("^\\s+|\\s*\\uFFFC\\s*|\\s\\s+|\\s+$");



    public static Spanned fromHtmlAndClean(String html) {
        // Step 1: Run system-provided fromHtml
        Spanned rawFromHtml = Html.fromHtml(html);

        // Step 2: Trim whitespace
        // This code is loosely based on implementation of Matcher.replaceAll
        // but unlike replaceAll() keeps Spanned text
        SpannableStringBuilder spanned = new SpannableStringBuilder();
        Matcher matcher = CONSECUTIVE_WHITESPACE.matcher(rawFromHtml);
        int appendPos = 0;

        while (matcher.find()) {
            // Append text between matches (end of previous match to start of this match)
            spanned.append(rawFromHtml.subSequence(appendPos, matcher.start()));

            // Append replacement (space, newline or two newlines)
            // but only if this isn't match at start or end of string
            if (matcher.start() != 0 && matcher.end() != rawFromHtml.length()) {
                String group = matcher.group();

                // http://stackoverflow.com/a/8910767
                int newlineCount = group.length() - group.replace("\n", "").length();

                // Replace group of whitespace
                // Keep up to two newlines
                if (newlineCount == 0) {
                    spanned.append(" ");
                } else if (newlineCount == 1) {
                    spanned.append("\n");
                } else {
                    spanned.append("\n\n");
                }
            }

            appendPos = matcher.end();
        }

        // Place remaining text
        spanned.append(rawFromHtml.subSequence(appendPos, rawFromHtml.length()));


        // Step 3: filter spans
        Object[] spans = spanned.getSpans(0, spanned.length(), Object.class);
        for (Object span : spans) {

            if (span instanceof RelativeSizeSpan) {
                // RelativeSizeSpan: headings; replace with bold text
                int start = spanned.getSpanStart(span);
                int end = spanned.getSpanEnd(span);
                spanned.removeSpan(span);
                spanned.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            } else if (span instanceof ImageSpan) {
                // Delete ImageSpan (they shouldn't slip through whitespace stripping step anyway)
                spanned.removeSpan(span);
            }
        }
        return spanned;
    }

    /**
     * Convert case in string to make only beginnings of word uppercase
     *
     * Taken from http://stackoverflow.com/a/1892812
     */
    public static String capitalizeString(String string) {
        char[] chars = string.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i]) || chars[i] == '.' || chars[i] == '\'') { // You can add other chars here
                found = false;
            }
        }
        return String.valueOf(chars);
    }
}
