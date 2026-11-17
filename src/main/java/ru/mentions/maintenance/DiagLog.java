package ru.mentions.maintenance;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class DiagLog {
   public static final int CAPACITY = 256;
   private static final ArrayDeque<DiagEntry> BUFFER = new ArrayDeque<>(256);

   private DiagLog() {
   }

   public static synchronized void note(String tag, String message) {
      push(tag, message, null);
   }

   public static synchronized void fail(String tag, Throwable thrown) {
      push(tag, thrown == null ? "null" : thrown.getClass().getName(), thrown);
   }

   public static synchronized void win32(String api, int code) {
      push("win32", api + " -> GetLastError=" + code, null);
   }

   private static void push(String tag, String message, Throwable thrown) {
      if (BUFFER.size() >= 256) {
         BUFFER.removeFirst();
      }

      BUFFER.addLast(new DiagEntry(System.currentTimeMillis(), tag, message, thrown));
   }

   public static synchronized List<DiagEntry> snapshot() {
      return new ArrayList<>(BUFFER);
   }

   public static synchronized DiagEntry last() {
      return BUFFER.peekLast();
   }

   public static synchronized int size() {
      return BUFFER.size();
   }

   public static synchronized void clear() {
      BUFFER.clear();
   }
}
