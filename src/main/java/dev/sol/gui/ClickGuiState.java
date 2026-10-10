package dev.sol.gui;

import com.google.gson.JsonObject;
import dev.sol.module.Category;
import java.util.HashSet;
import java.util.Set;

/** Persistent ClickGUI state: which tab is selected and which module's settings are open. */
public class ClickGuiState {
   public static final String THEMES_TAB = "__themes__";
   private final Set<String> expandedModules = new HashSet();
   private String tab = Category.COMBAT.name();

   public String tab() {
      return this.tab;
   }

   public void setTab(String tab) {
      this.tab = tab;
   }

   public Category category() {
      for(Category c : Category.values()) {
         if (c.name().equals(this.tab)) {
            return c;
         }
      }

      return null;
   }

   /** The module whose settings window is open, or null. Only one is open at a time. */
   public String openModuleKey() {
      return this.expandedModules.isEmpty() ? null : (String)this.expandedModules.iterator().next();
   }

   public boolean isExpanded(String moduleKey) {
      return this.expandedModules.contains(moduleKey);
   }

   public void setExpanded(String moduleKey, boolean expanded) {
      if (expanded) {
         this.expandedModules.clear();
         this.expandedModules.add(moduleKey);
      } else {
         this.expandedModules.remove(moduleKey);
      }

   }

   public JsonObject toJson() {
      JsonObject json = new JsonObject();
      json.addProperty("v", 5);
      json.addProperty("tab", this.tab);
      return json;
   }

   public void fromJson(JsonObject json) {
      if (json != null && json.has("v") && json.get("v").getAsInt() >= 5 && json.has("tab")) {
         String t = json.get("tab").getAsString();
         if (THEMES_TAB.equals(t)) {
            this.tab = t;
         } else {
            for(Category c : Category.values()) {
               if (c.name().equals(t)) {
                  this.tab = t;
               }
            }
         }
      }

   }
}
