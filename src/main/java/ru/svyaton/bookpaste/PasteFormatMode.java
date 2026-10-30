package ru.svyaton.bookpaste;

public enum PasteFormatMode {
    SMART_FILL("smart_fill"),
    WHOLE_PARAGRAPHS("whole_paragraphs");

    private final String id;

    PasteFormatMode(String id) {
        this.id = id;
    }

    public String id() {
        return this.id;
    }

    public PasteFormatMode next() {
        PasteFormatMode[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }

}
