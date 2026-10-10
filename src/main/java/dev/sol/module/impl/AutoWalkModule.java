package dev.sol.module.impl;

import dev.sol.mixin.KeyMappingAccessor;
import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.ModeSetting;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_3675;
import net.minecraft.class_3675.class_307;

public class AutoWalkModule extends Module {
   public final ModeSetting mode = (ModeSetting)this.addSetting(new ModeSetting("Mode", "Walking mode.", "Simple", new String[]{"Simple", "Smart"}));
   public final ModeSetting direction = (ModeSetting)this.addSetting(new ModeSetting("Direction", "The direction to walk in Simple mode.", "Forwards", new String[]{"Forwards", "Backwards", "Left", "Right"}));
   public final BooleanSetting disableOnInput = (BooleanSetting)this.addSetting(new BooleanSetting("Disable On Input", "Disable the module on manual movement input.", false));
   public final BooleanSetting disableOnY = (BooleanSetting)this.addSetting(new BooleanSetting("Disable On Y Change", "Disable the module if you move vertically.", false));
   public final BooleanSetting waitForChunks = (BooleanSetting)this.addSetting(new BooleanSetting("No Unloaded Chunks", "Do not walk into unloaded chunks.", true));

   public AutoWalkModule() {
      super("AutoWalk", "Automatically walks forward.", Category.MOVEMENT);
      this.direction.visibleWhen(() -> this.mode.check("Simple"));
      this.disableOnY.visibleWhen(() -> this.mode.check("Simple"));
      this.waitForChunks.visibleWhen(() -> this.mode.check("Simple"));
   }

   protected void onDisable() {
      this.release(class_310.method_1551());
   }

   public void onTick() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null && mc.field_1687 != null) {
         this.release(mc);
         if (this.mode.check("Smart")) {
            mc.field_1690.field_1894.method_23481(true);
            mc.field_1690.field_1867.method_23481(true);
            if (mc.field_1724.field_5976 && mc.field_1724.method_24828()) {
               mc.field_1690.field_1903.method_23481(true);
            }
         } else if ((Boolean)this.disableOnY.get() && mc.field_1724.field_6036 != mc.field_1724.method_23318()) {
            this.toggle();
         } else if (!(Boolean)this.waitForChunks.get() || this.chunkAheadLoaded(mc)) {
            switch (var2) {
               case "Backwards":
                  mc.field_1690.field_1881.method_23481(true);
                  break;
               case "Left":
                  mc.field_1690.field_1913.method_23481(true);
                  break;
               case "Right":
                  mc.field_1690.field_1849.method_23481(true);
                  break;
               case "Forwards":
                  mc.field_1690.field_1894.method_23481(true);
            }
         }
      } else {
         this.release(mc);
      }

   }

   public boolean onKeyPress(int keyCode) {
      class_310 mc = class_310.method_1551();
      if ((Boolean)this.disableOnInput.get() && mc.field_1755 == null && this.isMovementKey(mc, keyCode)) {
         this.toggle();
      }

      return false;
   }

   private boolean chunkAheadLoaded(class_310 mc) {
      double dx;
      double dz;
      label20: {
         double yawRad = Math.toRadians((double)mc.field_1724.method_36454());
         double fx = -Math.sin(yawRad);
         double fz = Math.cos(yawRad);
         byte bz = -1;
         switch (bx) {
            case "Backwards":
               dx = -fx;
               dz = -fz;
               break label20;
            case "Left":
               dx = -fz;
               dz = fx;
               break label20;
            case "Right":
               dx = fz;
               dz = -fx;
               break label20;
         }

         dx = fx;
         dz = fz;
      }

      int bx = (int)Math.floor(mc.field_1724.method_23317() + dx * (double)2.0F);
      int bz = (int)Math.floor(mc.field_1724.method_23321() + dz * (double)2.0F);
      return mc.field_1687.method_2935().method_12123(bx >> 4, bz >> 4);
   }

   private void release(class_310 mc) {
      if (mc.field_1690 != null) {
         mc.field_1690.field_1894.method_23481(false);
         mc.field_1690.field_1881.method_23481(false);
         mc.field_1690.field_1913.method_23481(false);
         mc.field_1690.field_1849.method_23481(false);
         mc.field_1690.field_1903.method_23481(false);
         mc.field_1690.field_1867.method_23481(false);
      }

   }

   private boolean isMovementKey(class_310 mc, int keyCode) {
      class_315 o = mc.field_1690;
      return matches(o.field_1894, keyCode) || matches(o.field_1881, keyCode) || matches(o.field_1913, keyCode) || matches(o.field_1849, keyCode) || matches(o.field_1903, keyCode) || matches(o.field_1832, keyCode);
   }

   private static boolean matches(class_304 km, int keyCode) {
      class_3675.class_306 key = ((KeyMappingAccessor)km).solclient$getKey();
      return key.method_1442() == class_307.field_1668 && key.method_1444() == keyCode;
   }
}
