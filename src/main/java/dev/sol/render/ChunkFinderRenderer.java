package dev.sol.render;

import dev.sol.SolClient;
import dev.sol.module.impl.ChunkFinderModule;
import dev.sol.util.Colors;
import net.minecraft.class_1923;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.minecraft.class_4597;

public final class ChunkFinderRenderer {
   private static final float RENDER_Y = 55.0F;
   private static final int FILL_ALPHA = 190;

   private ChunkFinderRenderer() {
   }

   public static void render(class_4597.class_4598 bufferSource, class_4587 poseStack, class_243 camera, ChunkFinderModule module) {
      if (!module.flaggedChunks().isEmpty()) {
         int accent = SolClient.themes().current().accent();
         int fill = Colors.withAlpha(Colors.darken(accent, 0.58F), 190);
         int outline = Colors.withAlpha(accent, 255);

         for(long key : module.flaggedChunks()) {
            double x0 = (double)class_1923.method_8325(key) * (double)16.0F;
            double z0 = (double)class_1923.method_8332(key) * (double)16.0F;
            double x1 = x0 + (double)16.0F;
            double z1 = z0 + (double)16.0F;
            FlatOverlay.fillQuad(bufferSource, poseStack, camera, x0, z0, x1, z1, (double)55.0F, fill);
            FlatOverlay.edge(bufferSource, poseStack, camera, x0, z0, x1, z0, (double)55.0F, outline, 2.0F);
            FlatOverlay.edge(bufferSource, poseStack, camera, x1, z0, x1, z1, (double)55.0F, outline, 2.0F);
            FlatOverlay.edge(bufferSource, poseStack, camera, x1, z1, x0, z1, (double)55.0F, outline, 2.0F);
            FlatOverlay.edge(bufferSource, poseStack, camera, x0, z1, x0, z0, (double)55.0F, outline, 2.0F);
         }

         FlatOverlay.flush(bufferSource);
      }

   }
}
