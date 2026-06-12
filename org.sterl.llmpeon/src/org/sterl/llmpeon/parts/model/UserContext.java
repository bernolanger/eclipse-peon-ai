package org.sterl.llmpeon.parts.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextSelection;
import org.sterl.llmpeon.StandingOrdersBuilder.MessageProvider;
import org.sterl.llmpeon.parts.shared.EclipseUtil;
import org.sterl.llmpeon.parts.shared.JdtUtil;
import org.sterl.llmpeon.shared.FileLines;
import org.sterl.llmpeon.shared.StringUtil;

public class UserContext implements MessageProvider {
    private volatile IProject currentProject;
    private volatile boolean projectPinned = false;

    private volatile IResource selectedResource;
    private volatile ITextSelection textSelection;

    @Override
    public String get() {
        if (currentProject == null && selectedResource == null) return null;
        
        var sb = new StringBuilder();
        if (currentProject != null) {
            sb.append("Select in Eclipse:\n");
            sb.append(EclipseUtil.projectInfo(currentProject));
        }
        addUserSelection(sb);
        return sb.toString();
    }

    private void addUserSelection(StringBuilder sb) {
        if (hasTextSelection()) {
            if (selectedResource == null || !(selectedResource instanceof IFile)) {
                sb.append("\n\n```\n" + FileLines.format(textSelection.getText(), textSelection.getStartLine() + 1) + "\n```");
                sb.append("\n").append(selectedResource == null ? "selected content not in a file." : "In " + selectedResource.getName() + " not a file.");
            } else {
                sb.append("\n").append(JdtUtil.pathOf(selectedResource)).append(" full content. Selected lines ").append(lines(textSelection)).append(":\n");
                try {
                    sb.append(((IFile)selectedResource).readString());
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (selectedResource != null) {
            sb.append("\nFile selected: ").append(JdtUtil.pathOf(selectedResource));
        }
    }
    
    public String getSelectedFile() {
        var open = EclipseUtil.getOpenFile();
        if (hasTextSelection()) {
            if (open.isEmpty()) return "Text line " + lines(textSelection);
            else {
                selectedResource = open.get();
                return open.get().getName() + ":" + lines(textSelection);
            }
        } else {
            if (selectedResource == null && open.isPresent()) return open.get().getName();
            if (selectedResource instanceof IFile rf) return rf.getName();
        }
        return null;
    }
    
    public boolean hasTextSelection() {
        return textSelection != null && !textSelection.isEmpty()
                && StringUtil.hasValue(textSelection.getText());
    }
    
    private static String lines(ITextSelection selection) {
        if (selection == null || selection.isEmpty()) return "";
        return (selection.getStartLine() + 1) + " - " + (selection.getEndLine() + 1);
    }
    
    public IResource getSelectedResource() {
        return selectedResource;
    }

    public IProject getCurrentProject() {
        return currentProject;
    }

    public void setCurrentProject(IProject currentProject) {
        this.currentProject = currentProject;
    }

    public ITextSelection getTextSelection() {
        return textSelection;
    }

    public void setTextSelection(ITextSelection textSelection) {
        this.textSelection = textSelection;
    }

    public void setSelectedResource(IResource selectedResource) {
        this.selectedResource = selectedResource;
    }

    public boolean isProjectPinned() {
        return projectPinned;
    }

    public void setProjectPinned(boolean projectPinned) {
        this.projectPinned = projectPinned;
    }
}
