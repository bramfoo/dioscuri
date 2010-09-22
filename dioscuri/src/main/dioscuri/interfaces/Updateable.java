package dioscuri.interfaces;

public interface Updateable extends Module {

    int getUpdateInterval();

    void setUpdateInterval(int interval);

    void update();
}
