package dev.sol.gametest;

import dev.sol.SolClient;
import dev.sol.gui.ClickGuiScreen;
import dev.sol.gui.widget.StringWidget;
import dev.sol.module.impl.FakePayModule;
import dev.sol.settings.StringSetting;
import java.util.Objects;
import java.util.stream.IntStream;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.class_437;

public class StringInputGameTest implements FabricClientGameTest {
   private static final String EURO = codePoint(8364);
   private static final String POUND = codePoint(163);
   private static final String YEN = codePoint(165);

   public void runTest(ClientGameTestContext context) {
      TestSingleplayerContext world = context.worldBuilder().create();

      try {
         world.getClientWorld().waitForChunksRender();
         context.runOnClient((mc) -> mc.field_1690.field_1837 = false);
         context.getInput().resizeWindow(1600, 900);
         context.waitTicks(2);
         context.runOnClient((mc) -> {
            FakePayModule fp = SolClient.modules().fakePay;
            require(fp != null, "FakePay registered");
            StringSetting currency = fp.currency;
            StringWidget widget = new StringWidget(SolClient.themes(), currency);
            widget.setBounds(0.0F, 0.0F, 200.0F);
            require(widget.mouseClicked(5.0F, 5.0F, 0), "left click focuses the field");
            require(widget.isListening(), "focused field listens for typing");
            currency.set("");
            type(widget, EURO);
            boolean var10000 = ((String)currency.get()).equals(EURO);
            String var10001 = (String)currency.get();
            require(var10000, "euro sign typed, got '" + var10001 + "'");
            currency.set("");
            type(widget, "nullnull");
            var10000 = ((String)currency.get()).equals("nullnull");
            var10001 = (String)currency.get();
            require(var10000, "pound+yen typed, got '" + var10001 + "'");
            currency.set("$");
            widget.keyPressed(259);
            require(((String)currency.get()).isEmpty(), "backspace cleared the '$'");
            type(widget, "$");
            var10000 = ((String)currency.get()).equals("$");
            var10001 = (String)currency.get();
            require(var10000, "'$' retyped after clearing, got '" + var10001 + "'");
            currency.set("");
            type(widget, "USD");
            var10000 = ((String)currency.get()).equals("USD");
            var10001 = (String)currency.get();
            require(var10000, "letters still accepted, got '" + var10001 + "'");
            currency.set("");
            type(widget, "$$$$$$");
            require(((String)currency.get()).length() == 4, "maxLength caps at 4, got " + ((String)currency.get()).length());
            currency.set(EURO);
         });
         context.runOnClient((mc) -> mc.method_1507(new ClickGuiScreen()));
         context.waitTicks(3);
         context.takeScreenshot("stringinput-clickgui-open");
         context.runOnClient((mc) -> mc.method_1507((class_437)null));
         context.waitTicks(2);
         context.runOnClient((mc) -> {
            FakePayModule fp = SolClient.modules().fakePay;
            fp.feedback.set("Both");
            fp.setEnabled(true);
            require(((String)fp.currency.get()).equals(EURO), "currency persisted as euro");
            mc.field_1724.field_3944.method_45730("pay Notch 250k");
         });
         context.waitTicks(2);
         context.takeScreenshot("stringinput-fakepay-euro-receipt");
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

   private static String codePoint(int cp) {
      return new String(Character.toChars(cp));
   }

   private static void type(StringWidget widget, String text) {
      IntStream var10000 = text.codePoints();
      Objects.requireNonNull(widget);
      var10000.forEach(widget::charTyped);
   }

   private static void require(boolean condition, String what) {
      if (!condition) {
         throw new AssertionError("FAILED: null");
      }
   }
}
