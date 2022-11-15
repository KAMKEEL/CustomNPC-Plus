package noppes.npcs.api.handler;

import noppes.npcs.api.handler.data.ITransportCategory;

public interface ITransportHandler {

    ITransportCategory[] categories();

    void createCategory(String title);

    ITransportCategory getCategory(String title);

    void removeCategory(String title);
}
