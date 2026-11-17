package ru.mentions.maintenance.win;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import ru.mentions.maintenance.DiagLog;

public final class FsOps {
   private static final long RETRY_DELAY_MS = 200L;

   private FsOps() {
   }

   public static boolean attempt(String action, Path target, FsOps.Op op) {
      if (op == null) {
         return false;
      } else if (runOnce(action, op, "")) {
         return true;
      } else {
         sleep(200L);
         if (runOnce(action, op, "-retry")) {
            return true;
         } else {
            DiagLog.note("fs", action + ":skip");
            return false;
         }
      }
   }

   private static boolean runOnce(String action, FsOps.Op op, String suffix) {
      try {
         if (op.run()) {
            DiagLog.note("fs", action + ":ok" + suffix);
            return true;
         }
      } catch (Throwable var4) {
         DiagLog.note("fs", action + ":err" + suffix + ":" + var4.getClass().getSimpleName());
      }

      return false;
   }

   public static boolean delete(Path target) {
      return target == null ? false : attempt("delete", target, () -> Files.deleteIfExists(target));
   }

   public static boolean rename(Path from, Path to) {
      return from != null && to != null ? attempt("rename", from, () -> {
         Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
         return true;
      }) : false;
   }

   private static void sleep(long millis) {
      try {
         Thread.sleep(millis);
      } catch (InterruptedException var3) {
         Thread.currentThread().interrupt();
      }
   }

   @FunctionalInterface
   public interface Op {
      boolean run() throws Throwable;
   }
}
