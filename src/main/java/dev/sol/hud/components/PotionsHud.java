package dev.sol.hud.components;

import dev.sol.hud.HudComponent;
import dev.sol.hud.HudDragController;
import dev.sol.hud.HudStyle;
import dev.sol.render.nanovg.NVGImages;
import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.theme.Theme;
import dev.sol.theme.ThemeManager;
import dev.sol.util.Colors;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1657;
import net.minecraft.class_2960;
import net.minecraft.class_310;

/** Potion effects: one glass panel with an "EFFECTS" header and hairline-separated rows. */
public class PotionsHud extends HudComponent {
   private static final float ROW = 19.0F;
   private static final float ICON = 14.0F;
   private static final float FONT = 11.0F;
   private ThemeManager themes;

   public PotionsHud(ThemeManager themes, BooleanSupplier visible) {
      super("potions", 0.995F, 0.6F, visible);
      this.themes = themes;
   }

   private List<class_1293> effects() {
      class_1657 player = class_310.method_1551().field_1724;
      if (player == null) {
         return List.of();
      } else {
         List<class_1293> list = new ArrayList(player.method_6026());
         list.sort(Comparator.comparingInt(class_1293::method_5584).reversed());
         return list;
      }
   }

   private static String label(class_1293 effect) {
      String name = ((class_1291)effect.method_5579().comp_349()).method_5560().getString();
      int amp = effect.method_5578();
      return amp > 0 ? name + " " + (amp + 1) : name;
   }

   private static String timer(class_1293 effect) {
      if (effect.method_48559()) {
         return "\u221e";
      } else {
         int seconds = effect.method_5584() / 20;
         return String.format("%d:%02d", seconds / 60, seconds % 60);
      }
   }

   public float measureWidth(NVGRenderer vg) {
      List<class_1293> effects = this.effects();
      float max = Math.max(120.0F, HudStyle.headerWidth(vg, "Effects", Integer.toString(effects.size())));

      for(class_1293 effect : effects) {
         max = Math.max(max, HudStyle.PAD + ICON + 6.0F + vg.textWidth(label(effect), FONT) + 14.0F + vg.textWidthBold(timer(effect), FONT) + HudStyle.PAD);
      }

      return max;
   }

   public float measureHeight(NVGRenderer vg) {
      int n = Math.max(1, this.effects().size());
      return HudStyle.RULE + HudStyle.HEADER_H + 4.0F + (float)n * ROW + 3.0F;
   }

   public void render(NVGRenderer vg, float x, float y, float w, float h) {
      Theme theme = this.themes.current();
      List<class_1293> effects = this.effects();
      if (effects.isEmpty() && !HudDragController.isEditing()) {
         return;
      }

      HudStyle.panel(vg, theme, x, y, w, h);
      float rowY = HudStyle.header(vg, theme, "Effects", Integer.toString(effects.size()), x, y, w);
      if (effects.isEmpty()) {
         vg.text("No active effects", x + HudStyle.PAD, rowY + ROW / 2.0F, FONT, theme.textDisabled());
      } else {
         for(int i = 0; i < effects.size(); ++i) {
            class_1293 effect = (class_1293)effects.get(i);
            float cy = rowY + ROW / 2.0F;
            float ix = x + HudStyle.PAD;
            class_2960 effectId = (class_2960)effect.method_5579().method_40230().map((k) -> k.method_29177()).orElse(null);
            if (effectId != null) {
               int image = NVGImages.fromResource(class_2960.method_60655(effectId.method_12836(), "textures/mob_effect/" + effectId.method_12832() + ".png"));
               if (image > 0) {
                  vg.imagePattern(image, ix, cy - ICON / 2.0F, ICON, ICON, ix, cy - ICON / 2.0F, ICON, ICON, 1.0F);
               }
            }

            vg.text(label(effect), ix + ICON + 6.0F, cy, FONT, theme.textPrimary());
            String t = timer(effect);
            boolean ending = !effect.method_48559() && effect.method_5584() < 200;
            vg.textBold(t, x + w - HudStyle.PAD - vg.textWidthBold(t, FONT), cy, FONT, ending ? -495247 : theme.accent());
            if (i < effects.size() - 1) {
               vg.rect(x + HudStyle.PAD, rowY + ROW, w - HudStyle.PAD * 2.0F, 1.0F, 0.0F, Colors.withAlpha(-1, 0.04F));
            }

            rowY += ROW;
         }
      }

   }
}
