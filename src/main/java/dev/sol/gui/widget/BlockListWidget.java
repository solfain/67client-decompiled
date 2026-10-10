package dev.sol.gui.widget;

import dev.sol.gui.ClickGuiScreen;
import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.settings.BlockListSetting;
import dev.sol.theme.Theme;
import dev.sol.theme.ThemeManager;
import dev.sol.util.Colors;
import dev.sol.util.UiSounds;
import net.minecraft.class_310;
import net.minecraft.class_437;

public class BlockListWidget extends SettingWidget {
   private static final float ROW = 26.0F;
   private BlockListSetting setting;

   public BlockListWidget(ThemeManager themes, BlockListSetting setting) {
      super(themes, setting);
      this.setting = setting;
   }

   public float height(NVGRenderer vg) {
      return 22.0F;
   }

   public void render(NVGRenderer vg, float mx, float my) {
      Theme theme = this.theme();
      float cy = this.floatVal2 + 11.0F;
      vg.text(this.setting.getName(), this.floatVal, cy, 12.0F, theme.textMuted());
      float bx = this.floatVal + this.width - 70.0F;
      float by = cy - 8.0F;
      boolean hovered = mx >= bx && mx <= bx + 70.0F && my >= by && my <= by + 16.0F;
      vg.rect(bx, by, 70.0F, 16.0F, 2.0F, hovered ? Colors.withAlpha(theme.accent(), 0.18F) : Colors.withAlpha(-16777216, 0.35F));
      vg.rectOutline(bx, by, 70.0F, 16.0F, 2.0F, 1.0F, hovered ? theme.accent() : theme.outline());
      float tw = vg.textWidth("Pick Block", 11.0F);
      vg.text("Pick Block", bx + (70.0F - tw) / 2.0F, by + 8.0F, 11.0F, hovered ? theme.accent() : theme.textPrimary());
      long var10000 = this.setting.enabledCount();
      String count = var10000 + "/" + this.setting.size();
      vg.text(count, bx - 8.0F - vg.textWidth(count, 11.5F), cy, 11.5F, theme.textDisabled());
   }

   public boolean mouseClicked(float mx, float my, int button) {
      if (button == 0 && this.contains(mx, my)) {
         class_437 var5 = class_310.method_1551().field_1755;
         if (var5 instanceof ClickGuiScreen) {
            ClickGuiScreen gui = (ClickGuiScreen)var5;
            gui.openBlockPicker(this.setting);
            UiSounds.select();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
