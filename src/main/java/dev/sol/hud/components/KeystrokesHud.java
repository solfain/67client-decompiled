package dev.sol.hud.components;

import dev.sol.hud.HudComponent;
import dev.sol.render.anim.Animation;
import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.theme.Theme;
import dev.sol.theme.ThemeManager;
import dev.sol.util.Colors;
import dev.sol.util.CpsTracker;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_315;

public class KeystrokesHud extends HudComponent {
   private static final float KEY = 26.0F;
   private static final float GAP = 3.0F;
   private static final float FONT = 12.5F;
   private ThemeManager themes;
   private final Map<String, Animation> press = new HashMap();

   public KeystrokesHud(ThemeManager themes, BooleanSupplier visible) {
      super("keystrokes", 0.03F, 0.72F, visible);
      this.themes = themes;
   }

   public float measureWidth(NVGRenderer vg) {
      return 84.0F;
   }

   public float measureHeight(NVGRenderer vg) {
      float h = 55.0F;
      h += 29.0F;
      return h + 18.6F;
   }

   private float pressT(String id, boolean down) {
      Animation anim = (Animation)this.press.computeIfAbsent(id, (k) -> new Animation(110.0F, 0.0F));
      anim.setTarget(down ? 1.0F : 0.0F);
      return anim.value();
   }

   private void key(NVGRenderer vg, Theme theme, String id, String label, boolean down, float x, float y, float w, float h) {
      float t = this.pressT(id, down);
      int bg = Colors.lerp(Colors.withAlpha(-15462118, 0.78F), Colors.withAlpha(theme.accent(), 0.85F), t);
      vg.rect(x, y, w, h, 6.0F, bg);
      int fg = Colors.lerp(theme.textMuted(), -1, t);
      vg.text(label, x + (w - vg.textWidth(label, 12.5F)) / 2.0F, y + h / 2.0F, 12.5F, fg);
   }

   public void render(NVGRenderer vg, float x, float y, float w, float h) {
      Theme theme = this.themes.current();
      class_315 options = class_310.method_1551().field_1690;
      this.key(vg, theme, "w", "W", isDown(options.field_1894), x + 26.0F + 3.0F, y, 26.0F, 26.0F);
      float row = y + 29.0F;
      this.key(vg, theme, "a", "A", isDown(options.field_1913), x, row, 26.0F, 26.0F);
      this.key(vg, theme, "s", "S", isDown(options.field_1881), x + 26.0F + 3.0F, row, 26.0F, 26.0F);
      this.key(vg, theme, "d", "D", isDown(options.field_1849), x + 58.0F, row, 26.0F, 26.0F);
      row += 29.0F;
      float half = (w - 3.0F) / 2.0F;
      this.key(vg, theme, "lmb", "LMB " + CpsTracker.get(0), isDown(options.field_1886), x, row, half, 26.0F);
      this.key(vg, theme, "rmb", "RMB " + CpsTracker.get(1), isDown(options.field_1904), x + half + 3.0F, row, half, 26.0F);
      row += 29.0F;
      half = this.pressT("space", isDown(options.field_1903));
      int bg = Colors.lerp(Colors.withAlpha(-15462118, 0.78F), Colors.withAlpha(theme.accent(), 0.85F), half);
      vg.rect(x, row, w, 15.6F, 6.0F, bg);
      vg.rect(x + w * 0.25F, row + 7.8F - 1.25F, w * 0.5F, 2.5F, 1.25F, Colors.lerp(theme.textMuted(), -1, half));
   }

   private static boolean isDown(class_304 mapping) {
      return mapping.method_1434();
   }
}
