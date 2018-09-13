package noppes.npcs.util;

import noppes.npcs.util.NBTJsonUtil.JsonFile;

public class JsonException extends Exception{
	public JsonException(String message, JsonFile json){
		super(message + ": " + json.getCurrentPos());
	}
}
