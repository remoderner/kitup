package utils;

import classroom.Component;
import classroom.Project;
import javafx.scene.control.Button;

public class Threader extends Thread { //Отдельный поток для выполнения действий
    private Project project;
    private Component component;
    private ComponentOperator componentOperator;
    private Button button;

    public Threader(Project project, Component component, ComponentOperator componentOperator, Button button) {
        this.project = project;
        this.component = component;
        this.componentOperator = componentOperator;
        this.button = button;
    }

    @Override
    public void run() {
            componentOperator.updateComponent(project, component);
            button.setStyle(null);
            button.setDisable(false);
    }
}