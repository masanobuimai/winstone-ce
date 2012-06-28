package com.googlecode.intellimars.winstone;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.JavaRunConfigurationExtensionManager;
import com.intellij.execution.configurations.JavaRunConfigurationModule;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.ResourceBundle;


public class WinstoneConfiguration extends ModuleBasedConfiguration<JavaRunConfigurationModule> {
    @NonNls
    private static ResourceBundle ourBundle = ResourceBundle.getBundle("com.googlecode.intellimars.winstone.message");
    private static final Logger LOGGER = Logger.getInstance("Winstone");

    public String WEBAPP_DIRECTORY = "";
    public boolean USE_WEB_ROOT = true;
    public boolean USE_WEB_APPS;
    public String WORKING_DIRECTORY = "";
    public String CONTEXT_NAME = "";
    public String HTTP_PORT = "8080";
    public String AJP13_PORT = "-1";
    public boolean OPEN_WEB_BROWSER;
    public boolean USE_JASPER;
    public boolean USE_JNDI;
    public boolean USE_SERVLET_RELOADING;
    public boolean USE_DIRECTORY_LISTS = true;
    public String DEBUG_LEVEL = "5";
    public String WINSTONE_PROPERTIES_FILE = "";
    public String WINSTONE_JAR = "";
    public String COMMONLIB_DIRECTORY = "";
    public String PROGRAM_PARAMETERS = "";
    public String VM_PARAMETERS = "";
    public String ALTERNATIVE_JRE_PATH = "";
    public boolean ALTERNATIVE_JRE_PATH_ENABLED;
    public boolean USE_TOOLS_JAR;
    public String TOOLS_JAR;


    public WinstoneConfiguration(String name, Project project, WinstoneConfigurationType configurationType) {
        super(name, new JavaRunConfigurationModule(project, false), configurationType.getConfigurationFactories()[0]);
    }

    public Collection<Module> getValidModules() {
        return JavaRunConfigurationModule.getModulesForClass(getProject(), "winstone.Launcher");
    }

    protected ModuleBasedConfiguration createInstance() {
        return new WinstoneConfiguration(getName(), getProject(), WinstoneConfigurationType.getInstance());
    }

    public SettingsEditor<WinstoneConfiguration> getConfigurationEditor() {
        SettingsEditorGroup<WinstoneConfiguration> group = new SettingsEditorGroup<WinstoneConfiguration>();
        group.addEditor("Configuration", new WinstoneConfigurationEditor(getProject()));
        JavaRunConfigurationExtensionManager.getInstance().appendEditors(this, group);
        return group;
    }

    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
        WinstoneRunnableState state = new WinstoneRunnableState(env, this);
        state.setConsoleBuilder(TextConsoleBuilderFactory.getInstance().createBuilder(getProject()));
        return state;
    }

    public void checkConfiguration() throws RuntimeConfigurationException {
        if (!"".equals(CONTEXT_NAME) && CONTEXT_NAME.startsWith("/"))
            throw new RuntimeConfigurationException(ourBundle.getString("error.context.name"));
        if ("".equals(WEBAPP_DIRECTORY))
            throw new RuntimeConfigurationException(ourBundle.getString("error.webapp.dir"));
        if ("".equals(WORKING_DIRECTORY))
            throw new RuntimeConfigurationException(ourBundle.getString("error.working.dir"));
        if ("".equals(WINSTONE_JAR))
            throw new RuntimeConfigurationException(ourBundle.getString("error.winstone.jar"));
    }

    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);
        readModule(element);
        DefaultJDOMExternalizer.readExternal(this, element);
        if (WORKING_DIRECTORY == null || WORKING_DIRECTORY.equals(""))
            WORKING_DIRECTORY = getProject().getBaseDir().getPath();
        if (WINSTONE_JAR == null || WINSTONE_JAR.equals(""))
            WINSTONE_JAR = WinstoneRuntimeConfiguration.WINSTONE_JAR;
        if (COMMONLIB_DIRECTORY == null || COMMONLIB_DIRECTORY.equals(""))
            COMMONLIB_DIRECTORY = WinstoneRuntimeConfiguration.COMMONLIB_DIRECTORY;
    }

    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);
        writeModule(element);
        DefaultJDOMExternalizer.writeExternal(this, element);
    }
}
