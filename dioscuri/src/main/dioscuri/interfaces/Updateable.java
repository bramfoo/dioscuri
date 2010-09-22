package dioscuri.interfaces;

public interface Updateable {

    int getUpdateInterval();

    void setUpdateInterval(int interval);

    void update();
}
