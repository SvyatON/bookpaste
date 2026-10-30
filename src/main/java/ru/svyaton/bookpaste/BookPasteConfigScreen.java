package ru.svyaton.bookpaste;

//? if <26.3 {
import org.lwjgl.glfw.GLFW;
//?}

//? if >=26.1 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.KeyEvent;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;*/
//?}
//? if >=1.21.10 {
//? if <26.1 {
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
//?}
//?}
//? if >=1.20 {
//? if <1.21.10 {
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
//?}
//?}
//? if >=1.19 {
//? if <1.20 {
/*import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;*/
//?}
//?}
//? if <1.19 {
/*import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;*/
//?}

public final class BookPasteConfigScreen extends Screen {
    private static final int MAX_MOUSE_BUTTON = 7;
    private final Screen parent;
    private PasteFormatMode formatMode;
    private boolean customBookPages;
    private int maxPages = BookPasteConfigManager.getConfig().maxPages();
    private int rememberedMaxPages = this.maxPages == 100
            ? BookPasteConfigManager.getConfig().lastCustomMaxPages() : this.maxPages;
    //? if >=26.1 {
    /*private net.minecraft.client.gui.components.EditBox pageLimitField;
    private Button doneButton;
    private Button pagesButton;
    private boolean remoteServer;*/
    //?}
    //? if >=26.1 {
    /*private Button keyBindingButton;*/
    //?}
    //? if <26.1 {
    private ButtonWidget keyBindingButton;
    //?}
//? if <26.1 {
    private net.minecraft.client.gui.widget.TextFieldWidget pageLimitField;
    private ButtonWidget doneButton;
    private ButtonWidget pagesButton;
    private boolean remoteServer;

    private static Text bookpaste$text(String key, Object... arguments) {
        //? if >=1.19 {
        return Text.translatable(key, arguments);
        //?} else {
        /*return new TranslatableText(key, arguments);*/
        //?}
    }

    private ButtonWidget bookpaste$button(int x, int y, int width, Text text, ButtonWidget.PressAction action) {
        //? if >=1.19.3 {
        return this.bookpaste$addChild(ButtonWidget.builder(text, action).dimensions(x, y, width, 20).build());
        //?} else {
        /*return this.bookpaste$addChild(new ButtonWidget(x, y, width, 20, text, action));*/
        //?}
    }

//?}
//? if <1.20 {
/*private void bookpaste$centered(MatrixStack matrices, Text text, int x, int y, int color) {
    this.textRenderer.drawWithShadow(matrices, text, x - this.textRenderer.getWidth(text) / 2, y, color);
}*/
//?}
    private boolean waitingForKey;
    private int waitingForKeyTicks;

    //? if >=1.17 {
    //? if <26.1 {
    private net.minecraft.client.gui.widget.ButtonWidget bookpaste$addChild(net.minecraft.client.gui.widget.ButtonWidget widget) {
        return this.addDrawableChild(widget);
    }

    private void bookpaste$open(Screen screen) {
        MinecraftClient.getInstance().setScreen(screen);
    }
    //?}
    //?}
    //? if <1.17 {
    /*private net.minecraft.client.gui.widget.ButtonWidget bookpaste$addChild(net.minecraft.client.gui.widget.ButtonWidget widget) {
        return this.addButton(widget);
    }

    private void bookpaste$open(Screen screen) {
        MinecraftClient.getInstance().openScreen(screen);
    }*/
    //?}

    //? if >=26.1 {
    /*public BookPasteConfigScreen(Screen parent) {
        super(Component.translatable("text.bookpaste.config.title"));
        this.parent = parent;
        this.formatMode = BookPasteConfigManager.getConfig().formatMode();
        this.customBookPages = BookPasteConfigManager.getConfig().customBookPages();
    }*/
    //?}
    //? if >=1.19 {
    //? if <26.1 {
    public BookPasteConfigScreen(Screen parent) {
        super(Text.translatable("text.bookpaste.config.title"));
        this.parent = parent;
        this.formatMode = BookPasteConfigManager.getConfig().formatMode();
        this.customBookPages = BookPasteConfigManager.getConfig().customBookPages();
    }
    //?}
    //?}
    //? if <1.19 {
    /*public BookPasteConfigScreen(Screen parent) {
        super(new TranslatableText("text.bookpaste.config.title"));
        this.parent = parent;
        this.formatMode = BookPasteConfigManager.getConfig().formatMode();
        this.customBookPages = BookPasteConfigManager.getConfig().customBookPages();
    }*/
    //?}

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int buttonWidth = 260;
        int buttonX = centerX - buttonWidth / 2;
        int centerButtonWidth = 126;

        int layoutOffset = 0;
        //? if >=26.1 {
        /*layoutOffset = 16;
        this.remoteServer = Minecraft.getInstance().getConnection() != null && !Minecraft.getInstance().hasSingleplayerServer();
        String pageLimitText = this.pageLimitField == null
                ? Integer.toString(this.customBookPages ? this.maxPages : 100)
                : this.pageLimitField.getValue();
        if (this.remoteServer) pageLimitText = this.serverPageLimitText();
        this.pagesButton = this.addRenderableWidget(Button.builder(this.getPageLimitButtonText(), button -> {
            this.togglePageLimit();
        }).pos(buttonX, this.height / 2 - 90).size(buttonWidth, 20)
                .tooltip(net.minecraft.client.gui.components.Tooltip.create(
                        Component.translatable("text.bookpaste.config.max_pages.description"))).build());
        this.pagesButton.active = !this.remoteServer;
        this.pageLimitField = new net.minecraft.client.gui.components.EditBox(this.getFont(),
                centerX + 36, this.height / 2 - 62, 94, 20,
                Component.translatable("text.bookpaste.config.max_pages.label"));
        this.pageLimitField.setMaxLength(10);
        this.pageLimitField.setValue(pageLimitText);
        this.pageLimitField.setEditable(!this.remoteServer);
        this.pageLimitField.active = !this.remoteServer;
        this.pageLimitField.setResponder(value -> this.updatePageLimit());
        this.addRenderableWidget(this.pageLimitField);*/
        //?}
//? if <26.1 {
layoutOffset = 16;
        this.remoteServer = MinecraftClient.getInstance().getNetworkHandler() != null && !MinecraftClient.getInstance().isInSingleplayer();
        String pageLimitText = this.pageLimitField == null
                ? Integer.toString(this.customBookPages ? this.maxPages : 100)
                : this.pageLimitField.getText();
        if (this.remoteServer) pageLimitText = this.serverPageLimitText();
        this.pagesButton = this.bookpaste$button(buttonX, this.height / 2 - 90, buttonWidth,
                this.getPageLimitButtonText(), button -> this.togglePageLimit());
        this.pagesButton.active = !this.remoteServer;
        this.pageLimitField = new net.minecraft.client.gui.widget.TextFieldWidget(this.textRenderer,
                centerX + 36, this.height / 2 - 62, 94, 20,
                bookpaste$text("text.bookpaste.config.max_pages.label"));
        this.pageLimitField.setMaxLength(10);
        this.pageLimitField.setText(pageLimitText);
        this.pageLimitField.setEditable(!this.remoteServer);
        this.pageLimitField.active = !this.remoteServer;
        this.pageLimitField.setChangedListener(value -> this.updatePageLimit());
        //? if >=1.17 {
        this.addDrawableChild(this.pageLimitField);
        //?} else {
        /*this.children.add(this.pageLimitField);*/
        //?}
//?}

        //? if >=26.1 {
        /*this.addRenderableWidget(Button.builder(this.getModeButtonText(), button -> {
            this.formatMode = this.formatMode.next();
            button.setMessage(this.getModeButtonText());
        }).pos(buttonX, this.height / 2 - 20 + layoutOffset).size(buttonWidth, 20).build());*/
        //?}
        //? if >=1.19.3 {
        //? if <26.1 {
        this.bookpaste$addChild(ButtonWidget.builder(this.getModeButtonText(), button -> {
            this.formatMode = this.formatMode.next();
            button.setMessage(this.getModeButtonText());
        }).dimensions(buttonX, this.height / 2 - 20 + layoutOffset, buttonWidth, 20).build());
        //?}
        //?}
        //? if <1.19.3 {
        /*this.bookpaste$addChild(new ButtonWidget(buttonX, this.height / 2 - 20 + layoutOffset, buttonWidth, 20, this.getModeButtonText(), button -> {
            this.formatMode = this.formatMode.next();
            button.setMessage(this.getModeButtonText());
        }));*/
        //?}

        //? if >=26.1 {
        /*this.keyBindingButton = this.addRenderableWidget(Button.builder(this.getKeyBindingButtonText(), button -> {
            this.waitingForKey = true;
            this.waitingForKeyTicks = 0;
            button.setMessage(Component.translatable("text.bookpaste.config.hotkey.listening"));
        }).pos(buttonX, this.height / 2 + 18 + layoutOffset).size(buttonWidth, 20).build());*/
        //?}
        //? if >=1.19.3 {
        //? if <26.1 {
        this.keyBindingButton = this.bookpaste$addChild(ButtonWidget.builder(this.getKeyBindingButtonText(), button -> {
            this.waitingForKey = true;
            this.waitingForKeyTicks = 0;
            button.setMessage(Text.translatable("text.bookpaste.config.hotkey.listening"));
        }).dimensions(buttonX, this.height / 2 + 18 + layoutOffset, buttonWidth, 20).build());
        //?}
        //?}
        //? if <1.19.3 {
        //? if >=1.19 {
        /*this.keyBindingButton = this.bookpaste$addChild(new ButtonWidget(buttonX, this.height / 2 + 18 + layoutOffset, buttonWidth, 20, this.getKeyBindingButtonText(), button -> {
            this.waitingForKey = true;
            this.waitingForKeyTicks = 0;
            button.setMessage(Text.translatable("text.bookpaste.config.hotkey.listening"));
        }));*/
        //?}
        //? if <1.19 {
        /*this.keyBindingButton = this.bookpaste$addChild(new ButtonWidget(buttonX, this.height / 2 + 18 + layoutOffset, buttonWidth, 20, this.getKeyBindingButtonText(), button -> {
            this.waitingForKey = true;
            this.waitingForKeyTicks = 0;
            button.setMessage(new TranslatableText("text.bookpaste.config.hotkey.listening"));
        }));*/
        //?}
        //?}

        //? if >=26.1 {
        /*this.addRenderableWidget(Button.builder(Component.translatable("text.bookpaste.config.hotkey.reset"), button -> {
            BookPasteHotkeys.resetToggleFormatModeKey(Minecraft.getInstance());
            this.waitingForKey = false;
            this.updateKeyBindingButtonText();
        }).pos(centerX - 130, this.height / 2 + 46 + layoutOffset).size(centerButtonWidth, 20).build());*/
        //?}
        //? if >=1.19.3 {
        //? if <26.1 {
        this.bookpaste$addChild(ButtonWidget.builder(Text.translatable("text.bookpaste.config.hotkey.reset"), button -> {
            BookPasteHotkeys.resetToggleFormatModeKey(MinecraftClient.getInstance());
            this.waitingForKey = false;
            this.updateKeyBindingButtonText();
        }).dimensions(centerX - 130, this.height / 2 + 46 + layoutOffset, centerButtonWidth, 20).build());
        //?}
        //?}
        //? if <1.19.3 {
        //? if >=1.19 {
        /*this.bookpaste$addChild(new ButtonWidget(centerX - 130, this.height / 2 + 46 + layoutOffset, centerButtonWidth, 20, Text.translatable("text.bookpaste.config.hotkey.reset"), button -> {
            BookPasteHotkeys.resetToggleFormatModeKey(MinecraftClient.getInstance());
            this.waitingForKey = false;
            this.updateKeyBindingButtonText();
        }));*/
        //?}
        //? if <1.19 {
        /*this.bookpaste$addChild(new ButtonWidget(centerX - 130, this.height / 2 + 46 + layoutOffset, centerButtonWidth, 20, new TranslatableText("text.bookpaste.config.hotkey.reset"), button -> {
            BookPasteHotkeys.resetToggleFormatModeKey(MinecraftClient.getInstance());
            this.waitingForKey = false;
            this.updateKeyBindingButtonText();
        }));*/
        //?}
        //?}

        //? if >=26.1 {
        /*this.addRenderableWidget(Button.builder(Component.translatable("text.bookpaste.config.hotkey.controls"), button ->
                Minecraft.getInstance().setScreenAndShow(BookPasteHotkeys.createControlsScreen(Minecraft.getInstance(), this))
        ).pos(centerX + 4, this.height / 2 + 46 + layoutOffset).size(centerButtonWidth, 20).build());*/
        //?}
        //? if >=1.19.3 {
        //? if <26.1 {
        this.bookpaste$addChild(ButtonWidget.builder(Text.translatable("text.bookpaste.config.hotkey.controls"), button ->
                this.bookpaste$open(BookPasteHotkeys.createControlsScreen(MinecraftClient.getInstance(), this))
        ).dimensions(centerX + 4, this.height / 2 + 46 + layoutOffset, centerButtonWidth, 20).build());
        //?}
        //?}
        //? if <1.19.3 {
        //? if >=1.19 {
        /*this.bookpaste$addChild(new ButtonWidget(centerX + 4, this.height / 2 + 46 + layoutOffset, centerButtonWidth, 20, Text.translatable("text.bookpaste.config.hotkey.controls"), button ->
                this.bookpaste$open(BookPasteHotkeys.createControlsScreen(MinecraftClient.getInstance(), this))
        ));*/
        //?}
        //? if <1.19 {
        /*this.bookpaste$addChild(new ButtonWidget(centerX + 4, this.height / 2 + 46 + layoutOffset, centerButtonWidth, 20, new TranslatableText("text.bookpaste.config.hotkey.controls"), button ->
                this.bookpaste$open(BookPasteHotkeys.createControlsScreen(MinecraftClient.getInstance(), this))
        ));*/
        //?}
        //?}

        //? if >=26.1 {
        /*Button doneButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> {
            this.saveConfig();
            Minecraft.getInstance().setScreenAndShow(this.parent);
        }).pos(centerX - 102, this.height - 52).size(100, 20).build());*/
        //?}
        //? if >=1.19.3 {
        //? if <26.1 {
        this.doneButton = this.bookpaste$addChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> {
            this.saveConfig();
            this.bookpaste$open(this.parent);
        }).dimensions(centerX - 102, this.height - 52, 100, 20).build());
        //?}
        //?}
        //? if <1.19.3 {
        //? if >=1.19 {
        /*this.doneButton = this.bookpaste$addChild(new ButtonWidget(centerX - 102, this.height - 52, 100, 20, Text.translatable("gui.done"), button -> {
            this.saveConfig();
            this.bookpaste$open(this.parent);
        }));*/
        //?}
        //? if <1.19 {
        /*this.doneButton = this.bookpaste$addChild(new ButtonWidget(centerX - 102, this.height - 52, 100, 20, new TranslatableText("gui.done"), button -> {
            this.saveConfig();
            this.bookpaste$open(this.parent);
        }));*/
        //?}
        //?}

        //? if >=26.1 {
        /*this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button ->
                Minecraft.getInstance().setScreenAndShow(this.parent)
        ).pos(centerX + 2, this.height - 52).size(100, 20).build());*/
        //?}
        //? if >=1.19.3 {
        //? if <26.1 {
        this.bookpaste$addChild(ButtonWidget.builder(Text.translatable("gui.cancel"), button ->
                this.bookpaste$open(this.parent)
        ).dimensions(centerX + 2, this.height - 52, 100, 20).build());
        //?}
        //?}
        //? if <1.19.3 {
        //? if >=1.19 {
        /*this.bookpaste$addChild(new ButtonWidget(centerX + 2, this.height - 52, 100, 20, Text.translatable("gui.cancel"), button ->
                this.bookpaste$open(this.parent)
        ));*/
        //?}
        //? if <1.19 {
        /*this.bookpaste$addChild(new ButtonWidget(centerX + 2, this.height - 52, 100, 20, new TranslatableText("gui.cancel"), button ->
                this.bookpaste$open(this.parent)
        ));*/
        //?}
        //?}
        //? if >=26.1 {
        /*this.doneButton = doneButton;
        this.updatePageLimit();*/
        //?}
//? if <26.1 {
        this.updatePageLimit();
//?}

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
        //? if >=26.1 {
        /*if (!this.remoteServer) BookPaste.refreshLimits(Minecraft.getInstance().getSingleplayerServer());*/
        //?} else {
        if (!this.remoteServer) BookPaste.refreshLimits(MinecraftClient.getInstance().getServer());
        //?}
    }

    //? if >=26.1 {
    /*private void togglePageLimit() {
        if (this.remoteServer) return;
        this.pageLimitField.setValue(Integer.toString(this.customBookPages ? 100 : this.rememberedMaxPages));
        this.updatePageLimit();
    }

    private void updatePageContext() {
        Minecraft client = Minecraft.getInstance();
        boolean remote = client.getConnection() != null && !client.hasSingleplayerServer();
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
                Component.translatable(this.remoteServer ? "text.bookpaste.config.max_pages.server_description"
                        : valid ? "text.bookpaste.config.max_pages.description"
                        : "text.bookpaste.config.max_pages.invalid")));
        if (this.doneButton != null) {
            this.doneButton.active = valid;
        }
    }*/
    //?}

//? if <26.1 {
private void togglePageLimit() {
        if (this.remoteServer) return;
        this.pageLimitField.setText(Integer.toString(this.customBookPages ? 100 : this.rememberedMaxPages));
        this.updatePageLimit();
    }

    private void updatePageContext() {
        MinecraftClient client = MinecraftClient.getInstance();
        boolean remote = client.getNetworkHandler() != null && !client.isInSingleplayer();
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
            this.pageLimitField.setText(remote ? this.serverPageLimitText()
                    : Integer.toString(this.customBookPages ? this.maxPages : 100));
            this.updatePageLimit();
        }
        if (remote) {
            String value = this.serverPageLimitText();
            if (!value.equals(this.pageLimitField.getText())) this.pageLimitField.setText(value);
        }
    }

    private String serverPageLimitText() {
        int limit = ServerBookCapabilities.pageLimit(MinecraftClient.getInstance().getNetworkHandler());
        return Integer.toString(limit == Integer.MAX_VALUE ? 0 : limit);
    }

    private void updatePageLimit() {
        String value = this.pageLimitField.getText();
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
        this.pageLimitField.setEditableColor(valid ? 0xFFE0E0E0 : 0xFFFF5555);
//? if >=1.19.4 {
        this.pageLimitField.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(
                bookpaste$text(this.remoteServer ? "text.bookpaste.config.max_pages.server_description"
                        : valid ? "text.bookpaste.config.max_pages.description"
                        : "text.bookpaste.config.max_pages.invalid")));

//?}
        if (this.doneButton != null) {
            this.doneButton.active = valid;
        }
    }
//?}
    //? if >=26.1 {
    /*@Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        this.extractTransparentBackground(extractor);
        extractor.centeredText(this.getFont(), this.getTitle(), this.width / 2, 20, 0xFFFFFFFF);

        //? if >=26.1 {
        extractor.centeredText(this.getFont(), Component.translatable("text.bookpaste.config.pages.heading"),
                this.width / 2, this.height / 2 - 140, 0xFFFFFFFF);
        extractor.centeredText(this.getFont(), Component.translatable("text.bookpaste.config.pages.help_local"),
                this.width / 2, this.height / 2 - 124, 0xFFA0A0A0);
        extractor.centeredText(this.getFont(), Component.translatable("text.bookpaste.config.pages.help_server"),
                this.width / 2, this.height / 2 - 112, 0xFFA0A0A0);
        //?}
        int labelOffset = 0;
        //? if >=26.1 {
        labelOffset = 16;
        //?}
        //? if >=26.1 {
        extractor.text(this.getFont(), Component.translatable(this.remoteServer
                        ? "text.bookpaste.config.max_pages.server_label" : "text.bookpaste.config.max_pages.label"),
                this.width / 2 - 130, this.height / 2 - 56, 0xFFA0A0A0);
        //?}

        int y = this.height / 2 - 36 + labelOffset;
        extractor.centeredText(this.getFont(),
                Component.translatable("text.bookpaste.config.mode.label"),
                this.width / 2, y, 0xFFA0A0A0);

        extractor.centeredText(this.getFont(),
                Component.translatable("text.bookpaste.config.hotkey.label"),
                this.width / 2, this.height / 2 + 4 + labelOffset, 0xFFA0A0A0);

        super.extractRenderState(extractor, mouseX, mouseY, delta);
    }*/
    //?}
//? if <26.1 {
//? if >=1.20 {
@Override
public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    //? if >=1.20.4 {
    this.renderInGameBackground(context);
    //?} else {
    /*this.renderBackground(context);*/
    //?}
    context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFFFF);

    context.drawCenteredTextWithShadow(this.textRenderer, bookpaste$text("text.bookpaste.config.pages.heading"), this.width / 2, this.height / 2 -140, 0xFFFFFFFF);
    context.drawCenteredTextWithShadow(this.textRenderer, bookpaste$text("text.bookpaste.config.pages.help_local"), this.width / 2, this.height / 2 -124, 0xFFA0A0A0);
    context.drawCenteredTextWithShadow(this.textRenderer, bookpaste$text("text.bookpaste.config.pages.help_server"), this.width / 2, this.height / 2 -112, 0xFFA0A0A0);
    context.drawCenteredTextWithShadow(this.textRenderer, bookpaste$text(this.remoteServer ? "text.bookpaste.config.max_pages.server_label" : "text.bookpaste.config.max_pages.label"), this.width / 2 - 83, this.height / 2 - 56, 0xFFA0A0A0);
    int y = this.height / 2 - 20;
    context.drawCenteredTextWithShadow(this.textRenderer,
            Text.translatable("text.bookpaste.config.mode.label"),
            this.width / 2, y, 0xFFA0A0A0);

    context.drawCenteredTextWithShadow(this.textRenderer,
            Text.translatable("text.bookpaste.config.hotkey.label"),
            this.width / 2, this.height / 2 + 20, 0xFFA0A0A0);

    super.render(context, mouseX, mouseY, delta);
}
//?}
//? if >=1.19 {
//? if <1.20 {
/*@Override
public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
    this.renderBackground(matrices);
    this.textRenderer.drawWithShadow(matrices, this.title, this.width / 2, 20, 0xFFFFFFFF);

    this.bookpaste$centered(matrices, bookpaste$text("text.bookpaste.config.pages.heading"), this.width / 2, this.height / 2 -140, 0xFFFFFFFF);
    this.bookpaste$centered(matrices, bookpaste$text("text.bookpaste.config.pages.help_local"), this.width / 2, this.height / 2 -124, 0xFFA0A0A0);
    this.bookpaste$centered(matrices, bookpaste$text("text.bookpaste.config.pages.help_server"), this.width / 2, this.height / 2 -112, 0xFFA0A0A0);
    this.bookpaste$centered(matrices, bookpaste$text(this.remoteServer ? "text.bookpaste.config.max_pages.server_label" : "text.bookpaste.config.max_pages.label"), this.width / 2 - 83, this.height / 2 - 56, 0xFFA0A0A0);
    int y = this.height / 2 - 20;
    this.textRenderer.drawWithShadow(matrices,
            Text.translatable("text.bookpaste.config.mode.label"),
            this.width / 2, y, 0xFFA0A0A0);

    this.textRenderer.drawWithShadow(matrices,
            Text.translatable("text.bookpaste.config.hotkey.label"),
            this.width / 2, this.height / 2 + 20, 0xFFA0A0A0);

    super.render(matrices, mouseX, mouseY, delta);
    //? if <1.17 {
    this.pageLimitField.render(matrices, mouseX, mouseY, delta);
    //?}
}*/
//?}
//?}
//? if <1.19 {
/*@Override
public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
    this.renderBackground(matrices);
    this.textRenderer.drawWithShadow(matrices, this.title, this.width / 2, 20, 0xFFFFFFFF);

    this.bookpaste$centered(matrices, bookpaste$text("text.bookpaste.config.pages.heading"), this.width / 2, this.height / 2 -140, 0xFFFFFFFF);
    this.bookpaste$centered(matrices, bookpaste$text("text.bookpaste.config.pages.help_local"), this.width / 2, this.height / 2 -124, 0xFFA0A0A0);
    this.bookpaste$centered(matrices, bookpaste$text("text.bookpaste.config.pages.help_server"), this.width / 2, this.height / 2 -112, 0xFFA0A0A0);
    this.bookpaste$centered(matrices, bookpaste$text(this.remoteServer ? "text.bookpaste.config.max_pages.server_label" : "text.bookpaste.config.max_pages.label"), this.width / 2 - 83, this.height / 2 - 56, 0xFFA0A0A0);
    int y = this.height / 2 - 20;
    this.textRenderer.drawWithShadow(matrices,
            new TranslatableText("text.bookpaste.config.mode.label"),
            this.width / 2, y, 0xFFA0A0A0);

    this.textRenderer.drawWithShadow(matrices,
            new TranslatableText("text.bookpaste.config.hotkey.label"),
            this.width / 2, this.height / 2 + 20, 0xFFA0A0A0);

    super.render(matrices, mouseX, mouseY, delta);
    //? if <1.17 {
    this.pageLimitField.render(matrices, mouseX, mouseY, delta);
    //?}
}*/
//?}
//?}

    @Override
    public void tick() {
        super.tick();
        //? if >=26.1 {
        /*if (this.pageLimitField != null) this.updatePageContext();*/
        //?}
//? if <26.1 {
        if (this.pageLimitField != null) this.updatePageContext();
//?}

        if (!this.waitingForKey) {
            return;
        }

        this.waitingForKeyTicks++;
        if (this.waitingForKeyTicks < 3) {
            return;
        }

        //? if >=26.1 {
        /*long handle = Minecraft.getInstance().getWindow().handle();*/
        //?}
        //? if <26.1 {
        long handle = MinecraftClient.getInstance().getWindow().getHandle();
        //?}
        int pressedKey = -1;
        //? if >=26.3 {
        /*java.nio.ByteBuffer keys = org.lwjgl.sdl.SDLKeyboard.SDL_GetKeyboardState();
        if (keys == null) return;
        for (int keyCode = 1; keyCode < keys.limit(); keyCode++) {
            if (keys.get(keyCode) == 0) continue;
            if (pressedKey != -1) return;
            pressedKey = keyCode;
        }
        if (pressedKey == -1) {
            int buttons = org.lwjgl.sdl.SDLMouse.SDL_GetMouseState((java.nio.FloatBuffer) null, (java.nio.FloatBuffer) null);
            if (Integer.bitCount(buttons) != 1) return;
            this.handleMouseBindingChange(Integer.numberOfTrailingZeros(buttons) + 1);
            return;
        }*/
        //?} else {
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

        //?}
        this.handleKeyBindingChange(pressedKey);
    }

    //? if >=26.1 {
    /*@Override
    public boolean keyPressed(KeyEvent input) {
        if (!this.waitingForKey) {
            return super.keyPressed(input);
        }

        return this.handleKeyBindingChange(input.key());
    }*/
    //?}
    //? if >=1.21.10 {
    //? if <26.1 {
    @Override
    public boolean keyPressed(KeyInput input) {
        if (!this.waitingForKey) {
            return super.keyPressed(input);
        }

        return this.handleKeyBindingChange(input.getKeycode());
    }
    //?}
    //?}
    //? if <1.21.10 {
    /*@Override
    public boolean keyPressed(int keyCode, int scancode, int modifiers) {
        if (!this.waitingForKey) {
            return super.keyPressed(keyCode, scancode, modifiers);
        }

        return this.handleKeyBindingChange(keyCode);
    }*/
    //?}

    //? if >=26.1 {
    /*@Override
    public void onClose() {
        this.waitingForKey = false;
        Minecraft.getInstance().setScreenAndShow(this.parent);
    }*/
    //?}
    //? if >=1.18.2 {
    //? if <26.1 {
    @Override
    public void close() {
        this.waitingForKey = false;
        this.bookpaste$open(this.parent);
    }
    //?}
    //?}
    //? if <1.18.2 {
    /*@Override
    public void onClose() {
        this.waitingForKey = false;
        this.bookpaste$open(this.parent);
    }*/
    //?}

    //? if >=26.1 {
    /*private Component getModeButtonText() {
        return Component.translatable("text.bookpaste.config.mode.button",
                Component.translatable("text.bookpaste.config.mode." + this.formatMode.id()));
    }*/
    //?}
    //? if >=1.19 {
    //? if <26.1 {
    private Text getModeButtonText() {
        return Text.translatable("text.bookpaste.config.mode.button",
                Text.translatable("text.bookpaste.config.mode." + this.formatMode.id()));
    }
    //?}
    //?}
    //? if <1.19 {
    /*private Text getModeButtonText() {
        return new TranslatableText("text.bookpaste.config.mode.button",
                new TranslatableText("text.bookpaste.config.mode." + this.formatMode.id()));
    }*/
    //?}

    //? if >=26.1 {
    /*private Component getKeyBindingButtonText() {
        return Component.translatable("text.bookpaste.config.hotkey.button",
                BookPasteHotkeys.TOGGLE_FORMAT_MODE.getTranslatedKeyMessage());
    }*/
    //?}
    //? if >=1.19 {
    //? if <26.1 {
    private Text getKeyBindingButtonText() {
        return Text.translatable("text.bookpaste.config.hotkey.button",
                BookPasteHotkeys.TOGGLE_FORMAT_MODE.getBoundKeyLocalizedText());
    }
    //?}
    //?}
    //? if <1.19 {
    /*private Text getKeyBindingButtonText() {
        return new TranslatableText("text.bookpaste.config.hotkey.button",
                BookPasteHotkeys.TOGGLE_FORMAT_MODE.getBoundKeyLocalizedText());
    }*/
    //?}

    //? if >=26.1 {
    /*private Component getPageLimitButtonText() {
        return Component.translatable("text.bookpaste.config.pages.custom_button",
                Component.translatable(this.customBookPages ? "gui.yes" : "gui.no"));
    }*/
    //?}

//? if <26.1 {
private Text getPageLimitButtonText() {
        return bookpaste$text("text.bookpaste.config.pages.custom_button",
                bookpaste$text(this.customBookPages ? "gui.yes" : "gui.no"));
    }
//?}
    private void updateKeyBindingButtonText() {
        if (this.keyBindingButton != null) {
            this.keyBindingButton.setMessage(this.getKeyBindingButtonText());
        }
    }

    private boolean handleKeyBindingChange(int keyCode) {
        //? if >=26.1 {
        /*if (keyCode == InputConstants.KEY_ESCAPE) {*/
        //?}
        //? if >=1.17 {
        //? if <26.1 {
        if (keyCode == InputUtil.GLFW_KEY_ESCAPE) {
        //?}
        //?}
        //? if <1.17 {
        //? if <26.1 {
        /*if (keyCode == GLFW.GLFW_KEY_ESCAPE) {*/
        //?}
        //?}
            this.waitingForKey = false;
            this.waitingForKeyTicks = 0;
            this.updateKeyBindingButtonText();
            return true;
        }

        //? if >=26.1 {
        /*BookPasteHotkeys.setToggleFormatModeKey(Minecraft.getInstance(), InputConstants.getKey(new KeyEvent(keyCode, 0, 0)));*/
        //?}
        //? if <26.1 {
        BookPasteHotkeys.setToggleFormatModeKey(MinecraftClient.getInstance(), InputUtil.Type.KEYSYM.createFromCode(keyCode));
        //?}
        this.waitingForKey = false;
        this.waitingForKeyTicks = 0;
        this.updateKeyBindingButtonText();
        return true;
    }

    private void handleMouseBindingChange(int button) {
        //? if >=26.1 {
        /*BookPasteHotkeys.setToggleFormatModeKey(Minecraft.getInstance(), InputConstants.Type.MOUSE.getOrCreate(button));*/
        //?}
        //? if <26.1 {
        BookPasteHotkeys.setToggleFormatModeKey(MinecraftClient.getInstance(), InputUtil.Type.MOUSE.createFromCode(button));
        //?}
        this.waitingForKey = false;
        this.waitingForKeyTicks = 0;
        this.updateKeyBindingButtonText();
    }
}
