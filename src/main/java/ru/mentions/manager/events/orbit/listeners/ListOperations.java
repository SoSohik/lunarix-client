package ru.mentions.manager.events.orbit.listeners;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ListOperations {
   private static final int MIN_BINARY_SEARCH_THRESHOLD = 8;

   public static IListener remove(List<IListener> listeners, IListener listener) {
      int priority = listener.getPriority();

      for (int i = 0; i < listeners.size(); i++) {
         IListener o = listeners.get(i);
         if (o == listener) {
            return listeners.remove(i);
         }

         if (o.getPriority() < priority) {
            break;
         }
      }

      return null;
   }

   public static boolean contains(List<IListener> listeners, IListener listener) {
      int priority = listener.getPriority();

      for (IListener o : listeners) {
         if (o == listener) {
            return true;
         }

         if (o.getPriority() < priority) {
            break;
         }
      }

      return false;
   }

   public static void insert(List<IListener> listeners, IListener listener) {
      int index = listeners.size() < 8 ? linearSearch(listeners, listener) : binarySearch(listeners, listener);
      listeners.add(index, listener);
   }

   private static int linearSearch(List<IListener> listeners, IListener listener) {
      int size = listeners.size();

      for (int i = 0; i < size; i++) {
         if (listener.getPriority() > listeners.get(i).getPriority()) {
            return i;
         }
      }

      return size;
   }

   private static int binarySearch(List<IListener> listeners, IListener listener) {
      int low = 0;
      int high = listeners.size() - 1;

      while (low <= high) {
         int mid = low + (high - low) / 2;
         IListener midElement = listeners.get(mid);
         int comp = Integer.compare(listener.getPriority(), midElement.getPriority());
         if (comp == 0) {
            return mid;
         }

         if (comp < 0) {
            low = mid + 1;
         } else {
            high = mid - 1;
         }
      }

      return low;
   }
}
