package dev.sol.theme;

import com.google.gson.JsonObject;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.Setting;
import dev.sol.settings.SliderSetting;
import java.util.List;

public class SoundSettings {
   public final SliderSetting masterVolume = new SliderSetting("Master Volume", "Volume for all client sounds", (double)60.0F, (double)0.0F, (double)100.0F, (double)5.0F, "%");
   public final BooleanSetting guiSounds = new BooleanSetting("GUI Open/Close", "Whoosh when the menu opens and closes", true);
   public final BooleanSetting hoverSounds = new BooleanSetting("Hover", "Soft ticks when hovering elements", true);
   public final BooleanSetting clickSounds = new BooleanSetting("Clicks & Toggles", "Pops for toggles, sliders and keybinds", true);
   public final BooleanSetting notificationSounds = new BooleanSetting("Notifications", "Chimes with toggle toasts", true);
   public final BooleanSetting startupSol = new BooleanSetting("Sol", "Startup track candidate", true);
   public final BooleanSetting startupSad = new BooleanSetting("Sol Sad Song", "Startup track candidate", false);
   public final BooleanSetting startupSong = new BooleanSetting("Sol Song", "Startup track candidate", false);
   public final BooleanSetting startupTiki = new BooleanSetting("Sol Tiki Phonk", "Startup track candidate", false);
   private final List<Setting<?>> all;

   public SoundSettings() {
      this.all = List.of(this.masterVolume, this.guiSounds, this.hoverSounds, this.clickSounds, this.notificationSounds, this.startupSol, this.startupSad, this.startupSong, this.startupTiki);
   }

   public List<Setting<?>> all() {
      return this.all;
   }

   public float volume() {
      return this.masterVolume.getFloat() / 100.0F;
   }

   public JsonObject toJson() {
      JsonObject json = new JsonObject();

      for(Setting<?> setting : this.all) {
         json.add(setting.getName(), setting.toJson());
      }

      return json;
   }

   public void fromJson(JsonObject json) {
      for(Setting<?> setting : this.all) {
         if (json.has(setting.getName())) {
            setting.fromJson(json.get(setting.getName()));
         }
      }

   }
}
