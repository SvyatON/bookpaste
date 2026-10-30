package ru.svyaton.bookpaste;

public final class ServerBookCapabilities {
    private static volatile Object connection;
    private static volatile int maxPages = BookPasteBookLimits.MAX_PAGE_COUNT;

    private ServerBookCapabilities() {
    }

    public static void reset() {
        connection = null;
        maxPages = BookPasteBookLimits.MAX_PAGE_COUNT;
    }

    public static void accept(Object source, int protocol, int pages) {
        if (source == null || protocol != 1 || pages < 1) return;
        maxPages = pages;
        connection = source;
    }

    public static int pageLimit(Object currentConnection) {
        return currentConnection != null && currentConnection == connection
                ? maxPages : BookPasteBookLimits.MAX_PAGE_COUNT;
    }
}
