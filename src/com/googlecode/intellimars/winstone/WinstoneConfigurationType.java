package com.googlecode.intellimars.winstone;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class WinstoneConfigurationType implements ConfigurationType {
    private static final Icon ICON = IconLoader.getIcon("/com/googlecode/intellimars/winstone/small-icon.gif");
    private final ConfigurationFactory myFactory;


    public WinstoneConfigurationType() {
        myFactory = new ConfigurationFactory(this) {
            public RunConfiguration createTemplateConfiguration(Project project) {
                return new WinstoneConfiguration("", project, WinstoneConfigurationType.this);
            }
        };
    }


    public String getDisplayName() {
        return "Winstone";
    }

    public String getConfigurationTypeDescription() {
        return "Winstone Configuration";
    }

    public Icon getIcon() {
        return ICON;
    }

    @NotNull public String getId() {
        return "winstone";
    }

    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[] { myFactory };
    }

    @Nullable
    public static WinstoneConfigurationType getInstance() {
        return ContainerUtil.findInstance(Extensions.getExtensions(CONFIGURATION_TYPE_EP), WinstoneConfigurationType.class);
    }
}

