package dev.sol.module.impl;

import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.ModeSetting;
import dev.sol.settings.SliderSetting;
import java.util.Random;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1743;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_3966;
import net.minecraft.class_239.class_240;

public class TriggerbotModule extends Module {
   public final SliderSetting minDelay = (SliderSetting)this.addSetting(new SliderSetting("Min Delay", "", (double)9.0F, (double)0.0F, (double)20.0F, (double)1.0F));
   public final SliderSetting maxDelay = (SliderSetting)this.addSetting(new SliderSetting("Max Delay", "", (double)11.0F, (double)0.0F, (double)20.0F, (double)1.0F));
   public final BooleanSetting onlyItem = (BooleanSetting)this.addSetting(new BooleanSetting("Only Item", "", false));
   public final ModeSetting itemFilter = (ModeSetting)this.addSetting(new ModeSetting("Item Filter", "", "Sword", new String[]{"Sword", "Axe", "Hand"}));
   public final BooleanSetting onlyCrit = (BooleanSetting)this.addSetting(new BooleanSetting("Only Crit", "", false));
   public final BooleanSetting checkShield = (BooleanSetting)this.addSetting(new BooleanSetting("Check Shield", "", false));
   private final Random random = new Random();
   private int tickCounter;
   private int currentDelay = 10;

   public TriggerbotModule() {
      super("Triggerbot", "Auto-attacks entities on crosshair", Category.COMBAT);
   }

   protected void onEnable() {
      this.tickCounter = 0;
      this.randomizeDelay();
   }

   protected void onDisable() {
      this.tickCounter = 0;
   }

   public void onTick() {
      class_310 client = class_310.method_1551();
      if (client.field_1724 != null && client.field_1761 != null && client.field_1755 == null) {
         if (client.field_1765 != null && client.field_1765.method_17783() == class_240.field_1331) {
            class_1297 target = ((class_3966)client.field_1765).method_17782();
            if (target instanceof class_1309 && target != client.field_1724) {
               if ((Boolean)this.onlyItem.get()) {
                  if (this.itemFilter.check("Sword") && !this.isSword(client.field_1724.method_6047().method_7909())) {
                     return;
                  }

                  if (this.itemFilter.check("Axe") && !(client.field_1724.method_6047().method_7909() instanceof class_1743)) {
                     return;
                  }

                  if (this.itemFilter.check("Hand") && !client.field_1724.method_6047().method_7960()) {
                     return;
                  }
               }

               if (!(Boolean)this.onlyCrit.get() || !client.field_1724.method_24828() && !(client.field_1724.field_6017 <= (double)0.0F)) {
                  if ((Boolean)this.checkShield.get() && target instanceof class_1657) {
                     class_1657 targetPlayer = (class_1657)target;
                     if (targetPlayer.method_6039()) {
                        return;
                     }
                  }

                  if (!(client.field_1724.method_7261(0.5F) < 1.0F)) {
                     ++this.tickCounter;
                     if (this.tickCounter >= this.currentDelay) {
                        client.field_1761.method_2918(client.field_1724, target);
                        client.field_1724.method_6104(class_1268.field_5808);
                        this.tickCounter = 0;
                        this.randomizeDelay();
                     }
                  }
               }
            }
         } else {
            this.tickCounter = 0;
         }
      }

   }

   private boolean isSword(class_1792 item) {
      return item == class_1802.field_8091 || item == class_1802.field_8528 || item == class_1802.field_8371 || item == class_1802.field_8845 || item == class_1802.field_8802 || item == class_1802.field_22022;
   }

   private void randomizeDelay() {
      int min = this.minDelay.getInt();
      int max = this.maxDelay.getInt();
      if (max < min) {
         max = min;
      }

      this.currentDelay = min + (max > min ? this.random.nextInt(max - min + 1) : 0);
   }
}
