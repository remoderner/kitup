package classroom;

public class Component {
    private String componentName;
    private String serviceName;
    private String lastVersionDirName;
    private String componentDirName;

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

    public String getLastVersionDirName() {
        return lastVersionDirName;
    }

    public void setLastVersionDirName(String sourceDirName) {
        this.lastVersionDirName = sourceDirName;
    }

    public String getComponentDirName() {
        return componentDirName;
    }

    public void setComponentDirName(String targetSourceDir) {
        this.componentDirName = targetSourceDir;
    }

}

