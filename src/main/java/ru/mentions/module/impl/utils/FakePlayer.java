package ru.mentions.module.impl.utils;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1507;
import net.minecraft.class_1528;
import net.minecraft.class_1549;
import net.minecraft.class_1551;
import net.minecraft.class_1559;
import net.minecraft.class_1576;
import net.minecraft.class_1593;
import net.minecraft.class_1613;
import net.minecraft.class_1614;
import net.minecraft.class_1627;
import net.minecraft.class_1628;
import net.minecraft.class_1639;
import net.minecraft.class_1641;
import net.minecraft.class_1642;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1887;
import net.minecraft.class_1890;
import net.minecraft.class_1893;
import net.minecraft.class_2378;
import net.minecraft.class_239;
import net.minecraft.class_2398;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2664;
import net.minecraft.class_2824;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_3966;
import net.minecraft.class_4466;
import net.minecraft.class_5134;
import net.minecraft.class_5136;
import net.minecraft.class_5321;
import net.minecraft.class_5455;
import net.minecraft.class_6880;
import net.minecraft.class_745;
import net.minecraft.class_7924;
import net.minecraft.class_2824.class_5908;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.EventPacket;
import ru.mentions.manager.event_impl.EventRender3D;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.module.api.Category;
import ru.mentions.module.api.Module;
import ru.mentions.module.api.ModuleInfo;
import ru.mentions.module.impl.render.Particles;
import ru.mentions.module.impl.render.TotemGhost;
import ru.mentions.utils.math.MathUtil;

@ModuleInfo(
   name = "Fake Player",
   desc = "Создает фейк игрока",
   category = Category.PLAYER
)
@Environment(EnvType.CLIENT)
public class FakePlayer extends Module {
   public static class_745 fakePlayer;
   private int fakeRegenTicks = 0;
   private long lastSoundTime = 0L;

   @EventHandler
   public void onRender(EventRender3D event) {
      if (mc.field_1724 != null && mc.field_1687 != null && fakePlayer != null) {
         fakePlayer.method_36456(0.0F);
         fakePlayer.method_36457(0.0F);
         fakePlayer.method_5847(0.0F);
         fakePlayer.method_5636(0.0F);
         fakePlayer.method_74090(mc.field_1724.method_6068());
         if (this.fakeRegenTicks > 0) {
            this.fakeRegenTicks--;
            if (this.fakeRegenTicks % 20 == 0) {
               fakePlayer.method_6033(Math.min(fakePlayer.method_6032() + 1.0F, 20.0F));
            }
         }
      }
   }

   @EventHandler
   public void onPacket(EventPacket event) {
      if (mc.field_1724 != null && mc.field_1687 != null && fakePlayer != null) {
         if (event.isSend()) {
            if (event.getPacket() instanceof class_2824 packet) {
               class_239 class_2392 = mc.field_1765;
               class_3966 entityHit;
               class_1297 targetEntity;
               if (class_2392 instanceof class_3966
                  && (targetEntity = (entityHit = (class_3966)class_2392).method_17782()) != null
                  && targetEntity.method_5628() == fakePlayer.method_5628()) {
                  packet.method_34209(new class_5908() {
                     public void method_34218() {
                        FakePlayer.this.handleAttack();
                     }

                     public void method_34219(class_1268 hand) {
                     }

                     public void method_34220(class_1268 hand, class_243 pos) {
                     }
                  });
               }
            }

            class_2596 var7;
            if ((var7 = event.getPacket()) instanceof class_2664) {
               class_2664 explosion = (class_2664)var7;
               this.handleExplosion(explosion);
            }
         }
      }
   }

   private void handleAttack() {
      if (!(mc.field_1724.method_5858(fakePlayer) > 36.0)) {
         float damage = this.calculateDamage(mc.field_1724, fakePlayer);
         long currentTime = System.currentTimeMillis();
         if (currentTime - this.lastSoundTime >= 100L) {
            boolean crit = mc.field_1724.method_18798().field_1351 < -0.08 && !mc.field_1724.method_24828() && !mc.field_1724.method_5624();
            mc.field_1724.method_5783(crit ? class_3417.field_15016 : class_3417.field_14840, 1.0F, 1.0F);
            this.lastSoundTime = currentTime;
         }

         this.applyFakeDamage(fakePlayer, damage);
      }
   }

   private float calculateDamage(class_1657 attacker, class_1309 target) {
      class_1799 weapon = attacker.method_6047();
      double base = attacker.method_45325(class_5134.field_23721);
      float cooled = attacker.method_7261(0.5F);
      float damage = (float)(base * (double)(0.2F + cooled * cooled * 0.8F));
      boolean critical = attacker.method_18798().field_1351 < -0.08 && !attacker.method_24828() && !attacker.method_5624();
      if (critical) {
         damage *= 1.5F;
      }

      class_5455 registryManager = attacker.method_73183().method_30349();
      class_2378 enchantmentRegistry = registryManager.method_30530(class_7924.field_41265);
      int sharpness = this.enchantmentLevel(enchantmentRegistry, class_1893.field_9118, weapon);
      if (sharpness > 0) {
         damage += 0.5F + 0.5F * (float)sharpness;
      }

      int smite = this.enchantmentLevel(enchantmentRegistry, class_1893.field_9123, weapon);
      if (smite > 0 && this.isUndead(target)) {
         damage += 2.5F * (float)smite;
      }

      int bane = this.enchantmentLevel(enchantmentRegistry, class_1893.field_9112, weapon);
      if (bane > 0 && this.isArthropod(target)) {
         damage += 2.5F * (float)bane;
      }

      return damage;
   }

   private int enchantmentLevel(class_2378 registry, class_5321<class_1887> enchantment, class_1799 weapon) {
      Object value = registry.method_10223(enchantment.method_29177()).map(entry -> class_1890.method_8225((class_6880)entry, weapon)).orElse(0);
      return value instanceof Number ? ((Number)value).intValue() : 0;
   }

   private boolean isUndead(class_1297 e) {
      return e instanceof class_1642
         || e instanceof class_1613
         || e instanceof class_1639
         || e instanceof class_1528
         || e instanceof class_5136
         || e instanceof class_1507
         || e instanceof class_1641
         || e instanceof class_1576
         || e instanceof class_1627
         || e instanceof class_1551
         || e instanceof class_1593;
   }

   private boolean isArthropod(class_1297 e) {
      return e instanceof class_1628 || e instanceof class_1549 || e instanceof class_1614 || e instanceof class_1559 || e instanceof class_4466;
   }

   private void applyFakeDamage(class_745 target, float damage) {
      float absorption = target.method_6067();
      if (absorption > 0.0F) {
         float used = Math.min(absorption, damage);
         target.method_6073(absorption - used);
         damage -= used;
      }

      if (damage > 0.0F) {
         float oldHealth = target.method_6032();
         float newHealth = Math.max(target.method_6032() - damage, 0.5F);
         target.method_6033(newHealth);
      }

      target.field_6235 = 10;
      target.field_6008 = 10;
      if (!target.method_6059(class_1294.field_5924) || target.method_6112(class_1294.field_5924).method_5584() < 100) {
         target.method_6092(new class_1293(class_1294.field_5924, 900, 1));
      }

      if (!target.method_6059(class_1294.field_5898) || target.method_6112(class_1294.field_5898).method_5584() < 20) {
         target.method_6092(new class_1293(class_1294.field_5898, 100, 1));
      }

      if (target.method_6032() <= 1.0F && target.method_6079().method_31574(class_1802.field_8288)) {
         this.triggerTotem(target);
      }
   }

   private void triggerTotem(class_745 target) {
      mc.field_1724.method_5783(class_3417.field_14931, 1.0F, 1.0F);
      TotemGhost totemGhost = Client.get().moduleManager().get(TotemGhost.class);
      if (totemGhost != null) {
         totemGhost.spawnGhost(target);
      }

      Particles particles = Client.get().moduleManager().get(Particles.class);
      if (particles.totem.getValue()) {
         for (int i = 0; (float)i < particles.onTtSizs.getValue(); i++) {
            double angle = MathUtil.randomValue(0.0, Math.PI * 2);
            double speedXZ = MathUtil.randomValue(2.5, 4.0);
            double motionX = Math.cos(angle) * speedXZ;
            double motionZ = Math.sin(angle) * speedXZ;
            double motionY = MathUtil.randomValue(0.0, (double)(target.method_17682() / 2.0F));
            particles.spawnParticleTotem(
               particles.totemParticles,
               new class_243(
                  target.method_23317() + MathUtil.randomValue(-0.2, 0.2),
                  target.method_23318() + (double)MathUtil.randomValue(0.1F, target.method_17682()),
                  target.method_23321() + MathUtil.randomValue(-0.2, 0.2)
               ),
               new class_243(motionX, motionY, motionZ)
            );
         }
      } else {
         mc.field_1713.method_3051(target, class_2398.field_11220, 30);
      }

      target.method_6012();
      target.method_6033(4.0F);
      target.method_6073(12.0F);
      this.fakeRegenTicks = 1400;
   }

   private void handleExplosion(class_2664 explosion) {
      class_243 expPos = explosion.comp_2883();
      double dist = fakePlayer.method_73189().method_1022(expPos);
      if (!(dist > 11.0)) {
         float damage = (float)((1.0 - dist / 11.0) * 12.0);
         this.applyFakeDamage(fakePlayer, damage);
         mc.field_1724.method_5783((class_3414)class_3417.field_15152.comp_349(), 0.7F, 1.0F);
      }
   }

   public void onToggled(boolean active) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (!active) {
            if (fakePlayer != null) {
               fakePlayer.method_31472();
               fakePlayer = null;
            }
         } else {
            GameProfile profile = new GameProfile(UUID.fromString("32d0a964-137a-2c03-7cc9-df6700000000"), "Lunarix");
            fakePlayer = new class_745(mc.field_1687, profile);
            fakePlayer.method_5808(
               mc.field_1724.method_23317(),
               mc.field_1724.method_23318(),
               mc.field_1724.method_23321(),
               mc.field_1724.method_36454(),
               mc.field_1724.method_36455()
            );
            fakePlayer.method_6122(class_1268.field_5808, new class_1799(class_1802.field_22022));
            fakePlayer.method_6122(class_1268.field_5810, new class_1799(class_1802.field_8288));
            fakePlayer.method_6033(20.0F);
            fakePlayer.method_6073(0.0F);
            mc.field_1687.method_53875(fakePlayer);
         }
      }
   }

   @Override
   public void onEnable() {
      this.onToggled(true);
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.onToggled(false);
      super.onDisable();
   }
}
