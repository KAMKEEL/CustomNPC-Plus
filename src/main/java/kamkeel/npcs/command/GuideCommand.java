package kamkeel.npcs.command;

import kamkeel.npcs.network.packets.data.GuideTargetPacket;
import kamkeel.npcs.util.ColorUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.guide.GuideNameMatcher;
import noppes.npcs.guide.GuideNpcLocator;

import java.util.Collections;
import java.util.List;

/**
 * /guide —— 面向所有玩家的 NPC 引路指令，权限级 0，不需要 OP。
 *
 * stop 和无参两种形式的实际处理都在客户端（引路状态只存在于客户端），服务端这里只负责
 * 转成控制包发回去。
 */
public class GuideCommand extends CommandBase {

    private static final String USAGE = "/guide <NPC名>  |  /guide stop  |  /guide";

    @Override
    public String getCommandName() {
        return "guide";
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("yl");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return USAGE;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        // 安全网：1.7.10 里指令处理漏出去的异常会直接崩服。
        try {
            processInternal(sender, args);
        } catch (Throwable t) {
            System.out.println("[CustomNPC+] /guide 指令处理抛出未处理异常：");
            t.printStackTrace();
            ColorUtil.sendError(sender, "内部错误（详情见日志）：" + t);
        }
    }

    private void processInternal(ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayerMP)) {
            ColorUtil.sendError(sender, "只有玩家能使用引路指令");
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) sender;

        if (args.length == 0) {
            GuideTargetPacket.sendStatus(player);
            return;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("stop")) {
            GuideTargetPacket.sendStop(player);
            return;
        }

        // NPC 名字可能带空格，把剩下的参数拼回去。
        String query = joinArgs(args);

        GuideNameMatcher.Result result = GuideNpcLocator.findInPlayerDimension(player, query);
        if (result.chosen == null) {
            // 这两条是给玩家看的信息提示，不是错误：走 sendError 会渲染成红色的
            // "[CustomNPC+] Error: ..."，玩家打错一个字就被吼一句 Error，跟文案表的中性
            // 措辞对不上。真正的内部错误才用 sendError（见 processCommand 的兜底）。
            Integer other = GuideNpcLocator.findOtherDimension(player, query);
            if (other != null) {
                ColorUtil.sendMessage(sender, "『" + query + "』在" + GuideNpcLocator.dimensionName(other)
                    + "，无法跨维度引路");
            } else {
                ColorUtil.sendMessage(sender, "附近没有叫『" + query + "』的 NPC（它所在的区块可能没有加载）");
            }
            return;
        }

        if (result.matchCount > 1) {
            ColorUtil.sendMessage(sender, "找到 " + result.matchCount + " 个『" + query + "』，已选最近的（"
                + (int) Math.sqrt(result.chosen.distanceSq) + " 格）");
        }
        GuideTargetPacket.sendStart(player, (EntityNPCInterface) result.chosen.payload);
    }

    private static String joinArgs(String[] args) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                out.append(' ');
            }
            out.append(args[i]);
        }
        return out.toString();
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length != 1 || !(sender instanceof EntityPlayerMP)) {
            return null;
        }
        List<String> options = GuideNpcLocator.loadedNpcNames((EntityPlayerMP) sender);
        options.add("stop");
        return getListOfStringsMatchingLastWord(args, options.toArray(new String[options.size()]));
    }
}
