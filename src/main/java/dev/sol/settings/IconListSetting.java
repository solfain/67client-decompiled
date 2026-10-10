package dev.sol.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_1792;

public class IconListSetting extends Setting<List<Entry>> {
   private final Map<String, Entry> byKey = new LinkedHashMap();

   public IconListSetting(String name, String description) {
      super(name, description, new ArrayList());
   }

   public Entry add(String key, String label, class_1792 icon, boolean enabled, int color) {
      Entry entry = new Entry(key, label, icon, enabled, color);
      ((List)this.value).add(entry);
      this.byKey.put(key, entry);
      return entry;
   }

   public List<Entry> entries() {
      return this.value;
   }

   public Entry get(String key) {
      return (Entry)this.byKey.get(key);
   }

   public boolean isEnabled(String key) {
      Entry e = (Entry)this.byKey.get(key);
      return e != null && (Boolean)e.enabled.get();
   }

   public int color(String key) {
      Entry e = (Entry)this.byKey.get(key);
      return e != null ? (Integer)e.color.get() : -1;
   }

   public int size() {
      return ((List)this.value).size();
   }

   public long enabledCount() {
      return ((List)this.value).stream().filter((e) -> (Boolean)e.enabled.get()).count();
   }

   public JsonElement toJson() {
      JsonArray arr = new JsonArray();

      for(Entry e : this.value) {
         JsonObject o = new JsonObject();
         o.addProperty("key", e.key);
         o.addProperty("enabled", (Boolean)e.enabled.get());
         o.addProperty("color", (Number)e.color.get());
         arr.add(o);
      }

      return arr;
   }

   public void fromJson(JsonElement element) {
      if (element != null && element.isJsonArray()) {
         for(JsonElement el : element.getAsJsonArray()) {
            if (el.isJsonObject()) {
               JsonObject o = el.getAsJsonObject();
               if (o.has("key")) {
                  Entry e = (Entry)this.byKey.get(o.get("key").getAsString());
                  if (e != null) {
                     if (o.has("enabled")) {
                        e.enabled.set(o.get("enabled").getAsBoolean());
                     }

                     if (o.has("color")) {
                        e.color.set(o.get("color").getAsInt());
                     }
                  }
               }
            }
         }
      }

   }

   public final class Entry {
      private String key;
      private String label;
      private class_1792 icon;
      public BooleanSetting enabled;
      public ColorSetting color;

      Entry(String key, String label, class_1792 icon, boolean enabled, int color) {
         this.key = key;
         this.label = label;
         this.icon = icon;
         this.enabled = new BooleanSetting("Enabled", "Highlight this type", enabled);
         this.color = new ColorSetting(label, "Highlight color", color);
      }

      public String key() {
         return this.key;
      }

      public String label() {
         return this.label;
      }

      public class_1792 icon() {
         return this.icon;
      }

      public boolean matches(String lowerQuery) {
         return this.label.toLowerCase(Locale.ROOT).contains(lowerQuery) || this.key.contains(lowerQuery);
      }
   }
}
