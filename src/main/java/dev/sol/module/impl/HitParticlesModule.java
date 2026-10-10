package dev.sol.module.impl;

import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.ColorSetting;
import dev.sol.settings.ModeSetting;
import dev.sol.settings.SliderSetting;
import dev.sol.util.Colors;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_5819;

public class HitParticlesModule extends Module {
   public static final int STYLE_SPARKS = 0;
   public static final int STYLE_HEARTS = 1;
   public static final int STYLE_LIGHTNING = 2;
   public static final int STYLE_SOL = 3;
   public final ModeSetting style = (ModeSetting)this.addSetting(new ModeSetting("Style", "Burst style", "Sparks", new String[]{"Sparks", "Hearts", "Lightning", "Sol"}));
   public final SliderSetting amount = (SliderSetting)this.addSetting(new SliderSetting("Amount", "Particles spawned per hit", (double)14.0F, (double)4.0F, (double)40.0F, (double)1.0F));
   public final SliderSetting size = (SliderSetting)this.addSetting(new SliderSetting("Size", "Particle scale", (double)1.0F, 0.3, (double)3.0F, 0.1, "x"));
   public final SliderSetting lifetime = (SliderSetting)this.addSetting(new SliderSetting("Lifetime", "How long the burst lingers", 0.7, 0.3, (double)2.0F, 0.1, "s"));
   public final SliderSetting spread = (SliderSetting)this.addSetting(new SliderSetting("Spread", "How far particles fly out", (double)1.0F, 0.3, (double)2.5F, 0.1, "x"));
   public final ColorSetting color = (ColorSetting)this.addSetting(new ColorSetting("Color", "Particle tint", -26368));
   public final BooleanSetting rainbow = (BooleanSetting)this.addSetting(new BooleanSetting("Rainbow", "Cycle each burst through the rainbow", false));
   public final SliderSetting glow = (SliderSetting)this.addSetting(new SliderSetting("Glow", "Soft outer halo intensity", (double)65.0F, (double)0.0F, (double)100.0F, (double)5.0F, "%"));
   public final BooleanSetting shockwave = (BooleanSetting)this.addSetting(new BooleanSetting("Shockwave", "Expanding ring on each hit", true));
   private final Deque<HitParticle> particles = new ArrayDeque();
   private final Deque<Shock> shocks = new ArrayDeque();
   private static final int MAX_PARTICLES = 600;
   private static final int MAX_SHOCKS = 24;

   public HitParticlesModule() {
      super("HitParticles", "Themed particle bursts when you hit an entity", Category.VISUALS);
   }

   public Deque<HitParticle> particles() {
      return this.particles;
   }

   public Deque<Shock> shocks() {
      return this.shocks;
   }

   protected void onDisable() {
      this.clear();
   }

   public void clear() {
      this.particles.clear();
      this.shocks.clear();
   }

   private int currentRgb() {
      if ((Boolean)this.rainbow.get()) {
         float hue = (float)(System.currentTimeMillis() % 3500L) / 3500.0F * 360.0F;
         return Colors.hsvToRgb(hue, 0.85F, 1.0F) & 16777215;
      } else {
         return (Integer)this.color.get() & 16777215;
      }
   }

   public void onHit(class_1297 target) {
      if (this.isEnabled() && target != null) {
         double cx = target.method_23317();
         double cy = target.method_23318() + (double)target.method_17682() * 0.6;
         double cz = target.method_23321();
         this.spawnAt(cx, cy, cz);
      }

   }

   public void spawnAt(double cx, double cy, double cz) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1687 != null) {
         class_5819 random = mc.field_1687.field_9229;
         int styleId = this.styleId();
         int rgb = this.currentRgb();
         int count = this.amount.getInt();
         float life = this.lifetime.getFloat();
         float spreadScale = this.spread.getFloat();
         float sizeScale = this.size.getFloat();
         long now = System.nanoTime();

         for(int i = 0; i < count; ++i) {
            this.spawnOne(random, styleId, rgb, cx, cy, cz, life, spreadScale, sizeScale, now);
         }

         if ((Boolean)this.shockwave.get()) {
            this.shocks.addLast(new Shock(cx, cy, cz, rgb, styleId, now));

            while(this.shocks.size() > 24) {
               this.shocks.removeFirst();
            }
         }

         while(this.particles.size() > 600) {
            this.particles.removeFirst();
         }
      }

   }

   private void spawnOne(class_5819 random, int styleId, int rgb, double cx, double cy, double cz, float life, float spreadScale, float sizeScale, long now) {
      double theta = random.method_43058() * Math.PI * (double)2.0F;
      double cosPhi = (double)2.0F * random.method_43058() - (double)1.0F;
      double sinPhi = Math.sqrt(Math.max((double)0.0F, (double)1.0F - cosPhi * cosPhi));
      float dx = (float)(sinPhi * Math.cos(theta));
      float dy = (float)cosPhi;
      float dz = (float)(sinPhi * Math.sin(theta));
      float particleLife = life * (0.75F + random.method_43057() * 0.25F);
      float pSize = sizeScale * (0.7F + random.method_43057() * 0.6F);
      float speed;
      float gravity;
      switch (styleId) {
         case 1:
            dx *= 0.5F;
            dz *= 0.5F;
            dy = 0.4F + Math.abs(dy) * 0.5F;
            speed = (1.6F + random.method_43057() * 1.0F) * spreadScale;
            gravity = 1.2F;
            particleLife = life * (1.0F + random.method_43057() * 0.3F);
            break;
         case 2:
            dy *= 0.25F;
            float horiz = class_3532.method_15355(Math.max(1.0E-4F, dx * dx + dz * dz));
            dx /= horiz;
            dz /= horiz;
            speed = (5.0F + random.method_43057() * 3.0F) * spreadScale;
            gravity = 0.6F;
            particleLife = life * (0.45F + random.method_43057() * 0.25F);
            break;
         case 3:
            dy = 0.12F + dy * 0.35F;
            speed = (1.9F + random.method_43057() * 1.4F) * spreadScale;
            gravity = 1.3F;
            particleLife = life * (1.0F + random.method_43057() * 0.25F);
            break;
         default:
            dy = dy * 0.7F + 0.3F;
            speed = (4.0F + random.method_43057() * 3.5F) * spreadScale;
            gravity = 6.5F;
      }

      float vx = dx * speed;
      float vy = dy * speed;
      float vz = dz * speed;
      float rot = random.method_43057() * ((float)Math.PI * 2F);
      float rotSpeed = (random.method_43057() - 0.5F) * 8.0F;
      this.particles.addLast(new HitParticle(cx, cy, cz, vx, vy, vz, rgb, styleId, pSize, rot, rotSpeed, gravity, particleLife, now));
   }

   private int styleId() {
      if (this.style.check("Hearts")) {
         return 1;
      } else if (this.style.check("Lightning")) {
         return 2;
      } else {
         return this.style.check("Sol") ? 3 : 0;
      }
   }

   public float glowStrength() {
      return this.glow.getFloat() / 100.0F;
   }

   public final class HitParticle {
      public double doubleVal;
      public double doubleVal2;
      public double doubleVal3;
      public float floatVal;
      public float floatVal2;
      public float floatVal3;
      public int rgb;
      public int styleId;
      public float size;
      public float rot;
      public float rotSpeed;
      public float gravity;
      public float lifetime;
      public long spawnNanos;

      HitParticle(double ox, double oy, double oz, float vx, float vy, float vz, int rgb, int styleId, float size, float rot, float rotSpeed, float gravity, float lifetime, long spawnNanos) {
         this.doubleVal = ox;
         this.doubleVal2 = oy;
         this.doubleVal3 = oz;
         this.floatVal = vx;
         this.floatVal2 = vy;
         this.floatVal3 = vz;
         this.rgb = rgb;
         this.styleId = styleId;
         this.size = size;
         this.rot = rot;
         this.rotSpeed = rotSpeed;
         this.gravity = gravity;
         this.lifetime = lifetime;
         this.spawnNanos = spawnNanos;
      }

      public float ageSeconds(long nowNanos) {
         return (float)(nowNanos - this.spawnNanos) / 1.0E9F;
      }

      public double doubleOf(float age) {
         return this.doubleVal + (double)(this.floatVal * age);
      }

      public double doubleOf2(float age) {
         return this.doubleVal2 + (double)(this.floatVal2 * age) - (double)0.5F * (double)this.gravity * (double)age * (double)age;
      }

      public double doubleOf3(float age) {
         return this.doubleVal3 + (double)(this.floatVal3 * age);
      }
   }

   public final class Shock {
      public double doubleVal;
      public double doubleVal2;
      public double doubleVal3;
      public int rgb;
      public int styleId;
      public long spawnNanos;

      Shock(double x, double y, double z, int rgb, int styleId, long spawnNanos) {
         this.doubleVal = x;
         this.doubleVal2 = y;
         this.doubleVal3 = z;
         this.rgb = rgb;
         this.styleId = styleId;
         this.spawnNanos = spawnNanos;
      }

      public float ageSeconds(long nowNanos) {
         return (float)(nowNanos - this.spawnNanos) / 1.0E9F;
      }
   }
}
