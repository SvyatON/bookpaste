package ru.svyaton.bookpaste;

import java.util.List;
//? if >=26.1 {
/*import net.minecraft.world.entity.player.Player;*/
//?} else {
import net.minecraft.entity.player.PlayerEntity;
//?}

public interface BookPasteBookEditAccess {
    List<String> bookpaste$getPages();

    int bookpaste$getCurrentPage();

    void bookpaste$setPagesAndCurrentPage(List<String> pages, int currentPage);
    //? if >=26.1 {
    /*Player bookpaste$getPlayer();*/
    //?} else {
    PlayerEntity bookpaste$getPlayer();
    //?}
}
