package dev.sol.module.impl;

import dev.sol.SolClient;
import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.KeybindSetting;
import dev.sol.settings.ModeSetting;
import net.minecraft.class_2338;
import net.minecraft.class_239;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;

public class CoordSnapperModule extends Module {
   public final ModeSetting format = (ModeSetting)this.addSetting(new ModeSetting("Format", "Clipboard format.", "X Y Z", new String[]{"X Y Z", "JSON", "Command"}));
   public final ModeSetting target = (ModeSetting)this.addSetting(new ModeSetting("Target", "Which coordinates to copy.", "Looked-at Block", new String[]{"Looked-at Block", "Player"}));
   public final KeybindSetting copyKey = (KeybindSetting)this.addSetting(new KeybindSetting("Copy Key", "Press to copy the coordinates.", -1));
   public final BooleanSetting notify = (BooleanSetting)this.addSetting(new BooleanSetting("Notify", "Show a confirmation when copied.", true));
   private String lastCopied = "";

   public CoordSnapperModule() {
      super("CoordSnapper", "Copies looked-at coordinates with one key.", Category.PLAYER);
   }

   public String lastCopied() {
      return this.lastCopied;
   }

   public boolean onKeyPress(int keyCode) {
      if (!this.copyKey.matches(keyCode)) {
         return false;
      } else {
         this.snap();
         return true;
      }
   }

   private void snap() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null) {
         class_2338 pos = this.resolvePos(mc);
         if (pos == null) {
            if ((Boolean)this.notify.get()) {
               SolClient.notifications().pushInfo("CoordSnapper · no block in view");
            }
         } else {
            String var10000;
            label25: {
               switch (var4) {
                  case "Command":
                     int var5 = pos.method_10263();
                     var10000 = "/tp " + var5 + " " + pos.method_10264() + " " + pos.method_10260();
                     break label25;
                  case "JSON":
                     int var100001 = pos.method_10263();
                     var10000 = "{\"x\": " + var100001 + ", \"y\": " + pos.method_10264() + ", \"z\": " + pos.method_10260() + "}";
                     break label25;
               }

               int var6 = pos.method_10263();
               var10000 = var6 + " " + pos.method_10264() + " " + pos.method_10260();
            }

            mc.field_1774.method_1455(var10000);
            this.lastCopied = var10000;
            if ((Boolean)this.notify.get()) {
               SolClient.notifications().pushInfo("Copied · null");
            }
         }
      }

   }

   private class_2338 resolvePos(class_310 mc) {
      if (this.target.check("Player")) {
         return mc.field_1724.method_24515();
      } else {
         class_239 hit = mc.field_1765;
         class_2338 var10000;
         if (hit instanceof class_3965) {
            class_3965 block = (class_3965)hit;
            if (hit.method_17783() == class_240.field_1332) {
               var10000 = block.method_17777();
               return var10000;
            }
         }

         var10000 = null;
         return var10000;
      }
   }
}
