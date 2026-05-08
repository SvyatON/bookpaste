package ru.svyaton.bookpaste;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public final class BookPasteTextCompat {
    private BookPasteTextCompat() {
    }

    public static Text literal(String text) {
        return new LiteralText(text);
    }

    public static Text translatable(String key, Object... args) {
        return new TranslatableText(key, args);
    }
}
