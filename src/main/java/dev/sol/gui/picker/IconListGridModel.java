package dev.sol.gui.picker;

import dev.sol.settings.ColorSetting;
import dev.sol.settings.IconListSetting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1799;

public final class IconListGridModel implements PickerGrid {
   private IconListSetting setting;
   private List<PickerGrid.Cell> cells;

   public IconListGridModel(IconListSetting setting) {
      this.setting = setting;
   }

   public String title() {
      return this.setting.getName();
   }

   public long activeCount() {
      return this.setting.enabledCount();
   }

   public List<PickerGrid.Cell> cells() {
      if (this.cells == null) {
         List<PickerGrid.Cell> out = new ArrayList(this.setting.entries().size());

         for(IconListSetting.Entry entry : this.setting.entries()) {
            out.add(new EntryCell(entry));
         }

         this.cells = out;
      }

      return this.cells;
   }

   final class EntryCell implements PickerGrid.Cell {
      private IconListSetting.Entry entry;
      private class_1799 icon;

      EntryCell(IconListSetting.Entry entry) {
         this.entry = entry;
         this.icon = new class_1799(entry.icon());
      }

      public class_1799 icon() {
         return this.icon;
      }

      public String label() {
         return this.entry.label();
      }

      public boolean matches(String lowerQuery) {
         return this.entry.matches(lowerQuery);
      }

      public boolean tracked() {
         return true;
      }

      public boolean enabled() {
         return (Boolean)this.entry.enabled.get();
      }

      public boolean selected() {
         return (Boolean)this.entry.enabled.get();
      }

      public int color() {
         return (Integer)this.entry.color.get();
      }

      public void toggle() {
         this.entry.enabled.toggle();
      }

      public ColorSetting colorTarget() {
         return this.entry.color;
      }
   }
}
