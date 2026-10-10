package dev.sol.gui;

import com.google.gson.JsonObject;
import dev.sol.module.Category;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ClickGuiState {
   public static final String THEMES_PANEL = "__themes__";
   private final Map<String, PanelState> panels = new LinkedHashMap();
   private final Set<String> expandedModules = new HashSet();
   private boolean laidOut;
   private boolean customized;
   private float lastLayoutWidth = -1.0F;
   private float lastLayoutHeight = -1.0F;
   private static final int LAYOUT_VERSION = 4;
   private static final float PANEL_W = 172.0F;
   private static final float PANEL_GAP = 10.0F;
   private static final float TOP_Y = 46.0F;

   public void markCustomized() {
      this.customized = true;
   }

   public ClickGuiState() {
      float x = 10.0F;

      for(Category category : Category.values()) {
         this.panels.put(category.name(), new PanelState(x, TOP_Y));
         x += PANEL_W + PANEL_GAP;
      }

      this.panels.put("__themes__", new PanelState(x, TOP_Y));
   }

   /** Lays the windows out in a centred row under the top bar (wrapping when the screen is narrow). */
   public void ensureDefaultLayout(float uiWidth, float uiHeight) {
      if (!this.customized && (!this.laidOut || uiWidth != this.lastLayoutWidth || uiHeight != this.lastLayoutHeight)) {
         this.laidOut = true;
         this.lastLayoutWidth = uiWidth;
         this.lastLayoutHeight = uiHeight;
         String[] order = new String[]{Category.COMBAT.name(), Category.MISC.name(), Category.RENDER.name(), Category.VISUALS.name(), Category.CLIENT.name(), "__themes__"};
         float step = PANEL_W + PANEL_GAP;
         int perRow = Math.max(1, Math.min(order.length, (int)((uiWidth - PANEL_GAP) / step)));
         int rows = (order.length + perRow - 1) / perRow;

         for(int i = 0; i < order.length; ++i) {
            int row = i / perRow;
            int col = i % perRow;
            int inThisRow = Math.min(perRow, order.length - row * perRow);
            float rowWidth = (float)inThisRow * step - PANEL_GAP;
            PanelState ps = this.panel(order[i]);
            ps.floatVal = (uiWidth - rowWidth) / 2.0F + (float)col * step;
            ps.floatVal2 = TOP_Y + (float)row * (uiHeight - TOP_Y) / (float)rows;
            ps.collapsed = false;
         }
      }

   }

   public PanelState panel(String key) {
      return (PanelState)this.panels.computeIfAbsent(key, (k) -> new PanelState(16.0F, 16.0F));
   }

   public boolean isExpanded(String moduleKey) {
      return this.expandedModules.contains(moduleKey);
   }

   /** The module whose settings window is open, or null. Only one is open at a time. */
   public String openModuleKey() {
      return this.expandedModules.isEmpty() ? null : (String)this.expandedModules.iterator().next();
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
      json.addProperty("v", 4);
      json.addProperty("custom", this.customized);

      for(Map.Entry<String, PanelState> entry : this.panels.entrySet()) {
         JsonObject p = new JsonObject();
         p.addProperty("x", ((PanelState)entry.getValue()).floatVal);
         p.addProperty("y", ((PanelState)entry.getValue()).floatVal2);
         p.addProperty("collapsed", ((PanelState)entry.getValue()).collapsed);
         json.add((String)entry.getKey(), p);
      }

      return json;
   }

   public void fromJson(JsonObject json) {
      if (json.has("v") && json.get("v").getAsInt() >= 4 && json.has("custom") && json.get("custom").getAsBoolean()) {
         this.laidOut = true;
         this.customized = true;

         for(Map.Entry<String, PanelState> entry : this.panels.entrySet()) {
            JsonObject p = json.getAsJsonObject((String)entry.getKey());
            if (p != null) {
               if (p.has("x")) {
                  ((PanelState)entry.getValue()).floatVal = p.get("x").getAsFloat();
               }

               if (p.has("y")) {
                  ((PanelState)entry.getValue()).floatVal2 = p.get("y").getAsFloat();
               }

               if (p.has("collapsed")) {
                  ((PanelState)entry.getValue()).collapsed = p.get("collapsed").getAsBoolean();
               }
            }
         }
      }

   }

   public class PanelState {
      public float floatVal;
      public float floatVal2;
      public boolean collapsed;

      PanelState(float x, float y) {
         this.floatVal = x;
         this.floatVal2 = y;
      }
   }
}
