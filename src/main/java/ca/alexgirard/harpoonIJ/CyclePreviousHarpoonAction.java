package ca.alexgirard.harpoonIJ;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class CyclePreviousHarpoonAction extends AnAction {

    // Stores the last file that was opened through any Harpoon action
    private static VirtualFile lastHarpoonFile = null;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        var files = HarpoonState.GetFiles(project);
        if (files.isEmpty()) {
            showNotification(project, "No files in Harpoon list");
            return;
        }

        // Get the current index based on either the current file or last harpoon file
        int currentIndex = findCurrentIndex(project, files);

        // Calculate previous index with wraparound
        int prevIndex = (currentIndex - 1 + files.size()) % files.size();

        // Find first non-null file starting from prevIndex going backwards
        while (prevIndex != currentIndex) {
            if (files.get(prevIndex) != null) {
                HarpoonState.OpenFile(files.get(prevIndex), project);
                return;
            }
            prevIndex = (prevIndex - 1 + files.size()) % files.size();
        }
    }

    /**
     * Finds the index of either the currently open file or the last harpoon file in the files list
     * @param project Current project
     * @param files List of harpoon files
     * @return Index in the list, or -1 if not found
     */
    protected static int findCurrentIndex(Project project, java.util.List<VirtualFile> files) {
        // First try to find the currently open file
        var currentFile = FileEditorManager.getInstance(project).getSelectedFiles();
        if (currentFile.length > 0) {
            for (int i = 0; i < files.size(); i++) {
                if (files.get(i) != null && files.get(i).getPath().equals(currentFile[0].getPath())) {
                    return i;
                }
            }
        }

        // If not found, try to find the last accessed harpoon file
        VirtualFile lastFile = HarpoonState.GetLastAccessedFile();
        if (lastFile != null) {
            for (int i = 0; i < files.size(); i++) {
                if (files.get(i) != null && files.get(i).getPath().equals(lastFile.getPath())) {
                    return i;
                }
            }
        }

        // If neither found, return last index to start from beginning
        return files.size() - 1;
    }

    private void showNotification(Project project, String message) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("Harpoon Notifications")
                .createNotification(message, NotificationType.INFORMATION)
                .notify(project);
    }
} 