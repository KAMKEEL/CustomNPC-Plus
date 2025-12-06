package noppes.npcs.entity;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.addon.DBCAddon;
import kamkeel.npcs.addon.client.DBCClient;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumSoundOperation;
import kamkeel.npcs.network.packets.data.ChatBubblePacket;
import kamkeel.npcs.network.packets.data.QuestCompletionPacket;
import kamkeel.npcs.network.packets.data.SoundManagementPacket;
import kamkeel.npcs.network.packets.data.npc.UpdateNpcPacket;
import kamkeel.npcs.network.packets.data.npc.WeaponNpcPacket;
import kamkeel.npcs.util.AttributeAttackUtil;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.NPCEntityHelper;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIRestrictSun;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ServerChatEvent;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.DataAI;
import noppes.npcs.DataAdvanced;
import noppes.npcs.DataDisplay;
import noppes.npcs.DataInventory;
import noppes.npcs.DataStats;
import noppes.npcs.EventHooks;
import noppes.npcs.IChatMessages;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcDamageSource;
import noppes.npcs.VersionCompatibility;
import noppes.npcs.ai.CombatHandler;
import noppes.npcs.ai.EntityAIAmbushTarget;
import noppes.npcs.ai.EntityAIAnimation;
import noppes.npcs.ai.EntityAIAttackTarget;
import noppes.npcs.ai.EntityAIAvoidTarget;
import noppes.npcs.ai.EntityAIBustDoor;
import noppes.npcs.ai.EntityAIDodgeShoot;
import noppes.npcs.ai.EntityAIFindShade;
import noppes.npcs.ai.EntityAIFollow;
import noppes.npcs.ai.EntityAIJob;
import noppes.npcs.ai.EntityAILook;
import noppes.npcs.ai.EntityAIMoveIndoors;
import noppes.npcs.ai.EntityAIMovingPath;
import noppes.npcs.ai.EntityAIOrbitTarget;
import noppes.npcs.ai.EntityAIPanic;
import noppes.npcs.ai.EntityAILeapAtTargetNpc;
import noppes.npcs.ai.EntityAIPounceTarget;
import noppes.npcs.ai.EntityAIRangedAttack;
import noppes.npcs.ai.EntityAIReturn;
import noppes.npcs.ai.EntityAIRole;
import noppes.npcs.ai.EntityAISprintToTarget;
import noppes.npcs.ai.EntityAIStalkTarget;
import noppes.npcs.ai.EntityAITransform;
import noppes.npcs.ai.EntityAIWander;
import noppes.npcs.ai.EntityAIWatchClosest;
import noppes.npcs.ai.EntityAIWaterNav;
import noppes.npcs.ai.EntityAIWorldLines;
import noppes.npcs.ai.EntityAIZigZagTarget;
import noppes.npcs.ai.pathfinder.FlyingMoveHelper;
import noppes.npcs.ai.pathfinder.PathNavigateFlying;
import noppes.npcs.ai.selector.NPCAttackSelector;
import noppes.npcs.ai.target.EntityAIClearTarget;
import noppes.npcs.ai.target.EntityAIClosestTarget;
import noppes.npcs.ai.target.EntityAIOwnerHurtByTarget;
import noppes.npcs.ai.target.EntityAIOwnerHurtTarget;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.handler.data.ILine;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.client.EntityUtil;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.constants.EnumCombatPolicy;
import noppes.npcs.constants.EnumJobType;
import noppes.npcs.constants.EnumMovingType;
import noppes.npcs.constants.EnumNavType;
import noppes.npcs.constants.EnumPotionType;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.constants.EnumStandingType;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.LinkedNpcController;
import noppes.npcs.controllers.LinkedNpcController.LinkedData;
import noppes.npcs.controllers.data.DataScript;
import noppes.npcs.controllers.data.DataTransform;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.controllers.data.action.ActionManager;
import noppes.npcs.entity.data.DataTimers;
import noppes.npcs.roles.JobBard;
import noppes.npcs.roles.JobFollower;
import noppes.npcs.roles.JobInterface;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.roles.RoleFollower;
import noppes.npcs.roles.RoleMount;
import noppes.npcs.roles.RoleInterface;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.entity.ScriptNpc;
import noppes.npcs.scripted.event.NpcEvent;
import noppes.npcs.util.GameProfileAlt;
import noppes.npcs.util.NPCMountUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public abstract class EntityNPCInterface extends EntityCreature implements IEntityAdditionalSpawnData, ICommandSender, IRangedAttackMob, IBossDisplayData {
    public ICustomNpc wrappedNPC;

    public static final GameProfileAlt chateventProfile = new GameProfileAlt();
    public static FakePlayer chateventPlayer;
    public DataDisplay display;
    public DataStats stats;
    public DataAI ais;
    public DataAdvanced advanced;
    public DataInventory inventory;
    public DataScript script;
    public DataTransform transform;
    public DataTimers timers;

    public CombatHandler combatHandler = new CombatHandler(this);
    public ActionManager actionManager = new ActionManager();

    public String linkedName = "";
    public long linkedLast = 0;
    public LinkedData linkedData;

    public float baseHeight = 1.8f;
    public float scaleX, scaleY, scaleZ;
    private boolean wasKilled = false;
    public RoleInterface roleInterface;
    public JobInterface jobInterface;
    public HashMap<Integer, DialogOption> dialogs;
    public boolean hasDied = false;
    public long killedtime = 0;
    public long totalTicksAlive = 0;
    private int taskCount = 1;
    public int lastInteract = 0;
    public boolean isDrawn = false;

    public Faction faction; //should only be used server side

    private EntityAIRangedAttack aiRange;
    private EntityAIBase aiResponse, aiLeap, aiSprint, aiAttackTarget;

    public List<EntityLivingBase> interactingEntities = new ArrayList<EntityLivingBase>();

    public ResourceLocation textureLocation = null;

    private final NPCMountUtil.MountState mountState = new NPCMountUtil.MountState();
    private static final int NPC_STATE_FLAG_FLYING = 1;
    private static final int NPC_STATE_FLAG_JUMPING = 1 << 1;

    public EnumAnimation currentAnimation = EnumAnimation.NONE;

    public int npcVersion = VersionCompatibility.ModRev;
    public IChatMessages messages;

    public boolean updateClient = false;
    public boolean updateAI = false;

    public FlyingMoveHelper flyMoveHelper = new FlyingMoveHelper(this);
    public PathNavigate flyNavigator = new PathNavigateFlying(this, worldObj);

    public EntityNPCInterface(World world) {
        super(world);
        try {
            if (canFly()) {
                this.getNavigator().setCanSwim(true);
                this.tasks.addTask(0, new EntityAISwimming(this));
            } else {
                this.tasks.addTask(0, new EntityAIWaterNav(this));
            }

            dialogs = new HashMap<Integer, DialogOption>();
            if (!ConfigMain.DefaultInteractLine.isEmpty())
                advanced.interactLines.lines.put(0, new Line(ConfigMain.DefaultInteractLine));

            experienceValue = 0;
            scaleX = scaleY = scaleZ = 0.9375f;

            faction = getFaction();
            setFaction(faction.id);
            setSize(1, 1);
            this.updateTasks();
            this.func_110163_bv();

            if (!this.isRemote() && this.wrappedNPC == null) {
                this.wrappedNPC = new ScriptNpc<>(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();

        display = new DataDisplay(this);
        stats = new DataStats(this);
        ais = new DataAI(this);
        advanced = new DataAdvanced(this);
        inventory = new DataInventory(this);
        transform = new DataTransform(this);
        script = new DataScript(this);
        timers = new DataTimers(this);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage);

        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(stats.maxHealth);
        this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(ConfigMain.NpcNavRange);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(this.getSpeed());
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(stats.getAttackStrength());
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataWatcher.addObject(13, "");//faction
        this.dataWatcher.addObject(14, Integer.valueOf(0)); // Animation
        this.dataWatcher.addObject(15, Integer.valueOf(0)); // isWalking
        this.dataWatcher.addObject(16, ""); // Role
        this.dataWatcher.addObject(17, Byte.valueOf((byte) 0)); // Mount state flags
    }

    protected boolean isAIEnabled() {
        return true;
    }

    @Override
    public boolean getLeashed() {
        return false; //Prevents npcs from being leashed
    }

    @Override
    public boolean isEntityAlive() {
        return super.isEntityAlive() && !isKilled();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!isRemote()) {
            if (this.ticksExisted % 10 == 0) {
                EventHooks.onNPCUpdate(this);
            }
            for (int i = 1; i < 3; i++) {
                ItemStack itemstack = inventory.prevWeapons.get(i);
                ItemStack itemstack1 = inventory.weapons.get(i);
                if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
                    NBTTagCompound itemNBT = new NBTTagCompound();
                    if (itemstack1 != null) {
                        itemstack1.writeToNBT(itemNBT);
                    }

                    PacketHandler.Instance.sendTracking(new WeaponNpcPacket(getEntityId(), i, itemNBT), this);
                    inventory.prevWeapons.put(i, itemstack1 == null ? null : itemstack1.copy());
                }
            }
            this.timers.update();
        }
    }

    public void setWorld(World world) {
        super.setWorld(world);
        script.setWorld(world);
    }

    public int getMaxSafePointTries() {
        return 3;
    }

    @Override
    public boolean attackEntityAsMob(Entity receiver) {
        float f = stats.getAttackStrength();

        if (receiver instanceof EntityPlayer && !isRemote())
            f = AttributeAttackUtil.calculateDamageNPCtoPlayer(this, (EntityPlayer) receiver, f);

        if (stats.attackSpeed < 10) {
            receiver.hurtResistantTime = 0;
        }
        if (receiver instanceof EntityLivingBase && !isRemote()) {
            NpcEvent.MeleeAttackEvent event = new NpcEvent.MeleeAttackEvent(wrappedNPC, f, (EntityLivingBase) receiver);
            if (EventHooks.onNPCMeleeAttack(this, event))
                return false;
            f = event.getDamage();
        }

        boolean didAttack = false;
        if (DBCAddon.instance.canDBCAttack(this, f, receiver)) {
            didAttack = receiver.attackEntityFrom(new NpcDamageSource("mob", this), 0.001f);
            if (didAttack)
                DBCAddon.instance.doDBCDamage(this, f, receiver);
        } else
            didAttack = receiver.attackEntityFrom(new NpcDamageSource("mob", this), f);


        if (didAttack) {
            if (getOwner() instanceof EntityPlayer)
                NPCEntityHelper.setRecentlyHit((EntityLivingBase) receiver);
            if (stats.knockback > 0) {
                receiver.addVelocity(-MathHelper.sin(this.rotationYaw * (float) Math.PI / 180.0F) * (float) stats.knockback * 0.5F, 0.1D, MathHelper.cos(this.rotationYaw * (float) Math.PI / 180.0F) * (float) stats.knockback * 0.5F);
                this.motionX *= 0.6D;
                this.motionZ *= 0.6D;
            }
            if (advanced.role == EnumRoleType.Companion) {
                ((RoleCompanion) roleInterface).attackedEntity(receiver);
            }
        }

        if (stats.potionType != EnumPotionType.None) {
            if (stats.potionType != EnumPotionType.Fire)
                ((EntityLivingBase) receiver).addPotionEffect(new PotionEffect(this.getPotionEffect(stats.potionType), stats.potionDuration * 20, stats.potionAmp));
            else
                receiver.setFire(stats.potionDuration);
        }
        return didAttack;
    }

    @Override
    public void swingItem() {
        ItemStack stack = this.getHeldItem();
        if (stack != null && stack.getItem() != null) {
            Item item = stack.getItem();
            if (item.onEntitySwing(this, stack)) {
                return;
            }
        }
        if (!this.isSwingInProgress || this.swingProgressInt >= this.getArmSwingAnimationEnd() / 2 || this.swingProgressInt < 0) {
            this.swingProgressInt = -1;
            this.isSwingInProgress = true;

            if (this.worldObj instanceof WorldServer) {
                if (!isRemote()) {
                    NpcEvent.SwingEvent event = new NpcEvent.SwingEvent(wrappedNPC, stack);
                    if (EventHooks.onNPCMeleeSwing(this, event))
                        return;
                }

                ((WorldServer) this.worldObj).getEntityTracker().func_151247_a(this, new S0BPacketAnimation(this, 0));
            }
        }
    }

    @Override
    public void onLivingUpdate() {
        if (CustomNpcs.FreezeNPCs)
            return;
        totalTicksAlive++;
        this.updateArmSwingProgress();
        if (this.ticksExisted % 20 == 0)
            faction = getFaction();
        if (!worldObj.isRemote) {
            if (!isKilled() && this.ticksExisted % 20 == 0) {
                if (this.getHealth() < this.getMaxHealth()) {
                    if (stats.healthRegen > 0 && !isAttacking())
                        heal(stats.healthRegen);
                    if (stats.combatRegen > 0 && isAttacking())
                        heal(stats.combatRegen);
                }
                if (faction.getsAttacked && !isAttacking()) {
                    List<EntityMob> list = this.worldObj.getEntitiesWithinAABB(EntityMob.class, this.boundingBox.expand(16, 16, 16));
                    for (EntityMob mob : list) {
                        if (mob.getAttackTarget() == null && this.canSee(mob)) {
                            if (mob instanceof EntityZombie && !mob.getEntityData().hasKey("AttackNpcs")) {
                                mob.tasks.addTask(2, new EntityAIAttackOnCollide(mob, EntityLivingBase.class, 1.0D, false));
                                mob.getEntityData().setBoolean("AttackNpcs", true);
                            }
                            mob.setAttackTarget(this);
                        }
                    }
                }
                if (linkedData != null && linkedData.time > linkedLast) {
                    LinkedNpcController.Instance.loadNpcData(this);
                }
                if (updateClient) {
                    this.updateClient();
                }
                if (updateAI) {
                    updateTasks();
                    updateAI = false;
                }
            }
            if (getHealth() <= 0) {
                clearActivePotions();
                setBoolFlag(true, 8);
                updateTasks();
                updateHitbox();
            }
            setBoolFlag(!getNavigator().noPath(), 1);
            setBoolFlag(isInteracting(), 2);
            combatHandler.update();
            onCollide();

            actionManager.tick();
        }

        if (wasKilled != isKilled() && wasKilled) {
            reset();
        }

        wasKilled = isKilled();

        if (this.worldObj.isDaytime() && !this.worldObj.isRemote && this.stats.burnInSun) {
            float f = this.getBrightness(1.0F);

            if (f > 0.5F && this.rand.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && this.worldObj.canBlockSeeTheSky(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ))) {
                this.setFire(8);
            }
        }

        super.onLivingUpdate();

        handleMountRiderState();

        if (worldObj.isRemote) {
            if (roleInterface != null) {
                roleInterface.clientUpdate();
            }

            if (!display.cloakTexture.isEmpty())
                cloakUpdate();
            if (currentAnimation.ordinal() != dataWatcher.getWatchableObjectInt(14)) {
                currentAnimation = EnumAnimation.values()[dataWatcher.getWatchableObjectInt(14)];
                updateHitbox();
            }
            if (advanced.job == EnumJobType.Bard)
                ((JobBard) jobInterface).onLivingUpdate();

            DBCClient.Instance.renderDBCAuras(this);
        }
    }

    @Override
    protected void damageEntity(DamageSource damageSrc, float damageAmount) {
        super.damageEntity(damageSrc, damageAmount);
        combatHandler.damage(damageSrc, damageAmount);
    }

    public void updateClient() {
        NBTTagCompound compound = writeSpawnData();
        compound.setInteger("EntityId", getEntityId());
        PacketHandler.Instance.sendTracking(new UpdateNpcPacket(compound), this);
        updateClient = false;
    }

    @Override
    public boolean interact(EntityPlayer player) {
        if (worldObj.isRemote)
            return false;
        ItemStack currentItem = player.inventory.getCurrentItem();
        if (currentItem != null) {
            Item item = currentItem.getItem();
            if (item == CustomItems.cloner || item == CustomItems.wand || item == CustomItems.mount || item == CustomItems.scripter || item == CustomItems.soulstoneEmpty) {
                setAttackTarget(null);
                setRevengeTarget(null);
                return true;
            }
            if (item == CustomItems.moving) {
                setAttackTarget(null);
                if (currentItem.stackTagCompound == null)
                    currentItem.stackTagCompound = new NBTTagCompound();

                currentItem.stackTagCompound.setInteger("NPCID", this.getEntityId());
                player.addChatMessage(new ChatComponentTranslation("Registered " + this.getCommandSenderName() + " to your NPC Pather"));
                return true;
            }
        }

        if (EventHooks.onNPCInteract(this, player) || isAttacking() || isKilled() || faction.isAggressiveToPlayer(player))
            return false;

        addInteract(player);

        Dialog dialog = getDialog(player);
        PlayerQuestData playerdata = PlayerData.get(player).questData;
        QuestData data = playerdata.getQuestCompletion(player, this);
        Party partyCompleted = playerdata.getPartyQuestCompletion(player, this);
        if (partyCompleted != null) {
            NoppesUtilPlayer.questPartyCompletion(partyCompleted);
        } else if (data != null) {
            NoppesUtilPlayer.questCompletion((EntityPlayerMP) player, data.quest.id);
            QuestCompletionPacket.sendQuestComplete((EntityPlayerMP) player, data.quest.writeToNBT(new NBTTagCompound()));
        } else if (dialog != null) {
            NoppesUtilServer.openDialog(player, this, dialog, 0);
        } else if (roleInterface != null)
            roleInterface.interact(player);
        else
            say(player, advanced.getInteractLine());

        return true;
    }

    @Override
    public PathNavigate getNavigator() {
        if (canFly())
            return this.flyNavigator;
        else {
            return super.getNavigator();
        }
    }

    @Override
    public EntityMoveHelper getMoveHelper() {
        if (canFly())
            return this.flyMoveHelper;
        else {
            return super.getMoveHelper();
        }
    }

    public boolean canBreathe() {
        return this.isInWater() && this.stats.drowningType == 2 || !this.isInWater() && this.stats.drowningType == 1 || this.stats.drowningType == 0;
    }

    @Override
    protected void updateAITasks() {
        this.getNavigator().onUpdateNavigation();
        this.getMoveHelper().onUpdateMoveHelper();
        try {
            super.updateAITasks();
        } catch (ConcurrentModificationException ignored) {
        }

        if (!this.canBreathe()) {
            this.setAir(this.decreaseAirSupply(this.getAir()));

            if (this.getAir() <= -20) {
                this.setAir(0);
                this.attackEntityFrom(DamageSource.drown, 2.0F);
            }
        }
    }

    public void addInteract(EntityLivingBase entity) {
        if (!ais.stopAndInteract || isAttacking() || !entity.isEntityAlive())
            return;
        if ((ticksExisted - lastInteract) < 180)
            interactingEntities.clear();
        getNavigator().clearPathEntity();
        lastInteract = ticksExisted;
        if (!interactingEntities.contains(entity))
            interactingEntities.add(entity);
    }

    public boolean isInteracting() {
        if ((ticksExisted - lastInteract) < 40 || isRemote() && getBoolFlag(2))
            return true;
        return ais.stopAndInteract && !interactingEntities.isEmpty() && (ticksExisted - lastInteract) < 180;
    }

    private Dialog getDialog(EntityPlayer player) {
        for (DialogOption option : dialogs.values()) {
            if (option == null)
                continue;
            if (!option.hasDialog())
                continue;
            Dialog dialog = option.getDialog();
            if (dialog.availability.isAvailable(player)) {
                return dialog;
            }
        }
        return null;
    }

    @Override
    public boolean attackEntityFrom(DamageSource damagesource, float i) {
        if (this.worldObj.isRemote || CustomNpcs.FreezeNPCs || (damagesource.damageType != null && damagesource.damageType.equals("inWall"))) {
            return false;
        }
        if (damagesource.damageType != null && damagesource.damageType.equals("outOfWorld") && isKilled()) {
            reset();
        }

        if ((float) this.hurtResistantTime > (float) this.maxHurtResistantTime / 2.0F && i <= this.lastDamage)
            return false;

        Entity entity = NoppesUtilServer.GetDamageSource(damagesource);
        EntityLivingBase attackingEntity = null;

        if (entity instanceof EntityLivingBase)
            attackingEntity = (EntityLivingBase) entity;

        if (attackingEntity != null && attackingEntity == getOwner())
            return false;
        else if (attackingEntity instanceof EntityNPCInterface) {
            EntityNPCInterface npc = (EntityNPCInterface) attackingEntity;
            if (npc.faction.id == faction.id)
                return false;
            if (npc.getOwner() instanceof EntityPlayer)
                this.recentlyHit = 100;
        } else if (attackingEntity instanceof EntityPlayer && faction.isFriendlyToPlayer((EntityPlayer) attackingEntity))
            return false;

        // Attribute
        if (!DBCAddon.IsAvailable() && attackingEntity instanceof EntityPlayer)
            i = AttributeAttackUtil.calculateDamagePlayerToNPC((EntityPlayer) attackingEntity, this, i);

        //  Resistances
        i = stats.resistances.applyResistance(damagesource, i);

        NpcEvent.DamagedEvent event = new NpcEvent.DamagedEvent(this.wrappedNPC, attackingEntity, i, damagesource);
        if (EventHooks.onNPCDamaged(this, event) || isKilled())
            return false;
        i = event.getDamage();

        if (isKilled())
            return false;

        if (attackingEntity == null)
            return super.attackEntityFrom(damagesource, i);

        try {
            if (isAttacking()) {
                if (getAttackTarget() != null) {
                    if (ais.combatPolicy != EnumCombatPolicy.Brute) {
                        boolean closerTargetFound = this.getDistanceSqToEntity(getAttackTarget()) > this.getDistanceSqToEntity(attackingEntity);
                        switch (ais.combatPolicy) {
                            case Flip:
                                if (closerTargetFound) {
                                    setAttackTarget(attackingEntity);
                                }
                                break;
                            case Stubborn:
                                if (closerTargetFound && combatHandler.shouldChangeTarget(ais.tacticalChance)) {
                                    setAttackTarget(attackingEntity);
                                }
                                break;
                            case Tactical:
                                if (attackingEntity != getAttackTarget() && combatHandler.shouldSwitchTactically(getAttackTarget(), attackingEntity, ais.tacticalChance > 50)) {
                                    setAttackTarget(attackingEntity);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
                return super.attackEntityFrom(damagesource, i);
            }
            if (i > 0) {
                List<EntityNPCInterface> inRange = worldObj.getEntitiesWithinAABB(EntityNPCInterface.class, this.boundingBox.expand(32D, 16D, 32D));
                for (EntityNPCInterface npc : inRange) {
                    if (npc.isKilled() || !npc.advanced.defendFaction || npc.faction.id != faction.id)
                        continue;

                    if (npc.canSee(this) || npc.ais.directLOS || npc.canSee(attackingEntity))
                        npc.onAttack(attackingEntity);
                }
                setAttackTarget(attackingEntity);
            }
            return super.attackEntityFrom(damagesource, i);
        } finally {
            if (event.getClearTarget()) {
                setAttackTarget(null);
                setRevengeTarget(null);
            }
        }
    }

    public void onAttack(EntityLivingBase entity) {
        if (entity == null || entity == this || isAttacking() || ais.onAttack == 3 || entity == getOwner())
            return;
        super.setAttackTarget(entity);
    }

    @Override
    public void setAttackTarget(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.disableDamage || entity != null && entity == getOwner())
            return;
        if (entity instanceof EntityPlayer && DBCAddon.instance.isKO(this, (EntityPlayer) entity))
            return;
        if (!isRemote()) {
            if (getAttackTarget() != entity) {
                if (entity != null) {
                    NpcEvent.TargetEvent event = new NpcEvent.TargetEvent(wrappedNPC, entity);
                    if (EventHooks.onNPCTarget(this, event))
                        return;

                    if (event.getTarget() == null)
                        entity = null;
                    else
                        entity = event.getTarget().getMCEntity();
                }
                if (getAttackTarget() != null) {
                    if (EventHooks.onNPCTargetLost(this, getAttackTarget(), entity))
                        return;
                }
            }
            if (entity != null && entity != this && ais.onAttack != 3 && !isAttacking()) {
                Line line = advanced.getAttackLine();
                if (line != null)
                    saySurrounding(line.formatTarget(entity));
            }
        }

        super.setAttackTarget(entity);
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase entity, float f) {
        ItemStack proj = inventory.getProjectile();
        if (proj == null) {
            updateTasks();
            return;
        }
        if (!isRemote()) {
            NpcEvent.RangedLaunchedEvent event = new NpcEvent.RangedLaunchedEvent(wrappedNPC, stats.pDamage, entity);
            if (EventHooks.onNPCRangedAttack(this, event))
                return;
            for (int i = 0; i < this.stats.shotCount; i++) {
                EntityProjectile projectile = shoot(entity, stats.accuracy, proj, f == 1);
                projectile.damage = event.getDamage();
            }

            if (this.stats.playBurstSound || !this.stats.onSoundBegin) {
                this.playSound(this.stats.fireSound, 1.5F, 1.0f);
                this.stats.playBurstSound = false;
            }
        }
    }

    public EntityProjectile shoot(EntityLivingBase entity, int accuracy, ItemStack proj, boolean indirect) {
        return shoot(entity.posX, entity.boundingBox.minY + (double) (entity.height / 2.0F), entity.posZ, accuracy, proj, indirect);
    }

    public EntityProjectile shoot(double x, double y, double z, int accuracy, ItemStack proj, boolean indirect) {
        EntityProjectile projectile = new EntityProjectile(this.worldObj, this, proj.copy(), true);

        double throwerHeight = this.getEyeHeight();
        double varX = x - this.posX;
        double varY = y - (this.posY + throwerHeight);
        double varZ = z - this.posZ;
        float varF = projectile.hasGravity() ? MathHelper.sqrt_double(varX * varX + varZ * varZ) : 0.0F;
        float angle = projectile.getAngleForXYZ(varX, varY, varZ, varF, indirect);
        float acc = 20.0F - MathHelper.floor_float(accuracy / 5.0F);
        projectile.setThrowableHeading(varX, varY, varZ, angle, acc);
        worldObj.spawnEntityInWorld(projectile);
        return projectile;
    }

    private void clearTasks(EntityAITasks tasks) {
        Iterator iterator = tasks.taskEntries.iterator();
        List<EntityAITaskEntry> list = new ArrayList(tasks.taskEntries);
        for (EntityAITaskEntry entityaitaskentry : list) {
            try {
                tasks.removeTask(entityaitaskentry.action);
            } catch (Throwable e) {

            }
        }
        tasks.taskEntries = new ArrayList<EntityAITaskEntry>();
    }

    public void updateTasks() {
        if (worldObj == null || worldObj.isRemote)
            return;

        clearTasks(tasks);
        clearTasks(targetTasks);
        if (isKilled())
            return;

        if (!faction.isPassive()) {
            IEntitySelector attackEntitySelector = new NPCAttackSelector(this);
            this.targetTasks.addTask(0, new EntityAIClearTarget(this));
            this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
            this.targetTasks.addTask(2, new EntityAIClosestTarget(this, EntityLivingBase.class, 4, this.ais.directLOS, false, attackEntitySelector));
            this.targetTasks.addTask(3, new EntityAIOwnerHurtByTarget(this));
            this.targetTasks.addTask(4, new EntityAIOwnerHurtTarget(this));
        }

        if (canFly()) {
            this.getNavigator().setCanSwim(true);
        } else {
            this.tasks.addTask(0, new EntityAIWaterNav(this));
        }

        this.taskCount = 1;
        this.doorInteractType();
        this.seekShelter();
        this.setResponse();
        this.setMoveType();
        this.addRegularEntries();
    }

    private void removeTask(EntityAIBase task) {
        if (task != null)
            tasks.removeTask(task);
    }

    /*
     * Branch task function for setting how an NPC responds to a threat
     */
    public void setResponse() {
        removeTask(aiLeap);
        removeTask(aiResponse);
        removeTask(aiSprint);
        removeTask(aiAttackTarget);
        removeTask(aiRange);
        aiLeap = aiAttackTarget = aiResponse = aiSprint = aiRange = null;

        if (this.ais.canSprint)
            this.tasks.addTask(this.taskCount++, new EntityAISprintToTarget(this));

        if (this.ais.onAttack == 1)
            this.tasks.addTask(this.taskCount++, aiResponse = new EntityAIPanic(this, 1.2F));
        else if (this.ais.onAttack == 2) {
            this.tasks.addTask(this.taskCount++, aiResponse = new EntityAIAvoidTarget(this));
        } else if (this.ais.onAttack == 0 && !faction.isPassive()) {
            this.setLeapTask();
            if (this.inventory.getProjectile() == null || this.ais.useRangeMelee == 2) {
                switch (this.ais.tacticalVariant) {
                    case Dodge:
                        this.tasks.addTask(this.taskCount++, aiResponse = new EntityAIZigZagTarget(this, 1.2D, this.ais.tacticalRadius));
                        break;
                    case Surround:
                        this.tasks.addTask(this.taskCount++, aiResponse = new EntityAIOrbitTarget(this, 1.2D, this.ais.tacticalRadius, true));
                        break;
                    case HitNRun:
                        this.tasks.addTask(this.taskCount++, aiResponse = new EntityAIAvoidTarget(this));
                        break;
                    case Ambush:
                        this.tasks.addTask(this.taskCount++, aiResponse = new EntityAIAmbushTarget(this, 1.2D, this.ais.tacticalRadius, false));
                        break;
                    case Stalk:
                        this.tasks.addTask(this.taskCount++, aiResponse = new EntityAIStalkTarget(this, this.ais.tacticalRadius));
                        break;
                    default:
                }
            } else {
                switch (this.ais.tacticalVariant) {
                    case Dodge:
                        this.tasks.addTask(this.taskCount++, aiResponse = new EntityAIDodgeShoot(this));
                        break;
                    case Surround:
                        this.tasks.addTask(this.taskCount++, aiResponse = new EntityAIOrbitTarget(this, 1.2D, stats.rangedRange, false));
                        break;
                    case HitNRun:
                        this.tasks.addTask(this.taskCount++, aiResponse = new EntityAIAvoidTarget(this));
                        break;
                    case Ambush:
                        this.tasks.addTask(this.taskCount++, aiResponse = new EntityAIAmbushTarget(this, 1.2D, this.ais.tacticalRadius, false));
                        break;
                    case Stalk:
                        this.tasks.addTask(this.taskCount++, aiResponse = new EntityAIStalkTarget(this, this.ais.tacticalRadius));
                        break;
                    default:
                }
            }
            this.tasks.addTask(this.taskCount, aiAttackTarget = new EntityAIAttackTarget(this));
            ((EntityAIAttackTarget) aiAttackTarget).navOverride(ais.tacticalVariant == EnumNavType.None);

            if (this.inventory.getProjectile() != null) {
                this.tasks.addTask(this.taskCount++, aiRange = new EntityAIRangedAttack(this));
                aiRange.navOverride(ais.tacticalVariant == EnumNavType.None);
            }
        } else if (this.ais.onAttack == 3) {
            //do nothing
        }
    }

    /*
     * Branch task function for setting if an NPC wanders or not
     */
    public void setMoveType() {
        if (ais.movingType == EnumMovingType.Wandering) {
            this.tasks.addTask(this.taskCount++, new EntityAIWander(this));
        }
        if (ais.movingType == EnumMovingType.MovingPath) {
            this.tasks.addTask(this.taskCount++, new EntityAIMovingPath(this));
        }
    }

    /*
     * Branch task function for adjusting NPC door interactivity
     */
    public void doorInteractType() {
        if (canFly()) //currently flying does not support opening doors
            return;
        EntityAIBase aiDoor = null;
        if (this.ais.doorInteract == 1) {
            this.tasks.addTask(this.taskCount++, aiDoor = new EntityAIOpenDoor(this, true));
        } else if (this.ais.doorInteract == 0) {
            this.tasks.addTask(this.taskCount++, aiDoor = new EntityAIBustDoor(this));
        }
        this.getNavigator().setBreakDoors(aiDoor != null);
    }

    /*
     * Branch task function for finding shelter under the appropriate conditions
     */
    public void seekShelter() {
        if (this.ais.findShelter == 0) {
            this.tasks.addTask(this.taskCount++, new EntityAIMoveIndoors(this));
        } else if (this.ais.findShelter == 1) {
            if (!canFly()) // doesnt work when flying
                this.tasks.addTask(this.taskCount++, new EntityAIRestrictSun(this));
            this.tasks.addTask(this.taskCount++, new EntityAIFindShade(this));
        }
    }

    /*
     * Branch task function for leaping
     */
    public void setLeapTask() {
        if (this.ais.leapType == 1)
            this.tasks.addTask(this.taskCount++, aiLeap = new EntityAILeapAtTargetNpc(this, 0.4F));
        if (this.ais.leapType == 2)
            this.tasks.addTask(this.taskCount++, aiLeap = new EntityAIPounceTarget(this));
    }

    /*
     * Add immutable task entries.
     */
    public void addRegularEntries() {
        this.tasks.addTask(this.taskCount++, new EntityAIFollow(this));
        this.tasks.addTask(this.taskCount++, new EntityAIReturn(this));
        if (this.ais.standingType != EnumStandingType.NoRotation && this.ais.standingType != EnumStandingType.HeadRotation)
            this.tasks.addTask(this.taskCount++, new EntityAIWatchClosest(this, EntityLivingBase.class, 5.0F));
        this.tasks.addTask(this.taskCount++, new EntityAILook(this));
        this.tasks.addTask(this.taskCount++, new EntityAIWorldLines(this));
        this.tasks.addTask(this.taskCount++, new EntityAIJob(this));
        this.tasks.addTask(this.taskCount++, new EntityAIRole(this));
        this.tasks.addTask(this.taskCount++, new EntityAIAnimation(this));
        if (transform.isValid())
            this.tasks.addTask(this.taskCount++, new EntityAITransform(this));
    }

    /*
     * Function for getting proper move speeds. This way we don't have to modify them every time we use them.
     */
    public float getSpeed() {
        return (float) ais.getWalkingSpeed() / 20.0F;
    }

    @Override
    public float getBlockPathWeight(int par1, int par2, int par3) {
        float weight = this.worldObj.getLightBrightness(par1, par2, par3) - 0.5F;
        Block block = worldObj.getBlock(par1, par2, par3);
        if (block.isOpaqueCube())
            weight += 10;
        return weight;
    }

    /*
     * Used for getting the applied potion effect from dataStats.
     */
    private int getPotionEffect(EnumPotionType p) {
        switch (p) {
            case Poison:
                return Potion.poison.id;
            case Hunger:
                return Potion.hunger.id;
            case Weakness:
                return Potion.weakness.id;
            case Slowness:
                return Potion.moveSlowdown.id;
            case Nausea:
                return Potion.confusion.id;
            case Blindness:
                return Potion.blindness.id;
            case Wither:
                return Potion.wither.id;
            default:
                return 0;
        }
    }

    @Override
    public void setAir(int air) {
        if (this.isInWater() || air < this.getAir() || this.stats.drowningType != 2) {
            super.setAir(air);
        }
    }

    @Override
    protected int decreaseAirSupply(int par1) {
        if (this.stats.drowningType == 0)
            return par1;
        return super.decreaseAirSupply(par1);
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return this.stats.creatureType;
    }

    /**
     * Returns the sound this mob makes while it's alive.
     */
    @Override
    protected String getLivingSound() {
        if (!this.isEntityAlive())
            return null;
        if (this.getAttackTarget() != null)
            return advanced.angrySound.isEmpty() ? null : advanced.angrySound;

        return advanced.idleSound.isEmpty() ? null : advanced.idleSound;
    }

    @Override
    public int getTalkInterval() {
        return 160;
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    @Override
    protected String getHurtSound() {
        if (this.advanced.hurtSound.isEmpty())
            return null;
        return this.advanced.hurtSound;
    }

    /**
     * Returns the sound this mob makes on death.
     */
    @Override
    protected String getDeathSound() {
        if (this.advanced.deathSound.isEmpty())
            return null;
        return this.advanced.deathSound;
    }

    @Override
    protected float getSoundPitch() {
        if (this.advanced.disablePitch)
            return 1;
        return super.getSoundPitch();
    }

    /**
     * Plays step sound at given x, y, z for the entity
     */
    @Override
    protected void func_145780_a(int p_145780_1_, int p_145780_2_, int p_145780_3_, Block p_145780_4_) {
        if (!this.advanced.stepSound.equals("")) {
            this.playSound(this.advanced.stepSound, 0.15F, 1.0F);
        } else {
            super.func_145780_a(p_145780_1_, p_145780_2_, p_145780_3_, p_145780_4_);
        }
    }

    public EntityPlayerMP getFakePlayer() {
        if (worldObj.isRemote)
            return null;
        if (chateventPlayer == null)
            chateventPlayer = new FakePlayer((WorldServer) worldObj, chateventProfile);
        EntityUtil.Copy(this, chateventPlayer);
        chateventProfile.npc = this;
        chateventPlayer.refreshDisplayName();
        IItemStack stack = NpcAPI.Instance().createItem("minecraft:stone", 0, 1);
        chateventPlayer.setCurrentItemOrArmor(0, stack.getMCItemStack());
        return chateventPlayer;
    }

    public void saySurrounding(ILine line) {
        if (line == null || line.getText() == null || getFakePlayer() == null)
            return;
        ServerChatEvent event = new ServerChatEvent(getFakePlayer(), line.getText(), new ChatComponentTranslation(line.getText().replace("%", "%%")));
        if (MinecraftForge.EVENT_BUS.post(event) || event.component == null) {
            return;
        }
        line.setText(event.component.getUnformattedText().replace("%%", "%"));
        List<EntityPlayer> inRange = worldObj.getEntitiesWithinAABB(EntityPlayer.class, this.boundingBox.expand(20D, 20D, 20D));
        for (EntityPlayer player : inRange)
            say(player, line);
    }

    public void say(EntityPlayer player, ILine line) {
        if (line == null || !this.canSee(player) || line.getText() == null)
            return;

        if (!line.getSound().isEmpty()) {
            PacketHandler.Instance.sendToPlayer(new SoundManagementPacket(EnumSoundOperation.PLAY_SOUND, line.getSound(), (float) posX, (float) posY, (float) posZ), (EntityPlayerMP) player);
        }
        PacketHandler.Instance.sendToPlayer(new ChatBubblePacket(this.getEntityId(), line.getText(), !line.hideText()), (EntityPlayerMP) player);
    }

    public boolean getAlwaysRenderNameTagForRender() {
        return true;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        npcVersion = compound.getInteger("ModRev");
        VersionCompatibility.CheckNpcCompatibility(this, compound);

        display.readToNBT(compound);
        stats.readToNBT(compound);
        ais.readToNBT(compound);
        script.readFromNBT(compound);
        script.readEventsFromNBT(compound);
        timers.readFromNBT(compound);
        advanced.readToNBT(compound);
        if (advanced.role != EnumRoleType.None && roleInterface != null)
            roleInterface.readFromNBT(compound);
        if (advanced.job != EnumJobType.None && jobInterface != null)
            jobInterface.readFromNBT(compound);

        inventory.readEntityFromNBT(compound);
        transform.readToNBT(compound);

        killedtime = compound.getLong("KilledTime");
        totalTicksAlive = compound.getLong("TotalTicksAlive");

        linkedName = compound.getString("LinkedNpcName");

        if (!isRemote())
            LinkedNpcController.Instance.loadNpcData(this);

        this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(ConfigMain.NpcNavRange);

        this.updateTasks();
        this.func_110163_bv();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        display.writeToNBT(compound);
        stats.writeToNBT(compound);
        ais.writeToNBT(compound);
        script.writeToNBT(compound);
        script.writeEventsToNBT(compound);
        timers.writeToNBT(compound);
        advanced.writeToNBT(compound);
        if (advanced.role != EnumRoleType.None && roleInterface != null)
            roleInterface.writeToNBT(compound);
        if (advanced.job != EnumJobType.None && jobInterface != null)
            jobInterface.writeToNBT(compound);

        inventory.writeEntityToNBT(compound);
        transform.writeToNBT(compound);

        compound.setLong("KilledTime", killedtime);
        compound.setLong("TotalTicksAlive", totalTicksAlive);
        compound.setInteger("ModRev", npcVersion);
        compound.setString("LinkedNpcName", linkedName);
    }

    public void updateHitbox() {

        float newWidth;
        float newHeight;
        if (currentAnimation == EnumAnimation.LYING || currentAnimation == EnumAnimation.CRAWLING) {
            newWidth = 0.8f;
            newHeight = 0.4f;
        } else if (isRiding()) {
            newWidth = 0.6f;
            newHeight = baseHeight * 0.77f;
        } else {
            newWidth = 0.6f;
            newHeight = baseHeight;
        }
        newWidth = (newWidth / 5f) * display.modelSize;
        newHeight = (newHeight / 5f) * display.modelSize;

        if (display.hitboxData.isHitboxEnabled()) {
            newWidth = newWidth * display.hitboxData.getWidthScale();
            newHeight = newHeight * display.hitboxData.getHeightScale();
        }

        if (isKilled() && stats.hideKilledBody) {
            newWidth = 0.00001f;
        }

        newWidth = Math.max(newWidth, 0.00001f);
        newHeight = Math.max(newHeight, 0.00001f);

        if (newWidth / 2 > World.MAX_ENTITY_RADIUS) {
            World.MAX_ENTITY_RADIUS = newWidth / 2;
        }

        setSize(newWidth, newHeight);
        this.setPosition(posX, posY, posZ);
    }

    @Override
    public void onDeathUpdate() {
        if (stats.spawnCycle == 3) {
            super.onDeathUpdate();
            return;
        }

        ++this.deathTime;
        if (worldObj.isRemote)
            return;
        if (!hasDied) {
            setDead();
        }
        if (killedtime < System.currentTimeMillis()) {
            if (stats.spawnCycle == 0 || (this.worldObj.isDaytime() && stats.spawnCycle == 1) || (!this.worldObj.isDaytime() && stats.spawnCycle == 2)) {
                reset();
            }
        }
    }

    public void reset() {
        hasDied = false;
        isDead = false;
        wasKilled = false;
        setSprinting(false);
        setHealth(getMaxHealth());
        dataWatcher.updateObject(14, 0); // animation Normal
        dataWatcher.updateObject(15, 0);
        if (ConfigScript.ClearActionsOnDeath)
            actionManager.clear();
        combatHandler.reset();
        this.setAttackTarget(null);
        this.setRevengeTarget(null);
        this.deathTime = 0;
        //fleeingTick = 0;
        if (ais.returnToStart && !hasOwner() && !this.isRemote())
            setLocationAndAngles(getStartXPos(), getStartYPos(), getStartZPos(), rotationYaw, rotationPitch);
        killedtime = 0;
        extinguish();
        this.clearActivePotions();
        getNavigator().clearPathEntity();
        if (this.canFly()) {
            ((PathNavigateFlying) getNavigator()).targetPos = null;
            getNavigator().clearPathEntity();
            ((FlyingMoveHelper) getMoveHelper()).update = false;
        }
        moveEntityWithHeading(0, 0);
        distanceWalkedModified = 0;
        currentAnimation = EnumAnimation.NONE;
        updateHitbox();
        updateAI = true;
        ais.movingPos = 0;
        if (getOwner() != null) {
            getOwner().setLastAttacker(null);
        }

        if (jobInterface != null)
            jobInterface.reset();

        if (!isRemote()) {
            EventHooks.onNPCInit(this);
        }
    }

    public void onCollide() {
        if (!isEntityAlive() || ticksExisted % 4 != 0)
            return;

        AxisAlignedBB axisalignedbb = null;

        if (this.ridingEntity != null && this.ridingEntity.isEntityAlive()) {
            axisalignedbb = this.boundingBox.func_111270_a(this.ridingEntity.boundingBox).expand(1.0D, 0.0D, 1.0D);
        } else {
            axisalignedbb = this.boundingBox.expand(1.0D, 0.5D, 1.0D);
        }

        List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, axisalignedbb);
        if (list == null)
            return;

        if (!isRemote()) {
            for (int i = 0; i < list.size(); ++i) {
                Entity entity = (Entity) list.get(i);
                if (entity.isEntityAlive()) {
                    EventHooks.onNPCCollide(this, entity);
                }
            }
        }
    }

    @Override
    public void setInPortal() {
        //prevent npcs from walking into portals
    }

    public double field_20066_r;
    public double field_20065_s;
    public double field_20064_t;
    public double field_20063_u;
    public double field_20062_v;
    public double field_20061_w;

    public void cloakUpdate() {
        field_20066_r = field_20063_u;
        field_20065_s = field_20062_v;
        field_20064_t = field_20061_w;
        double d = posX - field_20063_u;
        double d1 = posY - field_20062_v;
        double d2 = posZ - field_20061_w;
        double d3 = 10D;
        if (d > d3) {
            field_20066_r = field_20063_u = posX;
        }
        if (d2 > d3) {
            field_20064_t = field_20061_w = posZ;
        }
        if (d1 > d3) {
            field_20065_s = field_20062_v = posY;
        }
        if (d < -d3) {
            field_20066_r = field_20063_u = posX;
        }
        if (d2 < -d3) {
            field_20064_t = field_20061_w = posZ;
        }
        if (d1 < -d3) {
            field_20065_s = field_20062_v = posY;
        }
        field_20063_u += d * 0.25D;
        field_20061_w += d2 * 0.25D;
        field_20062_v += d1 * 0.25D;
    }

    @Override
    protected boolean canDespawn() {
        return stats.canDespawn;
    }

    @Override
    public ItemStack getHeldItem() {
        if (isAttacking())
            return inventory.getWeapon();
        else if (advanced.role == EnumRoleType.Companion)
            return ((RoleCompanion) roleInterface).getHeldItem();
        else if (jobInterface != null && jobInterface.overrideMainHand)
            return jobInterface.mainhand;
        else
            return inventory.getWeapon();
    }

    @Override
    public ItemStack getEquipmentInSlot(int slot) {
        if (slot == 0)
            return inventory.weapons.get(0);
        return inventory.armorItemInSlot(4 - slot);
    }

    @Override
    public ItemStack func_130225_q(int slot) {
        return inventory.armorItemInSlot(3 - slot);
    }

    @Override
    public void setCurrentItemOrArmor(int slot, ItemStack item) {
        if (slot == 0)
            inventory.setWeapon(item);
        else {
            inventory.armor.put(4 - slot, item);
        }
    }

    private static final ItemStack[] lastActive = new ItemStack[5];

    @Override
    public ItemStack[] getLastActiveItems() {
        return lastActive;
    }

    @Override
    protected void dropEquipment(boolean p_82160_1_, int p_82160_2_) {

    }

    public ItemStack getOffHand() {
        if (isAttacking())
            return inventory.getOffHand();
        else if (jobInterface != null && jobInterface.overrideOffHand)
            return jobInterface.offhand;
        else
            return inventory.getOffHand();
    }

    @Override
    public void onDeath(DamageSource damagesource) {
        setSprinting(false);
        getNavigator().clearPathEntity();
        extinguish();
        clearActivePotions();

        Entity entity = damagesource.getEntity();

        EntityLivingBase attackingEntity = null;

        if (entity instanceof EntityLivingBase)
            attackingEntity = (EntityLivingBase) entity;

        if ((entity instanceof EntityArrow) && ((EntityArrow) entity).shootingEntity instanceof EntityLivingBase)
            attackingEntity = (EntityLivingBase) ((EntityArrow) entity).shootingEntity;
        else if ((entity instanceof EntityThrowable))
            attackingEntity = ((EntityThrowable) entity).getThrower();


        int droppedXp = inventory.getDroppedXp();
        ArrayList<ItemStack> droppedItems = inventory.getDroppedItems(damagesource);

        if (!isRemote()) {
            NpcEvent.DiedEvent event = new NpcEvent.DiedEvent(this.wrappedNPC, damagesource, entity, droppedItems, droppedXp);
            if (EventHooks.onNPCKilled(this, event))
                return;

            droppedItems.clear();
            for (IItemStack iItemStack : event.droppedItems) {
                droppedItems.add(iItemStack.getMCItemStack());
            }
            droppedXp = event.expDropped;

            if (this.recentlyHit > 0) {
                inventory.dropItems(entity, droppedItems);
                inventory.dropXp(entity, droppedXp);
            }
            Line line = advanced.getKilledLine();
            if (line != null)
                saySurrounding(line.formatTarget(attackingEntity));
        }
        super.onDeath(damagesource);
    }

    @Override
    public void setDead() {
        hasDied = true;
        if (worldObj.isRemote || stats.spawnCycle == 3) {
            this.spawnExplosionParticle();
            delete();
        } else {
            if (this.riddenByEntity != null) {
                NPCMountUtil.stabilizeDismountedRider(this.riddenByEntity);
                this.riddenByEntity.mountEntity(null);
                NPCMountUtil.haltMountedMotion(this, mountState);
            }
            if (this.ridingEntity != null)
                this.mountEntity(null);
            mountState.lastRider = null;
            NPCMountUtil.resetMountedFlightState(this, mountState);
            setHealth(-1);
            setSprinting(false);
            getNavigator().clearPathEntity();
            if (killedtime <= 0)
                killedtime = stats.respawnTime * 1000L + System.currentTimeMillis();

            if (advanced.role != EnumRoleType.None && roleInterface != null)
                roleInterface.killed();
            if (advanced.job != EnumJobType.None && jobInterface != null)
                jobInterface.killed();
        }
    }

    public void delete() {
        if (advanced.role != EnumRoleType.None && roleInterface != null)
            roleInterface.delete();
        if (advanced.job != EnumJobType.None && jobInterface != null)
            jobInterface.delete();
        actionManager.clear();

        super.setDead();
    }

    public float getStartXPos() {

        return getStartPos()[0] + ais.bodyOffsetX / 10;
    }

    public float getStartZPos() {
        return getStartPos()[2] + ais.bodyOffsetZ / 10;
    }

    public int[] getStartPos() {
        if (ais.startPos == null || ais.startPos.length != 3)
            ais.startPos = new int[]{MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ)};
        return ais.startPos;
    }

    public boolean isVeryNearAssignedPlace() {
        double xx = posX - getStartXPos();
        double zz = posZ - getStartZPos();
        if (xx < -0.2 || xx > 0.2)
            return false;
        return !(zz < -0.2) && !(zz > 0.2);
    }

    @Override
    public IIcon getItemIcon(ItemStack par1ItemStack, int par2) {
        if (par1ItemStack.getItem() instanceof ItemBow) {
            return par1ItemStack.getItem().getIcon(par1ItemStack, par2);
        }
        EntityPlayer player = CustomNpcs.proxy.getPlayer();
        if (player == null)
            return super.getItemIcon(par1ItemStack, par2);
        return player.getItemIcon(par1ItemStack, par2);
    }

    public double getStartYPos() {
        int i = getStartPos()[0];
        int j = getStartPos()[1];
        int k = getStartPos()[2];
        double yy = 0;
        for (int ii = j; ii >= 0; ii--) {
            if (this.canFly()) {
                if (ii < j - 1) {
                    yy = j;
                    break;
                }
                Block block = worldObj.getBlock(i, ii, k);
                AxisAlignedBB bb = block.getCollisionBoundingBoxFromPool(worldObj, i, ii, k);
                if (bb != null) {
                    yy = bb.maxY;
                    break;
                }
            } else {
                Block block = worldObj.getBlock(i, ii, k);
                if (block == null || block == Blocks.air)
                    continue;
                AxisAlignedBB bb = block.getCollisionBoundingBoxFromPool(worldObj, i, ii, k);
                if (bb == null)
                    continue;
                yy = bb.maxY;
                break;
            }
        }
        if (yy <= 0)
            setDead();
        return yy;
    }

    public void givePlayerItem(EntityPlayer player, ItemStack item) {
        if (worldObj.isRemote) {
            return;
        }
        item = item.copy();
        float f = 0.7F;
        double d = (double) (worldObj.rand.nextFloat() * f) + (double) (1.0F - f);
        double d1 = (double) (worldObj.rand.nextFloat() * f) + (double) (1.0F - f);
        double d2 = (double) (worldObj.rand.nextFloat() * f) + (double) (1.0F - f);
        EntityItem entityitem = new EntityItem(worldObj, posX + d, posY + d1, posZ + d2, item);
        entityitem.delayBeforeCanPickup = 2;
        worldObj.spawnEntityInWorld(entityitem);

        int i = item.stackSize;

        if (player.inventory.addItemStackToInventory(item)) {
            worldObj.playSoundAtEntity(entityitem, "random.pop", 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            player.onItemPickup(entityitem, i);

            if (item.stackSize <= 0) {
                entityitem.setDead();
            }
        }
    }

    @Override
    public boolean isPlayerSleeping() {
        return currentAnimation == EnumAnimation.LYING && !isAttacking();
    }

    @Override
    public boolean isRiding() {
        return currentAnimation == EnumAnimation.SITTING && !isAttacking() || ridingEntity != null;
    }

    public boolean isWalking() {
        return ais.movingType != EnumMovingType.Standing || isAttacking() || isFollower() || getBoolFlag(1);
    }

    public boolean isFlying() {
        return isNpcFlying();
    }

    public boolean isNpcFlying() {
        return (dataWatcher.getWatchableObjectByte(17) & NPC_STATE_FLAG_FLYING) != 0;
    }

    public boolean isNpcJumping() {
        return (dataWatcher.getWatchableObjectByte(17) & NPC_STATE_FLAG_JUMPING) != 0;
    }

    private void setNpcStateFlag(int mask, boolean value) {
        byte current = dataWatcher.getWatchableObjectByte(17);
        byte updated = value ? (byte) (current | mask) : (byte) (current & ~mask);
        if (current != updated) {
            dataWatcher.updateObject(17, updated);
        }
    }

    public void setNpcFlyingState(boolean flying) {
        setNpcStateFlag(NPC_STATE_FLAG_FLYING, flying);
    }

    public void setNpcJumpingState(boolean jumping) {
        setNpcStateFlag(NPC_STATE_FLAG_JUMPING, jumping);
        this.isJumping = jumping;
    }

    public void setBoolFlag(boolean bo, int id) {
        int i = dataWatcher.getWatchableObjectInt(15);
        if (bo && (i & id) == 0)
            dataWatcher.updateObject(15, i | id);
        if (!bo && (i & id) != 0)
            dataWatcher.updateObject(15, i - id);
    }

    /**
     * 1: walking, 2:interacting, 4:attacking, 8:killed
     */
    public boolean getBoolFlag(int id) {
        return (dataWatcher.getWatchableObjectInt(15) & id) != 0;
    }

    @Override
    public boolean isSneaking() {
        return currentAnimation == EnumAnimation.SNEAKING;
    }

    @Override
    public void knockBack(Entity par1Entity, float par2, double par3, double par5) {
        if (stats.resistances.knockback >= 2)
            return;
        this.isAirBorne = true;
        float f1 = MathHelper.sqrt_double(par3 * par3 + par5 * par5);
        float f2 = 0.5F * (2 - stats.resistances.knockback);
        this.motionX /= 2.0D;
        this.motionY /= 2.0D;
        this.motionZ /= 2.0D;
        this.motionX -= par3 / (double) f1 * (double) f2;
        this.motionY += 0.2 + f2 / 2;
        this.motionZ -= par5 / (double) f1 * (double) f2;

        if (this.motionY > 0.4000000059604645D) {
            this.motionY = 0.4000000059604645D;
        }
    }

    @Override
    public void addVelocity(double p_70024_1_, double p_70024_3_, double p_70024_5_) {
        if (this.attackingPlayer != null) {
            float f2 = 0.5F * (2 - stats.resistances.knockback);
            super.addVelocity(p_70024_1_ * (double) f2, p_70024_3_ * (double) f2, p_70024_5_ * (double) f2);
        } else {
            super.addVelocity(p_70024_1_, p_70024_3_, p_70024_5_);
        }
    }

    public Faction getFaction() {
        String[] split = dataWatcher.getWatchableObjectString(13).split(":");
        int faction = 0;
        if (worldObj == null || split.length <= 1 && worldObj.isRemote)
            return new Faction();
        if (split.length > 1)
            faction = Integer.parseInt(split[0]);
        if (worldObj.isRemote) {
            Faction fac = new Faction();
            fac.id = faction;
            fac.color = Integer.parseInt(split[1]);
            fac.name = split[2];
            return fac;
        } else {
            Faction fac = FactionController.getInstance().get(faction);
            if (fac == null) {
                faction = FactionController.getInstance().getFirstFactionId();
                fac = FactionController.getInstance().get(faction);
            }
            return fac;
        }
    }

    public boolean isRemote() {
        return worldObj == null || worldObj.isRemote;
    }

    public void setFaction(int integer) {
        if (integer < 0 || isRemote())
            return;
        Faction faction = FactionController.getInstance().get(integer);
        if (faction == null)
            return;
        String str = faction.id + ":" + faction.color + ":" + faction.name;
        if (str.length() > 64)
            str = str.substring(0, 64);
        dataWatcher.updateObject(13, str);
    }

    @Override
    public boolean isPotionApplicable(PotionEffect effect) {
        if (stats.potionImmune)
            return false;
        if (getCreatureAttribute() == EnumCreatureAttribute.ARTHROPOD && effect.getPotionID() == Potion.poison.id)
            return false;
        return super.isPotionApplicable(effect);
    }

    public boolean isAttacking() {
        return getBoolFlag(4);
    }

    public boolean isKilled() {
        return getBoolFlag(8) || isDead;
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        try {
            ByteBufUtils.writeNBT(buffer, writeSpawnData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public NBTTagCompound writeSpawnData() {
        NBTTagCompound compound = new NBTTagCompound();
        display.writeToNBT(compound);
        compound.setDouble("MaxHealth", stats.maxHealth);
        compound.setTag("Armor", NBTTags.nbtItemStackList(inventory.getArmor()));
        compound.setTag("Weapons", NBTTags.nbtItemStackList(inventory.getWeapons()));
        compound.setInteger("Speed", ais.getWalkingSpeed());
        compound.setBoolean("DeadBody", stats.hideKilledBody);
        compound.setInteger("StandingState", ais.standingType.ordinal());
        compound.setInteger("MovingState", ais.movingType.ordinal());
        compound.setInteger("Orientation", ais.orientation);
        compound.setFloat("OffsetY", ais.bodyOffsetY);
        compound.setInteger("Role", advanced.role.ordinal());
        compound.setInteger("Job", advanced.job.ordinal());
        if (advanced.job == EnumJobType.Bard) {
            NBTTagCompound bard = new NBTTagCompound();
            jobInterface.writeToNBT(bard);
            compound.setTag("Bard", bard);
        }
        if (advanced.role == EnumRoleType.Companion) {
            NBTTagCompound bard = new NBTTagCompound();
            roleInterface.writeToNBT(bard);
            compound.setTag("Companion", bard);
        }
        if (advanced.role == EnumRoleType.Mount) {
            NBTTagCompound mount = new NBTTagCompound();
            roleInterface.writeToNBT(mount);
            compound.setTag("Mount", mount);
        }

        if (this instanceof EntityCustomNpc) {
            compound.setTag("ModelData", ((EntityCustomNpc) this).modelData.writeToNBT());
        }
        return compound;
    }

    @Override
    public void readSpawnData(ByteBuf buf) {
        try {
            readSpawnData(ByteBufUtils.readNBT(buf));
        } catch (IOException e) {
        }
    }

    public void readSpawnData(NBTTagCompound compound) {
        stats.maxHealth = compound.getDouble("MaxHealth");
        ais.setWalkingSpeed(compound.getInteger("Speed"));
        stats.hideKilledBody = compound.getBoolean("DeadBody");
        ais.standingType = EnumStandingType.values()[compound.getInteger("StandingState") % EnumStandingType.values().length];
        ais.movingType = EnumMovingType.values()[compound.getInteger("MovingState") % EnumMovingType.values().length];
        ais.orientation = compound.getInteger("Orientation");
        ais.bodyOffsetY = compound.getFloat("OffsetY");

        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(stats.maxHealth);
        inventory.setArmor(NBTTags.getItemStackList(compound.getTagList("Armor", 10)));
        inventory.setWeapons(NBTTags.getItemStackList(compound.getTagList("Weapons", 10)));
        advanced.setRole(compound.getInteger("Role"));
        advanced.setJob(compound.getInteger("Job"));
        if (advanced.job == EnumJobType.Bard) {
            NBTTagCompound bard = compound.getCompoundTag("Bard");
            jobInterface.readFromNBT(bard);
        }
        if (advanced.role == EnumRoleType.Companion) {
            NBTTagCompound companion = compound.getCompoundTag("Companion");
            roleInterface.readFromNBT(companion);
        }
        if (advanced.role == EnumRoleType.Mount) {
            NBTTagCompound mount = compound.getCompoundTag("Mount");
            roleInterface.readFromNBT(mount);
        }
        if (this instanceof EntityCustomNpc) {
            ((EntityCustomNpc) this).modelData.readFromNBT(compound.getCompoundTag("ModelData"));
        }
        display.readToNBT(compound);
    }

    @Override
    public String getCommandSenderName() {
        return display.name;
    }

    @Override
    public boolean canCommandSenderUseCommand(int var1, String var2) {
        if (ConfigMain.NpcUseOpCommands)
            return true;
        return var1 <= 2;
    }

    @Override
    public ChunkCoordinates getPlayerCoordinates() {
        return new ChunkCoordinates(MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ));
    }

    @Override
    public boolean canAttackClass(Class par1Class) {
        return EntityBat.class != par1Class;
    }

    public void setImmuneToFire(boolean immuneToFire) {
        this.isImmuneToFire = immuneToFire;
        stats.immuneToFire = immuneToFire;
    }

    public boolean handleLavaMovement() {
        return !stats.immuneToFire && super.handleLavaMovement();
    }

    public boolean handleWaterMovement() {
        if (stats.drowningType != 1) {
            if (this.worldObj.handleMaterialAcceleration(this.boundingBox.expand(0.0D, -this.height / 2.0D, 0.0D).contract(0.001D, 0.001D, 0.001D), Material.water, this)) {
                this.fallDistance = 0.0F;
                this.inWater = true;
                this.setFire(0);
            } else {
                this.inWater = false;
            }
            return false;
        } else {
            return super.handleWaterMovement();
        }
    }

    public boolean canBreatheUnderwater() {
        return stats.drowningType != 1;
    }

    public void setAvoidWater(boolean avoidWater) {
        this.getNavigator().setAvoidsWater(avoidWater);
        ais.avoidsWater = avoidWater;
    }

    @Override
    protected void fall(float distance) {
        if (NPCMountUtil.isFlyingMountWithFlightEnabled(this)) {
            return;
        }
        if (!this.stats.noFallDamage)
            super.fall(distance);
    }

    @Override
    public void setInWeb() {
        if (!stats.ignoreCobweb)
            super.setInWeb();
    }

    public boolean isInRange(Entity entity, double range) {
        return this.isInRange(entity.posX, entity.posY, entity.posZ, range);
    }

    public boolean isInRange(double posX, double posY, double posZ, double range) {
        double y = Math.abs(this.posY - posY);
        if (posY >= 0 && y > range)
            return false;

        double x = Math.abs(this.posX - posX);
        double z = Math.abs(this.posZ - posZ);

        return x <= range && z <= range;
    }

    @Override
    public boolean canBeCollidedWith() {
        return !isKilled();
    }

    @Override
    public boolean canBePushed() {
        return this.stats.collidesWith == 0;
    }

    // checks for any entity within a certain boundingbox, eg minecarts
    @Override
    protected void collideWithNearbyEntities() {
        if (this.stats.collidesWith != 1) {
            List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(0.20000000298023224D, 0.0D, 0.20000000298023224D));

            if (list != null && !list.isEmpty()) {
                for (int i = 0; i < list.size(); ++i) {
                    Entity entity = (Entity) list.get(i);

                    if (this.canBePushed() || (entity instanceof EntityNPCInterface && (this.stats.collidesWith == 2 || this.stats.collidesWith == 4)) || (entity instanceof EntityPlayerMP && (this.stats.collidesWith == 3 || this.stats.collidesWith == 4))) {
                        super.collideWithEntity(entity);
                    }
                }
            }
        }
    }

    public void applyEntityCollision(Entity entity) {
        if (entity.riddenByEntity != this && entity.ridingEntity != this) {
            double d0 = entity.posX - this.posX;
            double d1 = entity.posZ - this.posZ;
            double d2 = MathHelper.abs_max(d0, d1);

            if (d2 >= 0.009999999776482582D) {
                d2 = MathHelper.sqrt_double(d2);
                d0 /= d2;
                d1 /= d2;
                double d3 = 1.0D / d2;

                if (d3 > 1.0D) {
                    d3 = 1.0D;
                }

                d0 *= d3;
                d1 *= d3;
                d0 *= 0.05000000074505806D;
                d1 *= 0.05000000074505806D;
                d0 *= 1.0F - this.entityCollisionReduction;
                d1 *= 1.0F - this.entityCollisionReduction;
                if (this.canBePushed() || (entity instanceof EntityNPCInterface && (this.stats.collidesWith == 2 || this.stats.collidesWith == 4)) || (entity instanceof EntityPlayerMP && (this.stats.collidesWith == 3 || this.stats.collidesWith == 4)))
                    this.addVelocity(-d0, 0.0D, -d1);
                entity.addVelocity(d0, 0.0D, d1);
            }
        }
    }

    // not any entity can collide. if you want projectiles to still be
    // effective, you will have to handle those yourself
    @Override
    protected void collideWithEntity(Entity p_82167_1_) {

    }

    public boolean canFly() {
        return false;
    }

    public EntityAIRangedAttack getRangedTask() {
        return this.aiRange;
    }

    public String getRoleDataWatcher() {
        return dataWatcher.getWatchableObjectString(16);
    }

    public void setRoleDataWatcher(String s) {
        dataWatcher.updateObject(16, s);
    }

    @Override
    public World getEntityWorld() {
        return worldObj;
    }

    @Override
    public boolean isInvisibleToPlayer(EntityPlayer player) {
        return (scriptInvisibleToPlayer(player) || display.visible == 1) && (player.getHeldItem() == null || player.getHeldItem().getItem() != CustomItems.wand);
    }

    public boolean scriptInvisibleToPlayer(EntityPlayer player) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            return isInvisibleToClient(player);
        } else {
            return display.invisibleToList != null && display.invisibleToList.contains(player.getPersistentID());
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean isInvisibleToClient(EntityPlayer player) {
        return (player == Minecraft.getMinecraft().thePlayer ? display.isInvisibleToMe : display.getTempScriptInvisible(player.getEntityId()));
    }

    @Override
    public boolean isInvisible() {
        return display.visible != 0;
    }

    @Override
    public void addChatMessage(IChatComponent var1) {
    }

    public void setCurrentAnimation(EnumAnimation animation) {
        currentAnimation = animation;
        dataWatcher.updateObject(14, animation.ordinal());
    }

    public boolean canSee(Entity entity) {
        return this.getEntitySenses().canSee(entity);
    }

    public boolean isFollower() {
        return advanced.role == EnumRoleType.Follower && ((RoleFollower) roleInterface).isFollowing() || advanced.role == EnumRoleType.Companion && ((RoleCompanion) roleInterface).isFollowing() || advanced.job == EnumJobType.Follower && ((JobFollower) jobInterface).isFollowing();
    }

    public EntityLivingBase getOwner() {
        if (roleInterface instanceof RoleFollower)
            return ((RoleFollower) roleInterface).owner;

        if (roleInterface instanceof RoleCompanion)
            return ((RoleCompanion) roleInterface).owner;

        if (jobInterface instanceof JobFollower)
            return ((JobFollower) jobInterface).following;

        return null;
    }

    public boolean hasOwner() {
        return advanced.role == EnumRoleType.Follower && ((RoleFollower) roleInterface).hasOwner() || advanced.role == EnumRoleType.Companion && ((RoleCompanion) roleInterface).hasOwner() || advanced.job == EnumJobType.Follower && ((JobFollower) jobInterface).hasOwner();
    }

    public int followRange() {
        if (advanced.role == EnumRoleType.Follower && ((RoleFollower) roleInterface).isFollowing())
            return 36;
        if (advanced.role == EnumRoleType.Companion && ((RoleCompanion) roleInterface).isFollowing())
            return ((RoleCompanion) roleInterface).followRange();
        if (advanced.job == EnumJobType.Follower && ((JobFollower) jobInterface).isFollowing())
            return 16;

        return 225;
    }

    @Override
    public void setHomeArea(int x, int y, int z, int range) {
        super.setHomeArea(x, y, z, range);
        ais.startPos = new int[]{x, y, z};
    }

    @Override
    protected float applyArmorCalculations(DamageSource source, float damage) {
        if (advanced.role == EnumRoleType.Companion)
            damage = ((RoleCompanion) roleInterface).applyArmorCalculations(source, damage);
        return damage;
    }

    @Override
    public boolean isOnSameTeam(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer && !isRemote() && getFaction().isFriendlyToPlayer((EntityPlayer) entity))
            return true;
        return super.isOnSameTeam(entity);
    }

    public void setDataWatcher(DataWatcher dataWatcher) {
        this.dataWatcher = dataWatcher;
    }

    @Override
    public void moveEntityWithHeading(float strafe, float forward) {
        if (handleMountedMovement(strafe, forward)) {
            return;
        }
        double d0 = this.posX;
        double d1 = this.posY;
        double d2 = this.posZ;
        super.moveEntityWithHeading(strafe, forward);
        if (advanced.role == EnumRoleType.Companion && !isRemote())
            ((RoleCompanion) roleInterface).addMovementStat(this.posX - d0, this.posY - d1, this.posZ - d2);
    }

    protected boolean handleMountedMovement(float strafe, float forward) {
        return NPCMountUtil.handleMountedMovement(this, mountState, strafe, forward);
    }
    
    public void performMountedMovement(float strafe, float forward, float moveSpeed) {
        this.moveStrafing = strafe;
        this.moveForward = forward;

        boolean flightMode = isMountFlightModeActive();
        double prevMotionX = this.motionX;
        double prevMotionY = this.motionY;
        double prevMotionZ = this.motionZ;
        float previousJumpFactor = this.jumpMovementFactor;
        float appliedJumpFactor = getMountedAirStrafeFactor(moveSpeed, flightMode);

        this.jumpMovementFactor = appliedJumpFactor;

        // Update move speed on both sides so the client can accurately predict ground
        // movement when controlling a mount. Only setting it server-side causes visible
        // delay/desync for ground mounts while the server reconciles the slower client
        // prediction.
        this.setAIMoveSpeed(moveSpeed);

        super.moveEntityWithHeading(strafe, forward);

        if (flightMode) {
            this.motionY = prevMotionY;
            this.isAirBorne = true;
        }

        this.jumpMovementFactor = previousJumpFactor;

        if (!worldObj.isRemote) {
            double newMotionX = this.motionX;
            double newMotionY = this.motionY;
            double newMotionZ = this.motionZ;
            if (Math.abs(newMotionX - prevMotionX) > 1.0E-5D || Math.abs(newMotionY - prevMotionY) > 1.0E-5D || Math.abs(newMotionZ - prevMotionZ) > 1.0E-5D) {
                this.velocityChanged = true;
            }
        }

        syncMountedRiderVelocity();
        updateMountedLimbSwing();
    }

    protected boolean isMountFlightModeActive() {
        return NPCMountUtil.isMountInFlightMode(mountState);
    }

    private float getMountedAirStrafeFactor(float moveSpeed, boolean flightMode) {
        if (moveSpeed <= 0.0F) {
            return 0.0F;
        }
        if (!flightMode) {
            return moveSpeed * 0.1F;
        }
        // Keep airborne strafing consistent with on-ground acceleration. Vanilla
        // ground movement uses the block slipperiness (default 0.6F) which effectively
        // multiplies moveSpeed by ~1x. Matching that behavior prevents flying mounts
        // from feeling sluggish compared to their walking speed.
        return moveSpeed;
    }

    private void syncMountedRiderVelocity() {
        if (!(this.riddenByEntity instanceof EntityLivingBase)) {
            return;
        }
        EntityLivingBase rider = (EntityLivingBase) this.riddenByEntity;
        double riderPrevMotionX = rider.motionX;
        double riderPrevMotionY = rider.motionY;
        double riderPrevMotionZ = rider.motionZ;

        rider.motionX = this.motionX;
        rider.motionY = this.motionY;
        rider.motionZ = this.motionZ;
        rider.fallDistance = 0.0F;
        rider.isAirBorne = !this.onGround;

        if (!worldObj.isRemote) {
            if (Math.abs(rider.motionX - riderPrevMotionX) > 1.0E-5D || Math.abs(rider.motionY - riderPrevMotionY) > 1.0E-5D || Math.abs(rider.motionZ - riderPrevMotionZ) > 1.0E-5D) {
                rider.velocityChanged = true;
            }
        }
    }

    private void updateMountedLimbSwing() {
        this.prevLimbSwingAmount = this.limbSwingAmount;
        double deltaX = this.posX - this.prevPosX;
        double deltaZ = this.posZ - this.prevPosZ;
        if (worldObj.isRemote && deltaX * deltaX + deltaZ * deltaZ < 1.0E-6D) {
            deltaX = this.motionX;
            deltaZ = this.motionZ;
        }
        float limbSwingSpeed = MathHelper.sqrt_double(deltaX * deltaX + deltaZ * deltaZ) * 4.0F;
        if (limbSwingSpeed > 1.0F) {
            limbSwingSpeed = 1.0F;
        }
        this.limbSwingAmount += (limbSwingSpeed - this.limbSwingAmount) * 0.4F;
        this.limbSwing += this.limbSwingAmount;
    }

    @Override
    public boolean allowLeashing() {
        return false;
    }

    @Override
    public boolean shouldDismountInWater(Entity rider) {
        return false;
    }

    @Override
    public void updateRiderPosition() {
        if (advanced.role == EnumRoleType.Mount && roleInterface instanceof RoleMount && riddenByEntity != null) {
            RoleMount mount = (RoleMount) roleInterface;
            double scale = Math.max(0.1D, this.display.modelSize / 5.0D);
            Vec3 seat = Vec3.createVectorHelper(mount.getOffsetX(), 0.0D, mount.getOffsetZ() + (0.8D * scale));
            seat.rotateAroundY((float) Math.toRadians(-this.renderYawOffset));
            double px = this.posX + seat.xCoord;
            double py = this.posY + this.getMountedYOffset() + riddenByEntity.getYOffset() + mount.getOffsetY();
            double pz = this.posZ + seat.zCoord;

            riddenByEntity.setPosition(px, py, pz);

            if (riddenByEntity instanceof EntityLivingBase) {
                EntityLivingBase rider = (EntityLivingBase) riddenByEntity;
                rider.prevRotationPitch = rider.rotationPitch;
                rider.prevRotationYaw = rider.rotationYaw;
                rider.prevRenderYawOffset = this.renderYawOffset;
                rider.renderYawOffset = this.renderYawOffset;
                rider.prevRotationYawHead = this.renderYawOffset;
                rider.rotationYawHead = this.renderYawOffset;
            }
        } else {
            super.updateRiderPosition();
        }
    }

    private void handleMountRiderState() {
        NPCMountUtil.handleMountRiderState(this, mountState);
    }

    // Model Types: 0: Steve 64x32, 1: Steve 64x64, 2: Alex 64x64
    public int getModelType() {
        return this.display.modelType;
    }

    public void setModelType(int val) {
        this.display.modelType = val;
    }

    protected void updateLeashedState() {
    }
}
