package dev.sol.gui;

import dev.sol.SolClient;
import dev.sol.gui.widget.BooleanWidget;
import dev.sol.gui.widget.ColorWidget;
import dev.sol.gui.widget.SettingWidget;
import dev.sol.gui.widget.SliderWidget;
import dev.sol.render.anim.Animation;
import dev.sol.render.nanovg.NVGRenderer;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.ColorSetting;
import dev.sol.settings.Setting;
import dev.sol.settings.SliderSetting;
import dev.sol.theme.SoundSettings;
import dev.sol.theme.Theme;
import dev.sol.theme.ThemeManager;
import dev.sol.util.Colors;
import dev.sol.util.UiSounds;
import java.util.ArrayList;
import java.util.List;

/** Content of the "Themes" tab: accent swatch tiles, custom accent picker and sound options. */
public class ThemesView {
   private static final float TILE_W = 118.0F;
   private static final float TILE_H = 66.0F;
   private static final float GAP = 10.0F;
   private static final float SECTION_H = 30.0F;
   private static final int STARTUP_FIRST_WIDGET = 5;
   private final ThemeManager themes;
   private final ColorSetting accentProxy;
   private final ColorWidget accentWidget;
   private final List<SettingWidget> soundWidgets = new ArrayList();
   private final Animation scroll = new Animation(160.0F, 0.0F);
   private float maxScroll;
   private float x;
   private float y;
   private float w;
   private float h;
   private float tilesTop;
   private int cols = 1;
   private Theme hoveredTile;

   public ThemesView(final ThemeManager themes) {
      this.themes = themes;
      this.accentProxy = new ColorSetting("Custom accent", "Accent color of the selected custom theme", themes.current().accent()) {
         public void set(Integer value) {
            super.set(value);
            Theme current = themes.current();
            if (current.isCustom()) {
               current.setAccent(value | -16777216);
            }

         }
      };
      this.accentWidget = new ColorWidget(themes, this.accentProxy);
      SoundSettings sounds = SolClient.sounds();
      if (sounds != null) {
         for(Setting<?> setting : sounds.all()) {
            if (setting instanceof SliderSetting slider) {
               this.soundWidgets.add(new SliderWidget(themes, slider));
            } else if (setting instanceof BooleanSetting bool) {
               this.soundWidgets.add(new BooleanWidget(themes, bool));
            }
         }
      }

   }

   private Theme theme() {
      return this.themes.current();
   }

   private int tileCount() {
      return this.themes.getThemes().size() + 1;
   }

   private float tilesHeight() {
      int rows = (this.tileCount() + this.cols - 1) / this.cols;
      return (float)rows * (TILE_H + GAP) - GAP;
   }

   private float contentHeight(NVGRenderer vg) {
      float hgt = SECTION_H + this.tilesHeight() + 16.0F;
      if (this.theme().isCustom()) {
         hgt += this.accentWidget.height(vg) + 12.0F;
      }

      hgt += SECTION_H * 2.0F;

      for(SettingWidget widget : this.soundWidgets) {
         hgt += widget.height(vg) + 4.0F;
      }

      return hgt + 12.0F;
   }

   private float section(NVGRenderer vg, String title, float sy) {
      Theme theme = this.theme();
      vg.textBold(title, this.x, sy + 15.0F, 13.0F, theme.textPrimary());
      float lx = this.x + vg.textWidthBold(title, 13.0F) + 10.0F;
      vg.rect(lx, sy + 15.0F, Math.max(0.0F, this.x + this.w - lx), 1.0F, 0.0F, theme.outline());
      return sy + SECTION_H;
   }

   public void render(NVGRenderer vg, float mx, float my, float x, float y, float w, float h) {
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
      Theme active = this.theme();
      this.cols = Math.max(1, (int)((w + GAP) / (TILE_W + GAP)));
      this.maxScroll = Math.max(0.0F, this.contentHeight(vg) - h);
      this.scroll.setTarget(Math.clamp(this.scroll.getTarget(), 0.0F, this.maxScroll));
      boolean inside = mx >= x && mx <= x + w && my >= y && my <= y + h;
      float hmx = inside ? mx : -10000.0F;
      float hmy = inside ? my : -10000.0F;
      vg.save();
      vg.scissor(x - 4.0F, y, w + 8.0F, h);
      float cy = y - this.scroll.value();
      cy = this.section(vg, "Accent", cy);
      this.tilesTop = cy;
      List<Theme> list = this.themes.getThemes();
      Theme newHover = null;

      for(int i = 0; i < this.tileCount(); ++i) {
         float tx = x + (float)(i % this.cols) * (TILE_W + GAP);
         float ty = cy + (float)(i / this.cols) * (TILE_H + GAP);
         boolean hover = hmx >= tx && hmx <= tx + TILE_W && hmy >= ty && hmy <= ty + TILE_H;
         if (i < list.size()) {
            Theme t = (Theme)list.get(i);
            if (hover) {
               newHover = t;
            }

            boolean selected = t == active;
            vg.rect(tx, ty, TILE_W, TILE_H, 6.0F, active.surface());
            vg.rectVaryingGradient(tx, ty, TILE_W, 34.0F, 6.0F, 6.0F, 0.0F, 0.0F, t.accent(), Colors.darken(t.accent(), 0.35F));
            vg.text(t.getName(), tx + 10.0F, ty + 50.0F, 12.5F, selected ? active.textPrimary() : active.textMuted());
            if (selected) {
               vg.rectOutline(tx, ty, TILE_W, TILE_H, 6.0F, 1.5F, active.accent());
               vg.circle(tx + TILE_W - 14.0F, ty + 50.0F, 6.0F, active.accent());
               vg.checkmark(tx + TILE_W - 19.0F, ty + 45.0F, 10.0F, 1.5F, active.onAccent());
            } else {
               vg.rectOutline(tx, ty, TILE_W, TILE_H, 6.0F, 1.0F, hover ? active.textDisabled() : active.outline());
            }

            if (t.isCustom() && hover) {
               vg.rect(tx + TILE_W - 20.0F, ty + 4.0F, 16.0F, 16.0F, 4.0F, Colors.withAlpha(-16777216, 0.4F));
               vg.cross(tx + TILE_W - 18.0F, ty + 6.0F, 12.0F, 1.5F, -1);
            }
         } else {
            vg.rect(tx, ty, TILE_W, TILE_H, 6.0F, Colors.withAlpha(-16777216, 0.2F));
            vg.rectOutline(tx, ty, TILE_W, TILE_H, 6.0F, 1.0F, hover ? active.accent() : active.outline());
            float pcx = tx + TILE_W / 2.0F;
            float pcy = ty + 26.0F;
            int col = hover ? active.accent() : active.textMuted();
            vg.line(pcx - 6.0F, pcy, pcx + 6.0F, pcy, 1.6F, col);
            vg.line(pcx, pcy - 6.0F, pcx, pcy + 6.0F, 1.6F, col);
            String label = "Custom";
            vg.text(label, pcx - vg.textWidth(label, 12.0F) / 2.0F, ty + 50.0F, 12.0F, col);
         }
      }

      if (newHover != this.hoveredTile && newHover != null) {
         UiSounds.hover();
      }

      this.hoveredTile = newHover;
      cy += this.tilesHeight() + 16.0F;
      if (active.isCustom()) {
         this.accentWidget.setBounds(x, cy, Math.min(300.0F, w));
         this.accentWidget.render(vg, hmx, hmy);
         cy += this.accentWidget.height(vg) + 12.0F;
      }

      cy = this.section(vg, "Sounds", cy);
      float colW = Math.min(360.0F, w);

      for(int i = 0; i < this.soundWidgets.size(); ++i) {
         if (i == STARTUP_FIRST_WIDGET) {
            cy = this.section(vg, "Startup sound", cy + 6.0F) - 6.0F;
         }

         SettingWidget widget = (SettingWidget)this.soundWidgets.get(i);
         widget.setBounds(x, cy, colW);
         widget.render(vg, hmx, hmy);
         cy += widget.height(vg) + 4.0F;
      }

      vg.restore();
      if (this.maxScroll > 0.5F) {
         float thumbH = Math.max(20.0F, h * (h / (h + this.maxScroll)));
         float thumbY = y + (h - thumbH) * (this.scroll.value() / this.maxScroll);
         vg.rect(x + w + 6.0F, thumbY, 3.0F, thumbH, 1.5F, Colors.withAlpha(active.accent(), 0.5F));
      }

   }

   public boolean contains(float mx, float my) {
      return mx >= this.x && mx <= this.x + this.w && my >= this.y && my <= this.y + this.h;
   }

   public boolean mouseClicked(float mx, float my, int button) {
      if (!this.contains(mx, my)) {
         this.accentWidget.mouseClicked(-10000.0F, -10000.0F, button);
         return false;
      } else if (this.accentWidget.mouseClicked(mx, my, button)) {
         return true;
      } else {
         for(SettingWidget widget : this.soundWidgets) {
            if (widget.mouseClicked(mx, my, button)) {
               return true;
            }
         }

         if (button != 0) {
            return false;
         } else {
            List<Theme> list = this.themes.getThemes();

            for(int i = 0; i < this.tileCount(); ++i) {
               float tx = this.x + (float)(i % this.cols) * (TILE_W + GAP);
               float ty = this.tilesTop + (float)(i / this.cols) * (TILE_H + GAP);
               if (mx >= tx && mx <= tx + TILE_W && my >= ty && my <= ty + TILE_H) {
                  if (i < list.size()) {
                     Theme t = (Theme)list.get(i);
                     if (t.isCustom() && mx >= tx + TILE_W - 20.0F && my <= ty + 20.0F) {
                        this.themes.removeCustom(t);
                     } else {
                        this.themes.select(t);
                        if (t.isCustom()) {
                           this.accentProxy.set(t.accent());
                        }
                     }
                  } else {
                     Theme custom = this.themes.addCustom(this.themes.current().accent());
                     this.themes.select(custom);
                     this.accentProxy.set(custom.accent());
                     this.accentWidget.setExpanded(true);
                  }

                  UiSounds.select();
                  return true;
               }
            }

            return true;
         }
      }
   }

   public void mouseDragged(float mx, float my) {
      this.accentWidget.mouseDragged(mx, my);

      for(SettingWidget widget : this.soundWidgets) {
         widget.mouseDragged(mx, my);
      }

   }

   public void mouseReleased() {
      this.accentWidget.mouseReleased();

      for(SettingWidget widget : this.soundWidgets) {
         widget.mouseReleased();
      }

   }

   public void onScroll(double amount) {
      if (this.maxScroll > 0.0F) {
         this.scroll.setTarget(Math.clamp(this.scroll.getTarget() - (float)amount * 36.0F, 0.0F, this.maxScroll));
      }

   }

   public boolean keyPressed(int keyCode) {
      return this.accentWidget.keyPressed(keyCode);
   }

   public boolean charTyped(int codepoint) {
      return this.accentWidget.charTyped(codepoint);
   }

   public boolean isListening() {
      return this.accentWidget.isListening();
   }
}
