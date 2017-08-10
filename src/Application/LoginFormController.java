package Application;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import com.chilkatsoft.CkCert;
import com.chilkatsoft.CkSocket;
import com.chilkatsoft.CkTrustedRoots;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author arman
 */
public class LoginFormController implements Initializable {

    private String certificateFilePath; //versatile use for either server or client (pfx/pem)

    @FXML
    private Label label;
    @FXML
    private Button btn_connect;
    @FXML
    private TextField txtField_ip;
    @FXML
    private TextField txtField_port_Connect;
    @FXML
    private TextField txtField_port_Wait;
    @FXML
    private Button btn_wait;
    @FXML
    private Button btn_loadTrustedRoots;
    @FXML
    private Button btn_loadServerCertificate;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    private void onClick_btnConnect(ActionEvent event) {
        try {
            boolean useSsl = (certificateFilePath == null) ? false : true;
            loadTrustedCertificates();
            CkSocket socket = new CkSocket();
            boolean success;

            success = socket.UnlockComponent("Anything for 30-day trial");
            if (success != true) {
                System.out.println(socket.lastErrorText());
                return;
            }

            int maxWaitMillisec = 10000;
            success = socket.Connect(txtField_ip.getText(), Integer.parseInt(txtField_port_Connect.getText()), useSsl, maxWaitMillisec);
            if (success != true) {
                System.out.println(socket.lastErrorText());
                return;
            }
            if (useSsl) {
                CkCert cert = socket.GetSslServerCert();
                success = validateServerCertificate(cert);
                if (!success) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("UnTrusted");
                    alert.setHeaderText("Server Is Untrusted");
                    alert.setContentText("connection is refused");
                    alert.show();
                    socket.Close(2000);
                } else {
                    goToMainForm(socket);
                }
            } else {
                goToMainForm(socket);
            }

        } catch (Exception ex) {
            Logger.getLogger(LoginFormController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void onClick_btnWait(ActionEvent event) {
        try {
            boolean useSsl = (certificateFilePath == null) ? false : true;
            CkSocket listenSslSocket = new CkSocket();
            boolean success;
            success = listenSslSocket.UnlockComponent("Anything for 30-day trial");
            if (success != true) {
                System.out.println(listenSslSocket.lastErrorText());
                return;
            }

            if (useSsl) {
                //  Load the certificate to be used for SSL:
                CkCert cert = new CkCert();
                String password = "arman";
                cert.LoadPfxFile(certificateFilePath, password);

                //  Use the certificate:
                success = listenSslSocket.InitSslServer(cert);
                if (success != true) {
                    System.out.println(listenSslSocket.lastErrorText());
                    return;
                }
            }

            int backLog = 5;
            listenSslSocket.put_MaxReadIdleMs(10000);
            listenSslSocket.put_MaxSendIdleMs(10000);
            int maxWaitMillisec = 100000;

            success = listenSslSocket.BindAndListen(Integer.parseInt(txtField_port_Wait.getText()), backLog);
            if (success != true) {
                System.out.println(listenSslSocket.lastErrorText());
                return;
            }

            CkSocket clientSock = listenSslSocket.AcceptNextConnection(maxWaitMillisec);
            if (clientSock == null) {
                System.out.println(listenSslSocket.lastErrorText());
                return;
            }
            goToMainForm(clientSock);

        } catch (Exception ex) {
            Logger.getLogger(LoginFormController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void goToMainForm(CkSocket socket) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MainForm.fxml"));
            Parent root = (Parent) fxmlLoader.load();
            MainFormController controller = fxmlLoader.getController();
            controller.setSocket(socket);
            Stage stage = new Stage();
            stage.setTitle("Chat Room");
            stage.setScene(new Scene(root));
            stage.show();
            Stage currentStage = (Stage) btn_connect.getScene().getWindow();
            currentStage.close();
        } catch (IOException ex) {
            Logger.getLogger(LoginFormController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadTrustedCertificates() {
        CkTrustedRoots troots = new CkTrustedRoots();
        boolean success = troots.LoadCaCertsPem(certificateFilePath);
        if (success != true) {
            System.out.println(troots.lastErrorText());
            return;
        }

        //  Activate this specific set of trusted roots.
        success = troots.Activate();
        if (success != true) {
            System.out.println(troots.lastErrorText());
        }
    }

    private boolean validateServerCertificate(CkCert cert) {
        boolean bExpired = false;
        boolean bRevoked = false;
        boolean bSignatureVerified = false;
        boolean bTrustedRoot = false;
        if (!(cert == null)) {

            System.out.println("Server Certificate:");
            System.out.println("Distinguished Name: " + cert.subjectDN());
            System.out.println("Common Name: " + cert.subjectCN());
            System.out.println("Issuer Distinguished Name: " + cert.issuerDN());
            System.out.println("Issuer Common Name: " + cert.issuerCN());

            bExpired = cert.get_Expired();
            bRevoked = cert.get_Revoked();
            bSignatureVerified = cert.get_SignatureVerified();
            bTrustedRoot = cert.get_TrustedRoot();

            System.out.println("Expired: " + bExpired);
            System.out.println("Revoked: " + bRevoked);
            System.out.println("Signature Verified: " + bSignatureVerified);
            System.out.println("Trusted Root: " + bTrustedRoot);
        }
        return (bTrustedRoot && !bExpired && bSignatureVerified);
    }

    @FXML
    private void onClick_loadTrustedRoots(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Pem File", "*.pem");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(null);
        certificateFilePath = file.getPath();
    }

    @FXML
    private void onClick_loadServerCertificate(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Personal Exchange File", "*.pfx");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(null);
        certificateFilePath = file.getPath();
    }
}
