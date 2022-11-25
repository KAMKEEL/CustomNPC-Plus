package noppes.npcs.api.jobs;

public interface IJobBard extends IJob {
    
    String getSong();

    void setSong(String song);

    void setInstrument(int i);

    int getInstrumentId();

    void setMinRange(int range);
    int getMinRange();

    void setMaxRange(int range);
    int getMaxRange();

    void setStreaming(boolean streaming);
    boolean getStreaming();

    void hasOffRange(boolean value);
    boolean hasOffRange();
}
