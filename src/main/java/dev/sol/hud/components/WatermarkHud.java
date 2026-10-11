package dev.sol.hud.components;

import dev.sol.SolClient;
import dev.sol.gui.ClickGuiScreen;
import dev.sol.hud.HudComponent;
import dev.sol.hud.HudStyle;
import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.theme.Theme;
import dev.sol.theme.ThemeManager;
import dev.sol.util.Colors;
import java.util.function.BooleanSupplier;
import net.minecraft.class_310;

public class WatermarkHud extends HudComponent {
   private static final float HEIGHT = 21.0F;
   private static final float NAME = 10.5F;
   private static final float META = 10.0F;
   private ThemeManager themes;

   public WatermarkHud(ThemeManager themes, BooleanSupplier visible) {
      super("watermark", 0.997F, 0.008F, visible);
      this.themes = themes;
   }

   private static String fps() {
      return class_310.method_1551().method_47599() + " fps";
   }

   public float measureWidth(NVGRenderer vg) {
      return HudStyle.PAD + 16.0F + HudStyle.spacedWidth(vg, "SOL", NAME, true) + 17.0F + vg.textWidth(SolClient.VERSION, META) + 17.0F + vg.textWidth(fps(), META) + HudStyle.PAD;
   }

   public float measureHeight(NVGRenderer vg) {
      return HEIGHT;
   }

   public void render(NVGRenderer vg, float x, float y, float w, float h) {
      Theme theme = this.themes.current();
      HudStyle.panel(vg, theme, x, y, w, h);
      float cy = y + HudStyle.RULE / 2.0F + h / 2.0F;
      float tx = x + HudStyle.PAD;
      ClickGuiScreen.drawSun(vg, tx + 5.0F, cy, 2.6F, theme.accent());
      tx += 16.0F;
      tx += HudStyle.spaced(vg, "SOL", tx, cy, NAME, theme.textPrimary(), true);
      tx += 8.0F;
      vg.rect(tx, cy - 5.0F, 1.0F, 10.0F, 0.0F, Colors.withAlpha(-1, 0.18F));
      tx += 9.0F;
      tx += vg.text(SolClient.VERSION, tx, cy, META, theme.textMuted());
      tx += 8.0F;
      vg.rect(tx, cy - 5.0F, 1.0F, 10.0F, 0.0F, Colors.withAlpha(-1, 0.18F));
      tx += 9.0F;
      vg.text(fps(), tx, cy, META, Colors.withAlpha(theme.textPrimary(), 0.85F));
   }
}
