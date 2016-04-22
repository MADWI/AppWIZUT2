package pl.edu.zut.mad.appwizut2.utils;

import android.graphics.Typeface;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.ParagraphStyle;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import org.xml.sax.XMLReader;

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
        final SpannableStringBuilder spanned;
        {
            Spanned rawFromHtml = Html.fromHtml(html, null, new MyTagHandler());
            spanned =
                    rawFromHtml instanceof SpannableStringBuilder ?
                            (SpannableStringBuilder) rawFromHtml :
                            new SpannableStringBuilder(rawFromHtml);
        }

        // Step 2: Wrap ParagraphStyle spans
        for (ParagraphStyle paragraphStyle : spanned.getSpans(0, spanned.length(), ParagraphStyle.class)) {
            spanned.setSpan(
                    new WrappedParagraphStyle(paragraphStyle),
                    spanned.getSpanStart(paragraphStyle),
                    spanned.getSpanEnd(paragraphStyle),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            spanned.removeSpan(paragraphStyle);
        }

        // Step 3: Trim whitespace
        final String originalString = spanned.toString();
        final Matcher matcher = CONSECUTIVE_WHITESPACE.matcher(spanned);

        runRegexCallbackBackward(matcher, new RegexCallback() {
            @Override
            public void handleMatch(int start, int end) {
                String newText;
                if (start != 0 && end != originalString.length()) {
                    String oldText = originalString.substring(start, end);

                    // http://stackoverflow.com/a/8910767
                    int newlineCount = oldText.length() - oldText.replace("\n", "").length();

                    // Replace group of whitespace
                    // Keep up to two newlines
                    if (newlineCount == 0) {
                        newText = " ";
                    } else if (newlineCount == 1) {
                        newText = "\n";
                    } else {
                        newText = "\n\n";
                    }
                } else {
                    newText = "";
                }
                spanned.replace(start, end, newText);
            }
        });

        // Step 4: filter spans
        Object[] spans = spanned.getSpans(0, spanned.length(), Object.class);
        for (Object span : spans) {
            if (span instanceof WrappedParagraphStyle) {
                ((WrappedParagraphStyle) span).unwrapOn(spanned);
            } else if (span instanceof RelativeSizeSpan) {
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
     * Find all matches and run callback in reverse order of their occurrences
     */
    private static void runRegexCallbackBackward(Matcher matcher, RegexCallback callback) {
        // (start, end) pairs
        int results[] = new int[40];

        // Position in results array
        int position = 0;

        // Iterate over matches
        while (matcher.find()) {
            // Grow results array
            if (results.length < position + 2) {
                int results2[] = new int[results.length + 40];
                System.arraycopy(results, 0, results2, 0, results.length);
                results = results2;
            }

            results[position] = matcher.start();
            results[position + 1] = matcher.end();
            position += 2;
        }

        // Run callback passing it arguments we collected from matching
        // iterating backward over results
        for (position -= 2; position >= 0; position -= 2) {
            callback.handleMatch(results[position], results[position + 1]);
        }
    }

    private interface RegexCallback {
        /**
         * Matcher has matched
         *
         * @param start Position of match start ({@link Matcher#start()})
         * @param end Position of match end ({@link Matcher#end()})
         */
        void handleMatch(int start, int end);
    }

    /**
     * Helper class for wrapping ParagraphStyle objects as they are not handled well when
     * editing Spannable. We replace all ParagraphStyle spans with these set as non-paragraph spans
     * and later we restore paragraph spans restoring paragraph invariants
     */
    private static class WrappedParagraphStyle {
        private static final Pattern NOT_WHITESPACE = Pattern.compile("\\S");
        private ParagraphStyle mTarget;

        WrappedParagraphStyle(ParagraphStyle target) {
            mTarget = target;
        }

        /**
         * Replace this span with it's target
         */
        void unwrapOn(Spannable spannable) {
            int start = spannable.getSpanStart(this);
            int end = spannable.getSpanEnd(this);

            // Move start to paragraph boundary
            // Based on checks in SpannableStringBuilder implementation
            // https://github.com/android/platform_frameworks_base/blob/d4a9b5bad7f2f4b1ea3234777e66e6d4e092e5d4/core/java/android/text/SpannableStringBuilder.java#L668
            if (start != 0 && start != spannable.length() && spannable.charAt(start - 1) != '\n') {
                start--;
            }

            // Move end to paragraph boundary
            if (end != 0 && end != spannable.length() && spannable.charAt(end - 1) != '\n') {
                end++;
            }

            // Set the span
            if (
                    // Not just empty space
                    NOT_WHITESPACE.matcher(spannable.subSequence(start, end)).find() &&
                            // No other ParagraphStyle spans in this place
                            spannable.getSpans(start, end, ParagraphStyle.class).length == 0
                    ) {
                spannable.setSpan(mTarget, start, end, Spanned.SPAN_PARAGRAPH);
            }
            spannable.removeSpan(this);
        }
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

    /**
     * Helper class to handle tags that {@link Html#fromHtml(String)} don't support by default
     *
     * Currently only recognizes 'li'
     */
    private static class MyTagHandler implements Html.TagHandler {
        private static void appendNewLineIfNotExists(Editable output) {
            int length = output.length();
            if (length != 0 && output.charAt(length - 1) != '\n') {
                output.append('\n');
            }
        }

        @Override
        public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
            if ("li".equals(tag)) {
                appendNewLineIfNotExists(output);
                if (opening) {
                    output.append("\u25CF ");
                }
            }
        }
    }
}
