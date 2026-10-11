package dev.sol.hud.components;

import dev.sol.hud.HudComponent;
import dev.sol.hud.HudStyle;
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
   private static final float KEY = 25.0F;
   private static final float GAP = 3.0F;
   private static final float FONT = 11.5F;
   private static final float SPACE_H = 13.0F;
   private ThemeManager themes;
   private final Map<String, Animation> press = new HashMap();

   public KeystrokesHud(ThemeManager themes, BooleanSupplier visible) {
      super("keystrokes", 0.03F, 0.72F, visible);
      this.themes = themes;
   }

   public float measureWidth(NVGRenderer vg) {
      return KEY * 3.0F + GAP * 2.0F;
   }

   public float measureHeight(NVGRenderer vg) {
      return (KEY + GAP) * 3.0F + SPACE_H;
   }

   private float pressT(String id, boolean down) {
      Animation anim = (Animation)this.press.computeIfAbsent(id, (k) -> new Animation(90.0F, 0.0F));
      anim.setTarget(down ? 1.0F : 0.0F);
      return anim.value();
   }

   private void keyBox(NVGRenderer vg, Theme theme, float t, float x, float y, float w, float h) {
      vg.rect(x, y, w, h, HudStyle.RADIUS, HudStyle.panelColor());
      if (t > 0.01F) {
         vg.rect(x, y, w, h, HudStyle.RADIUS, Colors.withAlpha(theme.accent(), 0.92F * t));
      }

      vg.rectOutline(x, y, w, h, HudStyle.RADIUS, 1.0F, Colors.lerp(Colors.withAlpha(-1, 0.10F), theme.accent(), t));
      vg.rect(x + 4.0F, y + h - 3.0F, (w - 8.0F) * t, 1.0F, 0.0F, Colors.withAlpha(theme.onAccent(), 0.6F * t));
   }

   private void key(NVGRenderer vg, Theme theme, String id, String label, boolean down, float x, float y, float w, float h) {
      float t = this.pressT(id, down);
      this.keyBox(vg, theme, t, x, y, w, h);
      int fg = Colors.lerp(theme.textPrimary(), theme.onAccent(), t);
      vg.textBold(label, x + (w - vg.textWidthBold(label, FONT)) / 2.0F, y + h / 2.0F, FONT, fg);
   }

   private void mouseKey(NVGRenderer vg, Theme theme, String id, String label, int cps, boolean down, float x, float y, float w, float h) {
      float t = this.pressT(id, down);
      this.keyBox(vg, theme, t, x, y, w, h);
      int fg = Colors.lerp(theme.textPrimary(), theme.onAccent(), t);
      int sub = Colors.lerp(theme.textMuted(), Colors.withAlpha(theme.onAccent(), 0.75F), t);
      vg.textBold(label, x + (w - vg.textWidthBold(label, 9.5F)) / 2.0F, y + h / 2.0F - 4.5F, 9.5F, fg);
      String c = cps + " cps";
      vg.text(c, x + (w - vg.textWidth(c, 8.0F)) / 2.0F, y + h / 2.0F + 5.5F, 8.0F, sub);
   }

   public void render(NVGRenderer vg, float x, float y, float w, float h) {
      Theme theme = this.themes.current();
      class_315 options = class_310.method_1551().field_1690;
      float col2 = x + KEY + GAP;
      float col3 = x + (KEY + GAP) * 2.0F;
      this.key(vg, theme, "w", "W", isDown(options.field_1894), col2, y, KEY, KEY);
      float row = y + KEY + GAP;
      this.key(vg, theme, "a", "A", isDown(options.field_1913), x, row, KEY, KEY);
      this.key(vg, theme, "s", "S", isDown(options.field_1881), col2, row, KEY, KEY);
      this.key(vg, theme, "d", "D", isDown(options.field_1849), col3, row, KEY, KEY);
      row += KEY + GAP;
      float half = (w - GAP) / 2.0F;
      this.mouseKey(vg, theme, "lmb", "LMB", CpsTracker.get(0), isDown(options.field_1886), x, row, half, KEY);
      this.mouseKey(vg, theme, "rmb", "RMB", CpsTracker.get(1), isDown(options.field_1904), x + half + GAP, row, half, KEY);
      row += KEY + GAP;
      float t = this.pressT("space", isDown(options.field_1903));
      this.keyBox(vg, theme, t, x, row, w, SPACE_H);
      vg.rect(x + w * 0.3F, row + SPACE_H / 2.0F - 0.75F, w * 0.4F, 1.5F, 0.0F, Colors.lerp(theme.textMuted(), theme.onAccent(), t));
   }

   private static boolean isDown(class_304 mapping) {
      return mapping.method_1434();
   }
}
