package dev.sol.gui.widget;

import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.settings.KeybindSetting;
import dev.sol.theme.Theme;
import dev.sol.theme.ThemeManager;
import dev.sol.util.Colors;

/** Click the key box, then press a key (Esc clears, any mouse button but left binds it). */
public class KeybindWidget extends SettingWidget {
   public static final float HEIGHT = 20.0F;
   private KeybindSetting setting;
   private boolean listening;

   public KeybindWidget(ThemeManager themes, KeybindSetting setting) {
      super(themes, setting);
      this.setting = setting;
   }

   public float height(NVGRenderer vg) {
      return HEIGHT;
   }

   public void render(NVGRenderer vg, float mx, float my) {
      Theme theme = this.theme();
      float cy = this.floatVal2 + HEIGHT / 2.0F;
      boolean hovered = this.contains(mx, my);
      vg.text(this.setting.getName(), this.floatVal, cy, 12.0F, hovered ? theme.textPrimary() : theme.textMuted());
      String label = this.listening ? "Press a key..." : this.setting.keyName();
      float chipW = Math.max(34.0F, vg.textWidth(label, 11.0F) + 14.0F);
      float chipX = this.floatVal + this.width - chipW;
      float chipY = cy - 8.0F;
      vg.rect(chipX, chipY, chipW, 16.0F, 2.0F, this.listening ? Colors.withAlpha(theme.accent(), 0.18F) : Colors.withAlpha(0xFF000000, 0.35F));
      int outline = theme.outline();
      if (this.listening) {
         float pulse = (float)(0.5 + 0.5 * Math.sin((double)System.nanoTime() / 2.0E8));
         outline = Colors.withAlpha(theme.accent(), 0.45F + 0.55F * pulse);
      } else if (hovered) {
         outline = theme.accent();
      }

      vg.rectOutline(chipX, chipY, chipW, 16.0F, 2.0F, 1.0F, outline);
      vg.text(label, chipX + (chipW - vg.textWidth(label, 11.0F)) / 2.0F, cy, 11.0F, this.listening ? theme.accent() : theme.textPrimary());
   }

   public boolean mouseClicked(float mx, float my, int button) {
      if (this.listening) {
         if (button == 0) {
            this.listening = false;
            return this.contains(mx, my);
         } else {
            this.setting.set(button);
            this.listening = false;
            return true;
         }
      } else if (button == 0 && this.contains(mx, my)) {
         this.listening = true;
         return true;
      } else {
         return false;
      }
   }

   public boolean keyPressed(int keyCode) {
      if (!this.listening) {
         return false;
      } else {
         if (keyCode == 256) {
            this.setting.set(-1);
         } else {
            this.setting.set(keyCode);
         }

         this.listening = false;
         return true;
      }
   }

   public boolean isListening() {
      return this.listening;
   }
}
