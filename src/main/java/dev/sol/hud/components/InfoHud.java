package dev.sol.hud.components;

import dev.sol.hud.HudComponent;
import dev.sol.hud.HudStyle;
import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.theme.Theme;
import dev.sol.theme.ThemeManager;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class InfoHud extends HudComponent {
   private static final float HEIGHT = 20.0F;
   private static final float CAPTION = 8.5F;
   private static final float VALUE = 11.5F;
   private static final float GAP = 6.0F;
   private ThemeManager themes;
   private String label;
   private Supplier<String> value;

   public InfoHud(String id, ThemeManager themes, String label, Supplier<String> value, float defaultFx, float defaultFy, BooleanSupplier visible) {
      super(id, defaultFx, defaultFy, visible);
      this.themes = themes;
      this.label = label.toUpperCase(Locale.ROOT);
      this.value = value;
   }

   private String currentValue() {
      try {
         return (String)this.value.get();
      } catch (Exception var2) {
         return "?";
      }
   }

   public float measureWidth(NVGRenderer vg) {
      return HudStyle.PAD + HudStyle.spacedWidth(vg, this.label, CAPTION, true) + GAP + vg.textWidthBold(this.currentValue(), VALUE) + HudStyle.PAD;
   }

   public float measureHeight(NVGRenderer vg) {
      return HEIGHT;
   }

   public void render(NVGRenderer vg, float x, float y, float w, float h) {
      Theme theme = this.themes.current();
      HudStyle.panel(vg, theme, x, y, w, h);
      float cy = y + HudStyle.RULE / 2.0F + h / 2.0F;
      float tx = x + HudStyle.PAD;
      tx += HudStyle.spaced(vg, this.label, tx, cy, CAPTION, theme.accent(), true) + GAP;
      vg.textBold(this.currentValue(), tx, cy, VALUE, theme.textPrimary());
   }
}
