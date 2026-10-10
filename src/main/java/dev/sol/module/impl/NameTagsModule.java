package dev.sol.module.impl;

import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.SliderSetting;

public class NameTagsModule extends Module {
   public final BooleanSetting players = (BooleanSetting)this.addSetting(new BooleanSetting("Players", "Tag players with their IGN", true));
   public final BooleanSetting self = (BooleanSetting)this.addSetting(new BooleanSetting("Self", "Also tag your own player (best in 3rd person)", false));
   public final BooleanSetting items = (BooleanSetting)this.addSetting(new BooleanSetting("Items", "Tag dropped items on the ground", true));
   public final BooleanSetting hidePlayerTags = (BooleanSetting)this.addSetting(new BooleanSetting("Hide Player Tags", "Remove the vanilla nametag of other players — only the module's shows", true));
   public final BooleanSetting hideOwnTag = (BooleanSetting)this.addSetting(new BooleanSetting("Hide Own Tag", "Remove your own original nametag — only the module's Self tag shows", true));
   public final BooleanSetting hideOtherTags = (BooleanSetting)this.addSetting(new BooleanSetting("Hide Other Tags", "Also remove vanilla nametags of mobs, armor stands & frames", false));
   public final BooleanSetting armor = (BooleanSetting)this.addSetting(new BooleanSetting("Armor", "Show a player's equipped armor above their tag", true));
   public final BooleanSetting heldItem = (BooleanSetting)this.addSetting(new BooleanSetting("Held Item", "Show what a player is holding above their tag", true));
   public final BooleanSetting health = (BooleanSetting)this.addSetting(new BooleanSetting("Health", "Health bar under player tags", true));
   public final BooleanSetting distance = (BooleanSetting)this.addSetting(new BooleanSetting("Distance", "Append the distance in blocks", true));
   public final BooleanSetting itemAmount = (BooleanSetting)this.addSetting(new BooleanSetting("Item Amount", "Show the stack size on item tags", true));
   public final SliderSetting scale = (SliderSetting)this.addSetting(new SliderSetting("Scale", "Tag size", (double)1.0F, (double)0.5F, (double)2.0F, 0.1, "x"));
   public final SliderSetting opacity = (SliderSetting)this.addSetting(new SliderSetting("Opacity", "Tag transparency", (double)100.0F, (double)10.0F, (double)100.0F, (double)5.0F, "%"));
   public final SliderSetting range = (SliderSetting)this.addSetting(new SliderSetting("Range", "Only tag within this distance", (double)64.0F, (double)8.0F, (double)256.0F, (double)4.0F, "m"));

   public NameTagsModule() {
      super("NameTags", "HUD-styled nametags for players & items", Category.RENDER);
   }
}
