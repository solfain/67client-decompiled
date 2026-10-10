package dev.sol.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.lwjgl.glfw.GLFW;

public class KeybindSetting extends Setting<Integer> {
   public static final int NONE = -1;

   public KeybindSetting(String name, String description, int defaultKey) {
      super(name, description, defaultKey);
   }

   public boolean isBound() {
      return (Integer)this.get() != -1;
   }

   public boolean matches(int keyCode) {
      return this.isBound() && (Integer)this.get() == keyCode;
   }

   public String keyName() {
      int key = (Integer)this.get();
      if (key == -1) {
         return "None";
      } else if (key >= 0 && key <= 7) {
         String var3;
         switch (key) {
            case 0 -> var3 = "LMB";
            case 1 -> var3 = "RMB";
            case 2 -> var3 = "MMB";
            default -> var3 = "MB" + (key + 1);
         }

         return var3;
      } else {
         String name = GLFW.glfwGetKeyName(key, 0);
         if (name != null) {
            return name.toUpperCase();
         } else {
            String var10000;
            switch (key) {
               case 32 -> var10000 = "SPACE";
               case 257 -> var10000 = "ENTER";
               case 258 -> var10000 = "TAB";
               case 259 -> var10000 = "BACK";
               case 260 -> var10000 = "INSERT";
               case 261 -> var10000 = "DELETE";
               case 262 -> var10000 = "RIGHT";
               case 263 -> var10000 = "LEFT";
               case 264 -> var10000 = "DOWN";
               case 265 -> var10000 = "UP";
               case 266 -> var10000 = "PGUP";
               case 267 -> var10000 = "PGDN";
               case 268 -> var10000 = "HOME";
               case 269 -> var10000 = "END";
               case 280 -> var10000 = "CAPS";
               case 340 -> var10000 = "LSHIFT";
               case 341 -> var10000 = "LCTRL";
               case 342 -> var10000 = "LALT";
               case 344 -> var10000 = "RSHIFT";
               case 345 -> var10000 = "RCTRL";
               case 346 -> var10000 = "RALT";
               default -> var10000 = key >= 290 && key <= 314 ? "F" + (key - 290 + 1) : "KEY" + key;
            }

            return var10000;
         }
      }
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
