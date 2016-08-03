import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Created by AriAy on 31.07.2016.
 */
public class ToolWindowRemote implements ToolWindowFactory {
    private JLabel connectedInfoText;
    private JTextField connectTo;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JPanel rootPanel = new JPanel(new VerticalFlowLayout());


        JPanel controlsPanel = new JPanel(new BorderLayout());
        controlsPanel.add(generateActionButton(23, "Enter"), BorderLayout.CENTER);
        controlsPanel.add(generateActionButton(19, "^"), BorderLayout.NORTH);
        controlsPanel.add(generateActionButton(20, "v"), BorderLayout.SOUTH);
        controlsPanel.add(generateActionButton(21, "<"), BorderLayout.WEST);
        controlsPanel.add(generateActionButton(22, ">"), BorderLayout.EAST);

        this.connectTo = new JTextField("192.168.0.80");
        connectedInfoText = new JLabel("Not Connected");
        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> adbConnect());
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> connectdCheck());

        JPanel extraRow1 = new JPanel(new BorderLayout());
        extraRow1.add(generateActionButton(4, "Back"), BorderLayout.EAST);
        extraRow1.add(generateActionButton(3, "Home"), BorderLayout.WEST);

        //Fill the content Panel
        rootPanel.add(connectedInfoText);

        if (isADBInstalled()) {
            rootPanel.add(connectTo);
            rootPanel.add(connectButton);
            rootPanel.add(refreshButton);

            rootPanel.add(controlsPanel);
            rootPanel.add(extraRow1);
        } else {
            //do not show controlls if adb is missing
            connectedInfoText.setText("ADB is not in the path");
        }
        //Add the content to the toolwindow
        toolWindow.getComponent().add(rootPanel);
    }

    private void adbConnect() {
        Utils.runCommand("adb connect " + connectTo.getText());
        connectdCheck();
    }

    private void connectdCheck() {
        try {
            Process p = Runtime.getRuntime().exec("adb devices");
            p.waitFor();
            String result = Utils.streamToString(p.getInputStream());
            String[] resultLines = result.split("\\r?\\n");
            boolean connected = false;
            for (String line : resultLines) {
                if (line.contains("no device") | line.contains("offline")) continue;
                if (line.contains("\tdevice")) {
                    this.connectedInfoText.setText("Connected to: " + line.substring(0, line.indexOf("\t")));
                    connected = true;
                }
            }
            if (connected == false) this.connectedInfoText.setText("Not connected");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private JButton generateActionButton(final int androidKeyEventID, String displayText) {
        JButton button = new JButton(displayText);
        button.addActionListener(e -> Utils.runCommand("adb shell input keyevent " + androidKeyEventID));
        return button;
    }

    private boolean isADBInstalled() {
        try {
            Process p = Runtime.getRuntime().exec("adb version");
            p.waitFor();
            String result = Utils.streamToString(p.getInputStream());
            if(result.contains("Android Debug Bridge"))return true;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

}
