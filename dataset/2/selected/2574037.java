package jlocator;

import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * The application's main frame.
 */
public class hacktohellView extends FrameView {

    public StringBuilder get(String q) {
        StringBuilder builder = new StringBuilder();
        try {
            URL url = new URL(q);
            URLConnection urlc = url.openConnection();
            BufferedInputStream buffer = new BufferedInputStream(urlc.getInputStream());
            int byteRead;
            while ((byteRead = buffer.read()) != -1) builder.append((char) byteRead);
            buffer.close();
        } catch (MalformedURLException ex) {
            JOptionPane.showMessageDialog(null, "Error " + ex.toString());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error " + ex.toString());
        }
        return builder;
    }

    public hacktohellView(SingleFrameApplication app) {
        super(app);
        initComponents();
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = hacktohell.getApplication().getMainFrame();
            aboutBox = new hacktohellAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        hacktohell.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {
        mainPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        getmyip = new javax.swing.JButton();
        getip = new javax.swing.JTextField();
        remip = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        answer = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        mainPanel.setName("mainPanel");
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(jlocator.hacktohell.class).getContext().getResourceMap(hacktohellView.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text"));
        jLabel1.setName("jLabel1");
        getmyip.setText(resourceMap.getString("getmyip.text"));
        getmyip.setName("getmyip");
        getmyip.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getmyipActionPerformed(evt);
            }
        });
        getip.setEditable(false);
        getip.setText(resourceMap.getString("getip.text"));
        getip.setName("getip");
        remip.setText(resourceMap.getString("remip.text"));
        remip.setName("remip");
        jButton3.setText(resourceMap.getString("jButton3.text"));
        jButton3.setName("jButton3");
        jButton3.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jScrollPane1.setName("jScrollPane1");
        answer.setColumns(20);
        answer.setEditable(false);
        answer.setRows(5);
        answer.setName("answer");
        jScrollPane1.setViewportView(answer);
        jLabel2.setText(resourceMap.getString("jLabel2.text"));
        jLabel2.setName("jLabel2");
        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(mainPanelLayout.createSequentialGroup().addContainerGap().addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE).addGroup(mainPanelLayout.createSequentialGroup().addComponent(getmyip).addGap(24, 24, 24).addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addComponent(remip).addComponent(getip, javax.swing.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE))).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup().addComponent(jLabel2).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 66, Short.MAX_VALUE).addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(18, 18, 18).addComponent(jButton3))).addContainerGap()));
        mainPanelLayout.setVerticalGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(mainPanelLayout.createSequentialGroup().addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(18, 18, 18).addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(getmyip).addComponent(getip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addGap(43, 43, 43).addComponent(remip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(mainPanelLayout.createSequentialGroup().addComponent(jLabel2).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 66, Short.MAX_VALUE).addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)).addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addContainerGap()));
        menuBar.setName("menuBar");
        fileMenu.setText(resourceMap.getString("fileMenu.text"));
        fileMenu.setName("fileMenu");
        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(jlocator.hacktohell.class).getContext().getActionMap(hacktohellView.class, this);
        exitMenuItem.setAction(actionMap.get("quit"));
        exitMenuItem.setName("exitMenuItem");
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);
        helpMenu.setText(resourceMap.getString("helpMenu.text"));
        helpMenu.setName("helpMenu");
        aboutMenuItem.setAction(actionMap.get("showAboutBox"));
        aboutMenuItem.setName("aboutMenuItem");
        helpMenu.add(aboutMenuItem);
        menuBar.add(helpMenu);
        statusPanel.setName("statusPanel");
        statusPanelSeparator.setName("statusPanelSeparator");
        statusMessageLabel.setName("statusMessageLabel");
        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel");
        progressBar.setName("progressBar");
        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE).addGroup(statusPanelLayout.createSequentialGroup().addContainerGap().addComponent(statusMessageLabel).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 230, Short.MAX_VALUE).addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(statusAnimationLabel).addContainerGap()));
        statusPanelLayout.setVerticalGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(statusPanelLayout.createSequentialGroup().addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(statusMessageLabel).addComponent(statusAnimationLabel).addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addGap(3, 3, 3)));
        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }

    private void getmyipActionPerformed(java.awt.event.ActionEvent evt) {
        StringBuilder r = get("http://icanhazip.com");
        getip.setText(r.toString());
    }

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {
        StringBuilder urlocation = new StringBuilder();
        StringBuilder remlocation = new StringBuilder();
        if (!getip.getText().equals("")) {
            urlocation = get("http://geoip.wtanaka.com/cc/" + getip.getText());
        }
        if (!remip.getText().equals("")) {
            remlocation = get("http://geoip.wtanaka.com/cc/" + remip.getText());
        }
        String ans = "Your Location:-" + (urlocation.toString()) + "\nRemote Location:-" + (remlocation.toString());
        answer.setText(ans);
    }

    private javax.swing.JTextArea answer;

    private javax.swing.JTextField getip;

    private javax.swing.JButton getmyip;

    private javax.swing.JButton jButton3;

    private javax.swing.JLabel jLabel1;

    private javax.swing.JLabel jLabel2;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JPanel mainPanel;

    private javax.swing.JMenuBar menuBar;

    private javax.swing.JProgressBar progressBar;

    private javax.swing.JTextField remip;

    private javax.swing.JLabel statusAnimationLabel;

    private javax.swing.JLabel statusMessageLabel;

    private javax.swing.JPanel statusPanel;

    private final Timer messageTimer;

    private final Timer busyIconTimer;

    private final Icon idleIcon;

    private final Icon[] busyIcons = new Icon[15];

    private int busyIconIndex = 0;

    private JDialog aboutBox;
}
