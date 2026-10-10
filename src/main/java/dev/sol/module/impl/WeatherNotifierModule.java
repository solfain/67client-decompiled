package dev.sol.module.impl;

import dev.sol.SolClient;
import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.notification.NotificationManager;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.ModeSetting;
import net.minecraft.class_1109;
import net.minecraft.class_1937;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;

public class WeatherNotifierModule extends Module {
   public final BooleanSetting rain = (BooleanSetting)this.addSetting(new BooleanSetting("Rain", "Announce when rain starts/stops", true));
   public final BooleanSetting thunder = (BooleanSetting)this.addSetting(new BooleanSetting("Thunder", "Announce when a thunderstorm starts/stops", true));
   public final BooleanSetting sound = (BooleanSetting)this.addSetting(new BooleanSetting("Sound", "Play a ping on each change", true));
   public final ModeSetting output = (ModeSetting)this.addSetting(new ModeSetting("Output", "How the change is shown", "Notification", new String[]{"Notification", "Chat", "Action Bar"}));
   private Boolean lastRaining;
   private Boolean lastThundering;

   public WeatherNotifierModule() {
      super("WeatherNotifier", "Themed toast when the weather changes", Category.WORLD);
   }

   protected void onEnable() {
      this.lastRaining = null;
      this.lastThundering = null;
   }

   public void onTick() {
      class_310 mc = class_310.method_1551();
      class_1937 level = mc.field_1687;
      if (level != null && mc.field_1724 != null) {
         boolean raining = level.method_8419();
         boolean thundering = level.method_8546();
         if (this.lastRaining != null && this.lastThundering != null) {
            if (thundering != this.lastThundering) {
               this.lastThundering = thundering;
               if ((Boolean)this.thunder.get()) {
                  if (thundering) {
                     this.notify(mc, "Thunderstorm", "A storm rolls in", NotificationManager.Weather.THUNDER, true);
                  } else {
                     this.notify(mc, "Storm cleared", "The thunder has passed", NotificationManager.Weather.CLEAR, false);
                  }
               }
            }

            if (raining != this.lastRaining) {
               this.lastRaining = raining;
               if ((Boolean)this.rain.get() && !thundering) {
                  if (raining) {
                     this.notify(mc, "Rain", "Rain starts to fall", NotificationManager.Weather.RAIN, true);
                  } else {
                     this.notify(mc, "Skies cleared", "The rain has stopped", NotificationManager.Weather.CLEAR, false);
                  }
               }
            }
         } else {
            this.lastRaining = raining;
            this.lastThundering = thundering;
         }
      }

   }

   private void notify(class_310 mc, String title, String subtitle, NotificationManager.Weather weather, boolean started) {
      if (mc.field_1724 != null) {
         if (this.output.check("Notification")) {
            if (SolClient.notifications() != null) {
               SolClient.notifications().pushWeather(title, subtitle, weather, started);
            }
         } else {
            boolean actionBar = this.output.check("Action Bar");
            mc.field_1724.method_7353(class_2561.method_43470("§6[Sol] §fnull — null"), actionBar);
         }

         if ((Boolean)this.sound.get()) {
            float pitch = started ? 1.2F : 0.8F;
            mc.method_1483().method_4873(class_1109.method_4757((class_3414)class_3417.field_14793.comp_349(), pitch, 0.6F));
         }
      }

   }
}
