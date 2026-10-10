package dev.sol.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BooleanSetting extends Setting<Boolean> {
   public BooleanSetting(String name, String description, boolean defaultValue) {
      super(name, description, defaultValue);
   }

   public void toggle() {
      this.set(!(Boolean)this.get());
   }

   public JsonElement toJson() {
      return new JsonPrimitive(this.value);
   }

   public void fromJson(JsonElement element) {
      if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
         this.value = element.getAsBoolean();
      }

   }
}
