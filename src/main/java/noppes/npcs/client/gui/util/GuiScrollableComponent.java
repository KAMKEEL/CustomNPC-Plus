package noppes.npcs.client.gui.util;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ChatComponentText;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.CustomNPCsException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiScrollableComponent extends GuiNPCInterface{

    protected ScaledResolution scaledResolution;
    public int clipWidth;
    public int clipHeight;

    protected int xPos = 0;
    protected int yPos = 0;

    protected int maxScrollY = 0;
    protected float scrollY = 0;
    protected float nextScrollY = 0;

    private double lerp(double a, double b, double lambda){
        return a + lambda * (b - a);
    }

    public GuiScrollableComponent(EntityNPCInterface npc, int posX, int posY, int clipWidth, int clipHeight, int maxScroll){
        super(npc);
        this.xPos = posX;
        this.yPos = posY;
        this.clipWidth = clipWidth;
        this.clipHeight = clipHeight;
        this.maxScrollY = maxScroll;
    }

    @Override
    public void save() {

    }

    @Override
    public void initGui(){
        super.initGui();
        scaledResolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

        addButton(new GuiNpcButton(0, 0, 0, "Testtt"));

        float scrollPerc = 0;
        if((guiTop+clipHeight) != 0){
            scrollPerc = (nextScrollY) / (clipHeight);
        }

        nextScrollY = scrollPerc * (clipHeight);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        scrollY = (float) lerp(scrollY, nextScrollY, partialTicks);
        if(Float.isNaN(scrollY))
            scrollY = 0;

        boolean drawBackground = this.drawDefaultBackground;

        GL11.glPushMatrix();

        //Enable clipping
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        setClip();

        this.drawDefaultBackground = false;
        if(drawBackground)
            this.drawDefaultBackground();

        GL11.glTranslatef(xPos, yPos+scrollY, 0);
        super.drawScreen(mouseX-xPos, (int) (mouseY-scrollY-yPos), partialTicks);

        this.drawDefaultBackground = drawBackground;

        //Disable clipping (VERY IMPORTANT)
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glTranslatef(0, 0, 0);

        GL11.glPopMatrix();
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks, int mouseScroll){
        adjustScroll(mouseScroll);

        this.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void adjustScroll(int mouseScroll) {
        nextScrollY -= (float) mouseScroll/25;
        if(nextScrollY < 0)
            nextScrollY = 0;
        if(nextScrollY > maxScrollY) {
            nextScrollY = maxScrollY;
        }
    }

    @Override
    public void addScrollableGui(int id, GuiScrollableComponent d){
        throw new CustomNPCsException("You can't put scrollable GUIs into a scrollable GUI");
    }

    @Override
    public void buttonEvent(GuiButton button){
        mc.thePlayer.addChatMessage(new ChatComponentText("LOL"));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton){
        super.mouseClicked(mouseX-xPos, (int) (mouseY-yPos-scrollY), mouseButton);
    }

    @Override
    public void handleMouseInput(){
        super.handleMouseInput();

        adjustScroll(Mouse.getEventDWheel());
    }

    public boolean isMouseOver(int mouseX, int mouseY){
        return mouseX >= xPos && mouseX <= clipWidth+xPos && mouseY >= yPos && mouseY <= clipHeight+yPos;
    }

    protected void setClip(){
        setClip(xPos, yPos, clipWidth, clipHeight);
    }

    protected void setClip(int x, int y, int width, int height){
        if(scaledResolution == null)
            return;
        int scaleFactor = scaledResolution.getScaleFactor();

        //Correct the positions for Screen Space in OpenGL
        x*=scaleFactor;
        y*=scaleFactor;
        width*=scaleFactor;
        height*=scaleFactor;

        //Adjust position to Top-Left origin (OpenGL window/screen space uses bottom-left origin)
        y=mc.displayHeight-y;

        //Set clip
        GL11.glScissor(x, y-height, width, height);
    }
}
