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

public class EntityCustomModel extends EntityCreature implements IAnimatable, IAnimationTickable {
    private AnimationFactory factory = new AnimationFactory(this);
    public ResourceLocation modelResLoc=new ResourceLocation("geckolib3", "geo/npc.geo.json");
    public ResourceLocation animResLoc=new ResourceLocation("custom", "geo_npc.animation.json");
    public ResourceLocation textureResLoc = new ResourceLocation("geckolib3", "textures/model/entity/geo_npc.png");
    public String idleAnim = "";
    public String walkAnim = "";
    public String hurtAnim = "";
    public String attackAnim = "";
    public String dialogAnim = "";
    public AnimationBuilder manualAnim = new AnimationBuilder();
    public ItemStack leftHeldItem;
    public AnimationController<EntityCustomModel> dialogController;
    public AnimationController<EntityCustomModel> manualController;
    private <E extends IAnimatable> PlayState predicateMovement(AnimationEvent<E> event) {
        if(dialogController!=null && dialogController.getAnimationState()!= AnimationState.Stopped){
            return PlayState.STOP;
        }
        if(manualController!=null && manualController.getAnimationState()!= AnimationState.Stopped){
            return PlayState.STOP;
        }
        if(!event.isMoving()){
            if(!Objects.equals(idleAnim, "")) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation(idleAnim, true));
            }else{
                return PlayState.STOP;
            }
        }else{
            if(!Objects.equals(idleAnim, "")) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation(walkAnim, true));
            }else{
                return PlayState.STOP;
            }
        }
        return PlayState.CONTINUE;
    }
    private <E extends IAnimatable> PlayState predicateDialog(AnimationEvent<E> event) {
        if(!Objects.equals(dialogAnim, "")) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation(dialogAnim, false));
        }else{
            return PlayState.STOP;
        }
        return PlayState.CONTINUE;
    }
    private <E extends IAnimatable> PlayState predicateAttack(AnimationEvent<E> event) {
        return PlayState.CONTINUE;
    }
    private <E extends IAnimatable> PlayState predicateManual(AnimationEvent<E> event) {
        event.getController().setAnimation(manualAnim);
        return PlayState.CONTINUE;
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
        dialogController = new AnimationController<>(this, "dialog", 10, this::predicateDialog);
        data.addAnimationController(dialogController);
        manualController = new AnimationController<>(this, "manual", 10, this::predicateManual);
        data.addAnimationController(manualController);
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