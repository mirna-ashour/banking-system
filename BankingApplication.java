/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

 package com.mycompany.bankingapplication;
 import java.io.FileOutputStream;
 import java.io.FileNotFoundException;  
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Time;
 import java.sql.Timestamp;
 import java.time.LocalDateTime;
 import java.time.format.DateTimeFormatter;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.ArrayList;
//PDF Generation imports
import com.itextpdf.text.Document;  
import com.itextpdf.text.DocumentException;  
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfPage;   
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.draw.*; 
import com.itextpdf.text.pdf.*;
import java.io.ObjectOutputStream;

 /**
  *
  * @author Rohan Bhalla and Mirna Ashour
  */
 
 //SERVER SIDE CODE
 
 
 //ServerSide Code in this Application
 public class BankingApplication {
 
     public static void main(String[] args) {
         System.out.println("Banking Server:");
         
         //Connect to the database and see if it works
         ConnectDatabase.connection();
         
         //Start server
         //Socket connections code
         try {
             ServerSocket ss = new ServerSocket(5050);
             while (true){
                 Socket sock = ss.accept(); //Blocking system call
                 System.out.println("Got a connection from: "+sock.getInetAddress());
                 ProcessConnection pConnect = new ProcessConnection(sock);
                 ProcessConnection.conn = ConnectDatabase.c;
                 pConnect.start();
             }       
         } catch (IOException ex) {
             System.out.println("Unable to bind to port!");
         }    
         
         
     }
     
 }
 
 
 
 //Create classes for all the entities probably (corresponding to tables in DB)
 //Only used for reference and to know the corresponding fields
 class user{
     int account_num;
     String first_name;
     String last_name;
     String username;
     String password;
     int balance;
 }
 
 
 class transaction{
     int trans_id;
     int account_num;
     int amount;
     String type;
     Time time;
     Date date;
 }
 
 class ProcessConnection extends Thread{
     static Connection conn;
     String userName;
     Socket sock;
     //Account number variable will be set when the user logs in
     //Use this variable for the actions of depositing, transfering and withdrawing from the account
     int accountNum;
     
     //Socket variables to be used throughout the class
     BufferedReader sin;
     PrintStream sout;
     ProcessConnection(Socket newSock){
         sock = newSock;
     }
     @Override
     public void run(){
         try{
             sin = new BufferedReader(new InputStreamReader(sock.getInputStream())); 
             sout = new PrintStream(sock.getOutputStream());
            String message = "";
            while ((message = sin.readLine()) != null){
                System.out.println("Received message from client: " + message);
                if(message.equalsIgnoreCase("REGISTER"))
                {
                    registerUser();

                } else if(message.equalsIgnoreCase("LOGIN"))
                {
                    authenticateUser();

                }
                else if(message.equalsIgnoreCase("TRANSFER"))
                {
                    int amount = Integer.parseInt(sin.readLine());
                    int toAccount = Integer.parseInt(sin.readLine());
                    transfer(amount, toAccount);
                    
                }
                else if(message.equalsIgnoreCase("DEPOSIT"))
                {
                    int amount = Integer.parseInt(sin.readLine());
                    deposit(amount);

                }else if(message.equalsIgnoreCase("WITHDRAW"))
                {
                    int amount = Integer.parseInt(sin.readLine());
                    withdraw(amount);

                }else if(message.equalsIgnoreCase("REPORT"))
                {
                   sendOverTrans();
                }
                else if(message.equalsIgnoreCase("BALANCE"))
                {
                    getBalance();
                }
                System.out.println(message);
            }

            
         }catch(Exception e){
             System.out.println(sock.getInetAddress()+" disconnected");
         }
     }
     
     //Not sure how to organize the file yet so just going
     //to implement most functions here and then take care of it later one
     
     //Register User Function
     boolean registerUser()
     {
         boolean registered = false;
         //TESTING PRINT OUT
        //  sout.println("Registration---------------");
         //Create a new user object
         user newUser = new user();
         
         //Give prompts and fill in its fields with user input
         try {
            //  sout.println("Enter First Name:");
             newUser.first_name = sin.readLine();
            //  sout.println("Enter Last Name:");
             newUser.last_name = sin.readLine();
            //  sout.println("Set Username:");
             newUser.username = sin.readLine();
            //  sout.println("Set Password:");
             newUser.password = sin.readLine();
            //  sout.println("Starting Balance with default amount: $200");
             newUser.balance = 200;            
         } catch (IOException ex) {
             System.out.println("Unable to receive registration input");
         }
         //SQL query to insert into database
         //Name all the column names after auto incrment 
         try {

            PreparedStatement checkStmt = conn.prepareStatement("SELECT account_num FROM user WHERE username = ?");
            checkStmt.setString(1, newUser.first_name);
            ResultSet results = checkStmt.executeQuery();
            //Check if user isnt already present
            if(!results.isBeforeFirst()) {
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO user (first_name, last_name, username, password, balance) VALUES (?, ?, ?, ?, ?)");
                stmt.setString(1, newUser.first_name);
                stmt.setString(2, newUser.last_name);
                stmt.setString(3, newUser.username);
                stmt.setString(4, newUser.password);
                stmt.setInt(5, newUser.balance);
                stmt.executeUpdate();
                registered = true;
                //Return success code
                sout.println("200");
            }
            else {
                registered = false;
                sout.println("500");
            }
            
         } catch (SQLException ex) {
             System.out.println(ex.toString());
             System.out.println("SQL insert for registration didn't work");
         }
         //After registering calling in login function
 
         return registered;
     }
     
     //Authenticate & Login Function for users
      Boolean authenticateUser()
     {
         String password = "";
         //Test line commented out
        //  sout.println("Login---------------");
         //Get username and password from the user
         try {
             // sout.println("Enter Username:");
             userName = sin.readLine();
             // sout.println("Enter Password:");
             password = sin.readLine();
         } catch (IOException ex) {
             // sout.println("Incorrect Username or Password!");
             System.out.println("Unable to Authenticate user");
 
         }
         
         
         //Connection c is the datbase connector
         //Need the socket of the client to be able to send it a message for connected or cancelled
         Boolean answer = false;
         try {
             //Query from the database and get            
             //Prepared statement is used to create parameterized queries
             PreparedStatement s = conn.prepareStatement("SELECT * FROM user WHERE username=? AND password=?");
             
             
             s.setString(1, userName);
             s.setString(2, password);
             ResultSet rs = s.executeQuery();
             answer = rs.next();
            
         } catch (SQLException ex) {
             System.out.println("Authentication problem!");
         }
         if(answer)
         {
             //reply with 200
             try {
                 PrintStream sendMsg = new PrintStream(this.sock.getOutputStream());
                 sendMsg.println("200");
                 //Setup vairable with account number if logged in successfully
                getUserAccountNum(userName);
                // System.out.println("200");
             } catch (IOException ex) {
                System.out.println("Unable to send confirmation of login!");
             }
         }
         else
         {
             //reply with 500
             try {
                 PrintStream sendMsg = new PrintStream(this.sock.getOutputStream());
                 sendMsg.println("500");
 //                System.out.println("500");
             } catch (IOException ex) {
                System.out.println("Unable to send login didn't work!");
             }
         }         
         //Place login entry into login table using insertlogin
         insertLogin();
         return answer;
     }
 
 
     void getUserAccountNum(String username)
     {
         PreparedStatement selectStmt = null;
 
         try{
             String sql = "SELECT account_num FROM user WHERE username = ?";
             selectStmt = conn.prepareStatement(sql);
 
             // Set the parameter
             selectStmt.setString(1, username);
 
             // Execute the SQL statement
             ResultSet resultSet = selectStmt.executeQuery();
 
             // Set the account number by taking it from the database
             if (resultSet.next()) {
                 accountNum = resultSet.getInt("account_num");
             } else {
                 System.out.println("User with username " + username + " not found.");
             }
 
         } catch(SQLException se) {
             System.out.println("Unable to get account number for operations");
 
         }
         
 
     }

    
     //This is another function for inserting an entry into the login table for every login
     void insertLogin()
     {
         try {
            //get current data/time for database field time
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            //Get user account num from accountNum variable
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO login (account_num, date_time) VALUES (?, ?)");
            stmt.setInt(1, accountNum);
            stmt.setTimestamp(2, Timestamp.valueOf(now));
            stmt.executeUpdate();
            
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            System.out.println("SQL insert for Login didn't work");
        }
    }

    void insertTransaction(String type, double amount)
     {
        try {
            //get current data/time for database field time
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            Date date = new Date(0L);
            //Setting time
            date.setTime(new java.util.Date().getTime());
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO transaction (account_num, amount, type, date, time) VALUES (?, ?, ?, ?, ?)");
            stmt.setInt(1, accountNum);
            stmt.setDouble(2, amount);
            stmt.setString(3, type);
            stmt.setDate(4, java.sql.Date.valueOf(java.time.LocalDate.now()));
            stmt.setTime(5, new Time (date.getTime()));
            stmt.executeUpdate();
        }catch (SQLException ex) {
            System.out.println(ex.toString());
            System.out.println("SQL insert for Transaction didn't work");
        }

     }

      
      //View Account Information Function
      void getBalance()
      {
          //SQL Query to get user's information
          PreparedStatement prepSelectStmt = null;
          PreparedStatement prepUpdateStmt = null;
          //Get all the information send it using server output to client
          try {
            // Select the user based on the account number
            String selectSql = "SELECT * FROM user WHERE account_num = ?";
            prepSelectStmt = conn.prepareStatement(selectSql);
            prepSelectStmt.setInt(1, accountNum);
            ResultSet resultSet = prepSelectStmt.executeQuery();

            //User existence check
            if (resultSet.next()) {
                double currentBalance = 0;
                // Get current balance from DB
                currentBalance = resultSet.getDouble("balance");
                sout.println(currentBalance);
            }
        } catch (SQLException se) {
            sout.println("Problem in getting balance");
         } 
          
      }
      
      //Deposit into Account Function 
      Boolean deposit(double amount)
      {
          Boolean deposited = false;
          PreparedStatement prepSelectStmt = null;
          PreparedStatement prepUpdateStmt = null;
 
          //
          try {
             // Select the user based on the account number
             String selectSql = "SELECT * FROM user WHERE account_num = ?";
             prepSelectStmt = conn.prepareStatement(selectSql);
             prepSelectStmt.setInt(1, accountNum);
             ResultSet resultSet = prepSelectStmt.executeQuery();
 
             //User existence check
             if (resultSet.next()) {
                 // Get current balance from DB
                 double currentBalance = resultSet.getDouble("balance");
 
                 // Updated balance field insert into DB
                 double newBalance = currentBalance + amount;
 
                 // Update the user balance in DB
                 String updateSql = "UPDATE user SET balance = ? WHERE account_num = ?";
                 prepUpdateStmt = conn.prepareStatement(updateSql);
                 prepUpdateStmt.setDouble(1, newBalance);
                 prepUpdateStmt.setInt(2, accountNum);
                 int rowsAffected = prepUpdateStmt.executeUpdate();
                //  System.out.println(rowsAffected + " rows updated");
                 deposited = true;
                 System.out.println("Deposited successfully. Account balance: " + newBalance);
                 sout.println("200");


             } else {
                 System.out.println("Account number: " + accountNum + " is invalid.");
                 sout.println("Account number: " + accountNum + " is invalid.");
             }
         } catch (SQLException se) {
            sout.println("SQL Problem in Depositing");
         } 
         //Place an entry upon success into the transaction table
         insertTransaction("DEPOSIT",amount);
         return deposited;
     }
      //Withdraw from Account Function
      Boolean withdraw(double amount)
      {
          Boolean withdrawn = false;
          PreparedStatement prepSelectStmt = null;
          PreparedStatement prepUpdateStmt = null;
 
          //
          try {
             // Select the user based on the account number
             String selectSql = "SELECT * FROM user WHERE account_num = ?";
             prepSelectStmt = conn.prepareStatement(selectSql);
             prepSelectStmt.setInt(1, accountNum);
             ResultSet resultSet = prepSelectStmt.executeQuery();
 
             // User Existence check
             if (resultSet.next()) {
                 // Get current balance from DB
                 double currentBalance = resultSet.getDouble("balance");
 
                 // updated balance for DB
                 double newBalance = currentBalance - amount;
 
                 //place new Balance into DB
                 String updateSql = "UPDATE user SET balance = ? WHERE account_num = ?";
                 prepUpdateStmt = conn.prepareStatement(updateSql);
                 prepUpdateStmt.setDouble(1, newBalance);
                 prepUpdateStmt.setInt(2, accountNum);
                 int rowsAffected = prepUpdateStmt.executeUpdate();
                 System.out.println(rowsAffected + " rows updated");
                 //Current system doesn't prevent account balance from becoming negative
                 withdrawn = true;
                 System.out.println("Withdrawn successfully. Account balance: " + newBalance);
                 sout.println("200");

             } else {
                 System.out.println("Account number: " + accountNum + " is invalid.");
                 sout.println("500");
             }
         } catch (SQLException se) {
             // Handle errors for JDBC
             System.out.println("SQL Problem in Withdrawing");
         }
         //Place an entry upon success into the transaction table
         insertTransaction("WITHDRAW",amount);
         return withdrawn;
     }
      
      //Transfer Between Accounts Function
      Boolean transfer(double amount, int transferToAcc)
      {
          Boolean transferred = false;
          PreparedStatement selectStmt = null;
          PreparedStatement updateStmt = null;
          try { 
              // Select the user to withdraw from their account
              String selectFromSql = "SELECT * FROM user WHERE account_num = ?";
              selectStmt = conn.prepareStatement(selectFromSql);
              selectStmt.setInt(1, accountNum);
              ResultSet fromResult = selectStmt.executeQuery();
  
              // User existence and balance checking in the if statements
              if (fromResult.next()) {
                  double fromBalance = fromResult.getDouble("balance");
                  if (fromBalance >= amount) {
                      // Generate updated balance after transfering 
                      double newFromBalance = fromBalance - amount;
  
                      // Update the "from" account's balance in the database
                      String updateFromSql = "UPDATE user SET balance = ? WHERE account_num = ?";
                      updateStmt = conn.prepareStatement(updateFromSql);
                      updateStmt.setDouble(1, newFromBalance);
                      updateStmt.setInt(2, accountNum);
                      int fromRowsAffected = updateStmt.executeUpdate();
                      System.out.println(fromRowsAffected + " rows updated for withdrawal");
  
                      // Select the user to whom to deposit
                      String selectToSql = "SELECT * FROM user WHERE account_num = ?";
                      selectStmt = conn.prepareStatement(selectToSql);
                      selectStmt.setInt(1, transferToAcc);
                      ResultSet toResult = selectStmt.executeQuery();
  
                      // Check if the "to" account exists
                      if (toResult.next()) {
                          double toBalance = toResult.getDouble("balance");
  
                          // Calculate the new balance for the "to" account
                          double newToBalance = toBalance + amount;
  
                          // Update the "to" account's balance in the database
                          String updateToSql = "UPDATE user SET balance = ? WHERE account_num = ?";
                          updateStmt = conn.prepareStatement(updateToSql);
                          updateStmt.setDouble(1, newToBalance);
                          updateStmt.setInt(2, transferToAcc);
                          int toRowsAffected = updateStmt.executeUpdate();
                          System.out.println(toRowsAffected + " rows updated for deposit");
  
                          System.out.println("Transfer successful. New balance for " + transferToAcc + ": " + newFromBalance);
                          System.out.println("New balance for " + transferToAcc + ": " + newToBalance);
                          //Sending code 200 for success in transferring 
                          sout.println("200");
                      } else {
                          System.out.println("User with account number " + transferToAcc + " not found.");
                          sout.println("User with account number " + transferToAcc + " not found.");
                      }
                  } else {
                      System.out.println("Insufficient balance in account " + accountNum + ".");
                      sout.println("Insufficient balance in account " + accountNum + ".");
                  }
              } else {
                  System.out.println("User with account number " + accountNum + " not found.");
                  sout.println("User with account number " + accountNum + " not found.");
              }
          } catch (SQLException se) {
              // Handle errors for JDBC
            System.out.println("JDBC PROBLEM");
          } catch (Exception e) {
              // Handle errors for Class.forName
              System.out.println("OTHER PROBLEM");
              
          }
          insertTransaction("TRANSFER", amount);
          return transferred;
          
      }
      //Transaction history function that will get all the transactions for a customer
      //This will be used by generate PDF for reporting purposes
      ArrayList<String> transHistory()
      {
          ArrayList<String> transactionRows = new ArrayList<>();
          PreparedStatement selectStmt = null;
          try { 
            // SQL statement for selecting rows from transaction table
            String sql = "SELECT * FROM transaction WHERE account_num = ?";
            selectStmt = conn.prepareStatement(sql);
            selectStmt.setInt(1, accountNum);

            // Execute the SQL statement
            ResultSet resultSet = selectStmt.executeQuery();

            // Iterate over the result set and retrieve rows
            while (resultSet.next()) {
                int transId = resultSet.getInt("trans_id");
                double amount = resultSet.getDouble("amount");
                String type = resultSet.getString("type");
                String date = resultSet.getString("date");
                String time = resultSet.getString("time");

                String row = "TransID: " + transId + ", Amount: " + amount + ", Type: " + type +
                        ", Date: " + date + ", Time: " + time;
                transactionRows.add(row);
            }


            
          } catch (SQLException se) {
            System.out.println("SQL problem getting rows for Transaction history");
          }
          return transactionRows;

      }
      
      
    //Generate PDF code (test first) 
    Boolean generatePDF()
    {
       Boolean generated  = false;
       //Get the transactions for the user
       ArrayList<String> transactionRows = transHistory();

       Document doc = new Document();  
       try{
           //The place where the pdf will be 
           PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream("/Users/ronballer/NetBeansProjects/bankingApplication/src/main/java/statement.pdf"));  
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
       return generated;
    }

    void sendOverTrans()
    {
       ArrayList<String> transactionRows = transHistory();
       try{
           ObjectOutputStream outputStream = new ObjectOutputStream(sock.getOutputStream());
           //Send the array of strings to the client
           outputStream.writeObject(transactionRows);

       }catch(Exception e){
           System.out.println("Transmitting the transactions failed");

       }
       
    }


 }

 
 //Class and functions to connect to the database in the server side
 class ConnectDatabase{
     static Connection c;
     static void connection()
     {
         try {
 //            System.out.println("Chat server:");
             
             String username = "root";
             String password = ""; //
            //  String password = "";
             String url = "jdbc:mysql://localhost:3306/banking_system";
             Class.forName("com.mysql.cj.jdbc.Driver");
             c = DriverManager.getConnection(url, username, password);
             
             //Add in later to attach to process connections
             //ProcessConnection.conn = c;
             if(c!= null)
             {
                 //Connection successful then do the socket stuff
                 System.out.print("DB Connection successful!\n");
             }
             else
             {
                 System.out.print("Connection FAILED!");
             }
             
         } catch (ClassNotFoundException ex) {
             System.out.print("Class driver not working 1");
         } catch (SQLException ex) {
              System.out.print("SQL problem");
         }
         
     }
     
 }
 
 
 
 
 
 
 
 