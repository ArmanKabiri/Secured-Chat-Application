package Application;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import com.chilkatsoft.CkSocket;
import com.chilkatsoft.CkTask;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author arman
 */
public class MainFormController implements Initializable {

    private ReceiverMsgThread receiverThread;

    private CkSocket socket;
    @FXML
    private TextArea txtField_chatBox;
    @FXML
    private Button btn_send;
    @FXML
    private TextField txtField_yourMsg;
    @FXML
    private Button btn_exit;

    public void setSocket(CkSocket socket) {
        this.socket = socket;
        receiverThread = new ReceiverMsgThread(socket, this);
        receiverThread.start();
    }

    @FXML
    private void onclick_btnSend(ActionEvent event) {
        try {
            boolean success = socket.SendString(txtField_yourMsg.getText());
            if (success != true) {
                System.out.println(socket.lastErrorText());
                return;
            }
//            CkTask sendTask = socket.SendStringAsync(txtField_yourMsg.getText() + " -EOF-");
//            sendTask.Run();
            txtField_chatBox.appendText(txtField_yourMsg.getText() + "\n");
            txtField_yourMsg.clear();
        } catch (Exception ex) {
            Logger.getLogger(MainFormController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void onclick_btnExit(ActionEvent event) {
        Platform.setImplicitExit(true);
        Platform.exit();
        System.exit(0);
    }

    public void printReceivedMsg(String msg) {
        txtField_chatBox.appendText(msg + "\n");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
