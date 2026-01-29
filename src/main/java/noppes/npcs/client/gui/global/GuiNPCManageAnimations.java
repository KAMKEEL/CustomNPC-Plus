package noppes.npcs.client.gui.global;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.animation.AnimationGetPacket;
import kamkeel.npcs.network.packets.request.animation.AnimationRemovePacket;
import kamkeel.npcs.network.packets.request.animation.AnimationSavePacket;
import kamkeel.npcs.network.packets.request.animation.AnimationsGetPacket;
import kamkeel.npcs.network.packets.request.animation.BuiltInAnimationGetPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiModelInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiTexturedButton;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.controllers.data.AnimationData;
import noppes.npcs.controllers.data.Frame;
import noppes.npcs.controllers.data.FramePart;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GuiNPCManageAnimations extends GuiModelInterface2 implements IScrollData, ICustomScrollListener, ITextfieldListener, IGuiData, ISubGuiListener, GuiYesNoCallback {
    private GuiCustomScroll scrollAnimations;
    private HashMap<String, Integer> customData = new HashMap<String, Integer>();
    private HashMap<String, Integer> builtInData = new HashMap<String, Integer>();
    private Animation animation = new Animation();
    public boolean playingAnimation = false;
    private long prevTick;
    private String selected = null;
    private String search = "";

    // Toggle between Custom (false) and Built-in (true) animations
    private boolean showingBuiltIn = false;
    // Track if current animation is built-in
    private boolean currentIsBuiltIn = false;

    public GuiNPCManageAnimations(EntityNPCInterface npc, boolean save) {
        super(npc);
        this.setSave(save);
        this.xOffset = -148 + 70;
        this.yOffset = -170 + 137;
        PacketClient.sendClient(new AnimationsGetPacket());

        AnimationData data = npc.display.animationData;
        data.setEnabled(true);
    }

    public void initGui() {
        super.initGui();

        // Add/Remove buttons - only show for custom animations
        if (!showingBuiltIn) {
            this.addButton(new GuiNpcButton(0, guiLeft + 368, guiTop + 8, 45, 20, "gui.add"));
            this.addButton(new GuiNpcButton(1, guiLeft + 368, guiTop + 32, 45, 20, "gui.remove"));
        }

        // Toggle button for Custom/Built-in - under Remove button
        String toggleLabel = showingBuiltIn ? "gui.builtin" : "gui.custom";
        this.addButton(new GuiNpcButton(10, guiLeft + 368, guiTop + 56, 45, 20, toggleLabel));

        if (scrollAnimations == null) {
            scrollAnimations = new GuiCustomScroll(this, 0, 0);
            scrollAnimations.setSize(143, 185);
        }
        scrollAnimations.guiLeft = guiLeft + 220;
        scrollAnimations.guiTop = guiTop + 4;
        addScroll(scrollAnimations);

        // Search field
        addTextField(new GuiNpcTextField(55, this, fontRendererObj, guiLeft + 220, guiTop + 192, 143, 20, search));

        // Update scroll list based on current view
        scrollAnimations.setList(getSearchList());

        if (animation.id == -1 && !currentIsBuiltIn)
            return;

        // ID label - only for custom animations
        if (!currentIsBuiltIn) {
            addLabel(new GuiNpcLabel(10, "ID", guiLeft + 368, guiTop + 192));
            addLabel(new GuiNpcLabel(11, animation.id + "", guiLeft + 368, guiTop + 192 + 10));
        } else {
            addLabel(new GuiNpcLabel(10, "gui.builtin", guiLeft + 368, guiTop + 192));
        }

        AnimationData data = npc.display.animationData;
        if (!playingAnimation) {
            data.setAnimation(new Animation());
            data.animation.smooth = animation.smooth;
            data.animation.loop = 0;

            if (this.animation.frames.size() > 0) {
                Frame firstFrame = new Frame();
                firstFrame.parent = data.animation;
                firstFrame.readFromNBT(this.animation.frames.get(0).writeToNBT());
                data.animation.addFrame(firstFrame);
            }
        }

        // Edit button - only for custom animations
        if (animation != null && !currentIsBuiltIn) {
            this.addButton(new GuiNpcButton(100, guiLeft + 10, guiTop + 192, 45, 20, "gui.edit"));
        }

        String animTexture = "customnpcs:textures/gui/animation.png";
        int playButtonOffsetX = currentIsBuiltIn ? 80 : 140;
        if (animation != null && !animation.frames.isEmpty()) {
            if (!this.playingAnimation || data.animation.paused) {//Play
                this.addLabel(new GuiNpcLabel(90, data.animation.paused ? "animation.paused" : "animation.stopped", guiLeft + playButtonOffsetX - 15, guiTop + 198));
                if (data.animation.paused) {
                    this.addLabel(new GuiNpcLabel(94, "", guiLeft + playButtonOffsetX + 21, guiTop + 198));
                }
                this.addButton(new GuiTexturedButton(91, "", guiLeft + playButtonOffsetX + 35, guiTop + 192, 11, 20, animTexture, 18, 71));
            } else {//Pause
                this.addLabel(new GuiNpcLabel(90, "animation.playing", guiLeft + playButtonOffsetX - 15, guiTop + 198));
                this.addLabel(new GuiNpcLabel(94, "", guiLeft + playButtonOffsetX + 20, guiTop + 198));
                this.addButton(new GuiTexturedButton(92, "", guiLeft + playButtonOffsetX + 35, guiTop + 192, 14, 20, animTexture, 0, 71));
            }
            if (this.playingAnimation) {//Stop
                this.addButton(new GuiTexturedButton(93, "", guiLeft + playButtonOffsetX + 55, guiTop + 192, 14, 20, animTexture, 33, 71));
            }
        }
    }

    @Override
    public void drawScreen(int par1, int par2, float partialTicks) {
        AnimationData data = npc.display.animationData;
        if (!data.isActive() && this.playingAnimation) {
            this.playingAnimation = false;
            initGui();
        } else if (data.isActive()) {
            long time = mc.theWorld.getTotalWorldTime();
            if (time != prevTick) {
                npc.display.animationData.increaseTime();
                GuiNpcLabel label = this.getLabel(94);
                if (label != null) {
                    label.label += ".";
                    if (label.label.length() % 4 == 0) {
                        label.label = "";
                    }
                }
            }
            prevTick = time;
        }
        super.drawScreen(par1, par2, partialTicks);
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (getTextField(55) != null) {
            if (getTextField(55).isFocused()) {
                if (search.equals(getTextField(55).getText()))
                    return;
                search = getTextField(55).getText().toLowerCase();
                scrollAnimations.resetScroll();
                scrollAnimations.setList(getSearchList());
            }
        }
    }

    private HashMap<String, Integer> getCurrentData() {
        return showingBuiltIn ? builtInData : customData;
    }

    private List<String> getSearchList() {
        HashMap<String, Integer> data = getCurrentData();
        if (search.isEmpty()) {
            return new ArrayList<String>(data.keySet());
        }
        List<String> list = new ArrayList<String>();
        for (String name : data.keySet()) {
            if (name.toLowerCase().contains(search))
                list.add(name);
        }
        return list;
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        GuiNpcButton button = (GuiNpcButton) guibutton;

        // Toggle button
        if (button.id == 10) {
            showingBuiltIn = !showingBuiltIn;
            // Clear selection when switching views
            selected = null;
            animation = new Animation();
            currentIsBuiltIn = false;
            scrollAnimations.clear();
            initGui();
            return;
        }

        // Add button - only for custom
        if (button.id == 0 && !showingBuiltIn) {
            save();
            String name = "New";
            while (customData.containsKey(name))
                name += "_";
            Animation animation = new Animation(-1, name);
            PacketClient.sendClient(new AnimationSavePacket(animation.writeToNBT()));
        }

        // Remove button - only for custom
        if (button.id == 1 && !showingBuiltIn) {
            if (customData.containsKey(scrollAnimations.getSelected())) {
                GuiYesNo guiyesno = new GuiYesNo(this, scrollAnimations.getSelected(), StatCollector.translateToLocal("gui.delete"), 1);
                displayGuiScreen(guiyesno);
            }
        }

        AnimationData data = npc.display.animationData;
        if (guibutton.id == 91) {
            if (!this.playingAnimation || !data.isActive()) {
                animation.currentFrame = 0;
                animation.currentFrameTime = 0;
                for (Frame frame : animation.frames) {
                    for (FramePart framePart : frame.frameParts.values()) {
                        framePart.prevRotations = new float[]{0, 0, 0};
                        framePart.prevPivots = new float[]{0, 0, 0};
                    }
                }
            }
            this.playingAnimation = true;
            data.setAnimation(this.animation);
            data.animation.paused = false;
        } else if (guibutton.id == 92) {
            data.animation.paused = true;
        } else if (guibutton.id == 93) {
            this.playingAnimation = false;
            data.animation.paused = false;
        }

        // Edit button - only for custom
        if (guibutton.id == 100 && !currentIsBuiltIn) {
            NoppesUtil.openGUI(player, new GuiNPCEditAnimation(this, this.animation, npc));
        }

        initGui();
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        this.animation = new Animation();
        animation.readFromNBT(compound);

        // Check if this is a built-in animation
        currentIsBuiltIn = compound.hasKey("BuiltIn") && compound.getBoolean("BuiltIn");

        setSelected(animation.name);
        npc.display.animationData.setAnimation(this.animation);
        this.playingAnimation = false;
        npc.display.animationData.animation.paused = false;
        initGui();
    }

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data, EnumScrollData type) {
        String name = scrollAnimations.getSelected();

        if (type == EnumScrollData.ANIMATIONS) {
            this.customData = data;
        } else if (type == EnumScrollData.BUILTIN_ANIMATIONS) {
            this.builtInData = data;
        }

        scrollAnimations.setList(getSearchList());

        if (name != null)
            scrollAnimations.setSelected(name);
    }

    @Override
    public void setSelected(String selected) {
        this.selected = selected;
        scrollAnimations.setSelected(selected);
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll.id == 0) {
            selected = scrollAnimations.getSelected();
            HashMap<String, Integer> data = getCurrentData();

            if (showingBuiltIn) {
                // Fetch built-in animation by name
                PacketClient.sendClient(new BuiltInAnimationGetPacket(selected));
            } else {
                // Fetch custom animation by ID
                if (data.containsKey(selected)) {
                    PacketClient.sendClient(new AnimationGetPacket(data.get(selected)));
                }
            }
        }
    }

    public void save() {
        // Only save custom animations
        if (selected != null && customData.containsKey(selected) && animation != null && !currentIsBuiltIn) {
            PacketClient.sendClient(new AnimationSavePacket(animation.writeToNBT()));
        }
    }

    @Override
    public void unFocused(GuiNpcTextField guiNpcTextField) {
        // Don't allow renaming built-in animations
        if (animation.id == -1 || currentIsBuiltIn)
            return;

        if (guiNpcTextField.id == 0) {
            String name = guiNpcTextField.getText();
            if (!name.isEmpty() && !customData.containsKey(name)) {
                String old = animation.name;
                customData.remove(animation.name);
                animation.name = name;
                customData.put(animation.name, animation.id);
                selected = name;
                scrollAnimations.replace(old, animation.name);
            }
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        NoppesUtil.openGUI(player, this);
        if (!result)
            return;
        if (id == 1) {
            // Only delete custom animations
            if (customData.containsKey(scrollAnimations.getSelected()) && !showingBuiltIn) {
                PacketClient.sendClient(new AnimationRemovePacket(customData.get(selected)));
                scrollAnimations.clear();
                animation = new Animation();
                currentIsBuiltIn = false;
                initGui();
            }
        }
    }
}
