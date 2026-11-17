package ru.mentions.module.impl.combat.aura.rotation;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.utils.annotation.IMinecraft;

@Environment(EnvType.CLIENT)
public final class CursorTrajectoryLibrary implements IMinecraft {
   private static CursorTrajectoryLibrary cachedLibrary;
   private static long cachedModifiedAt;
   private static final int MATCH_SEARCH_RADIUS = 20;
   public final List<CursorTrajectoryLibrary.Trajectory> trajectories;
   public final double degreesPerUnit;

   private CursorTrajectoryLibrary(List<CursorTrajectoryLibrary.Trajectory> trajectories, double degreesPerUnit) {
      this.trajectories = trajectories;
      this.degreesPerUnit = degreesPerUnit;
   }

   public boolean isEmpty() {
      return this.trajectories.isEmpty();
   }

   public CursorTrajectoryLibrary.TrajectoryMatch findMatch(double yawErr, double pitchErr, boolean mirrored) {
      int size = this.trajectories.size();
      if (size == 0) {
         return null;
      } else {
         double magnitude = Math.hypot(yawErr, pitchErr);
         int lo = 0;
         int hi = size - 1;

         while (lo < hi) {
            int mid = lo + hi >>> 1;
            if (this.trajectories.get(mid).startMagnitude < magnitude) {
               lo = mid + 1;
            } else {
               hi = mid;
            }
         }

         int from = Math.max(0, lo - 20);
         int to = Math.min(size - 1, lo + 20);
         CursorTrajectoryLibrary.Trajectory best = null;
         double bestDist = Double.MAX_VALUE;

         for (int i = from; i <= to; i++) {
            CursorTrajectoryLibrary.Trajectory t = this.trajectories.get(i);
            double sy = mirrored ? -t.startYawDelta : t.startYawDelta;
            double sp = mirrored ? -t.startPitchDelta : t.startPitchDelta;
            double d = Math.hypot(yawErr - sy, pitchErr - sp);
            if (d < bestDist) {
               bestDist = d;
               best = t;
            }
         }

         return best != null ? new CursorTrajectoryLibrary.TrajectoryMatch(best, mirrored) : null;
      }
   }

   public static synchronized CursorTrajectoryLibrary getCached() {
      File file = libraryFile();
      if (file == null) {
         return cachedLibrary;
      } else {
         long modified = file.exists() ? file.lastModified() : 0L;
         if (cachedLibrary != null && modified == cachedModifiedAt) {
            return cachedLibrary;
         } else {
            try {
               cachedLibrary = load(file);
               cachedModifiedAt = modified;
            } catch (Exception var4) {
               cachedLibrary = null;
               cachedModifiedAt = modified;
            }

            return cachedLibrary;
         }
      }
   }

   public static File libraryFile() {
      return mc != null && mc.field_1697 != null ? new File(mc.field_1697, "isle/cursor_library.json") : null;
   }

   public static CursorTrajectoryLibrary load(File file) throws IOException {
      if (file != null && file.exists()) {
         Gson gson = new Gson();
         BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));

         JsonObject root;
         try {
            root = (JsonObject)gson.fromJson(reader, JsonObject.class);
         } catch (Throwable var24) {
            try {
               reader.close();
            } catch (Throwable var23) {
               var24.addSuppressed(var23);
            }

            throw var24;
         }

         reader.close();
         if (root == null) {
            throw new IOException("empty cursor_library.json");
         } else {
            double degreesPerUnit = root.has("deg_per_unit") ? root.get("deg_per_unit").getAsDouble() : 0.15;
            JsonArray array = root.getAsJsonArray("trajectories");
            if (array == null) {
               throw new IOException("no 'trajectories' array");
            } else {
               ArrayList<CursorTrajectoryLibrary.Trajectory> trajectories = new ArrayList<>(array.size());

               for (int i = 0; i < array.size(); i++) {
                  JsonObject obj = array.get(i).getAsJsonObject();
                  String session = obj.has("session") ? obj.get("session").getAsString() : "";
                  int trajectoryId = obj.has("trajectoryId") ? obj.get("trajectoryId").getAsInt() : 0;
                  double startYawDelta = obj.has("start_dYaw") ? obj.get("start_dYaw").getAsDouble() : 0.0;
                  double startPitchDelta = obj.has("start_dPitch") ? obj.get("start_dPitch").getAsDouble() : 0.0;
                  double startMagnitude = obj.has("start_magnitude") ? obj.get("start_magnitude").getAsDouble() : Math.hypot(startYawDelta, startPitchDelta);
                  JsonArray dxArr = obj.getAsJsonArray("dx");
                  JsonArray dyArr = obj.getAsJsonArray("dy");
                  if (dxArr != null && dyArr != null) {
                     int n = Math.min(dxArr.size(), dyArr.size());
                     if (n != 0) {
                        double[] dx = new double[n];
                        double[] dy = new double[n];

                        for (int j = 0; j < n; j++) {
                           dx[j] = dxArr.get(j).getAsDouble();
                           dy[j] = dyArr.get(j).getAsDouble();
                        }

                        trajectories.add(new CursorTrajectoryLibrary.Trajectory(session, trajectoryId, startYawDelta, startPitchDelta, startMagnitude, dx, dy));
                     }
                  }
               }

               trajectories.sort(Comparator.comparingDouble(t -> t.startMagnitude));
               return new CursorTrajectoryLibrary(trajectories, degreesPerUnit);
            }
         }
      } else {
         throw new IOException("cursor_library.json not found at " + file);
      }
   }

   public static final class Trajectory {
      public final String session;
      public final int trajectoryId;
      public final double startYawDelta;
      public final double startPitchDelta;
      public final double startMagnitude;
      public final double[] dx;
      public final double[] dy;

      public Trajectory(String session, int trajectoryId, double startYawDelta, double startPitchDelta, double startMagnitude, double[] dx, double[] dy) {
         this.session = session;
         this.trajectoryId = trajectoryId;
         this.startYawDelta = startYawDelta;
         this.startPitchDelta = startPitchDelta;
         this.startMagnitude = startMagnitude;
         this.dx = dx;
         this.dy = dy;
      }

      public int length() {
         return this.dx.length;
      }
   }

   public static final class TrajectoryMatch {
      public final CursorTrajectoryLibrary.Trajectory trajectory;
      public final boolean mirrored;

      public TrajectoryMatch(CursorTrajectoryLibrary.Trajectory trajectory, boolean mirrored) {
         this.trajectory = trajectory;
         this.mirrored = mirrored;
      }
   }
}
