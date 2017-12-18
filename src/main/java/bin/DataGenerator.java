package bin;

import classroom.Component;
import classroom.Project;
import classroom.Server;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class DataGenerator {
    private ArrayList<Project> projects = new ArrayList<>();
    private ArrayList<String> ignoreByFileNameListOnUpdate;
    private ArrayList<String> ignoreByFileTypeListOnUpdate;
    private ArrayList<String> ignoreByFileNameListOnRollback;
    private ArrayList<String> ignoreByFileTypeListOnRollback;
    private int monitorVersionInterval;
    private int monitorStateInterval;
    private String loggerStatus;

    public DataGenerator(String configFileName) {
        fromXML(configFileName);
    }

    public ArrayList<Project> getProjects() {
        return projects;
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

    public int getMonitorVersionInterval() {
        return monitorVersionInterval;
    }

    public int getMonitorStateInterval() {
        return monitorStateInterval;
    }

    public String getLoggerStatus() {
        return loggerStatus;
    }

    private void fromXML(String configFileName) { //Загрузка и чтение настроек из xml-файла
        String filePath = new File("").getAbsolutePath(); //Получить путь к текущему каталогу

        File xmlFile = new File(filePath.concat("/" + configFileName)); ///src/main/java/bin/
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        Document doc = null;

        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        try {
            assert dBuilder != null;
            doc = dBuilder.parse(xmlFile);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }

        assert doc != null;
        doc.getDocumentElement().normalize();

        NodeList projectsList = doc.getElementsByTagName("project");
        NodeList configsList = doc.getElementsByTagName("config");

        for (int i = 0; i < projectsList.getLength(); i++) { //Чтение и запись данных по проектам
            Project project = new Project();
            Node nProject = projectsList.item(i);
            NodeList nodeList = nProject.getChildNodes();

            if (nProject.getNodeType() == Node.ELEMENT_NODE) {
                Element eProject = (Element) nProject;
                project.setProjectName(eProject.getAttribute("projectName"));
                project.setServerName(eProject.getAttribute("serverName"));
                project.setSalesDirName(eProject.getAttribute("salesDirName"));
            }

            for (int x = 0; x < nodeList.getLength(); x++) { //Чтение и запись данных по компонентам проекта
                Node nComponent = nodeList.item(x);

                if (nComponent.getNodeType() == Node.ELEMENT_NODE) {
                    if (nComponent.getNodeName().contains("component")) {
                        Component component = new Component();
                        Element eComponent = (Element) nComponent;
                        component.setComponentName(eComponent.getAttribute("componentName"));
                        component.setServiceName(eComponent.getAttribute("serviceName"));
                        component.setLastVersionDirName(eComponent.getAttribute("lastVersionDirName"));
                        component.setComponentDirName(eComponent.getAttribute("componentDirName"));
                        project.setComponents(component);
                    } else if (nComponent.getNodeName().contains("server")) {
                        Server server = new Server();
                        Element eComponent = (Element) nComponent;
                        server.setServerName(eComponent.getAttribute("serverName"));
                        server.setServiceNameQortes(eComponent.getAttribute("serviceNameQortes"));
                        server.setServiceNameQortesDB(eComponent.getAttribute("serviceNameQortesDB"));
                        server.setExeNameQortes(eComponent.getAttribute("exeNameQortes"));
                        server.setExeNameQortesDB(eComponent.getAttribute("exeNameQortesDB"));
                        server.setLastVersionDirName(eComponent.getAttribute("lastVersionDirName"));
                        server.setServerDirName(eComponent.getAttribute("serverDirName"));
                        project.setServers(server);
                    }
                }
            }
            projects.add(project);
        }

        for (int i = 0; i < configsList.getLength(); i++) {
            Node nConfig = configsList.item(i);
            NodeList nodeList = nConfig.getChildNodes();

            for (int x = 0; x < nodeList.getLength(); x++) {
                Node nConfigType = nodeList.item(x);

                if (nConfigType.getNodeType() == Node.ELEMENT_NODE) {
                    if (nConfigType.getNodeName().contains("ignoredCopyFilesOnUpdate")) {
                        Element eConfigType = (Element) nConfigType;
                        ignoreByFileNameListOnUpdate = new ArrayList<>(Arrays.asList(eConfigType.getAttribute("ignoreByFileName").split(",")));
                        ignoreByFileTypeListOnUpdate = new ArrayList<>(Arrays.asList(eConfigType.getAttribute("ignoreByFileType").split(",")));
                    } else if (nConfigType.getNodeName().contains("ignoredCopyFilesOnRollback")) {
                        Element eConfigType = (Element) nConfigType;
                        ignoreByFileNameListOnRollback = new ArrayList<>(Arrays.asList(eConfigType.getAttribute("ignoreByFileName").split(",")));
                        ignoreByFileTypeListOnRollback = new ArrayList<>(Arrays.asList(eConfigType.getAttribute("ignoreByFileType").split(",")));
                    } else if (nConfigType.getNodeName().contains("monitoringInterval")) {
                        Element eConfigType = (Element) nConfigType;
                        monitorVersionInterval = Integer.parseInt(eConfigType.getAttribute("monitorVersion"));
                        monitorStateInterval = Integer.parseInt(eConfigType.getAttribute("monitorState"));
                    } else if (nConfigType.getNodeName().contains("logger")) {
                        Element eConfigType = (Element) nConfigType;
                        loggerStatus = eConfigType.getAttribute("status");
                    }
                }
            }
        }
    }
}
