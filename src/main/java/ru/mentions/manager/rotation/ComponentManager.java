package ru.mentions.manager.rotation;

import java.util.HashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.mentions.Client;
import ru.mentions.manager.DragComponent;

@Environment(EnvType.CLIENT)
public final class ComponentManager extends HashMap<Class<? extends Component>, Component> {
   public void init() {
      this.add(new RotationProcess(), new FreeLookUtil(), new DragComponent(), new TestRotation());
      this.values().forEach(component -> Client.eventHandler().subscribe(component));
   }

   public void add(Component... components) {
      for (Component component : components) {
         this.put((Class<? extends Component>)component.getClass(), component);
      }
   }

   public void unregister(Component... components) {
      for (Component component : components) {
         Client.eventHandler().unsubscribe(component);
         this.remove(component.getClass());
      }
   }

   public <T extends Component> T get(Class<T> clazz) {
      return this.values().stream().filter(component -> component.getClass() == clazz).map(clazz::cast).findFirst().orElse(null);
   }
}
