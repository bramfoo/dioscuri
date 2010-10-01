package dioscuri.config;

import dioscuri.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;

public class AtaPanel extends AbstractModulePanel {

    final JTextField updateInterval = new JTextField(10);
    final JTabbedPane hds = new JTabbedPane();
    final Emulator.Architecture.Modules.Ata ata;
    final java.util.List<HD> hdList;

    AtaPanel(GUI parent, Emulator emuConfig)
    {
        super(parent, emuConfig);
        super.setLayout(new BorderLayout(5, 5));

        ata = emuConfig.getArchitecture().getModules().getAta();
        hdList = new ArrayList<HD>();

        updateInterval.setText(ata.getUpdateintervalmicrosecs().toString());
        JPanel intervalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        intervalPanel.add(new JLabel("Update interval  "));
        intervalPanel.add(updateInterval);
        intervalPanel.add(new JLabel("microseconds"));

        for (int i = 1; i <= ata.getHarddiskdrive().size(); i++) {
            HD temp = new HD(ata.getHarddiskdrive().get(i - 1));
            hds.add("hd" + i, temp);
            hdList.add(temp);
        }

        super.add(intervalPanel, BorderLayout.NORTH);
        super.add(hds, BorderLayout.SOUTH);
    }

    @Override
    void save() throws Exception
    {
        ata.setUpdateintervalmicrosecs(new BigInteger(updateInterval.getText()));
        for (HD hd : hdList) {
            hd.save();
        }
    }

    class HD extends JPanel {

        final Emulator.Architecture.Modules.Ata.Harddiskdrive hd;

        final JCheckBox enabled = new JCheckBox();
        final JTextField channelIndex = new JTextField();
        final JCheckBox master = new JCheckBox();
        final JCheckBox autoDetect = new JCheckBox();
        final JTextField cylinders = new JTextField();
        final JTextField heads = new JTextField();
        final JTextField sectors = new JTextField();
        final JTextField imageFile = new JTextField();
        final JButton browse = new JButton("browse");

        HD(Emulator.Architecture.Modules.Ata.Harddiskdrive hd)
        {

            this.hd = hd;

            enabled.setSelected(hd.isEnabled());
            channelIndex.setText(hd.getChannelindex().toString());
            master.setSelected(hd.isMaster());
            autoDetect.setSelected(hd.isAutodetectcylinders());
            cylinders.setText(hd.getCylinders().toString());
            heads.setText(hd.getHeads().toString());
            sectors.setText(hd.getSectorspertrack().toString());
            imageFile.setText(hd.getImagefilepath());
            imageFile.setEditable(false);

            imageFile.setToolTipText(imageFile.getText());

            imageFile.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e)
                {
                    imageFile.setCaretPosition(imageFile.getText().length());
                }
            });

            super.setLayout(new GridLayout(0, 3, 5, 5));

            super.add(new JLabel("Enabled"));
            super.add(enabled);
            super.add(new JLabel());

            super.add(new JLabel("Channel index"));
            super.add(channelIndex);
            super.add(new JLabel());

            super.add(new JLabel("Master"));
            super.add(master);
            super.add(new JLabel());

            super.add(new JLabel("Auto detect"));
            super.add(autoDetect);
            super.add(new JLabel());

            super.add(new JLabel("Cylinders"));
            super.add(cylinders);
            super.add(new JLabel());

            super.add(new JLabel("Heads"));
            super.add(heads);
            super.add(new JLabel());

            super.add(new JLabel("Sectors"));
            super.add(sectors);
            super.add(new JLabel());

            super.add(new JLabel("Image file"));
            super.add(imageFile);
            super.add(browse);

            browse.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    File file = AtaPanel.super.chooseFile();
                    if (file != null) {
                        imageFile.setText(file.getAbsolutePath());
                        imageFile.setToolTipText(imageFile.getText());
                    }
                }
            });
        }

        void save() throws Exception
        {
            hd.setEnabled(enabled.isSelected());
            hd.setChannelindex(new BigInteger(channelIndex.getText()));
            hd.setMaster(master.isSelected());
            hd.setAutodetectcylinders(autoDetect.isSelected());
            hd.setCylinders(new BigInteger(cylinders.getText()));
            hd.setHeads(new BigInteger(heads.getText()));
            hd.setSectorspertrack(new BigInteger(sectors.getText()));
            hd.setImagefilepath(imageFile.getText().trim());
        }
    }
}

