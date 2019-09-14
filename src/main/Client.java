package main;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import services.Hashing;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

@SuppressWarnings("unused")
public class Client extends Application {

    private static Socket soketZaKomunikaciju = null;
    private static BufferedReader serverInput = null;
    private static PrintStream serverOutput = null;
    private boolean isConnected = false;
    @FXML
    private JFXTextField username; // username sa login screena
    @FXML
    private JFXPasswordField password; // password sa login screena
    @FXML
    private JFXTextField username1; // username sa register screena
    @FXML
    private JFXPasswordField password1; // password sa register screena
    @FXML
    private JFXPasswordField password1R; // ponovljeni password sa register screena
    @FXML
    private JFXTextField prvi; // prvi broj
    @FXML
    private JFXTextField drugi; // drugi broj
    @FXML
    private Label rezultat; // rezultat racunanja

    @Override
    public void init() throws Exception {
        super.init();

        // konektovanje na server
        try {
            soketZaKomunikaciju = new Socket("localhost", 9000);
            serverInput = new BufferedReader(new InputStreamReader(soketZaKomunikaciju.getInputStream()));
            serverOutput = new PrintStream(soketZaKomunikaciju.getOutputStream());
            System.out.println("Server: " + serverInput.readLine());
            isConnected = true;
        } catch (UnknownHostException e){
            System.out.println("Greška: Nije poznat server.");
        } catch (IOException e){
            System.out.println("Greška: Server trenutno ne radi.");
        }
    }

    @Override
    public void start(Stage primaryStage){
        getPrimaryStage();
        showAlertBox();
    }

    @Override
    public void stop() throws Exception {
        super.stop();

        // prekid konekcije
        if(isConnected) { // prekid ako je konektovan
            serverInput.close();
            serverOutput.close();
            soketZaKomunikaciju.close();
            System.out.println("Konekcija je prekinuta.");
            isConnected = false;
        }
    }

    private void getPrimaryStage(){
        Parent root;
        try {
            Stage primaryStage = new Stage();
            root = FXMLLoader.load(getClass().getResource("../view/LoginScreen.fxml"));
            primaryStage.setTitle("Login");
            primaryStage.setScene(new Scene(root));
            primaryStage.setHeight(550);
            primaryStage.setWidth(400);
            primaryStage.setResizable(false);
            primaryStage.getIcons().add(new Image("img/icon.png"));
            primaryStage.show();
        } catch (IOException e) {
            System.out.println("Učitavanje ekrana nije uspelo.");
        }
    } // ucitavanje prvog ekrana

    private void alertBox(String poruka){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Kalkulator");
        alert.setHeaderText(null);
        alert.setContentText(poruka);
        alert.showAndWait();
    }

    private void showAlertBox(){
        if (isConnected){ // ako je konaktovan na server
            alertBox("Došlo je do konekcije!\nMolim Vas ulogujte se.");
        } else { // ako nije konektovan
            alertBox("Nije došlo je do konekcije, molim Vas pokušajte ponovo.");
            System.exit(0);
        }
    } // obavestenje o konekciji pri pokretanju klijenta

    @FXML
    private void registerScene(MouseEvent event){
        try {
            Parent root = FXMLLoader.load(getClass().getResource("../view/RegisterScreen.fxml"));
            Stage primaryStage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            primaryStage.setHeight(550);
            primaryStage.setWidth(400);
            primaryStage.setResizable(false);
            primaryStage.setTitle("Registrujte se");
            primaryStage.getIcons().add(new Image("img/icon.png"));
            primaryStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }

    } // prebacivanje na register screen pri kliku

    @FXML
    private void loginScene(MouseEvent event){
        try {
            Parent root = FXMLLoader.load(getClass().getResource("../view/LoginScreen.fxml"));
            Stage primaryStage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            primaryStage.setHeight(550);
            primaryStage.setWidth(400);
            primaryStage.setResizable(false);
            primaryStage.setTitle("Login");
            primaryStage.getIcons().add(new Image("img/icon.png"));
            primaryStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }

    } // prebacivanje na login screen pri kliku

    @FXML
    private void login(MouseEvent event){
        String username = this.username.getText();
        String hashPass = Hashing.getHash(this.password.getText());
//        String username = "Kuzma";
//        String hashPass = Hashing.getHash("Ukikuzma96");
        if (username.equals("") || hashPass.equals("")){
            alertBox("Molim Vas popunite sva polja.");
            return;
        }
        if (username.equals("Gost")){
            alertBox("Logovanje nije uspelo.\nMolim Vas pokušajte ponovo.");
            password.setText("");
            return;
        }
        String opcija = "1"; // opcija za login je 1
        String odgovor = "";

        serverOutput.println(opcija);
        serverOutput.println(username);
        serverOutput.println(hashPass);

        try {
            odgovor = serverInput.readLine();
        } catch (IOException e) {
            System.out.println("Greška prilikom preuzimanja poruke.");
        }

        if (odgovor.startsWith("1")){ // ako je logovanje moguce
            alertBox("Uspešno ste se ulogovali.");
            setUserScreen(event);
        } else {
            alertBox("Logovanje nije uspelo.\nMolim Vas pokušajte ponovo.");
            password.setText("");
        }
    } // logovanje kao registrovani korisnik

    @FXML
    private void loginGost(MouseEvent event){
        String username = "Gost";
        String hashPass = Hashing.getHash("Gost");
        String opcija = "1"; // opcija za login je 1

        serverOutput.println(opcija);
        serverOutput.println(username);
        serverOutput.println(hashPass);

        try {
            serverInput.readLine();
        } catch (IOException e) {
            System.out.println("Greška prilikom preuzimanja poruke.");
        }

        alertBox("Uspečno ste se ulogovali kao gost.\nImate 3 računanja.");
        setUserScreen(event);
    } // logovanje kao gost

    private void setUserScreen(MouseEvent event) {
        Parent root;
        try {
            Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            root = FXMLLoader.load(getClass().getResource("../view/UserScreen.fxml"));
            primaryStage.setTitle("Kalkulator");
            primaryStage.setScene(new Scene(root));
            primaryStage.setHeight(550);
            primaryStage.setWidth(400);
            primaryStage.setResizable(false);
            primaryStage.getIcons().add(new Image("img/icon.png"));
            primaryStage.show();
        } catch (IOException e) {
            System.out.println("Učitavanje ekrana nije uspelo.");
        }
    } // prebacivanje na ekran kalkulatora

    @FXML
    private void register(MouseEvent event){
        String username = this.username1.getText();
        String pass = this.password1.getText();
        String pass1 = this.password1R.getText();
        if (username.equals("") || pass.equals("") || pass1.equals("")){
            alertBox("Molim Vas popunite sva polja.");
            return;
        }
        if (username.equals("Gost")){
            alertBox("Korisničko ime ne može biti 'Gost'.");
            return;
        }
        if(!proveraSifre(pass)){
            alertBox("Šifra se mora sastojati iz minimum 8 karaktera, " +
                    "minimum jednog velikog slova (A-Z) i minimum jednog broja (0-9).");
            return;
        }
        if (!pass.equals(pass1)){
            alertBox("Unete šifre se ne podudaraju.");
            return;
        }

        String hashPass = Hashing.getHash(pass);
        String opcija = "0"; // opcija za registrovanje je 0
        String odgovor = "";

        serverOutput.println(opcija);
        serverOutput.println(username);
        serverOutput.println(hashPass);

        try {
            odgovor = serverInput.readLine();
        } catch (IOException e) {
            System.out.println("Greška prilikom preuzimanja poruke.");
        }

        if (odgovor.startsWith("1")){ // ako je logovanje moguce
            alertBox("Uspešno ste se registrovali.");
            loginScene(event);
        } else {
            alertBox("Korisničko ime je zauzeto.");
        }
    } // registrovanje korisnika

    private boolean proveraSifre(String pass){
        int brojacSlova = 0;
        int brojacBrojeva = 0;
        if (pass.length() < 8)
            return false;
        for (int i = 0; i < pass.length(); i++){
            if ((int)pass.charAt(i) >= 65 && (int)pass.charAt(i) <= 90) // da li sadrzi veliko slovo
                brojacSlova++;
            if ((int)pass.charAt(i) >= 48 && (int)pass.charAt(i) <= 57) // da li sadrzi broj
                brojacBrojeva++;
        }
        return (brojacSlova > 0 && brojacBrojeva > 0);
    } // proverava da li sifra ispunjava uslove

    @FXML
    private void saberi(MouseEvent event){
        String prvi = this.prvi.getText();
        String drugi = this.drugi.getText();
        String rez;
        if (prvi.equals("") || drugi.equals("")){
            alertBox("Molim Vas popunite oba polja.");
            return;
        } if (!isNumber(prvi) || !isNumber(drugi)){
            alertBox("Molim Vas unesite samo brojeve");
            return;
        }
        try {
            float provera1 = Float.parseFloat(prvi);
            float provera2 = Float.parseFloat(drugi);
        } catch (NumberFormatException e){
            alertBox("Brojevi su izvan opsega.");
            return;
        }
        serverOutput.println("1");
        serverOutput.println(prvi + " + " + drugi);
        try {
            rez = serverInput.readLine();
        } catch (IOException e) {
            System.out.println("Greška pri preuzimanju poruke");
            return;
        }
        proveraRez(rez);
        this.rezultat.setText(rez);
    } // sabira dva broja

    @FXML
    private void oduzmi(MouseEvent event){
        String prvi = this.prvi.getText();
        String drugi = this.drugi.getText();
        String rez;
        if (prvi.equals("") || drugi.equals("")){
            alertBox("Molim Vas popunite oba polja.");
            return;
        } if (!isNumber(prvi) && !isNumber(drugi)){
            alertBox("Molim Vas unesite samo brojeve");
            return;
        }
        try {
            float provera1 = Float.parseFloat(prvi);
            float provera2 = Float.parseFloat(drugi);
            float provera3 = provera1 - provera2;
        } catch (NumberFormatException e){
            alertBox("Brojevi su izvan opsega.");
            return;
        }
        serverOutput.println("1");
        serverOutput.println(prvi + " - " + drugi);
        try {
            rez = serverInput.readLine();
        } catch (IOException e) {
            System.out.println("Greška pri preuzimanju poruke");
            return;
        }
        proveraRez(rez);
        this.rezultat.setText(rez);
    } // oduzima dva broja

    @FXML
    private void pomnozi(MouseEvent event){
        String prvi = this.prvi.getText();
        String drugi = this.drugi.getText();
        String rez;
        if (prvi.equals("") || drugi.equals("")){
            alertBox("Molim Vas popunite oba polja.");
            return;
        } if (!isNumber(prvi) && !isNumber(drugi)){
            alertBox("Molim Vas unesite samo brojeve");
            return;
        }
        try {
            float provera1 = Float.parseFloat(prvi);
            float provera2 = Float.parseFloat(drugi);
            float provera3 = provera1 * provera2;
        } catch (NumberFormatException e){
            alertBox("Brojevi su izvan opsega.");
            return;
        }
        serverOutput.println("1");
        serverOutput.println(prvi + " * " + drugi);
        try {
            rez = serverInput.readLine();
        } catch (IOException e) {
            System.out.println("Greška pri preuzimanju poruke");
            return;
        }
        proveraRez(rez);
        this.rezultat.setText(rez);
    } // mnozi dva broja

    @FXML
    private void podeli(MouseEvent event){
        String prvi = this.prvi.getText();
        String drugi = this.drugi.getText();
        String rez;
        if (prvi.equals("") || drugi.equals("")){
            alertBox("Molim Vas popunite oba polja.");
            return;
        } if (!isNumber(prvi) && !isNumber(drugi)){
            alertBox("Molim Vas unesite samo brojeve");
            return;
        } if (drugi.equals("0")){
            alertBox("Delilac ne sme biti 0!");
            return;
        }
        try {
            float provera1 = Float.parseFloat(prvi);
            float provera2 = Float.parseFloat(drugi);
            float provera3 = provera1 / provera2;
        } catch (NumberFormatException e){
            alertBox("Brojevi su izvan opsega.");
            return;
        }
        serverOutput.println("1");
        serverOutput.println(prvi + " / " + drugi);
        try {
            rez = serverInput.readLine();
        } catch (IOException e) {
            System.out.println("Greška pri preuzimanju poruke");
            return;
        }
        proveraRez(rez);
        this.rezultat.setText(rez);
    } // deli dva broja

    private void proveraRez(String rez){
        if (rez.equals("Registrujte se")){
            alertBox("Molim Vas registrujte se kako biste nastavili sa računanjem.");
        } else if (rez.equals("exception")){
            alertBox("Brojevi su izvan opsega");
        }
    } // proverava odgovor servera

    @FXML
    private void sacuvaj(ActionEvent event){
        serverOutput.println("2");
        String odgovor = "";
        try {
            odgovor = serverInput.readLine();
        } catch (IOException e) {
            System.out.println("Greška pri preuzimanju poruke");
        }
        if (odgovor.equals("gost")){
            alertBox("Registrujte se kako biste mogli da čuvate listu računanja.");
        } else if (odgovor.equals("1")) {
            primiFajl();
        }
    } // trazi serveru listu racunanja iz baze

    private void primiFajl(){
        int bytesRead;
        int current = 0;
        FileOutputStream fileOutputStream;
        BufferedOutputStream bufferedOutputStream;
        int velicina;
        String username;
        try {
            velicina = Integer.parseInt(serverInput.readLine());
            username = serverInput.readLine();
        } catch (IOException e) {
            System.out.println("Greska prilikom prijema");
            return;
        }
        String naziv = "racunanje-" + username + ".txt";
        byte[] bajtovi = new byte[velicina];
        try {
            InputStream inputStream = soketZaKomunikaciju.getInputStream();
            fileOutputStream = new FileOutputStream(naziv);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            inputStream.read(bajtovi, 0, velicina);
            bufferedOutputStream.write(bajtovi, 0, velicina);
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
            fileOutputStream.close();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(naziv)));
            String ispis = "";
            String linija;
            while ((linija = bufferedReader.readLine()) != null){
                ispis += linija + "\n";
            }
            bufferedReader.close();
            alertBox(ispis);
        } catch (IOException e) {
            System.out.println("Greska prilikom primanja fajla");
        }
    } // prima fajl od servera

    @FXML
    private void izlogujSe(MouseEvent event){
        serverOutput.println("3");
        alertBox("Izlogovali ste se sa ovog korisničkog imena");
        loginScene(event);
    }

    private static boolean isNumber(String text){
        for (int i = 0; i < text.length(); i++){
            if ((int)text.charAt(i) > 48 || (int)text.charAt(i) < 57)
                return true;
        }
        return false;
    } // proverava da li je string broj

    @FXML
    private void exit(ActionEvent event){
        serverOutput.println("0");
        try {
            stop();
            Stage stage = (Stage) prvi.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            System.out.println("Greska prilikom prekidanja konekcije.");
        }
    } // izlazak iz aplikacije

    public static void main(String[] args) {
        launch(args);
    }

}
