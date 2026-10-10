package dev.sol.module.impl;

import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.ColorSetting;
import dev.sol.settings.ModeSetting;
import dev.sol.settings.SliderSetting;
import dev.sol.util.Colors;

public class CustomCrosshairModule extends Module {
   private static final int SOL_ORANGE = -26368;
   public final ModeSetting style = (ModeSetting)this.addSetting(new ModeSetting("Style", "Crosshair shape", "Cross", new String[]{"Dot", "Cross", "Circle", "T-Shape", "Brackets", "Chevron", "Sol"}));
   public final SliderSetting size = (SliderSetting)this.addSetting(new SliderSetting("Size", "Overall crosshair size", (double)7.0F, (double)2.0F, (double)24.0F, (double)1.0F, "px"));
   public final SliderSetting thickness = (SliderSetting)this.addSetting(new SliderSetting("Thickness", "Line / dot thickness", (double)2.0F, (double)1.0F, (double)6.0F, (double)0.5F, "px"));
   public final SliderSetting gap = (SliderSetting)this.addSetting(new SliderSetting("Gap", "Center gap for line styles", (double)3.0F, (double)0.0F, (double)12.0F, (double)1.0F, "px"));
   public final ColorSetting color = (ColorSetting)this.addSetting(new ColorSetting("Color", "Crosshair color", -26368));
   public final BooleanSetting rainbow = (BooleanSetting)this.addSetting(new BooleanSetting("Rainbow", "Cycle through the rainbow", false));
   public final BooleanSetting centerDot = (BooleanSetting)this.addSetting(new BooleanSetting("Center Dot", "Add a filled dot at the very center", false));
   public final BooleanSetting outline = (BooleanSetting)this.addSetting(new BooleanSetting("Outline", "Dark border for contrast on any background", true));
   public final BooleanSetting glow = (BooleanSetting)this.addSetting(new BooleanSetting("Glow", "Soft glow behind the crosshair", true));
   public final BooleanSetting hideVanilla = (BooleanSetting)this.addSetting(new BooleanSetting("Hide Vanilla", "Hide Minecraft's default crosshair", true));

   public CustomCrosshairModule() {
      super("CustomCrosshair", "Draws a custom crosshair", Category.RENDER);
   }

   public boolean shouldHideVanilla() {
      return this.isEnabled() && (Boolean)this.hideVanilla.get();
   }

   private int resolveColor() {
      if ((Boolean)this.rainbow.get()) {
         float hue = (float)(System.nanoTime() % 3000000000L) / 3.0E9F;
         return -16777216 | Colors.hsvToRgb(hue, 0.85F, 1.0F) & 16777215;
      } else {
         return (Integer)this.color.get();
      }
   }

   public void render(NVGRenderer vg, float cx, float cy) {
      int col = this.resolveColor();
      int line = (Boolean)this.outline.get() ? -1342177280 : 0;
      float t = this.thickness.getFloat();
      float s = this.size.getFloat();
      float g = this.gap.getFloat();
      if ((Boolean)this.glow.get()) {
         vg.circleGlow(cx, cy, s + 2.0F, 6.0F, Colors.withAlpha(col, 0.45F));
      }

      label52: {
         switch (var9) {
            case "Chevron":
               if (line != 0) {
                  vg.chevron(cx, cy + s * 0.15F, s + 2.0F, t + 2.0F, line, true);
               }

               vg.chevron(cx, cy + s * 0.15F, s, t, col, true);
               break label52;
            case "T-Shape":
               this.cross(vg, cx, cy, s, t, g, col, line, false, true);
               break label52;
            case "Sol":
               this.logo(vg, cx, cy, s, col);
               break label52;
            case "Dot":
               this.dot(vg, cx, cy, Math.max(1.5F, s * 0.35F), col, line);
               break label52;
            case "Brackets":
               this.brackets(vg, cx, cy, s, t, g, col, line);
               break label52;
            case "Cross":
               this.cross(vg, cx, cy, s, t, g, col, line, true, true);
               break label52;
            case "Circle":
               this.ring(vg, cx, cy, s, t, col, line);
               break label52;
         }

         this.cross(vg, cx, cy, s, t, g, col, line, true, true);
      }

      if ((Boolean)this.centerDot.get() && !this.style.check("Dot") && !this.style.check("Sol")) {
         this.dot(vg, cx, cy, Math.max(1.2F, t * 0.8F), col, line);
      }

   }

   private void dot(NVGRenderer vg, float cx, float cy, float r, int col, int line) {
      if (line != 0) {
         vg.circle(cx, cy, r + 1.0F, line);
      }

      vg.circle(cx, cy, r, col);
   }

   private void ring(NVGRenderer vg, float cx, float cy, float r, float t, int col, int line) {
      if (line != 0) {
         vg.circleOutline(cx, cy, r, t + 2.0F, line);
      }

      vg.circleOutline(cx, cy, r, t, col);
   }

   private void cross(NVGRenderer vg, float cx, float cy, float len, float t, float g, int col, int line, boolean top, boolean bottom) {
      if (line != 0) {
         this.segments(vg, cx, cy, len, t + 2.0F, g, line, top, bottom);
      }

      this.segments(vg, cx, cy, len, t, g, col, top, bottom);
   }

   private void segments(NVGRenderer vg, float cx, float cy, float len, float t, float g, int argb, boolean top, boolean bottom) {
      vg.line(cx + g, cy, cx + g + len, cy, t, argb);
      vg.line(cx - g, cy, cx - g - len, cy, t, argb);
      if (top) {
         vg.line(cx, cy - g, cx, cy - g - len, t, argb);
      }

      if (bottom) {
         vg.line(cx, cy + g, cx, cy + g + len, t, argb);
      }

   }

   private void brackets(NVGRenderer vg, float cx, float cy, float len, float t, float g, int col, int line) {
      float arm = Math.max(2.0F, len * 0.5F);
      float d = len + g * 0.4F;
      if (line != 0) {
         this.drawBrackets(vg, cx, cy, d, arm, t + 2.0F, line);
      }

      this.drawBrackets(vg, cx, cy, d, arm, t, col);
   }

   private void drawBrackets(NVGRenderer vg, float cx, float cy, float d, float arm, float t, int argb) {
      vg.line(cx - d, cy - d, cx - d + arm, cy - d, t, argb);
      vg.line(cx - d, cy - d, cx - d, cy - d + arm, t, argb);
      vg.line(cx + d, cy - d, cx + d - arm, cy - d, t, argb);
      vg.line(cx + d, cy - d, cx + d, cy - d + arm, t, argb);
      vg.line(cx - d, cy + d, cx - d + arm, cy + d, t, argb);
      vg.line(cx - d, cy + d, cx - d, cy + d - arm, t, argb);
      vg.line(cx + d, cy + d, cx + d - arm, cy + d, t, argb);
      vg.line(cx + d, cy + d, cx + d, cy + d - arm, t, argb);
   }

   private void logo(NVGRenderer vg, float cx, float cy, float s, int col) {
      // Sol sun mark: a core disc with 8 rays
      float core = Math.max(1.5F, s * 0.45F);
      float inner = core + Math.max(1.5F, s * 0.35F);
      float outer = inner + Math.max(2.0F, s * 0.6F);
      float stroke = Math.max(1.0F, s * 0.22F);
      vg.circleGlow(cx, cy, core, core, Colors.withAlpha(col, 0.45F));
      vg.circle(cx, cy, core, col);

      for(int i = 0; i < 8; ++i) {
         double a = Math.PI / 4.0 * (double)i;
         float dx = (float)Math.cos(a);
         float dy = (float)Math.sin(a);
         vg.line(cx + dx * inner, cy + dy * inner, cx + dx * outer, cy + dy * outer, stroke, col);
      }

   }
}
