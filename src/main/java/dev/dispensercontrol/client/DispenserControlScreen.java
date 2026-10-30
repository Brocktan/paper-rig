package dev.dispensercontrol.client;

import dev.dispensercontrol.config.DispenserConfig;
import dev.dispensercontrol.network.SaveDispenserPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public final class DispenserControlScreen extends Screen {
    private final BlockPos pos;
    private String configName = "Default";
    private String displayedItem = "minecraft:diamond";
    private String actualItem = "minecraft:cobblestone";
    private String notes = "";
    private int quantity = 1;
    private boolean enabled;
    private boolean playSound = true;
    private boolean showParticles = true;
    private String status = "Waiting for server configuration…";
    private TextFieldWidget nameField;
    private TextFieldWidget notesField;

    public DispenserControlScreen(BlockPos pos) {
        super(Text.literal("Dispenser Control"));
        this.pos = pos.toImmutable();
    }

    @Override
    protected void init() {
        int left = width / 2 - 160;
        int top = height / 2 - 120;
        nameField = new TextFieldWidget(textRenderer, left + 8, top + 36, 304, 20, Text.literal("Configuration name"));
        nameField.setMaxLength(48);
        nameField.setText(configName);
        nameField.setChangedListener(value -> configName = value);
        addDrawableChild(nameField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Choose displayed item"), button ->
                client.setScreen(new ItemPickerScreen(this, true))).dimensions(left + 8, top + 76, 148, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Choose actual output"), button ->
                client.setScreen(new ItemPickerScreen(this, false))).dimensions(left + 164, top + 76, 148, 22).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("−"), button -> setQuantity(quantity - 1))
                .dimensions(left + 8, top + 132, 28, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+"), button -> setQuantity(quantity + 1))
                .dimensions(left + 126, top + 132, 28, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal(enabled ? "Enabled" : "Disabled"), button -> {
            enabled = !enabled;
            button.setMessage(Text.literal(enabled ? "Enabled" : "Disabled"));
        }).dimensions(left + 164, top + 132, 148, 22).build());

        notesField = new TextFieldWidget(textRenderer, left + 8, top + 164, 304, 20, Text.literal("Notes"));
        notesField.setMaxLength(256);
        notesField.setPlaceholder(Text.literal("Optional notes"));
        notesField.setText(notes);
        notesField.setChangedListener(value -> notes = value);
        addDrawableChild(notesField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Save configuration"), button -> save())
                .dimensions(left + 8, top + 194, 148, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Presets"), button -> client.setScreen(new PresetScreen(this)))
                .dimensions(left + 164, top + 194, 148, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), button -> apply(DispenserConfig.DEFAULT))
                .dimensions(left + 8, top + 222, 96, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Advanced"), button -> client.setScreen(new AdvancedSettingsScreen(this)))
                .dimensions(left + 112, top + 222, 96, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), button -> close())
                .dimensions(left + 216, top + 222, 96, 20).build());
    }

    private void setQuantity(int value) { quantity = Math.max(1, Math.min(64, value)); }

    private void save() {
        configName = nameField.getText();
        notes = notesField.getText();
        if (!ClientPlayNetworking.canSend(SaveDispenserPayload.ID)) {
            status = "Server mod required: this change is preview-only.";
            return;
        }
        status = "Waiting for server validation…";
        ClientPlayNetworking.send(new SaveDispenserPayload(pos, configName, displayedItem, actualItem,
                quantity, enabled, notes, playSound, showParticles));
    }

    public void apply(DispenserConfig config) {
        configName = config.name();
        displayedItem = config.displayedItem();
        actualItem = config.actualItem();
        quantity = config.quantity();
        enabled = config.enabled();
        notes = config.notes();
        playSound = config.playSound();
        showParticles = config.showParticles();
        if (nameField != null) nameField.setText(configName);
        if (notesField != null) notesField.setText(notes);
    }

    public void setItem(boolean displayed, String id) {
        if (displayed) displayedItem = id;
        else actualItem = id;
    }

    public void applyServerState(dev.dispensercontrol.network.DispenserStatePayload payload) {
        if (!pos.equals(payload.pos())) return;
        apply(new DispenserConfig(payload.name(), payload.displayedItem(), payload.actualItem(),
                payload.quantity(), payload.enabled(), payload.notes(), payload.playSound(), payload.showParticles()));
        status = payload.configured() ? "Loaded server configuration." : "Using defaults.";
    }

    public void setServerStatus(String message) { status = message; }
    public DispenserConfig currentConfig() {
        return new DispenserConfig(configName, displayedItem, actualItem, quantity, enabled, notes, playSound, showParticles).validated();
    }
    public BlockPos dispenserPos() { return pos; }
    public void setAdvanced(boolean sound, boolean particles, String newNotes) {
        playSound = sound;
        showParticles = particles;
        notes = newNotes;
        if (notesField != null) notesField.setText(newNotes);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        int left = width / 2 - 160;
        int top = height / 2 - 120;
        context.fill(left, top, left + 320, top + 250, 0xE91B1B22);
        context.fill(left, top, left + 320, top + 24, 0xFF34343F);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, top + 7, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer, "Displayed preview", left + 8, top + 62, 0xFFB9C8D8);
        context.drawTextWithShadow(textRenderer, "Actual server output", left + 164, top + 62, 0xFFB9C8D8);
        drawItemPreview(context, displayedItem, left + 66, top + 104);
        drawItemPreview(context, actualItem, left + 222, top + 104);
        context.drawTextWithShadow(textRenderer, "Quantity: " + quantity, left + 48, top + 138, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer, status, left + 8, top + 248, status.startsWith("Rejected") ? 0xFFFF7777 : 0xFFB7D8A5);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawItemPreview(DrawContext context, String itemId, int x, int y) {
        Identifier id = Identifier.tryParse(itemId);
        if (id == null) return;
        var item = Registries.ITEM.getOptionalValue(id).orElse(null);
        if (item == null) return;
        context.drawItem(new ItemStack(item), x, y);
        context.drawTextWithShadow(textRenderer, itemId, x - 50, y + 20, 0xFFE0E0E0);
    }

    @Override public void close() { if (client != null) client.setScreen(null); }
}
