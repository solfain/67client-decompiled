package dev.sol.module.impl;

import dev.sol.SolClient;
import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.ModeSetting;
import dev.sol.settings.SliderSetting;
import dev.sol.settings.StringSetting;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_310;

public class AutoTpaModule extends Module {
   public final ModeSetting mode = (ModeSetting)this.addSetting(new ModeSetting("Mode", "Which request to send.", "TPA", new String[]{"TPA", "TPAHere"}));
   public final StringSetting target = (StringSetting)this.addSetting(new StringSetting("Target", "Player to send the request to.", "", 32, "Steve"));
   public final SliderSetting delay = (SliderSetting)this.addSetting(new SliderSetting("Delay", "Time between requests. Lower is faster", (double)2000.0F, (double)250.0F, (double)10000.0F, (double)50.0F, "ms"));
   public final SliderSetting humanize = (SliderSetting)this.addSetting(new SliderSetting("Humanize", "Adds a random change to each delay so it does not look like a bot. 0 = off.", (double)25.0F, (double)0.0F, (double)60.0F, (double)5.0F, "%"));
   public final BooleanSetting notify = (BooleanSetting)this.addSetting(new BooleanSetting("Notify", "Show a notification each time a request is sent.", false));
   private long nextSendAtMs = -1L;
   private String lastSent;

   public AutoTpaModule() {
      super("AutoTPA", "Sends TPA requests to a player with random delays.", Category.PLAYER);
   }

   protected void onEnable() {
      if (((String)this.target.get()).trim().isEmpty()) {
         SolClient.notifications().pushInfo("AutoTPA: set a target first");
      }

      this.lastSent = null;
      this.nextSendAtMs = 0L;
   }

   protected void onDisable() {
      this.nextSendAtMs = -1L;
   }

   public void onTick() {
      if (this.nextSendAtMs >= 0L) {
         class_310 mc = class_310.method_1551();
         if (mc.field_1724 != null && mc.field_1724.field_3944 != null && System.currentTimeMillis() >= this.nextSendAtMs) {
            String name = ((String)this.target.get()).trim();
            if (name.isEmpty()) {
               this.nextSendAtMs = this.scheduleNext();
            } else {
               String var10000 = this.mode.check("TPAHere") ? "tpahere " : "tpa ";
               String cmd = var10000 + name;
               mc.field_1724.field_3944.method_45730(cmd);
               this.lastSent = cmd;
               if ((Boolean)this.notify.get()) {
                  SolClient.notifications().pushInfo("AutoTPA: /" + cmd);
               }

               this.nextSendAtMs = this.scheduleNext();
            }
         }
      }

   }

   public String lastSent() {
      return this.lastSent;
   }

   private long scheduleNext() {
      double base = (Double)this.delay.get();
      double j = (Double)this.humanize.get() / (double)100.0F;
      double factor = j <= (double)0.0F ? (double)1.0F : (double)1.0F + (ThreadLocalRandom.current().nextDouble() * (double)2.0F - (double)1.0F) * j;
      long wait = Math.max(0L, Math.round(base * factor));
      return System.currentTimeMillis() + wait;
   }
}
