package dioscuri.config.temp;

import dioscuri.GUI;
import dioscuri.util.Utilities;

import javax.swing.*;
import java.io.File;

public abstract class AbstractModulePanel extends JPanel {

    final GUI parent;
    final dioscuri.config.Emulator emuConfig;

    AbstractModulePanel(GUI parent, dioscuri.config.Emulator emuConfig) {
        this.parent = parent;
        this.emuConfig = emuConfig;
    }

    File chooseFile() {
        File file = null;
        final JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
        }
        return file;
    }
    
    abstract void save() throws Exception;

    void writeXML() {
        if (!Utilities.saveXML(emuConfig, parent.getConfigFilePath())) {
            JOptionPane.showMessageDialog(this, "Error saving " + "???"
                    + " parameter to configuration file.", "DIOSCURI",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}