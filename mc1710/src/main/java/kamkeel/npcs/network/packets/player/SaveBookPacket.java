package kamkeel.npcs.network.packets.player;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import kamkeel.npcs.network.LargeAbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumPlayerPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEditableBook;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import noppes.npcs.blocks.tiles.TileBook;

import java.io.IOException;

public class SaveBookPacket extends LargeAbstractPacket {
    public static final String packetName = "Player|SaveBook";

    private int x, y, z;
    private boolean sign;
    private NBTTagCompound compound;

    public SaveBookPacket() {

    }

    public SaveBookPacket(int x, int y, int z, boolean sign, NBTTagCompound compound) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.sign = sign;
        this.compound = compound;
    }

    @Override
    protected byte[] getData() throws IOException {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        buffer.writeBoolean(sign);
        ByteBufUtils.writeBigNBT(buffer, compound);
        return buffer.array();
    }

    @Override
    protected void handleCompleteData(ByteBuf data, EntityPlayer player) throws IOException {
        int x = data.readInt(), y = data.readInt(), z = data.readInt();

        if (player.worldObj.blockExists(x, y, z)) {
            TileEntity tileentity = player.worldObj.getTileEntity(x, y, z);
            if (!(tileentity instanceof TileBook))
                return;
            TileBook tile = (TileBook) tileentity;
            if (tile.book.getItem() == Items.written_book)
                return;
            boolean sign = data.readBoolean();
            ItemStack book = ItemStack.loadItemStackFromNBT(ByteBufUtils.readBigNBT(data));
            if (book == null)
                return;
            if (book.getItem() == Items.writable_book && !sign && ItemWritableBook.func_150930_a(book.getTagCompound())) {
                tile.book.setTagInfo("pages", book.getTagCompound().getTagList("pages", 8));
            }
            if (book.getItem() == Items.written_book && sign && ItemEditableBook.validBookTagContents(book.getTagCompound())) {
                tile.book.setTagInfo("author", new NBTTagString(player.getCommandSenderName()));
                tile.book.setTagInfo("title", new NBTTagString(book.getTagCompound().getString("title")));
                tile.book.setTagInfo("pages", book.getTagCompound().getTagList("pages", 8));
                tile.book.func_150996_a(Items.written_book);
            }
        }
    }

    @Override
    public Enum getType() {
        return EnumPlayerPacket.SaveBook;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.PLAYER_PACKET;
    }
}
