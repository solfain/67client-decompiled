package dev.sol.module.impl;

import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.SliderSetting;

public class HitBoxModule extends Module {
   public final SliderSetting expand = (SliderSetting)this.addSetting(new SliderSetting("Expand", "Does nothing on most servers. Kept so old configs still load", (double)0.5F, (double)0.0F, (double)2.0F, 0.05));
   public final BooleanSetting enableRender = (BooleanSetting)this.addSetting(new BooleanSetting("Enable Render", "", true));

   public HitBoxModule() {
      super("HitBox", "Does not change server hitboxes. Kept for old configs", Category.COMBAT);
   }

   public float getHitboxExpansion() {
      return this.isEnabled() ? this.expand.getFloat() : 0.0F;
   }

   public boolean shouldRender() {
      return this.isEnabled() && (Boolean)this.enableRender.get();
   }
}
