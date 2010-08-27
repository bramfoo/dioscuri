package dioscuri.module;

/**
 * User: bki010
 * Date: Aug 26, 2010
 * Time: 3:53:22 PM
 */
public interface Updateable {

    int getUpdateInterval();

    void setUpdateInterval(int interval);

    void update();
}
