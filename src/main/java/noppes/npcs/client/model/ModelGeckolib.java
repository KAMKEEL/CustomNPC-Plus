package noppes.npcs.client.model;

import net.minecraft.util.ResourceLocation;
import noppes.npcs.entity.EntityGeckoModel;
import software.bernie.geckolib3.GeckoLib;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class ModelGeckolib extends AnimatedGeoModel<EntityGeckoModel> {
    @Override
    public ResourceLocation getAnimationFileLocation(EntityGeckoModel entity) {
        return entity.animResLoc;//new ResourceLocation(GeckoLib.ModID, "animations/bike.animation.json");
    }

    @Override
    public ResourceLocation getModelLocation(EntityGeckoModel entity) {
        return entity.modelResLoc;//new ResourceLocation(GeckoLib.ModID, "geo/bike.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(EntityGeckoModel entity) {
        return entity.textureResLoc;//new ResourceLocation(GeckoLib.ModID, "textures/model/entity/bike.png");
    }
}