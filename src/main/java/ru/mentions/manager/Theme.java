package ru.mentions.manager;

import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.utils.animation.satoshi.Animation;
import ru.mentions.utils.animation.satoshi.EaseInOutQuad;
import ru.mentions.utils.colors.ColorUtil;

@Environment(EnvType.CLIENT)
public enum Theme {
   NIGHT(
      "Green",
      new Color(5628047).getRGB(),
      new Color(-1726867175, true).getRGB(),
      ColorUtil.getColor(240),
      ColorUtil.getColor(160),
      ColorUtil.getColor(24, 24, 27),
      new Color(5628047).getRGB()
   ),
   AKAR(
      "Red",
      new Color(16735324).getRGB(),
      new Color(-1726344682, true).getRGB(),
      ColorUtil.getColor(240),
      ColorUtil.getColor(160),
      ColorUtil.getColor(24, 24, 27),
      new Color(16735324).getRGB()
   ),
   BLUE(
      "Blue",
      new Color(5081343).getRGB(),
      new Color(-1726933986, true).getRGB(),
      ColorUtil.getColor(240),
      ColorUtil.getColor(160),
      ColorUtil.getColor(24, 24, 27),
      new Color(5081343).getRGB()
   ),
   VIOLKA(
      "Violet",
      new Color(10315263).getRGB(),
      new Color(-1726672098, true).getRGB(),
      ColorUtil.getColor(240),
      ColorUtil.getColor(160),
      ColorUtil.getColor(24, 24, 27),
      new Color(10315263).getRGB()
   ),
   TOXIS(
      "Toxis",
      new Color(9109309).getRGB(),
      new Color(-1726735851, true).getRGB(),
      ColorUtil.getColor(240),
      ColorUtil.getColor(160),
      ColorUtil.getColor(24, 24, 27),
      new Color(9109309).getRGB()
   ),
   White(
      "White",
      new Color(14671839).getRGB(),
      new Color(-1726408419, true).getRGB(),
      ColorUtil.getColor(240),
      ColorUtil.getColor(160),
      ColorUtil.getColor(24, 24, 27),
      new Color(14671839).getRGB()
   ),
   Turquoise(
      "Turquoise",
      new Color(3731390).getRGB(),
      new Color(-1726998245, true).getRGB(),
      ColorUtil.getColor(240),
      ColorUtil.getColor(160),
      ColorUtil.getColor(24, 24, 27),
      new Color(3731390).getRGB()
   ),
   Purple(
      "Purple",
      new Color(14632184).getRGB(),
      new Color(-1726410466, true).getRGB(),
      ColorUtil.getColor(240),
      ColorUtil.getColor(160),
      ColorUtil.getColor(24, 24, 27),
      new Color(14632184).getRGB()
   ),
   SUNSET(
      "Sunset",
      new Color(16744504).getRGB(),
      new Color(-1726344172, true).getRGB(),
      ColorUtil.getColor(240),
      ColorUtil.getColor(160),
      ColorUtil.getColor(24, 24, 27),
      new Color(16744504).getRGB()
   ),
   SAKURA(
      "Sakura",
      new Color(16740536).getRGB(),
      new Color(-1726344421, true).getRGB(),
      ColorUtil.getColor(240),
      ColorUtil.getColor(160),
      ColorUtil.getColor(24, 24, 27),
      new Color(16740536).getRGB()
   ),
   MIDNIGHT(
      "Midnight",
      new Color(4939944).getRGB(),
      new Color(-1726934502, true).getRGB(),
      ColorUtil.getColor(240),
      ColorUtil.getColor(160),
      ColorUtil.getColor(24, 24, 27),
      new Color(4939944).getRGB()
   ),
   GOLD(
      "Gold",
      new Color(16172101).getRGB(),
      new Color(-1726343403, true).getRGB(),
      ColorUtil.getColor(240),
      ColorUtil.getColor(160),
      ColorUtil.getColor(24, 24, 27),
      new Color(16172101).getRGB()
   ),
   EMERALD(
      "Emerald",
      new Color(3066993).getRGB(),
      new Color(-1726998505, true).getRGB(),
      ColorUtil.getColor(240),
      ColorUtil.getColor(160),
      ColorUtil.getColor(24, 24, 27),
      new Color(3066993).getRGB()
   );

   private final String name;
   private final int client;
   private final int rect;
   private final int text;
   private final int text_dark;
   private final int rect_two;
   private final int text_client_c;
   public Animation animation = new EaseInOutQuad(300, 1.0);

   private Theme(String name, int client, int rect, int text, int text_dark, int rect_two, int text_client_c) {
      this.name = name;
      this.client = client;
      this.rect = rect;
      this.text = text;
      this.text_dark = text_dark;
      this.rect_two = rect_two;
      this.text_client_c = text_client_c;
   }

   public String getName() {
      return this.name;
   }

   public int getClient() {
      return this.client;
   }

   public int getRect() {
      return this.rect;
   }

   public int getText() {
      return this.text;
   }

   public int getText_dark() {
      return this.text_dark;
   }

   public int getRect_two() {
      return this.rect_two;
   }

   public int getText_client_c() {
      return this.text_client_c;
   }
}
