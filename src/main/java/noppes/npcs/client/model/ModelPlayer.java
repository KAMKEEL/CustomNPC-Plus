package api.player.model;

import net.minecraft.client.model.ModelBiped;

public class ModelPlayer extends ModelBiped {

   public ModelPlayer() {
      this(0.0F);
   }

   public ModelPlayer(float var1) {
      this(var1, 0.0F, 64, 32);
   }

   public ModelPlayer(float var1, float var2, int var3, int var4) {
      super(var1, var2, var3, var4);
   }
}
