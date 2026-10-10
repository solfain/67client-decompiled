package dev.sol.module.impl;

import dev.sol.mixin.MinecraftAccessor;
import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.ModeSetting;
import dev.sol.settings.SliderSetting;
import net.minecraft.class_310;

public class AutoClickerModule extends Module {
   public final BooleanSetting inScreens = (BooleanSetting)this.addSetting(new BooleanSetting("While In Screens", "Whether to click while a screen is open.", true));
   public final ModeSetting leftMode = (ModeSetting)this.addSetting(new ModeSetting("Left Click Mode", "The method of clicking for left clicks.", "Press", new String[]{"Disabled", "Hold", "Press"}));
   public final SliderSetting leftDelay = (SliderSetting)this.addSetting(new SliderSetting("Left Click Delay", "Delay between left clicks in ticks.", (double)2.0F, (double)0.0F, (double)60.0F, (double)1.0F, " ticks"));
   public final ModeSetting rightMode = (ModeSetting)this.addSetting(new ModeSetting("Right Click Mode", "The method of clicking for right clicks.", "Press", new String[]{"Disabled", "Hold", "Press"}));
   public final SliderSetting rightDelay = (SliderSetting)this.addSetting(new SliderSetting("Right Click Delay", "Delay between right clicks in ticks.", (double)2.0F, (double)0.0F, (double)60.0F, (double)1.0F, " ticks"));
   private int leftTimer;
   private int rightTimer;

   public AutoClickerModule() {
      super("AutoClicker", "Automatically clicks.", Category.COMBAT);
      this.leftDelay.visibleWhen(() -> this.leftMode.check("Press"));
      this.rightDelay.visibleWhen(() -> this.rightMode.check("Press"));
   }

   protected void onEnable() {
      this.leftTimer = 0;
      this.rightTimer = 0;
      this.release();
   }

   protected void onDisable() {
      this.release();
   }

   private void release() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1690 != null) {
         mc.field_1690.field_1886.method_23481(false);
         mc.field_1690.field_1904.method_23481(false);
      }

   }

   public void onTick() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null && ((Boolean)this.inScreens.get() || mc.field_1755 == null)) {
         switch (var2) {
            case "Hold":
               mc.field_1690.field_1886.method_23481(true);
               break;
            case "Press":
               ++this.leftTimer;
               if (this.leftTimer > this.leftDelay.getInt()) {
                  this.leftClick(mc);
                  this.leftTimer = 0;
               }
         }

         while(true) {
            var2 = (String)this.rightMode.get();
            switch (var2.hashCode()) {
               case 2255071:
                  if (!var2.equals("Hold")) {
                     return;
                  }

                  mc.field_1690.field_1886.method_23481(true);
                  break;
               case 77378595:
                  if (!var2.equals("Press")) {
                     return;
                  }

                  ++this.leftTimer;
                  if (this.leftTimer > this.leftDelay.getInt()) {
                     this.leftClick(mc);
                     this.leftTimer = 0;
                  }
                  break;
               default:
                  return;
            }
         }
      }
   }

   private void leftClick(class_310 mc) {
      if (mc.field_1771 == 10000) {
         mc.field_1771 = 0;
      }

      mc.field_1690.field_1886.method_23481(true);
      ((MinecraftAccessor)mc).solclient$startAttack();
      mc.field_1690.field_1886.method_23481(false);
   }

   private void rightClick(class_310 mc) {
      ((MinecraftAccessor)mc).solclient$startUseItem();
   }
}
