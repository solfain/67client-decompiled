package dev.sol.render;

import dev.sol.SolClient;
import dev.sol.module.Modules;
import dev.sol.util.Colors;
import net.minecraft.class_12074;
import net.minecraft.class_12249;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_9974;

public final class BlockOutlineRenderer {
   private BlockOutlineRenderer() {
   }

   public static void render(class_4597.class_4598 bufferSource, class_4587 poseStack, class_12074 state, class_243 camera, Modules.BlockOutlineModule module) {
      double dx = (double)state.comp_4932().method_10263() - camera.field_1352;
      double dy = (double)state.comp_4932().method_10264() - camera.field_1351;
      double dz = (double)state.comp_4932().method_10260() - camera.field_1350;
      double seconds = (double)(System.nanoTime() % 1000000000000L) / (double)1.0E9F;
      int color = animatedColor(module, seconds);
      float pulse = pulseFactor(module, seconds);
      float fill = module.fillOpacity.getFloat() / 100.0F;
      if (fill > 0.004F) {
         class_4588 quads = bufferSource.method_73477(class_12249.method_76023());
         int fillColor = Colors.withAlpha(color, fill * (0.75F + 0.25F * pulse));

         for(class_238 box : state.comp_4935().method_1090()) {
            emitBox(poseStack, quads, box.method_1014(-0.002).method_989(dx, dy, dz), fillColor);
         }

         bufferSource.method_37104();
      }

      float thickness = module.thickness.getFloat();
      float glow = module.glow.getFloat() / 100.0F;
      if (glow > 0.02F) {
         class_4588 halo = bufferSource.method_73477(class_12249.method_76017());
         class_9974.method_62296(poseStack, halo, state.comp_4935(), dx, dy, dz, Colors.withAlpha(color, (0.16F + 0.22F * pulse) * glow), thickness * 3.2F);
         bufferSource.method_37104();
         class_4588 mid = bufferSource.method_73477(class_12249.method_76015());
         class_9974.method_62296(poseStack, mid, state.comp_4935(), dx, dy, dz, Colors.withAlpha(color, (0.3F + 0.25F * pulse) * glow), thickness * 2.0F);
         bufferSource.method_37104();
      }

      class_4588 core = bufferSource.method_73477(class_12249.method_76015());
      class_9974.method_62296(poseStack, core, state.comp_4935(), dx, dy, dz, Colors.withAlpha(color, 0.85F + 0.15F * pulse), thickness);
      bufferSource.method_37104();
   }

   private static int animatedColor(Modules.BlockOutlineModule module, double seconds) {
      if ((Boolean)module.rainbow.get()) {
         return Colors.hsvToRgb((float)(seconds * (double)42.0F % (double)360.0F), 0.75F, 1.0F);
      } else {
         int base = ((Boolean)module.themeColor.get() ? SolClient.themes().current().accent() : (Integer)module.color.get()) | -16777216;
         if (module.animation.check("Gradient Flow")) {
            float[] hsv = Colors.rgbToHsv(base);
            float hue = (hsv[0] + (float)(Math.sin(seconds * 1.6) * (double)28.0F)) % 360.0F;
            if (hue < 0.0F) {
               hue += 360.0F;
            }

            return Colors.hsvToRgb(hue, Math.max(0.4F, hsv[1]), hsv[2]);
         } else {
            return base;
         }
      }
   }

   private static float pulseFactor(Modules.BlockOutlineModule module, double seconds) {
      return module.animation.check("Pulse") ? (float)((double)0.5F + (double)0.5F * Math.sin(seconds * Math.PI * (double)2.0F / 1.6)) : 1.0F;
   }

   private static void emitBox(class_4587 poseStack, class_4588 buffer, class_238 b, int color) {
      class_4587.class_4665 pose = poseStack.method_23760();
      float x0 = (float)b.field_1323;
      float y0 = (float)b.field_1322;
      float z0 = (float)b.field_1321;
      float x1 = (float)b.field_1320;
      float y1 = (float)b.field_1325;
      float z1 = (float)b.field_1324;
      quad(buffer, pose, color, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
      quad(buffer, pose, color, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
      quad(buffer, pose, color, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
      quad(buffer, pose, color, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
      quad(buffer, pose, color, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
      quad(buffer, pose, color, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
   }

   private static void quad(class_4588 buffer, class_4587.class_4665 pose, int color, float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz, float dx2, float dy2, float dz2) {
      buffer.method_56824(pose, ax, ay, az).method_39415(color);
      buffer.method_56824(pose, bx, by, bz).method_39415(color);
      buffer.method_56824(pose, cx, cy, cz).method_39415(color);
      buffer.method_56824(pose, dx2, dy2, dz2).method_39415(color);
   }
}
