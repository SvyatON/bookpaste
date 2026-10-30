package ru.svyaton.bookpaste;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.function.Function;

public final class BookPasteConfigLayout {
    public final int width;
    public final int left;
    public final int halfWidth;
    public final int fieldX;
    public final int fieldWidth;
    public final int pagesY;
    public final int limitY;
    public final int modeY;
    public final int keyY;
    public final int actionsY;
    public final int footerY;
    public final List<Label> labels = new ArrayList<>();

    public BookPasteConfigLayout(int screenWidth, int screenHeight, boolean remote,
            Function<String, String> text, ToIntFunction<String> measure) {
        this.width = Math.min(360, screenWidth - 24);
        this.left = (screenWidth - this.width) / 2;
        this.halfWidth = (this.width - 8) / 2;
        this.fieldWidth = Math.min(100, this.width / 3);
        this.fieldX = this.left + this.width - this.fieldWidth;
        this.footerY = screenHeight - 28;
        List<String> heading = wrap(text.apply("pages.heading"), this.width, measure);
        List<String> help = wrap(text.apply("pages.help_local"), this.width, measure);
        help.addAll(wrap(text.apply("pages.help_server"), this.width, measure));
        List<String> mode = wrap(text.apply("mode.label"), this.width, measure);
        List<String> key = wrap(text.apply("hotkey.label"), this.width, measure);
        int fullHeight = (heading.size() + help.size() + mode.size() + key.size()) * 12 + 158;
        boolean compact = fullHeight > this.footerY - 40;
        if (compact) help = wrap(text.apply(remote ? "pages.help_server" : "pages.help_local"), this.width, measure);
        int height = compact ? (heading.size() + help.size()) * 12 + 136 : fullHeight;
        boolean minimal = height > screenHeight - 64;
        int gap = minimal ? Math.min(6, Math.max(2, (screenHeight - 164) / 4)) : 6;
        int step = 20 + gap;
        if (minimal) {
            compact = true;
            height = 100 + gap * 4;
        }
        int y = Math.max(28, 28 + (this.footerY - 36 - height) / 2);
        int center = screenWidth / 2;
        if (!minimal) {
            y = add(heading, center, y, 0xFFFFFFFF) + 4;
            y = add(help, center, y, 0xFFA0A0A0) + 8;
        }
        this.pagesY = y;
        this.limitY = y + step;
        List<String> limit = wrap(text.apply(remote ? "max_pages.server_label" : "max_pages.label"),
                this.width - this.fieldWidth - 12, measure);
        add(limit, this.left + (this.width - this.fieldWidth - 12) / 2,
                this.limitY + (20 - limit.size() * 10) / 2, 0xFFA0A0A0, 10);
        y = this.limitY + step;
        if (!compact) y = add(mode, center, y + 10, 0xFFA0A0A0) + 2;
        this.modeY = y;
        y += step;
        if (!compact) y = add(key, center, y + 6, 0xFFA0A0A0) + 2;
        this.keyY = y;
        this.actionsY = y + step;
    }

    private int add(List<String> lines, int x, int y, int color) {
        return add(lines, x, y, color, 12);
    }

    private int add(List<String> lines, int x, int y, int color, int lineHeight) {
        for (String line : lines) {
            this.labels.add(new Label(line, x, y, color));
            y += lineHeight;
        }
        return y;
    }

    private static List<String> wrap(String text, int width, ToIntFunction<String> measure) {
        List<String> lines = new ArrayList<>();
        String remaining = text;
        while (!remaining.isEmpty()) {
            int end = remaining.length();
            if (measure.applyAsInt(remaining) > width) {
                end = 0;
                int space = -1;
                while (end < remaining.length()) {
                    int next = end + Character.charCount(remaining.codePointAt(end));
                    if (measure.applyAsInt(remaining.substring(0, next)) > width && end > 0) break;
                    if (Character.isWhitespace(remaining.charAt(end))) space = end;
                    end = next;
                }
                if (space > 0 && end < remaining.length()) end = space;
            }
            lines.add(remaining.substring(0, end).trim());
            remaining = remaining.substring(end).trim();
        }
        return lines;
    }

    public static final class Label {
        public final String text;
        public final int x;
        public final int y;
        public final int color;

        private Label(String text, int x, int y, int color) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }
}
