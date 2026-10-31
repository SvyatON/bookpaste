package ru.svyaton.bookpaste;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.lwjgl.glfw.GLFW;

public final class BookPasteConfigScreen extends Screen {
    private static final int MAX_MOUSE_BUTTON = 7;
    private BookPasteConfigLayout layout;
    private final Screen parent;
    private PasteFormatMode formatMode;
    private boolean customBookPages;
    private int maxPages = BookPasteConfigManager.getConfig().maxPages();
    private int rememberedMaxPages = this.maxPages == 100
            ? BookPasteConfigManager.getConfig().lastCustomMaxPages() : this.maxPages;

    private Button keyBindingButton;

    private net.minecraft.client.gui.components.EditBox pageLimitField;
    private Button doneButton;
    private Button pagesButton;
    private boolean remoteServer;

    private static Component bookpaste$text(String key, Object... arguments) {
        return Component.translatable(key, arguments);
    }

    private Button bookpaste$button(int x, int y, int width, Component text, Button.OnPress action) {
        return this.bookpaste$addChild(Button.builder(text, action).bounds(x, y, width, 20).build());
    }

    private boolean waitingForKey;
    private int waitingForKeyTicks;

    private net.minecraft.client.gui.components.Button bookpaste$addChild(net.minecraft.client.gui.components.Button widget) {
        return this.addRenderableWidget(widget);
    }

    private void bookpaste$open(Screen screen) {
        Minecraft.getInstance().setScreen(screen);
    }

    public BookPasteConfigScreen(Screen parent) {
        super(Component.translatable("text.bookpaste.config.title"));
        this.parent = parent;
        this.formatMode = BookPasteConfigManager.getConfig().formatMode();
        this.customBookPages = BookPasteConfigManager.getConfig().customBookPages();
    }

    @Override
    protected void init() {

        this.remoteServer = Minecraft.getInstance().getConnection() != null && !Minecraft.getInstance().isLocalServer();
        this.layout = new BookPasteConfigLayout(this.width, this.height, this.remoteServer,
                key -> bookpaste$text("text.bookpaste.config." + key).getString(), this.font::width);

        int buttonWidth = this.layout.width;
        int buttonX = this.layout.left;
        int centerButtonWidth = this.layout.halfWidth;

        this.remoteServer = Minecraft.getInstance().getConnection() != null && !Minecraft.getInstance().isLocalServer();
        String pageLimitText = this.pageLimitField == null
                ? Integer.toString(this.customBookPages ? this.maxPages : 100)
                : this.pageLimitField.getValue();
        if (this.remoteServer) pageLimitText = this.serverPageLimitText();
        this.pagesButton = this.bookpaste$button(buttonX, this.layout.pagesY, buttonWidth,
                this.getPageLimitButtonText(), button -> this.togglePageLimit());
        this.pagesButton.active = !this.remoteServer;
        this.pageLimitField = new net.minecraft.client.gui.components.EditBox(this.font,
                this.layout.fieldX, this.layout.limitY, this.layout.fieldWidth, 20,
                bookpaste$text("text.bookpaste.config.max_pages.label"));
        this.pageLimitField.setMaxLength(10);
        this.pageLimitField.setValue(pageLimitText);
        this.pageLimitField.setEditable(!this.remoteServer);
        this.pageLimitField.active = !this.remoteServer;
        this.pageLimitField.setResponder(value -> this.updatePageLimit());

        this.addRenderableWidget(this.pageLimitField);

        this.bookpaste$addChild(Button.builder(this.getModeButtonText(), button -> {
            this.formatMode = this.formatMode.next();
            button.setMessage(this.getModeButtonText());
        }).bounds(buttonX, this.layout.modeY, buttonWidth, 20).build());

        this.keyBindingButton = this.bookpaste$addChild(Button.builder(this.getKeyBindingButtonText(), button -> {
            this.waitingForKey = true;
            this.waitingForKeyTicks = 0;
            button.setMessage(Component.translatable("text.bookpaste.config.hotkey.listening"));
        }).bounds(buttonX, this.layout.keyY, buttonWidth, 20).build());

        this.bookpaste$addChild(Button.builder(Component.translatable("text.bookpaste.config.hotkey.reset"), button -> {
            BookPasteHotkeys.resetToggleFormatModeKey(Minecraft.getInstance());
            this.waitingForKey = false;
            this.updateKeyBindingButtonText();
        }).bounds(buttonX, this.layout.actionsY, centerButtonWidth, 20).build());

        this.bookpaste$addChild(Button.builder(Component.translatable("text.bookpaste.config.hotkey.controls"), button ->
                this.bookpaste$open(BookPasteHotkeys.createControlsScreen(Minecraft.getInstance(), this))
        ).bounds(buttonX + centerButtonWidth + 8, this.layout.actionsY, centerButtonWidth, 20).build());

        this.doneButton = this.bookpaste$addChild(Button.builder(Component.translatable("gui.done"), button -> {
            this.saveConfig();
            this.bookpaste$open(this.parent);
        }).bounds(buttonX, this.layout.footerY, centerButtonWidth, 20).build());

        this.bookpaste$addChild(Button.builder(Component.translatable("gui.cancel"), button ->
                this.bookpaste$open(this.parent)
        ).bounds(buttonX + centerButtonWidth + 8, this.layout.footerY, centerButtonWidth, 20).build());

        this.updatePageLimit();
    }

    private void saveConfig() {
        BookPasteConfig config = BookPasteConfigManager.getConfig();
        config.setFormatMode(this.formatMode);
        if (!this.remoteServer) {
            config.setCustomBookPages(this.customBookPages);
            config.setMaxPages(this.maxPages);
            config.setLastCustomMaxPages(this.rememberedMaxPages);
        }
        BookPasteConfigManager.save();

        if (!this.remoteServer) BookPaste.refreshLimits(Minecraft.getInstance().getSingleplayerServer());
    }

    private void togglePageLimit() {
        if (this.remoteServer) return;
        this.pageLimitField.setValue(Integer.toString(this.customBookPages ? 100 : this.rememberedMaxPages));
        this.updatePageLimit();
    }

    private void updatePageContext() {
        Minecraft client = Minecraft.getInstance();
        boolean remote = client.getConnection() != null && !client.isLocalServer();
        if (remote != this.remoteServer) {
            this.remoteServer = remote;
            if (!remote) {
                BookPasteConfig config = BookPasteConfigManager.getConfig();
                this.maxPages = config.maxPages();
                this.customBookPages = config.customBookPages();
                this.rememberedMaxPages = this.maxPages == 100 ? config.lastCustomMaxPages() : this.maxPages;
            }
            this.pageLimitField.setEditable(!remote);
            this.pageLimitField.active = !remote;
            this.pageLimitField.setValue(remote ? this.serverPageLimitText()
                    : Integer.toString(this.customBookPages ? this.maxPages : 100));
            this.updatePageLimit();
        }
        if (remote) {
            String value = this.serverPageLimitText();
            if (!value.equals(this.pageLimitField.getValue())) this.pageLimitField.setValue(value);
        }
    }

    private String serverPageLimitText() {
        int limit = ServerBookCapabilities.pageLimit(Minecraft.getInstance().getConnection());
        return Integer.toString(limit == Integer.MAX_VALUE ? 0 : limit);
    }

    private void updatePageLimit() {
        String value = this.pageLimitField.getValue();
        boolean valid = false;
        if (value.matches("[0-9]+")) {
            try {
                this.maxPages = Integer.parseInt(value);
                valid = true;
            } catch (NumberFormatException ignored) {
            }
        }
        if (valid) this.customBookPages = this.maxPages != 100;
        if (valid && !this.remoteServer && this.customBookPages) this.rememberedMaxPages = this.maxPages;
        if (this.pagesButton != null) {
            this.pagesButton.setMessage(this.getPageLimitButtonText());
            this.pagesButton.active = !this.remoteServer;
        }
        this.pageLimitField.setTextColor(valid ? 0xFFE0E0E0 : 0xFFFF5555);

        this.pageLimitField.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                bookpaste$text(this.remoteServer ? "text.bookpaste.config.max_pages.server_description"
                        : valid ? "text.bookpaste.config.max_pages.description"
                        : "text.bookpaste.config.max_pages.invalid")));

        if (this.doneButton != null) {
            this.doneButton.active = valid;
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFFFF);
        for (BookPasteConfigLayout.Label label : this.layout.labels) {
            context.drawCenteredString(this.font, label.text, label.x, label.y, label.color);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.pageLimitField != null) this.updatePageContext();

        if (!this.waitingForKey) {
            return;
        }

        this.waitingForKeyTicks++;
        if (this.waitingForKeyTicks < 3) {
            return;
        }

        long handle = Minecraft.getInstance().getWindow().getWindow();

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
    public boolean keyPressed(int keyCode, int scancode, int modifiers) {
        if (!this.waitingForKey) {
            return super.keyPressed(keyCode, scancode, modifiers);
        }

        return this.handleKeyBindingChange(keyCode);
    }

    @Override
    public void onClose() {
        this.waitingForKey = false;
        this.bookpaste$open(this.parent);
    }

    private Component getModeButtonText() {
        return Component.translatable("text.bookpaste.config.mode.button",
                Component.translatable("text.bookpaste.config.mode." + this.formatMode.id()));
    }

    private Component getKeyBindingButtonText() {
        return Component.translatable("text.bookpaste.config.hotkey.button",
                BookPasteHotkeys.TOGGLE_FORMAT_MODE.getTranslatedKeyMessage());
    }

private Component getPageLimitButtonText() {
        return bookpaste$text("text.bookpaste.config.pages.custom_button",
                bookpaste$text(this.customBookPages ? "gui.yes" : "gui.no"));
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
