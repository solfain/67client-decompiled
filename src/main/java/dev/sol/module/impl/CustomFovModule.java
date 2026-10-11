package dev.sol.module.impl;

import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.SliderSetting;
import java.util.Objects;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_310;

public class CustomFovModule extends Module {
   private static final double EASE_SPEED = (double)12.0F;
   private static final double SPRINT_SPEED = 0.2825;
   public final SliderSetting fov = (SliderSetting)this.addSetting((new SliderSetting("FOV", "Target field of view", (double)95.0F, (double)30.0F, (double)140.0F, (double)1.0F, "°")).withLabel((v) -> (int)v + "° (" + tag((int)v) + ")"));
   public final BooleanSetting smooth = (BooleanSetting)this.addSetting(new BooleanSetting("Smooth", "Ease FOV changes in and out", true));
   public final BooleanSetting noSprintZoom = (BooleanSetting)this.addSetting(new BooleanSetting("No Sprint Zoom", "Stop the FOV change when you sprint", true));
   public final BooleanSetting speedFov = (BooleanSetting)this.addSetting(new BooleanSetting("Speed FOV", "Widen the view with your movement speed", false));
   public final SliderSetting speedStrength = (SliderSetting)this.addSetting(new SliderSetting("Speed Strength", "Extra degrees at full sprint", (double)12.0F, (double)0.0F, (double)30.0F, (double)1.0F, "°"));
   private double current = (double)-1.0F;
   private long lastNanos = 0L;

   public CustomFovModule() {
      super("CustomFOV", "Overrides the field of view", Category.RENDER);
      SliderSetting var10000 = this.speedStrength;
      BooleanSetting var10001 = this.speedFov;
      Objects.requireNonNull(var10001);
      var10000.visibleWhen(var10001::get);
   }

   public float fovMultiplier(float vanillaSprintMultiplier) {
      class_310 mc = class_310.method_1551();
      int optionsFov = (Integer)mc.field_1690.method_41808().method_41753();
      if (optionsFov <= 0) {
         return vanillaSprintMultiplier;
      } else {
         double target = this.isEnabled() ? (Double)this.fov.get() + this.speedBonus(mc) : (double)optionsFov;
         long now = System.nanoTime();
         double dt = this.lastNanos == 0L ? (double)0.0F : (double)(now - this.lastNanos) / (double)1.0E9F;
         this.lastNanos = now;
         if (this.current < (double)0.0F) {
            this.current = (double)optionsFov;
         }

         if (!(Boolean)this.smooth.get()) {
            this.current = target;
         } else {
            double t = (double)1.0F - Math.exp((double)-12.0F * Math.max((double)0.0F, dt));
            this.current += (target - this.current) * t;
            if (Math.abs(this.current - target) < 0.05) {
               this.current = target;
            }
         }

         if (!this.isEnabled() && this.current == (double)optionsFov) {
            return vanillaSprintMultiplier;
         } else {
            float sprint = this.isEnabled() && (Boolean)this.noSprintZoom.get() ? 1.0F : vanillaSprintMultiplier;
            return (float)(this.current / (double)optionsFov) * sprint;
         }
      }
   }

   public double currentFov() {
      return this.current;
   }

   private double speedBonus(class_310 mc) {
      if (!(Boolean)this.speedFov.get()) {
         return (double)0.0F;
      } else {
         class_1657 p = mc.field_1724;
         if (p == null) {
            return (double)0.0F;
         } else {
            class_243 v = p.method_18798();
            double horizontal = Math.sqrt(v.field_1352 * v.field_1352 + v.field_1350 * v.field_1350);
            double t = Math.min((double)1.0F, horizontal / 0.2825);
            return (double)this.speedStrength.getFloat() * t;
         }
      }
   }

   private static String tag(int v) {
      if (v <= 45) {
         return "Tunnel";
      } else if (v <= 65) {
         return "Focused";
      } else if (v <= 80) {
         return "Normal";
      } else if (v <= 100) {
         return "Wide";
      } else {
         return v <= 118 ? "Ultra" : "Fisheye";
      }
   }
}
