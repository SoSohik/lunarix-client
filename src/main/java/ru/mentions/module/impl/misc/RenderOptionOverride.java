package ru.mentions.module.impl.misc;

import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class RenderOptionOverride<T> {
   private final Supplier<T> read;
   private final Consumer<T> write;
   private final BinaryOperator<T> reduce;
   private T original;
   private T applied;
   private T requested;
   private boolean owned;
   private boolean yielded;

   RenderOptionOverride(Supplier<T> read, Consumer<T> write, BinaryOperator<T> reduce) {
      this.read = read;
      this.write = write;
      this.reduce = reduce;
   }

   void update(boolean enabled, T cap) {
      if (!enabled) {
         this.restore();
      } else if (!this.yielded) {
         T current = this.read.get();
         if (this.owned && !Objects.equals(current, this.applied)) {
            this.owned = false;
            this.original = null;
            this.applied = null;
            this.requested = null;
            this.yielded = true;
         } else {
            T target = this.reduce.apply(this.owned ? this.original : current, cap);
            if (!this.owned || !Objects.equals(target, this.requested)) {
               if (!Objects.equals(current, target)) {
                  if (!this.owned) {
                     this.original = current;
                  }

                  this.write.accept(target);
                  this.applied = this.read.get();
                  this.requested = target;
                  this.owned = true;
               }
            }
         }
      }
   }

   void restore() {
      if (this.owned && Objects.equals(this.read.get(), this.applied) && !Objects.equals(this.original, this.applied)) {
         this.write.accept(this.original);
      }

      this.original = null;
      this.applied = null;
      this.requested = null;
      this.owned = false;
      this.yielded = false;
   }
}
