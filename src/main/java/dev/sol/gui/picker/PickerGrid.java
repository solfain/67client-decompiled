package dev.sol.gui.picker;

import dev.sol.settings.ColorSetting;
import java.util.List;
import net.minecraft.class_1799;

public interface PickerGrid {
   String title();

   long activeCount();

   List<Cell> cells();

   public interface Cell {
      class_1799 icon();

      String label();

      boolean matches(String var1);

      boolean tracked();

      boolean enabled();

      boolean selected();

      int color();

      void toggle();

      ColorSetting colorTarget();
   }
}
