package org.arxing.model;

import com.intellij.openapi.project.Project;

import org.arxing.util.FileHelper;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationData {

    private List<TraceTargetNode> trace = new ArrayList<>();

    public List<TraceTargetNode> getTrace() {
        return trace;
    }

    public void addNode(TraceTargetNode node) {
        this.trace.add(node);
    }

    public ConfigurationData cloneRelativeInstance(Project project) {
        ConfigurationData instance = new ConfigurationData();
        instance.trace.addAll(this.trace);
        for (TraceTargetNode targetNode : instance.trace) {
            targetNode.path = FileHelper.relativizeToConfigurationFile(project, targetNode.path);
            for (TraceChildNode childNode : targetNode.splits) {
                childNode.path = FileHelper.relativizeToConfigurationFile(project, childNode.path);
            }
        }
        return instance;
    }

    public static class TraceTargetNode {
        private String path;
        private String type;
        private List<TraceChildNode> splits = new ArrayList<>();

        public TraceTargetNode(String path, String type) {
            this.path = path;
            this.type = type;
        }

        public List<TraceChildNode> getChildren() {
            return splits;
        }

        public void addChild(TraceChildNode node) {
            splits.add(node);
        }

        public String getPath() {
            return path;
        }

        public String getType() {
            return type;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class TraceChildNode {
        private String path;
        private String type;

        public TraceChildNode(String path, String type) {
            this.path = path;
            this.type = type;
        }

        public String getPath() {
            return path;
        }

        public String getType() {
            return type;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
