package ru.svyaton.bookpaste;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class BookPasteConfigScreen extends Screen {
    private final Screen parent;
    private PasteFormatMode formatMode;
    private Button keyBindingButton;
    private boolean waitingForKey;
    private int waitingForKeyTicks;

    public BookPasteConfigScreen(Screen parent) {
        super(Component.translatable("text.bookpaste.config.title"));
        this.parent = parent;
        this.formatMode = BookPasteConfigManager.getConfig().formatMode();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int buttonWidth = 260;
        int buttonX = centerX - buttonWidth / 2;
        int centerButtonWidth = 126;

        this.addRenderableWidget(Button.builder(this.getModeButtonText(), button -> {
            this.formatMode = this.formatMode.next();
            button.setMessage(this.getModeButtonText());
        }).bounds(buttonX, this.height / 2 - 20, buttonWidth, 20).build());

        this.keyBindingButton = this.addRenderableWidget(Button.builder(this.getKeyBindingButtonText(), button -> {
            this.waitingForKey = true;
            this.waitingForKeyTicks = 0;
            button.setMessage(Component.translatable("text.bookpaste.config.hotkey.listening"));
        }).bounds(buttonX, this.height / 2 + 18, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("text.bookpaste.config.hotkey.reset"), button -> {
            BookPasteHotkeys.resetToggleFormatModeKey(Minecraft.getInstance());
            this.waitingForKey = false;
            this.updateKeyBindingButtonText();
        }).bounds(centerX - 130, this.height / 2 + 46, centerButtonWidth, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("text.bookpaste.config.hotkey.controls"), button ->
                Minecraft.getInstance().setScreen(BookPasteHotkeys.createControlsScreen(Minecraft.getInstance(), this))
        ).bounds(centerX + 4, this.height / 2 + 46, centerButtonWidth, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> {
            BookPasteConfigManager.getConfig().setFormatMode(this.formatMode);
            BookPasteConfigManager.save();
            Minecraft.getInstance().setScreen(this.parent);
        }).bounds(centerX - 102, this.height - 52, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button ->
                Minecraft.getInstance().setScreen(this.parent)
        ).bounds(centerX + 2, this.height - 52, 100, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        this.extractTransparentBackground(context);
        context.centeredText(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        context.centeredText(this.font, Component.translatable("text.bookpaste.config.mode.label"), this.width / 2,
                this.height / 2 - 56, 0xA0A0A0);
        context.centeredText(this.font, Component.translatable("text.bookpaste.config.hotkey.label"), this.width / 2,
                this.height / 2 + 40, 0xA0A0A0);
        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.waitingForKey) {
            this.waitingForKeyTicks++;
        }
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (!this.waitingForKey) {
            return super.keyPressed(input);
        }

        if (this.waitingForKeyTicks < 3) {
            return true;
        }

        return this.handleKeyBindingChange(input.key());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.waitingForKey && this.waitingForKeyTicks >= 3) {
            this.handleMouseBindingChange(event.button());
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void onClose() {
        this.waitingForKey = false;
        Minecraft.getInstance().setScreen(this.parent);
    }

    private Component getModeButtonText() {
        return Component.translatable("text.bookpaste.config.mode.button",
                Component.translatable("text.bookpaste.config.mode." + this.formatMode.id()));
    }

    private Component getKeyBindingButtonText() {
        return Component.translatable("text.bookpaste.config.hotkey.button",
                BookPasteHotkeys.TOGGLE_FORMAT_MODE.getTranslatedKeyMessage());
    }

    private void updateKeyBindingButtonText() {
        if (this.keyBindingButton != null) {
            this.keyBindingButton.setMessage(this.getKeyBindingButtonText());
        }
    }

    private boolean handleKeyBindingChange(int keyCode) {
        if (keyCode == InputConstants.KEY_ESCAPE) {
            this.waitingForKey = false;
            this.waitingForKeyTicks = 0;
            this.updateKeyBindingButtonText();
            return true;
        }

        BookPasteHotkeys.setToggleFormatModeKey(Minecraft.getInstance(), InputConstants.Type.KEYSYM.getOrCreate(keyCode));
        this.waitingForKey = false;
        this.waitingForKeyTicks = 0;
        this.updateKeyBindingButtonText();
        return true;
    }

    private void handleMouseBindingChange(int button) {
        BookPasteHotkeys.setToggleFormatModeKey(Minecraft.getInstance(), InputConstants.Type.MOUSE.getOrCreate(button));
        this.waitingForKey = false;
        this.waitingForKeyTicks = 0;
        this.updateKeyBindingButtonText();
    }
}
