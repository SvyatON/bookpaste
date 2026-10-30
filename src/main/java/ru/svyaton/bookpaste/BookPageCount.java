package ru.svyaton.bookpaste;

//? if <26.1 {
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;

public final class BookPageCount {
    private BookPageCount() {
    }

    public static int count(BookUpdateC2SPacket packet) {
        //? if >=1.20.5 {
        return packet.pages().size();
        //?}
        //? if >=1.17 && <1.20.5 {
        /*return packet.getPages().size();*/
        //?}
        //? if <1.17 {
        /*//? if >=1.16.5 {
net.minecraft.nbt.NbtCompound tag = packet.getBook().getTag();
//?} else {
net.minecraft.nbt.CompoundTag tag = packet.getBook().getTag();
//?}
        return tag == null ? 0 : tag.getList("pages", 8).size();*/
        //?}
    }
}
//?}
