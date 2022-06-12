package noppes.npcs.scripted.interfaces.handler;

import noppes.npcs.scripted.interfaces.handler.data.ITransportCategory;

public interface ITransportHandler {

    ITransportCategory[] categories();

    void createCategory(String title);

    ITransportCategory getCategory(String title);

    void removeCategory(String title);
}
