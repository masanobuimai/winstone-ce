package com.googlecode.intellimars.winstone;

import com.intellij.execution.ui.AlternativeJREPanel;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.RawCommandLineEditor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.util.ResourceBundle;

public class WinstoneConfigurationEditor extends SettingsEditor<WinstoneConfiguration> {
    @NonNls
    private static ResourceBundle ourBundle = ResourceBundle.getBundle("com.googlecode.intellimars.winstone.message");
    private static final Logger LOGGER = Logger.getInstance("Winstone");

    private Project project;

    private JPanel rootPanel;
    private JTextField contextName;
    private TextFieldWithBrowseButton webroot;
    private TextFieldWithBrowseButton workingdir;
    private JTextField httpPort;
    private JCheckBox useJasper;
    private TextFieldWithBrowseButton winstonePropertiesFile;
    private RawCommandLineEditor vmParameters;
    private AlternativeJREPanel alternateJDK;
    private HyperlinkLabel hyperlinkLabel;
    private JCheckBox useJNDI;
    private JComboBox debugLevel;
    private TextFieldWithBrowseButton winstoneJar;
    private TextFieldWithBrowseButton commonLib;
    private JCheckBox useServletReload;
    private JCheckBox useToolsJar;
    private TextFieldWithBrowseButton toolsJar;
    private JCheckBox openWebBrowser;
    private JRadioButton useWebApps;
    private JRadioButton useWebRoot;
    private JTextField ajp13Port;
    private JCheckBox useDirectoryLists;


    public WinstoneConfigurationEditor(Project _project) {
        project = _project;

        webroot.addBrowseFolderListener("Winstone", ourBundle.getString("select.webapp.dir"),
                project, new FileChooserDescriptor(false, true, false, false, false, false));

        workingdir.addBrowseFolderListener("Winstone", ourBundle.getString("select.working.dir"),
                project, new FileChooserDescriptor(false, true, false, false, false, false));

        winstonePropertiesFile.addBrowseFolderListener("Winstone", ourBundle.getString("select.winstone.props"),
                project,
                new FileChooserDescriptor(true, false, false, false, false, false) {
                    public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
                        if (!showHiddenFiles && file.getName().charAt(0) == '.')
                            return false;
                        return file.isDirectory() || "properties".equals(file.getExtension());
                    }
                });

        winstoneJar.addBrowseFolderListener("Winstone", ourBundle.getString("select.winstone.jar"), project,
                new FileChooserDescriptor(true, false, true, true, false, false));
        commonLib.addBrowseFolderListener("Winstone", ourBundle.getString("select.commonLibs"),
                project, new FileChooserDescriptor(false, true, false, false, false, false));
        useToolsJar.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                toolsJar.setEnabled(useToolsJar.isSelected());
            }
        });
        toolsJar.addBrowseFolderListener("Winstone", ourBundle.getString("select.tools.jar"), project,
                new FileChooserDescriptor(true, false, true, true, false, false));
    }


    protected void resetEditorFrom(WinstoneConfiguration config) {
        LOGGER.info("WinstoneConfigurationEditor.resetEditorFrom:" + config.hashCode());
        webroot.setText(config.WEBAPP_DIRECTORY);
        useWebRoot.setSelected(config.USE_WEB_ROOT);
        useWebApps.setSelected(config.USE_WEB_APPS);
        workingdir.setText(config.WORKING_DIRECTORY);
        contextName.setText(config.CONTEXT_NAME);
        openWebBrowser.setSelected(config.OPEN_WEB_BROWSER);
        httpPort.setText(config.HTTP_PORT);
        ajp13Port.setText(config.AJP13_PORT);
        useJasper.setSelected(config.USE_JASPER);
        useJNDI.setSelected(config.USE_JNDI);
        useServletReload.setSelected(config.USE_SERVLET_RELOADING);
        useDirectoryLists.setSelected(config.USE_DIRECTORY_LISTS);
        debugLevel.setSelectedItem(config.DEBUG_LEVEL);
        winstonePropertiesFile.setText(config.WINSTONE_PROPERTIES_FILE);
        winstoneJar.setText(config.WINSTONE_JAR);
        commonLib.setText(config.COMMONLIB_DIRECTORY);
        vmParameters.setText(config.VM_PARAMETERS);
        alternateJDK.init(config.ALTERNATIVE_JRE_PATH, config.ALTERNATIVE_JRE_PATH_ENABLED);
        useToolsJar.setSelected(config.USE_TOOLS_JAR);
        toolsJar.setText(config.TOOLS_JAR);
    }

    protected void applyEditorTo(WinstoneConfiguration config) throws ConfigurationException {
        config.WEBAPP_DIRECTORY = webroot.getText();
        config.USE_WEB_ROOT = useWebRoot.isSelected();
        config.USE_WEB_APPS = useWebApps.isSelected();
        config.WORKING_DIRECTORY = workingdir.getText();
        config.CONTEXT_NAME = contextName.getText();
        config.OPEN_WEB_BROWSER = openWebBrowser.isSelected();
        config.HTTP_PORT = httpPort.getText();
        config.AJP13_PORT = ajp13Port.getText();
        config.USE_JASPER = useJasper.isSelected();
        config.USE_JNDI = useJNDI.isSelected();
        config.USE_SERVLET_RELOADING = useServletReload.isSelected();
        config.USE_DIRECTORY_LISTS = useDirectoryLists.isSelected();
        config.DEBUG_LEVEL = (String) debugLevel.getSelectedItem();
        config.WINSTONE_PROPERTIES_FILE = winstonePropertiesFile.getText();
        config.WINSTONE_JAR = winstoneJar.getText();
        config.COMMONLIB_DIRECTORY = commonLib.getText();
        config.VM_PARAMETERS = vmParameters.getText();
        config.ALTERNATIVE_JRE_PATH = alternateJDK.getPath();
        config.ALTERNATIVE_JRE_PATH_ENABLED = alternateJDK.isPathEnabled();
        config.USE_TOOLS_JAR = useToolsJar.isSelected();
        config.TOOLS_JAR = toolsJar.getText();
    }

    @NotNull
    protected JComponent createEditor() {
        return rootPanel;
    }

    protected void disposeEditor() {
    }

    private void createUIComponents() {
        hyperlinkLabel = new HyperlinkLabel("Winstone Site");
        hyperlinkLabel.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                BrowserUtil.launchBrowser("http://winstone.sourceforge.net");
            }
        });
    }
}
