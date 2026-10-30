package ru.svyaton.bookpaste;

public final class BookPasteConfig {
    private volatile boolean customBookPages;
    private volatile int maxPages;
    private int lastCustomMaxPages;
    private PasteFormatMode formatMode = PasteFormatMode.SMART_FILL;

    public int lastCustomMaxPages() {
        return this.lastCustomMaxPages;
    }

    public void setLastCustomMaxPages(int pages) {
        if (pages < 0) throw new IllegalArgumentException("lastCustomMaxPages must be non-negative");
        this.lastCustomMaxPages = pages;
    }

    public int maxPages() {
        return this.maxPages;
    }

    public void setMaxPages(int maxPages) {
        if (maxPages < 0) {
            throw new IllegalArgumentException("maxPages must be non-negative");
        }
        this.maxPages = maxPages;
    }

    public boolean customBookPages() {
        return this.customBookPages;
    }

    public void setCustomBookPages(boolean customBookPages) {
        this.customBookPages = customBookPages;
    }

    public PasteFormatMode formatMode() {
        return this.formatMode;
    }

    public void setFormatMode(PasteFormatMode formatMode) {
        this.formatMode = formatMode;
    }
}
