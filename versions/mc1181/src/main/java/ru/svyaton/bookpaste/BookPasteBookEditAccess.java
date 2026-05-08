package ru.svyaton.bookpaste;

import java.util.List;

import net.minecraft.entity.player.PlayerEntity;

public interface BookPasteBookEditAccess {
    List<String> bookpaste$getPages();

    int bookpaste$getCurrentPage();

    void bookpaste$setPagesAndCurrentPage(List<String> pages, int currentPage);

    PlayerEntity bookpaste$getPlayer();
}
