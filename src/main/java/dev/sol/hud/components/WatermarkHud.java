package dev.sol.hud.components;

import dev.sol.SolClient;
import dev.sol.gui.ClickGuiScreen;
import dev.sol.hud.HudComponent;
import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.theme.Theme;
import dev.sol.theme.ThemeManager;
import dev.sol.util.Colors;
import java.util.function.BooleanSupplier;

/** Sol watermark: sun mark, "Sol Client" and the version on a flat dark tab with an accent edge. */
public class WatermarkHud extends HudComponent {
   private static final float HEIGHT = 22.0F;
   private static final float PAD = 8.0F;
   private static final float SUN_SPACE = 20.0F;
   private static final float NAME_SIZE = 13.0F;
   private static final float VERSION_SIZE = 10.5F;
   private ThemeManager themes;

   public WatermarkHud(ThemeManager themes, BooleanSupplier visible) {
      super("watermark", 0.006F, 0.01F, visible);
      this.themes = themes;
   }

   private static String version() {
      return "v" + SolClient.VERSION;
   }

   public float measureWidth(NVGRenderer vg) {
      return PAD + SUN_SPACE + vg.textWidth("Sol", NAME_SIZE) + 3.0F + vg.textWidth("Client", NAME_SIZE) + 6.0F + vg.textWidth(version(), VERSION_SIZE) + PAD;
   }

   public float measureHeight(NVGRenderer vg) {
      return HEIGHT;
   }

   public void render(NVGRenderer vg, float x, float y, float w, float h) {
      Theme theme = this.themes.current();
      float cy = y + h / 2.0F;
      vg.glow(x, y, w, h, 3.0F, 5.0F, Colors.withAlpha(0xFF000000, 0.30F));
      vg.rect(x, y, w, h, 3.0F, theme.headerTop());
      vg.rect(x, y, 2.0F, h, 1.0F, theme.accent());
      float tx = x + PAD;
      ClickGuiScreen.drawSun(vg, tx + 7.0F, cy, 3.5F, theme.accent());
      tx += SUN_SPACE;
      tx += vg.text("Sol", tx, cy, NAME_SIZE, theme.accent()) + 3.0F;
      tx += vg.text("Client", tx, cy, NAME_SIZE, theme.textPrimary()) + 6.0F;
      vg.text(version(), tx, cy + 0.5F, VERSION_SIZE, theme.textDisabled());
   }
}
