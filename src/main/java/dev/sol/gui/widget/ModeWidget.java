package dev.sol.gui.widget;

import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.settings.ModeSetting;
import dev.sol.theme.Theme;
import dev.sol.theme.ThemeManager;
import dev.sol.util.Colors;
import java.util.List;

/**
 * Compact mode selector: label on the left, a "‹ Mode ›" box on the right.
 * Left-click (or the right arrow) goes to the next mode, right-click (or the
 * left arrow) to the previous one.
 */
public class ModeWidget extends SettingWidget {
   private static final float HEIGHT = 20.0F;
   private static final float BOX_H = 16.0F;
   private static final float FONT = 11.5F;
   private ModeSetting setting;
   private float boxX;
   private float boxW;

   public ModeWidget(ThemeManager themes, ModeSetting setting) {
      super(themes, setting);
      this.setting = setting;
   }

   public float height(NVGRenderer vg) {
      return HEIGHT;
   }

   private float maxModeWidth(NVGRenderer vg) {
      float w = 0.0F;
      for(String mode : this.setting.getModes()) {
         w = Math.max(w, vg.textWidth(mode, FONT));
      }

      return w;
   }

   public void render(NVGRenderer vg, float mx, float my) {
      Theme theme = this.theme();
      float cy = this.floatVal2 + HEIGHT / 2.0F;
      boolean hovered = this.contains(mx, my);
      vg.text(this.setting.getName(), this.floatVal, cy, 12.0F, hovered ? theme.textPrimary() : theme.textMuted());
      this.boxW = Math.min(this.width * 0.6F, this.maxModeWidth(vg) + 30.0F);
      this.boxX = this.floatVal + this.width - this.boxW;
      float by = cy - BOX_H / 2.0F;
      boolean boxHover = mx >= this.boxX && mx <= this.boxX + this.boxW && my >= by && my <= by + BOX_H;
      vg.rect(this.boxX, by, this.boxW, BOX_H, 2.0F, Colors.withAlpha(0xFF000000, 0.35F));
      vg.rectOutline(this.boxX, by, this.boxW, BOX_H, 2.0F, 1.0F, boxHover ? theme.accent() : theme.outline());
      int arrow = boxHover ? theme.accent() : theme.textDisabled();
      vg.save();
      vg.translate(this.boxX + 7.0F, cy);
      vg.rotate((float)Math.PI);
      vg.chevron(0.0F, 0.0F, 3.0F, 1.3F, arrow, false);
      vg.restore();
      vg.chevron(this.boxX + this.boxW - 7.0F, cy, 3.0F, 1.3F, arrow, false);
      String current = (String)this.setting.get();
      float tw = vg.textWidth(current, FONT);
      vg.textTruncated(current, this.boxX + Math.max(14.0F, (this.boxW - tw) / 2.0F), cy, FONT, theme.textPrimary(), this.boxW - 28.0F);
   }

   private void step(int dir) {
      List<String> modes = this.setting.getModes();
      if (!modes.isEmpty()) {
         int idx = Math.max(0, modes.indexOf(this.setting.get()));
         idx = Math.floorMod(idx + dir, modes.size());
         this.setting.set((String)modes.get(idx));
      }
   }

   public boolean mouseClicked(float mx, float my, int button) {
      if (!this.contains(mx, my)) {
         return false;
      } else if (button == 1) {
         this.step(-1);
         return true;
      } else if (button == 0) {
         boolean leftArrow = mx >= this.boxX && mx <= this.boxX + 14.0F;
         this.step(leftArrow ? -1 : 1);
         return true;
      } else {
         return false;
      }
   }
}
