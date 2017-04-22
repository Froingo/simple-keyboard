/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rkr.simplekeyboard.inputmethod.latin.common;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import rkr.simplekeyboard.inputmethod.annotations.UsedForTesting;

public final class StringUtils {
    public static final int CAPITALIZE_NONE = 0;  // No caps, or mixed case
    public static final int CAPITALIZE_FIRST = 1; // First only
    public static final int CAPITALIZE_ALL = 2;   // All caps

    @NonNull
    private static final String EMPTY_STRING = "";

    private static final char CHAR_LINE_FEED = 0X000A;
    private static final char CHAR_VERTICAL_TAB = 0X000B;
    private static final char CHAR_FORM_FEED = 0X000C;
    private static final char CHAR_CARRIAGE_RETURN = 0X000D;
    private static final char CHAR_NEXT_LINE = 0X0085;
    private static final char CHAR_LINE_SEPARATOR = 0X2028;
    private static final char CHAR_PARAGRAPH_SEPARATOR = 0X2029;

    private StringUtils() {
        // This utility class is not publicly instantiable.
    }

    // Taken from android.text.TextUtils. We are extensively using this method in many places,
    // some of which don't have the android libraries available.
    /**
     * Returns true if the string is null or 0-length.
     * @param str the string to be examined
     * @return true if str is null or zero length
     */
    public static boolean isEmpty(@Nullable final CharSequence str) {
        return (str == null || str.length() == 0);
    }

    // Taken from android.text.TextUtils to cut the dependency to the Android framework.
    /**
     * Returns a string containing the tokens joined by delimiters.
     * @param delimiter the delimiter
     * @param tokens an array objects to be joined. Strings will be formed from
     *     the objects by calling object.toString().
     */
    @NonNull
    public static String join(@NonNull final CharSequence delimiter,
            @NonNull final Iterable<?> tokens) {
        final StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (final Object token: tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(token);
        }
        return sb.toString();
    }

    // Taken from android.text.TextUtils to cut the dependency to the Android framework.
    /**
     * Returns true if a and b are equal, including if they are both null.
     * <p><i>Note: In platform versions 1.1 and earlier, this method only worked well if
     * both the arguments were instances of String.</i></p>
     * @param a first CharSequence to check
     * @param b second CharSequence to check
     * @return true if a and b are equal
     */
    public static boolean equals(@Nullable final CharSequence a, @Nullable final CharSequence b) {
        if (a == b) {
            return true;
        }
        final int length;
        if (a != null && b != null && (length = a.length()) == b.length()) {
            if (a instanceof String && b instanceof String) {
                return a.equals(b);
            }
            for (int i = 0; i < length; i++) {
                if (a.charAt(i) != b.charAt(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static int codePointCount(@Nullable final CharSequence text) {
        if (isEmpty(text)) {
            return 0;
        }
        return Character.codePointCount(text, 0, text.length());
    }

    @NonNull
    public static String newSingleCodePointString(final int codePoint) {
        if (Character.charCount(codePoint) == 1) {
            // Optimization: avoid creating a temporary array for characters that are
            // represented by a single char value
            return String.valueOf((char) codePoint);
        }
        // For surrogate pair
        return new String(Character.toChars(codePoint));
    }

    public static boolean containsInArray(@NonNull final String text,
            @NonNull final String[] array) {
        for (final String element : array) {
            if (text.equals(element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Comma-Splittable Text is similar to Comma-Separated Values (CSV) but has much simpler syntax.
     * Unlike CSV, Comma-Splittable Text has no escaping mechanism, so that the text can't contain
     * a comma character in it.
     */
    @NonNull
    private static final String SEPARATOR_FOR_COMMA_SPLITTABLE_TEXT = ",";

    public static boolean containsInCommaSplittableText(@NonNull final String text,
            @Nullable final String extraValues) {
        if (isEmpty(extraValues)) {
            return false;
        }
        return containsInArray(text, extraValues.split(SEPARATOR_FOR_COMMA_SPLITTABLE_TEXT));
    }

    @NonNull
    public static String removeFromCommaSplittableTextIfExists(@NonNull final String text,
            @Nullable final String extraValues) {
        if (isEmpty(extraValues)) {
            return EMPTY_STRING;
        }
        final String[] elements = extraValues.split(SEPARATOR_FOR_COMMA_SPLITTABLE_TEXT);
        if (!containsInArray(text, elements)) {
            return extraValues;
        }
        final ArrayList<String> result = new ArrayList<>(elements.length - 1);
        for (final String element : elements) {
            if (!text.equals(element)) {
                result.add(element);
            }
        }
        return join(SEPARATOR_FOR_COMMA_SPLITTABLE_TEXT, result);
    }

    @NonNull
    public static String capitalizeFirstCodePoint(@NonNull final String s,
            @NonNull final Locale locale) {
        if (s.length() <= 1) {
            return s.toUpperCase(getLocaleUsedForToTitleCase(locale));
        }
        // Please refer to the comment below in
        // {@link #capitalizeFirstAndDowncaseRest(String,Locale)} as this has the same shortcomings
        final int cutoff = s.offsetByCodePoints(0, 1);
        return s.substring(0, cutoff).toUpperCase(getLocaleUsedForToTitleCase(locale))
                + s.substring(cutoff);
    }

    @NonNull
    public static int[] toCodePointArray(@NonNull final CharSequence charSequence) {
        return toCodePointArray(charSequence, 0, charSequence.length());
    }

    @NonNull
    private static final int[] EMPTY_CODEPOINTS = {};

    /**
     * Converts a range of a string to an array of code points.
     * @param charSequence the source string.
     * @param startIndex the start index inside the string in java chars, inclusive.
     * @param endIndex the end index inside the string in java chars, exclusive.
     * @return a new array of code points. At most endIndex - startIndex, but possibly less.
     */
    @NonNull
    public static int[] toCodePointArray(@NonNull final CharSequence charSequence,
            final int startIndex, final int endIndex) {
        final int length = charSequence.length();
        if (length <= 0) {
            return EMPTY_CODEPOINTS;
        }
        final int[] codePoints =
                new int[Character.codePointCount(charSequence, startIndex, endIndex)];
        copyCodePointsAndReturnCodePointCount(codePoints, charSequence, startIndex, endIndex,
                false /* downCase */);
        return codePoints;
    }

    /**
     * Copies the codepoints in a CharSequence to an int array.
     *
     * This method assumes there is enough space in the array to store the code points. The size
     * can be measured with Character#codePointCount(CharSequence, int, int) before passing to this
     * method. If the int array is too small, an ArrayIndexOutOfBoundsException will be thrown.
     * Also, this method makes no effort to be thread-safe. Do not modify the CharSequence while
     * this method is running, or the behavior is undefined.
     * This method can optionally downcase code points before copying them, but it pays no attention
     * to locale while doing so.
     *
     * @param destination the int array.
     * @param charSequence the CharSequence.
     * @param startIndex the start index inside the string in java chars, inclusive.
     * @param endIndex the end index inside the string in java chars, exclusive.
     * @param downCase if this is true, code points will be downcased before being copied.
     * @return the number of copied code points.
     */
    public static int copyCodePointsAndReturnCodePointCount(@NonNull final int[] destination,
            @NonNull final CharSequence charSequence, final int startIndex, final int endIndex,
            final boolean downCase) {
        int destIndex = 0;
        for (int index = startIndex; index < endIndex;
                index = Character.offsetByCodePoints(charSequence, index, 1)) {
            final int codePoint = Character.codePointAt(charSequence, index);
            // TODO: stop using this, as it's not aware of the locale and does not always do
            // the right thing.
            destination[destIndex] = downCase ? Character.toLowerCase(codePoint) : codePoint;
            destIndex++;
        }
        return destIndex;
    }

    @NonNull
    public static int[] toSortedCodePointArray(@NonNull final String string) {
        final int[] codePoints = toCodePointArray(string);
        Arrays.sort(codePoints);
        return codePoints;
    }

    public static boolean isIdenticalAfterUpcase(@NonNull final String text) {
        final int length = text.length();
        int i = 0;
        while (i < length) {
            final int codePoint = text.codePointAt(i);
            if (Character.isLetter(codePoint) && !Character.isUpperCase(codePoint)) {
                return false;
            }
            i += Character.charCount(codePoint);
        }
        return true;
    }

    public static boolean isIdenticalAfterDowncase(@NonNull final String text) {
        final int length = text.length();
        int i = 0;
        while (i < length) {
            final int codePoint = text.codePointAt(i);
            if (Character.isLetter(codePoint) && !Character.isLowerCase(codePoint)) {
                return false;
            }
            i += Character.charCount(codePoint);
        }
        return true;
    }

    public static boolean isIdenticalAfterCapitalizeEachWord(@NonNull final String text,
            @NonNull final int[] sortedSeparators) {
        boolean needsCapsNext = true;
        final int len = text.length();
        for (int i = 0; i < len; i = text.offsetByCodePoints(i, 1)) {
            final int codePoint = text.codePointAt(i);
            if (Character.isLetter(codePoint)) {
                if ((needsCapsNext && !Character.isUpperCase(codePoint))
                        || (!needsCapsNext && !Character.isLowerCase(codePoint))) {
                    return false;
                }
            }
            // We need a capital letter next if this is a separator.
            needsCapsNext = (Arrays.binarySearch(sortedSeparators, codePoint) >= 0);
        }
        return true;
    }

    // TODO: like capitalizeFirst*, this does not work perfectly for Dutch because of the IJ digraph
    // which should be capitalized together in *some* cases.
    @NonNull
    public static String capitalizeEachWord(@NonNull final String text,
            @NonNull final int[] sortedSeparators, @NonNull final Locale locale) {
        final StringBuilder builder = new StringBuilder();
        boolean needsCapsNext = true;
        final int len = text.length();
        for (int i = 0; i < len; i = text.offsetByCodePoints(i, 1)) {
            final String nextChar = text.substring(i, text.offsetByCodePoints(i, 1));
            if (needsCapsNext) {
                builder.append(nextChar.toUpperCase(locale));
            } else {
                builder.append(nextChar.toLowerCase(locale));
            }
            // We need a capital letter next if this is a separator.
            needsCapsNext = (Arrays.binarySearch(sortedSeparators, nextChar.codePointAt(0)) >= 0);
        }
        return builder.toString();
    }

    /**
     * Approximates whether the text before the cursor looks like a URL.
     *
     * This is not foolproof, but it should work well in the practice.
     * Essentially it walks backward from the cursor until it finds something that's not a letter,
     * digit, or common URL symbol like underscore. If it hasn't found a period yet, then it
     * does not look like a URL.
     * If the text:
     * - starts with www and contains a period
     * - starts with a slash preceded by either a slash, whitespace, or start-of-string
     * Then it looks like a URL and we return true. Otherwise, we return false.
     *
     * Note: this method is called quite often, and should be fast.
     *
     * TODO: This will return that "abc./def" and ".abc/def" look like URLs to keep down the
     * code complexity, but ideally it should not. It's acceptable for now.
     */
    public static boolean lastPartLooksLikeURL(@NonNull final CharSequence text) {
        int i = text.length();
        if (0 == i) {
            return false;
        }
        int wCount = 0;
        int slashCount = 0;
        boolean hasSlash = false;
        boolean hasPeriod = false;
        int codePoint = 0;
        while (i > 0) {
            codePoint = Character.codePointBefore(text, i);
            if (codePoint < Constants.CODE_PERIOD || codePoint > 'z') {
                // Handwavy heuristic to see if that's a URL character. Anything between period
                // and z. This includes all lower- and upper-case ascii letters, period,
                // underscore, arrobase, question mark, equal sign. It excludes spaces, exclamation
                // marks, double quotes...
                // Anything that's not a URL-like character causes us to break from here and
                // evaluate normally.
                break;
            }
            if (Constants.CODE_PERIOD == codePoint) {
                hasPeriod = true;
            }
            if (Constants.CODE_SLASH == codePoint) {
                hasSlash = true;
                if (2 == ++slashCount) {
                    return true;
                }
            } else {
                slashCount = 0;
            }
            if ('w' == codePoint) {
                ++wCount;
            } else {
                wCount = 0;
            }
            i = Character.offsetByCodePoints(text, i, -1);
        }
        // End of the text run.
        // If it starts with www and includes a period, then it looks like a URL.
        if (wCount >= 3 && hasPeriod) {
            return true;
        }
        // If it starts with a slash, and the code point before is whitespace, it looks like an URL.
        if (1 == slashCount && (0 == i || Character.isWhitespace(codePoint))) {
            return true;
        }
        // If it has both a period and a slash, it looks like an URL.
        if (hasPeriod && hasSlash) {
            return true;
        }
        // Otherwise, it doesn't look like an URL.
        return false;
    }

    /**
     * Examines the string and returns whether we're inside a double quote.
     *
     * This is used to decide whether we should put an automatic space before or after a double
     * quote character. If we're inside a quotation, then we want to close it, so we want a space
     * after and not before. Otherwise, we want to open the quotation, so we want a space before
     * and not after. Exception: after a digit, we never want a space because the "inch" or
     * "minutes" use cases is dominant after digits.
     * In the practice, we determine whether we are in a quotation or not by finding the previous
     * double quote character, and looking at whether it's followed by whitespace. If so, that
     * was a closing quotation mark, so we're not inside a double quote. If it's not followed
     * by whitespace, then it was an opening quotation mark, and we're inside a quotation.
     *
     * @param text the text to examine.
     * @return whether we're inside a double quote.
     */
    public static boolean isInsideDoubleQuoteOrAfterDigit(@NonNull final CharSequence text) {
        int i = text.length();
        if (0 == i) {
            return false;
        }
        int codePoint = Character.codePointBefore(text, i);
        if (Character.isDigit(codePoint)) {
            return true;
        }
        int prevCodePoint = 0;
        while (i > 0) {
            codePoint = Character.codePointBefore(text, i);
            if (Constants.CODE_DOUBLE_QUOTE == codePoint) {
                // If we see a double quote followed by whitespace, then that
                // was a closing quote.
                if (Character.isWhitespace(prevCodePoint)) {
                    return false;
                }
            }
            if (Character.isWhitespace(codePoint) && Constants.CODE_DOUBLE_QUOTE == prevCodePoint) {
                // If we see a double quote preceded by whitespace, then that
                // was an opening quote. No need to continue seeking.
                return true;
            }
            i -= Character.charCount(codePoint);
            prevCodePoint = codePoint;
        }
        // We reached the start of text. If the first char is a double quote, then we're inside
        // a double quote. Otherwise we're not.
        return Constants.CODE_DOUBLE_QUOTE == codePoint;
    }

    private static final String LANGUAGE_GREEK = "el";

    @NonNull
    private static Locale getLocaleUsedForToTitleCase(@NonNull final Locale locale) {
        // In Greek locale {@link String#toUpperCase(Locale)} eliminates accents from its result.
        // In order to get accented upper case letter, {@link Locale#ROOT} should be used.
        if (LANGUAGE_GREEK.equals(locale.getLanguage())) {
            return Locale.ROOT;
        }
        return locale;
    }

    @Nullable
    public static String toTitleCaseOfKeyLabel(@Nullable final String label,
            @NonNull final Locale locale) {
        if (label == null) {
            return label;
        }
        return label.toUpperCase(getLocaleUsedForToTitleCase(locale));
    }

    public static int toTitleCaseOfKeyCode(final int code, @NonNull final Locale locale) {
        if (!Constants.isLetterCode(code)) {
            return code;
        }
        final String label = newSingleCodePointString(code);
        final String titleCaseLabel = toTitleCaseOfKeyLabel(label, locale);
        return codePointCount(titleCaseLabel) == 1
                ? titleCaseLabel.codePointAt(0) : Constants.CODE_UNSPECIFIED;
    }

    public static int getTrailingSingleQuotesCount(@NonNull final CharSequence charSequence) {
        final int lastIndex = charSequence.length() - 1;
        int i = lastIndex;
        while (i >= 0 && charSequence.charAt(i) == Constants.CODE_SINGLE_QUOTE) {
            --i;
        }
        return lastIndex - i;
    }
}
