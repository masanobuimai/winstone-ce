package com.googlecode.intellimars.winstone;

import com.intellij.debugger.engine.DebuggerUtils;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.RunConfigurationExtension;
import com.intellij.execution.configurations.DebuggingRunnerData;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

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

        // Winstoneの起動クラスを設定
        javaParameters.setMainClass("winstone.Launcher");
        javaParameters.getClassPath().add(new File(config.WINSTONE_JAR).getPath());

        Module module = config.getConfigurationModule().getModule();
        Sdk jdk = module != null ?
                  ModuleRootManager.getInstance(module).getSdk() :
                  ProjectRootManager.getInstance(project).getProjectJdk();
        javaParameters.setJdk(jdk);

        // よく考えてみれば，Winstoneがクラスロードするから，Winstone自身にクラスパスを設定してあげる必要はない。
//        JavaParametersUtil.configureProject(project, javaParameters, JavaParameters.JDK_AND_CLASSES_AND_TESTS, null);
//        JavaSdkUtil.addRtJar(javaParameters.getClassPath());
        javaParameters.setWorkingDirectory(config.WORKING_DIRECTORY);

        ParametersList params = javaParameters.getProgramParametersList();
        if (config.USE_WEB_ROOT) params.add("--webroot=" + config.WEBAPP_DIRECTORY);
        if (config.USE_WEB_APPS) params.add("--webappsDir=" + config.WEBAPP_DIRECTORY);
        if (!"".equals(config.CONTEXT_NAME)) params.add("--prefix=/" + config.CONTEXT_NAME);
        if (!"".equals(config.HTTP_PORT)) params.add("--httpPort=" + config.HTTP_PORT);
        if (!"".equals(config.AJP13_PORT)) params.add("--ajp13Port=" + config.AJP13_PORT);
        if (config.USE_JASPER) {
            params.add("--useJasper=true");
            String JDK_HOME = jdk.getHomePath();
            params.add("--javaHome=" + JDK_HOME);
            String TOOLS_JAR = config.USE_TOOLS_JAR ? config.TOOLS_JAR : JDK_HOME + "/lib/tools.jar";
            params.add("--toolsJar=" + TOOLS_JAR);
        }
        params.add("--useJNDI=" + config.USE_JNDI);
        params.add("--useServletReloading=" + config.USE_SERVLET_RELOADING);
        params.add("--directoryListings=" + config.USE_DIRECTORY_LISTS);
        params.add("--debug=" + config.DEBUG_LEVEL);
        if (!"".equals(config.WINSTONE_PROPERTIES_FILE)) params.add("--config=" + config.WINSTONE_PROPERTIES_FILE);
        if (!"".equals(config.COMMONLIB_DIRECTORY))
            params.add("--commonLibFolder=" + new File(config.COMMONLIB_DIRECTORY).getPath());


        if (!"".equals(config.VM_PARAMETERS)) {
            ParametersList vmparams = javaParameters.getVMParametersList();
            //String vm_parameters = config.VM_PARAMETERS;
            // Need to split this string as GeneralCommandLine.appendParams()
            // quotes all params on Windows
            // See the (?=X) definition in the java.util.regex.Pattern javadoc
            vmparams.addAll(config.VM_PARAMETERS.split(" (?=-)", 0));
        }

        if (runnerSettings.getData() instanceof DebuggingRunnerData) {
            ParametersList vmparams = javaParameters.getVMParametersList();
            String hostname = "localhost";
            try {
                hostname = InetAddress.getLocalHost().getHostName();
            }
            catch (UnknownHostException ignore) {
            }
            DebuggingRunnerData debuggingRunnerData = (DebuggingRunnerData) runnerSettings.getData();
            String debugPort = debuggingRunnerData.getDebugPort();
            if (debugPort.length() == 0) {
                try {
                    debugPort = DebuggerUtils.getInstance().findAvailableDebugAddress(true);
                }
                catch (ExecutionException e) {
                    LOGGER.error(e);
                }
                debuggingRunnerData.setDebugPort(debugPort);
            }
            vmparams.add("-Xdebug");
            vmparams.add("-Xrunjdwp:transport=dt_socket,address=" + hostname + ':' + debugPort + ",suspend=y,server=n");
        }

        // ここで RunConfigurationExtension にまわすことで，Coverageプラグインが有効なら
        // カバレッジが取得できるようになる。
        RunConfigurationExtension extensions[] = Extensions.getExtensions(RunConfigurationExtension.EP_NAME);
        for (RunConfigurationExtension extension : extensions) {
            extension.updateJavaParameters(config, javaParameters, getRunnerSettings());
        }

        return javaParameters;
    }

    protected OSProcessHandler startProcess() throws ExecutionException {
        OSProcessHandler osprocesshandler = super.startProcess();
        RunConfigurationExtension extensions[] = Extensions.getExtensions(RunConfigurationExtension.EP_NAME);
        for (RunConfigurationExtension extension : extensions) {
            extension.handleStartProcess(config, osprocesshandler);
        }

        return osprocesshandler;
    }

}
