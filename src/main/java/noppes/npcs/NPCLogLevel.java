package noppes.npcs;

import java.util.logging.Level;

class NPCLogLevel extends Level {
  public static final Level CNPCLog = new NPCLogLevel("CNPC", 0);

  public NPCLogLevel(String name, int value) {
    super(name, value);
  }
}