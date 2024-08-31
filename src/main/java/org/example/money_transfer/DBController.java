package org.example.money_transfer;

import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
public class DBController {
    private static String url = "jdbc:sqlite:src/main/resources/DB/banking_transactions.db";

    @PostMapping("/newTransaction")
    public Map<String, String> newTransaction(@RequestBody Map<String, String> transaction) {
        String sourceAccountId = transaction.get("sourceAccountId");
        String targetAccountId = transaction.get("targetAccountId");
        double amount = Double.parseDouble(transaction.get("amount"));
        String currency = transaction.get("currency");
        String insertSQL = "INSERT INTO transactions values(?,?,?,?,?)";
        String result = null;
        double balance = 0.0;
        String currency1 = null;
        Account account = new Account();
        try {
            Connection connection = DriverManager.getConnection(url);
            PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
            preparedStatement.setString(2,sourceAccountId);
            preparedStatement.setString(3,targetAccountId);
            preparedStatement.setDouble(4,amount);
            preparedStatement.setString(5,currency);
            String selectSQL = "SELECT * FROM accounts WHERE id=?";
            PreparedStatement preparedStatement1 = connection.prepareStatement(selectSQL);
            PreparedStatement preparedStatement2 = connection.prepareStatement(selectSQL);
            preparedStatement1.setString(1,sourceAccountId);
            preparedStatement2.setString(1,targetAccountId);
            ResultSet resultSet = preparedStatement1.executeQuery();
            ResultSet resultSet1 = preparedStatement2.executeQuery();
            if (!resultSet.next() || !resultSet1.next()) {
                result = "Incorrect ID in source/ target banking account";
            }
            else {
                account.setBalance(resultSet.getDouble("balance"));
                balance = account.getBalance();
                if (balance < amount) {
                    result = "Insufficient balance";
                }
                else {
                    account.setCurrency(resultSet.getString("currency"));
                    currency1 = account.getCurrency();
                    if (!Objects.equals(currency1, currency)) {
                        result = "Insufficient currency";
                    }
                    else {
                        if (preparedStatement.executeUpdate() > 0) {
                            result = "The transaction has been completed";
                        }
                    }
                }
            }
            preparedStatement.close();
            connection.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
            if (sourceAccountId == null || targetAccountId == null || currency == null) {
                result = "There is an empty field";
            }
            else if (Objects.equals(sourceAccountId, targetAccountId)) {
                result = "Source account number is the same with target account number";
            }
            else if (amount <= 0) {
                result = "Amount must be greater than zero";
            }
            else if (currency.length() != 3) {
                result = "Currency must be 3 characters";
            }
        }
        Map<String, String> jsonMessage = new HashMap<>();
        jsonMessage.put("Message from DB", result);
        return jsonMessage;
    }
    @RequestMapping("/accountsList")
    public String accountsList() {
        String selectSQL = "SELECT * FROM accounts";
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Connection connection = DriverManager.getConnection(url);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(selectSQL);
            while (resultSet.next()) {
                stringBuilder.append("Account No: ").append(resultSet.getString("id")).append("   Balance: ").append(resultSet.getDouble("balance")).append(" ").append(resultSet.getString("currency")).append("\n\n");
            }
            statement.close();
            connection.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
    @RequestMapping("/transactionsList")
    public String transactionsList() {
        String selectSQL = "SELECT * FROM transactions";
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Connection connection = DriverManager.getConnection(url);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(selectSQL);
            while (resultSet.next()) {
                stringBuilder.append("ID: ").append(resultSet.getString("id")).append("   From Account: ").append(resultSet.getString("sourceAccountId")).append("   To Account: ").append(resultSet.getString("targetAccountId")).append("   Ammount: ").append(resultSet.getString("amount")).append(" ").append(resultSet.getString("currency")).append("\n\n");
            }
            statement.close();
            connection.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}
