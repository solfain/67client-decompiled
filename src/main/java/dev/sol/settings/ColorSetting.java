package dev.sol.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class ColorSetting extends Setting<Integer> {
   public ColorSetting(String name, String description, int defaultArgb) {
      super(name, description, defaultArgb);
   }

   public int red() {
      return (Integer)this.get() >> 16 & 255;
   }

   public int green() {
      return (Integer)this.get() >> 8 & 255;
   }

   public int blue() {
      return (Integer)this.get() & 255;
   }

   public int alpha() {
      return (Integer)this.get() >>> 24 & 255;
   }

   public void setRed(int r) {
      this.set((Integer)this.get() & -16711681 | (r & 255) << 16);
   }

   public void setGreen(int g) {
      this.set((Integer)this.get() & -65281 | (g & 255) << 8);
   }

   public void setBlue(int b) {
      this.set((Integer)this.get() & -256 | b & 255);
   }

   public String hex() {
      return String.format("#%06X", (Integer)this.get() & 16777215);
   }

   public JsonElement toJson() {
      return new JsonPrimitive((Number)this.value);
   }

   public void fromJson(JsonElement element) {
      if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
         this.value = element.getAsInt();
      }

   }
}
