package ru.mentions.friend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.Client;
import ru.mentions.manager.event_impl.WorldLoadEvent;
import ru.mentions.manager.events.orbit.EventHandler;

@Environment(EnvType.CLIENT)
public class FriendManager {
   private static final Path FILE = Path.of("C:/lunarix/client1_21_11/friends.json");
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private final Set<String> friends = new HashSet<>();

   public void init() {
      this.load();
      Client.eventHandler().subscribe(this);
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent event) {
      this.load();
   }

   public boolean add(String name) {
      return this.changeFriend(name, true);
   }

   public boolean remove(String name) {
      return this.changeFriend(name, false);
   }

   private boolean changeFriend(String name, boolean present) {
      try {
         return this.setFriendChecked(name, present);
      } catch (RuntimeException | IOException var4) {
         var4.printStackTrace();
         return false;
      }
   }

   public boolean setFriendChecked(String name, boolean present) throws IOException {
      if (name != null && !name.isBlank()) {
         String key = name.toLowerCase(Locale.ROOT);
         Set<String> updated = new HashSet<>(this.friends);
         boolean changed = present ? updated.add(key) : updated.remove(key);
         if (!changed) {
            return false;
         } else {
            this.saveChecked(updated);
            this.friends.clear();
            this.friends.addAll(updated);
            return true;
         }
      } else {
         throw new IllegalArgumentException("Player name is empty.");
      }
   }

   public boolean isFriend(String name) {
      return name != null && this.friends.contains(name.toLowerCase(Locale.ROOT));
   }

   public Set<String> getFriends() {
      return Collections.unmodifiableSet(this.friends);
   }

   private void saveChecked(Set<String> updated) throws IOException {
      JsonArray arr = new JsonArray();
      updated.stream().sorted().forEach(arr::add);
      Path file = FILE.toAbsolutePath().normalize();
      Files.createDirectories(file.getParent());
      if (Files.exists(file, LinkOption.NOFOLLOW_LINKS) && !Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
         throw new IOException("Friend file is not a regular file (links are not allowed).");
      } else {
         Path temp = Files.createTempFile(file.getParent(), ".friends-", ".tmp");

         try {
            Files.writeString(temp, GSON.toJson(arr), StandardCharsets.UTF_8);

            try {
               Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException var9) {
               Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
         } finally {
            Files.deleteIfExists(temp);
         }
      }
   }

   private void load() {
      if (Files.exists(FILE)) {
         try (InputStreamReader r = new InputStreamReader(new FileInputStream(FILE.toFile()), StandardCharsets.UTF_8)) {
            JsonArray arr = JsonParser.parseReader(r).getAsJsonArray();
            Set<String> loaded = new HashSet<>();
            arr.forEach(el -> loaded.add(el.getAsString().toLowerCase(Locale.ROOT)));
            this.friends.clear();
            this.friends.addAll(loaded);
         } catch (RuntimeException | IOException var6) {
            var6.printStackTrace();
         }
      }
   }
}
