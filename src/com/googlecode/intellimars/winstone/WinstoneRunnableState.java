package com.googlecode.intellimars.winstone;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.JavaRunConfigurationExtensionManager;
import com.intellij.execution.RunConfigurationExtension;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;

import java.io.File;

public class WinstoneRunnableState extends JavaCommandLineState {
    private static final Logger LOGGER = Logger.getInstance("Winstone");
    private RunnerSettings runnerSettings;
    private WinstoneConfiguration config;
    private Project project;


    public WinstoneRunnableState(ExecutionEnvironment env, WinstoneConfiguration _config) {
        super(env);
        runnerSettings = env.getRunnerSettings();
        config = _config;
        project = _config.getProject();
    }

    public ExecutionResult execute(Executor executor, ProgramRunner runner) throws ExecutionException {
        LOGGER.info("WinstoneRunnableState.execute");
        final ConsoleView console = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        ProcessHandler processHandler = startProcess();
        processHandler.addProcessListener(new ProcessAdapter() {
            public void startNotified(ProcessEvent event) {
                LOGGER.info("WinstoneRunnableState.startNotified");
                super.startNotified(event);
            }

            public void processTerminated(ProcessEvent event) {
                LOGGER.info("WinstoneRunnableState.processTerminated");
            }

            public void processWillTerminate(ProcessEvent event, boolean willBeDestroyed) {
                LOGGER.info("WinstoneRunnableState.processWillTerminate");
            }

            public void onTextAvailable(ProcessEvent event, Key outputType) {
                LOGGER.info("WinstoneRunnableState.onTextAvailable:");
                if (config.OPEN_WEB_BROWSER && event.getText().contains("HTTP Listener started: port=")) {
                    BrowserUtil.launchBrowser("http://localhost:" + config.HTTP_PORT + "/" + config.CONTEXT_NAME);
                }
                super.onTextAvailable(event, outputType);
            }
        });
        console.attachToProcess(processHandler);
        return new DefaultExecutionResult(console, processHandler, createActions(console, processHandler));
    }


    protected JavaParameters createJavaParameters() throws ExecutionException {
        LOGGER.info("WinstoneRunnableState.createJavaParameters");
        JavaParameters javaParameters = new JavaParameters();

        String jreHome = config.ALTERNATIVE_JRE_PATH_ENABLED ? config.ALTERNATIVE_JRE_PATH : null;
        JavaParametersUtil.configureProject(project, javaParameters, JavaParameters.JDK_ONLY, jreHome);
        JavaParametersUtil.configureConfiguration(javaParameters, config);
        javaParameters.setMainClass(config.WINSTONE_RUN_CLASS);
        javaParameters.getClassPath().add(new File(config.WINSTONE_JAR).getPath());

        // ここで RunConfigurationExtension にまわすことで，Coverageプラグインが有効なら
        // カバレッジが取得できるようになる。
        for (RunConfigurationExtension extension : Extensions.getExtensions(RunConfigurationExtension.EP_NAME)) {
            extension.updateJavaParameters(config, javaParameters, getRunnerSettings());
        }

        return javaParameters;
    }

    protected OSProcessHandler startProcess() throws ExecutionException {
        OSProcessHandler osprocesshandler = super.startProcess();
        JavaRunConfigurationExtensionManager.getInstance().attachExtensionsToProcess(config, osprocesshandler, runnerSettings);
        return osprocesshandler;
    }

}
