package dev.sol.gui;

import dev.sol.SolClient;
import dev.sol.gui.config.ConfigPanel;
import dev.sol.gui.panel.CategoryPanel;
import dev.sol.gui.panel.ModuleEntry;
import dev.sol.gui.panel.Panel;
import dev.sol.gui.panel.ThemesPanel;
import dev.sol.gui.picker.BlockGridModel;
import dev.sol.gui.picker.IconListGridModel;
import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.module.ModuleManager;
import dev.sol.module.Modules;
import dev.sol.module.impl.BlockEspModule;
import dev.sol.render.BlurHook;
import dev.sol.render.NvgDrawable;
import dev.sol.render.OverlayRenderer;
import dev.sol.render.anim.Animation;
import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.settings.BlockListSetting;
import dev.sol.settings.IconListSetting;
import dev.sol.theme.Theme;
import dev.sol.theme.ThemeManager;
import dev.sol.util.Colors;
import dev.sol.util.UiSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

/**
 * Sol Client ClickGUI.
 *
 * Layout: a slim top bar (brand, Modules / Configs tabs, search) above a row
 * of draggable category windows. Right-clicking a module opens its settings
 * in a separate {@link ModuleSettingsWindow}.
 */
public class ClickGuiScreen extends class_437 implements NvgDrawable {
   private static final ClickGuiState STATE = new ClickGuiState();
   private static final float BAR_Y = 10.0F;
   private static final float BAR_H = 26.0F;
   private static final float SEARCH_W = 170.0F;
   private static final float TAB_PAD = 12.0F;
   private static final float BRAND_SIZE = 14.0F;
   private static final float TAB_SIZE = 12.5F;
   private final List<Panel> panels;
   private final Animation openAnim;
   private boolean closing;
   private final ConfigPanel configPanel;
   private final StringBuilder search;
   private boolean searchFocused;
   private Panel dragging;
   private float dragOffsetX;
   private float dragOffsetY;
   private float pressX;
   private float pressY;
   private boolean dragMoved;
   private Panel pressedContentPanel;
   private class_437 parent;
   private ModuleSettingsWindow settingsWindow;
   private boolean settingsPressed;
   // top-bar hit boxes, refreshed every frame: {x, width}
   private float barX;
   private float barW;
   private float modulesTabX;
   private float modulesTabW;
   private float configsTabX;
   private float configsTabW;
   private float searchX;
   private final Animation tabSlide = new Animation(180.0F, 0.0F);

   public ClickGuiScreen() {
      this((class_437)null);
   }

   public ClickGuiScreen(class_437 parent) {
      super(class_2561.method_43470("Sol Client"));
      this.panels = new ArrayList();
      this.openAnim = new Animation(170.0F, 0.0F);
      this.configPanel = new ConfigPanel();
      this.search = new StringBuilder();
      this.parent = parent;
      STATE.ensureDefaultLayout(OverlayRenderer.uiWidth(), OverlayRenderer.uiHeight());
      ModuleManager modules = SolClient.modules();
      ThemeManager themes = SolClient.themes();

      for(Category category : Category.values()) {
         this.panels.add(new CategoryPanel(category, modules, themes, STATE));
      }

      this.panels.add(new ThemesPanel(themes, STATE));
      this.openAnim.setTarget(1.0F);
   }

   public void method_49589() {
      super.method_49589();
      UiSounds.guiOpen();
   }

   public static ClickGuiState state() {
      return STATE;
   }

   private Modules.ClickGuiModule guiModule() {
      return SolClient.modules().clickGui;
   }

   public boolean method_25421() {
      return false;
   }

   public void method_25419() {
      if (!this.closing) {
         this.closing = true;
         this.openAnim.setTarget(0.0F);
         UiSounds.guiClose();
      }
   }

   public void method_25432() {
      BlurHook.clear();
      SolClient.config().save();
   }

   public void method_25394(class_332 guiGraphics, int mouseX, int mouseY, float partialTick) {
      int alpha = (int)(70.0F * this.openAnim.value());
      guiGraphics.method_25294(0, 0, this.field_22789, this.field_22790, alpha << 24 | 0x050506);
      this.finishCloseIfDone();
   }

   public void method_25393() {
      this.finishCloseIfDone();
   }

   private void finishCloseIfDone() {
      if (this.closing && this.openAnim.isDone()) {
         this.field_22787.method_1507(this.parent);
      }
   }

   public void method_25420(class_332 guiGraphics, int mouseX, int mouseY, float partialTick) {
      if (this.field_22787.field_1687 == null) {
         this.method_57728(guiGraphics, partialTick);
      }

      if ((Boolean)this.guiModule().blur.get()) {
         BlurHook.set(this.guiModule().blurStrength.getFloat() * this.openAnim.value());
         guiGraphics.method_71278();
      } else {
         BlurHook.clear();
      }
   }

   // ------------------------------------------------------------------ settings window sync

   private Module findModule(String key) {
      if (key == null) {
         return null;
      } else {
         for(Module module : SolClient.modules().all()) {
            if (ModuleEntry.keyOf(module).equals(key)) {
               return module;
            }
         }

         return null;
      }
   }

   /** Keeps the floating settings window in sync with {@link ClickGuiState#openModuleKey()}. */
   private void syncSettingsWindow() {
      String wanted = STATE.openModuleKey();
      if (this.settingsWindow != null) {
         if (this.settingsWindow.isGone()) {
            this.settingsWindow = null;
         } else if (!this.settingsWindow.isClosing() && !this.settingsWindow.key().equals(wanted)) {
            this.settingsWindow.close();
         }
      }

      if (wanted != null && (this.settingsWindow == null || this.settingsWindow.isClosing())) {
         Module module = this.findModule(wanted);
         if (module == null) {
            STATE.setExpanded(wanted, false);
         } else {
            this.settingsWindow = new ModuleSettingsWindow(module, SolClient.themes(), wanted);
         }
      }
   }

   private void closeSettingsWindow() {
      if (this.settingsWindow != null) {
         STATE.setExpanded(this.settingsWindow.key(), false);
         this.settingsWindow.close();
      }
   }

   private boolean settingsOpen() {
      return this.settingsWindow != null && !this.settingsWindow.isClosing();
   }

   // ------------------------------------------------------------------ rendering

   public void renderNvg(NVGRenderer vg, float mouseX, float mouseY, float uiWidth, float uiHeight) {
      vg.setFontMode((String)this.guiModule().font.get());
      if (vg.hasFont()) {
         float t = this.openAnim.value();
         if (!(t <= 0.002F) || !this.closing) {
            this.syncSettingsWindow();
            boolean modal = this.settingsOpen() || this.configPanel.isOpen();
            float pmx = modal ? -10000.0F : mouseX;
            float pmy = modal ? -10000.0F : mouseY;
            vg.save();
            vg.alpha(t);
            vg.save();
            vg.translate(0.0F, (1.0F - t) * -8.0F);
            String query = this.search.toString();

            for(Panel panel : this.panels) {
               if (panel instanceof CategoryPanel categoryPanel) {
                  categoryPanel.setFilter(query);
               }
            }

            for(int i = this.panels.size() - 1; i >= 0; --i) {
               ((Panel)this.panels.get(i)).render(vg, pmx, pmy, uiWidth, uiHeight);
            }

            vg.restore();
            this.renderTopBar(vg, pmx, pmy, uiWidth);
            this.renderCorner(vg, uiWidth, uiHeight);
            if (this.settingsWindow != null) {
               this.settingsWindow.render(vg, mouseX, mouseY, uiWidth, uiHeight);
            }

            this.configPanel.render(vg, mouseX, mouseY, uiWidth, uiHeight);
            vg.restore();
         }
      }
   }

   private void renderTopBar(NVGRenderer vg, float mx, float my, float uiWidth) {
      Theme theme = SolClient.themes().current();
      float brandW = 18.0F + 6.0F + vg.textWidth("Sol", BRAND_SIZE) + TAB_PAD;
      this.modulesTabW = vg.textWidth("Modules", TAB_SIZE) + TAB_PAD * 2.0F;
      this.configsTabW = vg.textWidth("Configs", TAB_SIZE) + TAB_PAD * 2.0F;
      this.barW = brandW + this.modulesTabW + this.configsTabW + 8.0F + SEARCH_W + 4.0F;
      this.barX = (uiWidth - this.barW) / 2.0F;
      float y = BAR_Y;
      float cy = y + BAR_H / 2.0F;
      vg.glow(this.barX, y, this.barW, BAR_H, 3.0F, 6.0F, Colors.withAlpha(0xFF000000, 0.30F));
      vg.rect(this.barX, y, this.barW, BAR_H, 3.0F, theme.headerTop());
      vg.rect(this.barX, y + BAR_H - 1.5F, this.barW, 1.5F, 0.0F, Colors.withAlpha(theme.accent(), 0.85F));

      // brand: sun mark + "Sol"
      float bx = this.barX + 8.0F;
      drawSun(vg, bx + 9.0F, cy - 0.5F, 4.0F, theme.accent());
      vg.text("Sol", bx + 22.0F, cy, BRAND_SIZE, theme.textPrimary());
      float x = this.barX + brandW;
      vg.rect(x - 1.0F, y + 6.0F, 1.0F, BAR_H - 12.0F, 0.0F, theme.outline());

      // tabs
      this.modulesTabX = x;
      this.configsTabX = x + this.modulesTabW;
      this.tabSlide.setTarget(this.configPanel.isOpen() ? 1.0F : 0.0F);
      float s = this.tabSlide.value();
      float ulX = this.modulesTabX + (this.configsTabX - this.modulesTabX) * s;
      float ulW = this.modulesTabW + (this.configsTabW - this.modulesTabW) * s;
      vg.rect(ulX + 4.0F, y + 3.0F, ulW - 8.0F, BAR_H - 7.5F, 2.0F, Colors.withAlpha(theme.accent(), 0.16F));
      vg.rect(ulX + 4.0F, y + BAR_H - 3.5F, ulW - 8.0F, 2.0F, 1.0F, theme.accent());
      this.tab(vg, "Modules", this.modulesTabX, this.modulesTabW, cy, mx, my, s < 0.5F, theme);
      this.tab(vg, "Configs", this.configsTabX, this.configsTabW, cy, mx, my, s >= 0.5F, theme);

      // search field
      this.searchX = this.configsTabX + this.configsTabW + 8.0F;
      float sx = this.searchX;
      float sy = y + 4.0F;
      float sh = BAR_H - 9.0F;
      vg.rect(sx, sy, SEARCH_W, sh, 2.0F, Colors.withAlpha(0xFF000000, 0.35F));
      vg.rectOutline(sx, sy, SEARCH_W, sh, 2.0F, 1.0F, this.searchFocused ? theme.accent() : theme.outline());
      float ix = sx + 9.0F;
      float icy = sy + sh / 2.0F;
      vg.circleOutline(ix, icy - 1.0F, 3.5F, 1.3F, theme.textDisabled());
      vg.line(ix + 2.6F, icy + 1.6F, ix + 5.0F, icy + 4.0F, 1.3F, theme.textDisabled());
      float textX = sx + 19.0F;
      if (this.search.isEmpty() && !this.searchFocused) {
         vg.text("Search modules...", textX, icy, 11.5F, theme.textDisabled());
      } else {
         float w = vg.textTruncated(this.search.toString(), textX, icy, 11.5F, theme.textPrimary(), SEARCH_W - 36.0F);
         if (this.searchFocused && System.nanoTime() / 450000000L % 2L == 0L) {
            vg.rect(textX + w + 1.5F, icy - 5.5F, 1.2F, 11.0F, 0.0F, theme.accent());
         }
      }

      if (!this.search.isEmpty()) {
         vg.cross(sx + SEARCH_W - 15.0F, icy - 5.0F, 10.0F, 1.3F, theme.textMuted());
      }
   }

   private void tab(NVGRenderer vg, String label, float x, float w, float cy, float mx, float my, boolean active, Theme theme) {
      boolean hover = mx >= x && mx <= x + w && my >= BAR_Y && my <= BAR_Y + BAR_H;
      int color = active ? theme.textPrimary() : (hover ? theme.textPrimary() : theme.textMuted());
      vg.text(label, x + TAB_PAD, cy - 0.5F, TAB_SIZE, color);
   }

   private void renderCorner(NVGRenderer vg, float uiWidth, float uiHeight) {
      Theme theme = SolClient.themes().current();
      String version = "v1.0.0";
      float vw = vg.textWidth(version, 10.5F);
      float sw = vg.textWidth("Sol Client", 12.0F);
      float x = uiWidth - 10.0F - vw;
      float y = uiHeight - 12.0F;
      vg.text(version, x, y, 10.5F, theme.textDisabled());
      vg.text("Sol Client", x - 6.0F - sw, y, 12.0F, Colors.withAlpha(theme.accent(), 0.85F));
      String hint = "Left-click: toggle   Right-click: settings   Drag title: move";
      vg.text(hint, 10.0F, y, 10.5F, Colors.withAlpha(theme.textDisabled(), 0.8F));
   }

   /** The Sol sun mark: core disc + 8 short rays. */
   public static void drawSun(NVGRenderer vg, float cx, float cy, float r, int color) {
      vg.circle(cx, cy, r, color);
      float inner = r + 2.0F;
      float outer = r + 4.5F;

      for(int i = 0; i < 8; ++i) {
         double a = Math.PI / 4.0 * (double)i + Math.PI / 8.0;
         float dx = (float)Math.cos(a);
         float dy = (float)Math.sin(a);
         vg.line(cx + dx * inner, cy + dy * inner, cx + dx * outer, cy + dy * outer, 1.4F, color);
      }
   }

   // ------------------------------------------------------------------ public helpers

   public void openSearch(String query) {
      this.search.setLength(0);
      this.search.append(query);
      this.searchFocused = true;
   }

   public ConfigPanel configPanel() {
      return this.configPanel;
   }

   private boolean inBar(float mx, float my) {
      return mx >= this.barX && mx <= this.barX + this.barW && my >= BAR_Y && my <= BAR_Y + BAR_H;
   }

   private float uiX(double guiX) {
      return OverlayRenderer.guiToUi(guiX);
   }

   private float uiY(double guiY) {
      return OverlayRenderer.guiToUi(guiY);
   }

   // ------------------------------------------------------------------ input

   public boolean method_25402(class_11909 event, boolean doubled) {
      float mx = this.uiX(event.comp_4798());
      float my = this.uiY(event.comp_4799());
      int button = event.method_74245();
      NVGRenderer vg = NVGRenderer.get();
      float uiHeight = OverlayRenderer.uiHeight();
      if (this.configPanel.isOpen()) {
         this.configPanel.mouseClicked(mx, my, button);
         return true;
      } else if (this.settingsOpen()) {
         this.settingsPressed = true;
         if (!this.settingsWindow.mouseClicked(mx, my, button)) {
            this.closeSettingsWindow();
            this.settingsPressed = false;
         } else if (this.settingsWindow.isClosing()) {
            STATE.setExpanded(this.settingsWindow.key(), false);
         }

         return true;
      } else if (this.inBar(mx, my)) {
         if (mx >= this.configsTabX && mx <= this.configsTabX + this.configsTabW) {
            this.searchFocused = false;
            this.configPanel.open();
            UiSounds.guiOpen();
         } else if (mx >= this.searchX && mx <= this.searchX + SEARCH_W) {
            if (!this.search.isEmpty() && mx >= this.searchX + SEARCH_W - 20.0F) {
               this.search.setLength(0);
            }

            this.searchFocused = true;
            UiSounds.select();
         } else {
            this.searchFocused = false;
         }

         return true;
      } else {
         this.searchFocused = false;

         for(Panel panel : this.panels) {
            if (panel.headerHit(mx, my)) {
               this.bringToFront(panel);
               if (button == 0) {
                  this.dragging = panel;
                  this.dragOffsetX = mx - panel.getFloat();
                  this.dragOffsetY = my - panel.getFloat2();
                  this.pressX = mx;
                  this.pressY = my;
                  this.dragMoved = false;
               } else {
                  panel.toggleCollapsed();
                  UiSounds.panelCollapse();
               }

               return true;
            }

            if (panel.bodyHit(vg, mx, my, uiHeight)) {
               this.bringToFront(panel);
               this.pressedContentPanel = panel;
               panel.mouseClicked(mx, my, button);
               return true;
            }
         }

         return true;
      }
   }

   public boolean method_25403(class_11909 event, double dx, double dy) {
      if (this.configPanel.isOpen()) {
         return true;
      } else {
         float mx = this.uiX(event.comp_4798());
         float my = this.uiY(event.comp_4799());
         if (this.settingsWindow != null && this.settingsPressed) {
            this.settingsWindow.mouseDragged(mx, my);
            return true;
         } else if (this.dragging != null) {
            if (Math.abs(mx - this.pressX) + Math.abs(my - this.pressY) > 3.0F) {
               this.dragMoved = true;
            }

            if (this.dragMoved) {
               this.dragging.moveTo(mx - this.dragOffsetX, my - this.dragOffsetY);
            }

            return true;
         } else {
            if (this.pressedContentPanel != null) {
               this.pressedContentPanel.mouseDragged(mx, my);
            }

            return true;
         }
      }
   }

   public boolean method_25406(class_11909 event) {
      if (this.configPanel.isOpen()) {
         return true;
      } else if (this.settingsPressed) {
         this.settingsPressed = false;
         if (this.settingsWindow != null) {
            this.settingsWindow.mouseReleased();
         }

         return true;
      } else if (this.dragging == null) {
         if (this.pressedContentPanel != null) {
            this.pressedContentPanel.mouseReleased();
            this.pressedContentPanel = null;
         }

         return true;
      } else {
         if (!this.dragMoved && event.method_74245() == 0) {
            this.dragging.toggleCollapsed();
            UiSounds.panelCollapse();
         } else if (this.dragMoved) {
            STATE.markCustomized();
         }

         this.dragging = null;
         return true;
      }
   }

   public boolean method_25401(double mouseX, double mouseY, double scrollX, double scrollY) {
      if (this.configPanel.isOpen()) {
         return true;
      } else if (this.settingsOpen()) {
         this.settingsWindow.onScroll(scrollY);
         return true;
      } else {
         float mx = this.uiX(mouseX);
         float my = this.uiY(mouseY);
         NVGRenderer vg = NVGRenderer.get();
         float uiHeight = OverlayRenderer.uiHeight();

         for(Panel panel : this.panels) {
            if (panel.bodyHit(vg, mx, my, uiHeight) || panel.headerHit(mx, my)) {
               panel.onScroll(scrollY);
               return true;
            }
         }

         return true;
      }
   }

   public boolean method_25404(class_11908 event) {
      int key = event.comp_4795();
      if (this.configPanel.isOpen()) {
         this.configPanel.keyPressed(key);
         return true;
      } else if (this.settingsOpen()) {
         if (!this.settingsWindow.keyPressed(key) && key == 256) {
            this.closeSettingsWindow();
            UiSounds.guiClose();
         }

         return true;
      } else {
         for(Panel panel : this.panels) {
            if (panel.isListening()) {
               panel.keyPressed(key);
               return true;
            }
         }

         if (this.searchFocused) {
            switch (key) {
               case 256:
                  this.search.setLength(0);
                  this.searchFocused = false;
                  break;
               case 257:
               case 335:
                  this.searchFocused = false;
                  break;
               case 259:
                  if (!this.search.isEmpty()) {
                     this.search.deleteCharAt(this.search.length() - 1);
                  }
            }

            return true;
         } else if (event.method_74231() || this.guiModule().getKeybind().matches(key)) {
            this.method_25419();
            return true;
         } else {
            return super.method_25404(event);
         }
      }
   }

   public boolean method_25400(class_11905 event) {
      if (this.configPanel.isOpen()) {
         this.configPanel.charTyped(event.comp_4793());
         return true;
      } else if (this.settingsOpen()) {
         this.settingsWindow.charTyped(event.comp_4793());
         return true;
      } else {
         for(Panel panel : this.panels) {
            if (panel.isListening()) {
               panel.charTyped(event.comp_4793());
               return true;
            }
         }

         if (!this.searchFocused && event.method_74227() && this.search.isEmpty()) {
            // start typing anywhere to search
            this.searchFocused = true;
         }

         if (this.searchFocused && event.method_74227()) {
            if (this.search.length() < 32) {
               this.search.append(event.method_74226());
            }

            return true;
         } else {
            return super.method_25400(event);
         }
      }
   }

   private void bringToFront(Panel panel) {
      if (this.panels.remove(panel)) {
         this.panels.addFirst(panel);
      }
   }

   public void openBlockPicker(BlockListSetting setting) {
      BlockGridModel model = new BlockGridModel(setting, () -> {
         BlockEspModule blockEsp = SolClient.modules().blockEsp;
         return blockEsp != null ? (Integer)blockEsp.lineColor.get() : -16711736;
      }, "Pick Block");
      this.field_22787.method_1507(new IconPickerScreen(this, model, SolClient.themes()));
   }

   public void openIconPicker(IconListSetting setting) {
      IconListGridModel model = new IconListGridModel(setting);
      this.field_22787.method_1507(new IconPickerScreen(this, model, SolClient.themes()));
   }
}
