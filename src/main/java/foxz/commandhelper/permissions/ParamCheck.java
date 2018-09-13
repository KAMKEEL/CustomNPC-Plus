package foxz.commandhelper.permissions;

import foxz.commandhelper.AbstractCommandHelper;

public class ParamCheck extends AbstractPermission {

    String err;

    @Override
    public String errorMsg() {
        return err;
    }

    @Override
    public boolean delegate(AbstractCommandHelper parent, String[] args) {
        String[] np = parent.currentHelper.usage.split(" ");
        int countRequired = 0;
        for(String command : np){
        	if(command.startsWith("<"))
        		countRequired++;
        }
        if (args.length < countRequired) {
            err = np[args.length] + " missing";
            return false;
        }
        return true;
    }

}
