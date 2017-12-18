package classroom;

import bin.DataGenerator;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.ArrayList;

public class KitupConfig {
    private static volatile KitupConfig kitupConfig;
    private DataGenerator dataGenerator = new DataGenerator("Config.xml");
    private ArrayList<Project> projects = dataGenerator.getProjects();
    private ArrayList<String> ignoreByFileNameListOnUpdate = dataGenerator.getIgnoreByFileNameListOnUpdate();
    private ArrayList<String> ignoreByFileTypeListOnUpdate = dataGenerator.getIgnoreByFileTypeListOnUpdate();
    private ArrayList<String> ignoreByFileNameListOnRollback = dataGenerator.getIgnoreByFileNameListOnRollback();
    private ArrayList<String> ignoreByFileTypeListOnRollback = dataGenerator.getIgnoreByFileTypeListOnRollback();
    private int monitorVersionInterval = dataGenerator.getMonitorVersionInterval();
    private int monitorStateInterval = dataGenerator.getMonitorStateInterval();

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

    private KitupConfig() {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.toLevel(dataGenerator.getLoggerStatus()));
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

    public int getMonitorVersionInterval() {
        return monitorVersionInterval;
    }

    public int getMonitorStateInterval() {
        return monitorStateInterval;
    }

}
