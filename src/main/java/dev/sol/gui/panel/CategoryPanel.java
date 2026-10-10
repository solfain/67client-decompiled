package dev.sol.gui.panel;

import dev.sol.gui.ClickGuiState;
import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.module.ModuleManager;
import dev.sol.render.nanovg.NVGIcons;
import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.theme.ThemeManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategoryPanel extends Panel {
   private Category category;
   private final List<ModuleEntry> entries = new ArrayList();
   private String filter = "";

   public CategoryPanel(Category category, ModuleManager modules, ThemeManager themes, ClickGuiState state) {
      super(themes, state.panel(category.name()));
      this.category = category;

      for(Module module : modules.inCategory(category)) {
         this.entries.add(new ModuleEntry(module, themes, state));
      }
   }

   public Category getCategory() {
      return this.category;
   }

   public void setFilter(String query) {
      this.filter = query == null ? "" : query.toLowerCase(Locale.ROOT).trim();
   }

   private List<ModuleEntry> visibleEntries() {
      return this.filter.isEmpty() ? this.entries : this.entries.stream().filter((e) -> e.getModule().getName().toLowerCase(Locale.ROOT).contains(this.filter)).toList();
   }

   protected String title() {
      return this.category.getDisplayName();
   }

   protected int icon() {
      return NVGIcons.get(this.category);
   }

   protected String badge() {
      int on = 0;
      for(ModuleEntry entry : this.entries) {
         if (entry.getModule().isEnabled()) {
            ++on;
         }
      }

      return on > 0 ? String.valueOf(on) : null;
   }

   protected float contentHeight(NVGRenderer vg) {
      List<ModuleEntry> visible = this.visibleEntries();
      if (visible.isEmpty()) {
         return CONTENT_PAD * 2.0F + ModuleEntry.ROW_H;
      } else {
         return CONTENT_PAD * 2.0F + (float)visible.size() * ModuleEntry.ROW_H;
      }
   }

   protected void renderContent(NVGRenderer vg, float startY, float mx, float my, float viewTop, float viewBottom) {
      List<ModuleEntry> visible = this.visibleEntries();
      if (visible.isEmpty()) {
         String msg = "No matches";
         vg.text(msg, this.x() + (WIDTH - vg.textWidth(msg, 11.5F)) / 2.0F, startY + ModuleEntry.ROW_H / 2.0F, 11.5F, this.theme().textDisabled());
         return;
      }

      float rowY = startY;
      for(ModuleEntry entry : visible) {
         float h = entry.height(vg);
         entry.setBounds(this.x(), rowY, WIDTH);
         if (rowY + h >= viewTop - 10.0F && rowY <= viewBottom + 10.0F) {
            entry.render(vg, mx, my, this.edgeFade(rowY, rowY + h, viewTop, viewBottom));
         }

         rowY += h;
      }
   }

   public boolean mouseClicked(float mx, float my, int button) {
      for(ModuleEntry entry : this.visibleEntries()) {
         if (entry.mouseClicked(mx, my, button)) {
            return true;
         }
      }

      return false;
   }
}
