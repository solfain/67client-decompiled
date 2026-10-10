package dev.sol.module.impl;

import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.KeybindSetting;
import dev.sol.settings.SliderSetting;
import dev.sol.util.BlockHelper;
import dev.sol.util.InventoryHelper;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_239;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;
import org.lwjgl.glfw.GLFW;

public class DoubleAnchorModule extends Module {
   public final KeybindSetting activateKey = (KeybindSetting)this.addSetting(new KeybindSetting("Activate Key", "Hold to run the combo", 72));
   public final SliderSetting timing = (SliderSetting)this.addSetting(new SliderSetting("Timing", "Delay between the two blows", (double)120.0F, (double)40.0F, (double)400.0F, (double)10.0F, "ms"));
   public final SliderSetting detonateSlot = (SliderSetting)this.addSetting(new SliderSetting("Detonate Slot", "Hotbar slot to hold while detonating (anything but glowstone)", (double)1.0F, (double)1.0F, (double)9.0F, (double)1.0F));
   public final BooleanSetting rePlace = (BooleanSetting)this.addSetting(new BooleanSetting("Re-Place", "Place a fresh anchor for the second blow if the first is consumed", true));
   public final BooleanSetting switchBack = (BooleanSetting)this.addSetting(new BooleanSetting("Switch Back", "Return to the previous hotbar slot when done", true));
   private int step;
   private int tickCounter;
   private class_2338 targetPos;
   private int previousSlot = -1;

   public DoubleAnchorModule() {
      super("Double Anchor", "Two rapid anchor charge→detonate cycles", Category.COMBAT);
   }

   protected void onEnable() {
      this.resetState();
   }

   protected void onDisable() {
      this.resetState();
   }

   public void onTick() {
      class_310 client = class_310.method_1551();
      if (client.field_1724 != null && client.field_1761 != null && client.field_1687 != null && client.field_1755 == null) {
         if (this.step > 0) {
            this.processStep(client);
         } else if (this.keyDown(client)) {
            class_239 var3 = client.field_1765;
            if (var3 instanceof class_3965) {
               class_3965 hit = (class_3965)var3;
               if (hit.method_17783() == class_240.field_1332) {
                  class_2338 pos = hit.method_17777();
                  if (BlockHelper.isBlockAt(pos, class_2246.field_23152)) {
                     this.targetPos = pos.method_10062();
                     this.previousSlot = client.field_1724.method_31548().method_67532();
                     this.step = 1;
                     this.tickCounter = 0;
                  }
               }
            }
         }
      } else if (this.step != 0) {
         this.resetState();
      }

   }

   private void processStep(class_310 client) {
      ++this.tickCounter;
      class_239 var4 = client.field_1765;
      class_3965 var10000;
      if (var4 instanceof class_3965 bhr) {
         var10000 = bhr;
      } else {
         var10000 = null;
      }

      class_3965 live = var10000;
      switch (this.step) {
         case 1:
            if (!this.lookingAtTarget(live)) {
               this.resetState();
               return;
            }

            if (BlockHelper.isAnchorUncharged(this.targetPos)) {
               InventoryHelper.swapToItem(class_1802.field_8801);
               BlockHelper.interactBlock(live, true);
            }

            this.advance(2);
            break;
         case 2:
            if (!this.lookingAtTarget(live)) {
               this.resetState();
               return;
            }

            InventoryHelper.swap(this.detonateSlot.getInt() - 1);
            BlockHelper.interactBlock(live, true);
            this.advance(3);
            break;
         case 3:
            if (this.tickCounter >= this.gapTicks()) {
               this.advance(4);
            }
            break;
         case 4:
            if (BlockHelper.isBlockAt(this.targetPos, class_2246.field_23152)) {
               if (this.lookingAtTarget(live) && BlockHelper.isAnchorUncharged(this.targetPos)) {
                  InventoryHelper.swapToItem(class_1802.field_8801);
                  BlockHelper.interactBlock(live, true);
               }

               this.advance(5);
            } else if ((Boolean)this.rePlace.get() && live != null && InventoryHelper.getHotbarSlot(class_1802.field_23141) >= 0) {
               InventoryHelper.swapToItem(class_1802.field_23141);
               BlockHelper.interactBlock(live, true);
               this.targetPos = live.method_17777().method_10093(live.method_17780()).method_10062();
               this.advance(6);
            } else {
               this.finish(client);
            }
            break;
         case 5:
            if (this.lookingAtTarget(live) && BlockHelper.isBlockAt(this.targetPos, class_2246.field_23152)) {
               InventoryHelper.swap(this.detonateSlot.getInt() - 1);
               BlockHelper.interactBlock(live, true);
            }

            this.finish(client);
            break;
         case 6:
            if (BlockHelper.isBlockAt(this.targetPos, class_2246.field_23152)) {
               InventoryHelper.swapToItem(class_1802.field_8801);
               if (live != null) {
                  BlockHelper.interactBlock(live, true);
               }

               this.advance(5);
            } else {
               this.finish(client);
            }
            break;
         default:
            this.finish(client);
      }

   }

   private boolean lookingAtTarget(class_3965 live) {
      return live != null && live.method_17783() == class_240.field_1332 && live.method_17777().equals(this.targetPos);
   }

   private void advance(int next) {
      this.step = next;
      this.tickCounter = 0;
   }

   private int gapTicks() {
      return Math.max(1, this.timing.getInt() / 50);
   }

   private void finish(class_310 client) {
      if ((Boolean)this.switchBack.get() && this.previousSlot >= 0) {
         InventoryHelper.swap(this.previousSlot);
      }

      this.resetState();
   }

   private boolean keyDown(class_310 client) {
      int key = (Integer)this.activateKey.get();
      if (key == -1) {
         return false;
      } else {
         long handle = client.method_22683().method_4490();
         return key <= 7 ? GLFW.glfwGetMouseButton(handle, key) == 1 : GLFW.glfwGetKey(handle, key) == 1;
      }
   }

   private void resetState() {
      this.step = 0;
      this.tickCounter = 0;
      this.targetPos = null;
      this.previousSlot = -1;
   }
}
