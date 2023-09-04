package ca.alexgirard.harpoonIJ;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.EditorTextField;
import com.maddyhome.idea.vim.KeyHandler;
import com.maddyhome.idea.vim.api.VimInjectorKt;
import com.maddyhome.idea.vim.command.MappingMode;
import com.maddyhome.idea.vim.key.MappingOwner;
import com.maddyhome.idea.vim.newapi.IjVimEditorKt;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class HarpoonDialog extends DialogWrapper {

    public EditorTextField editorTextField;
    private final String text;
    private boolean forceNormalMode;
    private boolean normalModeForcedAlready = false;
    public int SelectedIndex = -1;
    private static boolean enterRemapped = false;

    public void Ok() {

        SelectedIndex = editorTextField.getEditor().getCaretModel().getLogicalPosition().line;
        doOKAction();
    }

    protected HarpoonDialog(String inputText) {
        super(true);
        AppSettingsState settings = AppSettingsState.getInstance();
        setSize(settings.dialogWidth, settings.dialogHeight);
        setTitle("Harpoon");
        var vimPlugin = PluginManagerCore.getPlugin(PluginId.getId("IdeaVIM"));
        var vimEnabled = vimPlugin != null && vimPlugin.isEnabled();
        if (vimEnabled) {
            forceNormalMode = settings.dialogForceVimNormalMode;
            setEnterRemap(settings);
        }
        text = inputText;
        init();
    }

    //TODO move this to app startup or something 
    private void setEnterRemap(AppSettingsState settings) {
        if (enterRemapped || !settings.enterRemap) return;
        var keys = VimInjectorKt.injector.getParser().parseKeys(":action SelectHarpoonItem<cr><cr>");
        var keyGroup = VimInjectorKt.injector.getKeyGroup();
        keyGroup.putKeyMapping(MappingMode.NVO,
                VimInjectorKt.injector.getParser().parseKeys("<cr>")
                , MappingOwner.Plugin.Companion.get("HarpoonIj"), keys, false);
        enterRemapped = true;
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return editorTextField;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        AppSettingsState appSettings = AppSettingsState.getInstance();
        editorTextField = new EditorTextField(text);
        editorTextField.setOneLineMode(false);
        editorTextField.addSettingsProvider(editor -> {
            editor.setFontSize(appSettings.dialogFontSize);
            editor.setInsertMode(true);
            editor.getCaretModel().moveToLogicalPosition(new LogicalPosition(0,0));
            var settings = editor.getSettings();
            settings.setLineNumbersShown(true);
        });


        if (forceNormalMode) {
            editorTextField.addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {
                    if (normalModeForcedAlready) return;
                    var editor = editorTextField.getEditor();
                    if (editor == null) return;
                    var context = VimInjectorKt.injector.getExecutionContextManager().onEditor(IjVimEditorKt.getVim(editor), null);
                    KeyHandler.getInstance().handleKey(
                            IjVimEditorKt.getVim(editor),
                            VimInjectorKt.injector.getParser().parseKeys("<ESC>").get(0),
                            context
                    );
                    normalModeForcedAlready = true;
                }

                public void focusLost(FocusEvent e) {
                }
            });
        }
        return editorTextField;
    }


    @Override
    protected JComponent createSouthPanel() {
        return null;
    }
}
