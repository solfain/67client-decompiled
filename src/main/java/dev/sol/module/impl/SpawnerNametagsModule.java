package dev.sol.module.impl;

import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.render.BlockScanCache;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.SliderSetting;
import net.minecraft.class_2246;

public class SpawnerNametagsModule extends Module {
   public final BooleanSetting nametag = (BooleanSetting)this.addSetting(new BooleanSetting("Nametag", "Floating type + distance label", true));
   public final BooleanSetting box = (BooleanSetting)this.addSetting(new BooleanSetting("Box", "Through-wall box on the spawner", true));
   public final BooleanSetting rangeRing = (BooleanSetting)this.addSetting(new BooleanSetting("Range Ring", "Draw the 16-block activation ring", true));
   public final BooleanSetting distance = (BooleanSetting)this.addSetting(new BooleanSetting("Distance", "Append the distance in blocks", true));
   public final SliderSetting opacity = (SliderSetting)this.addSetting(new SliderSetting("Opacity", "Nametag transparency", (double)100.0F, (double)10.0F, (double)100.0F, (double)5.0F, "%"));
   public final BooleanSetting tracers = (BooleanSetting)this.addSetting(new BooleanSetting("Tracers", "Draw lines from the crosshair to each spawner", false));
   public final BlockScanCache scan = new BlockScanCache((state) -> state.method_27852(class_2246.field_10260) || state.method_27852(class_2246.field_47336), 12, 8, 400, (double)96.0F);

   public SpawnerNametagsModule() {
      super("SpawnerNametags", "Shows spawner type and range", Category.WORLD);
   }

   public void onTick() {
      this.scan.scan();
   }

   protected void onDisable() {
      this.scan.clear();
   }
}
