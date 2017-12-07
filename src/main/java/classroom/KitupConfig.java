package classroom;

import bin.DataGenerator;

import java.util.ArrayList;

public class KitupConfig {
    private static volatile KitupConfig kitupConfig;
    private DataGenerator dataGenerator = new DataGenerator("Config.xml");
    private ArrayList<Project> projects = dataGenerator.getProjects();
    private ArrayList<String> ignoreByFileNameListOnUpdate = dataGenerator.getIgnoreByFileNameListOnUpdate();
    private ArrayList<String> ignoreByFileTypeListOnUpdate = dataGenerator.getIgnoreByFileTypeListOnUpdate();
    private ArrayList<String> ignoreByFileNameListOnRollback = dataGenerator.getIgnoreByFileTypeListOnRollback();
    private ArrayList<String> ignoreByFileTypeListOnRollback = dataGenerator.getIgnoreByFileTypeListOnRollback();

    public static KitupConfig getKitupConfig() {
        if (kitupConfig == null) {
            synchronized (KitupConfig.class) {
                if (kitupConfig == null) {
                    kitupConfig = new KitupConfig();
                }
            }
        }
        return kitupConfig;
    }

    public ArrayList<String> getIgnoreByFileNameListOnUpdate() {
        return ignoreByFileNameListOnUpdate;
    }

    public ArrayList<String> getIgnoreByFileTypeListOnUpdate() {
        return ignoreByFileTypeListOnUpdate;
    }

    public ArrayList<String> getIgnoreByFileNameListOnRollback() {
        return ignoreByFileNameListOnRollback;
    }

    public ArrayList<String> getIgnoreByFileTypeListOnRollback() {
        return ignoreByFileTypeListOnRollback;
    }

    public ArrayList<Project> getProjects() {
        return projects;
    }
}
