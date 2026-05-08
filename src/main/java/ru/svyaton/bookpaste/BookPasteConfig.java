package ru.svyaton.bookpaste;

public final class BookPasteConfig {
    private PasteFormatMode formatMode = PasteFormatMode.SMART_FILL;

    public PasteFormatMode formatMode() {
        return this.formatMode;
    }

    public void setFormatMode(PasteFormatMode formatMode) {
        this.formatMode = formatMode;
    }
}
