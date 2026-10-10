package dev.sol.module.impl;

import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.SliderSetting;

public class HitBoxModule extends Module {
   public final SliderSetting expand = (SliderSetting)this.addSetting(new SliderSetting("Expand", "Disabled vs prediction anticheats — kept for config compatibility", (double)0.5F, (double)0.0F, (double)2.0F, 0.05));
   public final BooleanSetting enableRender = (BooleanSetting)this.addSetting(new BooleanSetting("Enable Render", "", true));

   public HitBoxModule() {
      super("HitBox", "Hitbox expander (neutered: undetectable = does not widen server hitboxes)", Category.COMBAT);
   }

   public float getHitboxExpansion() {
      return this.isEnabled() ? this.expand.getFloat() : 0.0F;
   }

   public boolean shouldRender() {
      return this.isEnabled() && (Boolean)this.enableRender.get();
   }
}
