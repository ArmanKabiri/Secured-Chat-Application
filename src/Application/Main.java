package Application;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author arman
 */
public class Main extends Application {

    static {
//        try {
//            String s=System.getProperty("user.dir")+ "/nativeLib/";
//            System.out.println(s);
//            String directory = new File(".").getCanonicalPath() + "/nativeLib/";
//            System.load(s + "libchilkat.jnilib");
//        } catch (UnsatisfiedLinkError e) {
//            System.err.println("Native code library failed to load.\n" + e);
//            System.exit(1);
//        } catch (IOException ex) {
//            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//            System.exit(1);
//        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("LoginForm.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            String libPath = (args.length > 0) ? args[0] : System.getProperty("user.dir") + "/nativeLib/mac/libchilkat.jnilib";
            System.load(libPath);
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native code library failed to load.\n" + e);
            System.exit(1);
        }
        launch(args);
    }
}
