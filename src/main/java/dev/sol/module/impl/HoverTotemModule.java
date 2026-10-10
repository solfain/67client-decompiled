package dev.sol.module.impl;

import dev.sol.mixin.AbstractContainerScreenAccessor;
import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.SliderSetting;
import dev.sol.util.InventoryHelper;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_465;

public class HoverTotemModule extends Module {
   public final SliderSetting tickDelay = (SliderSetting)this.addSetting(new SliderSetting("Tick Delay", "", (double)0.0F, (double)0.0F, (double)20.0F, (double)1.0F));
   public final BooleanSetting hotbarTotem = (BooleanSetting)this.addSetting(new BooleanSetting("Hotbar Totem", "", true));
   public final SliderSetting hotbarSlot = (SliderSetting)this.addSetting(new SliderSetting("Hotbar Slot", "", (double)1.0F, (double)1.0F, (double)9.0F, (double)1.0F));
   public final BooleanSetting autoSwitchToTotem = (BooleanSetting)this.addSetting(new BooleanSetting("Auto Switch To Totem", "", false));
   private int tickCounter;

   public HoverTotemModule() {
      super("Hover Totem", "Equip totem when hovering over one in inventory", Category.PLAYER);
   }

   protected void onEnable() {
      this.tickCounter = 0;
   }

   protected void onDisable() {
      this.tickCounter = 0;
   }

   public void onTick() {
      class_310 client = class_310.method_1551();
      if (client.field_1724 != null && client.field_1761 != null) {
         if ((Boolean)this.autoSwitchToTotem.get() && (Boolean)this.hotbarTotem.get()) {
            int slot = this.hotbarSlot.getInt() - 1;
            if (client.field_1724.method_31548().method_5438(slot).method_31574(class_1802.field_8288)) {
               InventoryHelper.swap(slot);
            }
         }

         class_437 var3 = client.field_1755;
         if (var3 instanceof class_465) {
            class_465<?> handledScreen = (class_465)var3;
            ++this.tickCounter;
            if (this.tickCounter >= this.tickDelay.getInt()) {
               this.tickCounter = 0;
               class_1735 focusedSlot = this.getFocusedSlot(handledScreen);
               if (focusedSlot != null && focusedSlot.method_7681() && focusedSlot.method_7677().method_31574(class_1802.field_8288)) {
                  int slotId = focusedSlot.field_7874;
                  client.field_1761.method_2906(handledScreen.method_17577().field_7763, slotId, 0, class_1713.field_7790, client.field_1724);
                  client.field_1761.method_2906(handledScreen.method_17577().field_7763, 45, 0, class_1713.field_7790, client.field_1724);
                  client.field_1761.method_2906(handledScreen.method_17577().field_7763, slotId, 0, class_1713.field_7790, client.field_1724);
               }
            }
         }
      }

   }

   private class_1735 getFocusedSlot(class_465<?> screen) {
      return ((AbstractContainerScreenAccessor)screen).getHoveredSlot();
   }
}
