package dev.dispensercontrol.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class AdvancedSettingsScreen extends Screen {
    private final DispenserControlScreen parent;
    private boolean sound;
    private boolean particles;
    private TextFieldWidget notes;

    public AdvancedSettingsScreen(DispenserControlScreen parent) {
        super(Text.literal("Advanced Settings"));
        this.parent = parent;
        var current = parent.currentConfig();
        sound = current.playSound();
        particles = current.showParticles();
    }

    @Override protected void init() {
        int left = width / 2 - 150;
        int top = height / 2 - 65;
        notes = new TextFieldWidget(textRenderer, left, top + 12, 300, 20, Text.literal("Notes"));
        notes.setMaxLength(256);
        notes.setPlaceholder(Text.literal("Optional configuration notes"));
        notes.setText(parent.currentConfig().notes());
        addDrawableChild(notes);
        addDrawableChild(ButtonWidget.builder(Text.literal(sound ? "Sound: On" : "Sound: Off"), button -> {
            sound = !sound;
            button.setMessage(Text.literal(sound ? "Sound: On" : "Sound: Off"));
        }).dimensions(left, top + 52, 145, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal(particles ? "Particles: On" : "Particles: Off"), button -> {
            particles = !particles;
            button.setMessage(Text.literal(particles ? "Particles: On" : "Particles: Off"));
        }).dimensions(left + 155, top + 52, 145, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Apply"), button -> {
            parent.setAdvanced(sound, particles, notes.getText());
            client.setScreen(parent);
        }).dimensions(left, top + 88, 145, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), button -> client.setScreen(parent))
                .dimensions(left + 155, top + 88, 145, 22).build());
    }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        int left = width / 2 - 150;
        int top = height / 2 - 65;
        context.fill(left - 8, top - 12, left + 308, top + 122, 0xE91B1B22);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, top - 6, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer, "Optional notes", left, top - 2, 0xFFB9C8D8);
        context.drawTextWithShadow(textRenderer, "Sound and particle settings apply on the server after saving.", left, top + 118, 0xFFB9C8D8);
        super.render(context, mouseX, mouseY, delta);
    }
}
