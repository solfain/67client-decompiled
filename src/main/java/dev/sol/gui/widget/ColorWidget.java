package dev.sol.gui.widget;

import dev.sol.render.anim.Animation;
import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.settings.ColorSetting;
import dev.sol.theme.Theme;
import dev.sol.theme.ThemeManager;
import dev.sol.util.Colors;
import dev.sol.util.UiSounds;

public class ColorWidget extends SettingWidget {
   private static final float ROW = 22.0F;
   private static final float SV_H = 74.0F;
   private static final float HUE_H = 10.0F;
   private static final float BOTTOM_H = 22.0F;
   private static final float GAP = 6.0F;
   private ColorSetting setting;
   private final Animation expand = new Animation(170.0F, 0.0F);
   private boolean expanded;
   private float hue;
   private float sat;
   private float val;
   private boolean draggingSv;
   private boolean draggingHue;
   private boolean hexFocused;
   private final StringBuilder hexBuffer = new StringBuilder();

   public ColorWidget(ThemeManager themes, ColorSetting setting) {
      super(themes, setting);
      this.setting = setting;
      this.syncFromSetting();
   }

   private void syncFromSetting() {
      float[] hsv = Colors.rgbToHsv((Integer)this.setting.get());
      this.hue = hsv[0];
      this.sat = hsv[1];
      this.val = hsv[2];
   }

   public void setExpanded(boolean value) {
      if (value && !this.expanded) {
         this.syncFromSetting();
      }

      this.expanded = value;
   }

   public boolean isExpanded() {
      return this.expanded;
   }

   private void apply() {
      this.setting.set(Colors.hsvToRgb(this.hue, this.sat, this.val));
   }

   public float height(NVGRenderer vg) {
      return 22.0F + this.expand.value() * 124.0F;
   }

   public void render(NVGRenderer vg, float mx, float my) {
      Theme theme = this.theme();
      float cy = this.floatVal2 + 11.0F;
      vg.text(this.setting.getName(), this.floatVal, cy, 12.0F, theme.textMuted());
      vg.rect(this.floatVal + this.width - 24.0F, cy - 6.0F, 24.0F, 12.0F, 2.0F, (Integer)this.setting.get() | -16777216);
      vg.rectOutline(this.floatVal + this.width - 24.0F, cy - 6.0F, 24.0F, 12.0F, 2.0F, 1.0F, this.expanded ? theme.accent() : theme.outline());
      this.expand.setTarget(this.expanded ? 1.0F : 0.0F);
      float t = this.expand.value();
      if (!(t < 0.01F)) {
         vg.save();
         vg.scissor(this.floatVal - 4.0F, this.floatVal2 + 22.0F, this.width + 8.0F, t * 124.0F);
         vg.alpha(t);
         float svY = this.svTop();
         int hueColor = Colors.hsvToRgb(this.hue, 1.0F, 1.0F);
         vg.rect(this.floatVal, svY, this.width, 74.0F, 2.0F, hueColor);
         vg.rectGradient(this.floatVal, svY, this.width, 74.0F, 2.0F, -1, 16777215, false);
         vg.rectGradient(this.floatVal, svY, this.width, 74.0F, 2.0F, 0, -16777216, true);
         float knobX = this.floatVal + this.sat * this.width;
         float knobY = svY + (1.0F - this.val) * 74.0F;
         vg.circle(knobX, knobY, 6.0F, -1);
         vg.circle(knobX, knobY, 4.2F, Colors.hsvToRgb(this.hue, this.sat, this.val));
         float hueY = this.hueTop();
         float seg = this.width / 6.0F;

         for(int i = 0; i < 6; ++i) {
            int from = Colors.hsvToRgb((float)(i * 60), 1.0F, 1.0F);
            int to = Colors.hsvToRgb((i + 1) * 60 % 360 == 0 ? 359.9F : (float)((i + 1) * 60), 1.0F, 1.0F);
            vg.rectGradient(this.floatVal + (float)i * seg, hueY, seg + 0.5F, 10.0F, 0.0F, from, to, false);
         }

         vg.rectOutline(this.floatVal, hueY, this.width, 10.0F, 1.0F, 1.0F, theme.outline());
         float hueKnobX = this.floatVal + this.hue / 360.0F * this.width;
         vg.circle(hueKnobX, hueY + 5.0F, 6.0F, -1);
         vg.circle(hueKnobX, hueY + 5.0F, 4.2F, hueColor);
         float botY = this.bottomTop();
         vg.rect(this.floatVal, botY, 30.0F, 18.0F, 2.0F, (Integer)this.setting.get() | -16777216);
         vg.rectOutline(this.floatVal, botY, 30.0F, 18.0F, 2.0F, 1.0F, theme.outline());
         float hexX = this.floatVal + 38.0F;
         float hexW = this.width - 38.0F;
         int boxFill = this.hexFocused ? Colors.withAlpha(theme.accent(), 0.18F) : Colors.withAlpha(-16777216, 0.45F);
         vg.rect(hexX, botY, hexW, 18.0F, 2.0F, boxFill);
         if (this.hexFocused) {
            vg.rectOutline(hexX, botY, hexW, 18.0F, 2.0F, 1.0F, theme.accent());
         }

         String text = this.hexFocused ? "#" + String.valueOf(this.hexBuffer) + (System.nanoTime() / 400000000L % 2L == 0L ? "_" : "") : this.setting.hex();
         vg.text(text, hexX + 8.0F, botY + 9.0F, 12.0F, this.hexFocused ? theme.textPrimary() : theme.textMuted());
         vg.restore();
      }

   }

   private float svTop() {
      return this.floatVal2 + 22.0F + 2.0F;
   }

   private float hueTop() {
      return this.svTop() + 74.0F + 6.0F;
   }

   private float bottomTop() {
      return this.hueTop() + 10.0F + 6.0F;
   }

   public boolean mouseClicked(float mx, float my, int button) {
      if (button != 0) {
         return false;
      } else if (my >= this.floatVal2 && my <= this.floatVal2 + 22.0F && mx >= this.floatVal && mx <= this.floatVal + this.width) {
         this.expanded = !this.expanded;
         if (this.expanded) {
            this.syncFromSetting();
         }

         this.hexFocused = false;
         UiSounds.select();
         return true;
      } else if (!this.expanded) {
         return false;
      } else if (inRect(mx, my, this.floatVal, this.svTop(), this.width, 74.0F)) {
         this.draggingSv = true;
         this.applySv(mx, my);
         return true;
      } else if (inRect(mx, my, this.floatVal, this.hueTop() - 3.0F, this.width, 16.0F)) {
         this.draggingHue = true;
         this.applyHue(mx);
         return true;
      } else if (inRect(mx, my, this.floatVal + 38.0F, this.bottomTop(), this.width - 38.0F, 18.0F)) {
         this.hexFocused = true;
         this.hexBuffer.setLength(0);
         UiSounds.select();
         return true;
      } else {
         if (this.hexFocused) {
            this.commitHex();
         }

         return false;
      }
   }

   private static boolean inRect(float mx, float my, float rx, float ry, float rw, float rh) {
      return mx >= rx && mx <= rx + rw && my >= ry && my <= ry + rh;
   }

   private void applySv(float mx, float my) {
      this.sat = Math.clamp((mx - this.floatVal) / this.width, 0.0F, 1.0F);
      this.val = 1.0F - Math.clamp((my - this.svTop()) / 74.0F, 0.0F, 1.0F);
      this.apply();
   }

   private void applyHue(float mx) {
      this.hue = Math.clamp((mx - this.floatVal) / this.width, 0.0F, 1.0F) * 359.9F;
      this.apply();
   }

   public void mouseDragged(float mx, float my) {
      if (this.draggingSv) {
         this.applySv(mx, my);
      }

      if (this.draggingHue) {
         this.applyHue(mx);
      }

   }

   public void mouseReleased() {
      this.draggingSv = false;
      this.draggingHue = false;
   }

   public boolean keyPressed(int keyCode) {
      if (!this.hexFocused) {
         return false;
      } else if (keyCode == 256) {
         this.hexFocused = false;
         return true;
      } else if (keyCode != 257 && keyCode != 335) {
         if (keyCode == 259) {
            if (!this.hexBuffer.isEmpty()) {
               this.hexBuffer.deleteCharAt(this.hexBuffer.length() - 1);
            }

            return true;
         } else {
            char c = hexChar(keyCode);
            if (c != 0 && this.hexBuffer.length() < 6) {
               this.hexBuffer.append(c);
               if (this.hexBuffer.length() == 6) {
                  this.commitHex();
               }
            }

            return true;
         }
      } else {
         this.commitHex();
         return true;
      }
   }

   private static char hexChar(int keyCode) {
      if (keyCode >= 48 && keyCode <= 57) {
         return (char)(48 + keyCode - 48);
      } else if (keyCode >= 320 && keyCode <= 329) {
         return (char)(48 + keyCode - 320);
      } else {
         return keyCode >= 65 && keyCode <= 70 ? (char)(65 + keyCode - 65) : '\u0000';
      }
   }

   private void commitHex() {
      this.hexFocused = false;
      if (this.hexBuffer.length() == 6) {
         try {
            this.setting.set(-16777216 | Integer.parseInt(this.hexBuffer.toString(), 16));
            this.syncFromSetting();
            UiSounds.keybindSet();
         } catch (NumberFormatException var2) {
         }
      }

      this.hexBuffer.setLength(0);
   }

   public boolean isListening() {
      return this.hexFocused;
   }
}
