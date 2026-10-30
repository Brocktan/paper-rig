package dev.dispensercontrol.client;

import dev.dispensercontrol.config.DispenserConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Map;

public final class PresetScreen extends Screen {
    private final DispenserControlScreen parent;
    private final ArrayList<String> names = new ArrayList<>();
    private String selected;
    private int offset;
    private String message = "Select a preset or enter a name.";
    private TextFieldWidget nameField;

    public PresetScreen(DispenserControlScreen parent) {
        super(Text.literal("Dispenser Control Presets"));
        this.parent = parent;
        refresh();
    }

    private void refresh() {
        names.clear();
        names.addAll(ClientPresetStore.all().keySet());
        if (selected == null || !names.contains(selected)) selected = names.isEmpty() ? null : names.get(0);
        offset = Math.min(offset, Math.max(0, names.size() - 5));
    }

    @Override protected void init() {
        int left = width / 2 - 155;
        int top = height / 2 - 112;
        nameField = new TextFieldWidget(textRenderer, left, top + 152, 310, 20, Text.literal("Preset name"));
        nameField.setMaxLength(40);
        nameField.setPlaceholder(Text.literal("Preset name"));
        addDrawableChild(nameField);
        addDrawableChild(ButtonWidget.builder(Text.literal("Load"), b -> load()).dimensions(left, top + 178, 96, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Save as new"), b -> create()).dimensions(left + 107, top + 178, 96, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Duplicate"), b -> duplicate()).dimensions(left + 214, top + 178, 96, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Rename"), b -> rename()).dimensions(left, top + 204, 96, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Delete"), b -> delete()).dimensions(left + 107, top + 204, 96, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Restore defaults"), b -> {
            ClientPresetStore.restoreDefaults(); refresh(); message = "Default preset restored.";
        }).dimensions(left + 214, top + 204, 96, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> client.setScreen(parent))
                .dimensions(left + 107, top + 230, 96, 20).build());
    }

    private void load() {
        if (selected == null) { message = "Select a preset first."; return; }
        DispenserConfig config = ClientPresetStore.all().get(selected);
        if (config == null) { message = "Select a preset first."; return; }
        parent.apply(config);
        client.setScreen(parent);
    }

    private void create() {
        String name = nameField.getText().strip();
        if (name.isEmpty() || ClientPresetStore.all().containsKey(name)) { message = "Enter a new, unused preset name."; return; }
        ClientPresetStore.put(name, parent.currentConfig()); selected = name; refresh(); message = "Preset saved locally.";
    }

    private void duplicate() {
        String target = nameField.getText().strip();
        if (selected == null || target.isEmpty() || ClientPresetStore.all().containsKey(target)) { message = "Choose a preset and an unused new name."; return; }
        ClientPresetStore.duplicate(selected, target); selected = target; refresh(); message = "Preset duplicated.";
    }

    private void rename() {
        String target = nameField.getText().strip();
        if (selected == null || target.isEmpty() || ClientPresetStore.all().containsKey(target)) { message = "Choose a preset and an unused new name."; return; }
        ClientPresetStore.rename(selected, target); selected = target; refresh(); message = "Preset renamed.";
    }

    private void delete() {
        if (selected == null) return;
        ClientPresetStore.remove(selected); selected = null; refresh(); message = "Preset deleted.";
    }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        int left = width / 2 - 155;
        int top = height / 2 - 112;
        context.fill(left - 8, top - 8, left + 318, top + 260, 0xE91B1B22);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, top, 0xFFFFFFFF);
        int y = top + 24;
        for (int i = offset; i < Math.min(names.size(), offset + 5); i++) {
            String name = names.get(i);
            boolean hover = mouseX >= left && mouseX <= left + 310 && mouseY >= y && mouseY < y + 22;
            context.fill(left, y, left + 310, y + 22, name.equals(selected) ? 0xFF55556A : hover ? 0xFF41414C : 0xFF2B2B34);
            context.drawTextWithShadow(textRenderer, name, left + 8, y + 7, 0xFFFFFFFF);
            y += 24;
        }
        context.drawTextWithShadow(textRenderer, message, left, top + 142, 0xFFB9D7A9);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        int left = width / 2 - 155;
        int top = height / 2 - 112;
        if (button == 0 && mouseX >= left && mouseX <= left + 310) {
            int row = (int) ((mouseY - (top + 24)) / 24.0);
            int index = offset + row;
            if (row >= 0 && row < 5 && index < names.size() && mouseY >= top + 24 && mouseY < top + 144) {
                selected = names.get(index);
                return true;
            }
        }
        return false;
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        offset = Math.max(0, Math.min(Math.max(0, names.size() - 5), offset - (int) Math.signum(verticalAmount)));
        return true;
    }
}
