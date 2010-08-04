package dioscuri.module;

public interface Updatable {

    int getUpdateInterval();

    void setUpdateInterval(int interval);

    void update();
}
