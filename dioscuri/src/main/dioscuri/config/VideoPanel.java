package dioscuri.config;

import dioscuri.GUI;

import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;

public class VideoPanel extends AbstractModulePanel {

    final Emulator.Architecture.Modules.Video video;
    final JTextField updateInterval = new JTextField();

    VideoPanel(GUI parent, Emulator emuConfig) {
        super(parent, emuConfig);
        this.video = emuConfig.getArchitecture().getModules().getVideo();
        super.setLayout(new GridLayout(0, 3, 5, 5));

        updateInterval.setText(video.getUpdateintervalmicrosecs().toString());

        super.add(new JLabel("update interval"));
        super.add(updateInterval);
        super.add(new JLabel("microseconds"));
    }

    @Override
    void save() throws Exception {
        video.setUpdateintervalmicrosecs(new BigInteger(updateInterval.getText()));
    }
}
