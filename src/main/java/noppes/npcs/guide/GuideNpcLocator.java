package noppes.npcs.guide;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 服务端按名字/UUID 查 NPC。
 *
 * 只能看到已加载区块里的实体——CNPC+ 的 NPC 存在区块里，没有全局位置索引，所以目标区块
 * 没被加载时这里就是找不到。这是引路功能已知且接受的限制。
 */
public final class GuideNpcLocator {

    private GuideNpcLocator() {}

    /** 在玩家所在维度的已加载实体里按名字找。 */
    public static GuideNameMatcher.Result findInPlayerDimension(EntityPlayerMP player, String query) {
        return GuideNameMatcher.match(collectCandidates(player.worldObj, player), query);
    }

    /** 按 UUID 精确找，用于刷新时锁死同一个 NPC，不会因为走近了另一个同名 NPC 就被换掉。 */
    public static EntityNPCInterface findByUuid(EntityPlayerMP player, String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            return null;
        }
        for (Object o : player.worldObj.loadedEntityList) {
            Entity e = (Entity) o;
            if (e instanceof EntityNPCInterface && uuid.equals(e.getUniqueID().toString())) {
                return (EntityNPCInterface) e;
            }
        }
        return null;
    }

    /**
     * 玩家所在维度找不到时，扫其它维度看有没有同名的，好给出"在下界，无法跨维度引路"
     * 这种有用的提示，而不是干巴巴一句找不到。返回那个维度的 ID；没有返回 null。
     */
    public static Integer findOtherDimension(EntityPlayerMP player, String query) {
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) {
            return null;
        }
        for (WorldServer world : server.worldServers) {
            if (world == null || world == player.worldObj) {
                continue;
            }
            GuideNameMatcher.Result r = GuideNameMatcher.match(collectCandidates(world, null), query);
            if (r.chosen != null) {
                return world.provider.dimensionId;
            }
        }
        return null;
    }

    /** Tab 补全用：玩家所在维度已加载 NPC 的名字，去重、保持遍历顺序。 */
    public static List<String> loadedNpcNames(EntityPlayerMP player) {
        Set<String> names = new LinkedHashSet<String>();
        for (Object o : player.worldObj.loadedEntityList) {
            Entity e = (Entity) o;
            if (e instanceof EntityNPCInterface) {
                String name = ((EntityNPCInterface) e).display.name;
                if (name != null && !name.isEmpty()) {
                    names.add(name);
                }
            }
        }
        return new ArrayList<String>(names);
    }

    public static String dimensionName(int id) {
        switch (id) {
            case -1: return "下界";
            case 0:  return "主世界";
            case 1:  return "末地";
            default: return "维度 " + id;
        }
    }

    /**
     * relativeTo 为 null 时距离一律填 0——扫别的维度只关心"有没有"，比距离没有意义。
     */
    private static List<GuideNameMatcher.Candidate> collectCandidates(World world, Entity relativeTo) {
        List<GuideNameMatcher.Candidate> out = new ArrayList<GuideNameMatcher.Candidate>();
        for (Object o : world.loadedEntityList) {
            Entity e = (Entity) o;
            if (!(e instanceof EntityNPCInterface)) {
                continue;
            }
            EntityNPCInterface npc = (EntityNPCInterface) e;
            double distSq = relativeTo == null ? 0.0 : relativeTo.getDistanceSqToEntity(npc);
            out.add(new GuideNameMatcher.Candidate(npc.display.name, distSq, npc));
        }
        return out;
    }
}
