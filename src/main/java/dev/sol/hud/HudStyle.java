package dev.sol.hud;

import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.theme.Theme;
import dev.sol.util.Colors;

/**
 * Shared look for every Sol HUD element.
 *
 * Square-ish smoked-glass panels, a thin accent rule across the top edge,
 * letter-spaced uppercase captions and bold values. No glows, no gradients,
 * no pills, no side strips.
 */
public final class HudStyle {
   public static final float RADIUS = 2.0F;
   public static final float RULE = 1.5F;
   public static final float PAD = 7.0F;
   public static final float CAPTION = 8.5F;
   public static final float HEADER_H = 17.0F;
   private static final int PANEL = 0xFF0A0B0E;
   private static final int EDGE = 0xFFFFFFFF;

   private HudStyle() {
   }

   public static int panelColor() {
      return Colors.withAlpha(PANEL, 0.68F);
   }

   public static int edgeColor() {
      return Colors.withAlpha(EDGE, 0.06F);
   }

   /** Glass panel with a top accent rule. */
   public static void panel(NVGRenderer vg, Theme theme, float x, float y, float w, float h) {
      vg.rect(x, y, w, h, RADIUS, panelColor());
      vg.rectOutline(x, y, w, h, RADIUS, 1.0F, edgeColor());
      vg.rect(x, y, w, RULE, 0.0F, theme.accent());
   }

   /** Glass panel without the accent rule (for nested/secondary surfaces). */
   public static void plain(NVGRenderer vg, float x, float y, float w, float h) {
      vg.rect(x, y, w, h, RADIUS, panelColor());
      vg.rectOutline(x, y, w, h, RADIUS, 1.0F, edgeColor());
   }

   /** Letter-spaced text; returns the advance. */
   public static float spaced(NVGRenderer vg, String text, float x, float cy, float size, int color, boolean bold) {
      float spacing = size * 0.18F;
      float cx = x;

      for(int i = 0; i < text.length(); ++i) {
         String ch = String.valueOf(text.charAt(i));
         float adv = bold ? vg.textBold(ch, cx, cy, size, color) : vg.text(ch, cx, cy, size, color);
         cx += adv + spacing;
      }

      return text.isEmpty() ? 0.0F : cx - x - spacing;
   }

   public static float spacedWidth(NVGRenderer vg, String text, float size, boolean bold) {
      float spacing = size * 0.18F;
      float w = 0.0F;

      for(int i = 0; i < text.length(); ++i) {
         String ch = String.valueOf(text.charAt(i));
         w += (bold ? vg.textWidthBold(ch, size) : vg.textWidth(ch, size)) + spacing;
      }

      return text.isEmpty() ? 0.0F : w - spacing;
   }

   /**
    * Panel header: spaced uppercase caption on the left, optional muted value on the right,
    * hairline divider underneath. Returns the y where content should start.
    */
   public static float header(NVGRenderer vg, Theme theme, String caption, String right, float x, float y, float w) {
      float cy = y + RULE + HEADER_H / 2.0F;
      spaced(vg, caption.toUpperCase(), x + PAD, cy, CAPTION, theme.accent(), true);
      if (right != null && !right.isEmpty()) {
         float rw = vg.textWidthBold(right, 10.0F);
         vg.textBold(right, x + w - PAD - rw, cy, 10.0F, theme.textMuted());
      }

      float dy = y + RULE + HEADER_H;
      vg.rect(x + PAD, dy, w - PAD * 2.0F, 1.0F, 0.0F, Colors.withAlpha(EDGE, 0.07F));
      return dy + 4.0F;
   }

   public static float headerWidth(NVGRenderer vg, String caption, String right) {
      float w = PAD + spacedWidth(vg, caption.toUpperCase(), CAPTION, true) + PAD;
      if (right != null && !right.isEmpty()) {
         w += 12.0F + vg.textWidthBold(right, 10.0F);
      }

      return w;
   }

   /** Text with a 1px drop shadow (used where there is no panel behind). */
   public static float shadowed(NVGRenderer vg, String text, float x, float cy, float size, int color, boolean bold) {
      int shadow = Colors.withAlpha(0xFF000000, 0.55F * (float)Colors.alpha(color) / 255.0F);
      if (bold) {
         vg.textBold(text, x + 1.0F, cy + 1.0F, size, shadow);
         return vg.textBold(text, x, cy, size, color);
      } else {
         vg.text(text, x + 1.0F, cy + 1.0F, size, shadow);
         return vg.text(text, x, cy, size, color);
      }
   }

   /** Thin flat progress bar (track + fill, square ends). */
   public static void bar(NVGRenderer vg, float x, float y, float w, float h, float frac, int fill) {
      vg.rect(x, y, w, h, 0.0F, Colors.withAlpha(EDGE, 0.10F));
      vg.rect(x, y, Math.max(0.0F, w * Math.clamp(frac, 0.0F, 1.0F)), h, 0.0F, fill);
   }

   /** Four corner brackets around a box (used for the HUD editor). */
   public static void brackets(NVGRenderer vg, float x, float y, float w, float h, float len, float stroke, int color) {
      float l = Math.min(len, Math.min(w, h) / 2.0F);
      vg.line(x, y, x + l, y, stroke, color);
      vg.line(x, y, x, y + l, stroke, color);
      vg.line(x + w, y, x + w - l, y, stroke, color);
      vg.line(x + w, y, x + w, y + l, stroke, color);
      vg.line(x, y + h, x + l, y + h, stroke, color);
      vg.line(x, y + h, x, y + h - l, stroke, color);
      vg.line(x + w, y + h, x + w - l, y + h, stroke, color);
      vg.line(x + w, y + h, x + w, y + h - l, stroke, color);
   }
}
