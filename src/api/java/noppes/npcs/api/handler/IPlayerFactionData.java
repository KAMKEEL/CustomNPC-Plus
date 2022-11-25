package noppes.npcs.api.handler;

public interface IPlayerFactionData {

    int getPoints(int id);

    void addPoints(int id, int points);

    void setPoints(int id, int points);
}
