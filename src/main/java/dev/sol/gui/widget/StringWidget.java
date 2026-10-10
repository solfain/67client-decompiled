package dev.sol.gui.widget;

import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.settings.StringSetting;
import dev.sol.theme.Theme;
import dev.sol.theme.ThemeManager;
import dev.sol.util.Colors;
import dev.sol.util.UiSounds;

public class StringWidget extends SettingWidget {
   private static final float HEIGHT = 34.0F;
   private static final float BOX_H = 18.0F;
   private static final float FONT = 12.0F;
   private StringSetting setting;
   private boolean focused;

   public StringWidget(ThemeManager themes, StringSetting setting) {
      super(themes, setting);
      this.setting = setting;
   }

   public float height(NVGRenderer vg) {
      return 34.0F;
   }

   public void render(NVGRenderer vg, float mx, float my) {
      Theme theme = this.theme();
      vg.text(this.setting.getName(), this.floatVal, this.floatVal2 + 8.0F, 12.0F, this.focused ? theme.textPrimary() : theme.textMuted());
      float bx = this.floatVal;
      float by = this.floatVal2 + 14.0F;
      float bw = this.width;
      int fill = Colors.withAlpha(-16777216, 0.35F);
      vg.rect(bx, by, bw, 18.0F, 2.0F, fill);
      vg.rectOutline(bx, by, bw, 18.0F, 2.0F, 1.0F, this.focused ? theme.accent() : theme.outline());
      float textX = bx + 8.0F;
      float textY = by + 9.0F;
      String value = (String)this.setting.get();
      if (value.isEmpty() && !this.focused) {
         vg.text(this.setting.getPlaceholder(), textX, textY, 12.0F, theme.textDisabled());
      } else {
         float w = vg.text(value, textX, textY, 12.0F, theme.textPrimary());
         if (this.focused && System.nanoTime() / 400000000L % 2L == 0L) {
            vg.rect(textX + w + 1.5F, textY - 5.5F, 1.2F, 11.0F, 0.0F, theme.accent());
         }
      }

   }

   public boolean mouseClicked(float mx, float my, int button) {
      if (button == 0 && this.contains(mx, my)) {
         this.focused = true;
         UiSounds.select();
         return true;
      } else {
         this.focused = false;
         return false;
      }
   }

   public boolean keyPressed(int keyCode) {
      if (!this.focused) {
         return false;
      } else {
         switch (keyCode) {
            case 256:
            case 257:
            case 335:
               this.focused = false;
               break;
            case 259:
               String v = (String)this.setting.get();
               if (!v.isEmpty()) {
                  this.setting.set(v.substring(0, v.length() - 1));
               }
         }

         return true;
      }
   }

   public boolean charTyped(int codepoint) {
      if (!this.focused) {
         return false;
      } else if (((String)this.setting.get()).length() >= this.setting.getMaxLength()) {
         return true;
      } else if (Character.isValidCodePoint(codepoint) && !Character.isISOControl(codepoint)) {
         StringSetting var10000 = this.setting;
         String var10001 = (String)this.setting.get();
         var10000.set(var10001 + new String(Character.toChars(codepoint)));
         return true;
      } else {
         return true;
      }
   }

   public boolean isListening() {
      return this.focused;
   }
}
