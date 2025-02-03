package kamkeel.npcs.network.packets.player;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import kamkeel.npcs.network.*;
import kamkeel.npcs.network.enums.EnumPlayerPacket;
import kamkeel.npcs.network.packets.data.large.GuiDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.containers.ContainerMail;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.PlayerQuestController;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.controllers.data.PlayerMailData;
import noppes.npcs.controllers.data.QuestData;

import java.io.IOException;
import java.util.Iterator;

public class MailActionPacket extends AbstractPacket {
    public static final String packetName = "Player|Mail";

    private Action action;
    private long time;
    private String username;

    public MailActionPacket() {
    }

    private MailActionPacket(Action action, long time, String sender) {
        this.action = action;
        this.time = time;
        this.username = sender;
    }

    public static void RequestMailData() {
        MailActionPacket packet = new MailActionPacket();
        packet.action = Action.GET;
        PacketClient.sendClient(packet);
    }
    public static void OpenMail(long time, String sender) {
        PacketClient.sendClient(new MailActionPacket(Action.OPEN, time, sender));
    }
    public static void DeleteMail(long time, String sender) {
        PacketClient.sendClient(new MailActionPacket(Action.DELETE, time, sender));
    }
    public static void ReadMail(long time, String sender) {
        PacketClient.sendClient(new MailActionPacket(Action.READ, time, sender));
    }
    public static void SendMail(String receiver, NBTTagCompound mailData) {
        PacketClient.sendClient(new MailSendPacket(receiver, mailData));
    }

    @Override
    public Enum getType() {
        return EnumPlayerPacket.MailBoxAction;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.PLAYER_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(action.ordinal());

        if (action == Action.GET)
            return;

        switch (action) {
            case OPEN:
            case DELETE:
            case READ:
                out.writeLong(time);
                ByteBufUtils.writeString(out, username);
                break;
        }
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        Action type = Action.values()[in.readInt()];

        if(!(player instanceof EntityPlayerMP))
            return;
        EntityPlayerMP playerMP = (EntityPlayerMP) player;
        PlayerMailData data = PlayerDataController.Instance.getPlayerData(playerMP).mailData;

        if (type == Action.GET) {
            GuiDataPacket.sendGuiData(playerMP, data.saveNBTData(new NBTTagCompound()));
            return;
        }

        long time = in.readLong();
        String user = ByteBufUtils.readString(in);
        Iterator<PlayerMail> it = data.playermail.iterator();
        switch (type) {
            case OPEN:
                playerMP.closeContainer();

                while(it.hasNext()){
                    PlayerMail mail = it.next();
                    if(mail.time == time && mail.sender.equals(user)){
                        ContainerMail.staticmail = mail;
                        playerMP.openGui(CustomNpcs.instance, EnumGuiType.PlayerMailman.ordinal(), playerMP.worldObj, 0, 0, 0);
                        break;
                    }
                }
                break;

            case DELETE:
                while(it.hasNext()){
                    PlayerMail mail = it.next();
                    if(mail.time == time && mail.sender.equals(user)){
                        it.remove();
                    }
                }
                GuiDataPacket.sendGuiData(playerMP, data.saveNBTData(new NBTTagCompound()));
                break;

            case READ:
                while(it.hasNext()){
                    PlayerMail mail = it.next();
                    if(mail.time == time && mail.sender.equals(user)){
                        mail.beenRead = true;
                        if(mail.hasQuest())
                            PlayerQuestController.addActiveQuest(new QuestData(mail.getQuest()), playerMP);
                    }
                }
                break;
        }
    }

    public static class MailSendPacket extends LargeAbstractPacket {
        public static final String packetName = "Player|MailSend";
        private String username;
        private NBTTagCompound mailContent;

        public MailSendPacket() {

        }
        private MailSendPacket(String username, NBTTagCompound mailContent) {
            this.username = username;
            this.mailContent = mailContent;
        }

        @Override
        protected byte[] getData() throws IOException {
            ByteBuf buffer = Unpooled.buffer();
            ByteBufUtils.writeString(buffer, this.username);
            ByteBufUtils.writeBigNBT(buffer, this.mailContent);
            return buffer.array();
        }

        @Override
        @SideOnly(Side.SERVER)
        protected void handleCompleteData(ByteBuf data, EntityPlayer player) throws IOException {
            if(!(player instanceof EntityPlayerMP))
                return;
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            if(!(playerMP.openContainer instanceof ContainerMail))
                return;

            String mailReceiver = PlayerDataController.Instance.hasPlayer(ByteBufUtils.readString(data));
            if (mailReceiver.isEmpty()) {
                NoppesUtilServer.sendGuiError(playerMP, 0);
                return;
            }

            PlayerMail mail = new PlayerMail();
            String s = playerMP.getDisplayName();
            if (!s.equals(playerMP.getCommandSenderName()))
                s += " (" +playerMP.getCommandSenderName() + ")";
            mail.readNBT(ByteBufUtils.readBigNBT(data));
            mail.sender = s;
            mail.items = ((ContainerMail) playerMP.openContainer).mail.items;

            if (mail.subject.isEmpty()) {
                NoppesUtilServer.sendGuiError(playerMP, 1);
                return;
            }
            PlayerDataController.Instance.addPlayerMessage(mailReceiver, mail);

            NBTTagCompound comp = new NBTTagCompound();
            comp.setString("username", mailReceiver);
            NoppesUtilServer.sendGuiClose(playerMP, 1,comp);
        }

        @Override
        public Enum getType() {
            return EnumPlayerPacket.MailSend;
        }

        @Override
        public PacketChannel getChannel() {
            return PacketHandler.PLAYER_PACKET;
        }
    }

    private enum Action {
        GET,
        OPEN,
        READ,
        DELETE,
    }
}
