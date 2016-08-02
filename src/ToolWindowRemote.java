import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Created by AriAy on 31.07.2016.
 */
public class ToolWindowRemote implements ToolWindowFactory {
    private JLabel connectedText;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JPanel rootPanel = new JPanel(new VerticalFlowLayout());

        JPanel controlsPanel = new JPanel(new BorderLayout());
        controlsPanel.add(generateActionButton(22, "Enter"), BorderLayout.CENTER);
        controlsPanel.add(generateActionButton(23, "^"), BorderLayout.NORTH);
        controlsPanel.add(generateActionButton(24, "v"), BorderLayout.SOUTH);
        controlsPanel.add(generateActionButton(25, "<"), BorderLayout.WEST);
        controlsPanel.add(generateActionButton(26, ">"), BorderLayout.EAST);

        connectedText = new JLabel("Not Connected");
        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> adbConnect());
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> connectdCheck());
        //Fill the content Panel
        rootPanel.add(connectedText);
        rootPanel.add(connectButton);
        rootPanel.add(refreshButton);

        rootPanel.add(controlsPanel);
        //Add the content to the toolwindow
        toolWindow.getComponent().add(rootPanel);
    }

    private void adbConnect() {
        Utils.runCommand("adb connect 192.168.0.80");
        connectdCheck();
    }

    private void connectdCheck() {
        try {
            Process p = Runtime.getRuntime().exec("adb devices");
            p.waitFor();
            String result = Utils.streamToString(p.getInputStream());
            String[] resultLines = result.split("\\r?\\n");
            boolean connected=false;
            for (String line : resultLines) {
                if(line.contains("no device")|line.contains("offline"))continue;
                    if(line.contains("\tdevice")){
                    this.connectedText.setText("Connected to: "+line.substring(0,line.indexOf("\t")));
                    connected=true;
                }
            }
            if(connected==false)this.connectedText.setText("Not connected");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private JButton generateActionButton(int androidKeyEventID, String displayText) {
        JButton button = new JButton(displayText);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getID() == ActionEvent.ACTION_PERFORMED) {
                    Utils.runCommand("adb input KeyEvent " + androidKeyEventID);
                }
            }
        });
        return button;
    }
}
