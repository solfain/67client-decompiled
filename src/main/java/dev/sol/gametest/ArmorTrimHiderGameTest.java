package dev.sol.gametest;

import dev.sol.SolClient;
import dev.sol.module.impl.ArmorTrimHiderModule;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_5498;
import net.minecraft.class_8053;

public class ArmorTrimHiderGameTest implements FabricClientGameTest {
   private static final String TRIM = "[minecraft:trim={material:\"minecraft:gold\",pattern:\"minecraft:sentry\"}]";

   public void runTest(ClientGameTestContext context) {
      TestSingleplayerContext world = context.worldBuilder().create();

      try {
         world.getClientWorld().waitForChunksRender();
         context.runOnClient((mc) -> mc.field_1690.field_1837 = false);
         context.getInput().resizeWindow(1280, 720);
         context.waitTicks(2);
         TestServerContext server = world.getServer();
         context.runOnClient((mc) -> require(SolClient.modules().armorTrimHider != null, "ArmorTrimHider registered"));
         server.runCommand("gamemode creative @a");
         server.runCommand("time set day");
         server.runCommand("weather clear");
         server.runCommand("fill -8 0 -8 8 0 8 minecraft:white_concrete");
         server.runCommand("tp @a 0 1 0 0 0");
         server.runCommand("item replace entity @a armor.head with minecraft:diamond_helmet[minecraft:trim={material:\"minecraft:gold\",pattern:\"minecraft:sentry\"}]");
         server.runCommand("item replace entity @a armor.chest with minecraft:diamond_chestplate[minecraft:trim={material:\"minecraft:gold\",pattern:\"minecraft:sentry\"}]");
         server.runCommand("item replace entity @a armor.legs with minecraft:diamond_leggings[minecraft:trim={material:\"minecraft:gold\",pattern:\"minecraft:sentry\"}]");
         server.runCommand("item replace entity @a armor.feet with minecraft:diamond_boots[minecraft:trim={material:\"minecraft:gold\",pattern:\"minecraft:sentry\"}]");
         server.runCommand("summon minecraft:armor_stand 2.5 1 3.5 {ShowArms:1b,NoGravity:1b,Rotation:[180f,0f]}");
         server.runCommand("item replace entity @e[type=armor_stand,limit=1] armor.head with minecraft:diamond_helmet[minecraft:trim={material:\"minecraft:gold\",pattern:\"minecraft:sentry\"}]");
         server.runCommand("item replace entity @e[type=armor_stand,limit=1] armor.chest with minecraft:diamond_chestplate[minecraft:trim={material:\"minecraft:gold\",pattern:\"minecraft:sentry\"}]");
         server.runCommand("item replace entity @e[type=armor_stand,limit=1] armor.legs with minecraft:diamond_leggings[minecraft:trim={material:\"minecraft:gold\",pattern:\"minecraft:sentry\"}]");
         server.runCommand("item replace entity @e[type=armor_stand,limit=1] armor.feet with minecraft:diamond_boots[minecraft:trim={material:\"minecraft:gold\",pattern:\"minecraft:sentry\"}]");
         context.runOnClient((mc) -> {
            mc.field_1690.method_31043(class_5498.field_26665);
            SolClient.modules().blockOutline.setEnabled(false);
            SolClient.modules().armorTrimHider.setEnabled(false);
         });
         context.waitTicks(10);
         context.takeScreenshot("atrim-01-baseline");
         context.runOnClient((mc) -> {
            ArmorTrimHiderModule m = SolClient.modules().armorTrimHider;
            m.mode.set("Hide");
            m.ownArmor.set(true);
            m.setEnabled(true);
            class_1799 chest = new class_1799(class_1802.field_8058);
            class_8053 any = m.mapTrim(chest, (class_8053)null);
            require(m.mapTrim(chest, any) == null, "Hide maps trim to null");
         });
         context.waitTicks(5);
         context.takeScreenshot("atrim-02-hide-all");
         context.runOnClient((mc) -> SolClient.modules().armorTrimHider.ownArmor.set(false));
         context.waitTicks(5);
         context.takeScreenshot("atrim-03-hide-others-only");
         context.runOnClient((mc) -> {
            ArmorTrimHiderModule m = SolClient.modules().armorTrimHider;
            m.mode.set("Random");
            m.ownArmor.set(true);
            class_1799 boots = new class_1799(class_1802.field_8285);
            class_8053 r1 = m.mapTrim(boots, (class_8053)null);
            class_8053 r2 = m.mapTrim(boots, (class_8053)null);
            require(r1 != null, "Random adds a trim to untrimmed armor");
            require(r1.equals(r2), "Random is stable for the same item");
         });
         context.waitTicks(5);
         context.takeScreenshot("atrim-04-random");
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
