package com.googlecode.intellimars.winstone;

import com.intellij.openapi.application.PathManager;

import java.io.File;

public class WinstoneRuntimeConfiguration {
    public final static String COMMONLIB_DIRECTORY;
    public final static String WINSTONE_JAR;

    static {
        File commonLib = new File(PathManager.getSystemPath(), "plugins");
        commonLib.mkdir();
        commonLib = new File(commonLib, "winstone-ce");
        commonLib.mkdir();
        commonLib = new File(commonLib, "commonLibs");
        commonLib.mkdir();
        COMMONLIB_DIRECTORY = commonLib.getAbsolutePath();

        WINSTONE_JAR = PathManager.getPluginsPath() + File.separator + "winstone-ce"
                       + File.separator + "lib" + File.separator + "winstone-0.9.10.jar";
    }


}

