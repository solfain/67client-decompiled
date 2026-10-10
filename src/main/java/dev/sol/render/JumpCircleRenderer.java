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
   private static final class_2960 TEXTURE_RIPPLE = class_2960.method_60655("solclient", "textures/misc/jump/ripple.png");
   private static final class_2960 TEXTURE_HALO = class_2960.method_60655("solclient", "textures/misc/jump/halo.png");
   private static final class_2960 TEXTURE_GLOW = class_2960.method_60655("solclient", "textures/misc/jump/glow.png");
   private static final float RING_STAGGER = 0.14F;

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
            String style = (String)module.style.get();
            if ("Halo".equals(style)) {
               // soft glow underneath, then the spinning dashed ring on top
               class_4588 glow = bufferSource.method_73477(class_12249.method_76002(TEXTURE_GLOW));
               for(JumpCirclesModule.JumpCircle circle : circles) {
                  renderHaloGlow(poseStack, glow, circle, cam, now, lifetime, size, baseRgb);
               }

               class_4588 halo = bufferSource.method_73477(class_12249.method_76002(TEXTURE_HALO));
               float spin = module.spin.getFloat();
               for(JumpCirclesModule.JumpCircle circle : circles) {
                  renderHalo(poseStack, halo, circle, cam, now, lifetime, size, baseRgb, spin);
               }
            } else if ("Pulse".equals(style)) {
               class_4588 glow = bufferSource.method_73477(class_12249.method_76002(TEXTURE_GLOW));
               for(JumpCirclesModule.JumpCircle circle : circles) {
                  renderPulse(poseStack, glow, circle, cam, now, lifetime, size, baseRgb);
               }
            } else {
               class_4588 ripple = bufferSource.method_73477(class_12249.method_76002(TEXTURE_RIPPLE));
               int rings = Math.max(1, module.rings.getInt());
               for(JumpCirclesModule.JumpCircle circle : circles) {
                  renderRipple(poseStack, ripple, circle, cam, now, lifetime, size, baseRgb, rings);
               }
            }
         }
      }

   }

   /** Several thin rings that start one after another and race outwards. */
   private static void renderRipple(class_4587 poseStack, class_4588 buffer, JumpCirclesModule.JumpCircle circle, class_243 cam, long now, float lifetime, float size, int baseRgb, int rings) {
      float age = circle.ageSeconds(now);
      float stagger = Math.min(RING_STAGGER * lifetime, lifetime / (float)(rings + 1));
      float ringLife = Math.max(0.05F, lifetime - stagger * (float)(rings - 1));

      for(int i = 0; i < rings; ++i) {
         float local = age - stagger * (float)i;
         if (!(local < 0.0F) && !(local > ringLife)) {
            float t = local / ringLife;
            float half = lerp(easeOutCubic(t), 0.15F, 1.0F) * size * (1.0F - 0.12F * (float)i);
            float alpha = (1.0F - easeInQuad(t)) * (i == 0 ? 0.95F : 0.7F);
            int rgb = lerpRgb(baseRgb, 16777215, 0.25F * (1.0F - t));
            place(poseStack, circle, cam, circle.yLift + 0.0005F * (float)i);
            emitQuad(poseStack.method_23760(), buffer, half, withAlpha(rgb, alpha));
            poseStack.method_22909();
         }
      }

   }

   private static void renderHaloGlow(class_4587 poseStack, class_4588 buffer, JumpCirclesModule.JumpCircle circle, class_243 cam, long now, float lifetime, float size, int baseRgb) {
      float age = circle.ageSeconds(now);
      float alpha = envelope(age, lifetime) * 0.35F;
      place(poseStack, circle, cam, circle.yLift * 0.5F);
      emitQuad(poseStack.method_23760(), buffer, 0.75F * size, withAlpha(baseRgb, alpha));
      poseStack.method_22909();
   }

   /** A dashed ring that pops in, spins, then tightens and fades. */
   private static void renderHalo(class_4587 poseStack, class_4588 buffer, JumpCirclesModule.JumpCircle circle, class_243 cam, long now, float lifetime, float size, int baseRgb, float spinDegPerSec) {
      float age = circle.ageSeconds(now);
      float scale;
      if (age < 0.22F) {
         scale = easeOutBack(age / 0.22F);
      } else {
         float endT = clamp01((age - (lifetime - 0.35F)) / 0.35F);
         scale = lerp(easeInQuad(endT), 1.0F, 0.55F);
      }

      float alpha = envelope(age, lifetime);
      place(poseStack, circle, cam, circle.yLift);
      poseStack.method_22907(class_7833.field_40716.rotationDegrees(circle.yawDegrees + age * spinDegPerSec));
      emitQuad(poseStack.method_23760(), buffer, 0.7F * size * scale, withAlpha(lerpRgb(baseRgb, 16777215, 0.15F), alpha));
      poseStack.method_22909();
   }

   /** A glowing disc that beats twice (lub-dub) and fades out. */
   private static void renderPulse(class_4587 poseStack, class_4588 buffer, JumpCirclesModule.JumpCircle circle, class_243 cam, long now, float lifetime, float size, int baseRgb) {
      float age = circle.ageSeconds(now);
      float t = clamp01(age / lifetime);
      float beat1 = bump(t, 0.0F, 0.28F);
      float beat2 = bump(t, 0.30F, 0.62F);
      float half = (0.45F + 0.4F * beat1 + 0.55F * beat2 + 0.25F * t) * size;
      float alpha = (0.35F + 0.65F * Math.max(beat1, beat2)) * (1.0F - easeInQuad(t));
      place(poseStack, circle, cam, circle.yLift);
      emitQuad(poseStack.method_23760(), buffer, half, withAlpha(lerpRgb(baseRgb, 16777215, 0.3F * beat2), alpha));
      poseStack.method_22909();
   }

   /** Pushes the pose and moves it to the circle's ground position. Caller must pop. */
   private static void place(class_4587 poseStack, JumpCirclesModule.JumpCircle circle, class_243 cam, float lift) {
      poseStack.method_22903();
      poseStack.method_22904(circle.doubleVal - cam.field_1352, circle.doubleVal2 + (double)lift - cam.field_1351, circle.doubleVal3 - cam.field_1350);
   }

   /** Fade in over 0.12s, hold, fade out over the last 0.3s. */
   private static float envelope(float age, float lifetime) {
      float in = clamp01(age / 0.12F);
      float out = 1.0F - easeInQuad(clamp01((age - (lifetime - 0.3F)) / 0.3F));
      return easeOutCubic(in) * out;
   }

   /** Smooth 0 -> 1 -> 0 hump between a and b. */
   private static float bump(float t, float a, float b) {
      if (!(t <= a) && !(t >= b)) {
         float x = (t - a) / (b - a);
         return class_3532.method_15374((double)(x * (float)Math.PI));
      } else {
         return 0.0F;
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
