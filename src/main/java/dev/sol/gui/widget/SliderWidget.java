package dev.sol.gui.widget;

import dev.sol.render.anim.Animation;
import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.settings.SliderSetting;
import dev.sol.theme.Theme;
import dev.sol.theme.ThemeManager;
import dev.sol.util.Colors;
import dev.sol.util.UiSounds;

/** Label + value on one line, thin track with a small square handle underneath. */
public class SliderWidget extends SettingWidget {
   public static final float HEIGHT = 28.0F;
   private static final float TRACK_H = 3.0F;
   private static final float HANDLE = 8.0F;
   private SliderSetting setting;
   private final Animation fill = new Animation(80.0F, 0.0F);
   private final Animation hover = new Animation(120.0F, 0.0F);
   private boolean dragging;

   public SliderWidget(ThemeManager themes, SliderSetting setting) {
      super(themes, setting);
      this.setting = setting;
      this.fill.snapTo((float)setting.getNormalized());
   }

   public float height(NVGRenderer vg) {
      return HEIGHT;
   }

   public void render(NVGRenderer vg, float mx, float my) {
      Theme theme = this.theme();
      boolean hovered = this.contains(mx, my) || this.dragging;
      this.hover.setTarget(hovered ? 1.0F : 0.0F);
      float textY = this.floatVal2 + 8.0F;
      vg.text(this.setting.getName(), this.floatVal, textY, 12.0F, hovered ? theme.textPrimary() : theme.textMuted());
      String value = this.setting.formatValue();
      vg.text(value, this.floatVal + this.width - vg.textWidth(value, 11.5F), textY, 11.5F, theme.accent());
      float trackY = this.floatVal2 + HEIGHT - 7.0F - TRACK_H / 2.0F;
      this.fill.setTarget((float)this.setting.getNormalized());
      float t = Math.clamp(this.fill.value(), 0.0F, 1.0F);
      vg.rect(this.floatVal, trackY, this.width, TRACK_H, 1.0F, theme.track());
      vg.rect(this.floatVal, trackY, Math.max(1.0F, t * this.width), TRACK_H, 1.0F, theme.accent());
      float hs = HANDLE + 2.0F * this.hover.value();
      float hx = this.floatVal + t * (this.width - hs);
      vg.rect(hx, trackY + TRACK_H / 2.0F - hs / 2.0F, hs, hs, 2.0F, Colors.lerp(theme.textPrimary(), theme.accentBright(), this.dragging ? 1.0F : 0.0F));
   }

   public boolean mouseClicked(float mx, float my, int button) {
      if (button == 0 && this.contains(mx, my) && !(my < this.floatVal2 + 12.0F)) {
         this.dragging = true;
         this.applyMouse(mx);
         return true;
      } else {
         return false;
      }
   }

   public void mouseDragged(float mx, float my) {
      if (this.dragging) {
         this.applyMouse(mx);
      }
   }

   public void mouseReleased() {
      this.dragging = false;
   }

   private void applyMouse(float mx) {
      double before = this.setting.getNormalized();
      this.setting.setNormalized((double)((mx - this.floatVal) / this.width));
      if (this.setting.getNormalized() != before) {
         UiSounds.sliderTick((float)this.setting.getNormalized());
      }
   }
}
