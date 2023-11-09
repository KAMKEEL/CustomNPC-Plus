package noppes.npcs.client.gui.global;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.SubGuiAnimationFrame;
import noppes.npcs.client.gui.SubGuiAnimationOptions;
import noppes.npcs.controllers.data.AnimationData;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumAnimationPart;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.controllers.data.Frame;
import noppes.npcs.controllers.data.FramePart;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ValueUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class GuiNPCEditAnimation extends GuiModelInterface implements ITextfieldListener, ISliderListener {
    private final Animation animation;

    private EnumAnimationPart editingPart = EnumAnimationPart.HEAD;
    private int frameIndex = 0;
    public boolean playingAnimation = false;
    private Frame prevFrame;
    private GuiScreen parent;
    private long prevTick;
    private static int partEditMode;
    private static int sliderSelection = 0;

    private GuiNpcSlider[] rotationSliders = new GuiNpcSlider[3];
    private GuiNpcSlider[] pivotSliders = new GuiNpcSlider[3];

    public GuiNPCEditAnimation(GuiScreen parent, Animation animation, EntityNPCInterface npc) {
        super((EntityCustomNpc) npc);
        this.parent = parent;
        xOffset = 0;
        yOffset = -21;

        this.animation = animation;
        AnimationData data = npc.display.animationData;
        data.animation = animation;
        data.setEnabled(true);

        int bodyPartX = 280;
        int bodyPartY = -5;
        for (int i = 0; i < 3; i++) {
            int yOffset = 20;
            if(i != 0)
                yOffset += 3;
            yOffset *= i;
            this.rotationSliders[i] = new GuiNpcSlider(this, 90 + i, guiLeft + bodyPartX, guiTop + bodyPartY + 115 + yOffset, 0.5F);
            this.pivotSliders[i] = new GuiNpcSlider(this, 95 + i, guiLeft + bodyPartX, guiTop + bodyPartY + 115 + yOffset, 0.5F);
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        if (animation == null) return;

        frameIndex = !animation.frames.isEmpty() ? frameIndex % animation.frames.size() : 0;

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

        if (animation.loop >= animation.frames.size()) {
            animation.loop = -1;
        }


        this.addLabel(new GuiNpcLabel(10, "animation.frames", guiLeft + 40, guiTop + 176 - 10, 0xFFFFFF));
        this.addButton(new GuiNpcButton(11, guiLeft - 10, guiTop + 189 - 10, 45, 20, "gui.add"));
        if (!animation.frames.isEmpty()) {
            this.addButton(new GuiNpcButton(12, guiLeft + 35, guiTop + 189 - 10, 45, 20, "gui.remove"));
            this.addButton(new GuiNpcButton(13, guiLeft + 80, guiTop + 189 - 10, 45, 20, "gui.copy"));
            this.addButton(new GuiNpcButton(14, guiLeft - 10, guiTop + 210 - 3, 20, 20, "<"));
            this.addTextField(new GuiNpcTextField(15, this, guiLeft + 15, guiTop + 212 - 3, 20, 17, frameIndex + ""));
            this.getTextField(15).integersOnly = true;
            this.getTextField(15).setMinMaxDefault(0, animation.frames.size() - 1, frameIndex);
            this.addButton(new GuiNpcButton(16, guiLeft + 40, guiTop + 210 - 3, 20, 20, ">"));
        }

        int playPauseX = 330;
        int playPauseY = 10;
        String animTexture = "customnpcs:textures/gui/animation.png";
        if (data.animation != null && data.animation.frames.size() > 0) {
            if (!this.playingAnimation || data.animation.paused) {//Play
                this.addLabel(new GuiNpcLabel(210, data.animation.paused ? "animation.paused" : "animation.stopped", guiLeft + playPauseX - 15, guiTop + playPauseY + 203, 0xFFFFFF));
                if (data.animation.paused) {
                    this.addLabel(new GuiNpcLabel(211, "", guiLeft + playPauseX + 21, guiTop + playPauseY + 203, 0xFFFFFF));
                }
                this.addButton(new GuiTexturedButton(200, "", guiLeft + playPauseX + 35, guiTop + playPauseY + 197, 11, 20, animTexture, 18, 71));
            } else {//Pause
                this.addLabel(new GuiNpcLabel(212, "animation.playing", guiLeft + playPauseX - 15, guiTop + playPauseY + 203, 0xFFFFFF));
                this.addLabel(new GuiNpcLabel(213, "", guiLeft + playPauseX + 20, guiTop + playPauseY + 203, 0xFFFFFF));
                this.addButton(new GuiTexturedButton(201, "", guiLeft + playPauseX + 35, guiTop + playPauseY + 197, 14, 20, animTexture, 0, 71));
            }
            if (this.playingAnimation) {//Stop
                this.addButton(new GuiTexturedButton(202, "", guiLeft + playPauseX + 55, guiTop + playPauseY + 197, 14, 20, animTexture, 33, 71));
            }
        }

        //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

        int animationX = -10;
        int animationY = -2;

        //name - textfield
        //      Send the old animation name (string selected on the scroll).
        //      On the server side, if the new animation's name is different from the old one but already exists,
        //      do not change the name (set the name to the old animation name after the animation is read from NBT).
        this.addTextField(new GuiNpcTextField(30, this, guiLeft + animationX, guiTop + animationY, 120, 15, animation.name));
        //
        //speed - textfield
        this.addLabel(new GuiNpcLabel(31, "stats.speed", guiLeft + animationX, guiTop + animationY + 24, 0xFFFFFF));
        this.addTextField(new GuiNpcTextField(31, this, guiLeft + animationX + 88, guiTop + animationY + 22, 30, 15, animation.speed + ""));
        this.getTextField(31).floatsOnly = true;
        this.getTextField(31).setMinMaxDefaultFloat(0,Float.MAX_VALUE,1.0F);
        //
        //smooth - button
        this.addLabel(new GuiNpcLabel(32, "animation.smoothing", guiLeft + animationX, guiTop + animationY + 46, 0xFFFFFF));
        this.addButton(new GuiNpcButton(32, guiLeft + animationX + 55, guiTop + animationY + 40, 65, 20, new String[]{"animation.smooth", "animation.linear", "gui.none"}, animation.smooth));
        //
        //loop - button
        this.addLabel(new GuiNpcLabel(33, "animation.loop", guiLeft + animationX, guiTop + animationY + 68, 0xFFFFFF));
        this.addButton(new GuiNpcButton(33, guiLeft + animationX + 55, guiTop + animationY + 62, 65, 20, animation.loop == -1 ? "gui.none" : "Frame " + animation.loop));

        this.addButton(new GuiNpcButton(34,guiLeft + animationX, guiTop + animationY + 84, 120, 20, "animation.animationOptions"));

        //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

        if (editingFrame != null) {
            int frameX = animationX;
            int frameY = playPauseY + 100;

            this.addLabel(new GuiNpcLabel(50, "animation.frame", guiLeft + frameX + 50, guiTop + frameY - 3, 0xFFFFFF));
            //
            //duration - textfield
            this.addLabel(new GuiNpcLabel(51, "animation.duration", guiLeft + frameX, guiTop + frameY + 15, 0xFFFFFF));
            this.addTextField(new GuiNpcTextField(51, this, guiLeft + frameX + 88, guiTop + frameY + 11, 30, 15, editingFrame.duration + ""));
            this.getTextField(51).integersOnly = true;
            this.getTextField(51).setMinMaxDefaultFloat(0, Integer.MAX_VALUE, 10);

            this.addButton(new GuiNpcButton(52,guiLeft + frameX, guiTop + frameY + 31 , 120, 20, "animation.frameOptions"));

            int bodyPartX = 280;
            int bodyPartY = -5;

            //
            //6 body part textured buttons
            //Add/remove part button
            //      6 custom GUI textured buttons for each body part determine "this.currentPart (enum)".
            //      If the current frame doesn't have that part, the button will say "Add part"
            //      otherwise, the button will say "Remove", and more options for editing the part become available:
            //

            //Head
            this.addButton(new GuiTexturedButton(60, "",guiLeft + bodyPartX, guiTop + bodyPartY,22,23, animTexture,0,0));
            //Body
            this.addButton(new GuiTexturedButton(61, "",guiLeft + bodyPartX, guiTop + bodyPartY + 23,22,23, animTexture,24,0));
            //Right Arm
            this.addButton(new GuiTexturedButton(62, "",guiLeft + bodyPartX - 9, guiTop + bodyPartY + 23,8,23, animTexture,48,0));
            //Left Arm
            this.addButton(new GuiTexturedButton(63, "",guiLeft + bodyPartX + 26, guiTop + bodyPartY + 23,8,23, animTexture,48,0));
            //Right Leg
            this.addButton(new GuiTexturedButton(64, "",guiLeft + bodyPartX + 1, guiTop + bodyPartY + 48,10,23, animTexture,58,0));
            //Left Leg
            this.addButton(new GuiTexturedButton(65, "",guiLeft + bodyPartX + 12, guiTop + bodyPartY + 48,10,23, animTexture,58,0));
            //Full Body
            this.addButton(new GuiTexturedButton(66, "",guiLeft + bodyPartX + 40, guiTop + bodyPartY + 2,17,23, animTexture,70,0));

            for (int i = 0; i < 7; i++) {
                if (this.buttons.containsKey(60 + i)) {
                    ((GuiTexturedButton) this.getButton(60 + i)).scale = 1.2F;
                    if (!editingFrame.frameParts.containsKey(EnumAnimationPart.values()[i])) {
                        ((GuiTexturedButton) this.getButton(60 + i)).textureX += 96;
                    }
                }
            }

            //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
            //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
            //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

            if (editingPart != null) {
                //remove part button
                this.addLabel(new GuiNpcLabel(67, editingPart.part.name(), guiLeft + bodyPartX + 65, guiTop + bodyPartY + 20, 0xFFFFFF));
                this.addButton(new GuiNpcButton(67, guiLeft + bodyPartX + 45, guiTop + bodyPartY + 35, 60, 20, "gui.remove"));

                this.addButton(new GuiNpcButton(68, guiLeft + bodyPartX + 45, guiTop + bodyPartY + 57, 60, 20, new String[] {"Sliders","Manual"}, partEditMode));

                if (partEditMode == 0) {
                    this.addButton(new GuiNpcButton(69, guiLeft + bodyPartX, guiTop + bodyPartY + 80, 60, 20, "model.rotate"));
                    this.addButton(new GuiNpcButton(70, guiLeft + bodyPartX + 62, guiTop + bodyPartY + 80, 60, 20, "model.pivot"));

                    this.getButton(69).setEnabled(sliderSelection == 1);
                    this.getButton(70).setEnabled(sliderSelection == 0);

                    if(sliderSelection == 0){
                        for (int i = 0; i < 3; i++) {
                            this.rotationSliders[i].width = 110;
                            this.rotationSliders[i].height= 20;
                            this.rotationSliders[i].xPosition = guiLeft + bodyPartX + 10;
                            this.addSlider(this.rotationSliders[i]);
                        }

                        this.addLabel(new GuiNpcLabel(90, "X", guiLeft + bodyPartX, guiTop + bodyPartY + 112, 0xFFFFFF));
                        this.addLabel(new GuiNpcLabel(91, "Y", guiLeft + bodyPartX, guiTop + bodyPartY + 135, 0xFFFFFF));
                        this.addLabel(new GuiNpcLabel(92, "Z", guiLeft + bodyPartX, guiTop + bodyPartY + 157, 0xFFFFFF));
                    }
                    else {
                        for (int i = 0; i < 3; i++) {
                            this.pivotSliders[i].width = 110;
                            this.pivotSliders[i].height = 20;
                            this.pivotSliders[i].xPosition = guiLeft + bodyPartX + 10;
                            this.addSlider(this.pivotSliders[i]);
                        }

                        this.addLabel(new GuiNpcLabel(95, "X", guiLeft + bodyPartX, guiTop + bodyPartY + 112, 0xFFFFFF));
                        this.addLabel(new GuiNpcLabel(96, "Y", guiLeft + bodyPartX, guiTop + bodyPartY + 135, 0xFFFFFF));
                        this.addLabel(new GuiNpcLabel(97, "Z", guiLeft + bodyPartX, guiTop + bodyPartY + 157, 0xFFFFFF));
                    }
                } else {
                    //
                    //rotation - 3 textfields
                    this.addLabel(new GuiNpcLabel(70, "animation.rotations", guiLeft + bodyPartX, guiTop + bodyPartY + 85, 0xFFFFFF));
                    this.addTextField(new GuiNpcTextField(70, this, guiLeft + bodyPartX, guiTop + bodyPartY + 97, 35, 15, editingPart.rotation[0] + ""));
                    this.getTextField(70).floatsOnly = true;
                    this.getTextField(70).setMinMaxDefaultFloat(-Float.MAX_VALUE, Float.MAX_VALUE, 0);
                    this.addTextField(new GuiNpcTextField(71, this, guiLeft + bodyPartX + 40, guiTop + bodyPartY + 97, 35, 15, editingPart.rotation[1] + ""));
                    this.getTextField(71).floatsOnly = true;
                    this.getTextField(71).setMinMaxDefaultFloat(-Float.MAX_VALUE, Float.MAX_VALUE, 0);
                    this.addTextField(new GuiNpcTextField(72, this, guiLeft + bodyPartX + 80, guiTop + bodyPartY + 97, 35, 15, editingPart.rotation[2] + ""));
                    this.getTextField(72).floatsOnly = true;
                    this.getTextField(72).setMinMaxDefaultFloat(-Float.MAX_VALUE, Float.MAX_VALUE, 0);
                    //
                    //pivot - 3 textfields
                    this.addLabel(new GuiNpcLabel(80, "animation.pivots", guiLeft + bodyPartX, guiTop + bodyPartY + 117, 0xFFFFFF));
                    this.addTextField(new GuiNpcTextField(80, this, guiLeft + bodyPartX, guiTop + bodyPartY + 129, 35, 15, editingPart.pivot[0] + ""));
                    this.getTextField(80).floatsOnly = true;
                    this.getTextField(80).setMinMaxDefaultFloat(-Float.MAX_VALUE, Float.MAX_VALUE, 0);
                    this.addTextField(new GuiNpcTextField(81, this, guiLeft + bodyPartX + 40, guiTop + bodyPartY + 129, 35, 15, editingPart.pivot[1] + ""));
                    this.getTextField(81).floatsOnly = true;
                    this.getTextField(81).setMinMaxDefaultFloat(-Float.MAX_VALUE, Float.MAX_VALUE, 0);
                    this.addTextField(new GuiNpcTextField(82, this, guiLeft + bodyPartX + 80, guiTop + bodyPartY + 129, 35, 15, editingPart.pivot[2] + ""));
                    this.getTextField(82).floatsOnly = true;
                    this.getTextField(82).setMinMaxDefaultFloat(-Float.MAX_VALUE, Float.MAX_VALUE, 0);
                    //
                    //customized - button, enables all the following options.
                    this.addLabel(new GuiNpcLabel(83, "animation.customized", guiLeft + bodyPartX, guiTop + bodyPartY + 154, 0xFFFFFF));
                    this.addButton(new GuiNpcButton(83, guiLeft + bodyPartX + 55, guiTop + bodyPartY + 148, 30, 20, new String[]{"gui.yes", "gui.no"}, editingPart.isCustomized() ? 0 : 1));
                    if (editingPart.isCustomized()) {
                        //
                        //speed - textfield
                        this.addLabel(new GuiNpcLabel(84, "stats.speed", guiLeft + bodyPartX, guiTop + bodyPartY + 174, 0xFFFFFF));
                        this.addTextField(new GuiNpcTextField(84, this, guiLeft + bodyPartX + 60, guiTop + bodyPartY + 170, 30, 15, editingPart.speed + ""));
                        this.getTextField(84).floatsOnly = true;
                        this.getTextField(84).setMinMaxDefaultFloat(0, Float.MAX_VALUE, 1.0F);
                        //
                        //smooth - button
                        this.addLabel(new GuiNpcLabel(85, "animation.smoothing", guiLeft + bodyPartX, guiTop + bodyPartY + 194, 0xFFFFFF));
                        this.addButton(new GuiNpcButton(85, guiLeft + bodyPartX + 55, guiTop + bodyPartY + 190, 60, 20, new String[]{"animation.smooth", "animation.linear", "gui.none"}, editingPart.smooth));
                    }
                }
            }

            //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
            //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
            //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        } else {
            this.addLabel(new GuiNpcLabel(50, "animation.addFrame", guiLeft + 270, guiTop + 100, 0xFFFFFF));
        }
    }

    private void updateSliders() {
        FramePart part = this.editingPart();
        if (part == null) return;

        float[] rotations = part.getRotations();
        float[] pivots = part.getPivots();
        for (int i = 0; i < 3; i++) {
            this.rotationSliders[i].sliderValue = ((rotations[i] / 360.0F) + 1) / 2.0F;
            this.pivotSliders[i].sliderValue = (((ValueUtil.clamp(pivots[i], -100, 100)) / 100.0F) + 1) / 2.0F;
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
        int value = guibutton instanceof GuiNpcButton ? ((GuiNpcButton)guibutton).getValue() : 0;
        AnimationData data = npc.display.animationData;

        if (guibutton.id == 11) {
            if (frameIndex < animation.frames.size() - 1) {
                animation.frames.add(frameIndex + 1, new Frame(10));
            } else {
                animation.frames.add(new Frame(10));
            }
        } else if (guibutton.id == 12) {
            animation.frames.remove(frameIndex);
        } else if (guibutton.id == 13 && editingFrame != null) {
            if (frameIndex < animation.frames.size() - 1) {
                animation.frames.add(frameIndex + 1, editingFrame.copy());
            } else {
                animation.frames.add(editingFrame.copy());
            }
        } else if (guibutton.id == 14) {
            frameIndex--;
            if (frameIndex == -1) {
                frameIndex = animation.frames.size() - 1;
            }
        } else if (guibutton.id == 16) {
            frameIndex++;
        } else if (guibutton.id >= 60 && guibutton.id <= 66) {//Animation part buttons
            EnumAnimationPart enumPart = EnumAnimationPart.values()[guibutton.id-60];
            if (editingFrame != null && !editingFrame.frameParts.containsKey(enumPart)) {
                FramePart framePart = new FramePart(enumPart);
                framePart.prevRotations = new float[]{0, 0, 0};
                editingFrame.addPart(framePart);
            }
            this.editingPart = enumPart;
            this.updateSliders();
        } else if (guibutton.id == 67 && editingFrame != null) {
            editingFrame.removePart(this.editingPart.name());
        } else if (guibutton.id == 68 && editingFrame != null) {
            if (partEditMode == 1) {
                this.updateSliders();
            }
            partEditMode++;
            partEditMode %= 2;
        }  else if (guibutton.id == 69 && editingFrame != null) {
            if (partEditMode == 1) {
                this.updateSliders();
            }

            sliderSelection = 0;
        }  else if (guibutton.id == 70 && editingFrame != null) {
            if (partEditMode == 1) {
                this.updateSliders();
            }

            sliderSelection = 1;
        } else if (guibutton.id == 83 && editingFrame != null && part != null) {
            part.setCustomized(!part.isCustomized());
        } else if (guibutton.id == 32) {
            animation.smooth = (byte) value;
        } else if (guibutton.id == 85 && part != null) {
            part.smooth = (byte) value;
        } else if (guibutton.id == 33) {
            animation.loop++;
        } else if (guibutton.id == 200) {
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
        } else if (guibutton.id == 201) {
            data.animation.paused = true;
        } else if (guibutton.id == 202) {
            this.playingAnimation = false;
            data.animation.paused = false;
        } else if (guibutton.id == 34) {
            setSubGui(new SubGuiAnimationOptions(this.animation));
        } else if (guibutton.id == 52) {
            setSubGui(new SubGuiAnimationFrame(editingFrame));
        }

        initGui();
    }

    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
        super.drawScreen(par1,par2,par3);
        AnimationData data = npc.display.animationData;
        if (!data.isActive() && this.playingAnimation) {
            this.playingAnimation = false;
            initGui();
        } else if (data.isActive()) {
            Frame currentFrame = (Frame) data.animation.currentFrame();
            long time = mc.theWorld.getTotalWorldTime();
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
    public void unFocused(GuiNpcTextField textfield) {
        String text = textfield.getText();
        Frame frame = this.editingFrame();
        FramePart part = this.editingPart();

        if (textfield.id == 30 && !text.isEmpty()) {
            animation.name = text.replaceAll("[^a-zA-Z0-9_-]", "_");
        } else if (textfield.id == 15 && animation != null && animation.frames.size() > 0) {
            animation.frames.remove(frameIndex);
            animation.frames.add(textfield.getInteger(),frame);
            frameIndex = textfield.getInteger();
        } else if (textfield.id == 31) {
            animation.speed = textfield.getFloat();
        } else if (textfield.id == 84 && part != null) {
            part.speed = textfield.getFloat();
        } else if (textfield.id == 51 && frame != null) {
            frame.duration = textfield.getInteger();
        } else if (textfield.id >= 70 && textfield.id <= 73 && part != null) {
            part.rotation[textfield.id-70] = textfield.getFloat();
            this.updateSliders();
        } else if (textfield.id >= 80 && textfield.id <= 83 && part != null) {
            part.pivot[textfield.id-80] = textfield.getFloat();
            this.updateSliders();
        }
    }

    @Override
    public void close() {
        if (!this.hasSubGui()) {
            if (animation != null) {
                Client.sendData(EnumPacketServer.AnimationSave, animation.writeToNBT());
            }
            displayGuiScreen(parent);
        } else {
            closeSubGui(this.getSubGui());
        }
    }

    @Override
    public void mouseDragged(GuiNpcSlider guiNpcSlider) {
        FramePart part = this.editingPart();
        if (part == null) {
            return;
        }
        int id = guiNpcSlider.id;
        if (id >= 90 && id < 93) {
            part.rotation[id - 90] = 360.0F * 2.0F * (guiNpcSlider.sliderValue - 0.5F);
        }
        if (id >= 95 && id < 98) {
            part.pivot[id - 95] = 100.0F * (guiNpcSlider.sliderValue - 0.5F);
        }
    }

    @Override
    public void mousePressed(GuiNpcSlider guiNpcSlider) {

    }

    @Override
    public void mouseReleased(GuiNpcSlider guiNpcSlider) {

    }
}
