package dev.sol.render;

import dev.sol.module.impl.JumpCirclesModule;
import java.util.Deque;
import net.minecraft.class_12249;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_7833;

public final class JumpCircleRenderer {
   private static final class_2960 TEXTURE_SOL = class_2960.method_60655("solclient", "textures/misc/sol.png");
   private static final class_2960 TEXTURE_RING = class_2960.method_60655("solclient", "textures/misc/ring.png");
   private static final float POP_IN_TIME = 0.25F;
   private static final float FADE_OUT_TIME = 0.4F;
   private static final float SHOCKWAVE_TIME = 0.45F;
   private static final float PULSE_SPEED = 10.0F;
   private static final int SHIMMER_TARGET_RGB = 16761566;

   private JumpCircleRenderer() {
   }

   public static void render(class_4597.class_4598 bufferSource, class_4587 poseStack, class_243 cam, JumpCirclesModule module) {
      Deque<JumpCirclesModule.JumpCircle> circles = module.circles();
      if (!circles.isEmpty()) {
         long now = System.nanoTime();
         float lifetime = Math.max(0.1F, module.lifetime.getFloat());

         while(!circles.isEmpty() && ((JumpCirclesModule.JumpCircle)circles.peekFirst()).ageSeconds(now) > lifetime) {
            circles.removeFirst();
         }

         if (!circles.isEmpty()) {
            float size = module.size.getFloat();
            int baseRgb = currentBaseRgb(module);
            class_4588 decalBuffer = bufferSource.method_73477(class_12249.method_76002(TEXTURE_SOL));

            for(JumpCirclesModule.JumpCircle circle : circles) {
               renderDecal(poseStack, decalBuffer, circle, cam, now, lifetime, size, baseRgb);
            }

            if ((Boolean)module.shockwave.get()) {
               class_4588 ringBuffer = bufferSource.method_73477(class_12249.method_76002(TEXTURE_RING));

               for(JumpCirclesModule.JumpCircle circle : circles) {
                  renderShockwave(poseStack, ringBuffer, circle, cam, now, size, baseRgb);
               }
            }
         }
      }

   }

   private static void renderDecal(class_4587 poseStack, class_4588 buffer, JumpCirclesModule.JumpCircle circle, class_243 cam, long now, float lifetime, float size, int baseRgb) {
      float age = circle.ageSeconds(now);
      float scale;
      float alpha;
      if (age < 0.25F) {
         float t = age / 0.25F;
         scale = easeOutBack(t);
         alpha = easeOutCubic(Math.min(1.0F, t * 2.0F));
      } else if (age > lifetime - 0.4F) {
         float t = (age - (lifetime - 0.4F)) / 0.4F;
         scale = lerp(easeOutCubic(t), 1.0F, 1.3F);
         alpha = 1.0F - easeInQuad(t);
      } else {
         scale = 1.0F;
         alpha = 1.0F;
      }

      float pulse = 0.5F + 0.5F * class_3532.method_15374((double)(age * 10.0F));
      alpha *= 0.82F + 0.18F * pulse;
      int rgb = lerpRgb(baseRgb, 16761566, pulse * 0.35F);
      int argb = withAlpha(rgb, alpha);
      poseStack.method_22903();
      poseStack.method_22904(circle.doubleVal - cam.field_1352, circle.doubleVal2 + (double)circle.yLift - cam.field_1351, circle.doubleVal3 - cam.field_1350);
      poseStack.method_22907(class_7833.field_40716.rotationDegrees(180.0F - circle.yawDegrees));
      emitQuad(poseStack.method_23760(), buffer, 0.5F * size * scale, argb);
      poseStack.method_22909();
   }

   private static void renderShockwave(class_4587 poseStack, class_4588 buffer, JumpCirclesModule.JumpCircle circle, class_243 cam, long now, float size, int baseRgb) {
      float age = circle.ageSeconds(now);
      if (!(age >= 0.45F)) {
         float t = age / 0.45F;
         float half = lerp(easeOutCubic(t), 0.35F, 2.2F) * size;
         float alpha = 0.85F * (1.0F - easeInQuad(t));
         int argb = withAlpha(baseRgb, alpha);
         poseStack.method_22903();
         poseStack.method_22904(circle.doubleVal - cam.field_1352, circle.doubleVal2 + (double)(circle.yLift * 0.5F) + (double)0.004F - cam.field_1351, circle.doubleVal3 - cam.field_1350);
         emitQuad(poseStack.method_23760(), buffer, half, argb);
         poseStack.method_22909();
      }

   }

   private static int currentBaseRgb(JumpCirclesModule module) {
      if ((Boolean)module.rainbow.get()) {
         float hue = (float)(System.currentTimeMillis() % 4000L) / 4000.0F;
         return class_3532.method_15369(hue, 0.75F, 1.0F) & 16777215;
      } else {
         return module.baseRgb();
      }
   }

   private static void emitQuad(class_4587.class_4665 pose, class_4588 buffer, float half, int argb) {
      vertex(pose, buffer, -half, -half, 0.0F, 0.0F, argb);
      vertex(pose, buffer, -half, half, 0.0F, 1.0F, argb);
      vertex(pose, buffer, half, half, 1.0F, 1.0F, argb);
      vertex(pose, buffer, half, -half, 1.0F, 0.0F, argb);
   }

   private static void vertex(class_4587.class_4665 pose, class_4588 buffer, float x, float z, float u, float v, int argb) {
      buffer.method_56824(pose, x, 0.0F, z).method_39415(argb).method_22913(u, v).method_22922(class_4608.field_21444).method_60803(15728880).method_60831(pose, 0.0F, 1.0F, 0.0F);
   }

   private static int withAlpha(int rgb, float alpha) {
      int a = (int)(clamp01(alpha) * 255.0F);
      return a << 24 | rgb & 16777215;
   }

   private static int lerpRgb(int from, int to, float t) {
      int r = class_3532.method_48781(t, from >> 16 & 255, to >> 16 & 255);
      int g = class_3532.method_48781(t, from >> 8 & 255, to >> 8 & 255);
      int b = class_3532.method_48781(t, from & 255, to & 255);
      return r << 16 | g << 8 | b;
   }

   private static float clamp01(float t) {
      return t < 0.0F ? 0.0F : (t > 1.0F ? 1.0F : t);
   }

   private static float lerp(float t, float a, float b) {
      return a + (b - a) * t;
   }

   private static float easeOutCubic(float t) {
      t = clamp01(t);
      float inv = 1.0F - t;
      return 1.0F - inv * inv * inv;
   }

   private static float easeInQuad(float t) {
      t = clamp01(t);
      return t * t;
   }

   private static float easeOutBack(float t) {
      t = clamp01(t);
      float c3 = 3.2F;
      float u = t - 1.0F;
      return 1.0F + c3 * u * u * u + 2.2F * u * u;
   }
}
