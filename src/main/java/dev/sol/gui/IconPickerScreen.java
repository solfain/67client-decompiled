package dev.sol.gui;

import dev.sol.SolClient;
import dev.sol.gui.picker.PickerGrid;
import dev.sol.gui.widget.ColorWidget;
import dev.sol.hud.HudStyle;
import dev.sol.render.NvgDrawable;
import dev.sol.render.OverlayRenderer;
import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.settings.ColorSetting;
import dev.sol.theme.Theme;
import dev.sol.theme.ThemeManager;
import dev.sol.util.Colors;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import org.joml.Matrix3x2fStack;

public class IconPickerScreen extends class_437 implements NvgDrawable {
   private static final float HEADER_H = 44.0F;
   private static final float TOOLBAR_H = 30.0F;
   private static final float FOOTER_H = 26.0F;
   private static final float PAD = 14.0F;
   private static final float ROW_H = 30.0F;
   private static final float COL_MIN_W = 200.0F;
   private static final float GAP = 4.0F;
   private static final float ICON = 16.0F;
   private static final float BOX = 12.0F;
   private static final float SWATCH = 10.0F;
   private static final float TOGGLE_W = 92.0F;
   private class_437 parent;
   private PickerGrid model;
   private ThemeManager themes;
   private final StringBuilder search = new StringBuilder();
   private boolean searchFocused;
   private boolean selectedOnly;
   private List<PickerGrid.Cell> filtered = List.of();
   private String lastQuery = null;
   private boolean lastSelectedOnly;
   private boolean filterDirty = true;
   private float scroll;
   private float maxScroll;
   private ColorSetting colorSetting;
   private String colorTitle;
   private ColorWidget colorWidget;
   private final float[] popupRect = new float[4];
   private final float[] closeRect = new float[4];
   private final float[] searchRect = new float[4];
   private final float[] toggleRect = new float[4];

   public IconPickerScreen(class_437 parent, PickerGrid model, ThemeManager themes) {
      super(class_2561.method_43470(model.title()));
      this.parent = parent;
      this.model = model;
      this.themes = themes;
   }

   private void refreshFilter() {
      String q = this.search.toString().trim().toLowerCase(Locale.ROOT);
      if (this.filterDirty || !q.equals(this.lastQuery) || this.selectedOnly != this.lastSelectedOnly) {
         this.lastQuery = q;
         this.lastSelectedOnly = this.selectedOnly;
         this.filterDirty = false;
         List<PickerGrid.Cell> out = new ArrayList();

         for(PickerGrid.Cell cell : this.model.cells()) {
            if ((!this.selectedOnly || cell.selected()) && (q.isEmpty() || cell.matches(q))) {
               out.add(cell);
            }
         }

         this.filtered = out;
         this.scroll = 0.0F;
      }

   }

   private Layout layout() {
      float uiW = OverlayRenderer.uiWidth();
      float uiH = OverlayRenderer.uiHeight();
      float panelW = Math.min(uiW * 0.72F, 780.0F);
      float panelH = Math.min(uiH * 0.80F, 560.0F);
      float px = (float)Math.round((uiW - panelW) / 2.0F);
      float py = (float)Math.round((uiH - panelH) / 2.0F);
      float listX = px + PAD;
      float listY = py + HEADER_H + TOOLBAR_H + 8.0F;
      float listW = panelW - PAD * 2.0F;
      float listH = panelH - HEADER_H - TOOLBAR_H - 8.0F - FOOTER_H;
      int cols = Math.max(1, (int)((listW + GAP) / (COL_MIN_W + GAP)));
      float colW = (listW - GAP * (float)(cols - 1)) / (float)cols;
      return new Layout(px, py, panelW, panelH, listX, listY, listW, listH, cols, colW);
   }

   private float contentHeight(Layout l) {
      int rows = (this.filtered.size() + l.cols - 1) / l.cols;
      return (float)rows * (ROW_H + GAP);
   }

   private float rowX(Layout l, int index) {
      return l.listX + (float)(index % l.cols) * (l.colW + GAP);
   }

   private float rowY(Layout l, int index) {
      return l.listY - this.scroll + (float)(index / l.cols) * (ROW_H + GAP);
   }

   public void method_25394(class_332 gg, int mouseX, int mouseY, float partialTick) {
      this.refreshFilter();
      Layout l = this.layout();
      this.maxScroll = Math.max(0.0F, this.contentHeight(l) - l.listH);
      this.scroll = Math.clamp(this.scroll, 0.0F, this.maxScroll);
      Theme theme = this.themes.current();
      float uiScale = OverlayRenderer.uiScale();
      double guiScale = (double)this.field_22787.method_22683().method_4495();
      float k = (float)((double)uiScale / guiScale);
      float mx = OverlayRenderer.uiMouseX();
      float my = OverlayRenderer.uiMouseY();
      gg.method_25294(0, 0, this.field_22789, this.field_22790, Colors.withAlpha(0xFF000000, 0.55F));
      fill(gg, k, l.px, l.py, l.panelW, l.panelH, Colors.withAlpha(0xFF0E0E10, 0.97F));
      fill(gg, k, l.px, l.py + HEADER_H, l.panelW, 1.0F, Colors.withAlpha(0xFFFFFFFF, 0.06F));
      gg.method_44379(intOf(l.listX * k), intOf(l.listY * k), intOf((l.listX + l.listW) * k), intOf((l.listY + l.listH) * k));
      int first = Math.max(0, (int)(this.scroll / (ROW_H + GAP)) * l.cols);
      int last = Math.min(this.filtered.size(), first + l.cols * ((int)(l.listH / (ROW_H + GAP)) + 2));
      boolean popup = this.colorSetting != null;
      Matrix3x2fStack pose = gg.method_51448();

      for(int i = first; i < last; ++i) {
         PickerGrid.Cell cell = (PickerGrid.Cell)this.filtered.get(i);
         float x = this.rowX(l, i);
         float y = this.rowY(l, i);
         boolean on = cell.selected();
         boolean hover = !popup && mx >= x && mx <= x + l.colW && my >= y && my <= y + ROW_H && my >= l.listY && my <= l.listY + l.listH;
         int bg = on ? Colors.withAlpha(theme.accent(), 0.12F) : Colors.withAlpha(0xFFFFFFFF, 0.03F);
         fill(gg, k, x, y, l.colW, ROW_H, bg);
         if (hover) {
            fill(gg, k, x, y, l.colW, ROW_H, Colors.withAlpha(0xFFFFFFFF, 0.05F));
         }

         float ix = x + 8.0F;
         float iy = y + (ROW_H - ICON) / 2.0F;
         float sc = ICON * k / 16.0F;
         pose.pushMatrix();
         pose.translate(ix * k, iy * k);
         pose.scale(sc, sc);
         gg.method_51427(cell.icon(), 0, 0);
         pose.popMatrix();
      }

      gg.method_44380();
   }

   private static void fill(class_332 gg, float k, float x, float y, float w, float h, int color) {
      int x0 = intOf(x * k);
      int y0 = intOf(y * k);
      int x1 = Math.max(x0 + 1, intOf((x + w) * k));
      int y1 = Math.max(y0 + 1, intOf((y + h) * k));
      gg.method_25294(x0, y0, x1, y1, color);
   }

   private static int intOf(float v) {
      return Math.round(v);
   }

   public void renderNvg(NVGRenderer vg, float mouseX, float mouseY, float uiWidth, float uiHeight) {
      if (vg.hasFont()) {
         Theme theme = this.themes.current();
         Layout l = this.layout();
         boolean blink = System.nanoTime() / 400000000L % 2L == 0L;
         boolean popup = this.colorSetting != null;
         vg.rectOutline(l.px, l.py, l.panelW, l.panelH, 0.0F, 1.0F, Colors.withAlpha(0xFFFFFFFF, 0.08F));
         vg.rect(l.px, l.py, l.panelW, HudStyle.RULE, 0.0F, theme.accent());
         float tx = l.px + PAD;
         float tw = vg.textBold(this.model.title(), tx, l.py + 18.0F, 15.0F, theme.textPrimary());
         String count = this.model.activeCount() + " ON";
         HudStyle.spaced(vg, count, tx + tw + 10.0F, l.py + 18.5F, 8.5F, theme.accent(), true);
         HudStyle.spaced(vg, "CHOOSE WHAT TO SHOW", tx, l.py + 33.0F, 8.0F, theme.textDisabled(), false);
         float closeCx = l.px + l.panelW - PAD - 4.0F;
         float closeCy = l.py + 22.0F;
         boolean closeHover = !popup && Math.abs(mouseX - closeCx) < 10.0F && Math.abs(mouseY - closeCy) < 10.0F;
         vg.cross(closeCx - 5.0F, closeCy - 5.0F, 10.0F, 1.6F, closeHover ? theme.accent() : theme.textMuted());
         this.closeRect[0] = closeCx - 10.0F;
         this.closeRect[1] = closeCy - 10.0F;
         this.closeRect[2] = closeCx + 10.0F;
         this.closeRect[3] = closeCy + 10.0F;
         float sy = l.py + HEADER_H + 8.0F;
         float sh = 22.0F;
         float sx = l.listX;
         float sw = l.listW - TOGGLE_W - 8.0F;
         this.searchRect[0] = sx;
         this.searchRect[1] = sy;
         this.searchRect[2] = sx + sw;
         this.searchRect[3] = sy + sh;
         vg.rect(sx, sy, sw, sh, 0.0F, Colors.withAlpha(0xFFFFFFFF, 0.04F));
         vg.rect(sx, sy + sh - 1.0F, sw, 1.0F, 0.0F, this.searchFocused ? theme.accent() : Colors.withAlpha(0xFFFFFFFF, 0.12F));
         float iy = sy + sh / 2.0F;
         if (this.search.length() == 0 && !this.searchFocused) {
            vg.text("Search " + this.filtered.size() + " items", sx + 8.0F, iy, 12.0F, theme.textDisabled());
         } else {
            float w = vg.text(this.search.toString(), sx + 8.0F, iy, 12.0F, theme.textPrimary());
            if (this.searchFocused && blink) {
               vg.rect(sx + 8.0F + w + 1.5F, iy - 6.0F, 1.2F, 12.0F, 0.0F, theme.accent());
            }
         }

         float gx = sx + sw + 8.0F;
         this.toggleRect[0] = gx;
         this.toggleRect[1] = sy;
         this.toggleRect[2] = gx + TOGGLE_W;
         this.toggleRect[3] = sy + sh;
         boolean toggleHover = !popup && inRect(mouseX, mouseY, this.toggleRect);
         vg.rect(gx, sy, TOGGLE_W, sh, 0.0F, this.selectedOnly ? Colors.withAlpha(theme.accent(), 0.18F) : Colors.withAlpha(0xFFFFFFFF, toggleHover ? 0.08F : 0.04F));
         this.checkbox(vg, theme, gx + 8.0F, sy + (sh - BOX) / 2.0F, this.selectedOnly);
         vg.text("Only on", gx + 8.0F + BOX + 7.0F, iy, 12.0F, this.selectedOnly ? theme.textPrimary() : theme.textMuted());
         vg.save();
         vg.scissor(l.listX, l.listY, l.listW, l.listH);
         int first = Math.max(0, (int)(this.scroll / (ROW_H + GAP)) * l.cols);
         int last = Math.min(this.filtered.size(), first + l.cols * ((int)(l.listH / (ROW_H + GAP)) + 2));

         for(int i = first; i < last; ++i) {
            PickerGrid.Cell cell = (PickerGrid.Cell)this.filtered.get(i);
            float x = this.rowX(l, i);
            float y = this.rowY(l, i);
            boolean on = cell.selected();
            float cy = y + ROW_H / 2.0F;
            float right = x + l.colW;
            float bx = right - 8.0F - BOX;
            float swx = bx - 8.0F - SWATCH;
            boolean swHover = !popup && mouseX >= swx - 3.0F && mouseX <= swx + SWATCH + 3.0F && mouseY >= cy - 8.0F && mouseY <= cy + 8.0F;
            vg.textTruncated(cell.label(), x + 8.0F + ICON + 8.0F, cy, 12.0F, on ? theme.textPrimary() : theme.textMuted(), swx - 8.0F - (x + 8.0F + ICON + 8.0F));
            int color = cell.color() | 0xFF000000;
            vg.rect(swx, cy - SWATCH / 2.0F, SWATCH, SWATCH, 0.0F, color);
            vg.rectOutline(swx, cy - SWATCH / 2.0F, SWATCH, SWATCH, 0.0F, 1.0F, swHover ? theme.textPrimary() : Colors.withAlpha(0xFFFFFFFF, 0.25F));
            this.checkbox(vg, theme, bx, cy - BOX / 2.0F, on);
         }

         vg.restore();
         if (this.filtered.isEmpty()) {
            String empty = this.selectedOnly ? "Nothing is on yet. Turn off \"Only on\" to see everything." : "Nothing matches your search.";
            vg.text(empty, l.listX + 4.0F, l.listY + 14.0F, 12.0F, theme.textDisabled());
         }

         if (this.maxScroll > 0.0F) {
            float trackX = l.px + l.panelW - 5.0F;
            float thumbH = Math.max(24.0F, l.listH * (l.listH / this.contentHeight(l)));
            float thumbY = l.listY + (l.listH - thumbH) * (this.scroll / this.maxScroll);
            vg.rect(trackX, l.listY, 2.0F, l.listH, 0.0F, Colors.withAlpha(0xFFFFFFFF, 0.05F));
            vg.rect(trackX, thumbY, 2.0F, thumbH, 0.0F, theme.accent());
         }

         float fy = l.py + l.panelH - FOOTER_H / 2.0F;
         vg.text("Click a row to turn it on or off. Click the color square to change its color.", l.listX, fy, 10.5F, theme.textDisabled());
         if (popup && this.colorWidget != null) {
            this.renderColorPopup(vg, theme);
         } else {
            this.popupRect[0] = this.popupRect[1] = this.popupRect[2] = this.popupRect[3] = 0.0F;
         }
      }

   }

   private void checkbox(NVGRenderer vg, Theme theme, float x, float y, boolean on) {
      if (on) {
         vg.rect(x, y, BOX, BOX, 0.0F, theme.accent());
         vg.checkmark(x + 2.0F, y + 2.0F, BOX - 4.0F, 1.6F, theme.onAccent());
      } else {
         vg.rectOutline(x + 0.5F, y + 0.5F, BOX - 1.0F, BOX - 1.0F, 0.0F, 1.0F, Colors.withAlpha(0xFFFFFFFF, 0.30F));
      }

   }

   private void renderColorPopup(NVGRenderer vg, Theme theme) {
      this.colorWidget.setExpanded(true);
      float uiW = OverlayRenderer.uiWidth();
      float uiH = OverlayRenderer.uiHeight();
      float bodyH = this.colorWidget.height(vg);
      float cardW = 220.0F;
      float cardH = 30.0F + bodyH + 12.0F;
      float cardX = (float)Math.round((uiW - cardW) / 2.0F);
      float cardY = (float)Math.round((uiH - cardH) / 2.0F);
      this.popupRect[0] = cardX;
      this.popupRect[1] = cardY;
      this.popupRect[2] = cardX + cardW;
      this.popupRect[3] = cardY + cardH;
      vg.rect(cardX, cardY, cardW, cardH, 0.0F, 0xFF121215);
      vg.rectOutline(cardX, cardY, cardW, cardH, 0.0F, 1.0F, Colors.withAlpha(0xFFFFFFFF, 0.10F));
      vg.rect(cardX, cardY, cardW, HudStyle.RULE, 0.0F, theme.accent());
      HudStyle.spaced(vg, this.colorTitle.toUpperCase(Locale.ROOT), cardX + 12.0F, cardY + 16.0F, 8.5F, theme.textMuted(), true);
      this.colorWidget.setBounds(cardX + 12.0F, cardY + 30.0F, cardW - 24.0F);
      this.colorWidget.render(vg, OverlayRenderer.uiMouseX(), OverlayRenderer.uiMouseY());
   }

   private int rowAt(float mx, float my, Layout l) {
      if (!(mx < l.listX) && !(mx > l.listX + l.listW) && !(my < l.listY) && !(my > l.listY + l.listH)) {
         float stepX = l.colW + GAP;
         float stepY = ROW_H + GAP;
         int col = (int)((mx - l.listX) / stepX);
         float localY = my - (l.listY - this.scroll);
         int row = (int)(localY / stepY);
         if (col >= 0 && col < l.cols && row >= 0 && mx - l.listX - (float)col * stepX <= l.colW && localY - (float)row * stepY <= ROW_H) {
            int index = row * l.cols + col;
            return index < this.filtered.size() ? index : -1;
         } else {
            return -1;
         }
      } else {
         return -1;
      }
   }

   private boolean onSwatch(float mx, float my, Layout l, int index) {
      float x = this.rowX(l, index);
      float swx = x + l.colW - 8.0F - BOX - 8.0F - SWATCH;
      return mx >= swx - 3.0F && mx <= swx + SWATCH + 3.0F;
   }

   private float floatOf(double guiX) {
      return OverlayRenderer.guiToUi(guiX);
   }

   public boolean method_25402(class_11909 event, boolean doubled) {
      float mx = this.floatOf(event.comp_4798());
      float my = this.floatOf(event.comp_4799());
      Layout l = this.layout();
      int button = event.method_74245();
      if (this.colorSetting != null) {
         if (inRect(mx, my, this.popupRect)) {
            this.colorWidget.mouseClicked(mx, my, button);
         } else {
            this.closeColor();
         }

         return true;
      } else if (inRect(mx, my, this.closeRect)) {
         this.method_25419();
         return true;
      } else if (inRect(mx, my, this.toggleRect)) {
         this.selectedOnly = !this.selectedOnly;
         this.filterDirty = true;
         this.searchFocused = false;
         return true;
      } else if (inRect(mx, my, this.searchRect)) {
         this.searchFocused = true;
         return true;
      } else {
         this.searchFocused = false;
         if (mx >= l.px && mx <= l.px + l.panelW && my >= l.py && my <= l.py + l.panelH) {
            int index = this.rowAt(mx, my, l);
            if (index >= 0) {
               PickerGrid.Cell cell = (PickerGrid.Cell)this.filtered.get(index);
               if (button == 1 || button == 0 && this.onSwatch(mx, my, l, index)) {
                  this.openColor(cell.colorTarget(), cell.label());
               } else if (button == 0) {
                  boolean wasSelected = cell.selected();
                  cell.toggle();
                  if (this.selectedOnly && wasSelected && !cell.selected()) {
                     this.filterDirty = true;
                  }
               }
            }

            return true;
         } else {
            this.method_25419();
            return true;
         }
      }
   }

   private void openColor(ColorSetting target, String title) {
      if (target != null) {
         this.colorSetting = target;
         this.colorTitle = title;
         this.colorWidget = new ColorWidget(this.themes, target);
         this.colorWidget.setExpanded(true);
         this.searchFocused = false;
      }

   }

   private void closeColor() {
      this.colorSetting = null;
      this.colorTitle = null;
      this.colorWidget = null;
   }

   public boolean method_25403(class_11909 event, double dx, double dy) {
      if (this.colorSetting != null && this.colorWidget != null) {
         this.colorWidget.mouseDragged(this.floatOf(event.comp_4798()), this.floatOf(event.comp_4799()));
      }

      return true;
   }

   public boolean method_25406(class_11909 event) {
      if (this.colorWidget != null) {
         this.colorWidget.mouseReleased();
      }

      return true;
   }

   public boolean method_25401(double mouseX, double mouseY, double scrollX, double scrollY) {
      if (this.colorSetting == null) {
         this.scroll = Math.clamp(this.scroll - (float)(scrollY * (double)(ROW_H + GAP)), 0.0F, this.maxScroll);
      }

      return true;
   }

   public boolean method_25404(class_11908 event) {
      int key = event.comp_4795();
      if (this.colorSetting != null) {
         if (this.colorWidget != null && this.colorWidget.isListening()) {
            this.colorWidget.keyPressed(key);
         } else if (key == 256) {
            this.closeColor();
         }

         return true;
      } else if (this.searchFocused) {
         switch (key) {
            case 256:
            case 257:
            case 335:
               this.searchFocused = false;
               break;
            case 259:
               if (this.search.length() > 0) {
                  this.search.deleteCharAt(this.search.length() - 1);
                  this.filterDirty = true;
               }
         }

         return true;
      } else if (key == 256) {
         this.method_25419();
         return true;
      } else {
         return super.method_25404(event);
      }
   }

   public boolean method_25400(class_11905 event) {
      if (this.colorSetting != null) {
         return true;
      } else {
         char c = (char)event.comp_4793();
         boolean valid = c == ' ' || c == '_' || c == ':' || c == '/' || c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
         if (!this.searchFocused && valid && c != ' ') {
            this.searchFocused = true;
         }

         if (this.searchFocused && valid && this.search.length() < 48) {
            this.search.append(Character.toLowerCase(c));
            this.filterDirty = true;
         }

         return true;
      }
   }

   public void method_25419() {
      SolClient.config().save();
      this.field_22787.method_1507(this.parent);
   }

   public boolean method_25421() {
      return false;
   }

   private static boolean inRect(float mx, float my, float[] r) {
      return mx >= r[0] && mx <= r[2] && my >= r[1] && my <= r[3];
   }

   static final class Layout {
      final float px;
      final float py;
      final float panelW;
      final float panelH;
      final float listX;
      final float listY;
      final float listW;
      final float listH;
      final int cols;
      final float colW;

      Layout(float px, float py, float panelW, float panelH, float listX, float listY, float listW, float listH, int cols, float colW) {
         this.px = px;
         this.py = py;
         this.panelW = panelW;
         this.panelH = panelH;
         this.listX = listX;
         this.listY = listY;
         this.listW = listW;
         this.listH = listH;
         this.cols = cols;
         this.colW = colW;
      }
   }
}
