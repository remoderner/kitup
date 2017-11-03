package classroom;

public class Component {
    private String componentName;
    private String serviceName;
    private String sourceDirName;
    private String targetSourceDir;

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getSourceDirName() {
        return sourceDirName;
    }

    public void setSourceDirName(String sourceDirName) {
        this.sourceDirName = sourceDirName;
    }

    public String getTargetSourceDir() {
        return targetSourceDir;
    }

    public void setTargetSourceDir(String targetSourceDir) {
        this.targetSourceDir = targetSourceDir;
    }
}

