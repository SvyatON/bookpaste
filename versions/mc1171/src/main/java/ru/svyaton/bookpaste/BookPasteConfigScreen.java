package ru.svyaton.bookpaste;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public final class BookPasteConfigScreen extends Screen {
    private static final int MAX_MOUSE_BUTTON = 7;

    private final Screen parent;
    private PasteFormatMode formatMode;
    private ButtonWidget keyBindingButton;
    private boolean waitingForKey;
    private int waitingForKeyTicks;

    public BookPasteConfigScreen(Screen parent) {
        super(BookPasteTextCompat.translatable("text.bookpaste.config.title"));
        this.parent = parent;
        this.formatMode = BookPasteConfigManager.getConfig().formatMode();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int buttonWidth = 260;
        int buttonX = centerX - buttonWidth / 2;
        int centerButtonWidth = 126;

        this.addDrawableChild(new ButtonWidget(buttonX, this.height / 2 - 20, buttonWidth, 20, this.getModeButtonText(), button -> {
            this.formatMode = this.formatMode.next();
            button.setMessage(this.getModeButtonText());
        }));

        this.keyBindingButton = this.addDrawableChild(new ButtonWidget(buttonX, this.height / 2 + 18, buttonWidth, 20, this.getKeyBindingButtonText(), button -> {
            this.waitingForKey = true;
            this.waitingForKeyTicks = 0;
            button.setMessage(BookPasteTextCompat.translatable("text.bookpaste.config.hotkey.listening"));
        }));

        this.addDrawableChild(new ButtonWidget(centerX - 130, this.height / 2 + 46, centerButtonWidth, 20,
                BookPasteTextCompat.translatable("text.bookpaste.config.hotkey.reset"),
                button -> {
                    BookPasteHotkeys.resetToggleFormatModeKey(MinecraftClient.getInstance());
                    this.waitingForKey = false;
                    this.updateKeyBindingButtonText();
                }));

        this.addDrawableChild(new ButtonWidget(centerX + 4, this.height / 2 + 46, centerButtonWidth, 20,
                BookPasteTextCompat.translatable("text.bookpaste.config.hotkey.controls"),
                button -> MinecraftClient.getInstance().setScreen(BookPasteHotkeys.createControlsScreen(MinecraftClient.getInstance(), this))));

        this.addDrawableChild(new ButtonWidget(centerX - 102, this.height - 52, 100, 20,
                BookPasteTextCompat.translatable("gui.done"),
                button -> {
                    BookPasteConfigManager.getConfig().setFormatMode(this.formatMode);
                    BookPasteConfigManager.save();
                    MinecraftClient.getInstance().setScreen(this.parent);
                }));

        this.addDrawableChild(new ButtonWidget(centerX + 2, this.height - 52, 100, 20,
                BookPasteTextCompat.translatable("gui.cancel"),
                button -> MinecraftClient.getInstance().setScreen(this.parent)));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.bookpaste$drawCenteredText(matrices, this.title, 20, 0xFFFFFF);

        int y = this.height / 2 - 56;
        this.bookpaste$drawCenteredText(matrices, BookPasteTextCompat.translatable("text.bookpaste.config.mode.label"), y, 0xA0A0A0);

        this.bookpaste$drawCenteredText(matrices, BookPasteTextCompat.translatable("text.bookpaste.config.hotkey.label"), this.height / 2 + 40, 0xA0A0A0);

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.waitingForKey) {
            return;
        }

        this.waitingForKeyTicks++;
        if (this.waitingForKeyTicks < 3) {
            return;
        }

        long handle = MinecraftClient.getInstance().getWindow().getHandle();
        int pressedKey = -1;
        for (int keyCode = GLFW.GLFW_KEY_SPACE; keyCode <= GLFW.GLFW_KEY_LAST; keyCode++) {
            if (GLFW.glfwGetKey(handle, keyCode) != GLFW.GLFW_PRESS) {
                continue;
            }
            if (pressedKey != -1) {
                return;
            }
            pressedKey = keyCode;
        }

        if (pressedKey == -1) {
            int pressedMouseButton = -1;
            for (int button = 0; button <= MAX_MOUSE_BUTTON; button++) {
                if (GLFW.glfwGetMouseButton(handle, button) != GLFW.GLFW_PRESS) {
                    continue;
                }
                if (pressedMouseButton != -1) {
                    return;
                }
                pressedMouseButton = button;
            }

            if (pressedMouseButton == -1) {
                return;
            }

            this.handleMouseBindingChange(pressedMouseButton);
            return;
        }

        this.handleKeyBindingChange(pressedKey);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.waitingForKey) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        return this.handleKeyBindingChange(keyCode);
    }

    public void onClose() {
        this.waitingForKey = false;
        MinecraftClient.getInstance().setScreen(this.parent);
    }

    private Text getModeButtonText() {
        return BookPasteTextCompat.translatable("text.bookpaste.config.mode.button",
                BookPasteTextCompat.translatable("text.bookpaste.config.mode." + this.formatMode.id()));
    }

    private Text getKeyBindingButtonText() {
        return BookPasteTextCompat.translatable("text.bookpaste.config.hotkey.button",
                BookPasteHotkeys.TOGGLE_FORMAT_MODE.getBoundKeyLocalizedText());
    }

    private void updateKeyBindingButtonText() {
        if (this.keyBindingButton != null) {
            this.keyBindingButton.setMessage(this.getKeyBindingButtonText());
        }
    }

    private boolean handleKeyBindingChange(int keyCode) {
        if (keyCode == InputUtil.GLFW_KEY_ESCAPE) {
            this.waitingForKey = false;
            this.waitingForKeyTicks = 0;
            this.updateKeyBindingButtonText();
            return true;
        }

        BookPasteHotkeys.setToggleFormatModeKey(MinecraftClient.getInstance(), InputUtil.Type.KEYSYM.createFromCode(keyCode));
        this.waitingForKey = false;
        this.waitingForKeyTicks = 0;
        this.updateKeyBindingButtonText();
        return true;
    }

    private void handleMouseBindingChange(int button) {
        BookPasteHotkeys.setToggleFormatModeKey(MinecraftClient.getInstance(), InputUtil.Type.MOUSE.createFromCode(button));
        this.waitingForKey = false;
        this.waitingForKeyTicks = 0;
        this.updateKeyBindingButtonText();
    }

    private void bookpaste$drawCenteredText(MatrixStack matrices, Text text, int y, int color) {
        float x = (this.width - this.textRenderer.getWidth(text)) / 2.0F;
        this.textRenderer.drawWithShadow(matrices, text, x, y, color);
    }
}
