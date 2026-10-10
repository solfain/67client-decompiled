package dev.sol.module;

public enum Category {
   COMBAT("Combat", "combat"),
   MOVEMENT("Movement", "movement"),
   PLAYER("Player", "player"),
   RENDER("Render", "render"),
   WORLD("World", "world"),
   MISC("Misc", "misc"),
   CLIENT("Client", "client");

   private String displayName;
   private String iconId;

   private Category(String displayName, String iconId) {
      this.displayName = displayName;
      this.iconId = iconId;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public String getIconId() {
      return this.iconId;
   }


}
