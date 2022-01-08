package foxz.command;

import foxz.commandhelper.ChMcLogger;
import foxz.commandhelper.annotations.Command;
import foxz.commandhelper.annotations.SubCommand;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.util.NBTJsonUtil;

import java.io.File;
import java.util.UUID;

@Command(
  name = "adapt",
  desc = "Adapt all JSON data from one player to Zstd compress."
)
public class CmdAdapter extends ChMcLogger {

    public CmdAdapter(Object sender) {
        super(sender);
    }

    @SubCommand(
      usage = "update all",
      desc = "Update all JSON data from one player to Zstd compress.",
      name = "update"
    )
    public boolean update(String[] args) {
        File directory = PlayerDataController.instance.getSaveDir();

        try {
            for (String file : directory.list()) {
                if (!file.endsWith(".json")) continue;

                UUID uuid = UUID.fromString(
                  file.replace(".json", "")
                );
                NBTTagCompound compound = NBTJsonUtil.LoadFile(
                  new File(directory, file)
                );

                PlayerDataController.instance.savePlayerData(compound, uuid);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return true;
    }
}
