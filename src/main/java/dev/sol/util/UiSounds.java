package dev.sol.util;

import dev.sol.theme.SoundSettings;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1109;
import net.minecraft.class_310;
import net.minecraft.class_3414;

public final class UiSounds {
   private static SoundSettings settings = new SoundSettings();
   private static long lastHoverNanos;
   private static long lastSliderNanos;

   private UiSounds() {
   }

   public static void init(SoundSettings soundSettings) {
      settings = soundSettings;
   }

   private static void play(class_3414 event, float pitch, float volume) {
      float master = settings.volume();
      if (!(master <= 0.01F)) {
         class_310.method_1551().method_1483().method_4873(class_1109.method_4757(event, pitch, volume * master));
      }

   }

   public static void guiOpen() {
      if ((Boolean)settings.guiSounds.get()) {
         play(UiSoundEvents.GUI_OPEN, 1.0F, 0.85F);
      }

   }

   public static void guiClose() {
      if ((Boolean)settings.guiSounds.get()) {
         play(UiSoundEvents.GUI_CLOSE, 1.0F, 0.75F);
      }

   }

   public static void hover() {
      if ((Boolean)settings.hoverSounds.get()) {
         long now = System.nanoTime();
         if (now - lastHoverNanos >= 45000000L) {
            lastHoverNanos = now;
            play(UiSoundEvents.HOVER, 0.95F + (float)(now % 7L) * 0.015F, 0.5F);
         }
      }

   }

   public static void toggle(boolean enabled) {
      if ((Boolean)settings.clickSounds.get()) {
         play(enabled ? UiSoundEvents.TOGGLE_ON : UiSoundEvents.TOGGLE_OFF, 1.0F, 0.8F);
      }

   }

   public static void checkbox(boolean checked) {
      if ((Boolean)settings.clickSounds.get()) {
         play(checked ? UiSoundEvents.TOGGLE_ON : UiSoundEvents.TOGGLE_OFF, 1.25F, 0.55F);
      }

   }

   public static void select() {
      if ((Boolean)settings.clickSounds.get()) {
         play(UiSoundEvents.SELECT, 1.0F, 0.65F);
      }

   }

   public static void sliderTick(float normalized) {
      if ((Boolean)settings.clickSounds.get()) {
         long now = System.nanoTime();
         if (now - lastSliderNanos >= 60000000L) {
            lastSliderNanos = now;
            play(UiSoundEvents.SLIDER, 0.85F + normalized * 0.55F, 0.5F);
         }
      }

   }

   public static void keybindListen() {
      if ((Boolean)settings.clickSounds.get()) {
         play(UiSoundEvents.KEYBIND, 0.8F, 0.6F);
      }

   }

   public static void keybindSet() {
      if ((Boolean)settings.clickSounds.get()) {
         play(UiSoundEvents.KEYBIND, 1.1F, 0.6F);
      }

   }

   public static void notification(boolean enabled) {
      if ((Boolean)settings.notificationSounds.get()) {
         play(enabled ? UiSoundEvents.NOTIFY_ON : UiSoundEvents.NOTIFY_OFF, 1.0F, 0.7F);
      }

   }

   public static void panelCollapse() {
      if ((Boolean)settings.clickSounds.get()) {
         play(UiSoundEvents.SELECT, 0.8F, 0.5F);
      }

   }

   public static void playStartup() {
      List<class_3414> pool = new ArrayList();
      if ((Boolean)settings.startupSol.get()) {
         pool.add(UiSoundEvents.STARTUP_SOL);
      }

      if ((Boolean)settings.startupSad.get()) {
         pool.add(UiSoundEvents.STARTUP_SAD);
      }

      if ((Boolean)settings.startupSong.get()) {
         pool.add(UiSoundEvents.STARTUP_SONG);
      }

      if ((Boolean)settings.startupTiki.get()) {
         pool.add(UiSoundEvents.STARTUP_TIKI);
      }

      if (!pool.isEmpty()) {
         class_3414 pick = (class_3414)pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
         play(pick, 1.0F, 0.9F);
      }

   }
}
