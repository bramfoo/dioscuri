package dioscuri.config;

import dioscuri.GUI;
import dioscuri.interfaces.Module;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigDialog extends JDialog {

    private final dioscuri.config.Emulator emuConfig;
    private final GUI parent;
    private final Map<Module.Type, JPanel> moduleMap;

    public ConfigDialog(GUI parent) {
        super(parent.asJFrame());
        this.parent = parent;
        this.emuConfig = parent.getEmuConfig();
        this.moduleMap = new LinkedHashMap<Module.Type, JPanel>();
        super.setLayout(new BorderLayout(5, 5));
        setupModuleMap();
        setupGUI();
        super.setSize(parent.asJFrame().getWidth(), parent.asJFrame().getHeight());
        super.setLocation(parent.asJFrame().getLocation());
        super.setResizable(false);
        super.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        super.setVisible(true);
    }

    /*
        +--------------------------------------------------------+
        | +----------------------------------------------------+ |
        | | +------+ +---------------------------------------+ | |
        | | |      | |                                       | | |
        | | |      | |                                       | | |
        | | |    x----------------------------------------------------- moduleListPanel
        | | |      | |                                       | | |
        | | |      | |                                       | | |
        | | |      | |                                       | |x------ JDialog.getContentPane() :: BorderLayout
        | | |      | |                                       | | |
        | | |      | |                                     x----------- attributesPanel
        | | |      | |                                       | | |
        | | |      | |                                       | | |
        | | |      | |                                       |x-------- mainPanel :: BorderLayout
        | | |      | |                                       | | |
        | | +------+ +---------------------------------------+ | |
        | +----------------------------------------------------+ |
        | +----------------------------------------------------+ |
        | |                                                  x--------- buttonPanel :: FlowLayout, RIGHT
        | +----------------------------------------------------+ |
        +--------------------------------------------------------+      
     */
    private void setupGUI() {
        final JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        final JPanel attributesPanel = new JPanel(new BorderLayout(5, 5));
        attributesPanel.add(new JLabel("Select a module in the left menu."));
        final JPanel moduleListPanel = new JPanel(new BorderLayout(5, 5));
        moduleListPanel.setPreferredSize(new Dimension(180, 0));

        mainPanel.add(moduleListPanel, BorderLayout.WEST);
        mainPanel.add(attributesPanel, BorderLayout.CENTER);

        // button panel
        final JButton cancel = new JButton("cancel");
        final JButton save = new JButton("save");
        buttonPanel.add(cancel);
        buttonPanel.add(save);
        cancel.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                ConfigDialog.this.dispose();
            }
        });
        save.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO    
            }
        });

        // tree panel
        final JList moduleList = new JList(moduleMap.keySet().toArray(new Module.Type[moduleMap.keySet().size()]));
        moduleListPanel.add(new JScrollPane(moduleList));
        moduleList.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
                attributesPanel.removeAll();
                JPanel p = moduleMap.get((Module.Type)moduleList.getSelectedValue());
                attributesPanel.add(p, BorderLayout.CENTER);
                attributesPanel.validate();
                repaint();
            }
        });
        moduleList.addKeyListener(new KeyAdapter(){
            @Override
            public void keyReleased(KeyEvent e) {
                attributesPanel.removeAll();
                JPanel p = moduleMap.get((Module.Type)moduleList.getSelectedValue());
                attributesPanel.add(p, BorderLayout.CENTER);
                attributesPanel.validate();
                repaint();
            }
        });

        super.getContentPane().add(mainPanel, BorderLayout.CENTER);
        super.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupModuleMap() {
        this.moduleMap.put(Module.Type.ATA, new AtaPanel(emuConfig));
        this.moduleMap.put(Module.Type.BIOS, new BiosPanel());
        this.moduleMap.put(Module.Type.BOOT, new BootPanel());
        this.moduleMap.put(Module.Type.CPU, new CpuPanel());
        this.moduleMap.put(Module.Type.FDC, new FdcPanel());
        this.moduleMap.put(Module.Type.KEYBOARD, new KeyboardPanel());
        this.moduleMap.put(Module.Type.MOUSE, new MousePanel());
        this.moduleMap.put(Module.Type.MEMORY, new MemoryPanel());
        this.moduleMap.put(Module.Type.PIT, new PitPanel());
        this.moduleMap.put(Module.Type.VIDEO, new VideoPanel());
    }

    private static class ModulePanel extends JPanel {

        final dioscuri.config.Emulator emuConfig;

        ModulePanel(dioscuri.config.Emulator emuConfig) {
            this.emuConfig = emuConfig;
        }
    }

    private static class AtaPanel extends ModulePanel {

        JTextField updateInterval = new JTextField(10);
        JTabbedPane hds = new JTabbedPane();

        AtaPanel(dioscuri.config.Emulator emuConfig) {
            super(emuConfig);
            super.setLayout(new BorderLayout(5, 5));

            updateInterval.setText(emuConfig.architecture.modules.ata.getUpdateintervalmicrosecs().toString());
            JPanel intervalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            intervalPanel.add(new JLabel("Update interval  "));
            intervalPanel.add(updateInterval);
            intervalPanel.add(new JLabel("microseconds"));

            for(int i = 1; i <= emuConfig.architecture.modules.ata.getHarddiskdrive().size(); i++) {
                hds.add("disc "+i, new HD(emuConfig.architecture.modules.ata.getHarddiskdrive().get(i-1)));
            }

            super.add(intervalPanel, BorderLayout.NORTH);
            super.add(hds, BorderLayout.CENTER);
        }

        class HD extends JPanel {

            JCheckBox enabled = new JCheckBox();
            JTextField channelIndex = new JTextField();
            JCheckBox master = new JCheckBox();
            JCheckBox autoDetect = new JCheckBox();
            JTextField cylinders = new JTextField();
            JTextField heads = new JTextField();
            JTextField sectors = new JTextField();
            JTextArea imageFile = new JTextArea();
            JButton browse = new JButton("browse");

            HD(Emulator.Architecture.Modules.Ata.Harddiskdrive hd) {

                enabled.setSelected(hd.enabled);
                channelIndex.setText(hd.channelindex.toString());
                master.setSelected(hd.master);
                autoDetect.setSelected(hd.autodetectcylinders);
                cylinders.setText(hd.cylinders.toString());
                heads.setText(hd.heads.toString());
                sectors.setText(hd.sectorspertrack.toString());
                imageFile.setText(hd.imagefilepath);

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
                super.add(new JScrollPane(imageFile));
                super.add(browse);
            }
        }
    }

    private static class BiosPanel extends JPanel {

    }

    private static class BootPanel extends JPanel {

    }

    private static class CpuPanel extends JPanel {

    }

    private static class FdcPanel extends JPanel {

    }

    private static class KeyboardPanel extends JPanel {

    }

    private static class MousePanel extends JPanel {

    }

    private static class MemoryPanel extends JPanel {

    }

    private static class PitPanel extends JPanel {

    }

    private static class VideoPanel extends JPanel {

    }
}
