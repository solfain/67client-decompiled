package dev.sol.gui;

import dev.sol.SolClient;
import dev.sol.gui.config.ConfigPanel;
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
import dev.sol.render.nanovg.NVGIcons;
import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.settings.BlockListSetting;
import dev.sol.settings.IconListSetting;
import dev.sol.theme.Theme;
import dev.sol.theme.ThemeManager;
import dev.sol.util.Colors;
import dev.sol.util.UiSounds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

/**
 * Sol Client ClickGUI ("Sol Hub").
 *
 * One centred window:
 *   row 1  brand · search · Themes · Configs
 *   row 2  category tabs with a sliding underline
 *   body   a scrollable grid of module cards (name, description, switch, keybind)
 *
 * Left-click a card toggles it, right-click (or the ··· button) opens the
 * module's {@link ModuleSettingsWindow}. Typing anywhere searches all modules.
 */
public class ClickGuiScreen extends class_437 implements NvgDrawable {
   private static final ClickGuiState STATE = new ClickGuiState();
   private static final float RADIUS = 10.0F;
   private static final float TOP_H = 54.0F;
   private static final float TABS_H = 38.0F;
   private static final float PAD = 18.0F;
   private static final float TITLE_H = 40.0F;
   private static final float CARD_MIN_W = 212.0F;
   private static final float CARD_H = 66.0F;
   private static final float CARD_GAP = 10.0F;
   private static final float SEARCH_W = 210.0F;
   private static final float BTN_H = 26.0F;
   private final Animation openAnim = new Animation(200.0F, 0.0F);
   private final Animation tabX = new Animation(180.0F, 0.0F);
   private final Animation tabW = new Animation(180.0F, 0.0F);
   private final Animation scroll = new Animation(170.0F, 0.0F);
   private final Map<Module, Animation> cardHover = new HashMap();
   private final Map<Module, Animation> cardEnable = new HashMap();
   private final Map<String, List<String>> descCache = new HashMap();
   private final List<CardHit> cardHits = new ArrayList();
   private final List<float[]> tabHits = new ArrayList();
   private final ConfigPanel configPanel = new ConfigPanel();
   private final ThemesView themesView;
   private final StringBuilder search = new StringBuilder();
   private final class_437 parent;
   private boolean closing;
   private boolean searchFocused;
   private boolean tabsInit;
   private float maxScroll;
   private ModuleSettingsWindow settingsWindow;
   private boolean settingsPressed;
   private boolean themesPressed;
   private Module hoveredCard;
   // layout, refreshed every frame
   private float wx;
   private float wy;
   private float ww;
   private float wh;
   private float searchX;
   private float searchY;
   private float themesBtnX;
   private float themesBtnW;
   private float configsBtnX;
   private float configsBtnW;
   private float gridTop;
   private float gridBottom;

   public ClickGuiScreen() {
      this((class_437)null);
   }

   public ClickGuiScreen(class_437 parent) {
      super(class_2561.method_43470("Sol Client"));
      this.parent = parent;
      this.themesView = new ThemesView(SolClient.themes());
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
      int alpha = (int)(110.0F * this.openAnim.value());
      guiGraphics.method_25294(0, 0, this.field_22789, this.field_22790, alpha << 24 | 0x040405);
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

   // ------------------------------------------------------------------ helpers

   private Theme theme() {
      return SolClient.themes().current();
   }

   private boolean searching() {
      return !this.search.toString().isBlank();
   }

   private boolean onThemes() {
      return ClickGuiState.THEMES_TAB.equals(STATE.tab()) && !this.searching();
   }

   private List<Module> visibleModules() {
      ModuleManager modules = SolClient.modules();
      if (this.searching()) {
         String q = this.search.toString().toLowerCase(Locale.ROOT).trim();
         List<Module> out = new ArrayList();

         for(Category c : Category.values()) {
            for(Module m : modules.inCategory(c)) {
               if (m.getName().toLowerCase(Locale.ROOT).contains(q) || m.getDescription() != null && m.getDescription().toLowerCase(Locale.ROOT).contains(q)) {
                  out.add(m);
               }
            }
         }

         return out;
      } else {
         Category c = STATE.category();
         return c == null ? List.of() : modules.inCategory(c);
      }
   }

   private Animation anim(Map<Module, Animation> map, Module m, float ms, float initial) {
      return (Animation)map.computeIfAbsent(m, (k) -> new Animation(ms, initial));
   }

   private void selectTab(String tab) {
      if (!tab.equals(STATE.tab())) {
         STATE.setTab(tab);
         this.scroll.snapTo(0.0F);
         UiSounds.select();
      }

      this.search.setLength(0);
      this.searchFocused = false;
   }

   private List<String> wrap(NVGRenderer vg, String text, float maxW, float size, int maxLines) {
      String key = text + "|" + (int)maxW + "|" + size;
      List<String> cached = (List)this.descCache.get(key);
      if (cached != null) {
         return cached;
      } else {
         List<String> lines = new ArrayList();
         if (text != null && !text.isBlank()) {
            StringBuilder line = new StringBuilder();
            String[] words = text.trim().split("\\s+");

            for(int i = 0; i < words.length; ++i) {
               String candidate = line.isEmpty() ? words[i] : line + " " + words[i];
               if (vg.textWidth(candidate, size) > maxW && !line.isEmpty()) {
                  lines.add(line.toString());
                  line.setLength(0);
                  line.append(words[i]);
                  if (lines.size() == maxLines) {
                     break;
                  }
               } else {
                  line.setLength(0);
                  line.append(candidate);
               }
            }

            if (lines.size() < maxLines && !line.isEmpty()) {
               lines.add(line.toString());
            } else if (lines.size() == maxLines) {
               String last = (String)lines.get(maxLines - 1);
               while(!last.isEmpty() && vg.textWidth(last + "...", size) > maxW) {
                  last = last.substring(0, last.length() - 1);
               }

               lines.set(maxLines - 1, last.trim() + "...");
            }
         }

         this.descCache.put(key, lines);
         return lines;
      }
   }

   // ------------------------------------------------------------------ settings window sync

   private Module findModule(String key) {
      if (key == null) {
         return null;
      } else {
         for(Module module : SolClient.modules().all()) {
            if (ModuleSettingsWindow.keyOf(module).equals(key)) {
               return module;
            }
         }

         return null;
      }
   }

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

   private void openSettings(Module module) {
      STATE.setExpanded(ModuleSettingsWindow.keyOf(module), true);
      UiSounds.select();
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
            float mx = modal ? -10000.0F : mouseX;
            float my = modal ? -10000.0F : mouseY;
            this.ww = Math.min(920.0F, Math.max(Math.min(640.0F, uiWidth - 20.0F), uiWidth - 80.0F));
            this.wh = Math.min(600.0F, uiHeight - 80.0F);
            this.wx = (uiWidth - this.ww) / 2.0F;
            this.wy = (uiHeight - this.wh) / 2.0F - 6.0F + (1.0F - t) * 18.0F;
            vg.save();
            vg.alpha(t);
            Theme theme = this.theme();
            vg.glow(this.wx, this.wy, this.ww, this.wh, RADIUS, 22.0F, Colors.withAlpha(-16777216, 0.55F));
            vg.rect(this.wx, this.wy, this.ww, this.wh, RADIUS, Colors.withAlpha(theme.background(), 0.95F));
            vg.rectOutline(this.wx, this.wy, this.ww, this.wh, RADIUS, 1.0F, theme.outline());
            this.renderTopRow(vg, mx, my);
            this.renderTabs(vg, mx, my);
            if (this.onThemes()) {
               float cx = this.wx + PAD;
               float cy = this.wy + TOP_H + TABS_H + PAD;
               this.renderTitle(vg, "Themes", "Accent colors and sounds", cx, cy);
               this.gridTop = cy + TITLE_H;
               this.gridBottom = this.wy + this.wh - PAD;
               this.themesView.render(vg, mx, my, cx, this.gridTop, this.ww - PAD * 2.0F - 10.0F, this.gridBottom - this.gridTop);
               this.cardHits.clear();
            } else {
               this.renderGrid(vg, mx, my);
            }

            this.renderFooter(vg, uiWidth, uiHeight);
            if (this.settingsWindow != null) {
               this.settingsWindow.render(vg, mouseX, mouseY, uiWidth, uiHeight);
            }

            this.configPanel.render(vg, mouseX, mouseY, uiWidth, uiHeight);
            vg.restore();
         }
      }

   }

   private void renderTopRow(NVGRenderer vg, float mx, float my) {
      Theme theme = this.theme();
      float cy = this.wy + TOP_H / 2.0F;
      // brand
      float bx = this.wx + PAD;
      drawSun(vg, bx + 8.0F, cy, 5.0F, theme.accent());
      float tx = bx + 24.0F;
      tx += vg.textBold("Sol", tx, cy, 18.0F, theme.textPrimary()) + 5.0F;
      vg.text("Client", tx, cy + 0.5F, 14.0F, theme.textDisabled());
      // buttons (right aligned): Configs, Themes
      float right = this.wx + this.ww - PAD;
      this.configsBtnW = vg.textWidth("Configs", 12.5F) + 30.0F;
      this.configsBtnX = right - this.configsBtnW;
      this.themesBtnW = vg.textWidth("Themes", 12.5F) + 30.0F;
      this.themesBtnX = this.configsBtnX - 8.0F - this.themesBtnW;
      this.button(vg, "Themes", this.themesBtnX, this.themesBtnW, cy, mx, my, this.onThemes(), 0);
      this.button(vg, "Configs", this.configsBtnX, this.configsBtnW, cy, mx, my, this.configPanel.isOpen(), 1);
      // search pill
      this.searchX = this.themesBtnX - 12.0F - SEARCH_W;
      this.searchY = cy - 14.0F;
      float sh = 28.0F;
      vg.rect(this.searchX, this.searchY, SEARCH_W, sh, sh / 2.0F, Colors.withAlpha(-16777216, 0.35F));
      vg.rectOutline(this.searchX, this.searchY, SEARCH_W, sh, sh / 2.0F, 1.0F, this.searchFocused ? theme.accent() : theme.outline());
      float ix = this.searchX + 14.0F;
      vg.circleOutline(ix, cy - 1.0F, 4.0F, 1.4F, this.searchFocused ? theme.accent() : theme.textDisabled());
      vg.line(ix + 3.0F, cy + 2.0F, ix + 5.5F, cy + 4.5F, 1.4F, this.searchFocused ? theme.accent() : theme.textDisabled());
      float textX = this.searchX + 26.0F;
      if (this.search.isEmpty() && !this.searchFocused) {
         vg.text("Search modules", textX, cy, 12.0F, theme.textDisabled());
      } else {
         float w = vg.textTruncated(this.search.toString(), textX, cy, 12.0F, theme.textPrimary(), SEARCH_W - 50.0F);
         if (this.searchFocused && System.nanoTime() / 450000000L % 2L == 0L) {
            vg.rect(textX + w + 1.5F, cy - 6.0F, 1.2F, 12.0F, 0.0F, theme.accent());
         }
      }

      if (!this.search.isEmpty()) {
         vg.cross(this.searchX + SEARCH_W - 22.0F, cy - 5.0F, 10.0F, 1.4F, theme.textMuted());
      }

      vg.rect(this.wx, this.wy + TOP_H, this.ww, 1.0F, 0.0F, theme.outline());
   }

   private void button(NVGRenderer vg, String label, float x, float w, float cy, float mx, float my, boolean active, int glyph) {
      Theme theme = this.theme();
      float y = cy - BTN_H / 2.0F;
      boolean hover = mx >= x && mx <= x + w && my >= y && my <= y + BTN_H;
      int fill = active ? Colors.withAlpha(theme.accent(), 0.18F) : (hover ? theme.rowHover() : 0);
      if (fill != 0) {
         vg.rect(x, y, w, BTN_H, 6.0F, fill);
      }

      vg.rectOutline(x, y, w, BTN_H, 6.0F, 1.0F, active ? theme.accent() : (hover ? theme.textDisabled() : theme.outline()));
      int col = active ? theme.accent() : (hover ? theme.textPrimary() : theme.textMuted());
      float gx = x + 13.0F;
      if (glyph == 0) {
         // palette: three dots
         vg.circle(gx - 2.5F, cy - 2.0F, 2.0F, col);
         vg.circle(gx + 2.5F, cy - 2.0F, 2.0F, col);
         vg.circle(gx, cy + 2.5F, 2.0F, col);
      } else {
         // stacked sheets
         vg.rectOutline(gx - 4.0F, cy - 3.0F, 7.0F, 7.0F, 1.5F, 1.3F, col);
         vg.line(gx - 1.5F, cy - 5.0F, gx + 5.0F, cy - 5.0F, 1.3F, col);
         vg.line(gx + 5.0F, cy - 5.0F, gx + 5.0F, cy + 1.5F, 1.3F, col);
      }

      vg.text(label, x + 23.0F, cy, 12.5F, col);
   }

   private void renderTabs(NVGRenderer vg, float mx, float my) {
      Theme theme = this.theme();
      float y = this.wy + TOP_H;
      float cy = y + TABS_H / 2.0F;
      float x = this.wx + PAD - 8.0F;
      this.tabHits.clear();
      float activeX = -1.0F;
      float activeW = 0.0F;

      for(Category c : Category.values()) {
         String label = c.getDisplayName();
         int icon = NVGIcons.get(c);
         float w = 16.0F + (icon > 0 ? 20.0F : 0.0F) + vg.textWidth(label, 12.5F);
         boolean active = !this.searching() && c.name().equals(STATE.tab());
         boolean hover = mx >= x && mx <= x + w && my >= y && my <= y + TABS_H;
         int col = active ? theme.textPrimary() : (hover ? theme.textPrimary() : theme.textMuted());
         float lx = x + 8.0F;
         if (icon > 0) {
            vg.image(icon, lx, cy - 7.0F, 14.0F, 14.0F, active ? theme.accent() : col);
            lx += 20.0F;
         }

         vg.text(label, lx, cy, 12.5F, col);
         // enabled-count dot
         int on = 0;
         for(Module m : SolClient.modules().inCategory(c)) {
            if (m.isEnabled()) {
               ++on;
            }
         }

         if (on > 0 && c != Category.CLIENT) {
            vg.circle(x + w - 4.0F, cy - 6.0F, 2.0F, Colors.withAlpha(theme.accent(), 0.8F));
         }

         if (active) {
            activeX = x;
            activeW = w;
         }

         this.tabHits.add(new float[]{x, w, (float)c.ordinal()});
         x += w + 4.0F;
      }

      if (activeX >= 0.0F) {
         if (!this.tabsInit) {
            this.tabX.snapTo(activeX);
            this.tabW.snapTo(activeW);
            this.tabsInit = true;
         }

         this.tabX.setTarget(activeX);
         this.tabW.setTarget(activeW);
         vg.rect(this.tabX.value() + 6.0F, y + TABS_H - 2.5F, this.tabW.value() - 12.0F, 2.5F, 1.25F, theme.accent());
      }

      vg.rect(this.wx, y + TABS_H, this.ww, 1.0F, 0.0F, theme.outline());
   }

   private void renderTitle(NVGRenderer vg, String title, String sub, float x, float y) {
      Theme theme = this.theme();
      float cy = y + 12.0F;
      float tw = vg.textBold(title, x, cy, 20.0F, theme.textPrimary());
      vg.text(sub, x + tw + 12.0F, cy + 1.0F, 12.0F, theme.textDisabled());
   }

   private void renderGrid(NVGRenderer vg, float mx, float my) {
      Theme theme = this.theme();
      List<Module> list = this.visibleModules();
      float cx = this.wx + PAD;
      float cy = this.wy + TOP_H + TABS_H + PAD;
      float cw = this.ww - PAD * 2.0F - 10.0F;
      int on = 0;
      for(Module m : list) {
         if (m.isEnabled()) {
            ++on;
         }
      }

      String title;
      if (this.searching()) {
         title = "Search";
      } else {
         Category c = STATE.category();
         title = c == null ? "" : c.getDisplayName();
      }

      this.renderTitle(vg, title, list.size() + (list.size() == 1 ? " module" : " modules") + (on > 0 ? "  \u00b7  " + on + " on" : ""), cx, cy);
      this.gridTop = cy + TITLE_H;
      this.gridBottom = this.wy + this.wh - PAD;
      float viewH = this.gridBottom - this.gridTop;
      int cols = Math.max(1, (int)((cw + CARD_GAP) / (CARD_MIN_W + CARD_GAP)));
      float cardW = (cw - (float)(cols - 1) * CARD_GAP) / (float)cols;
      int rows = (list.size() + cols - 1) / cols;
      float contentH = rows == 0 ? 0.0F : (float)rows * (CARD_H + CARD_GAP) - CARD_GAP;
      this.maxScroll = Math.max(0.0F, contentH - viewH);
      this.scroll.setTarget(Math.clamp(this.scroll.getTarget(), 0.0F, this.maxScroll));
      this.cardHits.clear();
      boolean inView = my >= this.gridTop && my <= this.gridBottom;
      Module newHover = null;
      if (list.isEmpty()) {
         String msg = this.searching() ? "No modules match \"" + this.search + "\"" : "Nothing here yet";
         vg.text(msg, cx + (cw - vg.textWidth(msg, 13.0F)) / 2.0F, this.gridTop + 60.0F, 13.0F, theme.textDisabled());
      }

      vg.save();
      vg.scissor(cx - 6.0F, this.gridTop, cw + 12.0F, viewH);

      for(int i = 0; i < list.size(); ++i) {
         Module m = (Module)list.get(i);
         float x = cx + (float)(i % cols) * (cardW + CARD_GAP);
         float y = this.gridTop + (float)(i / cols) * (CARD_H + CARD_GAP) - this.scroll.value();
         if (!(y + CARD_H < this.gridTop - 4.0F) && !(y > this.gridBottom + 4.0F)) {
            boolean hover = inView && mx >= x && mx <= x + cardW && my >= y && my <= y + CARD_H;
            if (hover) {
               newHover = m;
            }

            CardHit hit = this.renderCard(vg, m, x, y, cardW, hover, mx, my);
            if (y < this.gridBottom && y + CARD_H > this.gridTop) {
               this.cardHits.add(hit);
            }
         }
      }

      vg.restore();
      if (newHover != this.hoveredCard && newHover != null) {
         UiSounds.hover();
      }

      this.hoveredCard = newHover;
      if (this.maxScroll > 0.5F) {
         float thumbH = Math.max(24.0F, viewH * (viewH / (viewH + this.maxScroll)));
         float thumbY = this.gridTop + (viewH - thumbH) * (this.scroll.value() / this.maxScroll);
         vg.rect(this.wx + this.ww - PAD + 2.0F, thumbY, 3.0F, thumbH, 1.5F, Colors.withAlpha(theme.accent(), 0.5F));
      }

   }

   private CardHit renderCard(NVGRenderer vg, Module m, float x, float y, float w, boolean hover, float mx, float my) {
      Theme theme = this.theme();
      Animation h = this.anim(this.cardHover, m, 120.0F, 0.0F);
      Animation e = this.anim(this.cardEnable, m, 160.0F, m.isEnabled() ? 1.0F : 0.0F);
      h.setTarget(hover ? 1.0F : 0.0F);
      e.setTarget(m.isEnabled() ? 1.0F : 0.0F);
      float ht = h.value();
      float et = e.value();
      float lift = -1.5F * ht;
      float yy = y + lift;
      vg.rect(x, yy, w, CARD_H, 8.0F, Colors.lerp(theme.surface(), Colors.withAlpha(Colors.lerp(theme.surface(), theme.accent(), 0.10F), 0.95F), et));
      if (ht > 0.01F) {
         vg.rect(x, yy, w, CARD_H, 8.0F, Colors.withAlpha(-1, 0.03F * ht));
      }

      int border = Colors.lerp(Colors.lerp(theme.outline(), theme.textDisabled(), ht), theme.accent(), et * 0.85F);
      vg.rectOutline(x, yy, w, CARD_H, 8.0F, 1.0F, border);
      // name
      float nameY = yy + 18.0F;
      vg.save();
      vg.scissor(x, yy, w - 50.0F, CARD_H);
      vg.textBold(m.getName(), x + 14.0F, nameY, 13.5F, Colors.lerp(theme.textPrimary(), theme.accentBright(), et * 0.4F));
      vg.restore();
      // switch
      float sw = 28.0F;
      float sh = 15.0F;
      float sx = x + w - 14.0F - sw;
      float sy = nameY - sh / 2.0F;
      vg.rect(sx, sy, sw, sh, sh / 2.0F, Colors.lerp(theme.track(), theme.accent(), et));
      vg.circle(sx + sh / 2.0F + (sw - sh) * et, nameY, sh / 2.0F - 2.5F, Colors.lerp(theme.textMuted(), -1, et));
      // description (2 lines)
      List<String> lines = this.wrap(vg, m.getDescription(), w - 28.0F - 30.0F, 11.0F, 2);
      float dy = yy + 37.0F;
      for(String line : lines) {
         vg.text(line, x + 14.0F, dy, 11.0F, theme.textDisabled());
         dy += 14.0F;
      }

      // bottom-right: keybind chip + settings dots
      float dotsX = x + w - 22.0F;
      float dotsY = yy + CARD_H - 14.0F;
      boolean hasSettings = !m.getSettings().isEmpty();
      boolean dotsHover = mx >= dotsX - 8.0F && mx <= dotsX + 16.0F && my >= dotsY - 9.0F && my <= dotsY + 9.0F;
      if (hasSettings || hover) {
         if (dotsHover) {
            vg.rect(dotsX - 6.0F, dotsY - 8.0F, 20.0F, 16.0F, 4.0F, theme.rowHover());
         }

         int dc = dotsHover ? theme.accent() : Colors.withAlpha(theme.textMuted(), 0.5F + 0.5F * ht);
         for(int i = 0; i < 3; ++i) {
            vg.circle(dotsX + (float)i * 4.0F, dotsY, 1.3F, dc);
         }
      }

      if (m.getKeybind().isBound()) {
         String key = m.getKeybind().keyName();
         float kw = vg.textWidth(key, 9.5F) + 10.0F;
         float kx = dotsX - 12.0F - kw;
         vg.rect(kx, dotsY - 7.0F, kw, 14.0F, 3.0F, Colors.withAlpha(-16777216, 0.35F));
         vg.text(key, kx + 5.0F, dotsY, 9.5F, theme.textMuted());
      }

      return new CardHit(m, x, y, w, CARD_H, sx - 4.0F, dotsX - 8.0F, dotsY - 9.0F);
   }

   private void renderFooter(NVGRenderer vg, float uiWidth, float uiHeight) {
      Theme theme = this.theme();
      String hint = "Click a card to toggle   \u00b7   Right-click for settings   \u00b7   Start typing to search";
      float hy = this.wy + this.wh + 18.0F;
      if (hy < uiHeight - 4.0F) {
         vg.text(hint, (uiWidth - vg.textWidth(hint, 11.0F)) / 2.0F, hy, 11.0F, Colors.withAlpha(theme.textMuted(), 0.75F));
      }

      String version = "v" + SolClient.VERSION;
      vg.text(version, uiWidth - 12.0F - vg.textWidth(version, 10.5F), uiHeight - 12.0F, 10.5F, theme.textDisabled());
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

   private float uiX(double guiX) {
      return OverlayRenderer.guiToUi(guiX);
   }

   private float uiY(double guiY) {
      return OverlayRenderer.guiToUi(guiY);
   }

   private boolean btnHit(float x, float w, float mx, float my) {
      float cy = this.wy + TOP_H / 2.0F;
      return mx >= x && mx <= x + w && my >= cy - BTN_H / 2.0F && my <= cy + BTN_H / 2.0F;
   }

   // ------------------------------------------------------------------ input

   public boolean method_25402(class_11909 event, boolean doubled) {
      float mx = this.uiX(event.comp_4798());
      float my = this.uiY(event.comp_4799());
      int button = event.method_74245();
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
      } else {
         // search pill
         if (mx >= this.searchX && mx <= this.searchX + SEARCH_W && my >= this.searchY && my <= this.searchY + 28.0F) {
            if (!this.search.isEmpty() && mx >= this.searchX + SEARCH_W - 28.0F) {
               this.search.setLength(0);
            }

            this.searchFocused = true;
            UiSounds.select();
            return true;
         } else {
            this.searchFocused = false;
            if (this.btnHit(this.configsBtnX, this.configsBtnW, mx, my)) {
               this.configPanel.open();
               UiSounds.guiOpen();
               return true;
            } else if (this.btnHit(this.themesBtnX, this.themesBtnW, mx, my)) {
               this.selectTab(ClickGuiState.THEMES_TAB);
               return true;
            } else {
               float tabsY = this.wy + TOP_H;
               if (my >= tabsY && my <= tabsY + TABS_H) {
                  for(float[] tab : this.tabHits) {
                     if (mx >= tab[0] && mx <= tab[0] + tab[1]) {
                        this.selectTab(Category.values()[(int)tab[2]].name());
                        return true;
                     }
                  }

                  return true;
               } else if (this.onThemes()) {
                  this.themesPressed = true;
                  this.themesView.mouseClicked(mx, my, button);
                  return true;
               } else {
                  for(CardHit hit : this.cardHits) {
                     if (hit.contains(mx, my)) {
                        boolean onDots = mx >= hit.dotsX && mx <= hit.dotsX + 24.0F && my >= hit.dotsY && my <= hit.dotsY + 18.0F;
                        if (button == 1 || button == 0 && onDots) {
                           this.openSettings(hit.module);
                        } else if (button == 0) {
                           hit.module.toggle();
                           UiSounds.toggle(hit.module.isEnabled());
                        }

                        return true;
                     }
                  }

                  return true;
               }
            }
         }
      }
   }

   public boolean method_25403(class_11909 event, double dx, double dy) {
      float mx = this.uiX(event.comp_4798());
      float my = this.uiY(event.comp_4799());
      if (this.configPanel.isOpen()) {
         return true;
      } else {
         if (this.settingsWindow != null && this.settingsPressed) {
            this.settingsWindow.mouseDragged(mx, my);
         } else if (this.themesPressed) {
            this.themesView.mouseDragged(mx, my);
         }

         return true;
      }
   }

   public boolean method_25406(class_11909 event) {
      if (this.configPanel.isOpen()) {
         return true;
      } else {
         if (this.settingsPressed) {
            this.settingsPressed = false;
            if (this.settingsWindow != null) {
               this.settingsWindow.mouseReleased();
            }
         }

         if (this.themesPressed) {
            this.themesPressed = false;
            this.themesView.mouseReleased();
         }

         return true;
      }
   }

   public boolean method_25401(double mouseX, double mouseY, double scrollX, double scrollY) {
      if (this.configPanel.isOpen()) {
         return true;
      } else if (this.settingsOpen()) {
         this.settingsWindow.onScroll(scrollY);
         return true;
      } else if (this.onThemes()) {
         this.themesView.onScroll(scrollY);
         return true;
      } else {
         if (this.maxScroll > 0.0F) {
            this.scroll.setTarget(Math.clamp(this.scroll.getTarget() - (float)scrollY * 40.0F, 0.0F, this.maxScroll));
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
      } else if (this.onThemes() && this.themesView.isListening()) {
         this.themesView.keyPressed(key);
         return true;
      } else if (this.searchFocused) {
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
      } else if (!event.method_74231() && !this.guiModule().getKeybind().matches(key)) {
         // arrow keys / tab cycle categories
         if (key == 262 || key == 263 || key == 258) {
            Category c = STATE.category();
            int idx = c == null ? 0 : c.ordinal();
            int n = Category.values().length;
            idx = Math.floorMod(idx + (key == 263 ? -1 : 1), n);
            this.selectTab(Category.values()[idx].name());
            return true;
         } else {
            return super.method_25404(event);
         }
      } else {
         this.method_25419();
         return true;
      }
   }

   public boolean method_25400(class_11905 event) {
      if (this.configPanel.isOpen()) {
         this.configPanel.charTyped(event.comp_4793());
         return true;
      } else if (this.settingsOpen()) {
         this.settingsWindow.charTyped(event.comp_4793());
         return true;
      } else if (this.onThemes() && this.themesView.isListening()) {
         this.themesView.charTyped(event.comp_4793());
         return true;
      } else {
         if (!this.searchFocused && event.method_74227() && this.search.isEmpty()) {
            this.searchFocused = true;
            this.scroll.snapTo(0.0F);
         }

         if (this.searchFocused && event.method_74227()) {
            if (this.search.length() < 32) {
               this.search.append(event.method_74226());
               this.scroll.snapTo(0.0F);
            }

            return true;
         } else {
            return super.method_25400(event);
         }
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

   private static final class CardHit {
      final Module module;
      final float x;
      final float y;
      final float w;
      final float h;
      final float switchX;
      final float dotsX;
      final float dotsY;

      CardHit(Module module, float x, float y, float w, float h, float switchX, float dotsX, float dotsY) {
         this.module = module;
         this.x = x;
         this.y = y;
         this.w = w;
         this.h = h;
         this.switchX = switchX;
         this.dotsX = dotsX;
         this.dotsY = dotsY;
      }

      boolean contains(float mx, float my) {
         return mx >= this.x && mx <= this.x + this.w && my >= this.y && my <= this.y + this.h;
      }
   }
}
