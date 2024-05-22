package noppes.npcs.client.gui.util;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import java.util.ArrayList;
import java.util.HashMap;

public class GuiScrollWindow extends GuiScreen implements ITextfieldListener, ICustomScrollListener, GuiYesNoCallback
{
    public static final ResourceLocation resource = new ResourceLocation("customnpcs","textures/gui/misc.png");

    protected GuiNPCInterface parent;
	public boolean drawDefaultBackground = true;
	public EntityNPCInterface npc;
	protected HashMap<Integer,GuiNpcButton> buttons = new HashMap<Integer,GuiNpcButton>();
    protected HashMap<Integer,GuiNpcTextField> textfields = new HashMap<Integer,GuiNpcTextField>();
    protected HashMap<Integer,GuiNpcLabel> labels = new HashMap<Integer,GuiNpcLabel>();
    protected HashMap<Integer,GuiCustomScroll> scrolls = new HashMap<Integer,GuiCustomScroll>();
    protected HashMap<Integer,GuiNpcSlider> sliders = new HashMap<Integer,GuiNpcSlider>();

    protected ScaledResolution scaledResolution;

    // Window Size
    public int clipWidth;
    public int clipHeight;

    // Location
    public int xPos = 0;
    public int yPos = 0;

    public int maxScrollY = 0;
    public float scrollY = 0;
    public float nextScrollY = 0;
    public int mouseScroll;
    boolean isScrolling;

    /**
     * Scrollable GUI component :P?
     * @param posX the X Position of the gui element on the screen
     * @param posY the Y Position of the GUI element on the screen
     * @param clipWidth width of the element
     * @param clipHeight height of the element
     * @param maxScroll extra Y area displayed through the scrollbar.
     */
    public GuiScrollWindow(GuiNPCInterface parent, int posX, int posY, int clipWidth, int clipHeight, int maxScroll){
        this.parent = parent;
        this.xPos = posX;
        this.yPos = posY;
        this.clipWidth = clipWidth;
        this.clipHeight = clipHeight;
        this.maxScrollY = maxScroll;
    }


    @Override
    public void initGui(){
    	super.initGui();

        scaledResolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

        float scrollPerc = 0;
        if((yPos+clipHeight) != 0){
            scrollPerc = (nextScrollY) / (clipHeight);
        }

        nextScrollY = scrollPerc * (clipHeight);

    	GuiNpcTextField.unfocus();
        buttonList.clear();
        labels.clear();
        textfields.clear();
        buttons.clear();
        scrolls.clear();
        sliders.clear();
        Keyboard.enableRepeatEvents(true);
    }
    @Override
    public void updateScreen(){
        for(GuiNpcTextField tf : textfields.values()){
            if(tf.enabled)
                tf.updateCursorCounter();
        }
        super.updateScreen();
    }

    public void mouseClicked(int i, int j, int k)
    {

        i = i - xPos;
        j = (int) (j-yPos+scrollY);

        for(GuiNpcTextField tf : new ArrayList<GuiNpcTextField>(textfields.values()))
            if(tf.enabled)
                tf.mouseClicked(i, j, k);

        if (k == 0){
            for(GuiCustomScroll scroll : new ArrayList<GuiCustomScroll>(scrolls.values())){
                scroll.mouseClicked(i, j, k);
            }
        }
        mouseEvent(i,j,k);
        super.mouseClicked(i, j, k);
    }

    public void mouseEvent(int i, int j, int k){};

    @Override
	protected void actionPerformed(GuiButton guibutton) {
        parent.buttonEvent(guibutton);
	}
    public void buttonEvent(GuiButton guibutton){
        parent.buttonEvent(guibutton);
    };

    @Override
	public void keyTyped(char c, int i){
    	for(GuiNpcTextField tf : textfields.values())
    			tf.textboxKeyTyped(c, i);
    }


    public void addButton(GuiNpcButton button){
    	buttons.put(button.id,button);
    	buttonList.add(button);
    }
	public GuiNpcButton getButton(int i) {
		return buttons.get(i);
	}
    public void addTextField(GuiNpcTextField tf){
    	textfields.put(tf.id,tf);
    }
    public GuiNpcTextField getTextField(int i){
    	return textfields.get(i);
    }
    public void addLabel(GuiNpcLabel label) {
		labels.put(label.id, label);
	}
    public GuiNpcLabel getLabel(int i){
    	return labels.get(i);
    }

    public void addSlider(GuiNpcSlider slider){
		sliders.put(slider.id,slider);
    	buttonList.add(slider);
    }
	public GuiNpcSlider getSlider(int i) {
		return sliders.get(i);
	}
	public void addScroll(GuiCustomScroll scroll) {
        scroll.setWorldAndResolution(mc, 350, 250);
        scrolls.put(scroll.id, scroll);
	}
	public GuiCustomScroll getScroll(int id){
		return scrolls.get(id);
	}

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        scrollY = (float) lerp(scrollY, nextScrollY, partialTicks);
        boolean drawBackground = this.drawDefaultBackground;

        GL11.glPushMatrix();

        // Enable clipping
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        setClip(xPos, yPos, clipWidth, clipHeight);

        this.drawDefaultBackground = false;
        if(drawBackground)
            this.drawGradientRect(0, 0, this.width, this.height, 0x66000000, 0x88000000);


        if(maxScrollY > 0)
            setClip(xPos, yPos, clipWidth-13, clipHeight);

        GL11.glTranslatef(xPos, yPos-scrollY, 0);


        this.drawComponents(mouseX -xPos, (int) (mouseY +scrollY-yPos), partialTicks);


        this.drawDefaultBackground = drawBackground;

        //Disable clipping (VERY IMPORTANT)
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glTranslatef(-xPos, -yPos+scrollY, 0);

        if(maxScrollY > 0){
            drawScrollBar();
        }

        GL11.glPopMatrix();
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks, int mouseScroll){
        adjustScroll(mouseX, mouseY, mouseScroll);
        this.mouseScroll = mouseScroll;
        this.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void drawComponents(int i, int j, float f){
        if(drawDefaultBackground)
            drawDefaultBackground();

        boolean subGui = parent.hasSubGui();
        for(GuiNpcLabel label : labels.values())
            label.drawLabel(this,fontRendererObj);
        for(GuiNpcTextField tf : textfields.values()){
            tf.drawTextBox(i, j);
        }
        for(GuiCustomScroll scroll : scrolls.values()){
            scroll.updateSubGUI(subGui);
            scroll.drawScreen(i, j, f, !subGui && scroll.isMouseOver(i, j)?mouseScroll:0);
        }
        super.drawScreen(i, j, f);
        for(GuiCustomScroll scroll : scrolls.values())
            if(scroll.hoverableText){
                scroll.drawHover(i, j);
            }
        for(GuiNpcButton button : buttons.values()){
            button.updateSubGUI(subGui);
            if(!button.hoverableText.isEmpty()){
                button.drawHover(i, j, subGui);
            }
        }
    }

	public void elementClicked() {
        parent.elementClicked();
	}
	@Override
    public boolean doesGuiPauseGame(){
        return false;
    }

	public void doubleClicked() {}

	public boolean isInventoryKey(int i){
        return i == mc.gameSettings.keyBindInventory.getKeyCode(); //inventory key
	}

    @Override
	public void drawDefaultBackground() {
		super.drawDefaultBackground();
	}

    protected void setClip(int x, int y, int width, int height){
        if(scaledResolution == null)
            return;
        int scaleFactor = scaledResolution.getScaleFactor();


        // Correct the positions for Screen Space in OpenGL
        x*=scaleFactor;
        y*=scaleFactor;
        width*=scaleFactor;
        height*=scaleFactor;

        // Adjust position to Top-Left origin (OpenGL window/screen space uses bottom-left origin)
        y=mc.displayHeight-y;

        // Set clip
        GL11.glScissor(x, y-height, width, height);
    }

    public boolean isMouseOver(int mouseX, int mouseY){
        return mouseX >= xPos && mouseX <= clipWidth+xPos && mouseY >= yPos && mouseY <= clipHeight+yPos;
    }

    public void adjustScroll(int mouseX, int mouseY, int mouseScroll) {
        mouseX -= xPos;
        mouseY -= yPos;
        if(Mouse.isButtonDown(0)){
            if(mouseX >= clipWidth-9 && mouseX < clipWidth-4 && mouseY >= 4 && mouseY < clipHeight){
                isScrolling = true;
            }
        }
        else
            isScrolling = false;

        if(isScrolling){
            // This Scroll Y is wrong
            float proportion = (mouseY - 4) / (float) (clipHeight - 8);
            nextScrollY = (int) (proportion * maxScrollY);
            if(nextScrollY < 0){
                nextScrollY = 0;
            }
            if(nextScrollY > maxScrollY){
                nextScrollY = maxScrollY;
            }
        }

        for(GuiCustomScroll guiCustomScroll : scrolls.values()) {
            if (guiCustomScroll.isMouseOver(mouseX, (int) (mouseY + scrollY))) {
                return;
            }
        }

        if(mouseScroll != 0){
            nextScrollY -= (float) mouseScroll/15;
            if(nextScrollY > maxScrollY)
                nextScrollY = maxScrollY;
            if(nextScrollY < 0)
                nextScrollY = 0;
        }
    }

    private void drawScrollBar()
    {
        mc.renderEngine.bindTexture(resource);
        GL11.glColor4f(1, 1, 1, 1);

        int maxSize = (int) ((clipHeight) * ( (double) (clipHeight)/ (clipHeight + maxScrollY)));
        int x = xPos + clipWidth - 9;
        int y = (int) (yPos + 4 + (scrollY/maxScrollY * (clipHeight-maxSize-8)));

        for(int k = y; k < y+maxSize && k-clipHeight+maxSize < clipHeight + yPos;k++){
            drawTexturedModalRect(x, k, 176, 9, 5, 1);
        }
    }

    private double lerp(double a, double b, double lambda){
        return a + lambda * (b - a);
    }


    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if(parent instanceof ICustomScrollListener)
            ((ICustomScrollListener) parent).customScrollClicked(i, j, k, guiCustomScroll);
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
        if(parent instanceof ICustomScrollListener)
            ((ICustomScrollListener) parent).customScrollDoubleClicked(selection, scroll);
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if(parent instanceof ITextfieldListener)
            ((ITextfieldListener) parent).unFocused(textfield);
    }
}
