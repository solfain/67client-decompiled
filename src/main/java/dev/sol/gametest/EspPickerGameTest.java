package dev.sol.gametest;

import dev.sol.SolClient;
import dev.sol.gui.ClickGuiScreen;
import dev.sol.gui.IconPickerScreen;
import dev.sol.module.ModuleManager;
import dev.sol.module.impl.BlockEntityEspModule;
import dev.sol.module.impl.MobEspModule;
import dev.sol.render.BlockEspRenderer;
import dev.sol.render.StorageEspRenderer;
import dev.sol.settings.IconListSetting;
import java.util.function.Function;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.class_310;
import net.minecraft.class_437;

public class EspPickerGameTest implements FabricClientGameTest {
   public void runTest(ClientGameTestContext context) {
      TestSingleplayerContext world = context.worldBuilder().create();

      try {
         world.getClientWorld().waitForChunksRender();
         context.runOnClient((mc) -> mc.field_1690.field_1837 = false);
         context.getInput().resizeWindow(1600, 900);
         context.waitTicks(2);
         TestServerContext server = world.getServer();
         context.runOnClient((mc) -> {
            ModuleManager m = SolClient.modules();
            require(m.blockEntityEsp != null, "BlockEntityESP registered");
            require(m.storageEsp != null, "StorageESP registered");
            require(m.blockEntityEsp.blockEntities.size() > 15, "BlockEntityESP seeded its type list");
            require(m.storageEsp.containers.size() == 8, "StorageESP seeded its 8 container groups");
         });
         server.runCommand("fill -8 0 -2 8 0 12 minecraft:stone");
         server.runCommand("setblock 0 1 5 minecraft:chest");
         server.runCommand("setblock 2 1 5 minecraft:trapped_chest");
         server.runCommand("setblock 4 1 5 minecraft:ender_chest");
         server.runCommand("setblock -2 1 5 minecraft:barrel");
         server.runCommand("setblock -4 1 5 minecraft:shulker_box");
         server.runCommand("setblock 6 1 5 minecraft:furnace");
         server.runCommand("setblock -6 1 5 minecraft:hopper");
         server.runCommand("setblock 0 1 8 minecraft:spawner");
         server.runCommand("setblock 2 1 8 minecraft:oak_sign");
         server.runCommand("summon minecraft:zombie 3 1 8 {NoAI:1b}");
         server.runCommand("tp @a 0 3 -2 0 18");
         context.waitTicks(20);
         context.runOnClient((mc) -> {
            BlockEntityEspModule be = SolClient.modules().blockEntityEsp;
            be.mode.set("Full");
            be.setEnabled(false);
            be.setEnabled(true);
         });
         context.waitTicks(6);
         context.runOnClient((mc) -> {
            int n = SolClient.modules().blockEntityEsp.cachedCount();
            require(n >= 6, "BlockEntityESP cached block entities on enable, got " + n);
         });
         context.takeScreenshot("be-esp-boxes");
         context.runOnClient((mc) -> SolClient.modules().blockEntityEsp.tracers.set(true));
         context.waitTicks(3);
         context.takeScreenshot("be-esp-tracers");
         server.runCommand("setblock 6 1 8 minecraft:diamond_ore");
         context.runOnClient((mc) -> {
            ModuleManager m = SolClient.modules();
            m.storageEsp.setEnabled(false);
            m.storageEsp.setEnabled(true);
            m.blockEsp.setEnabled(false);
            m.blockEsp.setEnabled(true);
         });
         context.waitTicks(30);
         context.runOnClient((mc) -> {
            int s = StorageEspRenderer.cachedCount();
            int b = BlockEspRenderer.cachedCount();
            require(s >= 6, "StorageESP incremental scan found containers, got " + s);
            require(b >= 1, "BlockESP incremental scan found the diamond ore, got " + b);
         });
         context.takeScreenshot("storage-blockesp-incremental");
         context.runOnClient((mc) -> {
            SolClient.modules().storageEsp.setEnabled(false);
            SolClient.modules().blockEsp.setEnabled(false);
         });
         context.runOnClient((mc) -> {
            SolClient.modules().blockEntityEsp.clear();
            require(SolClient.modules().blockEntityEsp.cachedCount() == 0, "cache cleared");
         });
         server.runCommand("tp @a 3000 -60 3000");
         context.waitTicks(40);
         server.runCommand("tp @a 0 3 -2 0 18");
         context.waitTicks(50);
         context.runOnClient((mc) -> {
            int n = SolClient.modules().blockEntityEsp.cachedCount();
            require(n >= 6, "raw chunk packet refilled BlockEntityESP cache, got " + n);
         });
         context.takeScreenshot("be-esp-after-reload");
         context.runOnClient((mc) -> {
            MobEspModule mob = SolClient.modules().mobEsp;
            mob.tracers.set(true);
            mob.setEnabled(true);
         });
         context.waitTicks(3);
         context.takeScreenshot("mob-esp-tracer");
         context.runOnClient((mc) -> SolClient.modules().mobEsp.setEnabled(false));
         context.runOnClient((mc) -> {
            ClickGuiScreen.state().setExpanded("BlockEntityESP@RENDER", true);
            mc.method_1507(new ClickGuiScreen());
         });
         context.waitTicks(3);
         context.takeScreenshot("be-esp-settings");
         openPicker(context, (mc) -> SolClient.modules().blockEntityEsp.blockEntities);
         context.takeScreenshot("be-esp-picker-grid");
         context.runOnClient((mc) -> {
            class_437 patt0$temp = mc.field_1755;
            if (patt0$temp instanceof IconPickerScreen p) {
               p.debugSetSearch("chest");
            }

         });
         context.waitTicks(4);
         context.takeScreenshot("be-esp-picker-search");
         context.runOnClient((mc) -> {
            class_437 patt0$temp = mc.field_1755;
            if (patt0$temp instanceof IconPickerScreen p) {
               p.debugSetSearch("");
               p.debugSetSelectedOnly(true);
               require(p.debugFilteredCount() > 0 && p.debugFilteredCount() < 20, "Selected-only filter narrowed the grid, showing " + p.debugFilteredCount());
            }

         });
         context.waitTicks(4);
         context.takeScreenshot("be-esp-picker-selected-only");
         context.runOnClient((mc) -> {
            class_437 patt0$temp = mc.field_1755;
            if (patt0$temp instanceof IconPickerScreen p) {
               p.debugOpenColor(0);
            }

         });
         context.waitTicks(4);
         context.takeScreenshot("be-esp-picker-color");
         context.runOnClient((mc) -> mc.method_1507(new ClickGuiScreen()));
         context.waitTicks(2);
         openPicker(context, (mc) -> SolClient.modules().storageEsp.containers);
         context.takeScreenshot("storage-esp-picker-grid");
         context.runOnClient((mc) -> {
            class_437 patt0$temp = mc.field_1755;
            if (patt0$temp instanceof IconPickerScreen p) {
               p.debugSetSelectedOnly(true);
            }

         });
         context.waitTicks(4);
         context.takeScreenshot("storage-esp-picker-selected-only");
         context.runOnClient((mc) -> mc.method_1507((class_437)null));
         context.waitTicks(2);
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

   private static void openPicker(ClientGameTestContext context, Function<class_310, IconListSetting> pick) {
      context.runOnClient((mc) -> {
         class_437 patt0$temp = mc.field_1755;
         if (patt0$temp instanceof ClickGuiScreen cg) {
            cg.openIconPicker((IconListSetting)pick.apply(mc));
         }

      });
      context.waitForScreen(IconPickerScreen.class);
      context.waitTicks(6);
   }

   private static void require(boolean condition, String what) {
      if (!condition) {
         throw new AssertionError("FAILED: null");
      }
   }
}
