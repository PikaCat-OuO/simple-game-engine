package editor;

import imgui.ImGui;
import observers.EventSystem;
import observers.events.Event;
import observers.events.EventType;

import javax.swing.*;

public class MenuBar {

    public void imgui() {
        ImGui.beginMainMenuBar();

        if (ImGui.beginMenu("File")) {
            if (ImGui.menuItem("Open", "")) {
                JFileChooser chooser = new JFileChooser("./");
                chooser.setMultiSelectionEnabled(false);
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    EventSystem.notify(null, new Event(EventType.LOAD_LEVEL,
                            chooser.getSelectedFile().getAbsolutePath()));
                }
            }

            if (ImGui.menuItem("Save", "")) {
                JFileChooser chooser = new JFileChooser("./");
                chooser.setMultiSelectionEnabled(false);
                if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    EventSystem.notify(null, new Event(EventType.SAVE_LEVEL,
                            chooser.getSelectedFile().getAbsolutePath()));
                }
            }

            ImGui.endMenu();
        }

        ImGui.endMainMenuBar();
    }
}
