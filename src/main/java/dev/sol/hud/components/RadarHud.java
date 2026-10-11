package dev.sol.hud.components;

import dev.sol.hud.HudComponent;
import dev.sol.hud.HudStyle;
import dev.sol.module.Modules;
import dev.sol.render.nanovg.NVGImages;
import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.suschunk.SusChunkScanner;
import dev.sol.theme.Theme;
import dev.sol.theme.ThemeManager;
import dev.sol.util.Colors;
import java.util.function.BooleanSupplier;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import net.minecraft.class_742;

public class RadarHud extends HudComponent {
   private static final float SIZE = 110.0F;
   private Modules.HudModule module;
   private Modules.SusChunkFinderModule susFinder;
   private ThemeManager themes;

   public RadarHud(Modules.HudModule module, Modules.SusChunkFinderModule susFinder, ThemeManager themes, BooleanSupplier visible) {
      super("radar", 0.997F, 0.28F, visible);
      this.module = module;
      this.susFinder = susFinder;
      this.themes = themes;
   }

   public float measureWidth(NVGRenderer vg) {
      return 110.0F;
   }

   public float measureHeight(NVGRenderer vg) {
      return 110.0F + HudStyle.RULE;
   }

   public void render(NVGRenderer vg, float x, float y, float w, float h) {
      Theme theme = this.themes.current();
      class_1657 self = class_310.method_1551().field_1724;
      if (self != null) {
         HudStyle.panel(vg, theme, x, y, w, h);
         float top = y + HudStyle.RULE;
         float half = Math.min(w, h - HudStyle.RULE) / 2.0F;
         float cx = x + w / 2.0F;
         float cy = top + (h - HudStyle.RULE) / 2.0F;
         int faint = Colors.withAlpha(-1, 0.07F);
         vg.line(cx - half + 4.0F, cy, cx + half - 4.0F, cy, 1.0F, faint);
         vg.line(cx, cy - half + 4.0F, cx, cy + half - 4.0F, 1.0F, faint);
         vg.rectOutline(cx - half * 0.5F, cy - half * 0.5F, half, half, 0.0F, 1.0F, faint);
         float yaw = self.method_36454();
         float facing = (float)Math.toRadians((double)(yaw + 90.0F));
         String[] names = new String[]{"N", "E", "S", "W"};
         float[][] dirs = new float[][]{{0.0F, -1.0F}, {1.0F, 0.0F}, {0.0F, 1.0F}, {-1.0F, 0.0F}};

         for(int i = 0; i < 4; ++i) {
            float sx = screenX(dirs[i][0], dirs[i][1], facing);
            float sy = screenY(dirs[i][0], dirs[i][1], facing);
            float k = (half - 7.0F) / Math.max(Math.max(Math.abs(sx), Math.abs(sy)), 0.001F);
            float mx = cx + sx * k;
            float my = cy + sy * k;
            boolean north = i == 0;
            float lw = vg.textWidthBold(names[i], 8.5F);
            vg.textBold(names[i], mx - lw / 2.0F, my, 8.5F, north ? theme.accent() : theme.textDisabled());
         }

         float range = this.module.radarRange.getFloat();
         if (this.susFinder.isEnabled() && (Boolean)this.susFinder.showOnRadar.get()) {
            int threshold = this.susFinder.scanner.threshold();

            for(SusChunkScanner.Zone zone : this.susFinder.scanner.zones()) {
               this.drawZoneBlip(vg, theme, cx, cy, half, range, facing, (float)(zone.centroidX() - self.method_23317()), (float)(zone.centroidZ() - self.method_23321()), strength(zone.maxScore(), threshold));
            }
         }

         float headSize = Math.max(9.0F, half * 0.16F);
         float reach = half - headSize / 2.0F - 3.0F;

         for(class_742 other : class_310.method_1551().field_1687.method_18456()) {
            if (other != self) {
               float dx = (float)(other.method_23317() - self.method_23317());
               float dz = (float)(other.method_23321() - self.method_23321());
               float rx = screenX(dx, dz, facing);
               float ry = screenY(dx, dz, facing);
               if (!(Math.abs(rx) > range) && !(Math.abs(ry) > range)) {
                  float px = cx + rx / range * reach;
                  float py = cy + ry / range * reach;
                  if ((Boolean)this.module.radarHeads.get()) {
                     int skin = NVGImages.wrapGlTexture(other.method_52814().comp_1626().comp_3627(), 64, 64);
                     if (skin > 0) {
                        float hx = px - headSize / 2.0F;
                        float hy = py - headSize / 2.0F;
                        NVGImages.drawSubImage(vg, skin, 64.0F, 64.0F, 8.0F, 8.0F, 16.0F, 16.0F, hx, hy, headSize, headSize, 1.0F);
                        NVGImages.drawSubImage(vg, skin, 64.0F, 64.0F, 40.0F, 8.0F, 48.0F, 16.0F, hx, hy, headSize, headSize, 1.0F);
                        vg.rectOutline(hx - 0.5F, hy - 0.5F, headSize + 1.0F, headSize + 1.0F, 0.0F, 1.0F, Colors.withAlpha(-16777216, 0.6F));
                        continue;
                     }
                  }

                  vg.rect(px - 2.0F, py - 2.0F, 4.0F, 4.0F, 0.0F, -1);
                  vg.rectOutline(px - 2.5F, py - 2.5F, 5.0F, 5.0F, 0.0F, 1.0F, Colors.withAlpha(-16777216, 0.6F));
               }
            }
         }

         vg.triangle(cx, cy - 5.0F, cx + 3.8F, cy + 3.5F, cx - 3.8F, cy + 3.5F, theme.accent());
      }

   }

   private static float strength(double maxScore, int threshold) {
      return threshold <= 0 ? 1.0F : Math.clamp((float)((maxScore - (double)threshold) / ((double)threshold * (double)2.0F)), 0.0F, 1.0F);
   }

   private void drawZoneBlip(NVGRenderer vg, Theme theme, float cx, float cy, float half, float range, float facing, float dx, float dz, float strength) {
      float dist = (float)Math.sqrt((double)(dx * dx + dz * dz));
      if (!(dist < 0.5F)) {
         float rx = screenX(dx, dz, facing);
         float ry = screenY(dx, dz, facing);
         float m = Math.max(Math.abs(rx), Math.abs(ry));
         boolean clamped = m > range;
         float scale = clamped ? range / m : 1.0F;
         float inset = clamped ? 5.0F : 8.0F;
         float bx = cx + rx * scale / range * (half - inset);
         float by = cy + ry * scale / range * (half - inset);
         double seconds = (double)(System.nanoTime() % 1000000000000L) / (double)1.0E9F;
         float pulse = (float)((double)0.5F + (double)0.5F * Math.sin(seconds * Math.PI * (double)2.0F / 1.8));
         float s = (clamped ? 2.2F : 2.8F + 1.4F * strength) * (1.0F + 0.12F * pulse);
         float alpha = (0.72F + 0.28F * strength) * (0.85F + 0.15F * pulse);
         vg.save();
         vg.translate(bx, by);
         vg.rotate(((float)Math.PI / 4F));
         vg.rect(-s, -s, s * 2.0F, s * 2.0F, 0.0F, Colors.withAlpha(theme.accent(), alpha));
         vg.restore();
         if (clamped && dist <= range * 2.5F) {
            String label = (int)dist + "m";
            float lw = vg.textWidth(label, 8.0F);
            float tx = bx + (cx - bx) * 0.22F;
            float ty = by + (cy - by) * 0.22F;
            vg.rect(tx - lw / 2.0F - 3.0F, ty - 5.0F, lw + 6.0F, 10.0F, 1.0F, HudStyle.panelColor());
            vg.text(label, tx - lw / 2.0F, ty, 8.0F, theme.accent());
         }
      }

   }

   private static float screenX(float dx, float dz, float facing) {
      return (float)((double)(-dx) * Math.sin((double)facing) + (double)dz * Math.cos((double)facing));
   }

   private static float screenY(float dx, float dz, float facing) {
      return (float)(-((double)dx * Math.cos((double)facing) + (double)dz * Math.sin((double)facing)));
   }
}
