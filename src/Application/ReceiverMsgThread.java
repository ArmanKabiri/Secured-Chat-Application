package Application;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import com.chilkatsoft.CkSocket;
import com.chilkatsoft.CkTask;
import java.io.BufferedReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.Initializable;

/**
 *
 * @author arman
 */
public class ReceiverMsgThread extends Thread {

    CkSocket socket;
    Initializable uiControler;

    public ReceiverMsgThread(CkSocket socket, Initializable ui) {
        uiControler = ui;
        this.socket = socket;
    }

    @Override
    public void run() {
        BufferedReader bufferReader = null;
        try {
            while (true) {
//                String receivedMsg = socket.receiveString();
                CkTask receiveTask = socket.ReceiveStringAsync();
                receiveTask.Run();
                receiveTask.SleepMs(300);
                String receivedMsg = receiveTask.getResultString();

//                if (socket.get_LastMethodSuccess() != true) {
//                    System.out.println(socket.lastErrorText());
//                    return;
//                }
                if (receivedMsg != null && !"".equals(receivedMsg)) {
                    Platform.runLater(() -> {
                        ((MainFormController) uiControler).printReceivedMsg(receivedMsg);
                    });
                } else {
                    receiveTask.Cancel();
                }

            }
        } catch (Exception ex) {
            System.out.println("Connection Closed");
        } finally {
            try {
                socket.Close(20000);
            } catch (Exception ex) {
                Logger.getLogger(ReceiverMsgThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
