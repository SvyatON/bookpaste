package ru.svyaton.bookpaste;

import java.util.List;

import net.minecraft.world.entity.player.Player;

public interface BookPasteBookEditAccess {
    List<String> bookpaste$getPages();

    int bookpaste$getCurrentPage();

    void bookpaste$setPagesAndCurrentPage(List<String> pages, int currentPage);

    Player bookpaste$getPlayer();
}
