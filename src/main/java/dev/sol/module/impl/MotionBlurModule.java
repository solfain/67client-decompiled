package dev.sol.module.impl;

import dev.sol.SolClient;
import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.render.MotionBlurRenderer;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.SliderSetting;
import java.util.Objects;

public class MotionBlurModule extends Module {
   public final SliderSetting strength = (SliderSetting)this.addSetting((new SliderSetting("Strength", "Blur strength", (double)30.0F, (double)5.0F, (double)100.0F, (double)5.0F, "%")).withLabel((v) -> {
      int pct = (int)Math.round(v);
      String tier = pct <= 20 ? "Light" : (pct <= 45 ? "Medium" : (pct <= 70 ? "Strong" : (pct <= 90 ? "Heavy" : "Max")));
      return pct + "% (" + tier + ")";
   }));
   public final BooleanSetting accentTrails = (BooleanSetting)this.addSetting(new BooleanSetting("Accent Trails", "Color the trails with your theme color", true));
   public final SliderSetting tint = (SliderSetting)this.addSetting(new SliderSetting("Tint", "How strongly trails take the theme color", (double)30.0F, (double)0.0F, (double)100.0F, (double)5.0F, "%"));
   public final BooleanSetting fpsCompensated = (BooleanSetting)this.addSetting(new BooleanSetting("FPS Compensated", "Keep the blur the same at any FPS", true));

   public MotionBlurModule() {
      super("MotionBlur", "Cinematic motion blur", Category.RENDER);
      SliderSetting var10000 = this.tint;
      BooleanSetting var10001 = this.accentTrails;
      Objects.requireNonNull(var10001);
      var10000.visibleWhen(var10001::get);
   }

   protected void onDisable() {
      MotionBlurRenderer.reset();
   }

   public double retention() {
      return Math.clamp((double)this.strength.getFloat() / (double)100.0F, 0.05, 0.95);
   }

   public float tintAmount() {
      return (Boolean)this.accentTrails.get() ? (float)Math.clamp((double)this.tint.getFloat() / (double)100.0F, (double)0.0F, (double)1.0F) : 0.0F;
   }

   public int accentColor() {
      return SolClient.themes().current().accent();
   }
}
