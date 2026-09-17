package noppes.npcs.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import noppes.npcs.AnimationMixinFunctions;
import java.util.HashSet;
import java.util.Set;
import noppes.npcs.client.ClientEventHandler;

public final class PlayerModelAnimation {
    private PlayerModelAnimation() {
    }

    public static Pose capture(ModelBiped model) {
        return new Pose(parts(model));
    }

    private static final Set<String> REPORTED = new HashSet<String>();

    public static void apply(ModelBiped model) {
        boolean applied = false;
        for (ModelRenderer part : parts(model)) {
            if (part != null && AnimationMixinFunctions.applyValues(part)) {
                applied = true;
            }
        }
        // Once per model class and outcome: whether the hook had an animation to put on.
        String key = model.getClass().getName() + (applied ? " applied" : " nothing");
        if (REPORTED.add(key)) {
            System.out.println("[CustomNPC+] Player animation on " + model.getClass().getName()
                + ": " + (applied ? "values applied" : "nothing to apply")
                + " (renderingPlayer=" + (ClientEventHandler.renderingPlayer != null)
                + ", renderingNpc=" + (ClientEventHandler.renderingNpc != null) + ")");
        }
    }

    private static ModelRenderer[] parts(ModelBiped model) {
        return new ModelRenderer[]{
            model.bipedHead,
            model.bipedBody,
            model.bipedRightArm,
            model.bipedLeftArm,
            model.bipedRightLeg,
            model.bipedLeftLeg
        };
    }

    public static final class Pose {
        private final ModelRenderer[] parts;
        private final float[][] values;

        private Pose(ModelRenderer[] parts) {
            this.parts = parts;
            this.values = new float[parts.length][6];
            for (int i = 0; i < parts.length; i++) {
                ModelRenderer part = parts[i];
                if (part == null) {
                    continue;
                }
                values[i][0] = part.rotationPointX;
                values[i][1] = part.rotationPointY;
                values[i][2] = part.rotationPointZ;
                values[i][3] = part.rotateAngleX;
                values[i][4] = part.rotateAngleY;
                values[i][5] = part.rotateAngleZ;
            }
        }

        public void restore() {
            for (int i = 0; i < parts.length; i++) {
                ModelRenderer part = parts[i];
                if (part == null) {
                    continue;
                }
                part.rotationPointX = values[i][0];
                part.rotationPointY = values[i][1];
                part.rotationPointZ = values[i][2];
                part.rotateAngleX = values[i][3];
                part.rotateAngleY = values[i][4];
                part.rotateAngleZ = values[i][5];
            }
        }
    }
}
