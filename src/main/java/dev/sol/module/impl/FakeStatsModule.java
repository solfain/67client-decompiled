package dev.sol.module.impl;

import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.SliderSetting;
import dev.sol.settings.StringSetting;
import dev.sol.util.Amounts;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.class_2561;
import net.minecraft.class_310;

public class FakeStatsModule extends Module {
   private static final int GRAY = 11184810;
   private static final int GREEN = 5635925;
   private static final String[] MONEY_KEYS = new String[]{"money", "balance", "coins", "cash"};
   private static final String[] SHARD_KEYS = new String[]{"shard"};
   private static final String[] KILL_KEYS = new String[]{"kill"};
   private static final String[] DEATH_KEYS = new String[]{"death"};
   private static final String[] PLAYTIME_KEYS = new String[]{"playtime", "play time", "time played", "played"};
   public final StringSetting money = (StringSetting)this.addSetting(new StringSetting("Money", "Your fake balance (1m, 500k, 250000). Wired to FakePay. Blank = off.", "1m", 32, "e.g. 1m, 250k"));
   public final SliderSetting moneyLine = (SliderSetting)this.addSetting((new SliderSetting("Money Line", "Which sidebar line is money (count from the top). 0 = auto by name.", (double)1.0F, (double)0.0F, (double)15.0F, (double)1.0F)).withLabel(FakeStatsModule::lineLabel));
   public final StringSetting shards = (StringSetting)this.addSetting(new StringSetting("Shards", "Fake shard count. Blank = off.", "", 32, "e.g. 9,999"));
   public final SliderSetting shardsLine = (SliderSetting)this.addSetting((new SliderSetting("Shards Line", "Which sidebar line is shards. 0 = auto by name.", (double)2.0F, (double)0.0F, (double)15.0F, (double)1.0F)).withLabel(FakeStatsModule::lineLabel));
   public final StringSetting kills = (StringSetting)this.addSetting(new StringSetting("Kills", "Fake kill count. Blank = off.", "", 32, "e.g. 1,000"));
   public final SliderSetting killsLine = (SliderSetting)this.addSetting((new SliderSetting("Kills Line", "Which sidebar line is kills. 0 = auto by name.", (double)3.0F, (double)0.0F, (double)15.0F, (double)1.0F)).withLabel(FakeStatsModule::lineLabel));
   public final StringSetting deaths = (StringSetting)this.addSetting(new StringSetting("Deaths", "Fake death count. Blank = off.", "", 32, "e.g. 0"));
   public final SliderSetting deathsLine = (SliderSetting)this.addSetting((new SliderSetting("Deaths Line", "Which sidebar line is deaths. 0 = auto by name.", (double)4.0F, (double)0.0F, (double)15.0F, (double)1.0F)).withLabel(FakeStatsModule::lineLabel));
   public final StringSetting playtime = (StringSetting)this.addSetting(new StringSetting("Playtime", "Fake playtime, any text. Blank = off.", "", 32, "e.g. 365d 12h"));
   public final SliderSetting playtimeLine = (SliderSetting)this.addSetting((new SliderSetting("Playtime Line", "Which sidebar line is playtime. 0 = auto by name.", (double)5.0F, (double)0.0F, (double)15.0F, (double)1.0F)).withLabel(FakeStatsModule::lineLabel));
   public final BooleanSetting sidebar = (BooleanSetting)this.addSetting(new BooleanSetting("Sidebar", "Rewrite the server scoreboard (leaderboard) on the right.", true));
   public final BooleanSetting balanceCommand = (BooleanSetting)this.addSetting(new BooleanSetting("Balance Command", "Intercept /bal & /balance to show your fake balance.", true));
   public final BooleanSetting deductOnPay = (BooleanSetting)this.addSetting(new BooleanSetting("Deduct On Pay", "FakePay subtracts what you pay from Money.", true));
   private String lastMoneyText;
   private double liveBalance;
   private int widthIndex;
   private int drawIndex;

   public FakeStatsModule() {
      super("FakeStats", "Fake balance + editable leaderboard, wired to FakePay", Category.MISC);
      this.moneyLine.visibleWhen(() -> (Boolean)this.sidebar.get() && !((String)this.money.get()).isBlank());
      this.shardsLine.visibleWhen(() -> (Boolean)this.sidebar.get() && !((String)this.shards.get()).isBlank());
      this.killsLine.visibleWhen(() -> (Boolean)this.sidebar.get() && !((String)this.kills.get()).isBlank());
      this.deathsLine.visibleWhen(() -> (Boolean)this.sidebar.get() && !((String)this.deaths.get()).isBlank());
      this.playtimeLine.visibleWhen(() -> (Boolean)this.sidebar.get() && !((String)this.playtime.get()).isBlank());
   }

   protected void onEnable() {
      this.lastMoneyText = null;
      this.syncFromSetting();
   }

   private static String lineLabel(double v) {
      return (int)v == 0 ? "Auto" : "Line " + (int)v;
   }

   private void syncFromSetting() {
      String text = (String)this.money.get();
      if (!Objects.equals(text, this.lastMoneyText)) {
         this.lastMoneyText = text;
         double parsed = Amounts.parse(text);
         this.liveBalance = Double.isNaN(parsed) ? (double)0.0F : Math.max((double)0.0F, parsed);
      }

   }

   public double getLiveBalance() {
      this.syncFromSetting();
      return this.liveBalance;
   }

   public void deduct(double amount) {
      this.syncFromSetting();
      this.liveBalance = Math.max((double)0.0F, this.liveBalance - Math.max((double)0.0F, amount));
   }

   public void beginSidebar() {
      this.widthIndex = 0;
      this.drawIndex = 0;
   }

   public class_2561 rewriteForWidth(class_2561 comp) {
      return this.rewriteLine(comp, true);
   }

   public class_2561 rewriteForDraw(class_2561 comp) {
      return this.rewriteLine(comp, false);
   }

   private class_2561 rewriteLine(class_2561 comp, boolean widthPass) {
      if (this.isEnabled() && (Boolean)this.sidebar.get()) {
         String text = stripCodes(comp.getString());
         if (text.isBlank()) {
            return comp;
         } else {
            int var10000;
            if (widthPass) {
               int var10002 = this.widthIndex;
               var10000 = var10002;
               this.widthIndex = var10002 + 1;
            } else {
               int var5 = this.drawIndex;
               var10000 = var5;
               this.drawIndex = var5 + 1;
            }

            int line = var10000;
            return this.applyOverride(line, text.toLowerCase(Locale.ROOT), comp);
         }
      } else {
         return comp;
      }
   }

   private class_2561 applyOverride(int line, String lower, class_2561 comp) {
      if (!((String)this.money.get()).isBlank() && matches(this.moneyLine, line, lower, MONEY_KEYS)) {
         return Amounts.replaceNumberStyled(comp, Amounts.shortForm(this.getLiveBalance()));
      } else if (!((String)this.shards.get()).isBlank() && matches(this.shardsLine, line, lower, SHARD_KEYS)) {
         return Amounts.replaceNumberStyled(comp, ((String)this.shards.get()).trim());
      } else if (!((String)this.kills.get()).isBlank() && matches(this.killsLine, line, lower, KILL_KEYS)) {
         return Amounts.replaceNumberStyled(comp, ((String)this.kills.get()).trim());
      } else if (!((String)this.deaths.get()).isBlank() && matches(this.deathsLine, line, lower, DEATH_KEYS)) {
         return Amounts.replaceNumberStyled(comp, ((String)this.deaths.get()).trim());
      } else {
         return !((String)this.playtime.get()).isBlank() && matches(this.playtimeLine, line, lower, PLAYTIME_KEYS) ? Amounts.replaceValueStyled(comp, ((String)this.playtime.get()).trim()) : comp;
      }
   }

   private static boolean matches(SliderSetting lineSetting, int line, String lower, String[] keys) {
      int configured = lineSetting.getInt();
      return configured > 0 ? line + 1 == configured : containsAny(lower, keys);
   }

   private static boolean containsAny(String haystack, String[] keys) {
      for(String k : keys) {
         if (haystack.contains(k)) {
            return true;
         }
      }

      return false;
   }

   public boolean tryInterceptBalance(String command) {
      if (this.isEnabled() && (Boolean)this.balanceCommand.get()) {
         String[] parts = command.trim().split("\\s+");
         if (parts.length == 0) {
            return false;
         } else {
            String name = parts[0];
            if (name.startsWith("/")) {
               name = name.substring(1);
            }

            if (!name.equalsIgnoreCase("bal") && !name.equalsIgnoreCase("balance")) {
               return false;
            } else {
               class_310 mc = class_310.method_1551();
               if (mc.field_1724 == null) {
                  return false;
               } else if (parts.length >= 2 && !parts[1].equalsIgnoreCase(mc.field_1724.method_7334().name())) {
                  return false;
               } else {
                  String amount = Amounts.shortForm(this.getLiveBalance());
                  class_2561 line = class_2561.method_43473().method_10852(class_2561.method_43470("You have ").method_54663(11184810)).method_10852(class_2561.method_43470(amount).method_54663(5635925));
                  mc.field_1724.method_7353(line, false);
                  mc.field_1724.method_7353(line, true);
                  return true;
               }
            }
         }
      } else {
         return false;
      }
   }

   private static String stripCodes(String s) {
      return s == null ? "" : s.replaceAll("(?i)§[0-9A-FK-OR]", "");
   }
}
