package dev.sol.gui.widget;

import dev.sol.render.anim.Animation;
import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.settings.BooleanSetting;
import dev.sol.theme.Theme;
import dev.sol.theme.ThemeManager;
import dev.sol.util.Colors;
import dev.sol.util.UiSounds;

/** Square checkbox on the right, label on the left. */
public class BooleanWidget extends SettingWidget {
   public static final float HEIGHT = 20.0F;
   private static final float BOX = 12.0F;
   private BooleanSetting setting;
   private final Animation check = new Animation(130.0F, 0.0F);

   public BooleanWidget(ThemeManager themes, BooleanSetting setting) {
      super(themes, setting);
      this.setting = setting;
      this.check.snapTo((Boolean)setting.get() ? 1.0F : 0.0F);
   }

   public float height(NVGRenderer vg) {
      return HEIGHT;
   }

   public void render(NVGRenderer vg, float mx, float my) {
      Theme theme = this.theme();
      float cy = this.floatVal2 + HEIGHT / 2.0F;
      boolean hovered = this.contains(mx, my);
      vg.text(this.setting.getName(), this.floatVal, cy, 12.0F, hovered ? theme.textPrimary() : theme.textMuted());
      this.check.setTarget((Boolean)this.setting.get() ? 1.0F : 0.0F);
      float t = this.check.value();
      float bx = this.floatVal + this.width - BOX;
      float by = cy - BOX / 2.0F;
      vg.rect(bx, by, BOX, BOX, 2.0F, Colors.withAlpha(0xFF000000, 0.35F));
      vg.rectOutline(bx, by, BOX, BOX, 2.0F, 1.0F, Colors.lerp(hovered ? theme.textDisabled() : theme.outline(), theme.accent(), t));
      if (t > 0.02F) {
         float inset = 2.0F + 2.0F * (1.0F - t);
         vg.rect(bx + inset, by + inset, BOX - inset * 2.0F, BOX - inset * 2.0F, 1.0F, Colors.withAlpha(theme.accent(), t));
      }
   }

   public boolean mouseClicked(float mx, float my, int button) {
      if (button == 0 && this.contains(mx, my)) {
         this.setting.toggle();
         UiSounds.checkbox((Boolean)this.setting.get());
         return true;
      } else {
         return false;
      }
   }
}
