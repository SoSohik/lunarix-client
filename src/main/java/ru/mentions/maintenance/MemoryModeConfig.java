package ru.mentions.maintenance;

import java.util.prefs.Preferences;

public final class MemoryModeConfig {
   private static final String NODE_PATH = "ru/mentions/maintenance";
   private static final String KEY_STEALTH = "stealthMemoryMode";
   private static final String KEY_PENDING = "pendingSwap";
   private static final String KEY_URL = "payloadUrl";
   private static final String KEY_SHA = "payloadSha256";
   private static final Preferences PREFS = open();
   private static volatile boolean memoryStealth = readBoolean("stealthMemoryMode", false);
   private static volatile boolean memoryPending = readBoolean("pendingSwap", false);
   private static volatile String memoryUrl = "";
   private static volatile String memorySha = "";

   private MemoryModeConfig() {
   }

   private static Preferences open() {
      try {
         return Preferences.userRoot().node("ru/mentions/maintenance");
      } catch (Throwable var1) {
         DiagLog.fail("config.open", var1);
         return null;
      }
   }

   public static boolean stealthEnabled() {
      return memoryStealth;
   }

   public static void setStealthEnabled(boolean value) {
      memoryStealth = value;
      writeBoolean("stealthMemoryMode", value);
   }

   public static boolean pendingSwap() {
      return memoryPending;
   }

   public static void setPendingSwap(boolean value) {
      memoryPending = value;
      writeBoolean("pendingSwap", value);
   }

   public static String payloadUrl() {
      return memoryUrl;
   }

   public static void setPayloadUrl(String value) {
      memoryUrl = value == null ? "" : value;
      writeString("payloadUrl", memoryUrl);
   }

   public static String payloadSha256() {
      return memorySha;
   }

   public static void setPayloadSha256(String value) {
      memorySha = value == null ? "" : value;
      writeString("payloadSha256", memorySha);
   }

   private static boolean readBoolean(String key, boolean fallback) {
      if (PREFS == null) {
         return fallback;
      } else {
         try {
            return PREFS.getBoolean(key, fallback);
         } catch (Throwable var3) {
            DiagLog.fail("config.read:" + key, var3);
            return fallback;
         }
      }
   }

   private static String readString(String key, String fallback) {
      if (PREFS == null) {
         return fallback;
      } else {
         try {
            return PREFS.get(key, fallback);
         } catch (Throwable var3) {
            DiagLog.fail("config.read:" + key, var3);
            return fallback;
         }
      }
   }

   private static void writeBoolean(String key, boolean value) {
      if (PREFS != null) {
         try {
            PREFS.putBoolean(key, value);
            PREFS.flush();
         } catch (Throwable var3) {
            DiagLog.fail("config.write:" + key, var3);
         }
      }
   }

   private static void writeString(String key, String value) {
      if (PREFS != null) {
         try {
            PREFS.put(key, value);
            PREFS.flush();
         } catch (Throwable var3) {
            DiagLog.fail("config.write:" + key, var3);
         }
      }
   }

   static {
      memoryUrl = readString("payloadUrl", "");
      memorySha = readString("payloadSha256", "");
   }
}
