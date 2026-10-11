package dev.sol.module.impl;

import dev.sol.license.ProtectedContent;
import dev.sol.module.AimAssistCompute;
import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.ModeSetting;
import dev.sol.settings.SliderSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AimAssistModule extends Module {
   private static final Logger LOG = LoggerFactory.getLogger("Client/AimAssist");
   public final SliderSetting range = (SliderSetting)this.addSetting(new SliderSetting("Range", "Assist any target within this distance", (double)4.0F, (double)1.0F, (double)8.0F, (double)0.5F, "m"));
   public final SliderSetting speed = (SliderSetting)this.addSetting(new SliderSetting("Speed", "How fast the aim moves (higher is faster)", (double)4.0F, (double)1.0F, (double)10.0F, (double)1.0F));
   public final SliderSetting smoothness = (SliderSetting)this.addSetting(new SliderSetting("Smoothness", "How smooth the aim moves (higher is smoother)", (double)6.0F, (double)1.0F, (double)10.0F, (double)1.0F));
   public final ModeSetting targetPart = (ModeSetting)this.addSetting(new ModeSetting("Target", "Which part of the target to aim at", "Body", new String[]{"Head", "Body", "Feet", "Nearest"}));
   public final SliderSetting fov = (SliderSetting)this.addSetting(new SliderSetting("FOV", "Only assist within this facing cone (180 = all around)", (double)180.0F, (double)10.0F, (double)180.0F, (double)5.0F, "°"));
   public final BooleanSetting vertical = (BooleanSetting)this.addSetting(new BooleanSetting("Vertical", "Also aim up and down, not just left and right", true));
   public final BooleanSetting players = (BooleanSetting)this.addSetting(new BooleanSetting("Players", "Target other players", true));
   public final BooleanSetting hostiles = (BooleanSetting)this.addSetting(new BooleanSetting("Hostiles", "Target hostile mobs", false));
   public final BooleanSetting passive = (BooleanSetting)this.addSetting(new BooleanSetting("Passive", "Target passive mobs", false));
   public final BooleanSetting invisibles = (BooleanSetting)this.addSetting(new BooleanSetting("Invisibles", "Also target invisible entities", false));
   public final BooleanSetting wallCheck = (BooleanSetting)this.addSetting(new BooleanSetting("Wall Check", "Only assist targets you can actually see", false));
   public final BooleanSetting alwaysActive = (BooleanSetting)this.addSetting(new BooleanSetting("Always Active", "Assist all the time. When off, only while holding left click", true));
   public final BooleanSetting sticky = (BooleanSetting)this.addSetting(new BooleanSetting("Sticky Target", "Keep one target until it leaves range", true));
   private final AimAssistCompute logic;

   public AimAssistModule() {
      super("AimAssist", "Smoothly turns your aim toward nearby targets", Category.COMBAT);
      this.logic = (AimAssistCompute)ProtectedContent.load(AimAssistCompute.class, "dev.sol.secured.AimAssistLogic", LOG);
   }

   public void onTick() {
   }

   protected void onDisable() {
      if (this.logic != null) {
         this.logic.reset();
      }

   }

   public double[] computePixels(double dt, double userDX, double userDY) {
      return this.logic == null ? null : this.logic.computePixels(this, dt, userDX, userDY);
   }
}
