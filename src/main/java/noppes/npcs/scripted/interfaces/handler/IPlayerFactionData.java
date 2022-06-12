package noppes.npcs.scripted.interfaces.handler;

public interface IPlayerFactionData {

    int getPoints(int id);

    void addPoints(int id, int points);

    void setPoints(int id, int points);
}
