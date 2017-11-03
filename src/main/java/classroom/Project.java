package classroom;

import java.util.ArrayList;

public class Project {
    private String projectName;
    private String serverName;
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

    public ArrayList<Component> getComponents() {
        return components;
    }

    public void setComponents(Component component) {
        this.components.add(component);
    }
}
