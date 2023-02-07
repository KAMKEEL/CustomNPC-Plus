package noppes.npcs.client.gui.global;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.AnimationData;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumAnimationPart;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.controllers.data.Frame;
import noppes.npcs.controllers.data.FramePart;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class GuiNPCManageAnimations extends GuiModelInterface implements IScrollData, ITextfieldListener, ICustomScrollListener, IGuiData {
    private Animation animation = null;

    private EnumAnimationPart editingPart = EnumAnimationPart.HEAD;
    private int editingMode = 0;
    private int frameIndex = 0;
    public boolean playingAnimation = false;
    private Frame prevFrame;

    private GuiCustomScroll scroll;
    private String prevSelected = "";
    private long prevTick;

    public GuiNPCManageAnimations(EntityNPCInterface npc) {
        super((EntityCustomNpc) npc);
        Client.sendData(EnumPacketServer.AnimationListGet);
        xOffset -= 20;
        yOffset = -45;
        npc.display.texture = "customnpcs:textures/entity/humanmale/AnimationBody.png";

        AnimationData data = npc.display.animationData;
        data.setEnabled(true);
    }

    @Override
    public void initGui() {
        super.initGui();

        if(scroll == null)
            scroll = new GuiCustomScroll(this,0);
        scroll.setSize(100, 175);
        scroll.guiLeft = guiLeft - 20;
        scroll.guiTop = guiTop + 15;
        this.addScroll(scroll);

        this.addButton(new GuiNpcButton(1,guiLeft - 20, guiTop - 5, 45, 20, "gui.add"));
        this.addButton(new GuiNpcButton(2,guiLeft + 30, guiTop - 5, 45, 20, "gui.remove"));

        if (animation != null) {
            Frame editingFrame = this.editingFrame();
            if (editingFrame != null) {
                if (editingFrame != prevFrame && prevFrame != null) {
                    HashMap<EnumAnimationPart,FramePart> frameParts = prevFrame.frameParts;
                    for (Map.Entry<EnumAnimationPart,FramePart> entry : frameParts.entrySet()) {
                        if (editingFrame.frameParts.containsKey(entry.getKey())) {
                            FramePart part = editingFrame.frameParts.get(entry.getKey());
                            FramePart prevPart = prevFrame.frameParts.get(entry.getKey());
                            part.prevRotations = prevPart.prevRotations;
                            part.prevPivots = prevPart.prevPivots;
                            part.partialRotationTick = prevPart.partialRotationTick;
                            part.partialPivotTick = prevPart.partialPivotTick;
                        }
                    }
                }
            }
            prevFrame = editingFrame;
            FramePart editingPart = this.editingPart();

            AnimationData data = npc.display.animationData;
            if (!playingAnimation) {
                data.animation = new Animation();
                data.animation.smooth = animation.smooth;
                data.animation.renderTicks = animation.renderTicks;
                data.animation.loop = 0;
                if (editingFrame != null) {
                    data.animation.frames.add(editingFrame);
                }
            }


            for (Frame frame : animation.frames) {
                frame.parent = animation;
                if (!frame.isCustomized()) {
                    frame.speed = animation.speed;
                    frame.smooth = animation.smooth;
                    frame.renderTicks = animation.renderTicks;
                }
                for (Map.Entry<EnumAnimationPart,FramePart> entry : frame.frameParts.entrySet()) {
                    FramePart part = entry.getValue();
                    part.parent = animation;
                    if (!part.isCustomized()) {
                        part.speed = frame.speed;
                        part.smooth = frame.smooth;
                    }
                }
            }

            editingMode %= 3;
            if (animation.loop >= animation.frames.size()) {
                animation.loop = -1;
            }

            String animTexture = "customnpcs:textures/gui/animation.png";
            if (data.animation != null && data.animation.frames.size() > 0) {
                if (!this.playingAnimation || data.animation.paused) {//Play
                    this.addLabel(new GuiNpcLabel(90, data.animation.paused ? "animation.paused" : "animation.stopped", guiLeft - 15, guiTop + 206, 0xFFFFFF));
                    if (data.animation.paused) {
                        this.addLabel(new GuiNpcLabel(94, "", guiLeft + 21, guiTop + 206, 0xFFFFFF));
                    }
                    this.addButton(new GuiTexturedButton(91, "", guiLeft + 35, guiTop + 200, 11, 20, animTexture, 18, 71));
                } else {//Pause
                    this.addLabel(new GuiNpcLabel(90, "animation.playing", guiLeft - 15, guiTop + 206, 0xFFFFFF));
                    this.addLabel(new GuiNpcLabel(94, "", guiLeft + 20, guiTop + 206, 0xFFFFFF));
                    this.addButton(new GuiTexturedButton(92, "", guiLeft + 35, guiTop + 200, 14, 20, animTexture, 0, 71));
                }
                if (this.playingAnimation) {//Stop
                    this.addButton(new GuiTexturedButton(93, "", guiLeft + 55, guiTop + 200, 14, 20, animTexture, 33, 71));
                }
            }


            this.addLabel(new GuiNpcLabel(10, "animation.frames", guiLeft + 9 + 145, guiTop + 180, 0xFFFFFF));
            this.addButton(new GuiNpcButton(11, guiLeft - 20 + 145, guiTop + 191, 45, 20, "gui.add"));
            if (animation.frames.size() > 0) {
                frameIndex %= animation.frames.size();
                this.addButton(new GuiNpcButton(12, guiLeft + 30 + 145, guiTop + 191, 45, 20, "gui.remove"));
                this.addButton(new GuiNpcButton(13, guiLeft - 3 + 145, guiTop + 210, 20, 20, "<"));
                this.addButton(new GuiNpcButton(14, guiLeft + 17 + 145, guiTop + 210, 20, 20, "" + frameIndex));
                this.addButton(new GuiNpcButton(15, guiLeft + 37 + 145, guiTop + 210, 20, 20, ">"));
            }

            this.addButton(new GuiNpcButton(20, guiLeft + 290, guiTop - 5, 75, 20, new String[]{"animation.animation", "animation.frame", "animation.framePart"}, editingMode));

            if (editingMode == 0) {
                //name - textfield
                //      Send the old animation name (string selected on the scroll).
                //      On the server side, if the new animation's name is different from the old one but already exists,
                //      do not change the name (set the name to the old animation name after the animation is read from NBT).
                this.addTextField(new GuiNpcTextField(30, this, guiLeft + 270, guiTop + 18, 100, 15, animation.name));
                //
                //speed - textfield
                this.addLabel(new GuiNpcLabel(31, "stats.speed", guiLeft + 270, guiTop + 44, 0xFFFFFF));
                this.addTextField(new GuiNpcTextField(32, this, guiLeft + 310, guiTop + 40, 30, 15, animation.speed + ""));
                this.getTextField(32).floatsOnly = true;
                this.getTextField(32).setMinMaxDefaultFloat(0,Float.MAX_VALUE,1.0F);
                //
                //smooth - button
                this.addLabel(new GuiNpcLabel(33, "animation.smoothing", guiLeft + 270, guiTop + 64, 0xFFFFFF));
                this.addButton(new GuiNpcButton(34, guiLeft + 325, guiTop + 58, 60, 20, new String[]{"animation.smooth", "animation.linear", "gui.none"}, animation.smooth));
                //
                //ticks - button
                this.addLabel(new GuiNpcLabel(35, "animation.tickType", guiLeft + 270, guiTop + 104, 0xFFFFFF));
                this.addButton(new GuiNpcButton(36, guiLeft + 325, guiTop + 98, 75, 20, new String[]{"animation.renderTicks", "animation.mcTicks"}, animation.renderTicks ? 0 : 1));
                //
                //loop - button
                this.addLabel(new GuiNpcLabel(37, "animation.loopStart", guiLeft + 270, guiTop + 84, 0xFFFFFF));
                this.addButton(new GuiNpcButton(38, guiLeft + 325, guiTop + 78, 75, 20, animation.loop == -1 ? "No Looping" : "Frame " + animation.loop));
                //
                //whileStanding - button
                this.addLabel(new GuiNpcLabel(39, "animation.whileStanding", guiLeft + 270, guiTop + 124, 0xFFFFFF));
                this.addButton(new GuiNpcButton(40, guiLeft + 345, guiTop + 118, 30, 20, new String[]{"gui.yes", "gui.no"}, animation.whileStanding ? 0 : 1));
                //
                //whileAttacking - button
                this.addLabel(new GuiNpcLabel(41, "animation.whileAttacking", guiLeft + 270, guiTop + 144, 0xFFFFFF));
                this.addButton(new GuiNpcButton(42, guiLeft + 345, guiTop + 138, 30, 20, new String[]{"gui.yes", "gui.no"}, animation.whileAttacking ? 0 : 1));
                //
                //whileMoving - button
                this.addLabel(new GuiNpcLabel(43, "animation.whileMoving", guiLeft + 270, guiTop + 164, 0xFFFFFF));
                this.addButton(new GuiNpcButton(44, guiLeft + 345, guiTop + 158, 30, 20, new String[]{"gui.yes", "gui.no"}, animation.whileMoving ? 0 : 1));

            } else if (editingFrame != null) {
                if (editingMode == 1) {
                    //
                    //duration - textfield
                    this.addLabel(new GuiNpcLabel(50, "animation.duration", guiLeft + 270, guiTop + 25, 0xFFFFFF));
                    this.addTextField(new GuiNpcTextField(51, this, guiLeft + 330, guiTop + 21, 30, 15, editingFrame.duration + ""));
                    this.getTextField(51).integersOnly = true;
                    this.getTextField(51).setMinMaxDefaultFloat(0, Integer.MAX_VALUE, 10);
                    //
                    //customized - button, enables all the following options.
                    this.addLabel(new GuiNpcLabel(52, "animation.customized", guiLeft + 270, guiTop + 44, 0xFFFFFF));
                    this.addButton(new GuiNpcButton(53, guiLeft + 325, guiTop + 38, 30, 20, new String[]{"gui.yes", "gui.no"}, editingFrame.isCustomized() ? 0 : 1));
                    if (editingFrame.isCustomized()) {
                        //
                        //speed - textfield
                        this.addLabel(new GuiNpcLabel(31, "stats.speed", guiLeft + 270, guiTop + 64, 0xFFFFFF));
                        this.addTextField(new GuiNpcTextField(32, this, guiLeft + 330, guiTop + 60, 30, 15, editingFrame.speed + ""));
                        this.getTextField(32).floatsOnly = true;
                        this.getTextField(32).setMinMaxDefaultFloat(0, Float.MAX_VALUE, 1.0F);
                        //
                        //smooth - button
                        this.addLabel(new GuiNpcLabel(33, "animation.smoothing", guiLeft + 270, guiTop + 84, 0xFFFFFF));
                        this.addButton(new GuiNpcButton(34, guiLeft + 325, guiTop + 78, 60, 20, new String[]{"animation.smooth", "animation.linear", "gui.none"}, editingFrame.smooth));
                        //
                        //ticks - button
                        this.addLabel(new GuiNpcLabel(35, "animation.tickType", guiLeft + 270, guiTop + 104, 0xFFFFFF));
                        this.addButton(new GuiNpcButton(36, guiLeft + 325, guiTop + 98, 75, 20, new String[]{"animation.renderTicks", "animation.mcTicks"}, editingFrame.renderTicks ? 0 : 1));
                    }
                } else if (editingMode == 2) {
                    //
                    //6 body part textured buttons
                    //Add/remove part button
                    //      6 custom GUI textured buttons for each body part determine "this.currentPart (enum)".
                    //      If the current frame doesn't have that part, the button will say "Add part"
                    //      otherwise, the button will say "Remove", and more options for editing the part become available:
                    //

                    //Head
                    this.addButton(new GuiTexturedButton(60, "",guiLeft + 270, guiTop + 18,22,23, animTexture,0,0));
                    //Body
                    this.addButton(new GuiTexturedButton(61, "",guiLeft + 270, guiTop + 43,22,23, animTexture,24,0));
                    //Right Arm
                    this.addButton(new GuiTexturedButton(62, "",guiLeft + 261, guiTop + 43,8,23, animTexture,48,0));
                    //Left Arm
                    this.addButton(new GuiTexturedButton(63, "",guiLeft + 296, guiTop + 43,8,23, animTexture,48,0));
                    //Right Leg
                    this.addButton(new GuiTexturedButton(64, "",guiLeft + 271, guiTop + 68,10,23, animTexture,58,0));
                    //Left Leg
                    this.addButton(new GuiTexturedButton(65, "",guiLeft + 282, guiTop + 68,10,23, animTexture,58,0));
                    //Full Body
                    this.addButton(new GuiTexturedButton(66, "",guiLeft + 310, guiTop + 20,17,23, animTexture,70,0));

                    for (int i = 0; i < 7; i++) {
                        if (this.buttons.containsKey(60 + i)) {
                            ((GuiTexturedButton) this.getButton(60 + i)).scale = 1.2F;
                            if (!editingFrame.frameParts.containsKey(EnumAnimationPart.values()[i])) {
                                ((GuiTexturedButton) this.getButton(60 + i)).textureX += 96;
                            }
                        }
                    }

                    if (editingPart != null) {
                        //remove part button
                        this.addLabel(new GuiNpcLabel(67, editingPart.part.name(), guiLeft + 320, guiTop + 60, 0xFFFFFF));
                        this.addButton(new GuiNpcButton(68, guiLeft + 315, guiTop + 75, 60, 20, "gui.remove"));
                        //
                        //rotation - 3 textfields
                        this.addLabel(new GuiNpcLabel(70, "animation.rotations", guiLeft + 270, guiTop + 105, 0xFFFFFF));
                        this.addTextField(new GuiNpcTextField(71, this, guiLeft + 270, guiTop + 117, 35, 15, editingPart.rotation[0] + ""));
                        this.getTextField(71).floatsOnly = true;
                        this.getTextField(71).setMinMaxDefaultFloat(-Float.MAX_VALUE, Float.MAX_VALUE, 0);
                        this.addTextField(new GuiNpcTextField(72, this, guiLeft + 310, guiTop + 117, 35, 15, editingPart.rotation[1] + ""));
                        this.getTextField(72).floatsOnly = true;
                        this.getTextField(72).setMinMaxDefaultFloat(-Float.MAX_VALUE, Float.MAX_VALUE, 0);
                        this.addTextField(new GuiNpcTextField(73, this, guiLeft + 350, guiTop + 117, 35, 15, editingPart.rotation[2] + ""));
                        this.getTextField(73).floatsOnly = true;
                        this.getTextField(73).setMinMaxDefaultFloat(-Float.MAX_VALUE, Float.MAX_VALUE, 0);
                        //
                        //pivot - 3 textfields
                        this.addLabel(new GuiNpcLabel(80, "animation.pivots", guiLeft + 270, guiTop + 137, 0xFFFFFF));
                        this.addTextField(new GuiNpcTextField(81, this, guiLeft + 270, guiTop + 149, 35, 15, editingPart.pivot[0] + ""));
                        this.getTextField(81).floatsOnly = true;
                        this.getTextField(81).setMinMaxDefaultFloat(-Float.MAX_VALUE, Float.MAX_VALUE, 0);
                        this.addTextField(new GuiNpcTextField(82, this, guiLeft + 310, guiTop + 149, 35, 15, editingPart.pivot[1] + ""));
                        this.getTextField(82).floatsOnly = true;
                        this.getTextField(82).setMinMaxDefaultFloat(-Float.MAX_VALUE, Float.MAX_VALUE, 0);
                        this.addTextField(new GuiNpcTextField(83, this, guiLeft + 350, guiTop + 149, 35, 15, editingPart.pivot[2] + ""));
                        this.getTextField(83).floatsOnly = true;
                        this.getTextField(83).setMinMaxDefaultFloat(-Float.MAX_VALUE, Float.MAX_VALUE, 0);
                        //
                        //customized - button, enables all the following options.
                        this.addLabel(new GuiNpcLabel(52, "animation.customized", guiLeft + 270, guiTop + 174, 0xFFFFFF));
                        this.addButton(new GuiNpcButton(53, guiLeft + 325, guiTop + 168, 30, 20, new String[]{"gui.yes", "gui.no"}, editingPart.isCustomized() ? 0 : 1));
                        if (editingPart.isCustomized()) {
                            //
                            //speed - textfield
                            this.addLabel(new GuiNpcLabel(31, "stats.speed", guiLeft + 270, guiTop + 194, 0xFFFFFF));
                            this.addTextField(new GuiNpcTextField(32, this, guiLeft + 330, guiTop + 190, 30, 15, editingPart.speed + ""));
                            this.getTextField(32).floatsOnly = true;
                            this.getTextField(32).setMinMaxDefaultFloat(0, Float.MAX_VALUE, 1.0F);
                            //
                            //smooth - button
                            this.addLabel(new GuiNpcLabel(33, "animation.smoothing", guiLeft + 270, guiTop + 214, 0xFFFFFF));
                            this.addButton(new GuiNpcButton(34, guiLeft + 325, guiTop + 208, 60, 20, new String[]{"animation.smooth", "animation.linear", "gui.none"}, editingPart.smooth));
                        }
                    }
                }
            } else {
                this.addLabel(new GuiNpcLabel(50, "animation.addFrame", guiLeft + 270, guiTop + 100, 0xFFFFFF));
            }
        }
    }

    private Frame editingFrame() {
        if (this.animation == null || this.frameIndex >= this.animation.frames.size()) {
            return null;
        }
        return animation.frames.get(this.frameIndex);
    }

    private FramePart editingPart() {
        Frame editingFrame = this.editingFrame();
        if (this.animation == null || editingFrame == null) {
            return null;
        }
        return editingFrame.frameParts.get(this.editingPart);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton){
        super.actionPerformed(guibutton);
        Frame editingFrame = this.editingFrame();
        FramePart part = this.editingPart();
        int value = ((GuiNpcButton)guibutton).getValue();
        AnimationData data = npc.display.animationData;

        if (guibutton.id == 1) {
            Client.sendData(EnumPacketServer.AnimationAdd);
            frameIndex = 0;
        } else if (guibutton.id == 2 && scroll.getSelected() != null) {
            Client.sendData(EnumPacketServer.AnimationDelete, scroll.getSelected());
            this.playingAnimation = false;
            data.animation = null;
            animation = null;
            frameIndex = 0;
        } else if (guibutton.id == 11) {
            if (frameIndex < animation.frames.size() - 1) {
                animation.frames.add(frameIndex + 1, new Frame(10));
            } else {
                animation.frames.add(new Frame(10));
            }
        } else if (guibutton.id == 12) {
            animation.frames.remove(frameIndex);
        } else if (guibutton.id == 13 && frameIndex > 0) {
            frameIndex--;
        } else if (guibutton.id == 14 || guibutton.id == 15) {
            frameIndex++;
        } else if (guibutton.id == 20) {
            editingMode++;
        } else if (guibutton.id >= 60 && guibutton.id <= 66) {//Animation part buttons
            EnumAnimationPart enumPart = EnumAnimationPart.values()[guibutton.id-60];
            if (editingFrame != null && !editingFrame.frameParts.containsKey(enumPart)) {
                FramePart framePart = new FramePart(enumPart);
                framePart.prevRotations = new float[]{0, 0, 0};
                editingFrame.addPart(framePart);
            }
            this.editingPart = enumPart;
        } else if (guibutton.id == 68 && editingFrame != null) {
            editingFrame.removePart(this.editingPart.name());
        } else if (guibutton.id == 53 && editingFrame != null) {
            if (editingMode == 1) {
                editingFrame.setCustomized(!editingFrame.isCustomized());
            } else if (editingMode == 2 && part != null) {
                part.setCustomized(!part.isCustomized());
            }
        } else if (guibutton.id == 34) {
            if (editingMode == 0) {
                animation.smooth = (byte) value;
            } else if (editingMode == 1 && editingFrame != null) {
                editingFrame.smooth = (byte) value;
            } else if (editingMode == 2 && part != null) {
                part.smooth = (byte) value;
            }
        } else if (guibutton.id == 36) {
            if (editingMode == 0) {
                animation.renderTicks = value == 0;
            } else if (editingMode == 1 && editingFrame != null) {
                editingFrame.renderTicks = value == 0;
            }
        } else if (guibutton.id == 38) {
            animation.loop++;
        } else if (guibutton.id == 40) {
            animation.whileStanding = value == 0;
        } else if (guibutton.id == 42) {
            animation.whileAttacking = value == 0;
        } else if (guibutton.id == 44) {
            animation.whileMoving = value == 0;
        } else if (guibutton.id == 91) {
            if (!this.playingAnimation || !data.isActive()) {
                animation.currentFrame = 0;
                animation.currentFrameTime = 0;
                for (Frame frame : animation.frames) {
                    for (FramePart framePart : frame.frameParts.values()) {
                        framePart.prevRotations = new float[]{0,0,0};
                        framePart.prevPivots = new float[]{0, 0, 0};
                    }
                }
            }
            this.playingAnimation = true;
            data.animation = animation;
            data.animation.paused = false;
        } else if (guibutton.id == 92) {
            data.animation.paused = true;
        } else if (guibutton.id == 93) {
            this.playingAnimation = false;
            data.animation.paused = false;
        }

        initGui();
    }

    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
        super.drawScreen(par1,par2,par3);
        AnimationData data = npc.display.animationData;
        if (!data.isActive()) {
            this.playingAnimation = false;
            initGui();
        } else {
            Frame currentFrame = (Frame) data.animation.currentFrame();
            long time = mc.theWorld.getWorldTime();
            if (time != prevTick) {
                if (currentFrame != null && !currentFrame.renderTicks) {
                    data.animation.increaseTime();
                }
                GuiNpcLabel label = this.getLabel(94);
                if (label != null) {
                    label.label += ".";
                    if (label.label.length()%4 == 0) {
                        label.label = "";
                    }
                }
            }
            prevTick = time;
        }
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
        if (scroll.id == 0) {
            if (animation != null) {
                if (!prevSelected.isEmpty()) {
                    Client.sendData(EnumPacketServer.AnimationSave, prevSelected, animation.writeToNBT());
                }
            }
            Client.sendData(EnumPacketServer.AnimationGet, scroll.getSelected());
            frameIndex = 0;
            prevSelected = scroll.getSelected();
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        String text = textfield.getText();
        Frame frame = this.editingFrame();
        FramePart part = this.editingPart();

        if (textfield.id == 30 && !text.isEmpty()) {
            animation.name = text.replaceAll("[^a-zA-Z0-9_-]", "_");
        } else if (textfield.id == 32) {
            if (editingMode == 0) {
                animation.speed = textfield.getFloat();
            } else if (editingMode == 1 && frame != null) {
                frame.speed = textfield.getFloat();
            } else if (editingMode == 2 && part != null) {
                part.speed = textfield.getFloat();
            }
        } else if (textfield.id == 51 && frame != null) {
            frame.duration = textfield.getInteger();
        } else if (textfield.id >= 71 && textfield.id <= 73 && part != null) {
            part.rotation[textfield.id-71] = textfield.getFloat();
        } else if (textfield.id >= 81 && textfield.id <= 83 && part != null) {
            part.pivot[textfield.id-81] = textfield.getFloat();
        }
    }

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data) {
        scroll.setList(list);
        initGui();
    }

    @Override
    public void setSelected(String selected) {

    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        this.animation = new Animation();
        animation.readFromNBT(compound);
        initGui();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (animation != null) {
            Client.sendData(EnumPacketServer.AnimationSave, prevSelected, animation.writeToNBT());
        }
    }
}
