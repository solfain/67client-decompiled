package dev.sol.module.impl;

import dev.sol.SolClient;
import dev.sol.module.Category;
import dev.sol.module.Module;
import dev.sol.settings.BooleanSetting;
import dev.sol.settings.ModeSetting;
import dev.sol.settings.SliderSetting;
import dev.sol.settings.StringSetting;
import dev.sol.staff.StaffDetector;
import dev.sol.staff.StaffEntry;
import dev.sol.staff.StaffTracker;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.class_1109;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;

public class StaffListModule extends Module {
   public final ModeSetting detectBy = (ModeSetting)this.addSetting(new ModeSetting("Detect By", "How staff are recognised: the DonutSMP coloured star, a text rank prefix, or both", "Star + Rank", new String[]{"Star + Rank", "Star Only", "Rank Only", "Names Only"}));
   public final StringSetting staffNames = (StringSetting)this.addSetting(new StringSetting("Staff Names", "Extra known staff usernames (comma-separated) — always shown when online", "", 220, "e.g. Notch, jeb_"));
   public final StringSetting rankKeywords;
   public final StringSetting starSymbols;
   public final BooleanSetting fontIcons;
   public final BooleanSetting showRank;
   public final BooleanSetting showPing;
   public final BooleanSetting vanished;
   public final SliderSetting maxRows;
   public final ModeSetting alerts;
   public final BooleanSetting alertSound;
   public final StaffTracker tracker;

   public StaffListModule() {
      super("StaffList", "Lists online staff on the HUD (DonutSMP coloured-star detection)", Category.MISC);
      this.rankKeywords = (StringSetting)this.addSetting(new StringSetting("Rank Keywords", "Words in a name tag that mark staff (comma-separated), most senior first", StaffDetector.DEFAULT_RANK_KEYWORDS_STRING, 256, "owner, admin, mod…"));
      this.starSymbols = (StringSetting)this.addSetting(new StringSetting("Star Symbols", "Marker glyphs that mean 'staff'. Paste the server's star here if detection misses", "★☆✦✧✪✩✫✬✭✮✯⭐✰❂⚝✴✵✶✷✸✹⍟", 96, "★ ✦ ⭐"));
      this.fontIcons = (BooleanSetting)this.addSetting(new BooleanSetting("Font Icons", "Also treat custom resource-pack icons (private-use glyphs) as staff stars", true));
      this.showRank = (BooleanSetting)this.addSetting(new BooleanSetting("Show Rank", "Show each staff member's rank label when the server exposes one", true));
      this.showPing = (BooleanSetting)this.addSetting(new BooleanSetting("Show Ping", "Show each staff member's latency", false));
      this.vanished = (BooleanSetting)this.addSetting(new BooleanSetting("Vanished Staff", "Include soft-vanished staff (spectator / hidden from tab), marked separately", true));
      this.maxRows = (SliderSetting)this.addSetting(new SliderSetting("Max Rows", "Most staff rows to show before collapsing into a '+N more' line", (double)6.0F, (double)1.0F, (double)20.0F, (double)1.0F));
      this.alerts = (ModeSetting)this.addSetting(new ModeSetting("Alerts", "Announce when a new staff member appears in your tab", "Toast", new String[]{"Toast", "Chat", "Off"}));
      this.alertSound = (BooleanSetting)this.addSetting(new BooleanSetting("Alert Sound", "Play a bell when a staff alert fires", true));
      this.tracker = new StaffTracker(this);
      this.rankKeywords.visibleWhen(this::usesRank);
      this.starSymbols.visibleWhen(this::usesStar);
      this.fontIcons.visibleWhen(this::usesStar);
      this.alertSound.visibleWhen(() -> !this.alerts.check("Off"));
   }

   private boolean usesStar() {
      return this.detectBy.check("Star + Rank") || this.detectBy.check("Star Only");
   }

   private boolean usesRank() {
      return this.detectBy.check("Star + Rank") || this.detectBy.check("Rank Only");
   }

   protected void onEnable() {
      this.tracker.reset();
   }

   protected void onDisable() {
      this.tracker.clear();
   }

   public void onTick() {
      this.tracker.tick();
   }

   public List<StaffEntry> staff() {
      return this.tracker.current();
   }

   public StaffDetector.DetectConfig detectConfig() {
      List<String> keywords = parseKeywords((String)this.rankKeywords.get());
      return new StaffDetector.DetectConfig((String)this.detectBy.get(), parseNames((String)this.staffNames.get()), keywords.isEmpty() ? StaffDetector.DEFAULT_RANK_KEYWORDS : keywords, (String)this.starSymbols.get(), (Boolean)this.fontIcons.get(), (Boolean)this.vanished.get());
   }

   public void onStaffAppear(StaffEntry e) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null && !this.alerts.check("Off")) {
         String rank = e.rankLabel().isEmpty() ? "Staff" : e.rankLabel();
         String tail = e.vanished() ? " (vanished)" : "";
         if (this.alerts.check("Toast")) {
            if (SolClient.notifications() != null) {
               SolClient.notifications().pushInfo(rank + " " + e.name() + " online" + tail);
            }
         } else if (this.alerts.check("Chat")) {
            mc.field_1724.method_7353(class_2561.method_43470("§d[67] §f" + rank + " §b" + e.name() + "§7 is online" + tail), false);
         }

         if ((Boolean)this.alertSound.get()) {
            mc.method_1483().method_4873(class_1109.method_4757((class_3414)class_3417.field_14793.comp_349(), 1.5F, 0.6F));
         }
      }

   }

   private static Set<String> parseNames(String raw) {
      Set<String> out = new HashSet();

      for(String part : raw.split("[,\\s]+")) {
         if (!part.isEmpty()) {
            out.add(part.toLowerCase(Locale.ROOT));
         }
      }

      return out;
   }

   private static List<String> parseKeywords(String raw) {
      List<String> out = new ArrayList();

      for(String part : raw.split("[,\\s]+")) {
         String kw = part.trim().toLowerCase(Locale.ROOT);
         if (!kw.isEmpty()) {
            out.add(kw);
         }
      }

      return out;
   }
}
