package dev.sol.gametest;

import dev.sol.SolClient;
import dev.sol.module.impl.CustomAccessoriesModule;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.class_5498;

public class AccessoryFirstPersonGameTest implements FabricClientGameTest {
   public void runTest(ClientGameTestContext context) {
      TestSingleplayerContext world = context.worldBuilder().create();

      try {
         world.getClientWorld().waitForChunksRender();
         context.runOnClient((mc) -> mc.field_1690.field_1837 = false);
         context.getInput().resizeWindow(1600, 900);
         context.waitTicks(2);
         world.getServer().runCommand("tp @a 500 -60 500 -90 0");
         world.getClientWorld().waitForChunksRender();
         context.runOnClient((mc) -> {
            SolClient.modules().susChunkFinder.setEnabled(false);
            SolClient.modules().blockOutline.setEnabled(false);
            SolClient.modules().motionBlur.setEnabled(false);
            if (mc.field_1724 != null) {
               mc.field_1724.method_36456(-90.0F);
               mc.field_1724.method_5636(-90.0F);
               mc.field_1724.method_36457(0.0F);
            }

            CustomAccessoriesModule acc = SolClient.modules().customAccessories;
            require(acc != null, "CustomAccessories registered");
            acc.firstPerson.set(false);
            acc.color.set(-26368);
            acc.rainbow.set(false);
            acc.glow.set((double)75.0F);
            acc.cape.set(true);
            acc.capeStyle.set("Sol");
            acc.capePhysics.set(true);
            acc.trail.set(false);
            acc.aura.set(true);
            acc.auraStyle.set("Orbit");
            acc.crown.set(true);
            acc.setEnabled(true);
         });
         context.waitTicks(20);
         context.runOnClient((mc) -> mc.field_1690.method_31043(class_5498.field_26665));
         context.waitTicks(6);
         context.takeScreenshot("accessories-fp-1-thirdperson-control");
         context.runOnClient((mc) -> mc.field_1690.method_31043(class_5498.field_26664));
         context.waitTicks(6);
         context.takeScreenshot("accessories-fp-2-firstperson-hidden");
         context.runOnClient((mc) -> {
            if (mc.field_1724 != null) {
               mc.field_1724.method_36457(60.0F);
            }

         });
         context.waitTicks(6);
         context.takeScreenshot("accessories-fp-3-firstperson-feet-aura-toggleoff");
         context.runOnClient((mc) -> SolClient.modules().customAccessories.firstPerson.set(true));
         context.waitTicks(6);
         context.takeScreenshot("accessories-fp-4-firstperson-feet-toggleon");
         context.runOnClient((mc) -> {
            CustomAccessoriesModule acc = SolClient.modules().customAccessories;
            acc.firstPerson.set(false);
            acc.setEnabled(false);
         });
      } catch (Throwable var6) {
         if (world != null) {
            try {
               world.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (world != null) {
         world.close();
      }

   }

   private static void require(boolean condition, String what) {
      if (!condition) {
         throw new AssertionError("FAILED: null");
      }
   }
}
