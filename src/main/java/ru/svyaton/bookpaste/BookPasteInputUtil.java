package ru.svyaton.bookpaste;

//? if <26.3 {
import org.lwjgl.glfw.GLFW;
//?}
//? if >=26.1 {
/*import net.minecraft.client.Minecraft;*/
//?} else {
import net.minecraft.client.MinecraftClient;
//?}

public final class BookPasteInputUtil {
    private BookPasteInputUtil() {
    }
    //? if >=26.1 {
    /*public static boolean isPasteShortcutPressed(Minecraft client) {*/
    //?} else {
    public static boolean isPasteShortcutPressed(MinecraftClient client) {
    //?}
        if (client == null) {
            return false;
        }
        //? if >=26.3 {
        /*return (com.mojang.blaze3d.platform.InputConstants.isKeyDown(com.mojang.blaze3d.platform.InputConstants.KEY_LCONTROL)
                || com.mojang.blaze3d.platform.InputConstants.isKeyDown(com.mojang.blaze3d.platform.InputConstants.KEY_RCONTROL))
                && com.mojang.blaze3d.platform.InputConstants.isKeyDown(com.mojang.blaze3d.platform.InputConstants.KEY_V)
                || (com.mojang.blaze3d.platform.InputConstants.isKeyDown(com.mojang.blaze3d.platform.InputConstants.KEY_LSHIFT)
                || com.mojang.blaze3d.platform.InputConstants.isKeyDown(com.mojang.blaze3d.platform.InputConstants.KEY_RSHIFT))
                && com.mojang.blaze3d.platform.InputConstants.isKeyDown(com.mojang.blaze3d.platform.InputConstants.KEY_INSERT);*/
        //?} else {
        //? if >=26.1 {
        /*long handle = client.getWindow().handle();*/
        //?} else {
        long handle = client.getWindow().getHandle();
        //?}
        return (isControlDown(handle) && isKeyPressed(handle, GLFW.GLFW_KEY_V))
                || (isShiftDown(handle) && isKeyPressed(handle, GLFW.GLFW_KEY_INSERT));
        //?}
    }

    //? if <26.3 {

    private static boolean isControlDown(long handle) {
        return isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_CONTROL)
                || isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    private static boolean isShiftDown(long handle) {
        return isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_SHIFT)
                || isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    private static boolean isKeyPressed(long handle, int keyCode) {
        int state = GLFW.glfwGetKey(handle, keyCode);
        return state == GLFW.GLFW_PRESS || state == GLFW.GLFW_REPEAT;
    }
    //?}

}
