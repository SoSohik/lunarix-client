package ru.mentions.manager.neuro;

import com.mojang.authlib.GameProfile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_745;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.AttackEvent;
import ru.mentions.manager.event_impl.EventDisplay;
import ru.mentions.manager.event_impl.EventTick;
import ru.mentions.manager.events.orbit.EventHandler;
import ru.mentions.manager.rotation.Rotation;
import ru.mentions.module.impl.utils.FakePlayer;
import ru.mentions.utils.annotation.IMinecraft;
import ru.mentions.utils.math.ChatUtils;

@Environment(EnvType.CLIENT)
public class NeuroManager implements IMinecraft {
   private static NeuroManager instance;
   private final Map<String, NeuroModel> models = new LinkedHashMap<>();
   private NeuroModel current;
   private boolean recording;
   private class_745 dummy;
   private class_243 dummyCenter;
   private boolean spawnedDummy;
   private final List<double[]> recX = new ArrayList<>();
   private final List<double[]> recY = new ArrayList<>();
   private float lastYaw;
   private float lastPitch;
   private float prevAppliedYaw;
   private float prevAppliedPitch;
   private float lastHitX = 0.5F;
   private float lastHitY = 0.5F;
   private float lastHitZ = 0.5F;
   private long trainStart;
   private float frameLastYaw;
   private float frameLastPitch;
   private boolean frameInit;
   private float framePath;
   private float framePeak;
   private int frameCount;
   private float prevSmoothness = 1.0F;
   private float prevPeak = 0.0F;
   private float emaYawSpeed;
   private float emaPitchSpeed;
   private final List<float[]> recStrokes = new ArrayList<>();
   private float emaSignedYaw;
   private float emaSignedPitch;
   private final List<float[]> recTraj = new ArrayList<>();
   private int attackTicksAgo = 999;
   private boolean trajGapPending;

   public static NeuroManager get() {
      if (instance == null) {
         instance = new NeuroManager();
         Client.eventHandler().subscribe(instance);
      }

      return instance;
   }

   private NeuroManager() {
      this.loadModels();
   }

   public boolean isRecording() {
      return this.recording;
   }

   public NeuroModel getCurrent() {
      return this.current;
   }

   public Collection<NeuroModel> all() {
      return this.models.values();
   }

   public boolean create(String name) {
      if (this.models.containsKey(name.toLowerCase())) {
         return false;
      } else {
         NeuroModel m = new NeuroModel(name);
         this.models.put(name.toLowerCase(), m);
         this.current = m;
         m.save();
         NeuroModel.saveCurrentName(name);
         return true;
      }
   }

   public boolean select(String name) {
      NeuroModel m = this.models.get(name.toLowerCase());
      if (m == null) {
         m = NeuroModel.load(name);
      }

      if (m == null) {
         return false;
      } else {
         this.models.put(name.toLowerCase(), m);
         this.current = m;
         NeuroModel.saveCurrentName(m.name);
         return true;
      }
   }

   public boolean delete(String name) {
      NeuroModel m = this.models.remove(name.toLowerCase());

      try {
         Files.deleteIfExists(NeuroModel.DIR.resolve(name + ".neuro"));
      } catch (Exception var4) {
      }

      if (m != null && this.current == m) {
         this.current = this.models.values().stream().max(Comparator.comparingInt(model -> model.samples)).orElse(null);
         NeuroModel.saveCurrentName(this.current == null ? null : this.current.name);
      }

      return m != null;
   }

   private void loadModels() {
      try {
         if (!Files.isDirectory(NeuroModel.DIR)) {
            return;
         }

         try (Stream<Path> stream = Files.list(NeuroModel.DIR)) {
            stream.filter(path -> path.getFileName().toString().endsWith(".neuro")).forEach(path -> {
               String file = path.getFileName().toString();
               String name = file.substring(0, file.length() - ".neuro".length());
               NeuroModel model = NeuroModel.load(name);
               if (model != null) {
                  this.models.put(name.toLowerCase(), model);
               }
            });
         }

         String selected = NeuroModel.loadCurrentName();
         if (selected != null) {
            this.current = this.models.get(selected.toLowerCase());
         }

         if (this.current == null) {
            this.current = this.models.values().stream().max(Comparator.comparingInt(model -> model.samples)).orElse(null);
         }
      } catch (Exception var6) {
      }
   }

   public void startTrain() {
      if (this.current == null) {
         ChatUtils.addChatMessage("§cСначала создай/выбери модель: §f.neuro create <имя>");
      } else if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.recording) {
            ChatUtils.addChatMessage("§7Тренировка уже идет. §f.neuro stop §7чтобы закончить");
         } else {
            if (FakePlayer.fakePlayer != null && FakePlayer.fakePlayer.method_5805()) {
               this.dummy = FakePlayer.fakePlayer;
               this.dummyCenter = this.dummy.method_73189();
               this.spawnedDummy = false;
            } else {
               this.spawnDummy();
            }

            this.recX.clear();
            this.recY.clear();
            this.lastYaw = Rotation.cameraYaw();
            this.lastPitch = Rotation.cameraPitch();
            this.prevAppliedYaw = 0.0F;
            this.prevAppliedPitch = 0.0F;
            this.lastHitX = 0.5F;
            this.lastHitY = 0.5F;
            this.lastHitZ = 0.5F;
            this.frameInit = false;
            this.framePath = 0.0F;
            this.framePeak = 0.0F;
            this.frameCount = 0;
            this.prevSmoothness = 1.0F;
            this.prevPeak = 0.0F;
            this.emaYawSpeed = 0.0F;
            this.emaPitchSpeed = 0.0F;
            this.recStrokes.clear();
            this.emaSignedYaw = 0.0F;
            this.emaSignedPitch = 0.0F;
            this.recTraj.clear();
            this.attackTicksAgo = 999;
            this.trajGapPending = false;
            this.trainStart = System.currentTimeMillis();
            this.recording = true;
            ChatUtils.addChatMessage(
               "§a[Neuro] §7Тренировка модели §f" + this.current.name + "§7 началась: веди прицелом по хитбоксу. §f.neuro stop§7 для завершения"
            );
         }
      }
   }

   public void stopTrain() {
      if (!this.recording) {
         ChatUtils.addChatMessage("§7Тренировка не запущена");
      } else {
         this.recording = false;
         this.removeDummy();
         int n = this.recX.size();
         if (this.current != null && n >= 10) {
            this.current.ensureModernShape();
            double[][] X = this.recX.toArray(new double[0][]);
            double[][] Y = this.recY.toArray(new double[0][]);
            double loss = this.current.net.train(X, Y, 650, 0.18);
            this.current.remember(X, Y);
            this.current.addStrokes(this.recStrokes);
            this.current.addTrajectory(this.recTraj);
            this.current.samples += n;
            this.current.loss = loss;
            this.current.save();
            ChatUtils.addChatMessage(
               String.format(
                  "§a[Neuro] §7Модель §f%s§7 обучена: §f%d§7 образцов, §f%d§7 штрихов, §f%d§7 тиков траектории, ошибка §f%.4f",
                  this.current.name,
                  n,
                  this.current.strokeCount(),
                  this.current.trajCount(),
                  loss
               )
            );
         } else {
            ChatUtils.addChatMessage("§cМало данных (" + n + "), модель не обучена. Поводи прицелом подольше");
         }
      }
   }

   private void spawnDummy() {
      double rad = Math.toRadians((double)Rotation.cameraYaw());
      double dx = -Math.sin(rad) * 3.5;
      double dz = Math.cos(rad) * 3.5;
      this.dummyCenter = new class_243(mc.field_1724.method_23317() + dx, mc.field_1724.method_23318(), mc.field_1724.method_23321() + dz);
      this.spawnedDummy = true;
      GameProfile profile = new GameProfile(UUID.fromString("32d0a964-137a-2c03-7cc9-df6700000001"), "NEURO_DUMMY");
      this.dummy = new class_745(mc.field_1687, profile);
      this.dummy.method_5808(this.dummyCenter.field_1352, this.dummyCenter.field_1351, this.dummyCenter.field_1350, 0.0F, 0.0F);
      this.dummy.method_5673(class_1304.field_6169, new class_1799(class_1802.field_22027));
      this.dummy.method_5673(class_1304.field_6174, new class_1799(class_1802.field_22028));
      this.dummy.method_5673(class_1304.field_6172, new class_1799(class_1802.field_22029));
      this.dummy.method_5673(class_1304.field_6166, new class_1799(class_1802.field_22030));
      this.dummy.method_6122(class_1268.field_5808, new class_1799(class_1802.field_22022));
      this.dummy.method_6122(class_1268.field_5810, new class_1799(class_1802.field_8288));
      this.dummy.method_6033(20.0F);
      this.dummy.method_5834(true);
      mc.field_1687.method_53875(this.dummy);
   }

   private void removeDummy() {
      if (this.dummy != null && this.spawnedDummy) {
         this.dummy.method_31472();
      }

      this.dummy = null;
      this.spawnedDummy = false;
   }

   @EventHandler
   public void onAttack(AttackEvent event) {
      if (this.recording) {
         this.attackTicksAgo = 0;
      }
   }

   @EventHandler
   public void onDisplay(EventDisplay e) {
      if (this.recording && mc.field_1724 != null) {
         float yaw = Rotation.cameraYaw();
         float pitch = Rotation.cameraPitch();
         if (!this.frameInit) {
            this.frameLastYaw = yaw;
            this.frameLastPitch = pitch;
            this.frameInit = true;
         } else {
            float dYaw = class_3532.method_15393(yaw - this.frameLastYaw);
            float dPitch = pitch - this.frameLastPitch;
            float step = (float)Math.hypot((double)dYaw, (double)dPitch);
            this.framePath += step;
            this.framePeak = Math.max(this.framePeak, step);
            this.frameCount++;
            this.frameLastYaw = yaw;
            this.frameLastPitch = pitch;
         }
      }
   }

   @EventHandler
   public void onTick(EventTick e) {
      if (this.recording && mc.field_1724 != null && mc.field_1687 != null && this.dummy != null && this.dummyCenter != null) {
         if (mc.field_1755 == null && mc.method_1569()) {
            long now = System.currentTimeMillis() - this.trainStart;
            double ang = (double)now / 650.0;
            double offX = Math.cos(ang) * 1.8;
            double offZ = Math.sin(ang * 0.8) * 1.4;
            double offY = Math.sin((double)now / 500.0) * 0.7;
            if (this.spawnedDummy) {
               this.dummy.method_5808(this.dummyCenter.field_1352 + offX, this.dummyCenter.field_1351 + offY, this.dummyCenter.field_1350 + offZ, 0.0F, 0.0F);
            }

            this.dummy.method_6033(20.0F);
            float curYaw = Rotation.cameraYaw();
            float curPitch = Rotation.cameraPitch();
            class_243 centerAim = this.dummy.method_5829().method_1005().method_1020(mc.field_1724.method_33571());
            float centerYaw = yawTo(centerAim);
            float centerPitch = pitchTo(centerAim);
            class_243 hitPoint = this.currentHitPoint(this.dummy, curYaw, curPitch);
            float[] hitNorm = normalizeHitPoint(this.dummy.method_5829(), hitPoint);
            this.lastHitX = hitNorm[0];
            this.lastHitY = hitNorm[1];
            this.lastHitZ = hitNorm[2];
            class_243 pointAim = hitPoint.method_1020(mc.field_1724.method_33571());
            float pointYaw = yawTo(pointAim);
            float pointPitch = pitchTo(pointAim);
            float yawErr = class_3532.method_15393(centerYaw - this.lastYaw);
            float pitchErr = centerPitch - this.lastPitch;
            float pointYawErr = class_3532.method_15393(pointYaw - this.lastYaw);
            float pointPitchErr = pointPitch - this.lastPitch;
            float appliedYaw = class_3532.method_15393(curYaw - this.lastYaw);
            float appliedPitch = curPitch - this.lastPitch;
            float yawShake = class_3532.method_15393(curYaw - pointYaw);
            float pitchShake = curPitch - pointPitch;
            float distance = (float)mc.field_1724.method_33571().method_1022(hitPoint);
            this.emaYawSpeed = this.emaYawSpeed + (Math.abs(appliedYaw) - this.emaYawSpeed) * 0.45F;
            this.emaPitchSpeed = this.emaPitchSpeed + (Math.abs(appliedPitch) - this.emaPitchSpeed) * 0.45F;
            float strokeJitterYaw = appliedYaw - this.emaSignedYaw;
            float strokeJitterPitch = appliedPitch - this.emaSignedPitch;
            this.emaSignedYaw = this.emaSignedYaw + (appliedYaw - this.emaSignedYaw) * 0.35F;
            this.emaSignedPitch = this.emaSignedPitch + (appliedPitch - this.emaSignedPitch) * 0.35F;
            this.recStrokes.add(new float[]{strokeJitterYaw, strokeJitterPitch, (float)Math.hypot((double)this.emaSignedYaw, (double)this.emaSignedPitch)});
            this.attackTicksAgo = Math.min(this.attackTicksAgo + 1, 999);
            if (this.trajGapPending) {
               this.recTraj.add(new float[]{0.0F, 0.0F, 0.0F, 0.0F, -1.0F});
               this.trajGapPending = false;
            }

            this.recTraj.add(new float[]{yawErr, pitchErr, appliedYaw, appliedPitch, (float)Math.min(this.attackTicksAgo, 60)});
            float displacement = (float)Math.hypot((double)appliedYaw, (double)appliedPitch);
            float smoothness = this.framePath < 0.02F ? 1.0F : class_3532.method_15363(displacement / this.framePath, 0.0F, 1.0F);
            float peak = this.frameCount <= 0 ? 0.0F : class_3532.method_15363(this.framePeak * (float)this.frameCount / 90.0F, 0.0F, 1.0F);
            this.framePath = 0.0F;
            this.framePeak = 0.0F;
            this.frameCount = 0;
            this.recX
               .add(
                  NeuroModel.features(
                     yawErr,
                     pitchErr,
                     appliedYaw,
                     appliedPitch,
                     this.prevAppliedYaw,
                     this.prevAppliedPitch,
                     this.lastHitX,
                     this.lastHitY,
                     this.lastHitZ,
                     distance,
                     this.prevSmoothness,
                     this.prevPeak
                  )
               );
            this.recY
               .add(
                  new double[]{
                     (double)(class_3532.method_15363(this.emaYawSpeed, 0.0F, 180.0F) / 180.0F),
                     (double)(class_3532.method_15363(this.emaPitchSpeed, 0.0F, 120.0F) / 120.0F),
                     (double)this.lastHitX,
                     (double)this.lastHitY,
                     (double)this.lastHitZ,
                     NeuroModel.signedToUnit(yawShake, 8.0F),
                     NeuroModel.signedToUnit(pitchShake, 5.0F),
                     NeuroModel.signedToUnit(appliedYaw, 35.0F),
                     NeuroModel.signedToUnit(appliedPitch, 22.0F),
                     NeuroModel.signedToUnit(appliedYaw - this.prevAppliedYaw, 35.0F),
                     NeuroModel.signedToUnit(appliedPitch - this.prevAppliedPitch, 22.0F),
                     (double)smoothness,
                     (double)peak
                  }
               );
            this.prevSmoothness = smoothness;
            this.prevPeak = peak;
            this.prevAppliedYaw = appliedYaw;
            this.prevAppliedPitch = appliedPitch;
            this.lastYaw = curYaw;
            this.lastPitch = curPitch;
         } else {
            this.lastYaw = Rotation.cameraYaw();
            this.lastPitch = Rotation.cameraPitch();
            this.prevAppliedYaw = 0.0F;
            this.prevAppliedPitch = 0.0F;
            this.framePath = 0.0F;
            this.framePeak = 0.0F;
            this.frameCount = 0;
            this.frameInit = false;
            this.emaSignedYaw = 0.0F;
            this.emaSignedPitch = 0.0F;
            this.trajGapPending = true;
         }
      }
   }

   private class_243 currentHitPoint(class_745 target, float yaw, float pitch) {
      class_243 eye = mc.field_1724.method_33571();
      class_243 end = eye.method_1019(rotationVector(yaw, pitch).method_1021(8.0));
      Optional hit = target.method_5829().method_992(eye, end);
      if (hit.isPresent()) {
         return (class_243)hit.get();
      } else {
         class_238 box = target.method_5829();
         return new class_243(
            class_3532.method_16436((double)this.lastHitX, box.field_1323, box.field_1320),
            class_3532.method_16436((double)this.lastHitY, box.field_1322, box.field_1325),
            class_3532.method_16436((double)this.lastHitZ, box.field_1321, box.field_1324)
         );
      }
   }

   private static float[] normalizeHitPoint(class_238 box, class_243 point) {
      return new float[]{
         normalize((float)((point.field_1352 - box.field_1323) / Math.max(0.001, box.field_1320 - box.field_1323))),
         normalize((float)((point.field_1351 - box.field_1322) / Math.max(0.001, box.field_1325 - box.field_1322))),
         normalize((float)((point.field_1350 - box.field_1321) / Math.max(0.001, box.field_1324 - box.field_1321)))
      };
   }

   private static float normalize(float value) {
      return class_3532.method_15363(value, 0.0F, 1.0F);
   }

   private static class_243 rotationVector(float yaw, float pitch) {
      double yawRad = Math.toRadians((double)yaw);
      double pitchRad = Math.toRadians((double)pitch);
      double cosPitch = Math.cos(pitchRad);
      return new class_243(-Math.sin(yawRad) * cosPitch, -Math.sin(pitchRad), Math.cos(yawRad) * cosPitch);
   }

   private static float usefulMovement(float applied, float error) {
      if (!(Math.abs(applied) < 0.001F) && !(Math.abs(error) < 0.001F)) {
         return Math.signum(applied) == Math.signum(error) ? Math.abs(applied) : 0.0F;
      } else {
         return 0.0F;
      }
   }

   private static float yawTo(class_243 aim) {
      return (float)Math.toDegrees(Math.atan2(-aim.field_1352, aim.field_1350));
   }

   private static float pitchTo(class_243 aim) {
      return (float)class_3532.method_15350(-Math.toDegrees(Math.atan2(aim.field_1351, Math.hypot(aim.field_1352, aim.field_1350))), -90.0, 90.0);
   }

   public float[] predict(float yawErr, float pitchErr) {
      return this.current != null && this.current.samples > 0 ? this.current.predict(yawErr, pitchErr) : new float[]{90.0F, 90.0F};
   }

   public NeuroModel.Prediction predictDetailed(float yawErr, float pitchErr) {
      return this.current != null && this.current.samples > 0
         ? this.current.predictDetailed(yawErr, pitchErr)
         : new NeuroModel.Prediction(90.0F, 90.0F, 0.5F, 0.5F, 0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, false);
   }

   public NeuroModel.Prediction predictDetailed(double[] features) {
      return this.current != null && this.current.samples > 0
         ? this.current.predictDetailed(features)
         : new NeuroModel.Prediction(90.0F, 90.0F, 0.5F, 0.5F, 0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, false);
   }

   public float[] nextMove(float yawErr, float pitchErr, float sinceAttack) {
      return this.current == null ? null : this.current.nextMove(yawErr, pitchErr, sinceAttack);
   }

   public float[] nextJitter(float speed) {
      return this.current == null ? new float[]{0.0F, 0.0F} : this.current.nextJitter(speed);
   }

   public boolean hasTrainedModel() {
      return this.current != null && this.current.samples > 0;
   }
}
