// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.remoteServer.configuration.deployment;

import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.remoteServer.configuration.RemoteServer;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.XCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DeploymentConfigurationBase<Self extends DeploymentConfigurationBase> extends DeploymentConfiguration
  implements PersistentStateComponent<Self> {
  private List<Option> myOptions = new ArrayList<>();

  @Override
  public PersistentStateComponent<?> getSerializer() {
    return this;
  }

  @Override
  public @Nullable Self getState() {
    return (Self)this;
  }

  @Override
  public void loadState(@NotNull Self state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  @Override
  public void checkConfiguration(RemoteServer<?> server, DeploymentSource deploymentSource) throws RuntimeConfigurationException {

  }

  @XCollection
  @Override
  public @NotNull List<Option> getSelectedOptions() {
    return myOptions;
  }

  @Override
  public void setSelectedOptions(@NotNull List<Option> selectedOptions) {
    myOptions = new ArrayList<>(selectedOptions);
  }
}
