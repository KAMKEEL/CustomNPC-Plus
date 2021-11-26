//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.interfaces;

import net.minecraft.util.math.BlockPos;

public interface IPos {
    int getX();

    int getY();

    int getZ();

    IPos up();

    IPos up(int var1);

    IPos down();

    IPos down(int var1);

    IPos north();

    IPos north(int var1);

    IPos east();

    IPos east(int var1);

    IPos south();

    IPos south(int var1);

    IPos west();

    IPos west(int var1);

    IPos add(int var1, int var2, int var3);

    IPos add(IPos var1);

    IPos subtract(int var1, int var2, int var3);

    IPos subtract(IPos var1);

    double[] normalize();

    BlockPos getMCBlockPos();

    IPos offset(int var1);

    IPos offset(int var1, int var2);

    double distanceTo(IPos var1);
}
