package dev.dispensercontrol.client;

import net.minecraft.client.gui.Click;
import dev.dispensercontrol.config.ConfigValidator;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Comparator;
import java.util.List;

/** Searchable vanilla item picker with paging and a live item preview. */
public final class ItemPickerScreen extends Screen {
    private final DispenserControlScreen parent;
    private final boolean displayed;
    private final List<Identifier> allItems;
    private TextFieldWidget search;
    private int offset;
    private List<Identifier> results = List.of();

    public ItemPickerScreen(DispenserControlScreen parent, boolean displayed) {
        super(Text.literal(displayed ? "Choose displayed item" : "Choose actual output"));
        this.parent = parent;
        this.displayed = displayed;
        this.allItems = Registries.ITEM.getIds().stream()
                .filter(id -> ConfigValidator.isValidItemId(id.toString()) && !id.equals(Identifier.of("minecraft", "air")))
                .sorted(Comparator.comparing(id -> Registries.ITEM.get(id).getName().getString(), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override protected void init() {
        int left = width / 2 - 150;
        search = new TextFieldWidget(textRenderer, left, height / 2 - 105, 300, 20, Text.literal("Search items"));
        search.setPlaceholder(Text.literal("Search by item name or ID"));
        search.setChangedListener(value -> { offset = 0; updateResults(); });
        addDrawableChild(search);
        addDrawableChild(net.minecraft.client.gui.widget.ButtonWidget.builder(Text.literal("Previous"), b -> {
            offset = Math.max(0, offset - 8); updateResults();
        }).dimensions(left, height / 2 + 104, 90, 20).build());
        addDrawableChild(net.minecraft.client.gui.widget.ButtonWidget.builder(Text.literal("Next"), b -> {
            offset = Math.min(Math.max(0, results.size() - 8), offset + 8); updateResults();
        }).dimensions(left + 210, height / 2 + 104, 90, 20).build());
        addDrawableChild(net.minecraft.client.gui.widget.ButtonWidget.builder(Text.literal("Cancel"), b -> client.setScreen(parent))
                .dimensions(left + 105, height / 2 + 104, 90, 20).build());
        updateResults();
    }

    private void updateResults() {
        String query = search == null ? "" : search.getText().toLowerCase(java.util.Locale.ROOT).strip();
        results = allItems.stream().filter(id -> {
            String name = Registries.ITEM.get(id).getName().getString().toLowerCase(java.util.Locale.ROOT);
            return query.isEmpty() || id.toString().contains(query) || name.contains(query);
        }).toList();
        offset = Math.min(offset, Math.max(0, results.size() - 1));
    }

    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        int left = width / 2 - 150;
        int top = height / 2 - 78;
        context.fill(left - 8, height / 2 - 120, left + 308, height / 2 + 130, 0xE91B1B22);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 116, 0xFFFFFFFF);
        int shown = Math.min(8, Math.max(0, results.size() - offset));
        for (int row = 0; row < shown; row++) {
            Identifier id = results.get(offset + row);
            int y = top + row * 24;
            boolean hover = mouseX >= left && mouseX <= left + 300 && mouseY >= y && mouseY < y + 22;
            context.fill(left, y, left + 300, y + 22, hover ? 0xFF454553 : 0xFF2C2C36);
            var item = Registries.ITEM.get(id);
            context.drawItem(new ItemStack(item), left + 4, y + 3);
            context.drawTextWithShadow(textRenderer, item.getName(), left + 28, y + 7, 0xFFFFFFFF);
            context.drawTextWithShadow(textRenderer, id.toString(), left + 160, y + 7, 0xFFB8B8C4);
        }
        context.drawTextWithShadow(textRenderer, results.size() + " matching items", left, height / 2 + 82, 0xFFB8B8C4);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override public boolean mouseClicked(Click click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) return true;
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        int left = width / 2 - 150;
        int top = height / 2 - 78;
        if (button == 0 && mouseX >= left && mouseX <= left + 300) {
            int row = (int) ((mouseY - top) / 24.0);
            int index = offset + row;
            if (row >= 0 && row < 8 && index < results.size() && mouseY >= top && mouseY < top + 8 * 24) {
                parent.setItem(displayed, results.get(index).toString());
                client.setScreen(parent);
                return true;
            }
        }
        return false;
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        offset = Math.max(0, Math.min(Math.max(0, results.size() - 8), offset - (int) Math.signum(verticalAmount) * 3));
        return true;
    }
}
