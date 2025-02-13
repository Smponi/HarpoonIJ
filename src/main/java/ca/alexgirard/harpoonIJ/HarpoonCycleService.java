package ca.alexgirard.harpoonIJ;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Service class for cycling through Harpoon files.
 * Provides common functionality for navigating through the file list.
 */
public class HarpoonCycleService {
    
    /**
     * Cycles through the Harpoon files in the specified direction
     * @param project Current project
     * @param direction 1 for forward, -1 for backward
     * @return true if a file was opened, false otherwise
     */
    public static boolean cycleFiles(Project project, int direction) {
        if (project == null) return false;

        var files = HarpoonState.GetFiles(project);
        if (files.isEmpty()) {
            showNotification(project, "No files in Harpoon list");
            return false;
        }

        return findCurrentIndex(project, files)
                .map(currentIndex -> findAndOpenNextFile(currentIndex, direction, files, project))
                .orElse(false);
    }

    /**
     * Finds and opens the next valid file in the specified direction
     */
    private static boolean findAndOpenNextFile(int currentIndex, int direction, List<VirtualFile> files, Project project) {
        int nextIndex = currentIndex;
        int size = files.size();
        
        do {
            nextIndex = Math.floorMod(nextIndex + direction, size);
            if (files.get(nextIndex) != null) {
                HarpoonState.OpenFile(files.get(nextIndex), project);
                return true;
            }
        } while (nextIndex != currentIndex);
        
        return false;
    }

    /**
     * Finds the index of either the currently open file or the last harpoon file
     */
    private static Optional<Integer> findCurrentIndex(Project project, List<VirtualFile> files) {
        // First try currently open file
        return Optional.of(FileEditorManager.getInstance(project).getSelectedFiles())
                .filter(currentFiles -> currentFiles.length > 0)
                .map(currentFiles -> currentFiles[0])
                .flatMap(currentFile -> findFileIndex(currentFile, files))
                // Then try last accessed file
                .or(() -> Optional.ofNullable(HarpoonState.GetLastAccessedFile())
                        .flatMap(lastFile -> findFileIndex(lastFile, files)))
                // If neither found, start from beginning/end based on direction
                .or(() -> Optional.of(-1));
    }

    /**
     * Finds the index of a specific file in the list using streams
     */
    private static Optional<Integer> findFileIndex(VirtualFile fileToFind, List<VirtualFile> files) {
        return IntStream.range(0, files.size())
                .filter(i -> Optional.ofNullable(files.get(i))
                        .map(f -> f.getPath().equals(fileToFind.getPath()))
                        .orElse(false))
                .boxed()
                .findFirst();
    }

    private static void showNotification(Project project, String message) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("Harpoon Notifications")
                .createNotification(message, NotificationType.INFORMATION)
                .notify(project);
    }
} 