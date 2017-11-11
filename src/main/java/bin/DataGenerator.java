package bin;

import classroom.Component;
import classroom.Project;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;

class DataGenerator {
    private ArrayList<Project> projects = new ArrayList<>();

    DataGenerator(String configFileName) {
        fromXML(configFileName);
    }

    ArrayList<Project> getProjects() {
        return projects;
    }

    private void fromXML(String configFileName) { //Загрузка и чтение настроек из xml-файла
        String filePath = new File("").getAbsolutePath(); //Получить путь к текущему каталогу

        try {
            File xmlFile = new File(filePath.concat("/" + configFileName)); ///src/main/java/bin/
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            doc.getDocumentElement().normalize();

            NodeList projectList = doc.getElementsByTagName("project");

            for (int i = 0; i < projectList.getLength(); i++) { //Чтение и запись данных по проектам
                Project project = new Project();
                Node nProject = projectList.item(i);
                NodeList componentList = nProject.getChildNodes();

                if (nProject.getNodeType() == Node.ELEMENT_NODE) {
                    Element eProject = (Element) nProject;
                    project.setProjectName(eProject.getAttribute("projectName"));
                    project.setServerName(eProject.getAttribute("serverName"));
                    project.setSalesDirName(eProject.getAttribute("salesDirName"));
                }

                for (int x = 0; x < componentList.getLength(); x++) { //Чтение и запись данных по компонентам проекта
                    Component component = new Component();
                    Node nComponent = componentList.item(x);

                    if (nComponent.getNodeType() == Node.ELEMENT_NODE) {
                        Element eComponent = (Element) nComponent;
                        component.setComponentName(eComponent.getAttribute("componentName"));
                        component.setServiceName(eComponent.getAttribute("serviceName"));
                        component.setLastVersionDirName(eComponent.getAttribute("lastVersionDirName"));
                        component.setComponentDirName(eComponent.getAttribute("componentDirName"));
                        project.setComponents(component);
                    }
                }
                projects.add(project);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
