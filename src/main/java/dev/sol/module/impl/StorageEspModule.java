package dev.sol.module.impl;

import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.render.StorageEspRenderer;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.IconListSetting;
import dev.sol.settings.ModeSetting;
import dev.sol.settings.SliderSetting;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2281;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2586;
import net.minecraft.class_2595;
import net.minecraft.class_2680;
import net.minecraft.class_2745;
import net.minecraft.class_310;

public class StorageEspModule extends Module {
   public final ModeSetting mode = (ModeSetting)this.addSetting(new ModeSetting("Mode", "Box style — hollow outline or translucent fill", "Outline", new String[]{"Outline", "Full"}));
   public final SliderSetting range = (SliderSetting)this.addSetting(new SliderSetting("Range", "Max distance a container is highlighted", (double)128.0F, (double)16.0F, (double)256.0F, (double)8.0F));
   public final SliderSetting highlightAlpha = (SliderSetting)this.addSetting(new SliderSetting("Highlight Alpha", "Box opacity (0-255)", (double)200.0F, (double)0.0F, (double)255.0F, (double)1.0F));
   public final BooleanSetting tracers = (BooleanSetting)this.addSetting(new BooleanSetting("Tracers", "Draw lines from the crosshair to each container", false));
   public final IconListSetting containers = (IconListSetting)this.addSetting(new IconListSetting("Containers", "Which container types to highlight"));
   public BooleanSetting hideOpened2;
   private final Set<class_2338> interactedBlocks = new HashSet();

   public StorageEspModule() {
      super("StorageESP", "Highlights chests, barrels, shulkers", Category.RENDER);

      for(StorageType type : StorageEspModule.StorageType.values()) {
         this.containers.add(type.key, type.label, type.icon, type.defaultEnabled, type.defaultColor);
      }

      this.hideOpened2 = (BooleanSetting)this.addSetting(new BooleanSetting("Hide Opened", "Don't highlight containers you've already opened", false));
   }

   public boolean isTypeEnabled(StorageType type) {
      return this.containers.isEnabled(type.key);
   }

   public int colorFor(StorageType type) {
      return this.containers.color(type.key);
   }

   public boolean hideOpened() {
      return (Boolean)this.hideOpened2.get();
   }

   public boolean isInteracted(int x, int y, int z) {
      return !this.interactedBlocks.isEmpty() && this.interactedBlocks.contains(new class_2338(x, y, z));
   }

   public void trackInteraction(class_2338 pos) {
      if (pos != null) {
         class_310 mc = class_310.method_1551();
         if (mc.field_1687 != null) {
            this.interactedBlocks.add(pos.method_10062());
            class_2586 be = mc.field_1687.method_8321(pos);
            if (be instanceof class_2595) {
               class_2680 state = mc.field_1687.method_8320(pos);
               if (state.method_26204() instanceof class_2281) {
                  class_2745 ct = (class_2745)state.method_11654(class_2281.field_10770);
                  if (ct != class_2745.field_12569) {
                     class_2350 facing = (class_2350)state.method_11654(class_2281.field_10768);
                     class_2338 other = pos.method_10093(ct == class_2745.field_12574 ? facing.method_10170() : facing.method_10160());
                     this.interactedBlocks.add(other);
                  }
               }
            }
         }
      }

   }

   protected void onEnable() {
      this.interactedBlocks.clear();
   }

   public void onTick() {
      StorageEspRenderer.scan(this);
   }

   protected void onDisable() {
      StorageEspRenderer.clear();
   }

   public static enum StorageType {
      CHEST("chest", "Chest", class_1802.field_8106, -22016, true),
      TRAPPED("trapped", "Trapped Chest", class_1802.field_8247, -65536, true),
      ENDER("ender", "Ender Chest", class_1802.field_8466, -8912641, true),
      SHULKER("shulker", "Shulker Box", class_1802.field_8545, -47873, true),
      BARREL("barrel", "Barrel", class_1802.field_16307, -7842560, true),
      SPAWNER("spawner", "Spawner", class_1802.field_8849, -16711936, true),
      HOPPER("hopper", "Hopper", class_1802.field_8239, -7829368, false),
      FURNACE("furnace", "Furnace", class_1802.field_8732, -7566196, false);

      final String key;
      final String label;
      final class_1792 icon;
      final int defaultColor;
      final boolean defaultEnabled;

      private StorageType(String key, String label, class_1792 icon, int defaultColor, boolean defaultEnabled) {
         this.key = key;
         this.label = label;
         this.icon = icon;
         this.defaultColor = defaultColor;
         this.defaultEnabled = defaultEnabled;
      }


   }
}
