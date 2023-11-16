package noppes.npcs.client.model;

import net.minecraft.util.ResourceLocation;
import noppes.npcs.entity.EntityCustomModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class ModelCustom extends AnimatedGeoModel<EntityCustomModel> {
    @Override
    public ResourceLocation getAnimationFileLocation(EntityCustomModel entity) {
        return entity.animResLoc;//new ResourceLocation(GeckoLib.ModID, "animations/bike.animation.json");
    }

    @Override
    public ResourceLocation getModelLocation(EntityCustomModel entity) {
        return entity.modelResLoc;//new ResourceLocation(GeckoLib.ModID, "geo/bike.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCustomModel entity) {
        return entity.textureResLoc;//new ResourceLocation(GeckoLib.ModID, "textures/model/entity/bike.png");
    }
}