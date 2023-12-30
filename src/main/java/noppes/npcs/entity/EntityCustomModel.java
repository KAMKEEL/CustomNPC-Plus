package noppes.npcs.entity;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Objects;
import java.util.PriorityQueue;

public class EntityCustomModel extends EntityCreature implements IAnimatable, IAnimationTickable {
    private AnimationFactory factory = new AnimationFactory(this);
    public ResourceLocation modelResLoc = new ResourceLocation("geckolib3", "geo/npc.geo.json");
    public ResourceLocation animResLoc = new ResourceLocation("custom", "geo_npc.animation.json");
    public ResourceLocation textureResLoc = new ResourceLocation("geckolib3", "textures/model/entity/geo_npc.png");
    public String idleAnim = "";
    public String walkAnim = "";
    public String hurtAnim = "";
    public String attackAnim = "";
    public AnimationBuilder dialogAnim = null;
    public AnimationBuilder manualAnim = null;
    public ItemStack leftHeldItem;

    private <E extends IAnimatable> PlayState predicateMovement(AnimationEvent<E> event) {
        if (manualAnim != null) {
            if (event.getController().getAnimationState() == AnimationState.Stopped) {
                manualAnim = null;
            } else {
                if (event.getController().currentAnimationBuilder != manualAnim) {
                    event.getController().markNeedsReload();
                }
                event.getController().setAnimation(manualAnim);
                return PlayState.CONTINUE;
            }
        }
        if (dialogAnim != null) {
            if (event.getController().getAnimationState() == AnimationState.Stopped) {
                dialogAnim = null;
            } else {
                if (event.getController().currentAnimationBuilder != dialogAnim) {
                    event.getController().markNeedsReload();
                }
                event.getController().setAnimation(dialogAnim);
                return PlayState.CONTINUE;
            }
        }
        if (!event.isMoving() || walkAnim.isEmpty()) {
            if (!idleAnim.isEmpty()) {
                event.getController().setAnimation(new AnimationBuilder().loop(idleAnim));
            } else {
                return PlayState.STOP;
            }
        } else {
            event.getController().setAnimation(new AnimationBuilder().loop(walkAnim));
        }
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState predicateAttack(AnimationEvent<E> event) {
        return PlayState.CONTINUE;
    }

    public void setDialogAnim(String name) {
        dialogAnim = new AnimationBuilder().playOnce(name);
    }

    public EntityCustomModel(World worldIn) {
        super(worldIn);
        this.ignoreFrustumCheck = true;
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.setSize(0.7F, 2F);
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "movement", 10, this::predicateMovement));
        data.addAnimationController(new AnimationController<>(this, "attack", 10, this::predicateAttack));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Override
    public int tickTimer() {
        return ticksExisted;
    }

    @Override
    public void tick() {
        super.onUpdate();
    }
}