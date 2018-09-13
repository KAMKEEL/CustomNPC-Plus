package foxz.commandhelper.permissions;

import foxz.commandhelper.AbstractCommandHelper;

public abstract class AbstractPermission {

    abstract public String errorMsg();

    abstract public boolean delegate(AbstractCommandHelper parent, String args[]);
}
