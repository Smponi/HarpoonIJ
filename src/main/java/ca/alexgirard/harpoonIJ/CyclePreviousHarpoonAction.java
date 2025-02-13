package ca.alexgirard.harpoonIJ;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Action to cycle to the previous file in the Harpoon list.
 * If at the beginning of the list, wraps around to the end.
 */
public class CyclePreviousHarpoonAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        HarpoonCycleService.cycleFiles(e.getProject(), -1);
    }
} 