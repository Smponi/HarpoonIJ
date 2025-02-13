package ca.alexgirard.harpoonIJ;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * Action to cycle to the next file in the Harpoon list.
 * If at the end of the list, wraps around to the beginning.
 */
public class CycleNextHarpoonAction extends AnAction {

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

        // Calculate next index with wraparound
        int nextIndex = (currentIndex + 1) % files.size();

        // Find first non-null file starting from nextIndex
        while (nextIndex != currentIndex) {
            if (files.get(nextIndex) != null) {
                HarpoonState.OpenFile(files.get(nextIndex), project);
                return;
            }
            nextIndex = (nextIndex + 1) % files.size();
        }
    }

    /**
     * Finds the index of either the currently open file or the last harpoon file in the files list
     * @param project Current project
     * @param files List of harpoon files
     * @return Index in the list, or last index if not found
     */
    private static int findCurrentIndex(Project project, java.util.List<VirtualFile> files) {
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

        // If neither found, return -1 to start from beginning
        return -1;
    }

    private void showNotification(Project project, String message) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("Harpoon Notifications")
                .createNotification(message, NotificationType.INFORMATION)
                .notify(project);
    }
} 