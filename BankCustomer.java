/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

 package com.mycompany.bankingapplication;
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 //PDF generation imports
 import com.itextpdf.text.Document;  
 import com.itextpdf.text.DocumentException;  
 import com.itextpdf.text.Paragraph;
 import com.itextpdf.text.pdf.PdfWriter;
 import com.itextpdf.text.DocumentException;  
 //File handling imports
 import java.io.FileOutputStream;
 import java.io.FileNotFoundException;  
 import java.io.ObjectOutputStream;
 
 /**
  *
  * @author Rohan Bhalla and Mirna Ashour
  */
 public class BankCustomer {
     static Socket s;
     static BufferedReader input;
     static PrintWriter output;
         
     public static void main(String[] args) {
         //Put the socket stuff in a class/function and organize it better
         try {
              s = new Socket("localhost", 5050);
             input = new BufferedReader(new InputStreamReader(s.getInputStream()));
             output = new PrintWriter(s.getOutputStream(), true);

         }
         catch (IOException ex) {
             System.out.println("Socket Problem");
         }
        StartPage start = new StartPage();
     }
 
 }
 
 
 //The ResponsesThread helps in being able to receive messages back from the server 
 class ReponsesThread extends Thread {
     BufferedReader input;
     static String gotthisback;
     ReponsesThread(BufferedReader inputParam){
         input = inputParam;
     }
     
     @Override
         public void run() {
             try {
                 // Read incoming messages from the server and print them to the console
                 String message;
                 while ((message = input.readLine()) != null) {
                     gotthisback = message;
                     System.out.println(message);
                 }
             } catch (Exception e) {
                 System.err.println("Error receiving message: " + e.getMessage());
             }
         }
 }
 
 
 class StartPage extends JFrame implements ActionListener {
     private JTextField usernameField;
     private JPasswordField passwordField;
     public StartPage() {
         setTitle("Customer Login");
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setVisible(true);
         setSize(450, 500);
 
         // Main panel
         JPanel mainPanel = new JPanel();
         mainPanel.setLayout(new BorderLayout());
 
         // Panel for the error message
         JPanel errorPanel = new JPanel();
         JLabel errorMessage = new JLabel("");
         errorPanel.add(errorMessage);
 
         // Panel for all the input elements 
         JPanel inputPanel = new JPanel();
         inputPanel.setLayout(new GridLayout(2, 2, 15, 15));
         JLabel usernameLabel = new JLabel("Username:");
         usernameField = new JTextField(20);
         JLabel passwordLabel = new JLabel("Password:");
         passwordField = new JPasswordField(20);
         inputPanel.add(usernameLabel);
         inputPanel.add(usernameField);
         inputPanel.add(passwordLabel);
         inputPanel.add(passwordField);
 
         // Panel for the register button
         JPanel buttonPanel = new JPanel();
         JButton registerButton = new JButton("Register");
         buttonPanel.add(registerButton);
 
         registerButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 setVisible(false);
                 new RegistrationPage();
             }
 
          });
 
 
         // Panel for the login button
         JButton loginButton = new JButton("Login");
         buttonPanel.add(loginButton);
 
         loginButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 String authCheck = "";
                 String username = usernameField.getText();
                 String password = new String(passwordField.getPassword());
                 
                 if(!username.isEmpty() && !password.isEmpty()) {   // checks to see if the fields are all entered
                     
                     // Communicates with the server to send and receive login information/authentication 
                     BankCustomer.output.println("login");
                     BankCustomer.output.println(username);
                     BankCustomer.output.println(password);
                     try{
                         authCheck = BankCustomer.input.readLine();
                     }
                     catch(Exception ex){
                         System.out.println("Authentication comm failed.");
                     }
                     // checks the response from the server for errors 
                     System.out.println("RECEIVED: " + authCheck);
                     if(authCheck.equals("200")){
                         setVisible(false);
                         //Login success will send user to menu page
                         new MenuPage();
                     }
                     else {
                         errorMessage.setText("Invalid username or password.");
                     }
                 }
                 else {     // error for not filling out all fields 
                     errorMessage.setText("Please fill out all the fields.");
                     BankCustomer.output.println("failed");
                 }
             }
         });
 
         mainPanel.add(errorPanel, BorderLayout.NORTH);
         mainPanel.add(inputPanel, BorderLayout.CENTER);
         mainPanel.add(buttonPanel, BorderLayout.SOUTH);
         add(mainPanel);
         pack();
         setLocationRelativeTo(null); // Center the window on the screen
     }
 
     public void actionPerformed(ActionEvent e) {
         
     }
    
 
 }
 
 class RegistrationPage extends JFrame implements ActionListener {
 
     private JLabel firstNameLabel, lastNameLabel, usernameLabel, passwordLabel;
     private JTextField firstNameField, lastNameField, usernameField, passwordField;
     private JButton submitButton;
     private JLabel errorMessage;
     private JPanel errorPanel;
 
     public RegistrationPage() {
         
         // configure UI design for the registration frame 
         setTitle("Customer Registration");
         firstNameLabel = new JLabel("First Name:");
         lastNameLabel = new JLabel("Last Name:");
         usernameLabel = new JLabel("Username:");
         passwordLabel = new JLabel("Password:");
 
         firstNameField = new JTextField();
         lastNameField = new JTextField();
         usernameField = new JTextField();
         passwordField = new JPasswordField();
         
         errorPanel = new JPanel();
         errorMessage = new JLabel("");
         errorPanel.add(errorMessage);
         errorMessage.setVisible(false);
 
         submitButton = new JButton("Register");
 
         JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
         formPanel.add(firstNameLabel);
         formPanel.add(firstNameField);
         formPanel.add(lastNameLabel);
         formPanel.add(lastNameField);
         formPanel.add(usernameLabel);
         formPanel.add(usernameField);
         formPanel.add(passwordLabel);
         formPanel.add(passwordField);
 
         JPanel buttonPanel = new JPanel();
         buttonPanel.add(submitButton);
 
         //Registration button action listener
         submitButton.addActionListener(new ActionListener(){
             @Override
             public void actionPerformed(ActionEvent e) {
                 // get input from the input fields 
                 String username = usernameField.getText();
                 String password = passwordField.getText();
                 String firstName = firstNameField.getText();
                 String lastName = lastNameField.getText();
                 String authCheck = "";
                 
                 // checks if any of the field area are empty 
                 if(!username.isEmpty() && !password.isEmpty() && !firstName.isEmpty() && !lastName.isEmpty() ) {
                         // send input values to server
                         BankCustomer.output.println("register");
                         BankCustomer.output.println(firstName);
                         BankCustomer.output.println(lastName);
                         BankCustomer.output.println(username);
                         BankCustomer.output.println(password);
                         try{
                             authCheck = BankCustomer.input.readLine();
                         }
                         catch(Exception ex){
                             System.out.println("Authentication comm failed.");
                         }
                         System.out.println("REGISTRATION CODE: " + authCheck);     // receive server response and check for errors to be printed
                         if(authCheck.equals("200")){
                             setVisible(false);
                             new StartPage();
                         }
                         else {     // checks if the registration already exists in the DB
                             errorMessage.setText("User already exists.");
                             errorMessage.setVisible(true);
                         }
                         
                 }
                 else{      // error for empty input fields 
                         errorMessage.setText("Please fill out all the fields.");
                         errorMessage.setVisible(true);
                 }
             }
         });
 
         // add elements to main panel 
         JPanel mainPanel = new JPanel(new BorderLayout());
         mainPanel.add(errorPanel, BorderLayout.NORTH);
         mainPanel.add(formPanel, BorderLayout.CENTER);
         mainPanel.add(buttonPanel, BorderLayout.SOUTH);
 
         add(mainPanel);
 
         setSize(400, 300);
         setLocationRelativeTo(null);
         setResizable(true);
         setVisible(true);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         pack();
 
     }
     
 
     public void actionPerformed(ActionEvent e) {
         
         
     }
 
 }
 
 
 class MenuPage extends JFrame implements ActionListener {
     
     private JButton depositButton;
     private JButton withdrawButton;
     private JButton transferButton;
     private JButton reportButton;
 
     public MenuPage() {
 
         // configure UI for customer menu
         setTitle("Main Menu");
         setSize(400, 400);
         setLocationRelativeTo(null);
         setResizable(true);
         setVisible(true);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         JPanel mainPanel = new JPanel(new BorderLayout());
         
         depositButton = new JButton("Deposit");
         depositButton.addActionListener(this);
         withdrawButton = new JButton("Withdraw");
         withdrawButton.addActionListener(this);
         transferButton = new JButton("Transfer");
         transferButton.addActionListener(this);
         reportButton = new JButton("Report");
         
        // action listener for the report button 
        reportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Report
                BankCustomer.output.println("report");
                new GeneratePDF();
            }
        });
         
         JPanel formPanel = new JPanel(new GridLayout(2, 2, 25, 25));
 
        //Receive balance from server to display
        JLabel balance = new JLabel();
        JPanel balanceP = new JPanel();
        balanceP.add(balance);
        try{
            BankCustomer.output.println("balance");
            String getBalance = BankCustomer.input.readLine();
            balance.setText("Balance: $" + getBalance);

        }catch(Exception ex){
            balance.setText("Error in getting balance");
        }
         
         // add buttons to menu  
         formPanel.add(depositButton);
         formPanel.add(withdrawButton);
         formPanel.add(transferButton);
         formPanel.add(reportButton);
         
         // add form elements to main panel 
         mainPanel.add(balanceP, BorderLayout.NORTH);
         mainPanel.add(formPanel, BorderLayout.CENTER);
 
         add(mainPanel);
 
     }
     
     public void actionPerformed(ActionEvent e) {
         
         // check which button was pressed in the menu and view the corresponding form for the option
         JButton activeButton = (JButton)e.getSource();
         String option = activeButton.getText();
         setVisible(false);
 
         switch (option) {
             case "Deposit":
                 new DepositForm();
                 break;
             case "Withdraw":
                 new WithdrawForm();
                 break;
             case "Transfer":
                 new TransferForm();
                 break;
             default:
                new GeneratePDF();
                setVisible(true);
                 break;
         }
     }
 
 }
 
 
 class WithdrawForm extends JFrame implements ActionListener {
 
     private JTextField amount;
     private JLabel amountLabel;
     private JButton withdraw;
     private JButton back;
     private JPanel mainPanel;
     //Error handling
     private JLabel errorMessage;
     private JPanel errorPanel;
 
     public WithdrawForm() {
 
         // configure UI for the withdrawal form 
         setTitle("Withdrawal Form");
         setSize(300, 140);
         setLocationRelativeTo(null);
         setResizable(true);
         setVisible(true);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 
         mainPanel = new JPanel(new BorderLayout());
         add(mainPanel);
 
         amount = new JTextField();
         amountLabel = new JLabel("Amount: ");
         JPanel input = new JPanel(new GridLayout(1, 2, 15, 15));
         input.add(amountLabel);
         input.add(amount);
         mainPanel.add(input, BorderLayout.CENTER);
 
         JPanel buttons = new JPanel();
         withdraw = new JButton("Withdraw");
         withdraw.addActionListener(this);
         back = new JButton("Back");
         back.addActionListener(this);
         buttons.add(withdraw);
         buttons.add(back);
         
         errorPanel = new JPanel();
         errorMessage = new JLabel("");
         errorPanel.add(errorMessage);
         
         // set up the elements in the panel
         mainPanel.add(buttons, BorderLayout.SOUTH);
         mainPanel.add(errorPanel, BorderLayout.NORTH);
 
     }
 
     public void actionPerformed(ActionEvent e) {
         JButton activeButton = (JButton)e.getSource();
         String option = activeButton.getText();
         
         if(option.equals("Back")) {        // checks if the back button was pressed. displays main menu again if pressed
             setVisible(false);
             new MenuPage();
         }
         else{
             
             // inputted text 
             String amountSend = amount.getText();
             String withdrawCheck = "";
         
             // checks if the input is empty 
             if(!amountSend.isEmpty()) {
                 // server communitcation and error handling 
                 BankCustomer.output.println("withdraw");
                 BankCustomer.output.println(amountSend);
                 try{
                     withdrawCheck = BankCustomer.input.readLine();
                 }
                 catch(Exception ex){
                     System.out.println("Authentication comm failed.");
                 }
                 if(withdrawCheck.equals("200")){
                     setVisible(false);
                    //  new StartPage();
                     new MenuPage();
                 }
                 else {
                     errorMessage.setText(withdrawCheck);
                 }
                 
             }
             else{      // error for empty input
                 errorMessage.setText("Please fill out all the fields.");
             }
         }
         
         
     }
 
 }
 
 
 class DepositForm extends JFrame implements ActionListener {
     
     private JTextField amount;
     private JLabel amountLabel;
     private JButton deposit;
     private JButton back;
     private JPanel mainPanel;
     //Error handling code
     private JLabel errorMessage;
     private JPanel errorPanel;
 
     public DepositForm() {
         
         // config deposit form UI
         setTitle("Deposit Form");
         setSize(300, 120);
         setLocationRelativeTo(null);
         setResizable(true);
         setVisible(true);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         mainPanel = new JPanel(new BorderLayout());
         add(mainPanel);
         amount = new JTextField();
         amountLabel = new JLabel("Amount: ");
         JPanel input = new JPanel(new GridLayout(1, 2, 15, 15));
         input.add(amountLabel);
         input.add(amount);
         mainPanel.add(input, BorderLayout.CENTER);
 
         JPanel buttons = new JPanel();
         deposit = new JButton("Deposit");
         deposit.addActionListener(this);
         back = new JButton("Back");
         back.addActionListener(this);
         buttons.add(deposit);
         buttons.add(back);
         
         errorPanel = new JPanel();
         errorMessage = new JLabel("");
         errorPanel.add(errorMessage);
 
         mainPanel.add(errorPanel, BorderLayout.NORTH);
         mainPanel.add(buttons, BorderLayout.SOUTH);
 
     }
 
     public void actionPerformed(ActionEvent e) {
         // checks if back buttton was pressed 
         JButton activeButton = (JButton)e.getSource();
         String option = activeButton.getText();
         
         if(option.equals("Back")) {
             setVisible(false);
             new MenuPage();
         }
         else{      // server communication and error handling 
             String amountSend = amount.getText();
             String depositCheck = "";
         
             if(!amountSend.isEmpty()) {
                 BankCustomer.output.println("deposit");
                 BankCustomer.output.println(amountSend);
                 try{
                     depositCheck = BankCustomer.input.readLine();
                 }
                 catch(Exception ex){
                     System.out.println("Authentication comm failed.");
                 }
                 if(depositCheck.equals("200")){
                     setVisible(false);
                    //  new StartPage();
                     new MenuPage();
                 }
                 else {
                     errorMessage.setText(depositCheck);
                 }
                 
             }
             else{
                 errorMessage.setText("Please fill out all the fields.");
             }
         }
         
         
     }
 }
 
 
 class TransferForm extends JFrame implements ActionListener {
     private JTextField amount;
     private JLabel amountLabel;
     private JLabel toAccLabel;
     private JTextField toAccount;
     private JButton transfer;
     private JButton back;
     private JPanel mainPanel;
     private JLabel errorMessage;
     private JPanel errorPanel;
 
     public TransferForm() {
         
         // config UI for transfer form
         setTitle("Transfer Form");
         setSize(400, 150);
         setLocationRelativeTo(null);
         setResizable(true);
         setVisible(true);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 
         mainPanel = new JPanel(new BorderLayout());
         add(mainPanel);
         
         JPanel inputPanel = new JPanel(new GridLayout(2,2, 20, 20));
 
         amountLabel = new JLabel("Amount: ");
         amount = new JTextField();
         inputPanel.add(amountLabel);
         inputPanel.add(amount);
 
         toAccount = new JTextField();
         toAccLabel = new JLabel("Transfer Account No#: ");
         inputPanel.add(toAccLabel);
         inputPanel.add(toAccount);
         
         mainPanel.add(inputPanel, BorderLayout.CENTER);
 
         JPanel buttons = new JPanel();
         transfer = new JButton("Transfer");
         transfer.addActionListener(this);
         back = new JButton("Back");
         back.addActionListener(this);
         buttons.add(transfer);
         buttons.add(back);
         
         errorPanel = new JPanel();
         errorMessage = new JLabel("");
         errorPanel.add(errorMessage);
 
         mainPanel.add(buttons, BorderLayout.SOUTH);
         mainPanel.add(errorPanel, BorderLayout.NORTH);
         pack();
 
     }
 
     public void actionPerformed(ActionEvent e) {
         
         // checks if back button was pressed
         JButton activeButton = (JButton)e.getSource();
         String option = activeButton.getText();
         
         if(option.equals("Back")) {
             setVisible(false);
             new MenuPage();
         }
         else{
             String transAmount = amount.getText();
             String transAccount = toAccount.getText();
             String transferCheck = "";
             
             // checks if input is empty + server communication and error handling 
             if(!transAmount.isEmpty() && !transAccount.isEmpty()){
                 BankCustomer.output.println("transfer");
                 BankCustomer.output.println(transAmount);
                 BankCustomer.output.println(transAccount);
                 try{
                     transferCheck = BankCustomer.input.readLine();
                 }
                 catch(Exception ex){
                     System.out.println("Authentication comm failed.");
                 }
                 if(transferCheck.equals("200")){
                     setVisible(false);
                     new MenuPage();
                 }
                 else {
                     errorMessage.setText(transferCheck);
                 }
                 
             }
             else{      // error for empty input 
                 errorMessage.setText("Please fill out all the fields.");
             }
         }
 
     }
 
 }
 
 //This function will generate a pdf called statement.pdf and place it in the target folder of the code
 class GeneratePDF{
    GeneratePDF( )
     {
        ArrayList<String> transactionRows = new ArrayList<String>();
        //Get object sent by server
        try{
            ObjectInputStream inputStream = new ObjectInputStream(BankCustomer.s.getInputStream());
            transactionRows = (ArrayList<String>) inputStream.readObject();
        }catch(Exception e){
            System.out.println("Unable to get arraylist from server");
 
        }
        //Get the transactions for the user is passed in
 
        Document doc = new Document();  
        try{
            //The place where the pdf will be 
            String jarFilePath = BankCustomer.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            String directoryPath = new File(jarFilePath).getParent();
            System.out.println("Directory Path is:"+ directoryPath);
            // PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream("/Users/ronballer/NetBeansProjects/bankingApplication/src/main/java/statement.pdf"));  
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(directoryPath+"/statement.pdf"));  

            //Document object
            System.out.println("PDF created.");  
            doc.open(); 
            doc.add(new Paragraph("Bank Statement: (Transactions History)"));   
            //Draw a line
            doc.add(new Paragraph("______________________________________________________________________")); 
            for(String line : transactionRows)
            {
                doc.add(new Paragraph(line));
            }
            //close the PDF file
            doc.close();  
            //closes the writer  
            writer.close();  
 
 
        } catch(DocumentException e) {
            System.out.println("Document Processing Exception");
 
        }
        catch (FileNotFoundException e){  
            System.out.println("File Not Found Exception");
        }  
     }
    
 }