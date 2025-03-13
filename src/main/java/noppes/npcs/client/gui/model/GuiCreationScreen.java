package noppes.npcs.client.gui.model;

import kamkeel.npcs.addon.client.GeckoAddonClient;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.npc.ModelDataSavePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.entity.NPCRendererHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.compat.PixelmonHelper;
import noppes.npcs.entity.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class GuiCreationScreen extends GuiModelInterface implements ICustomScrollListener {

    public HashMap<String, Class<? extends EntityLivingBase>> data = new HashMap<String, Class<? extends EntityLivingBase>>();
    private final List<String> list;
    private final String[] ignoredTags = {"CanBreakDoors", "Bred", "PlayerCreated", "Tame", "HasReproduced"};

    private GuiNpcButton prev, next;

    private final GuiScreen parent;

    private final HashMap<Integer, String> mapped = new HashMap<Integer, String>();

    public GuiCreationScreen(GuiScreen parent, EntityCustomNpc npc) {
        super(npc);
        this.parent = parent;
        Map<?, ?> mapping = EntityList.stringToClassMapping;
        for (Object name : mapping.keySet()) {
            Class<?> c = (Class<?>) mapping.get(name);
            try {
                if (!EntityCustomNpc.class.isAssignableFrom(c) && EntityLiving.class.isAssignableFrom(c) && c.getConstructor(World.class) != null && !Modifier.isAbstract(c.getModifiers())) {
                    if (RenderManager.instance.getEntityClassRenderObject(c) instanceof RendererLivingEntity) {
                        if (EntityNpcSlime.class.isAssignableFrom(c)) {
                            data.put("CustomNPC Slime", c.asSubclass(EntityLivingBase.class));
                        } else if (EntityNpcDragon.class.isAssignableFrom(c)) {
                            data.put("CustomNPC Dragon", c.asSubclass(EntityLivingBase.class));
                        } else if (EntityNpcPony.class.isAssignableFrom(c)) {
                            data.put("CustomNPC Pony", c.asSubclass(EntityLivingBase.class));
                        } else if (EntityNPCGolem.class.isAssignableFrom(c)) {
                            data.put("CustomNPC Golem", c.asSubclass(EntityLivingBase.class));
                        } else if (EntityNpcCrystal.class.isAssignableFrom(c)) {
                            data.put("CustomNPC Crystal", c.asSubclass(EntityLivingBase.class));
                        } else {
                            String className = name.toString();
                            if (className.equals("npcgecko.CustomModelEntity")) {
                                data.put("CustomNPC GeoModel", c.asSubclass(EntityLivingBase.class));
                            } else {
                                data.put(name.toString(), c.asSubclass(EntityLivingBase.class));
                            }

                        }
                    }
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
            }
        }
        list = new ArrayList<String>(data.keySet());
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
    }

    @Override
    public void initGui() {
        EntityLivingBase entity = playerdata.getEntity(npc);
        xOffset = entity == null ? 0 : 50;
        super.initGui();

        String title = "CustomNPC";
        if (entity != null)
            title = (String) EntityList.classToStringMapping.get(playerdata.getEntityClass());
        this.addButton(new GuiNpcButton(1, guiLeft + 140, guiTop, 100, 20, title));

        this.addButton(prev = new GuiNpcButton(0, guiLeft + 118, guiTop, 20, 20, "<"));
        this.addButton(next = new GuiNpcButton(2, guiLeft + 242, guiTop, 20, 20, ">"));
        prev.enabled = getCurrentEntityIndex() >= 0;
        next.enabled = getCurrentEntityIndex() < list.size() - 1;

        if (entity == null) {
            showPlayerButtons();
        } else if (PixelmonHelper.isPixelmon(entity)) {
            showPixelmonMenu(entity);
        } else {
            showEntityButtons(entity);
        }
    }

    private void showPlayerButtons() {
        int y = guiTop;

        addButton(new GuiNpcButton(9, guiLeft + 4, y += 22, 96, 20, "model.rotate"));

        addButton(new GuiNpcButton(8, guiLeft + 4, y += 22, 96, 20, "model.scale"));

        addButton(new GuiNpcButton(4, guiLeft + 50, y += 22, 50, 20, "selectServer.edit"));
        addLabel(new GuiNpcLabel(1, "Head", guiLeft, y + 5, 0xFFFFFF));

        addButton(new GuiNpcButton(5, guiLeft + 50, y += 22, 50, 20, "selectServer.edit"));
        addLabel(new GuiNpcLabel(2, "Body", guiLeft, y + 5, 0xFFFFFF));

        addButton(new GuiNpcButton(6, guiLeft + 50, y += 22, 50, 20, "selectServer.edit"));
        addLabel(new GuiNpcLabel(3, "Arms", guiLeft, y + 5, 0xFFFFFF));

        addButton(new GuiNpcButton(7, guiLeft + 50, y += 22, 50, 20, "selectServer.edit"));
        addLabel(new GuiNpcLabel(4, "Legs", guiLeft, y + 5, 0xFFFFFF));

        addButton(new GuiNpcButton(250, guiLeft + 50, y += 22, 50, 20, new String[]{"Default", "Steve64", "Alex"}, this.npc.display.modelType));
        addLabel(new GuiNpcLabel(250, "Model", guiLeft, y + 5, 0xFFFFFF));

        addButton(new GuiNpcButton(44, guiLeft + 310, guiTop + 14, 80, 20, "Save Model"));
        addButton(new GuiNpcButton(45, guiLeft + 310, guiTop + 36, 80, 20, "Load Model"));
    }

    private void showPixelmonMenu(EntityLivingBase entity) {
        GuiCustomScroll scroll = new GuiCustomScroll(this, 0);
        scroll.setSize(120, 200);
        scroll.guiLeft = guiLeft;
        scroll.guiTop = guiTop + 20;
        addScroll(scroll);

        scroll.setList(PixelmonHelper.getPixelmonList());
        scroll.setSelected(PixelmonHelper.getName(entity));

        Minecraft.getMinecraft().thePlayer.sendChatMessage(PixelmonHelper.getName(entity));
    }

    private void showEntityButtons(EntityLivingBase entity) {
        mapped.clear();
        if (entity instanceof EntityNPCInterface)
            return;
        int y = guiTop + 20;

        NBTTagCompound compound = getExtras(entity);
        Set<String> keys = compound.func_150296_c();
        int i = 0;
        for (String name : keys) {
            if (isIgnored(name))
                continue;
            NBTBase base = compound.getTag(name);
            if (name.equals("Age")) {
                i++;
                addLabel(new GuiNpcLabel(0, "Child", guiLeft, y + 5 + i * 22, 0xFFFFFF));
                addButton(new GuiNpcButton(30, guiLeft + 80, y + i * 22, 50, 20, new String[]{"gui.no", "gui.yes"}, entity.isChild() ? 1 : 0));
            } else if (base.getId() == 1) {
                byte b = ((NBTTagByte) base).func_150290_f();
                if (b != 0 && b != 1)
                    continue;
                if (playerdata.extra.hasKey(name))
                    b = playerdata.extra.getByte(name);
                i++;
                addLabel(new GuiNpcLabel(100 + i, name, guiLeft, y + 5 + i * 22, 0xFFFFFF));
                addButton(new GuiNpcButton(100 + i, guiLeft + 80, y + i * 22, 50, 20, new String[]{"gui.no", "gui.yes"}, b));

                mapped.put(i, name);
            }
        }
        if (EntityList.getEntityString(entity).equals("doggystyle.Dog")) {
            int breed = 0;
            try {
                Method method = entity.getClass().getMethod("getBreedID");
                breed = (Integer) method.invoke(entity);
            } catch (Exception e) {

            }
            i++;
            addLabel(new GuiNpcLabel(201, "Breed", guiLeft, y + 5 + i * 22, 0xFFFFFF));
            addButton(new GuiNpcButton(201, guiLeft + 80, y + i * 22, 50, 20, new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"}, breed));
        }

        GeckoAddonClient.Instance.showGeckoButtons(this, entity);
    }

    private boolean isIgnored(String tag) {
        for (String s : ignoredTags)
            if (s.equals(tag))
                return true;
        return false;
    }

    private NBTTagCompound getExtras(EntityLivingBase entity) {
        NBTTagCompound fake = new NBTTagCompound();
        new EntityFakeLiving(entity.worldObj).writeEntityToNBT(fake);

        NBTTagCompound compound = new NBTTagCompound();
        try {
            entity.writeEntityToNBT(compound);
        } catch (Exception e) {

        }
        Set<String> keys = fake.func_150296_c();
        for (String name : keys)
            compound.removeTag(name);

        return compound;
    }

    private int getCurrentEntityIndex() {
        return list.indexOf(EntityList.classToStringMapping.get(playerdata.getEntityClass()));
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        super.actionPerformed(btn);
        GuiNpcButton button = (GuiNpcButton) btn;
        if (button.id == 0) {
            int index = getCurrentEntityIndex();
            if (!prev.enabled)
                return;
            index--;
            try {
                if (index < 0) {
                    playerdata.setEntityClass(null);
                    npc.display.texture = "customnpcs:textures/entity/humanmale/Steve.png";
                } else {
                    playerdata.setEntityClass(data.get(list.get(index)));

                    EntityLivingBase entity = playerdata.getEntity(npc);
                    if (entity != null) {
                        RendererLivingEntity render = (RendererLivingEntity) RenderManager.instance.getEntityRenderObject(entity);
                        npc.display.texture = NPCRendererHelper.getTexture(render, entity);
                    }
                }
                //npc.display.glowTexture = "";
                npc.display.skinOverlayData.overlayList.remove(0);
                npc.textureLocation = null;
                npc.updateHitbox();
            } catch (Exception ex) {
                npc.display.texture = "customnpcs:textures/entity/humanmale/Steve.png";
            }
            initGui();
        }
        if (button.id == 2) {
            int index = getCurrentEntityIndex();
            if (!next.enabled)
                return;
            index++;
            playerdata.setEntityClass(data.get(list.get(index)));

            updateTexture();

            initGui();
        }

        if (button.id == 1) {
            this.mc.displayGuiScreen(new GuiEntitySelection(this, playerdata, npc));
        }

        if (button.id == 4) {
            this.mc.displayGuiScreen(new GuiModelHead(this, npc));
        }
        if (button.id == 5) {
            this.mc.displayGuiScreen(new GuiModelBody(this, npc));
        }
        if (button.id == 6) {
            this.mc.displayGuiScreen(new GuiModelArms(this, npc));
        }
        if (button.id == 7) {
            this.mc.displayGuiScreen(new GuiModelLegs(this, npc));
        }

        // New Button for Changing Models
        // URL Swapper
        if (button.id == 250) {
            if (npc.display.modelType == 0) {
                if (npc.display.skinType == 2) {
                    npc.display.skinType = 3;
                }
                npc.display.modelType = 1;
            } else if (npc.display.modelType == 1) {
                npc.display.modelType = 2;
            } else {
                if (npc.display.skinType == 3) {
                    npc.display.skinType = 2;
                }
                npc.display.modelType = 0;
            }
            npc.textureLocation = null;
        }

        if (button.id == 8) {
            this.mc.displayGuiScreen(new GuiModelScale(this, playerdata, npc));
        }
        if (button.id == 9) {
            this.mc.displayGuiScreen(new GuiModelRotate(this, playerdata, npc));
        }
        if (button.id == 30) {
            playerdata.extra.setInteger("Age", button.getValue() == 1 ? -24000 : 0);
            playerdata.clearEntity();
        }

        if (button.id == 44) {
            this.mc.displayGuiScreen(new GuiPresetSave(this, playerdata));
        }

        if (button.id == 45) {
            this.mc.displayGuiScreen(new GuiPresetSelection(this, playerdata));
        }
        if (button.id >= 100 && button.id < 200) {
            String name = mapped.get(button.id - 100);
            if (name != null) {
                playerdata.extra.setBoolean(name, button.getValue() == 1);
                playerdata.clearEntity();
            }
        }
        if (button.id == 201) {
            try {
                EntityLivingBase entity = playerdata.getEntity(npc);
                Method method = entity.getClass().getMethod("setBreedID", int.class);
                method.invoke(entity, button.getValue());
                NBTTagCompound comp = new NBTTagCompound();
                entity.writeEntityToNBT(comp);
                playerdata.extra.setString("EntityData21", comp.getString("EntityData21"));
                playerdata.clearEntity();
                updateTexture();
            } catch (Exception ignored) {
            }
        }

        GeckoAddonClient.Instance.geckoGuiCreationScreenActionPerformed(this, button);
    }

    private void updateTexture() {
        try {
            EntityLivingBase entity = playerdata.getEntity(npc);
            if (entity != null && entity.equals(npc.modelData.entity)) {
                return;
            }

            if (entity instanceof EntityNpcDragon) {
                npc.display.texture = "customnpcs:textures/entity/dragon/BlackDragon.png";
            } else if (entity instanceof EntityNpcSlime) {
                npc.display.texture = "customnpcs:textures/entity/slime/Slime.png";
            } else if (entity instanceof EntityNPCGolem) {
                npc.display.texture = "customnpcs:textures/entity/golem/Iron Golem.png";
            } else if (entity instanceof EntityNpcPony) {
                npc.display.texture = "customnpcs:textures/entity/ponies/MineLP Derpy Hooves.png";
            } else if (entity instanceof EntityNpcCrystal) {
                npc.display.texture = "customnpcs:textures/entity/crystal/EnderCrystal.png";
            } else if (entity != null) {
                RendererLivingEntity render = (RendererLivingEntity) RenderManager.instance.getEntityRenderObject(entity);
                npc.display.texture = NPCRendererHelper.getTexture(render, entity);
            } else {
                npc.display.texture = "customnpcs:textures/entity/humanmale/Steve.png";
            }

            npc.display.skinOverlayData.overlayList.remove(0);
            npc.textureLocation = null;
            npc.updateHitbox();
        } catch (Exception ex) {
            npc.display.texture = "customnpcs:textures/entity/humanmale/Steve.png";
        }
    }

    @Override
    public void close() {
        PacketClient.sendClient(new ModelDataSavePacket(playerdata.writeToNBT()));
        displayGuiScreen(parent);
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
        playerdata.clearEntity();
        playerdata.extra.setString("Name", scroll.getSelected());

        updateTexture();
    }
}
