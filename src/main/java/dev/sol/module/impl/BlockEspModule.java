package dev.sol.module.impl;

import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.render.BlockEspRenderer;
import dev.sol.settings.BlockListSetting;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.ColorSetting;
import dev.sol.settings.ModeSetting;
import dev.sol.settings.SliderSetting;
import java.util.Objects;

public class BlockEspModule extends Module {
   public final BlockListSetting targets = (BlockListSetting)this.addSetting(new BlockListSetting("Target Blocks", "Pick which blocks to highlight"));
   public final ModeSetting shapeMode = (ModeSetting)this.addSetting(new ModeSetting("Shape Mode", "How highlights are drawn", "Both", new String[]{"Both", "Lines", "Sides"}));
   public final ColorSetting lineColor = (ColorSetting)this.addSetting(new ColorSetting("Default Outline Color", "Color used for newly added blocks", -16711736));
   public final ColorSetting sideColor = (ColorSetting)this.addSetting(new ColorSetting("Fill Overlay", "Default fill tint", 419495880));
   public final BooleanSetting tracers = (BooleanSetting)this.addSetting(new BooleanSetting("Tracers", "Draw lines from the crosshair to each block", false));
   public final BooleanSetting tracer = (BooleanSetting)this.addSetting(new BooleanSetting("Tracer", "Secondary tracer enable", true));
   public final ColorSetting tracerColor = (ColorSetting)this.addSetting(new ColorSetting("Default Tracer Tint", "Tracer line color / alpha", 2097217480));
   public final SliderSetting highlightAlpha = (SliderSetting)this.addSetting(new SliderSetting("Highlight Alpha", "Box opacity", (double)255.0F, (double)0.0F, (double)255.0F, (double)5.0F));
   public final SliderSetting rangeExtraChunks = (SliderSetting)this.addSetting(new SliderSetting("Range Extra Chunks", "Extra scan radius beyond render distance", (double)1.0F, (double)0.0F, (double)4.0F, (double)1.0F));

   public BlockEspModule() {
      super("BlockESP", "Highlights chosen blocks through walls", Category.RENDER);
      this.targets.seedDefaults();
      BooleanSetting var10000 = this.tracer;
      BooleanSetting var10001 = this.tracers;
      Objects.requireNonNull(var10001);
      var10000.visibleWhen(var10001::get);
      ColorSetting var1 = this.tracerColor;
      var10001 = this.tracers;
      Objects.requireNonNull(var10001);
      var1.visibleWhen(var10001::get);
   }

   public void onTick() {
      BlockEspRenderer.scan(this);
   }

   protected void onDisable() {
      BlockEspRenderer.clear();
   }
}
