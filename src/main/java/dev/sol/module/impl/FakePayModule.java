package dev.sol.module.impl;

import dev.sol.SolClient;
import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.module.ModuleManager;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.ColorSetting;
import dev.sol.settings.ModeSetting;
import dev.sol.settings.StringSetting;
import dev.sol.util.Amounts;
import net.minecraft.class_1109;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_5250;

public class FakePayModule extends Module {
   private static final int WHITE = 16777215;
   private static final int RED = 16733525;
   public final StringSetting command = (StringSetting)this.addSetting(new StringSetting("Command", "Command name to hijack, without the slash.", "pay", 32, "pay"));
   public final ModeSetting feedback = (ModeSetting)this.addSetting(new ModeSetting("Feedback", "Where the confirmation shows.", "Both", new String[]{"Both", "Action Bar", "Chat"}));
   public final StringSetting currency = (StringSetting)this.addSetting(new StringSetting("Currency", "Symbol before the amount (colored).", "$", 4, "$"));
   public final ColorSetting currencyColor = (ColorSetting)this.addSetting(new ColorSetting("Currency Color", "Color of the currency symbol (DonutSMP blue by default).", -16740609));
   public final BooleanSetting sounds = (BooleanSetting)this.addSetting(new BooleanSetting("Sounds", "Level-up on success, villager 'no' when it fails.", true));
   public final BooleanSetting checkBalance = (BooleanSetting)this.addSetting(new BooleanSetting("Check Balance", "With FakeStats on, refuse pays you can't afford.", true));
   public final BooleanSetting selfGuard = (BooleanSetting)this.addSetting(new BooleanSetting("Self-Pay Guard", "Block paying your own name, like the real command.", true));

   public FakePayModule() {
      super("FakePay", "Fakes a /pay for clips — blocks the real command", Category.MISC);
   }

   public boolean tryIntercept(String rawCommand) {
      if (this.isEnabled() && rawCommand != null) {
         String[] parts = rawCommand.trim().split("\\s+");
         if (parts.length == 0) {
            return false;
         } else {
            String name = parts[0];
            if (name.startsWith("/")) {
               name = name.substring(1);
            }

            String target = ((String)this.command.get()).trim();
            if (!target.isEmpty() && name.equalsIgnoreCase(target)) {
               this.handle(parts);
               return true;
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   private void handle(String[] parts) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null) {
         if (parts.length < 3) {
            this.show(mc, class_2561.method_43470("Usage: /" + parts[0] + " <player> <amount>").method_54663(16733525));
            this.fail(mc);
         } else {
            String player = parts[1];
            double amount = Amounts.parse(parts[2]);
            if (!Double.isNaN(amount) && !(amount <= (double)0.0F)) {
               if ((Boolean)this.selfGuard.get() && player.equalsIgnoreCase(mc.field_1724.method_7334().name())) {
                  this.show(mc, class_2561.method_43470("You can't pay yourself!").method_54663(16733525));
                  this.fail(mc);
               } else {
                  FakeStatsModule stats = this.stats();
                  boolean useStats = stats != null && stats.isEnabled() && (Boolean)stats.deductOnPay.get();
                  if (useStats && (Boolean)this.checkBalance.get() && stats.getLiveBalance() < amount) {
                     this.show(mc, class_2561.method_43470("You don't have enough money!").method_54663(16733525));
                     this.fail(mc);
                  } else {
                     if (useStats) {
                        stats.deduct(amount);
                     }

                     class_5250 line = class_2561.method_43473().method_10852(class_2561.method_43470("You paid null").method_54663(16777215));
                     String symbol = (String)this.currency.get();
                     if (!symbol.isEmpty()) {
                        line.method_10852(class_2561.method_43470(" null").method_54663((Integer)this.currencyColor.get() & 16777215)).method_10852(class_2561.method_43470(Amounts.shortForm(amount)).method_54663(16777215));
                     } else {
                        line.method_10852(class_2561.method_43470(" " + Amounts.shortForm(amount)).method_54663(16777215));
                     }

                     this.show(mc, line);
                     this.succeed(mc);
                  }
               }
            } else {
               this.show(mc, class_2561.method_43470("Invalid amount: " + parts[2]).method_54663(16733525));
               this.fail(mc);
            }
         }
      }

   }

   private void show(class_310 mc, class_2561 component) {
      if (mc.field_1724 != null) {
         String mode = (String)this.feedback.get();
         if (mode.equals("Action Bar") || mode.equals("Both")) {
            mc.field_1724.method_7353(component, true);
         }

         if (mode.equals("Chat") || mode.equals("Both")) {
            mc.field_1724.method_7353(component, false);
         }
      }

   }

   private void succeed(class_310 mc) {
      if ((Boolean)this.sounds.get()) {
         this.play(mc, class_3417.field_14709, 1.0F);
      }

   }

   private void fail(class_310 mc) {
      if ((Boolean)this.sounds.get()) {
         this.play(mc, class_3417.field_15008, 1.0F);
      }

   }

   private void play(class_310 mc, class_3414 event, float pitch) {
      mc.method_1483().method_4873(class_1109.method_4757(event, pitch, 1.0F));
   }

   private FakeStatsModule stats() {
      ModuleManager modules = SolClient.modules();
      return modules == null ? null : modules.fakeStats;
   }
}
