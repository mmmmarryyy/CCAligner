package keyboardhero;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import keyboardhero.Util.*;
import keyboardhero.MidiDevicer.*;
import keyboardhero.MidiSequencer.*;

final class DialogSettings extends AbstractDialog {

    private final class TransparentComponent extends JComponent {

        private static final long serialVersionUID = -3015001141996676996L;

        private Image background;

        private int screenX, screenY;

        TransparentComponent(final Frame frame, final Rectangle bounds) {
            try {
                screenX = bounds.x;
                screenY = bounds.y;
                background = (new Robot()).createScreenCapture(bounds);
                frame.addComponentListener(new ComponentAdapter() {

                    public void componentShown(ComponentEvent e) {
                        repaint();
                    }

                    public void componentResized(ComponentEvent e) {
                        repaint();
                    }

                    public void componentMoved(ComponentEvent e) {
                        repaint();
                    }
                });
                frame.addWindowFocusListener(new WindowAdapter() {

                    public void windowGainedFocus(WindowEvent e) {
                        refresh();
                    }

                    public void windowLostFocus(WindowEvent e) {
                        refresh();
                    }
                });
            } catch (AWTException e) {
            }
        }

        public void paintComponent(Graphics g) {
            if (this.isShowing()) {
                final Point pos = getLocationOnScreen();
                g.drawImage(background, -pos.x + screenX, -pos.y + screenY, null);
            }
        }

        public void refresh() {
            if (isVisible()) {
                repaint();
            }
        }
    }

    static final class DeviceList extends JScrollPane {

        private static final String[] COLS = new String[] { "Device", "Input", "Song", "Output" };

        private static final long serialVersionUID = 3925531085505441301L;

        static final Device[] NULL_DEVICES = new Device[0];

        static final Component NULL_COMPONENT = new Component() {

            private static final long serialVersionUID = -4389167097504964247L;
        };

        static final TableCellRenderer NULL_RENDERER = new TableCellRenderer() {

            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                return NULL_COMPONENT;
            }
        };

        private static Device[] devices = NULL_DEVICES;

        private static boolean[][] values = new boolean[3][0];

        private static ArrayList<DeviceList> instances = new ArrayList<DeviceList>();

        private static JTable lastTable;

        private JTable table;

        private String configKey;

        private final ChangeListener valueChanged;

        DeviceList(final String configKey, final ChangeListener valueChanged) {
            super(lastTable = new JTable(new AbstractTableModel() {

                private static final long serialVersionUID = 704371080036381343L;

                public String getColumnName(int column) {
                    return Util.getMsg(COLS[column]);
                }

                public int getRowCount() {
                    return devices.length;
                }

                public int getColumnCount() {
                    return COLS.length;
                }

                public Object getValueAt(int rowIndex, int colIndex) {
                    switch(colIndex) {
                        case 0:
                            final MidiDevice.Info info = devices[rowIndex].info;
                            if (info == null) return "----- " + Util.getMsg("Default") + " -----";
                            return devices[rowIndex].getName();
                        case 1:
                        case 2:
                        case 3:
                            return values[colIndex - 1][rowIndex];
                    }
                    return null;
                }

                public boolean isCellEditable(int rowIndex, int colIndex) {
                    switch(colIndex) {
                        case 1:
                            return devices[rowIndex].hasInput;
                        case 2:
                        case 3:
                            return devices[rowIndex].hasOutput;
                    }
                    return false;
                }

                public void setValueAt(Object value, int row, int col) {
                    values[col - 1][row] = (Boolean) value;
                    if (valueChanged != null) {
                        valueChanged.stateChanged(null);
                    }
                }

                public Class<?> getColumnClass(int c) {
                    if (c == 0) return String.class;
                    return Boolean.class;
                }
            }) {

                private static final long serialVersionUID = 789352345234625423L;

                public String getToolTipText(MouseEvent e) {
                    final Point p = e.getPoint();
                    final int tRowIndex = rowAtPoint(p);
                    if (tRowIndex == -1) {
                        return null;
                    }
                    final int rowIndex = convertRowIndexToModel(tRowIndex);
                    final MidiDevice.Info info = devices[rowIndex].info;
                    if (info == null) {
                        return Util.getMsg("DefaultDeviceHelp");
                    }
                    final String description = info.getDescription();
                    final String vendor = info.getVendor();
                    return devices[rowIndex].getName() + (description.equals("No details available") ? "" : (", " + description)) + (vendor.equals("Unknown vendor") ? "" : (", " + vendor)) + ", " + info.getVersion();
                }

                protected JTableHeader createDefaultTableHeader() {
                    return new JTableHeader(columnModel) {

                        private static final long serialVersionUID = 89928354348912566L;

                        public String getToolTipText(MouseEvent e) {
                            final Point p = e.getPoint();
                            final int index = convertColumnIndexToModel(columnAtPoint(p));
                            return Util.getMsg("DeviceHelp_" + COLS[index]);
                        }
                    };
                }

                public TableCellRenderer getCellRenderer(int row, int column) {
                    if (column != 0 && !isCellEditable(row, column)) {
                        return NULL_RENDERER;
                    }
                    final TableCellRenderer renderer = getColumnModel().getColumn(column).getCellRenderer();
                    if (renderer == null) {
                        return getDefaultRenderer(getColumnClass(column));
                    }
                    return renderer;
                }
            });
            table = lastTable;
            this.configKey = configKey;
            final TableColumnModel columnModel = table.getColumnModel();
            final String widths[] = Util.getProp(configKey + "ColumnWidths").split("\\|");
            for (int i = 0; i < widths.length && i < COLS.length; ++i) {
                try {
                    columnModel.getColumn(i).setPreferredWidth(Integer.parseInt(widths[i]));
                } catch (NumberFormatException e) {
                }
            }
            table.setFillsViewportHeight(true);
            table.setAutoCreateRowSorter(true);
            table.setSelectionModel(new DefaultListSelectionModel() {

                private static final long serialVersionUID = -8239018545766232662L;

                public int getLeadSelectionIndex() {
                    return -1;
                }

                public void addSelectionInterval(int index0, int index1) {
                }

                public void setSelectionInterval(int index0, int index1) {
                }
            });
            int sortIndex = Util.getPropInt(configKey + "SortIndex");
            if (sortIndex >= 10) {
                sortIndex -= 10;
                final RowSorter<?> rowSorter = table.getRowSorter();
                rowSorter.toggleSortOrder(sortIndex);
                rowSorter.toggleSortOrder(sortIndex);
            } else {
                table.getRowSorter().toggleSortOrder(sortIndex);
            }
            this.valueChanged = valueChanged;
            instances.add(this);
        }

        static void refresh() {
            MidiDevicer.refreshDevices();
        }

        static void refreshDevices() {
            devices = MidiDevicer.DEVICELIST.toArray(NULL_DEVICES);
            loadValues();
            for (DeviceList instance : instances) {
                instance.table.tableChanged(new TableModelEvent(instance.table.getModel()));
                instance.valueChanged.stateChanged(null);
            }
        }

        static void saveValues() {
            MidiDevicer.resetStatuses();
            final boolean isChanged = isChanged();
            for (int i = 0; i < devices.length; ++i) {
                if (values[0][i]) {
                    if (!devices[i].isInputConnected()) MidiDevicer.connectInputDevice(devices[i]);
                } else if (isChanged && devices[i].hasInput) MidiDevicer.disconnectInputDevice(devices[i]);
                if (values[1][i]) {
                    if (!devices[i].isSongConnected()) MidiDevicer.connectSongDevice(devices[i]);
                } else if (isChanged && devices[i].hasOutput) MidiDevicer.disconnectSongDevice(devices[i]);
                if (values[2][i]) {
                    if (!devices[i].isOutputConnected()) MidiDevicer.connectOutputDevice(devices[i]);
                } else if (isChanged && devices[i].hasOutput) MidiDevicer.disconnectOutputDevice(devices[i]);
            }
        }

        static void loadValues() {
            values = new boolean[3][devices.length];
            for (int i = 0; i < devices.length; ++i) {
                values[0][i] = devices[i].isInputConnected();
                values[1][i] = devices[i].isSongConnected();
                values[2][i] = devices[i].isOutputConnected();
            }
        }

        static boolean isChanged() {
            for (int i = 0; i < devices.length; ++i) {
                if (values[0][i] != devices[i].isInputConnected()) return true;
                if (values[1][i] != devices[i].isSongConnected()) return true;
                if (values[2][i] != devices[i].isOutputConnected()) return true;
            }
            return false;
        }

        void updateTexts() {
            final TableColumnModel columnModel = table.getTableHeader().getColumnModel();
            for (int i = 0; i < COLS.length; ++i) {
                columnModel.getColumn(i).setHeaderValue(COLS[i]);
            }
            table.tableChanged(new TableModelEvent(table.getModel()));
        }

        public void updateUI() {
            super.updateUI();
            if (table != null) table.updateUI();
        }

        void closure() {
            int songListerSortIndex = 1;
            for (RowSorter.SortKey k : table.getRowSorter().getSortKeys()) {
                final SortOrder sortOrder = k.getSortOrder();
                if (sortOrder == SortOrder.ASCENDING) {
                    songListerSortIndex = k.getColumn();
                    break;
                }
                if (sortOrder == SortOrder.DESCENDING) {
                    songListerSortIndex = k.getColumn() + 10;
                    break;
                }
            }
            Util.setProp(configKey + "SortIndex", songListerSortIndex);
            final StringBuffer buff = new StringBuffer();
            final TableColumnModel columnModel = table.getColumnModel();
            buff.append(columnModel.getColumn(0).getWidth());
            for (int i = 1; i < COLS.length; i++) {
                buff.append('|');
                buff.append(columnModel.getColumn(i).getPreferredWidth());
            }
            Util.setProp(configKey + "ColumnWidths", buff.toString());
        }
    }

    private static final Color ERROR_COLOR = Graphs.getColor("error");

    private static final Color SUCCESS_COLOR = Graphs.getColor("success");

    static final int GAME = 0, VIEW = 1, CONNECTION = 2;

    private static final long serialVersionUID = 3473266281557865837L;

    private static final MidiSequencer SEQUENCER = MidiSequencer.getInstance();

    private static DialogSettings instance = null;

    private JTabbedPane tabbedPane;

    private JTextArea errorArea;

    private JButton bnOk, bnCancel, bnApply;

    private JTextField gameGenName;

    private JCheckBox gameGenAskName, gameGenAutoPause;

    private JComboBox gameGenPerformance;

    private JPanel gameDevPanel;

    private DeviceList gameDevList;

    private JComboBox gameDevPause;

    private JTextArea gameDevTxt;

    private JProgressBar gameDevProgress;

    private JComboBox viewGenNoteLetters, viewGenKeyboardLetters, viewGenFirstKey, viewGenLastKey;

    private JCheckBox viewGenKeyboard, viewGenScoreImages;

    private JTextArea viewGenTxt;

    private JProgressBar viewGenProgress;

    private JLabel viewGenFirstLabel, viewGenLastLabel;

    private int detecterId = 0;

    private boolean firstKey, bothKeys;

    private JComboBox viewFullDevice, viewFullResolution, viewFullColorDepth, viewFullRefreshRate;

    private GraphicsDevice graphicsDevice;

    private ArrayList<JFrame> identifierFrames = new ArrayList<JFrame>();

    private JCheckBox connConnUpdate, connConnToplist, connConnClients;

    private JTextField connConnPort;

    private JLabel connConnPortLabel;

    private JButton connConnTest;

    private JTextArea connConnTxt;

    private JProgressBar connConnProgress;

    private int testPort = -1;

    private JComboBox connProxyType;

    private JTextField connProxyAddress, connProxyPort;

    private JLabel connProxyPortLabel;

    private JCheckBox connProxyNoLocal;

    private JButton connProxyTest;

    private JTextArea connProxyTestTxt;

    private JProgressBar connProxyProgress;

    /**
	 * Solo constructor. The constructor creates a new TargetsHelp object and applies it to the
	 * given frame.
	 * 
	 * @param frame
	 *            the owner of the dialog. This is the frame from which the dialog is displayed.
	 * @see JDialog#JDialog(Frame, boolean)
	 */
    DialogSettings(JFrame frame) {
        super(frame, "Set_Settings", "settings", false, true, new Runnable() {

            public void run() {
                if (instance.bnOk.isEnabled() && instance.isVisible()) {
                    instance.save();
                    instance.close();
                }
            }
        });
        setMinimumSize(new Dimension(300, 300));
        DocumentListener documentListener = new DocumentListener() {

            public void changedUpdate(DocumentEvent e) {
                checkErrorsAndChanges();
            }

            public void removeUpdate(DocumentEvent e) {
                checkErrorsAndChanges();
            }

            public void insertUpdate(DocumentEvent e) {
                checkErrorsAndChanges();
            }
        };
        ChangeListener changeListener = new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                checkErrorsAndChanges();
            }
        };
        ItemListener itemListener = new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                checkErrorsAndChanges();
            }
        };
        JPanel panel;
        JPanel spanel;
        JButton button;
        String cache;
        Container contentPane = getContentPane();
        tabbedPane = new JTabbedPane();
        tabbedPane.addComponentListener(new ComponentAdapter() {

            public void componentResized(ComponentEvent e) {
                if (gameDevList != null) {
                    gameDevList.setPreferredSize(new Dimension(0, 0));
                    final Dimension subPanel = gameDevPanel.getPreferredSize();
                    final Dimension panel = getSize();
                    subPanel.width = Math.max(Math.max(panel.width - 100, 200), subPanel.width - 50);
                    subPanel.height = Math.max(panel.height - 550, 100);
                    gameDevList.setPreferredSize(subPanel);
                    gameDevList.updateUI();
                }
            }
        });
        tabbedPane.addTab(Util.getMsgMnemonic("Set_Game"), null, new JScrollPane(panel = new JPanel()), Util.getMsg("Set_Game_Help"));
        tabbedPane.setMnemonicAt(GAME, Util.getLastMnemonic());
        tabbedPane.setDisplayedMnemonicIndexAt(GAME, Util.getLastMnemonicIndex());
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setName("Set_Game");
        panel.add(spanel = new JPanel());
        spanel.add(spanel = new JPanel());
        spanel.setBorder(new NamedTitledBorder("Set_Game_Gen"));
        spanel.setLayout(Util.GRIDBAG);
        Util.addLabeledComponent(spanel, "Set_Game_GenName", gameGenName = new JTextField(15), documentListener);
        Util.addLabeledComponent(spanel, "Set_Game_GenAskName", gameGenAskName = new JCheckBox(), changeListener);
        Util.addLabeledComponent(spanel, "Set_Game_GenAutoPause", gameGenAutoPause = new JCheckBox(), changeListener);
        gameGenPerformance = new JComboBox();
        for (Map.Entry<Integer, String> entry : Game.PERFORMANCES.entrySet()) {
            gameGenPerformance.addItem(new Item<Integer, String>(entry.getKey(), Util.getMsg(entry.getValue()), entry.getValue()));
        }
        Util.addLabeledComponent(spanel, "Set_Game_GenPerformance", gameGenPerformance, itemListener);
        panel.add(spanel = new JPanel());
        gameDevPanel = spanel;
        spanel.add(spanel = new JPanel());
        spanel.setBorder(new NamedTitledBorder("Set_Game_Dev"));
        spanel.setLayout(Util.GRIDBAG);
        Util.addToCenter(spanel, gameDevList = new DeviceList("deviceList", changeListener));
        Util.addToRight(spanel, button = new JButton());
        Util.updateButtonText(button, "Refresh");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DeviceList.refresh();
            }
        });
        Util.addToCenter(spanel, gameDevTxt = new JTextArea());
        gameDevTxt.setEditable(false);
        gameDevTxt.setBackground(new Color(UIManager.getColor("Panel.background").getRGB()));
        gameDevTxt.setHighlighter(null);
        gameDevTxt.setBorder(new EmptyBorder(10, 10, 10, 10));
        gameDevTxt.setFont(new Font(Font.SANS_SERIF, 0, 12));
        gameDevTxt.setVisible(false);
        Util.addToCenter(spanel, gameDevProgress = new JProgressBar());
        gameDevProgress.setIndeterminate(true);
        gameDevProgress.setVisible(false);
        gameDevPause = new JComboBox();
        final byte max = (byte) MidiDevicer.CONTROLLERS.length;
        for (byte i = 0; i < max; ++i) {
            gameDevPause.addItem(new Item<Byte, String>(MidiDevicer.CONTROLLERS[i], String.format("0x%02x (%d)", MidiDevicer.CONTROLLERS[i], MidiDevicer.CONTROLLERS[i])));
        }
        Util.addLabeledComponent(spanel, "Set_Game_DevPause", gameDevPause, itemListener, button = new JButton());
        Util.updateButtonText(button, "Detect");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        gameDevTxt.setForeground(UIManager.getColor("Label.foreground"));
                        gameDevTxt.setVisible(true);
                        gameDevTxt.setText(Util.getMsg("Set_Game_DevDetectStart"));
                        gameDevProgress.setVisible(true);
                    }
                });
                SEQUENCER.setWaitingForController(true);
                (new Thread() {

                    public void run() {
                        try {
                            Thread.sleep(10100);
                            if (SEQUENCER.isWaitingForController()) {
                                gameDevTxt.setForeground(ERROR_COLOR);
                                gameDevTxt.setText(Util.getMsg("Set_DetectTimeOut"));
                                gameDevProgress.setVisible(false);
                                SEQUENCER.setWaitingForController(false);
                            }
                        } catch (Exception e) {
                        }
                    }
                }).start();
            }
        });
        tabbedPane.addTab(Util.getMsgMnemonic("Set_View"), null, new JScrollPane(panel = new JPanel()), Util.getMsg("Set_View_Help"));
        tabbedPane.setMnemonicAt(VIEW, Util.getLastMnemonic());
        tabbedPane.setDisplayedMnemonicIndexAt(VIEW, Util.getLastMnemonicIndex());
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setName("Set_View");
        panel.add(spanel = new JPanel());
        spanel.add(spanel = new JPanel());
        spanel.setBorder(new NamedTitledBorder("Set_View_Gen"));
        spanel.setLayout(Util.GRIDBAG);
        viewGenNoteLetters = new JComboBox();
        viewGenNoteLetters.addItem(new Item<Integer, String>(0, Util.getMsg("Set_View_GenLetters_None"), "Set_View_GenLetters_None"));
        viewGenNoteLetters.addItem(new Item<Integer, String>(1, Util.getMsg("Set_View_GenLetters_Name"), "Set_View_GenLetters_Name"));
        viewGenNoteLetters.addItem(new Item<Integer, String>(2, Util.getMsg("Set_View_GenLetters_Shorthand"), "Set_View_GenLetters_Shorthand"));
        viewGenNoteLetters.addItem(new Item<Integer, String>(3, Util.getMsg("Set_View_GenLetters_Numbered"), "Set_View_GenLetters_Numbered"));
        Util.addLabeledComponent(spanel, "Set_View_GenNoteLetters", viewGenNoteLetters, itemListener);
        Util.addLabeledComponent(spanel, "Set_View_GenDisplayKeyboard", viewGenKeyboard = new JCheckBox());
        viewGenKeyboard.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                viewGenKeyboardLetters.setEnabled(((JCheckBox) e.getSource()).isSelected());
                checkErrorsAndChanges();
            }
        });
        viewGenKeyboardLetters = new JComboBox();
        viewGenKeyboardLetters.addItem(new Item<Integer, String>(0, Util.getMsg("Set_View_GenLetters_None"), "Set_View_GenLetters_None"));
        viewGenKeyboardLetters.addItem(new Item<Integer, String>(1, Util.getMsg("Set_View_GenLetters_Name"), "Set_View_GenLetters_Name"));
        viewGenKeyboardLetters.addItem(new Item<Integer, String>(2, Util.getMsg("Set_View_GenLetters_Shorthand"), "Set_View_GenLetters_Shorthand"));
        viewGenKeyboardLetters.addItem(new Item<Integer, String>(3, Util.getMsg("Set_View_GenLetters_Numbered"), "Set_View_GenLetters_Numbered"));
        Util.addLabeledComponent(spanel, "Set_View_GenKeyboardLetters", viewGenKeyboardLetters, itemListener);
        viewGenFirstKey = new JComboBox();
        viewGenFirstKey.setToolTipText(cache = Util.getMsg("Set_View_GenKey_Help"));
        viewGenLastKey = new JComboBox();
        viewGenFirstKey.setToolTipText(cache);
        for (byte i = 0; i < 127; ++i) {
            final Key key = new Key(i, 0);
            if (!key.higher) {
                final Item<Byte, String> item;
                viewGenFirstKey.addItem(item = new Item<Byte, String>(i, key.toShorthand() + " | " + key.toNumbered() + " (" + i + ")"));
                viewGenLastKey.addItem(item);
            }
        }
        Util.addLabeledComponent(spanel, "Set_View_GenScoreImages", viewGenScoreImages = new JCheckBox(), changeListener);
        viewGenFirstLabel = Util.addLabeledComponent(spanel, "Set_View_GenFirstKey", viewGenFirstKey, itemListener, button = new JButton());
        Util.updateButtonText(button, "Detect");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        viewGenTxt.setForeground(UIManager.getColor("Label.foreground"));
                        viewGenTxt.setVisible(true);
                        viewGenTxt.setText(Util.getMsg("Set_View_GenDetectFirstStart"));
                        viewGenProgress.setVisible(true);
                    }
                });
                bothKeys = false;
                firstKey = true;
                SEQUENCER.setWaitingForNote(true);
                (new Thread() {

                    private int id = ++detecterId;

                    public void run() {
                        try {
                            Thread.sleep(10100);
                            if (id == detecterId && SEQUENCER.isWaitingForNote()) {
                                viewGenTxt.setForeground(ERROR_COLOR);
                                viewGenTxt.setText(Util.getMsg("Set_DetectTimeOut"));
                                viewGenProgress.setVisible(false);
                                SEQUENCER.setWaitingForNote(false);
                            }
                        } catch (Exception e) {
                        }
                    }
                }).start();
            }
        });
        viewGenLastLabel = Util.addLabeledComponent(spanel, "Set_View_GenLastKey", viewGenLastKey, itemListener, button = new JButton());
        Util.updateButtonText(button, "AltDetect");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        viewGenTxt.setForeground(UIManager.getColor("Label.foreground"));
                        viewGenTxt.setVisible(true);
                        viewGenTxt.setText(Util.getMsg("Set_View_GenDetectLastStart"));
                        viewGenProgress.setVisible(true);
                    }
                });
                bothKeys = false;
                firstKey = false;
                SEQUENCER.setWaitingForNote(true);
                (new Thread() {

                    private int id = ++detecterId;

                    public void run() {
                        try {
                            Thread.sleep(10100);
                            if (id == detecterId && SEQUENCER.isWaitingForNote()) {
                                viewGenTxt.setForeground(ERROR_COLOR);
                                viewGenTxt.setText(Util.getMsg("Set_DetectTimeOut"));
                                viewGenProgress.setVisible(false);
                                SEQUENCER.setWaitingForNote(false);
                            }
                        } catch (Exception e) {
                        }
                    }
                }).start();
            }
        });
        Util.addToCenter(spanel, button = new JButton());
        Util.updateButtonText(button, "Set_View_Gen_DetectBothKeys");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        viewGenTxt.setForeground(UIManager.getColor("Label.foreground"));
                        viewGenTxt.setVisible(true);
                        viewGenTxt.setText(Util.getMsg("Set_View_GenDetectFirstStart"));
                        viewGenProgress.setVisible(true);
                    }
                });
                bothKeys = true;
                firstKey = true;
                SEQUENCER.setWaitingForNote(true);
                (new Thread() {

                    private int id = ++detecterId;

                    public void run() {
                        try {
                            Thread.sleep(10100);
                            if (id == detecterId && SEQUENCER.isWaitingForNote()) {
                                viewGenTxt.setForeground(ERROR_COLOR);
                                viewGenTxt.setText(Util.getMsg("Set_DetectTimeOut"));
                                viewGenProgress.setVisible(false);
                                SEQUENCER.setWaitingForNote(false);
                            }
                        } catch (Exception e) {
                        }
                    }
                }).start();
            }
        });
        Util.addToCenter(spanel, viewGenTxt = new JTextArea());
        viewGenTxt.setEditable(false);
        viewGenTxt.setBackground(new Color(UIManager.getColor("Panel.background").getRGB()));
        viewGenTxt.setHighlighter(null);
        viewGenTxt.setBorder(new EmptyBorder(10, 10, 10, 10));
        viewGenTxt.setFont(new Font(Font.SANS_SERIF, 0, 12));
        viewGenTxt.setVisible(false);
        Util.addToCenter(spanel, viewGenProgress = new JProgressBar());
        viewGenProgress.setIndeterminate(true);
        viewGenProgress.setVisible(false);
        panel.add(spanel = new JPanel());
        spanel.add(spanel = new JPanel());
        spanel.setBorder(new NamedTitledBorder("Set_View_Full"));
        spanel.setLayout(Util.GRIDBAG);
        Util.addLabeledComponent(spanel, "Set_View_FullDevice", viewFullDevice = new JComboBox());
        Util.addLabeledComponent(spanel, "Set_View_FullResolution", viewFullResolution = new JComboBox());
        Util.addLabeledComponent(spanel, "Set_View_FullColorDepth", viewFullColorDepth = new JComboBox());
        Util.addLabeledComponent(spanel, "Set_View_FullRefreshRate", viewFullRefreshRate = new JComboBox(), itemListener);
        viewFullDevice.addItemListener(new ItemListener() {

            @SuppressWarnings("unchecked")
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    final String device = ((Item<String, String>) e.getItem()).getKey();
                    graphicsDevice = null;
                    GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
                    if (device == null) {
                        graphicsDevice = genv.getDefaultScreenDevice();
                    } else {
                        for (GraphicsDevice g : genv.getScreenDevices()) {
                            if (device.equals(g.getIDstring())) {
                                graphicsDevice = g;
                                break;
                            }
                        }
                    }
                    if (graphicsDevice != null) {
                        ArrayList<Dimension> array = new ArrayList<Dimension>();
                        Dimension dim;
                        for (DisplayMode d : graphicsDevice.getDisplayModes()) {
                            dim = new Dimension(d.getWidth(), d.getHeight());
                            if (!array.contains(dim)) array.add(dim);
                        }
                        Item<Dimension, String> item = (Item<Dimension, String>) viewFullResolution.getSelectedItem();
                        Dimension selected = null;
                        if (item != null) selected = item.getKey();
                        viewFullResolution.removeAllItems();
                        viewFullResolution.addItem(new Item<Dimension, String>(null, Util.getMsgMnemonic("Default")));
                        for (Dimension d : array) {
                            viewFullResolution.addItem(new Item<Dimension, String>(d, d.width + " × " + d.height));
                            if (d.equals(selected)) viewFullResolution.setSelectedIndex(viewFullResolution.getItemCount() - 1);
                        }
                    }
                }
                checkErrorsAndChanges();
            }
        });
        viewFullResolution.addItemListener(new ItemListener() {

            @SuppressWarnings("unchecked")
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (graphicsDevice != null) {
                        Dimension resolution = ((Item<Dimension, Item>) e.getItem()).getKey();
                        if (resolution == null) {
                            DisplayMode dm = graphicsDevice.getDisplayMode();
                            resolution = new Dimension(dm.getWidth(), dm.getHeight());
                        }
                        ArrayList<Integer> array = new ArrayList<Integer>();
                        Integer i;
                        for (DisplayMode d : graphicsDevice.getDisplayModes()) {
                            if (d.getWidth() == resolution.width && d.getHeight() == resolution.height) {
                                if (!array.contains(i = d.getBitDepth())) array.add(i);
                            }
                        }
                        Item<Integer, String> item = (Item<Integer, String>) viewFullColorDepth.getSelectedItem();
                        Integer selected = null;
                        if (item != null) selected = item.getKey();
                        viewFullColorDepth.removeAllItems();
                        viewFullColorDepth.addItem(new Item<Integer, String>(null, Util.getMsgMnemonic("Default")));
                        for (Integer element : array) {
                            viewFullColorDepth.addItem(new Item<Integer, String>(element, element.toString()));
                            if (element.equals(selected)) viewFullColorDepth.setSelectedIndex(viewFullColorDepth.getItemCount() - 1);
                        }
                    }
                }
                checkErrorsAndChanges();
            }
        });
        viewFullColorDepth.addItemListener(new ItemListener() {

            @SuppressWarnings("unchecked")
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (graphicsDevice != null) {
                        Dimension resolution = ((Item<Dimension, Item>) viewFullResolution.getSelectedItem()).getKey();
                        if (resolution == null) {
                            DisplayMode dm = graphicsDevice.getDisplayMode();
                            resolution = new Dimension(dm.getWidth(), dm.getHeight());
                        }
                        Integer colorDepth = ((Item<Integer, String>) e.getItem()).getKey();
                        if (colorDepth == null) {
                            colorDepth = graphicsDevice.getDisplayMode().getBitDepth();
                        }
                        ArrayList<Integer> array = new ArrayList<Integer>();
                        Integer i;
                        for (DisplayMode d : graphicsDevice.getDisplayModes()) {
                            if (d.getWidth() == resolution.width && d.getHeight() == resolution.height && d.getBitDepth() == colorDepth) {
                                if (!array.contains(i = d.getRefreshRate())) array.add(i);
                            }
                        }
                        Item<Integer, String> item = (Item<Integer, String>) viewFullRefreshRate.getSelectedItem();
                        Integer selected = null;
                        if (item != null) selected = item.getKey();
                        viewFullRefreshRate.removeAllItems();
                        viewFullRefreshRate.addItem(new Item<Integer, String>(null, Util.getMsgMnemonic("Default")));
                        for (Integer element : array) {
                            viewFullRefreshRate.addItem(new Item<Integer, String>(element, element.toString()));
                            if (element.equals(selected)) viewFullRefreshRate.setSelectedIndex(viewFullRefreshRate.getItemCount() - 1);
                        }
                    }
                }
                checkErrorsAndChanges();
            }
        });
        Util.addToCenter(spanel, button = new JButton());
        Util.updateButtonText(button, "Set_View_FullIdentifyDevices");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                final GraphicsDevice def = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
                identifierFrames.clear();
                final Thread thread = new Thread() {

                    public void run() {
                        try {
                            Thread.sleep(3000);
                            try {
                                for (JFrame frame : identifierFrames) {
                                    frame.setVisible(false);
                                    frame.dispose();
                                }
                            } catch (Exception ex) {
                            }
                        } catch (InterruptedException e) {
                        }
                    }
                };
                for (final GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
                    final String id = def.equals(gd) ? gd.getIDstring() + " (" + Util.getMsgMnemonic("Default") + ")" : gd.getIDstring();
                    final JFrame frame = new JFrame(id);
                    final Rectangle bounds = gd.getDefaultConfiguration().getBounds();
                    final TransparentComponent component = new TransparentComponent(frame, bounds);
                    identifierFrames.add(frame);
                    component.setLayout(new OverlayLayout(component));
                    JPanel panel = new JPanel(new BorderLayout());
                    panel.setOpaque(false);
                    component.add(panel);
                    JLabel label = new JLabel(id, 0);
                    label.setFont(new Font(null, Font.BOLD, 96));
                    label.setForeground(Color.LIGHT_GRAY);
                    panel.add(label, BorderLayout.CENTER);
                    panel = new JPanel(new BorderLayout());
                    panel.setOpaque(false);
                    component.add(panel);
                    label = new JLabel(id, 0);
                    label.setBorder(new EmptyBorder(15, 15, 0, 0));
                    label.setFont(new Font(null, Font.BOLD, 96));
                    label.setForeground(Color.BLACK);
                    panel.add(label, BorderLayout.CENTER);
                    frame.setLayout(new BorderLayout());
                    frame.getContentPane().add("Center", component);
                    frame.addMouseListener(new MouseAdapter() {

                        public void mouseClicked(MouseEvent e) {
                            thread.interrupt();
                            for (JFrame frame : identifierFrames) {
                                try {
                                    frame.setVisible(false);
                                    frame.dispose();
                                } catch (Exception ex) {
                                }
                            }
                        }
                    });
                    frame.addKeyListener(new KeyAdapter() {

                        public void keyPressed(KeyEvent e) {
                            switch(e.getKeyCode()) {
                                case KeyEvent.VK_ESCAPE:
                                case KeyEvent.VK_ENTER:
                                case KeyEvent.VK_SPACE:
                                    thread.interrupt();
                                    for (JFrame frame : identifierFrames) {
                                        try {
                                            frame.setVisible(false);
                                            frame.dispose();
                                        } catch (Exception ex) {
                                        }
                                    }
                                    break;
                            }
                        }
                    });
                    frame.setUndecorated(true);
                    frame.setBounds(bounds);
                    frame.setVisible(true);
                }
                thread.start();
            }
        });
        tabbedPane.addTab(Util.getMsgMnemonic("Set_Connection"), null, new JScrollPane(panel = new JPanel()), Util.getMsg("Set_Connection_Help"));
        tabbedPane.setMnemonicAt(CONNECTION, Util.getLastMnemonic());
        tabbedPane.setDisplayedMnemonicIndexAt(CONNECTION, Util.getLastMnemonicIndex());
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setName("Set_Connection");
        panel.add(spanel = new JPanel());
        spanel.add(spanel = new JPanel());
        spanel.setBorder(new NamedTitledBorder("Set_Conn_Conn"));
        spanel.setLayout(Util.GRIDBAG);
        Util.addLabeledComponent(spanel, "Set_Conn_ConnUpdate", connConnUpdate = new JCheckBox(), changeListener);
        Util.addLabeledComponent(spanel, "Set_Conn_ConnToplist", connConnToplist = new JCheckBox(), changeListener);
        Util.addLabeledComponent(spanel, "Set_Conn_ConnClients", connConnClients = new JCheckBox());
        connConnPortLabel = Util.addLabeledComponent(spanel, "Set_Conn_ConnPort", connConnPort = new JTextField(5), documentListener);
        connConnPort.setDocument(new NumericDocument());
        connConnPort.getDocument().addDocumentListener(documentListener);
        connConnClients.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                final boolean active = ((JCheckBox) e.getSource()).isSelected();
                connConnPort.setEnabled(active);
                connConnTest.setEnabled(active);
                if (!active) {
                    final int port = Integer.parseInt(connConnPort.getText());
                    if (port < 0) {
                        connConnPort.setText("0");
                    } else if (port > 65535) {
                        connConnPort.setText("65535");
                    }
                }
                checkErrorsAndChanges();
            }
        });
        Util.addToCenter(spanel, connConnTxt = new JTextArea());
        connConnTxt.setEditable(false);
        connConnTxt.setBackground(new Color(UIManager.getColor("Panel.background").getRGB()));
        connConnTxt.setHighlighter(null);
        connConnTxt.setBorder(new EmptyBorder(10, 10, 10, 10));
        connConnTxt.setFont(new Font(Font.SANS_SERIF, 0, 12));
        connConnTxt.setVisible(false);
        Util.addToCenter(spanel, connConnProgress = new JProgressBar());
        connConnProgress.setIndeterminate(true);
        connConnProgress.setVisible(false);
        Util.addToCenter(spanel, connConnTest = new JButton());
        Util.updateButtonText(connConnTest, "Set_Conn_ConnTest");
        connConnTest.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                connConnTxt.setForeground(UIManager.getColor("Label.foreground"));
                connConnTxt.setVisible(true);
                connConnTxt.setText(Util.getMsg("Set_Conn_ConnTestStart"));
                connConnProgress.setVisible(true);
                (new Thread() {

                    private ServerSocket server;

                    boolean noOutcome;

                    public void run() {
                        try {
                            int port = Integer.parseInt(connConnPort.getText());
                            final ServerSocket s = Connection.getServer();
                            if (s == null || s.isClosed() || s.getLocalPort() != port) {
                                server = new ServerSocket(port);
                                port = server.getLocalPort();
                                newConnection();
                            } else {
                                port = s.getLocalPort();
                            }
                            if (Util.getDebugLevel() > 30) Util.debug("Port: " + port);
                            if (testPort != port) {
                                if (testPort == Util.getPropInt("connPort")) {
                                    Connection.newPortMappings(-1, port, true);
                                } else {
                                    Connection.newPortMappings(testPort, port, true);
                                }
                            }
                            testPort = port;
                            URL url = new URL(Connection.URL_STR + "?req=portcheck&port=" + port);
                            URLConnection connection = url.openConnection(Connection.getProxy());
                            connection.setRequestProperty("User-Agent", Connection.USER_AGENT);
                            BufferedReader bufferedRdr = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            noOutcome = true;
                            (new Thread() {

                                public void run() {
                                    try {
                                        Thread.sleep(12000);
                                        if (noOutcome) {
                                            connConnTxt.setForeground(ERROR_COLOR);
                                            connConnTxt.setText(Util.getMsg("Set_Conn_ConnTestCannotConnect"));
                                            connConnProgress.setVisible(false);
                                            server.close();
                                        }
                                    } catch (Exception e) {
                                    }
                                }
                            }).start();
                            final String line = bufferedRdr.readLine();
                            bufferedRdr.close();
                            if (line != null && line.equals("ok")) {
                                connConnTxt.setForeground(SUCCESS_COLOR);
                                connConnTxt.setText(Util.getMsg("Set_Conn_ConnTestOk"));
                            } else if (line != null && line.equals("later")) {
                                connConnTxt.setForeground(ERROR_COLOR);
                                connConnTxt.setText(Util.getMsg("Set_Conn_ConnTestTryLater"));
                            } else {
                                connConnTxt.setForeground(ERROR_COLOR);
                                connConnTxt.setText(Util.getMsg("Set_Conn_ConnTestWrongResponse"));
                            }
                            connConnProgress.setVisible(false);
                        } catch (java.nio.channels.ClosedChannelException e) {
                            if (Util.getDebugLevel() > 90) e.printStackTrace();
                        } catch (FileNotFoundException e) {
                            connConnTxt.setForeground(ERROR_COLOR);
                            connConnTxt.setText(Util.getMsg("Set_Conn_ConnTestCannotConnect") + "\n" + Util.getMsg("Err_FileNotFound"));
                            connConnProgress.setVisible(false);
                        } catch (BindException e) {
                            connConnTxt.setForeground(ERROR_COLOR);
                            connConnTxt.setText(Util.getMsg("Err_PortInUse") + "!");
                            connConnProgress.setVisible(false);
                        } catch (SocketException e) {
                            connConnTxt.setForeground(ERROR_COLOR);
                            connConnTxt.setText(Util.getMsg("Set_Conn_ConnTestCannotConnect") + "\n" + e.getLocalizedMessage());
                            connConnProgress.setVisible(false);
                        } catch (Exception e) {
                            connConnTxt.setForeground(ERROR_COLOR);
                            connConnTxt.setText(Util.getMsg("Set_Conn_ConnTestCannotConnect") + "\n" + e.toString());
                            connConnProgress.setVisible(false);
                            if (Util.getDebugLevel() > 90) e.printStackTrace();
                        } finally {
                            noOutcome = false;
                            try {
                                if (server != null && !server.isClosed()) server.close();
                            } catch (IOException e) {
                                if (Util.getDebugLevel() > 90) e.printStackTrace();
                            }
                        }
                    }

                    void newConnection() {
                        (new Thread() {

                            public void run() {
                                try {
                                    final Socket socket = server.accept();
                                    newConnection();
                                    socket.setKeepAlive(true);
                                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                    String ln;
                                    while ((ln = in.readLine()) != null) {
                                        if (Util.getDebugLevel() > 30) Util.debug(ln);
                                        if (ln.equals("\0c\0h\0e\0c\0k\0u\0p\0")) {
                                            socket.getOutputStream().write(new byte[] { 0, 'O', 0, 'k' });
                                            socket.close();
                                        } else {
                                            final char c = (ln.length() > 0 ? ln.charAt(0) : 0);
                                            socket.getOutputStream().write(new byte[] { 0, 'U', (byte) (c >> 8), (byte) c });
                                        }
                                    }
                                } catch (SocketException e) {
                                    if (Util.getDebugLevel() > 68) {
                                        if (!e.getMessage().equals("socket closed")) {
                                            e.printStackTrace();
                                        }
                                    }
                                } catch (IOException e) {
                                    if (Util.getDebugLevel() > 90) e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }).start();
            }
        });
        panel.add(spanel = new JPanel());
        spanel.add(spanel = new JPanel());
        spanel.setBorder(new NamedTitledBorder("Set_Conn_Proxy"));
        spanel.setLayout(Util.GRIDBAG);
        Util.addLabeledComponent(spanel, "Set_Conn_ProxyType", connProxyType = new JComboBox());
        connProxyType.addItem(new Item<Integer, String>(0, Util.getMsg("Set_Conn_ProxyAuto"), "Set_Conn_ProxyAuto"));
        connProxyType.addItem(new Item<Integer, String>(1, Util.getMsg("Set_Conn_ProxyDirect"), "Set_Conn_ProxyDirect"));
        connProxyType.addItem(new Item<Integer, String>(2, Util.getMsg("Set_Conn_ProxyHttp"), "Set_Conn_ProxyHttp"));
        connProxyType.addItem(new Item<Integer, String>(3, Util.getMsg("Set_Conn_ProxySocks"), "Set_Conn_ProxySocks"));
        Util.addLabeledComponent(spanel, "Set_Conn_ProxyAddress", connProxyAddress = new JTextField(20), documentListener);
        connProxyAddress.getDocument().addDocumentListener(documentListener);
        connProxyPortLabel = Util.addLabeledComponent(spanel, "Set_Conn_ProxyPort", connProxyPort = new JTextField(5), documentListener);
        connProxyPort.setDocument(new NumericDocument());
        connProxyPort.getDocument().addDocumentListener(documentListener);
        Util.addLabeledComponent(spanel, "Set_Conn_ProxyNoLocal", connProxyNoLocal = new JCheckBox(), changeListener);
        connProxyType.addItemListener(new ItemListener() {

            @SuppressWarnings("unchecked")
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    final boolean active = ((Item<Integer, String>) e.getItem()).getKey() >= 2;
                    connProxyAddress.setEnabled(active);
                    connProxyPort.setEnabled(active);
                    if (!active) {
                        final int port = Integer.parseInt(connProxyPort.getText());
                        if (port < 0) {
                            connProxyPort.setText("0");
                        } else if (port > 65535) {
                            connProxyPort.setText("65535");
                        }
                    }
                }
                checkErrorsAndChanges();
            }
        });
        Util.addToCenter(spanel, connProxyTestTxt = new JTextArea());
        connProxyTestTxt.setEditable(false);
        connProxyTestTxt.setBackground(new Color(UIManager.getColor("Panel.background").getRGB()));
        connProxyTestTxt.setHighlighter(null);
        connProxyTestTxt.setBorder(new EmptyBorder(10, 10, 10, 10));
        connProxyTestTxt.setFont(new Font(Font.SANS_SERIF, 0, 12));
        connProxyTestTxt.setVisible(false);
        Util.addToCenter(spanel, connProxyProgress = new JProgressBar());
        connProxyProgress.setIndeterminate(true);
        connProxyProgress.setVisible(false);
        Util.addToCenter(spanel, connProxyTest = new JButton());
        Util.updateButtonText(connProxyTest, "Set_Conn_ProxyTest");
        connProxyTest.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        connProxyTestTxt.setForeground(UIManager.getColor("Label.foreground"));
                        connProxyTestTxt.setVisible(true);
                        connProxyTestTxt.setText(Util.getMsg("Set_Conn_ProxyTestStart"));
                        connProxyProgress.setVisible(true);
                    }
                });
                (new Thread() {

                    public void run() {
                        try {
                            URL url = new URL(Connection.URL_STR + "?req=check");
                            URLConnection connection = url.openConnection(Connection.getProxy(getAsInt(connProxyType), connProxyAddress.getText(), Integer.parseInt(connProxyPort.getText())));
                            connection.setRequestProperty("User-Agent", Connection.USER_AGENT);
                            BufferedReader bufferedRdr = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            final String line = bufferedRdr.readLine();
                            bufferedRdr.close();
                            SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    if (line.equals("ok")) {
                                        connProxyTestTxt.setForeground(SUCCESS_COLOR);
                                        connProxyTestTxt.setText(Util.getMsg("Set_Conn_ProxyTestOk"));
                                    } else {
                                        connProxyTestTxt.setForeground(ERROR_COLOR);
                                        connProxyTestTxt.setText(Util.getMsg("Set_Conn_ProxyTestWrongResponse"));
                                    }
                                    connProxyProgress.setVisible(false);
                                }
                            });
                        } catch (final FileNotFoundException e) {
                            SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    connProxyTestTxt.setForeground(ERROR_COLOR);
                                    connProxyTestTxt.setText(Util.getMsg("Set_Conn_ProxyTestCannotConnect") + "\n" + Util.getMsg("Set_Conn_ProxyTestFileNotFound"));
                                    connProxyProgress.setVisible(false);
                                }
                            });
                        } catch (final SocketException e) {
                            SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    connProxyTestTxt.setForeground(ERROR_COLOR);
                                    connProxyTestTxt.setText(Util.getMsg("Set_Conn_ProxyTestCannotConnect") + "\n" + e.getLocalizedMessage());
                                    connProxyProgress.setVisible(false);
                                }
                            });
                        } catch (final Exception e) {
                            SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    connProxyTestTxt.setForeground(ERROR_COLOR);
                                    connProxyTestTxt.setText(Util.getMsg("Set_Conn_ProxyTestCannotConnect") + "\n" + e.toString());
                                    connProxyProgress.setVisible(false);
                                }
                            });
                            if (Util.getDebugLevel() > 90) e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        contentPane.add("Center", tabbedPane);
        contentPane.add("South", panel = new JPanel());
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(spanel = new JPanel());
        spanel.setLayout(new FlowLayout());
        spanel.add(errorArea = new JTextArea());
        errorArea.setEditable(false);
        errorArea.setBackground(new Color(spanel.getBackground().getRGB()));
        errorArea.setHighlighter(null);
        errorArea.setBorder(new EmptyBorder(0, 0, 0, 0));
        errorArea.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        errorArea.setForeground(ERROR_COLOR);
        panel.add(spanel = new JPanel());
        spanel.setLayout(new FlowLayout());
        spanel.add(bnOk = new JButton());
        Util.updateButtonText(bnOk, "OK");
        bnOk.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                save();
                close();
            }
        });
        getRootPane().setDefaultButton(bnOk);
        spanel.add(bnCancel = new JButton());
        Util.updateButtonText(bnCancel, "Cancel");
        bnCancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        spanel.add(bnApply = new JButton());
        Util.updateButtonText(bnApply, "Apply");
        bnApply.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                save();
                checkErrorsAndChanges();
            }
        });
        tabbedPane.setSelectedIndex(Util.getPropInt("settingsTabIndex"));
        contentPane.add(tabbedPane);
        refresh();
        load();
        instance = this;
    }

    void checkErrorsAndChanges() {
        boolean changed = false;
        if (!gameGenName.getText().equals(Util.getProp("name"))) {
            changed = true;
        } else if (gameGenAskName.isSelected() != Util.getPropBool("askName")) {
            changed = true;
        } else if (gameGenAutoPause.isSelected() != Util.getPropBool("autoPause")) {
            changed = true;
        } else if (getAsInt(gameGenPerformance) != Game.getSleepTime()) {
            changed = true;
        } else if (DeviceList.isChanged()) {
            changed = true;
        } else if (getAsInt(viewGenNoteLetters) != Graphs.getNoteLetters()) {
            changed = true;
        } else if (viewGenKeyboard.isSelected() != Graphs.isKeyboard()) {
            changed = true;
        } else if (getAsInt(viewGenKeyboardLetters) != Graphs.getKeyboardLetters()) {
            changed = true;
        } else if (viewGenScoreImages.isSelected() != Graphs.isScoreImages()) {
            changed = true;
        } else if (getAsInt(viewGenFirstKey) != Graphs.getFirstKeyInMidi()) {
            changed = true;
        } else if (getAsInt(viewGenLastKey) != Graphs.getLastKeyInMidi()) {
            changed = true;
        } else if (!getAsString(viewFullDevice).equals(Util.getProp("viewDevice"))) {
            changed = true;
        } else if (!getAsString(viewFullResolution).equals(Util.getProp("viewResolution"))) {
            changed = true;
        } else if (!getAsString(viewFullColorDepth).equals(Util.getProp("viewColorDepth"))) {
            changed = true;
        } else if (!getAsString(viewFullRefreshRate).equals(Util.getProp("viewRefreshRate"))) {
            changed = true;
        } else if (connConnUpdate.isSelected() != Util.getPropBool("connUpdate")) {
            changed = true;
        } else if (connConnToplist.isSelected() != Util.getPropBool("connToplist")) {
            changed = true;
        } else if (connConnClients.isSelected() != Util.getPropBool("connClients")) {
            changed = true;
        } else if (!connConnPort.getText().equals(Util.getProp("connPort"))) {
            changed = true;
        } else if (!getAsString(connProxyType).equals(Util.getProp("proxyType"))) {
            changed = true;
        } else if (!connProxyAddress.getText().equals(Util.getProp("proxyAddress"))) {
            changed = true;
        } else if (!connProxyPort.getText().equals(Util.getProp("proxyPort"))) {
            changed = true;
        } else if (connProxyNoLocal.isSelected() != Util.getPropBool("proxyNoLocal")) {
            changed = true;
        }
        boolean noErrors = true;
        String errorTxt = "";
        if (gameGenName.getText().length() > 14) {
            noErrors = false;
            errorTxt += Util.getMsg("Err_TooLongName") + "\n";
        }
        final Color color;
        if (getAsInt(viewGenFirstKey) > getAsInt(viewGenLastKey)) {
            noErrors = false;
            errorTxt += Util.getMsg("Err_FirstBeforeLast") + "\n";
            color = ERROR_COLOR;
        } else {
            color = UIManager.getColor("Label.foreground");
        }
        viewGenFirstLabel.setForeground(color);
        viewGenLastLabel.setForeground(color);
        if (graphicsDevice == null) {
            refresh();
        }
        boolean subOk = true;
        try {
            final int port = Integer.parseInt(connConnPort.getText());
            if (port < 0 || port > 65535) {
                subOk = noErrors = false;
                errorTxt += Util.getMsg("Err_PortNotInRange") + "\n";
            }
        } catch (NumberFormatException e) {
            subOk = noErrors = false;
            errorTxt += Util.getMsg("Err_PortNotNumber") + "\n";
        }
        connConnPortLabel.setForeground(subOk ? UIManager.getColor("Label.foreground") : ERROR_COLOR);
        connConnTest.setEnabled(subOk && connConnClients.isSelected());
        subOk = true;
        try {
            final int port = Integer.parseInt(connProxyPort.getText());
            if (port < 0 || port > 65535) {
                subOk = noErrors = false;
                errorTxt += Util.getMsg("Err_PortNotInRange") + "\n";
            }
        } catch (NumberFormatException e) {
            subOk = noErrors = false;
            errorTxt += Util.getMsg("Err_PortNotNumber") + "\n";
        }
        connProxyPortLabel.setForeground(subOk ? UIManager.getColor("Label.foreground") : ERROR_COLOR);
        connProxyTest.setEnabled(subOk);
        final int length = errorTxt.length();
        errorArea.setText(length > 0 ? errorTxt.substring(0, errorTxt.length() - 1) : "");
        bnApply.setEnabled(changed && noErrors);
        bnOk.setEnabled(noErrors);
    }

    protected void open() {
        open(-1);
    }

    protected void open(int tab) {
        if (tab != -1) tabbedPane.setSelectedIndex(tab);
        refresh();
        load();
        wasOpened = true;
        try {
            setVisible(true);
        } catch (Throwable e) {
            dispose();
            ((DialogSettings) getNewInstance((JFrame) this.getOwner())).open(tab);
        }
    }

    @SuppressWarnings("unchecked")
    void refresh() {
        String cache;
        Item<String, String> item = (Item<String, String>) viewFullDevice.getSelectedItem();
        String selected = null;
        if (item != null) selected = item.getKey();
        viewFullDevice.removeAllItems();
        viewFullDevice.addItem(new Item<String, String>(null, Util.getMsgMnemonic("Default")));
        for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            cache = gd.getIDstring();
            viewFullDevice.addItem(new Item<String, String>(cache, cache));
            if (cache.equals(selected)) viewFullDevice.setSelectedIndex(viewFullDevice.getItemCount() - 1);
        }
    }

    void save() {
        String cache;
        if (!(cache = gameGenName.getText()).equals(Util.getProp("name"))) {
            Connection.writeTotalScore();
            Util.setProp("name", cache);
            Connection.readTotalScore();
            Connection.sendName();
        }
        Util.setProp("askName", gameGenAskName.isSelected());
        Game.setAutoPause(gameGenAutoPause.isSelected());
        Game.setSleepTime(getAsInt(gameGenPerformance));
        DeviceList.saveValues();
        MidiSequencer.setPause((byte) getAsInt(gameDevPause));
        Graphs.setNoteLetters(getAsInt(viewGenNoteLetters));
        Graphs.setKeyboard(viewGenKeyboard.isSelected());
        Graphs.setKeyboardLetters(getAsInt(viewGenKeyboardLetters));
        Graphs.setScoreImages(viewGenScoreImages.isSelected());
        Graphs.setFirstKey(getAsInt(viewGenFirstKey));
        Graphs.setLastKey(getAsInt(viewGenLastKey));
        Util.setProp("viewDevice", getAsString(viewFullDevice));
        Util.setProp("viewResolution", getAsString(viewFullResolution));
        Util.setProp("viewColorDepth", getAsString(viewFullColorDepth));
        Util.setProp("viewRefreshRate", getAsString(viewFullRefreshRate));
        final boolean connUpdate = connConnUpdate.isSelected();
        if (Util.getPropBool("connUpdate") != connUpdate) {
            Util.setProp("connUpdate", connUpdate);
            Connection.removeStatuses();
        }
        Util.setProp("connToplist", connConnToplist.isSelected());
        boolean needListening = false;
        final boolean connClients = connConnClients.isSelected();
        if (connClients != Util.getPropBool("connClients") || connClients != Connection.isListening()) {
            Util.setProp("connClients", connClients);
            if (connClients) {
                needListening = true;
            } else {
                Connection.stopListening();
            }
        }
        if (!(cache = connConnPort.getText()).equals(Util.getProp("connPort"))) {
            Util.setProp("connPort", cache);
            final int port = Util.getPropInt("connPort");
            if (testPort != port) {
                Connection.newPortMappings(testPort, port);
                testPort = -1;
            }
            needListening = true;
        }
        if (needListening) {
            Connection.startListening();
        }
        boolean proxyChanged = false;
        if (!(cache = getAsString(connProxyType)).equals(Util.getProp("proxyType"))) {
            Util.setProp("proxyType", cache);
            proxyChanged = true;
        }
        if (!(cache = connProxyAddress.getText()).equals(Util.getProp("proxyAddress"))) {
            Util.setProp("proxyAddress", cache);
            proxyChanged = true;
        }
        if (!(cache = connProxyPort.getText()).equals(Util.getProp("proxyPort"))) {
            Util.setProp("proxyPort", cache);
            proxyChanged = true;
        }
        if (proxyChanged) {
            Connection.resetProxy();
        }
        Util.setProp("proxyNoLocal", connProxyNoLocal.isSelected());
    }

    @SuppressWarnings("unchecked")
    void load() {
        boolean active;
        gameGenName.setText(Util.getProp("name"));
        gameGenAskName.setSelected(Util.getPropBool("askName"));
        gameGenAutoPause.setSelected(Game.isAutoPause());
        Util.selectKey(gameGenPerformance, Game.getSleepTime());
        DeviceList.loadValues();
        Util.selectKey(gameDevPause, MidiSequencer.getPause());
        Util.selectKey(viewGenNoteLetters, Graphs.getNoteLetters());
        viewGenKeyboard.setSelected(active = Graphs.isKeyboard());
        Util.selectKey(viewGenKeyboardLetters, Graphs.getKeyboardLetters());
        viewGenKeyboardLetters.setEnabled(active);
        viewGenScoreImages.setSelected(Graphs.isScoreImages());
        Util.selectKey(viewGenFirstKey, (byte) Graphs.getFirstKeyInMidi());
        Util.selectKey(viewGenLastKey, (byte) Graphs.getLastKeyInMidi());
        Util.selectKey(viewFullDevice, getProp("viewDevice"));
        String[] parts = Util.getProp("viewResolution").split("x");
        try {
            Util.selectKey(viewFullResolution, new Dimension((int) Double.parseDouble(parts[0]), (int) Double.parseDouble(parts[1])));
        } catch (Exception e) {
            Util.selectKey(viewFullResolution, null);
        }
        Util.selectKey(viewFullColorDepth, getPropInt("viewColorDepth"));
        Util.selectKey(viewFullRefreshRate, getPropInt("viewRefreshRate"));
        connConnUpdate.setSelected(Util.getPropBool("connUpdate"));
        connConnToplist.setSelected(Util.getPropBool("connToplist"));
        connConnClients.setSelected(Util.getPropBool("connClients"));
        connConnPort.setText(Util.getProp("connPort"));
        active = connConnClients.isSelected();
        connConnPort.setEnabled(active);
        connConnTest.setEnabled(active);
        connConnTxt.setVisible(false);
        connConnTxt.setVisible(false);
        Util.selectKey(connProxyType, getPropInt("proxyType"));
        connProxyAddress.setText(Util.getProp("proxyAddress"));
        connProxyPort.setText(Util.getProp("proxyPort"));
        connProxyNoLocal.setSelected(Util.getPropBool("proxyNoLocal"));
        active = ((Item<Integer, String>) connProxyType.getSelectedItem()).getKey() >= 2;
        connProxyAddress.setEnabled(active);
        connProxyPort.setEnabled(active);
        connProxyTestTxt.setVisible(false);
        connProxyProgress.setVisible(false);
    }

    @Override
    protected void updateTexts() {
        super.updateTexts();
        final String cache;
        viewGenFirstKey.setToolTipText(cache = Util.getMsg("Set_View_GenKey_Help"));
        viewGenLastKey.setToolTipText(cache);
    }

    @Override
    protected void updateUI() {
        super.updateUI();
        gameDevTxt.setBackground(new Color(UIManager.getColor("Panel.background").getRGB()));
        viewGenTxt.setBackground(new Color(UIManager.getColor("Panel.background").getRGB()));
        connConnTxt.setBackground(new Color(UIManager.getColor("Panel.background").getRGB()));
        connProxyTestTxt.setBackground(new Color(UIManager.getColor("Panel.background").getRGB()));
    }

    private void controllerPressed(byte controller) {
        if (controller == -1) {
            gameDevTxt.setForeground(ERROR_COLOR);
            gameDevTxt.setText(Util.getMsg("Set_Game_DevDetectWrongType"));
        } else if (Arrays.binarySearch(MidiDevicer.CONTROLLERS, controller) < 0) {
            gameDevTxt.setForeground(ERROR_COLOR);
            gameDevTxt.setText(Util.getMsg("Set_Game_DevDetectAssignedController"));
            gameDevProgress.setVisible(false);
        } else {
            gameDevTxt.setForeground(SUCCESS_COLOR);
            gameDevTxt.setText(Util.getMsg("Set_Game_DevDetectOk"));
            gameDevProgress.setVisible(false);
            Util.selectKey(gameDevPause, controller);
        }
    }

    static void setController(byte controller) {
        instance.controllerPressed(controller);
    }

    private void notePressed(byte note) {
        if (note == -1) {
            viewGenTxt.setForeground(ERROR_COLOR);
            viewGenTxt.setText(Util.getMsg("Set_View_GenDetectWrongType"));
        } else {
            final Key key = new Key(note, 0);
            Util.selectKey((firstKey ? viewGenFirstKey : viewGenLastKey), (byte) (key.higher ? note - 1 : note));
            if (bothKeys) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        viewGenTxt.setForeground(UIManager.getColor("Label.foreground"));
                        viewGenTxt.setVisible(true);
                        viewGenTxt.setText(Util.getMsg("Set_View_GenDetectLastStart"));
                        viewGenProgress.setVisible(true);
                    }
                });
                bothKeys = false;
                firstKey = false;
                SEQUENCER.setWaitingForNote(true);
                (new Thread() {

                    private int id = ++detecterId;

                    public void run() {
                        try {
                            Thread.sleep(10100);
                            if (id == detecterId && SEQUENCER.isWaitingForNote()) {
                                viewGenTxt.setForeground(ERROR_COLOR);
                                viewGenTxt.setText(Util.getMsg("Set_DetectTimeOut"));
                                viewGenProgress.setVisible(false);
                                SEQUENCER.setWaitingForNote(false);
                            }
                        } catch (Exception e) {
                        }
                    }
                }).start();
            } else {
                viewGenTxt.setForeground(SUCCESS_COLOR);
                viewGenTxt.setText(Util.getMsg("Set_View_GenDetectOk"));
                viewGenProgress.setVisible(false);
            }
        }
    }

    static void setNote(byte note) {
        instance.notePressed(note);
    }

    private static int getAsInt(JComboBox comboBox) {
        final Item<?, ?> item = (Item<?, ?>) comboBox.getSelectedItem();
        if (item == null) return 0;
        final Object key = item.getKey();
        if (key instanceof String) {
            return Integer.parseInt((String) key);
        } else if (key instanceof Integer) {
            return (Integer) key;
        } else if (key instanceof Byte) {
            return (Byte) key;
        } else {
            return 0;
        }
    }

    private static String getAsString(JComboBox comboBox) {
        final Item<?, ?> item = (Item<?, ?>) comboBox.getSelectedItem();
        if (item == null) return "";
        final Object key = item.getKey();
        if (key instanceof String) {
            return (String) key;
        } else if (key instanceof Integer) {
            return ((Integer) key).toString();
        } else if (key instanceof Dimension) {
            Dimension dim = (Dimension) key;
            return dim.getWidth() + "x" + dim.getHeight();
        } else if (key instanceof Byte) {
            return ((Byte) key).toString();
        } else {
            return "";
        }
    }

    private static String getProp(String key) {
        String cache = Util.getProp(key);
        if (cache.equals("")) return null;
        return cache;
    }

    private static Integer getPropInt(String key) {
        if (Util.getProp(key).equals("")) return null;
        return Util.getPropInt(key);
    }

    @Override
    protected void close() {
        super.close();
        SEQUENCER.setWaitingForController(false);
        gameDevTxt.setVisible(false);
        gameDevProgress.setVisible(false);
        SEQUENCER.setWaitingForNote(false);
        viewGenTxt.setVisible(false);
        viewGenProgress.setVisible(false);
        if (testPort != -1 && testPort != Util.getPropInt("connPort")) {
            Connection.newPortMappings(testPort, -1);
            testPort = -1;
        }
    }

    @Override
    protected void closure() {
        if (wasOpened) {
            super.closure();
            gameDevList.closure();
            Util.setProp(configKey + "TabIndex", tabbedPane.getSelectedIndex());
        }
    }

    @Override
    protected AbstractDialog getNewInstance(JFrame frame) {
        return new DialogSettings(frame);
    }

    /**
	 * Creates a string containing the most important information about the game. This method is
	 * used only for debugging and testing purposes.
	 * 
	 * @return the created string.
	 */
    static String getString() {
        return "DialogSettings(visible=" + instance.isVisible() + "; location=" + instance.getLocationOnScreen() + "; bounds=" + instance.getBounds() + ")";
    }

    /**
	 * This method serves security purposes. Provides an integrity string that will be checked by
	 * the {@link Connection#integrityCheck()} method; thus the application can only be altered if
	 * the source is known. Every class in the {@link keyboardhero} package has an integrity string.
	 * 
	 * @return the string of this class used for integrity checking.
	 */
    static String getIntegrityString() {
        return "df+!v+-.sayyďż˝6ďż˝2LMdsa";
    }

    /**
	 * The tester object of this class. It provides a debugging menu and unit tests for this class.
	 * Its only purpose is debugging or testing.
	 */
    static final Tester TESTER = new Tester("DialogSettings", new String[] { "getString()" }) {

        void menu(int choice) throws Exception {
            switch(choice) {
                case 5:
                    System.out.println(getString());
                    break;
                default:
                    baseMenu(choice);
                    break;
            }
        }

        void runUnitTests() throws Exception {
            higherTestStart("DialogSettings");
            testEq("getIntegrityString()", "df+!v+-.sayyďż˝6ďż˝2LMdsa", DialogSettings.getIntegrityString());
            higherTestEnd();
        }
    };

    /**
	 * Starts the class's developing menu. If this build is a developer's one it starts the
	 * application in a normal way with the exception that it starts the debugging tool for this
	 * class as well; otherwise exits with an error message.
	 * 
	 * @param args
	 *            the arguments given to the program.
	 * @see KeyboardHero#startApp()
	 */
    public static void main(String[] args) {
        Tester.mainer(args, TESTER);
    }
}
