package dev.sol.gui.panel;

import dev.sol.SolClient;
import dev.sol.gui.ClickGuiState;
import dev.sol.gui.widget.BooleanWidget;
import dev.sol.gui.widget.ColorWidget;
import dev.sol.gui.widget.SettingWidget;
import dev.sol.gui.widget.SliderWidget;
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
import java.util.Locale;

public class ThemesPanel extends Panel {
   private static final float ROW_H = 26.0F;
   private static final float ADD_ROW_H = 28.0F;
   private static final float SECTION_H = 24.0F;
   private ColorWidget accentWidget;
   private ColorSetting accentProxy;
   private final List<SettingWidget> soundWidgets = new ArrayList();
   private static final int STARTUP_FIRST_WIDGET = 5;
   private int hoveredRow = -1;
   private float lastStartY = Float.MIN_VALUE;

   public ThemesPanel(final ThemeManager themes, ClickGuiState state) {
      super(themes, state.panel("__themes__"));
      this.accentProxy = new ColorSetting("Accent", "Custom theme accent color", themes.current().accent()) {
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
            if (setting instanceof SliderSetting) {
               SliderSetting slider = (SliderSetting)setting;
               this.soundWidgets.add(new SliderWidget(themes, slider));
            } else if (setting instanceof BooleanSetting) {
               BooleanSetting bool = (BooleanSetting)setting;
               this.soundWidgets.add(new BooleanWidget(themes, bool));
            }
         }
      }

   }

   protected String title() {
      return "Themes";
   }

   protected float contentHeight(NVGRenderer vg) {
      float h = 12.0F + (float)this.themes.getThemes().size() * 26.0F + 28.0F;
      if (this.themes.current().isCustom()) {
         h += this.accentWidget.height(vg) + 6.0F;
      }

      h += 48.0F;

      for(SettingWidget widget : this.soundWidgets) {
         h += widget.height(vg) + 3.0F;
      }

      return h;
   }

   protected void renderContent(NVGRenderer vg, float startY, float mx, float my, float viewTop, float viewBottom) {
      this.lastStartY = startY;
      Theme active = this.themes.current();
      float rowY = startY;
      int rowIndex = 0;
      int newHoveredRow = -1;

      for(Theme theme : this.themes.getThemes()) {
         float fade = this.edgeFade(rowY, rowY + 26.0F, viewTop, viewBottom);
         vg.save();
         vg.alpha(fade);
         boolean selected = theme == active;
         boolean hovered = my >= rowY && my <= rowY + 26.0F && mx >= this.clickGuiStatePanelState.floatVal + 6.0F && mx <= this.clickGuiStatePanelState.floatVal + WIDTH - 6.0F;
         if (hovered) {
            newHoveredRow = rowIndex;
         }

         ++rowIndex;
         if (selected || hovered) {
            vg.rect(this.clickGuiStatePanelState.floatVal, rowY, WIDTH, 26.0F, 0.0F, selected ? this.theme().moduleActiveFill() : this.theme().rowHover());
            if (selected) {
               vg.rect(this.clickGuiStatePanelState.floatVal, rowY + 3.0F, 2.0F, 20.0F, 1.0F, this.theme().accent());
            }
         }

         float sy = rowY + 13.0F;
         vg.rect(this.clickGuiStatePanelState.floatVal + 14.0F, sy - 6.0F, 12.0F, 12.0F, 2.0F, theme.accent());
         if (selected) {
            vg.rectOutline(this.clickGuiStatePanelState.floatVal + 12.0F, sy - 8.0F, 16.0F, 16.0F, 3.0F, 1.2F, this.theme().textPrimary());
         }

         vg.text(theme.getName(), this.clickGuiStatePanelState.floatVal + 36.0F, sy, 12.5F, selected ? this.theme().textPrimary() : this.theme().textMuted());
         if (theme.isCustom()) {
            vg.cross(this.clickGuiStatePanelState.floatVal + WIDTH - 28.0F, sy - 6.0F, 12.0F, 1.6F, this.theme().textDisabled());
         }

         vg.restore();
         rowY += 26.0F;
      }

      if (active.isCustom()) {
         this.accentWidget.setBounds(this.clickGuiStatePanelState.floatVal + 10.0F, rowY + 3.0F, (WIDTH - 20.0F));
         this.accentWidget.render(vg, mx, my);
         rowY += this.accentWidget.height(vg) + 6.0F;
      }

      float fadex = this.edgeFade(rowY, rowY + 28.0F, viewTop, viewBottom);
      vg.save();
      vg.alpha(fadex);
      boolean hoveredx = my >= rowY && my <= rowY + 28.0F - 4.0F && mx >= this.clickGuiStatePanelState.floatVal + 6.0F && mx <= this.clickGuiStatePanelState.floatVal + WIDTH - 6.0F;
      vg.rectOutline(this.clickGuiStatePanelState.floatVal + 8.0F, rowY + 2.0F, WIDTH - 16.0F, 20.0F, 2.0F, 1.0F, hoveredx ? this.theme().accent() : this.theme().outline());
      String label = "+ Custom accent";
      vg.text(label, this.clickGuiStatePanelState.floatVal + (WIDTH - vg.textWidth(label, 12.0F)) / 2.0F, rowY + 12.0F, 12.0F, hoveredx ? this.theme().accent() : this.theme().textMuted());
      vg.restore();
      if (hoveredx) {
         newHoveredRow = 999;
      }

      rowY += 28.0F;
      rowY = this.sectionHeader(vg, "Sounds", rowY, viewTop, viewBottom);

      for(int i = 0; i < this.soundWidgets.size(); ++i) {
         if (i == 5) {
            rowY = this.sectionHeader(vg, "Startup Sound", rowY, viewTop, viewBottom);
         }

         SettingWidget widget = (SettingWidget)this.soundWidgets.get(i);
         widget.setBounds(this.clickGuiStatePanelState.floatVal + 10.0F, rowY, (WIDTH - 20.0F));
         float wFade = this.edgeFade(rowY, rowY + widget.height(vg), viewTop, viewBottom);
         vg.save();
         vg.alpha(wFade);
         widget.render(vg, mx, my);
         vg.restore();
         rowY += widget.height(vg) + 3.0F;
      }

      if (newHoveredRow != this.hoveredRow && newHoveredRow != -1) {
         UiSounds.hover();
      }

      this.hoveredRow = newHoveredRow;
   }

   private float sectionHeader(NVGRenderer vg, String title, float rowY, float viewTop, float viewBottom) {
      float fade = this.edgeFade(rowY, rowY + 24.0F, viewTop, viewBottom);
      vg.save();
      vg.alpha(fade);
      float cy = rowY + 12.0F + 3.0F;
      vg.text(title.toUpperCase(Locale.ROOT), this.clickGuiStatePanelState.floatVal + 10.0F, cy, 10.5F, this.theme().accent());
      float lineX = this.clickGuiStatePanelState.floatVal + 10.0F + vg.textWidth(title.toUpperCase(Locale.ROOT), 10.5F) + 6.0F;
      vg.rect(lineX, cy - 0.5F, Math.max(0.0F, this.clickGuiStatePanelState.floatVal + WIDTH - 14.0F - lineX), 1.0F, 0.0F, this.theme().outline());
      vg.restore();
      return rowY + 24.0F;
   }

   public boolean mouseClicked(float mx, float my, int button) {
      if (this.accentWidget.mouseClicked(mx, my, button)) {
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
            float rowY = this.firstRowY();
            if (rowY == Float.MIN_VALUE) {
               return false;
            } else {
               for(Theme theme : this.themes.getThemes()) {
                  if (my >= rowY && my <= rowY + 26.0F && mx >= this.clickGuiStatePanelState.floatVal + 6.0F && mx <= this.clickGuiStatePanelState.floatVal + WIDTH - 6.0F) {
                     if (theme.isCustom() && mx >= this.clickGuiStatePanelState.floatVal + WIDTH - 34.0F) {
                        this.themes.removeCustom(theme);
                     } else {
                        this.themes.select(theme);
                        if (theme.isCustom()) {
                           this.accentProxy.set(theme.accent());
                        }
                     }

                     return true;
                  }

                  rowY += 26.0F;
               }

               if (this.themes.current().isCustom()) {
                  rowY += this.accentWidget.height((NVGRenderer)null) + 6.0F;
               }

               if (my >= rowY && my <= rowY + 28.0F - 4.0F && mx >= this.clickGuiStatePanelState.floatVal + 6.0F && mx <= this.clickGuiStatePanelState.floatVal + WIDTH - 6.0F) {
                  Theme custom = this.themes.addCustom(this.themes.current().accent());
                  this.themes.select(custom);
                  this.accentProxy.set(custom.accent());
                  return true;
               } else {
                  return false;
               }
            }
         }
      }
   }

   private float firstRowY() {
      return this.lastStartY;
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

   public boolean keyPressed(int keyCode) {
      return this.accentWidget.keyPressed(keyCode);
   }

   public boolean isListening() {
      return this.accentWidget.isListening();
   }
}
