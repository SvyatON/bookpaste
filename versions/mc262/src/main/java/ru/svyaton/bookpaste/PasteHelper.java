package ru.svyaton.bookpaste;

import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public final class PasteHelper {
    private PasteHelper() {
    }

    public static String normalizeClipboard(String text) {
        return text.replace("\r\n", "\n").replace('\r', '\n');
    }

    public static PageSlice buildPage(String base, String remaining, PageFitter fitter, boolean finalBookPage) {
        if (remaining.isEmpty()) {
            return new PageSlice(base, "");
        }

        if (BookPasteConfigManager.getConfig().formatMode() == PasteFormatMode.WHOLE_PARAGRAPHS) {
            PageSlice paragraphSlice = buildWholeParagraphPage(base, remaining, fitter, finalBookPage);
            if (paragraphSlice != null) {
                return paragraphSlice;
            }
        }

        int maxAppend = findMaxAppend(base, remaining, fitter);
        if (maxAppend <= 0) {
            return new PageSlice(base, remaining);
        }

        int preferredCut = chooseCutIndex(remaining, maxAppend, finalBookPage);
        String addition = trimRight(remaining.substring(0, preferredCut));
        if (addition.isEmpty()) {
            addition = remaining.substring(0, maxAppend);
            preferredCut = maxAppend;
        }

        return new PageSlice(base + addition, trimLeft(remaining.substring(preferredCut)));
    }

    private static PageSlice buildWholeParagraphPage(String base, String remaining, PageFitter fitter, boolean finalBookPage) {
        int maxAppend = findMaxAppend(base, remaining, fitter);
        if (maxAppend <= 0) {
            return new PageSlice(base, remaining);
        }

        if (maxAppend >= remaining.length()) {
            return new PageSlice(base + trimRight(remaining), "");
        }

        int paragraphCut = findLastBoundary(remaining, "\n\n", maxAppend, 0.0D);
        if (paragraphCut > 0) {
            String addition = trimRight(remaining.substring(0, paragraphCut));
            if (!addition.isEmpty()) {
                return new PageSlice(base + addition, trimLeft(remaining.substring(paragraphCut)));
            }
        }

        if (!base.isEmpty()) {
            return new PageSlice(base, remaining);
        }

        return buildSmartPage(base, remaining, fitter, finalBookPage, maxAppend);
    }

    private static PageSlice buildSmartPage(String base, String remaining, PageFitter fitter, boolean finalBookPage, int maxAppend) {
        int preferredCut = chooseCutIndex(remaining, maxAppend, finalBookPage);
        String addition = trimRight(remaining.substring(0, preferredCut));
        if (addition.isEmpty()) {
            addition = remaining.substring(0, maxAppend);
            preferredCut = maxAppend;
        }

        return new PageSlice(base + addition, trimLeft(remaining.substring(preferredCut)));
    }

    public static Component createStatusMessage(int pagesUsed, String remaining, PageFitter fitter) {
        String languageCode = getLanguageCode();
        if (remaining.isEmpty()) {
            return Component.literal(I18n.get("text.bookpaste.done", pagesUsed,
                    translateCountWord("page", pagesUsed, languageCode)));
        }

        int booksNeeded = Math.max(1, estimateBooksNeeded(remaining, fitter));
        if (pagesUsed == 0) {
            return Component.literal(I18n.get("text.bookpaste.nospace", booksNeeded,
                    translateCountWord("book", booksNeeded, languageCode)));
        }

        return Component.literal(I18n.get("text.bookpaste.partial", remaining.length(), booksNeeded,
                translateCountWord("book", booksNeeded, languageCode)));
    }

    public static Component createSignStatusMessage(int linesUsed, String remaining, PageFitter fitter) {
        if (remaining == null || remaining.isEmpty()) {
            int shownLines = Math.max(1, linesUsed);
            return Component.literal(I18n.get("text.bookpaste.sign.done",
                    shownLines,
                    translateCountWord("line", shownLines, getLanguageCode())));
        }

        int signsNeeded = estimateSignsNeeded(remaining, fitter);
        if (linesUsed <= 0) {
            return Component.literal(I18n.get("text.bookpaste.sign.nospace",
                    signsNeeded,
                    translateCountWord("sign", signsNeeded, getLanguageCode())));
        }

        return Component.literal(I18n.get("text.bookpaste.sign.partial",
                remaining.length(),
                signsNeeded,
                translateCountWord("sign", signsNeeded, getLanguageCode())));
    }

    public static int estimateSignsNeeded(String remaining, PageFitter fitter) {
        if (remaining == null || remaining.isEmpty()) {
            return 0;
        }

        String left = normalizeClipboard(remaining);
        int usedLines = 0;
        int lineInSign = 0;

        while (!left.isEmpty()) {
            PageSlice slice = buildSignLine("", left, fitter, lineInSign == 3);
            if (slice.pageText().isEmpty() && slice.remainingText().equals(left)) {
                return Math.max(1, (usedLines + 3) / 4 + 1);
            }

            left = slice.remainingText();
            usedLines++;
            lineInSign = (lineInSign + 1) % 4;
        }

        return Math.max(1, (usedLines + 3) / 4);
    }

    public static PageSlice buildSignLine(String base, String remaining, PageFitter fitter, boolean finalSignLine) {
        String safeBase = base == null ? "" : base;
        String left = trimLeft(remaining == null ? "" : remaining);
        if (left.isEmpty()) {
            return new PageSlice(safeBase, "");
        }

        int hardBreak = left.indexOf('\n');
        int segmentLength = hardBreak >= 0 ? hardBreak : left.length();
        String segment = left.substring(0, segmentLength);

        if (segment.isEmpty() && hardBreak == 0) {
            return new PageSlice(safeBase, trimLeft(left.substring(1)));
        }

        int maxAppend = findMaxAppend(safeBase, segment, fitter);
        if (maxAppend <= 0) {
            return new PageSlice(safeBase, left);
        }

        int cut = maxAppend >= segment.length()
                ? segment.length()
                : chooseSignCutIndex(segment, maxAppend, finalSignLine);
        String addition = trimRight(segment.substring(0, cut));
        if (addition.isEmpty()) {
            cut = maxAppend;
            addition = segment.substring(0, cut);
        }

        int remainingIndex = cut;
        if (hardBreak >= 0 && cut >= segmentLength) {
            remainingIndex = hardBreak + 1;
        }

        return new PageSlice(safeBase + addition, trimLeft(left.substring(remainingIndex)));
    }

    private static int chooseSignCutIndex(String text, int maxAppend, boolean finalSignLine) {
        if (maxAppend <= 0 || maxAppend >= text.length()) {
            return Math.max(0, Math.min(maxAppend, text.length()));
        }

        int paragraphBreak = text.lastIndexOf('\n', maxAppend - 1);
        if (paragraphBreak > 0) {
            return paragraphBreak + 1;
        }

        int punctuation = findSoftPunctuationBoundary(text, maxAppend, 0.45D);
        if (punctuation > 0) {
            return punctuation;
        }

        int sentence = findSentenceBoundary(text, maxAppend, 0.5D);
        if (sentence > 0) {
            return sentence;
        }

        int whitespace = findWhitespaceBoundary(text, maxAppend, finalSignLine ? 0.25D : 0.65D);
        if (whitespace > 0) {
            return whitespace;
        }

        return maxAppend;
    }
    public static int estimateBooksNeeded(String remaining, PageFitter fitter) {
        String left = normalizeClipboard(remaining);
        if (left.isEmpty()) {
            return 0;
        }

        int usedPages = 0;
        int pageInBook = 0;

        while (!left.isEmpty()) {
            PageSlice slice = buildPage("", left, fitter, pageInBook == BookPasteBookLimits.MAX_PAGE_COUNT - 1);
            if (slice.pageText().isEmpty() && slice.remainingText().equals(left)) {
                return 1;
            }

            left = slice.remainingText();
            usedPages++;
            pageInBook = (pageInBook + 1) % BookPasteBookLimits.MAX_PAGE_COUNT;
        }

        return Math.max(1, (usedPages + BookPasteBookLimits.MAX_PAGE_COUNT - 1) / BookPasteBookLimits.MAX_PAGE_COUNT);
    }

    private static int findMaxAppend(String base, String remaining, PageFitter fitter) {
        int low = 0;
        int high = remaining.length();

        while (low < high) {
            int mid = (low + high + 1) / 2;
            String candidate = base + remaining.substring(0, mid);
            if (fitter.fits(candidate)) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }

        return low;
    }

    private static int chooseCutIndex(String remaining, int maxAppend, boolean finalBookPage) {
        if (maxAppend >= remaining.length()) {
            return remaining.length();
        }

        if (finalBookPage) {
            int paragraphCut = findLastBoundary(remaining, "\n\n", maxAppend, 0.3D);
            if (paragraphCut > 0) {
                return paragraphCut;
            }

            int lineCut = findLastBoundary(remaining, "\n", maxAppend, 0.45D);
            if (lineCut > 0) {
                return lineCut;
            }

            int strongSentenceCut = findSentenceBoundary(remaining, maxAppend, 0.45D);
            if (strongSentenceCut > 0) {
                return strongSentenceCut;
            }

            int softPunctuationCut = findSoftPunctuationBoundary(remaining, maxAppend, 0.6D);
            if (softPunctuationCut > 0) {
                return softPunctuationCut;
            }

            int whitespaceCut = findWhitespaceBoundary(remaining, maxAppend, 0.78D);
            if (whitespaceCut > 0) {
                return whitespaceCut;
            }
        }

        int paragraphCut = findLastBoundary(remaining, "\n\n", maxAppend, 0.7D);
        if (paragraphCut > 0) {
            return paragraphCut;
        }

        int lineCut = findLastBoundary(remaining, "\n", maxAppend, 0.8D);
        if (lineCut > 0) {
            return lineCut;
        }

        int sentenceCut = findSentenceBoundary(remaining, maxAppend, 0.85D);
        if (sentenceCut > 0) {
            return sentenceCut;
        }

        int whitespaceCut = findWhitespaceBoundary(remaining, maxAppend, 0.92D);
        if (whitespaceCut > 0) {
            return whitespaceCut;
        }

        return maxAppend;
    }

    private static int findSoftPunctuationBoundary(String text, int maxIndex, double minFillRatio) {
        int minIndex = Math.max(1, (int)Math.floor(maxIndex * minFillRatio));
        for (int i = Math.min(maxIndex - 1, text.length() - 1); i >= minIndex; i--) {
            char current = text.charAt(i);
            if (isSoftPunctuationBoundaryChar(current)) {
                return i + 1;
            }

            if (isClosingChar(current) && i > 0 && isSoftPunctuationBoundaryChar(text.charAt(i - 1))) {
                return i + 1;
            }
        }
        return -1;
    }

    private static boolean isSoftPunctuationBoundaryChar(char character) {
        return character == ';' || character == ':' || character == '\u2014';
    }

    private static boolean isClosingChar(char character) {
        return character == '"' || character == '\'' || character == ')' || character == ']'
                || character == '}' || character == '\u00BB';
    }

    private static int findLastBoundary(String text, String token, int maxIndex, double minFillRatio) {
        int minIndex = Math.max(1, (int)Math.floor(maxIndex * minFillRatio));
        int boundary = text.lastIndexOf(token, Math.min(maxIndex - 1, text.length() - 1));
        return boundary >= minIndex ? boundary : -1;
    }

    private static int findSentenceBoundary(String text, int maxIndex, double minFillRatio) {
        int minIndex = Math.max(1, (int)Math.floor(maxIndex * minFillRatio));
        for (int i = Math.min(maxIndex - 1, text.length() - 1); i >= minIndex; i--) {
            char current = text.charAt(i);
            if (current == '.' || current == '!' || current == '?' || current == '\u2026') {
                return i + 1;
            }

            if (isClosingChar(current) && i > 0) {
                char previous = text.charAt(i - 1);
                if (previous == '.' || previous == '!' || previous == '?' || previous == '\u2026') {
                    return i + 1;
                }
            }
        }
        return -1;
    }

    private static int findWhitespaceBoundary(String text, int maxIndex, double minFillRatio) {
        int minIndex = Math.max(1, (int)Math.floor(maxIndex * minFillRatio));
        for (int i = Math.min(maxIndex - 1, text.length() - 1); i >= minIndex; i--) {
            if (Character.isWhitespace(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private static String trimLeft(String text) {
        int index = 0;
        while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
            index++;
        }
        return text.substring(index);
    }

    private static String trimRight(String text) {
        int index = text.length();
        while (index > 0 && Character.isWhitespace(text.charAt(index - 1))) {
            index--;
        }
        return text.substring(0, index);
    }

    private static String getLanguageCode() {
        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return "en_us";
        }
        return client.getLanguageManager().getSelected();
    }

    private static String translateCountWord(String noun, int count, String languageCode) {
        String pluralForm = getPluralForm(count, languageCode);
        String key = "text.bookpaste.unit." + noun + "." + pluralForm;
        String value = I18n.get(key);
        if (value.equals(key)) {
            key = "text.bookpaste.unit." + noun + ".other";
        }
        return I18n.get(key);
    }

    private static String getPluralForm(int count, String languageCode) {
        int value = Math.abs(count);
        String normalizedLanguage = languageCode.toLowerCase(Locale.ROOT);
        if (normalizedLanguage.startsWith("ru") || normalizedLanguage.startsWith("uk")) {
            int mod10 = value % 10;
            int mod100 = value % 100;
            if (mod10 == 1 && mod100 != 11) {
                return "one";
            }
            if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14)) {
                return "few";
            }
            return "many";
        }

        return value == 1 ? "one" : "other";
    }

    @FunctionalInterface
    public interface PageFitter {
        boolean fits(String candidate);
    }

    public record PageSlice(String pageText, String remainingText) {
    }
}

