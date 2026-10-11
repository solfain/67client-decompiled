package dev.sol.render;

import dev.sol.SolClient;
import dev.sol.module.impl.SpawnerNametagsModule;
import dev.sol.util.Colors;
import java.util.List;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import org.joml.Vector3fc;

public final class MiscBlockEspRenderer {
   private static final double SPAWNER_RANGE = (double)16.0F;
   private static final float TRACER_WIDTH = 1.2F;

   private MiscBlockEspRenderer() {
   }

   public static void renderSpawners(class_4597.class_4598 bufferSource, class_4587 poseStack, class_243 cam, SpawnerNametagsModule module) {
      List<class_2338> snapshot = module.scan.get();
      if (!snapshot.isEmpty()) {
         boolean ring = (Boolean)module.rangeRing.get();
         boolean box = (Boolean)module.box.get();
         boolean tracers = (Boolean)module.tracers.get();
         if (ring || box || tracers) {
            int spawnerColor = SolClient.themes().current().accent() | -16777216;
            Vector3fc forward = tracers ? class_310.method_1551().field_1773.method_19418().method_19335() : null;

            for(class_2338 pos : snapshot) {
               if (box) {
                  EspBoxRenderer.outline(bufferSource, poseStack, cam, (double)pos.method_10263(), (double)pos.method_10264(), (double)pos.method_10260(), (double)(pos.method_10263() + 1), (double)(pos.method_10264() + 1), (double)(pos.method_10260() + 1), spawnerColor, 2.0F);
               }

               if (ring) {
                  EspBoxRenderer.ring(bufferSource, poseStack, cam, (double)pos.method_10263() + (double)0.5F, (double)pos.method_10264() + (double)0.5F, (double)pos.method_10260() + (double)0.5F, (double)16.0F, 48, Colors.withAlpha(spawnerColor, 0.6F), 2.0F);
               }

               if (tracers) {
                  EspBoxRenderer.tracer(bufferSource, poseStack, cam, forward, (double)pos.method_10263() + (double)0.5F, (double)pos.method_10264() + (double)0.5F, (double)pos.method_10260() + (double)0.5F, Colors.withAlpha(spawnerColor, 0.7F), 1.2F);
               }
            }

            EspBoxRenderer.flush(bufferSource);
         }
      }

   }
}
