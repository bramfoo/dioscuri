package dioscuri.config;

import dioscuri.GUI;

import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;

public class PitPanel extends AbstractModulePanel {

    final Emulator.Architecture.Modules.Pit pit;
    final JTextField clockRate = new JTextField();

    PitPanel(GUI parent, Emulator emuConfig) {
        super(parent, emuConfig);
        this.pit = emuConfig.getArchitecture().getModules().getPit();
        super.setLayout(new GridLayout(0, 3, 5, 5));

        clockRate.setText(pit.getClockrate().toString());

        super.add(new JLabel("clock rate"));
        super.add(clockRate);
        super.add(new JLabel());
    }

    @Override
    void save() throws Exception {
        pit.setClockrate(new BigInteger(clockRate.getText()));
    }
}
