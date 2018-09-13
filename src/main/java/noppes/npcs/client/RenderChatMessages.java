package noppes.npcs.client;

import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import noppes.npcs.IChatMessages;
import noppes.npcs.CustomNpcs;
import noppes.npcs.entity.EntityNPCInterface;

import org.lwjgl.opengl.GL11;

public class RenderChatMessages implements IChatMessages{
	private Map<Long,TextBlockClient> messages = new TreeMap<Long,TextBlockClient>();
	
	private int boxLength = 46;
	private float scale = 0.5f;
	
	private String lastMessage = "";
	private long lastMessageTime = 0;
	
	@Override
	public void addMessage(String message, EntityNPCInterface npc){
		if(!CustomNpcs.EnableChatBubbles)
			return;
		long time = System.currentTimeMillis();
		if(message.equals(lastMessage) && lastMessageTime + 5000 > time){
			return;
		}
		Map<Long,TextBlockClient> messages = new TreeMap<Long,TextBlockClient>(this.messages);
		messages.put(time, new TextBlockClient(message, (int) (boxLength * 4), true, Minecraft.getMinecraft().thePlayer, npc));

		if(messages.size() > 3){
			messages.remove(messages.keySet().iterator().next());
		}
		this.messages = messages;
		lastMessage = message;
		lastMessageTime = time;
	}    

	@Override
	public void renderMessages(double par3, double par5, double par7, float textscale){
		Map<Long,TextBlockClient> messages = getMessages();
		if(messages.isEmpty())
			return;
		FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        float var13 = 1.6F;
        float var14 = 0.016666668F * var13;
        GL11.glPushMatrix();
        int size = 0;
        for(TextBlockClient block : messages.values())
        	size += block.lines.size();
        
        int textYSize = (int) (size * font.FONT_HEIGHT * scale);
        GL11.glTranslatef((float)par3 + 0.0F, (float)par5 + textYSize * textscale * var14, (float)par7);
        GL11.glScalef(textscale, textscale, textscale);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1F, 0.0F);
        GL11.glRotatef(RenderManager.instance.playerViewX, 1F, 0.0F, 0.0F);
        GL11.glScalef(-var14, -var14, var14);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        drawRect(-boxLength - 2, -2, boxLength + 2, textYSize + 1, 0xBBFFFFFF, 0.11);
        
        drawRect(-boxLength - 1, -3, boxLength + 1, -2, 0xFF000000, 0.1); //top
        drawRect(-boxLength - 1, textYSize + 2, -1, textYSize + 1, 0xFF000000, 0.1); //bottom1
        drawRect(3, textYSize + 2, boxLength + 1, textYSize + 1, 0xFF000000, 0.1); //bottom2
        drawRect(-boxLength - 3, -1, -boxLength - 2, textYSize, 0xFF000000, 0.1); //left
        drawRect(boxLength + 3, -1, boxLength + 2, textYSize, 0xFF000000, 0.1); //right

        drawRect(-boxLength - 2, -2, -boxLength - 1, -1, 0xFF000000, 0.1);
        drawRect(boxLength + 2, -2, boxLength + 1, -1, 0xFF000000, 0.1);
        drawRect(-boxLength - 2, textYSize + 1, -boxLength - 1, textYSize, 0xFF000000, 0.1);
        drawRect(boxLength + 2, textYSize + 1, boxLength + 1, textYSize, 0xFF000000, 0.1);

        drawRect(0, textYSize + 1, 3, textYSize + 4, 0xBBFFFFFF, 0.11);
        drawRect(-1, textYSize + 4, 1, textYSize + 5, 0xBBFFFFFF, 0.11);
        
        drawRect(-1, textYSize + 1, 0, textYSize + 4, 0xFF000000, 0.1);
        drawRect(3, textYSize + 1, 4, textYSize + 3, 0xFF000000, 0.1);
        drawRect(2, textYSize + 3, 3, textYSize + 4, 0xFF000000, 0.1);
        drawRect(1, textYSize + 4, 2, textYSize + 5, 0xFF000000, 0.1);
        drawRect(-2, textYSize + 4, -1, textYSize + 5, 0xFF000000, 0.1);
        
        drawRect(-2, textYSize + 5, 1, textYSize + 6, 0xFF000000, 0.1);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        
        GL11.glScalef(scale, scale, scale);
        int index = 0;
        for(TextBlockClient block : messages.values()){
        	for(IChatComponent chat : block.lines){
	        	String message = chat.getFormattedText();
	        	font.drawString(message, -font.getStringWidth(message) / 2, index * font.FONT_HEIGHT, 0);
	        	index++;
        	}
        }
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
        RenderHelper.enableStandardItemLighting();
        
    }    
	
	private void drawRect(int par0, int par1, int par2, int par3, int par4, double par5)
    {
        int j1;

        if (par0 < par2)
        {
            j1 = par0;
            par0 = par2;
            par2 = j1;
        }

        if (par1 < par3)
        {
            j1 = par1;
            par1 = par3;
            par3 = j1;
        }

        float f = (float)(par4 >> 24 & 255) / 255.0F;
        float f1 = (float)(par4 >> 16 & 255) / 255.0F;
        float f2 = (float)(par4 >> 8 & 255) / 255.0F;
        float f3 = (float)(par4 & 255) / 255.0F;
        Tessellator tessellator = Tessellator.instance;
        GL11.glColor4f(f1, f2, f3, f);
        tessellator.startDrawingQuads();
        tessellator.addVertex((double)par0, (double)par3, par5);
        tessellator.addVertex((double)par2, (double)par3, par5);
        tessellator.addVertex((double)par2, (double)par1, par5);
        tessellator.addVertex((double)par0, (double)par1, par5);
        tessellator.draw();
    }
	
	private Map<Long,TextBlockClient> getMessages(){
		Map<Long,TextBlockClient> messages = new TreeMap<Long,TextBlockClient>();
		long time = System.currentTimeMillis();
		for(long timestamp : this.messages.keySet()){
			if(time > timestamp + 10000)
				continue;
			TextBlockClient message = this.messages.get(timestamp);
			messages.put(timestamp, message);
		}
		this.messages = messages;
		return messages;
	}	
}
