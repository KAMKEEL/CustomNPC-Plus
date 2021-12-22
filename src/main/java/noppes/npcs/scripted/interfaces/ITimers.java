//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.interfaces;

public interface ITimers {
    void start(int var1, int var2, boolean var3);

    void forceStart(int var1, int var2, boolean var3);

    boolean has(int var1);

    boolean stop(int var1);

    void reset(int var1);

    void clear();

    int size();
}
