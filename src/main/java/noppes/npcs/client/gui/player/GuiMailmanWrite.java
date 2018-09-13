package noppes.npcs.client.gui.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiButtonNextPage;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiClose;
import noppes.npcs.client.gui.util.IGuiError;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.containers.ContainerMail;
import noppes.npcs.controllers.PlayerMail;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiMailmanWrite extends GuiContainerNPCInterface implements ITextfieldListener, IGuiError, IGuiClose, GuiYesNoCallback
{
    private static final ResourceLocation bookGuiTextures = new ResourceLocation("textures/gui/book.png");
    private static final ResourceLocation bookWidgets = new ResourceLocation("textures/gui/widgets.png");
    private static final ResourceLocation bookInventory = new ResourceLocation("textures/gui/container/inventory.png");


    /** Update ticks since the gui was opened */
    private int updateCount;
    private int bookImageWidth = 192;
    private int bookImageHeight = 192;
    private int bookTotalPages = 1;
    private int currPage;
    private NBTTagList bookPages;
    private GuiButtonNextPage buttonNextPage;
    private GuiButtonNextPage buttonPreviousPage;
    
    private boolean canEdit;
    private boolean canSend;

    /** The GuiButton to sign this book. */
    
    public static GuiScreen parent;
	public static PlayerMail mail = new PlayerMail();
    
    private Minecraft mc = Minecraft.getMinecraft();

	private String username = "";
	private GuiNpcLabel error;

    public GuiMailmanWrite(ContainerMail container, boolean canEdit, boolean canSend)
    {
    	super(null, container);
    	title = "";
        this.canEdit = canEdit;
        this.canSend = canSend;

        if(mail.message.hasKey("pages"))
        	this.bookPages = mail.message.getTagList("pages", 8);

        if (this.bookPages != null)
        {
            this.bookPages = (NBTTagList)this.bookPages.copy();
            this.bookTotalPages = this.bookPages.tagCount();

            if (this.bookTotalPages < 1)
            {
                this.bookTotalPages = 1;
            }
        }
        else
        {
            this.bookPages = new NBTTagList();
            this.bookPages.appendTag(new NBTTagString(""));
            this.bookTotalPages = 1;
        }
        xSize = 360;
        ySize = 260;
        drawDefaultBackground = false;
        closeOnEsc = true;
    }
    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        super.updateScreen();
        ++this.updateCount;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
    	super.initGui();
        this.buttonList.clear();
        Keyboard.enableRepeatEvents(true);

		if(canEdit && !canSend)
			addLabel(new GuiNpcLabel(0, "mailbox.sender", guiLeft + 170, guiTop + 32, 0));
		else
			addLabel(new GuiNpcLabel(0, "mailbox.username", guiLeft + 170, guiTop + 32, 0));
		
		if(canEdit && !canSend)
			addTextField(new GuiNpcTextField(2, this, fontRendererObj, guiLeft + 170, guiTop + 42, 114, 20, mail.sender));
		else if(canEdit)
			addTextField(new GuiNpcTextField(0, this, fontRendererObj, guiLeft + 170, guiTop + 42, 114, 20, username));
		else
			addLabel(new GuiNpcLabel(10, mail.sender, guiLeft + 170, guiTop + 42, 0));

		addLabel(new GuiNpcLabel(1, "mailbox.subject", guiLeft + 170, guiTop + 72, 0));
		if(canEdit)
			addTextField(new GuiNpcTextField(1, this, fontRendererObj, guiLeft + 170, guiTop + 82, 114, 20, mail.subject));
		else
			addLabel(new GuiNpcLabel(11, mail.subject, guiLeft + 170, guiTop + 82, 0));

		addLabel(error = new GuiNpcLabel(2, "", guiLeft + 170, guiTop + 114, 0xFF0000));
		if(canEdit && !canSend)
			addButton(new GuiNpcButton(0, this.guiLeft + 200, guiTop + 171, 60, 20, "gui.done"));
		else if(canEdit)
			addButton(new GuiNpcButton(0, this.guiLeft + 200, guiTop + 171, 60, 20, "mailbox.send"));
		
		if(!canEdit && !canSend)
			addButton(new GuiNpcButton(4, this.guiLeft + 200, guiTop + 171, 60, 20, "selectWorld.deleteButton"));
		if(!canEdit || canSend)
			addButton(new GuiNpcButton(3, this.guiLeft + 200, guiTop + 194, 60, 20, "gui.cancel"));

        this.buttonList.add(this.buttonNextPage = new GuiButtonNextPage(1, guiLeft + 120, guiTop + 156, true));
        this.buttonList.add(this.buttonPreviousPage = new GuiButtonNextPage(2, guiLeft + 38, guiTop + 156, false));
        this.updateButtons();
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    private void updateButtons()
    {
        this.buttonNextPage.setVisible(this.currPage < this.bookTotalPages - 1 || this.canEdit);
        this.buttonPreviousPage.setVisible(this.currPage > 0);
    }
    
	@Override
    public void confirmClicked(boolean flag, int i)
    {
		if(flag){
	        NoppesUtilPlayer.sendData(EnumPlayerPacket.MailDelete, mail.time, mail.sender);
	        close();
		}
		else
			NoppesUtil.openGUI(player, this);
    }

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    @Override
    protected void actionPerformed(GuiButton par1GuiButton)
    {
        if (par1GuiButton.enabled)
        {
        	int id = par1GuiButton.id;
    		if(id == 0){
    			mail.message.setTag("pages", bookPages);
    	    	if(canSend)
    	    		NoppesUtilPlayer.sendData(EnumPlayerPacket.MailSend, this.username, mail.writeNBT());
    	    	else
    	    		close();
    		}
            if (id == 3)
            {
                close();
            }
            if (id == 4)
            {
                GuiYesNo guiyesno = new GuiYesNo(this, "Confirm", StatCollector.translateToLocal("gui.delete"), 0);
                displayGuiScreen(guiyesno);
            }
            else if (id == 1)
            {
                if (this.currPage < this.bookTotalPages - 1)
                {
                    ++this.currPage;
                }
                else if (this.canEdit)
                {
                    this.addNewPage();

                    if (this.currPage < this.bookTotalPages - 1)
                    {
                        ++this.currPage;
                    }
                }
            }
            else if (id == 2)
            {
                if (this.currPage > 0)
                {
                    --this.currPage;
                }
            }

            this.updateButtons();
        }
    }

    private void addNewPage()
    {
        if (this.bookPages != null && this.bookPages.tagCount() < 50)
        {
            this.bookPages.appendTag(new NBTTagString(""));
            ++this.bookTotalPages;
        }
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    public void keyTyped(char par1, int par2)
    {
    	if(!GuiNpcTextField.isActive() && canEdit)
        	this.keyTypedInBook(par1, par2);
    	else
    		super.keyTyped(par1, par2);
    }

    /**
     * Processes keystrokes when editing the text of a book
     */
    private void keyTypedInBook(char par1, int par2)
    {
        switch (par1)
        {
            case 22:
                this.func_74160_b(GuiScreen.getClipboardString());//getClipboardString
                return;
            default:
                switch (par2)
                {
                    case 14:
                        String s = this.func_74158_i();

                        if (s.length() > 0)
                        {
                            this.func_74159_a(s.substring(0, s.length() - 1));
                        }

                        return;
                    case 28:
                    case 156:
                        this.func_74160_b("\n");
                        return;
                    default:
                        if (ChatAllowedCharacters.isAllowedCharacter(par1))
                        {
                            this.func_74160_b(Character.toString(par1));
                        }
                }
        }
    }

    private String func_74158_i()
    {
        if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount())
        {
            return this.bookPages.getStringTagAt(this.currPage);
        }
        else
        {
            return "";
        }
    }

    private void func_74159_a(String par1Str)
    {
        if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount())
        {
        	bookPages.func_150304_a(currPage, new NBTTagString(par1Str));
        }
    }

    private void func_74160_b(String par1Str)
    {
        String s1 = this.func_74158_i();
        String s2 = s1 + par1Str;
        int i = mc.fontRenderer.splitStringWidth(s2 + "" + EnumChatFormatting.BLACK + "_", 118);

        if (i <= 118 && s2.length() < 256)
        {
            this.func_74159_a(s2);
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int par1, int par2, float par3)
    {
        this.drawWorldBackground(0);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(bookGuiTextures);
        this.drawTexturedModalRect(guiLeft + 130, guiTop + 22, 0, 0, this.bookImageWidth, this.bookImageHeight / 3);
        this.drawTexturedModalRect(guiLeft + 130, guiTop + 22 + bookImageHeight/3, 0, bookImageHeight / 2, this.bookImageWidth, this.bookImageHeight / 2);

        this.drawTexturedModalRect(guiLeft, guiTop + 2, 0, 0, this.bookImageWidth, this.bookImageHeight);

        this.mc.getTextureManager().bindTexture(bookInventory);
        this.drawTexturedModalRect(guiLeft + 20, guiTop + 173, 0, 82, 180, 55);
        this.drawTexturedModalRect(guiLeft + 20, guiTop + 228, 0, 140, 180, 28);
        String s;
        String s1;
        int l;

        s = I18n.format("book.pageIndicator", new Object[] {Integer.valueOf(this.currPage + 1), Integer.valueOf(this.bookTotalPages)});
        s1 = "";

        if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount())
        {
            s1 = this.bookPages.getStringTagAt(this.currPage);
        }
        
        if(canEdit){
	        if (mc.fontRenderer.getBidiFlag())
	        {
	            s1 = s1 + "_";
	        }
	        else if (this.updateCount / 6 % 2 == 0)
	        {
	            s1 = s1 + "" + EnumChatFormatting.BLACK + "_";
	        }
	        else
	        {
	            s1 = s1 + "" + EnumChatFormatting.GRAY + "_";
	        }
        }

        l = mc.fontRenderer.getStringWidth(s);
        mc.fontRenderer.drawString(s, guiLeft - l + this.bookImageWidth - 44, guiTop + 18, 0);
        mc.fontRenderer.drawSplitString(s1, guiLeft + 36, guiTop + 18 + 16, 116, 0);

        this.drawGradientRect(guiLeft + 175, guiTop + 136, guiLeft + 269, guiTop + 154, -1072689136, -804253680);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        

        this.mc.getTextureManager().bindTexture(bookWidgets);
        for(int i = 0; i < 4; i++)
        	this.drawTexturedModalRect(guiLeft + 175 + i * 24, guiTop + 134, 0, 22, 24, 24);
        
        super.drawScreen(par1, par2, par3);
        
    }

    public void close(){
    	mc.displayGuiScreen(parent);
    	parent = null;
    	mail = new PlayerMail();
    }

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if(textField.id == 0)
			username = textField.getText();
		if(textField.id == 1)
			mail.subject = textField.getText();
		if(textField.id == 2)
			mail.sender = textField.getText();
	}

	@Override
	public void setError(int i, NBTTagCompound data) {
		if(i == 0)
			error.label = StatCollector.translateToLocal("mailbox.errorUsername");
		if(i == 1)
			error.label = StatCollector.translateToLocal("mailbox.errorSubject");
	}

	@Override
	public void setClose(int i, NBTTagCompound data) {
		player.addChatMessage(new ChatComponentTranslation("mailbox.succes", data.getString("username")));
	}

	@Override
	public void save() {
		
	}
}
