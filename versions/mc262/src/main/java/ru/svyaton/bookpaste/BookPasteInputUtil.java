package ru.svyaton.bookpaste;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;

public final class BookPasteInputUtil {
    private BookPasteInputUtil() {
    }

    public static boolean isPasteShortcutPressed(Minecraft client) {
        if (client == null) {
            return false;
        }

        long handle = client.getWindow().handle();
        return (isControlDown(handle) && isKeyPressed(handle, GLFW.GLFW_KEY_V))
                || (isShiftDown(handle) && isKeyPressed(handle, GLFW.GLFW_KEY_INSERT));
    }

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
}
