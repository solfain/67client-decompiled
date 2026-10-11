package dev.sol.module.impl;

import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.SliderSetting;
import dev.sol.util.InventoryHelper;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_490;
import net.minecraft.class_746;

public class AutoTotemModule extends Module {
   public final SliderSetting delay = (SliderSetting)this.addSetting(new SliderSetting("Delay", "", (double)4.0F, (double)1.0F, (double)40.0F, (double)1.0F));
   public final BooleanSetting forceTotem = (BooleanSetting)this.addSetting(new BooleanSetting("Force Totem", "", false));
   public final BooleanSetting hotbarTotem = (BooleanSetting)this.addSetting(new BooleanSetting("Hotbar Totem", "", false));
   public final SliderSetting hotbarSlot = (SliderSetting)this.addSetting(new SliderSetting("Hotbar Slot", "", (double)1.0F, (double)1.0F, (double)9.0F, (double)1.0F));
   public final SliderSetting hotbarDelay = (SliderSetting)this.addSetting(new SliderSetting("Hotbar Delay", "", (double)4.0F, (double)1.0F, (double)40.0F, (double)1.0F));
   private int tickCounter;
   private int pendingHotbarSlot = -1;

   public AutoTotemModule() {
      super("Auto Totem", "Puts a totem in your offhand or hotbar, pulling from your inventory when needed", Category.PLAYER);
   }

   protected void onEnable() {
      this.tickCounter = 0;
      this.pendingHotbarSlot = -1;
   }

   protected void onDisable() {
      this.tickCounter = 0;
      this.pendingHotbarSlot = -1;
   }

   public void onTick() {
      class_310 client = class_310.method_1551();
      if (client.field_1724 != null && client.field_1761 != null && canRun(client)) {
         ++this.tickCounter;
         int wait = (Boolean)this.hotbarTotem.get() && this.needsHotbarWork(client) && !this.needsOffhandWork(client) ? this.hotbarDelay.getInt() : this.delay.getInt();
         if (this.tickCounter >= wait) {
            this.tickCounter = 0;
            class_746 player = client.field_1724;
            int configuredHotbar = this.hotbarSlot.getInt() - 1;
            if ((Boolean)this.hotbarTotem.get() && this.needsHotbarWork(client)) {
               if (player.method_31548().method_5438(configuredHotbar).method_31574(class_1802.field_8288)) {
                  InventoryHelper.selectHotbarSlot(configuredHotbar);
               } else {
                  int source = findTotemInMainInventory(client);
                  if (source != -1 && canContainerClick(client)) {
                     InventoryHelper.swapInventoryToHotbar(source, configuredHotbar);
                  }
               }
            } else if (!this.needsOffhandWork(client)) {
               this.pendingHotbarSlot = -1;
            } else if (!player.method_6079().method_31574(class_1802.field_8288) || (Boolean)this.forceTotem.get()) {
               int hotbarTotemSlot = findTotemInHotbar(client);
               if (hotbarTotemSlot != -1) {
                  this.pendingHotbarSlot = hotbarTotemSlot;
                  InventoryHelper.selectHotbarSlot(hotbarTotemSlot);
                  if (player.method_6047().method_31574(class_1802.field_8288)) {
                     InventoryHelper.swapOffhand();
                  }
               } else if (this.pendingHotbarSlot >= 0) {
                  InventoryHelper.selectHotbarSlot(this.pendingHotbarSlot);
                  if (player.method_6047().method_31574(class_1802.field_8288)) {
                     InventoryHelper.swapOffhand();
                     this.pendingHotbarSlot = -1;
                  }
               } else {
                  int mainInvTotem = findTotemInMainInventory(client);
                  if (mainInvTotem != -1 && canContainerClick(client)) {
                     int targetHotbar = configuredHotbar;
                     if (!(Boolean)this.hotbarTotem.get()) {
                        targetHotbar = player.method_31548().method_67532();
                     }

                     InventoryHelper.swapInventoryToHotbar(mainInvTotem, targetHotbar);
                     this.pendingHotbarSlot = targetHotbar;
                  } else if (player.method_6047().method_31574(class_1802.field_8288)) {
                     InventoryHelper.swapOffhand();
                  }
               }
            }
         }
      }

   }

   private static boolean canRun(class_310 client) {
      return client.field_1755 == null || client.field_1755 instanceof class_490;
   }

   private static boolean canContainerClick(class_310 client) {
      return client.field_1755 != null || !isMoving(client);
   }

   private static boolean isMoving(class_310 client) {
      if (client.field_1724 == null) {
         return false;
      } else if (!client.field_1690.field_1894.method_1434() && !client.field_1690.field_1881.method_1434() && !client.field_1690.field_1913.method_1434() && !client.field_1690.field_1849.method_1434() && !client.field_1690.field_1903.method_1434() && !client.field_1724.method_5624() && !client.field_1724.method_5715()) {
         double vx = client.field_1724.method_18798().field_1352;
         double vz = client.field_1724.method_18798().field_1350;
         return vx * vx + vz * vz > 0.0025;
      } else {
         return true;
      }
   }

   private boolean needsOffhandWork(class_310 client) {
      boolean hasTotemOffhand = client.field_1724.method_6079().method_31574(class_1802.field_8288);
      return (Boolean)this.forceTotem.get() || !hasTotemOffhand;
   }

   private boolean needsHotbarWork(class_310 client) {
      if (!(Boolean)this.hotbarTotem.get()) {
         return false;
      } else {
         int slot = this.hotbarSlot.getInt() - 1;
         return !client.field_1724.method_31548().method_5438(slot).method_31574(class_1802.field_8288);
      }
   }

   private static int findTotemInMainInventory(class_310 client) {
      for(int i = 9; i < 36; ++i) {
         if (client.field_1724.method_31548().method_5438(i).method_31574(class_1802.field_8288)) {
            return i;
         }
      }

      return -1;
   }

   private static int findTotemInHotbar(class_310 client) {
      for(int i = 0; i < 9; ++i) {
         if (client.field_1724.method_31548().method_5438(i).method_31574(class_1802.field_8288)) {
            return i;
         }
      }

      return -1;
   }
}
