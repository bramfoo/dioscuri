package dioscuri;

import dioscuri.config.Emulator;

import javax.swing.*;

/**
 * User: bki010
 * Date: Feb 5, 2010
 * Time: 10:10:15 AM
 */
public class DummyGUI implements GUI {

    @Override
    public JFrame asJFrame() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean saveXML(Emulator params) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Emulator getEmuConfig() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getConfigFilePath() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void notifyGUI(int emuProcess) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void updateGUI(int activity) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setScreen(JPanel screen) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean setMouseEnabled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean setMouseDisabled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
