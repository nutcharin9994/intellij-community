// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.openapi.vcs.actions;

import com.intellij.ide.actions.QuickSwitchSchemeAction;
import com.intellij.ide.ui.customization.CustomActionsSchema;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsActions;
import com.intellij.openapi.vcs.VcsBundle;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.JBIterable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Context aware VCS actions quick list.
 * <p>
 * Can be customized using {@link VcsQuickListContentProvider} extension point.
 */
@ApiStatus.Internal
public final class VcsQuickListPopupAction extends QuickSwitchSchemeAction implements DumbAware {
  public VcsQuickListPopupAction() {
    myActionPlace = ActionPlaces.ACTION_PLACE_VCS_QUICK_LIST_POPUP_ACTION;
  }

  @Override
  protected String getPopupTitle(@NotNull AnActionEvent e) {
    return VcsBundle.message("vcs.quicklist.popup.title");
  }

  @Override
  protected void fillActions(@Nullable Project project,
                             @NotNull DefaultActionGroup group,
                             @NotNull DataContext dataContext) {
    if (project == null) return;
    CustomActionsSchema schema = CustomActionsSchema.getInstance();
    AnAction correctedAction = schema.getCorrectedAction(VcsActions.VCS_OPERATIONS_POPUP);
    if (correctedAction != null) {
      group.add(correctedAction);
    }
  }

  private static boolean isUnderVcs(@NotNull Project project) {
    return ProjectLevelVcsManager.getInstance(project).hasActiveVcss();
  }

  private static @Nullable AbstractVcs getContextVcs(@Nullable Project project, @NotNull DataContext dataContext) {
    if (project == null) return null;

    ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(project);
    AbstractVcs[] activeVcss = vcsManager.getAllActiveVcss();
    if (activeVcss.length == 0) return null;
    if (activeVcss.length == 1) return activeVcss[0];

    VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);
    if (file == null) return null;
    return ProjectLevelVcsManager.getInstance(project).getVcsFor(file);
  }

  @ApiStatus.Internal
  public static final class Providers extends ActionGroup implements DumbAware {
    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
      if (e == null) return EMPTY_ARRAY;
      Project project = e.getProject();
      if (project == null) return EMPTY_ARRAY;

      JBIterable<VcsQuickListContentProvider> providers = JBIterable.of(VcsQuickListContentProvider.EP_NAME.getExtensions());
      JBIterable<AnAction> actions;
      if (!isUnderVcs(project)) {
        actions = providers.flatMap(p -> p.getNotInVcsActions(project, e));
      }
      else {
        AbstractVcs vcs = getContextVcs(project, e.getDataContext());
        if (vcs != null) {
          List<AnAction> replacingActions = providers
            .filterMap(p -> p.replaceVcsActionsFor(vcs, e.getDataContext()) ? p.getVcsActions(project, vcs, e) : null)
            .first();
          actions = replacingActions != null ? JBIterable.from(replacingActions) :
                    providers.flatMap(p -> p.getVcsActions(project, vcs, e));
        }
        else {
          actions = JBIterable.empty();
        }
      }
      return actions.toList().toArray(EMPTY_ARRAY);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
      return ActionUpdateThread.BGT;
    }
  }

  @ApiStatus.Internal
  public static final class VcsNameSeparator extends ActionGroup implements DumbAware {
    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
      if (e == null) return EMPTY_ARRAY;
      Project project = e.getProject();
      AbstractVcs vcs = getContextVcs(project, e.getDataContext());
      if (vcs != null) {
        return new AnAction[]{Separator.create(vcs.getDisplayName())};
      }
      else {
        return EMPTY_ARRAY;
      }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
      return ActionUpdateThread.BGT;
    }
  }

  @ApiStatus.Internal
  public static final class VcsAware extends DefaultActionGroup implements DumbAware {
    @Override
    public void update(@NotNull AnActionEvent e) {
      Project project = e.getProject();
      AbstractVcs vcs = getContextVcs(project, e.getDataContext());
      if (vcs != null) {
        JBIterable<VcsQuickListContentProvider> providers = JBIterable.of(VcsQuickListContentProvider.EP_NAME.getExtensions());
        List<AnAction> replacingActions = providers
          .filterMap(p -> p.replaceVcsActionsFor(vcs, e.getDataContext()) ? p.getVcsActions(project, vcs, e) : null)
          .first();
        e.getPresentation().setVisible(replacingActions == null);
      }
      else {
        e.getPresentation().setVisible(false);
      }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
      return ActionUpdateThread.BGT;
    }
  }

  @ApiStatus.Internal
  public static final class NonVcsAware extends DefaultActionGroup implements DumbAware {
    @Override
    public void update(@NotNull AnActionEvent e) {
      Project project = e.getProject();
      e.getPresentation().setVisible(project != null && !isUnderVcs(project));
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
      return ActionUpdateThread.BGT;
    }
  }
}
