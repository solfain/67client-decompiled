package dev.sol.hud.components;

import dev.sol.hud.HudComponent;
import dev.sol.hud.HudStyle;
import dev.sol.render.nanovg.NVGImages;
import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.theme.Theme;
import dev.sol.theme.ThemeManager;
import dev.sol.util.Colors;
import java.util.function.BooleanSupplier;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_7923;

public class ArmorHud extends HudComponent {
   private static final class_1304[] SLOTS;
   private static final float ICON = 18.0F;
   private static final float CELL = 28.0F;
   private static final float PCT = 8.5F;
   private ThemeManager themes;

   public ArmorHud(ThemeManager themes, BooleanSupplier visible) {
      super("armor", 0.004F, 0.36F, visible);
      this.themes = themes;
   }

   public float measureWidth(NVGRenderer vg) {
      return 6.0F + (float)SLOTS.length * CELL + 6.0F - 4.0F;
   }

   public float measureHeight(NVGRenderer vg) {
      return HudStyle.RULE + 5.0F + ICON + 3.0F + 10.0F + 3.0F;
   }

   public void render(NVGRenderer vg, float x, float y, float w, float h) {
      Theme theme = this.themes.current();
      class_1657 player = class_310.method_1551().field_1724;
      if (player != null) {
         HudStyle.panel(vg, theme, x, y, w, h);
         float cx = x + 6.0F;
         float iy = y + HudStyle.RULE + 5.0F;

         for(class_1304 slot : SLOTS) {
            class_1799 stack = player.method_6118(slot);
            float ix = cx + (CELL - 4.0F - ICON) / 2.0F;
            float labelY = iy + ICON + 3.0F + 5.0F;
            if (stack.method_7960()) {
               vg.rectOutline(ix + 2.0F, iy + 2.0F, ICON - 4.0F, ICON - 4.0F, 1.0F, 1.0F, Colors.withAlpha(-1, 0.12F));
               String dash = "-";
               vg.text(dash, cx + (CELL - 4.0F - vg.textWidth(dash, PCT)) / 2.0F, labelY, PCT, theme.textDisabled());
            } else {
               class_2960 itemId = class_7923.field_41178.method_10221(stack.method_7909());
               int image = NVGImages.fromResource(class_2960.method_60655(itemId.method_12836(), "textures/item/" + itemId.method_12832() + ".png"));
               if (image > 0) {
                  vg.imagePattern(image, ix, iy, ICON, ICON, ix, iy, ICON, ICON, 1.0F);
               } else {
                  vg.rect(ix, iy, ICON, ICON, 1.0F, Colors.withAlpha(theme.accent(), 0.3F));
               }

               String label;
               int color;
               if (stack.method_7963()) {
                  float frac = 1.0F - (float)stack.method_7919() / (float)stack.method_7936();
                  label = Math.round(frac * 100.0F) + "%";
                  color = Colors.lerp(-1684147, -11671924, frac);
               } else {
                  label = "\u221e";
                  color = theme.textMuted();
               }

               vg.textBold(label, cx + (CELL - 4.0F - vg.textWidthBold(label, PCT)) / 2.0F, labelY, PCT, color);
            }

            cx += CELL;
         }
      }

   }

   static {
      SLOTS = new class_1304[]{class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166};
   }
}
