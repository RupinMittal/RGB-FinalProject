import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * Abstract class Menu will have features that will be used in the other menus (i.e. Main Menu and Pause Menu)
 *
 * @author Jonathan Lee
 * @version 28 May 2019
 */
public abstract class Menu extends Application
{
    private Button quit;
    private Button volume;
    
    public abstract void runMenu();
    
    private void options()
    {
        
    }
    
    private void credits()
    {
        
    }
}
