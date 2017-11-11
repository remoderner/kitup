package classroom;

import java.util.ArrayList;

public class Project {
    private String projectName;
    private String serverName;
    private String salesDirName;
    private ArrayList<Component> components = new ArrayList<>();

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getSalesDirName() {
        return salesDirName;
    }

    public void setSalesDirName(String pathPastVersion) {
        this.salesDirName = pathPastVersion;
    }

    public ArrayList<Component> getComponents() {
        return components;
    }

    public void setComponents(Component component) {
        this.components.add(component);
    }
}
