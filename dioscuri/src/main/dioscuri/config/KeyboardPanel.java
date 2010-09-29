package dioscuri.config;

import dioscuri.GUI;

import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;

public class KeyboardPanel extends AbstractModulePanel {

    final Emulator.Architecture.Modules.Keyboard keyboard;
    final JTextField updateInterval = new JTextField();

    KeyboardPanel(GUI parent, Emulator emuConfig) {
        super(parent, emuConfig);
        this.keyboard = emuConfig.getArchitecture().getModules().getKeyboard();
        super.setLayout(new GridLayout(0, 3, 5, 5));

        updateInterval.setText(keyboard.getUpdateintervalmicrosecs().toString());

        super.add(new JLabel("update interval"));
        super.add(updateInterval);
        super.add(new JLabel("microseconds"));
    }

    @Override
    void save() throws Exception {
        keyboard.setUpdateintervalmicrosecs(new BigInteger(updateInterval.getText()));
    }
}
