package ru.svyaton.bookpaste;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

public final class BookPasteConfigScreen extends Screen {
    private static final int MAX_MOUSE_BUTTON = 7;
    private final Screen parent;
    private PasteFormatMode formatMode;
    private ButtonWidget keyBindingButton;
    private boolean waitingForKey;
    private int waitingForKeyTicks;

    public BookPasteConfigScreen(Screen parent) {
        super(Text.translatable("text.bookpaste.config.title"));
        this.parent = parent;
        this.formatMode = BookPasteConfigManager.getConfig().formatMode();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int buttonWidth = 260;
        int buttonX = centerX - buttonWidth / 2;
        int centerButtonWidth = 126;

        this.addDrawableChild(ButtonWidget.builder(this.getModeButtonText(), button -> {
            this.formatMode = this.formatMode.next();
            button.setMessage(this.getModeButtonText());
        }).dimensions(buttonX, this.height / 2 - 20, buttonWidth, 20).build());

        this.keyBindingButton = this.addDrawableChild(ButtonWidget.builder(this.getKeyBindingButtonText(), button -> {
            this.waitingForKey = true;
            this.waitingForKeyTicks = 0;
            button.setMessage(Text.translatable("text.bookpaste.config.hotkey.listening"));
        }).dimensions(buttonX, this.height / 2 + 18, buttonWidth, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("text.bookpaste.config.hotkey.reset"), button -> {
            BookPasteHotkeys.resetToggleFormatModeKey(MinecraftClient.getInstance());
            this.waitingForKey = false;
            this.updateKeyBindingButtonText();
        }).dimensions(centerX - 130, this.height / 2 + 46, centerButtonWidth, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("text.bookpaste.config.hotkey.controls"), button ->
                MinecraftClient.getInstance().setScreen(BookPasteHotkeys.createControlsScreen(MinecraftClient.getInstance(), this))
        ).dimensions(centerX + 4, this.height / 2 + 46, centerButtonWidth, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> {
            BookPasteConfigManager.getConfig().setFormatMode(this.formatMode);
            BookPasteConfigManager.save();
            MinecraftClient.getInstance().setScreen(this.parent);
        }).dimensions(centerX - 102, this.height - 52, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"), button ->
                MinecraftClient.getInstance().setScreen(this.parent)
        ).dimensions(centerX + 2, this.height - 52, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);

        int y = this.height / 2 - 56;
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("text.bookpaste.config.mode.label"),
                this.width / 2, y, 0xA0A0A0);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("text.bookpaste.config.hotkey.label"),
                this.width / 2, this.height / 2 - 48 + 88, 0xA0A0A0);

        super.render(context, mouseX, mouseY, delta);
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

    @Override
    public void close() {
        this.waitingForKey = false;
        MinecraftClient.getInstance().setScreen(this.parent);
    }

    private Text getModeButtonText() {
        return Text.translatable("text.bookpaste.config.mode.button",
                Text.translatable("text.bookpaste.config.mode." + this.formatMode.id()));
    }

    private Text getKeyBindingButtonText() {
        return Text.translatable("text.bookpaste.config.hotkey.button",
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
}
