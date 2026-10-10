package dev.sol.hud.components;

import dev.sol.hud.HudComponent;
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
      super("radar", 0.006F, 0.45F, visible);
      this.module = module;
      this.susFinder = susFinder;
      this.themes = themes;
   }

   public float measureWidth(NVGRenderer vg) {
      return 110.0F;
   }

   public float measureHeight(NVGRenderer vg) {
      return 110.0F;
   }

   public void render(NVGRenderer vg, float x, float y, float w, float h) {
      Theme theme = this.themes.current();
      class_1657 self = class_310.method_1551().field_1724;
      if (self != null) {
         float radius = w / 2.0F;
         float cx = x + radius;
         float cy = y + radius;
         vg.circle(cx, cy, radius, Colors.withAlpha(-15856621, 0.55F));
         vg.circleOutline(cx, cy, radius * 0.5F, 1.0F, Colors.withAlpha(theme.accent(), 0.22F));
         int cross = Colors.withAlpha(theme.accent(), 0.18F);
         vg.line(cx - radius, cy, cx + radius, cy, 1.0F, cross);
         vg.line(cx, cy - radius, cx, cy + radius, 1.0F, cross);
         float yaw = self.method_36454();
         float facing = (float)Math.toRadians((double)(yaw + 90.0F));
         String[] names = new String[]{"N", "E", "S", "W"};
         float[][] dirs = new float[][]{{0.0F, -1.0F}, {1.0F, 0.0F}, {0.0F, 1.0F}, {-1.0F, 0.0F}};

         for(int i = 0; i < 4; ++i) {
            float sx = screenX(dirs[i][0], dirs[i][1], facing);
            float sy = screenY(dirs[i][0], dirs[i][1], facing);
            float mx = cx + sx * (radius - 9.0F);
            float my = cy + sy * (radius - 9.0F);
            boolean north = i == 0;
            vg.text(names[i], mx - vg.textWidth(names[i], 11.0F) / 2.0F, my, 11.0F, north ? theme.accentBright() : theme.textMuted());
         }

         float range = this.module.radarRange.getFloat();
         if (this.susFinder.isEnabled() && (Boolean)this.susFinder.showOnRadar.get()) {
            int threshold = this.susFinder.scanner.threshold();

            for(SusChunkScanner.Zone zone : this.susFinder.scanner.zones()) {
               this.drawZoneBlip(vg, theme, cx, cy, radius, range, facing, (float)(zone.centroidX() - self.method_23317()), (float)(zone.centroidZ() - self.method_23321()), strength(zone.maxScore(), threshold));
            }
         }

         vg.circleGlow(cx, cy, 3.0F, 4.0F, Colors.withAlpha(theme.accent(), 0.6F));
         vg.circle(cx, cy, 3.0F, theme.accentBright());
         float headSize = Math.max(10.0F, radius * 0.17F);

         for(class_742 other : class_310.method_1551().field_1687.method_18456()) {
            if (other != self) {
               float dx = (float)(other.method_23317() - self.method_23317());
               float dz = (float)(other.method_23321() - self.method_23321());
               float dist = (float)Math.sqrt((double)(dx * dx + dz * dz));
               if (!(dist > range)) {
                  float t = dist / range;
                  float px = cx + screenX(dx, dz, facing) / Math.max(dist, 0.001F) * t * (radius - headSize / 2.0F - 3.0F);
                  float py = cy + screenY(dx, dz, facing) / Math.max(dist, 0.001F) * t * (radius - headSize / 2.0F - 3.0F);
                  if ((Boolean)this.module.radarHeads.get()) {
                     int skin = NVGImages.wrapGlTexture(other.method_52814().comp_1626().comp_3627(), 64, 64);
                     if (skin > 0) {
                        float hx = px - headSize / 2.0F;
                        float hy = py - headSize / 2.0F;
                        NVGImages.drawSubImage(vg, skin, 64.0F, 64.0F, 8.0F, 8.0F, 16.0F, 16.0F, hx, hy, headSize, headSize, 1.0F);
                        NVGImages.drawSubImage(vg, skin, 64.0F, 64.0F, 40.0F, 8.0F, 48.0F, 16.0F, hx, hy, headSize, headSize, 1.0F);
                        vg.rectOutline(hx - 1.0F, hy - 1.0F, headSize + 2.0F, headSize + 2.0F, 3.0F, 1.0F, Colors.withAlpha(-1, 0.35F));
                        continue;
                     }
                  }

                  vg.circleGlow(px, py, 2.5F, 3.0F, Colors.withAlpha(-1, 0.4F));
                  vg.circle(px, py, 2.5F, -1);
               }
            }
         }
      }

   }

   private static float strength(double maxScore, int threshold) {
      return threshold <= 0 ? 1.0F : Math.clamp((float)((maxScore - (double)threshold) / ((double)threshold * (double)2.0F)), 0.0F, 1.0F);
   }

   private void drawZoneBlip(NVGRenderer vg, Theme theme, float cx, float cy, float radius, float range, float facing, float dx, float dz, float strength) {
      float dist = (float)Math.sqrt((double)(dx * dx + dz * dz));
      if (!(dist < 0.5F)) {
         boolean clamped = dist > range;
         float t = Math.min(dist / range, 1.0F);
         float inset = clamped ? 6.0F : 9.0F;
         float bx = cx + screenX(dx, dz, facing) / dist * t * (radius - inset);
         float by = cy + screenY(dx, dz, facing) / dist * t * (radius - inset);
         double seconds = (double)(System.nanoTime() % 1000000000000L) / (double)1.0E9F;
         float pulse = (float)((double)0.5F + (double)0.5F * Math.sin(seconds * Math.PI * (double)2.0F / 1.8));
         float half = (clamped ? 2.4F : 3.0F + 1.5F * strength) * (1.0F + 0.1F * pulse);
         float alpha = (0.72F + 0.28F * strength) * (0.88F + 0.12F * pulse);
         int accent = theme.accent();
         vg.glow(bx - half, by - half, half * 2.0F, half * 2.0F, half * 0.6F, 3.5F + 2.5F * pulse, Colors.withAlpha(accent, (0.3F + 0.25F * strength) * alpha));
         vg.rect(bx - half, by - half, half * 2.0F, half * 2.0F, half * 0.6F, Colors.withAlpha(accent, alpha));
         vg.rectOutline(bx - half, by - half, half * 2.0F, half * 2.0F, half * 0.6F, 1.0F, Colors.withAlpha(theme.accentBright(), 0.55F * alpha));
         if (clamped && dist <= range * 2.5F) {
            String label = (int)dist + "m";
            float lw = vg.textWidth(label, 8.5F);
            float tx = bx + (cx - bx) * 0.24F;
            float ty = by + (cy - by) * 0.24F;
            vg.rect(tx - lw / 2.0F - 3.0F, ty - 5.5F, lw + 6.0F, 11.0F, 5.5F, Colors.withAlpha(-15856621, 0.72F));
            vg.text(label, tx - lw / 2.0F, ty, 8.5F, Colors.withAlpha(theme.accentBright(), 0.95F));
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
