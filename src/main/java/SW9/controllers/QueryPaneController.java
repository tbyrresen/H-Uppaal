package SW9.controllers;

import SW9.NewMain;
import SW9.abstractions.Query;
import SW9.abstractions.QueryState;
import SW9.presentations.QueryPresentation;
import SW9.utility.helpers.DropShadowHelper;
import com.jfoenix.controls.JFXButton;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class QueryPaneController implements Initializable {

    public Label toolbarTitle;
    public JFXButton addQueryButton;
    public AnchorPane toolbar;
    public JFXButton runAllQueriesButton;
    public JFXButton clearAllQueriesButton;
    public VBox queriesList;
    public StackPane root;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        // We need to register these event manually this way because JFXButton overrides onPressed and onRelease to handle rippler effect
        addQueryButton.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> addQueryButtonPressed());
        addQueryButton.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> addQueryButtonReleased());

        NewMain.getProject().getQueries().addListener(new ListChangeListener<Query>() {
            @Override
            public void onChanged(final Change<? extends Query> c) {
                while (c.next()) {
                    for (final Query removeQuery : c.getRemoved()) {
                        queriesList.getChildren().remove(removeQuery);
                    }

                    for (final Query newQuery : c.getAddedSubList()) {
                        queriesList.getChildren().add(new QueryPresentation(newQuery));
                    }
                }
            }
        });
    }

    @FXML
    private void addQueryButtonClicked() {
        NewMain.getProject().getQueries().add(new Query("A[] not deadlock", "The model does not contain a deadlock", QueryState.UNKNOWN));
    }

    @FXML
    private void addQueryButtonPressed() {
        addQueryButton.setEffect(DropShadowHelper.generateElevationShadow(12));
    }

    @FXML
    private void addQueryButtonReleased() {
        addQueryButton.setEffect(DropShadowHelper.generateElevationShadow(6));
    }
}
