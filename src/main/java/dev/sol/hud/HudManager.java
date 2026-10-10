package dev.sol.hud;

import com.google.gson.JsonObject;
import dev.sol.hud.components.ArmorHud;
import dev.sol.hud.components.ArrayListHud;
import dev.sol.hud.components.InfoHud;
import dev.sol.hud.components.KeystrokesHud;
import dev.sol.hud.components.PotionsHud;
import dev.sol.hud.components.RadarHud;
import dev.sol.hud.components.RegionMapHud;
import dev.sol.hud.components.SpotifyHud;
import dev.sol.hud.components.StaffListHud;
import dev.sol.hud.components.WatermarkHud;
import dev.sol.module.ModuleManager;
import dev.sol.module.Modules;
import dev.sol.notification.NotificationManager;
import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.spotify.SpotifyService;
import dev.sol.theme.ThemeManager;
import dev.sol.util.CpsTracker;
import dev.sol.util.TpsTracker;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_310;
import net.minecraft.class_640;
import net.minecraft.class_746;

public class HudManager {
   private final List<HudComponent> components = new ArrayList();

   public HudManager(ModuleManager modules, ThemeManager themes, SpotifyService spotify, NotificationManager notifications) {
      Modules.HudModule hud = modules.hud;
      this.components.add(new WatermarkHud(themes, () -> hud.isEnabled() && (Boolean)hud.watermark.get()));
      this.components.add(new ArrayListHud(modules, hud, themes, () -> hud.isEnabled() && (Boolean)hud.arrayList.get()));
      this.components.add(new InfoHud("fps", themes, "FPS", () -> Integer.toString(class_310.method_1551().method_47599()), 0.006F, 0.985F, () -> hud.isEnabled() && (Boolean)hud.fps.get()));
      this.components.add(new InfoHud("ping", themes, "Ping", HudManager::pingString, 0.055F, 0.985F, () -> hud.isEnabled() && (Boolean)hud.ping.get()));
      this.components.add(new InfoHud("coords", themes, "XYZ", HudManager::coordsString, 0.115F, 0.985F, () -> hud.isEnabled() && (Boolean)hud.coordinates.get()));
      this.components.add(new InfoHud("direction", themes, "Facing", HudManager::directionString, 0.24F, 0.985F, () -> hud.isEnabled() && (Boolean)hud.direction.get()));
      this.components.add(new InfoHud("tps", themes, "TPS", () -> String.format("%.1f", TpsTracker.get()), 0.33F, 0.985F, () -> hud.isEnabled() && (Boolean)hud.tps.get()));
      this.components.add(new InfoHud("cps", themes, "CPS", () -> {
         int var10000 = CpsTracker.get(0);
         return var10000 + " | " + CpsTracker.get(1);
      }, 0.4F, 0.985F, () -> hud.isEnabled() && (Boolean)hud.cps.get()));
      this.components.add(new ArmorHud(themes, () -> hud.isEnabled() && (Boolean)hud.armor.get()));
      this.components.add(new PotionsHud(themes, () -> hud.isEnabled() && (Boolean)hud.potions.get()));
      this.components.add(new KeystrokesHud(themes, () -> hud.isEnabled() && (Boolean)hud.keystrokes.get()));
      this.components.add(new RadarHud(hud, modules.susChunkFinder, themes, () -> hud.isEnabled() && (Boolean)hud.radar.get()));
      this.components.add(new RegionMapHud(modules.regionMap, themes));
      this.components.add(new StaffListHud(modules.staffList, themes));
      this.components.add(new SpotifyHud(modules.spotify, spotify, themes));
      this.components.add(notifications);
   }

   private static String coordsString() {
      class_746 player = class_310.method_1551().field_1724;
      if (player == null) {
         return "0, 0, 0";
      } else {
         class_2338 pos = player.method_24515();
         int var10000 = pos.method_10263();
         return var10000 + ", " + pos.method_10264() + ", " + pos.method_10260();
      }
   }

   private static String pingString() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null && mc.method_1562() != null) {
         class_640 info = mc.method_1562().method_2871(mc.field_1724.method_5667());
         return info == null ? "0ms" : info.method_2959() + "ms";
      } else {
         return "0ms";
      }
   }

   private static String directionString() {
      class_746 player = class_310.method_1551().field_1724;
      if (player == null) {
         return "N";
      } else {
         class_2350 dir = player.method_5735();
         String var10000;
         switch (dir) {
            case field_11043 -> var10000 = "N  -Z";
            case field_11035 -> var10000 = "S  +Z";
            case field_11039 -> var10000 = "W  -X";
            case field_11034 -> var10000 = "E  +X";
            default -> var10000 = dir.method_10151().toUpperCase();
         }

         return var10000;
      }
   }

   public List<HudComponent> getComponents() {
      return this.components;
   }

   public List<Placement> layout(NVGRenderer vg, float uiWidth, float uiHeight, boolean includeHidden) {
      List<Placement> placements = new ArrayList();

      for(HudComponent component : this.components) {
         if (includeHidden || component.visible()) {
            float scale = component.getScale();
            float w = component.measureWidth(vg) * scale;
            float h = component.measureHeight(vg) * scale;
            float x = component.getFx() * (uiWidth - w);
            float y = component.getFy() * (uiHeight - h);
            placements.add(new Placement(component, x, y, w, h));
         }
      }

      return placements;
   }

   public void render(NVGRenderer vg, float uiWidth, float uiHeight) {
      for(Placement p : this.layout(vg, uiWidth, uiHeight, false)) {
         this.renderPlacement(vg, p);
      }

   }

   public void renderPlacement(NVGRenderer vg, Placement p) {
      float scale = p.component().getScale();
      vg.save();
      vg.translate((float)Math.round(p.getFloat()), (float)Math.round(p.getFloat2()));
      vg.scale(scale);
      p.component().render(vg, 0.0F, 0.0F, p.getFloat3() / scale, p.getFloat4() / scale);
      vg.restore();
   }

   public JsonObject toJson() {
      JsonObject json = new JsonObject();

      for(HudComponent component : this.components) {
         JsonObject entry = new JsonObject();
         entry.addProperty("fx", component.getFx());
         entry.addProperty("fy", component.getFy());
         entry.addProperty("scale", component.getScale());
         json.add(component.getId(), entry);
      }

      return json;
   }

   public void fromJson(JsonObject json) {
      for(HudComponent component : this.components) {
         JsonObject entry = json.getAsJsonObject(component.getId());
         if (entry != null && entry.has("fx") && entry.has("fy")) {
            component.setPosition(entry.get("fx").getAsFloat(), entry.get("fy").getAsFloat());
            if (entry.has("scale")) {
               component.setScale(entry.get("scale").getAsFloat());
            }
         }
      }

   }

   public final class Placement {
      private HudComponent component;
      private float floatVal;
      private float floatVal2;
      private float floatVal3;
      private float floatVal4;

      public Placement(HudComponent component, float x, float y, float w, float h) {
         this.component = component;
         this.floatVal = x;
         this.floatVal2 = y;
         this.floatVal3 = w;
         this.floatVal4 = h;
      }

      public boolean contains(float px, float py) {
         return px >= this.floatVal && px <= this.floatVal + this.floatVal3 && py >= this.floatVal2 && py <= this.floatVal2 + this.floatVal4;
      }

      public HudComponent component() {
         return this.component;
      }

      public float getFloat() {
         return this.floatVal;
      }

      public float getFloat2() {
         return this.floatVal2;
      }

      public float getFloat3() {
         return this.floatVal3;
      }

      public float getFloat4() {
         return this.floatVal4;
      }
   }
}
