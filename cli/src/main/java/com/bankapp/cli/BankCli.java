package com.bankapp.cli;

import com.bankapp.cli.ApiClient.ApiException;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.Console;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

/** Command-line front end. Every action is an HTTP call to the Spring Boot REST API. */
public class BankCli {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final Scanner in = new Scanner(System.in);
    private final ApiClient api = new ApiClient(System.getenv().getOrDefault("BANK_API_URL", "http://localhost:8080"));

    public static void main(String[] args) {
        new BankCli().run();
    }

    // ------------------------------------------------------------------ main menu

    private void run() {
        System.out.println("=== Java Banking App (CLI) ===");
        while (true) {
            System.out.println();
            System.out.println("1) Register");
            System.out.println("2) Log in");
            System.out.println("3) Exit");
            switch (prompt("Choose")) {
                case "1" -> register();
                case "2" -> { if (login()) dashboard(); }
                case "3" -> { System.out.println("Goodbye!"); return; }
                default -> System.out.println("Please enter 1, 2 or 3.");
            }
        }
    }

    private void register() {
        System.out.println("\n--- Register ---");
        String name = prompt("Full name");
        String phone = prompt("Phone number (11 digits)");
        String pin = readPin("PIN (4-6 digits)");
        if (!pin.equals(readPin("Confirm PIN"))) {
            System.out.println("PINs do not match.");
            return;
        }
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("fullName", name);
            body.put("phoneNumber", phone);
            body.put("pin", pin);
            JsonNode acct = api.post("/api/auth/register", body);
            System.out.println("\nRegistration successful!");
            System.out.println("Account number: " + acct.get("accountNumber").asText());
            System.out.println("Balance:        " + money(acct.get("balance")));
            System.out.println("You can now log in.");
        } catch (ApiException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private boolean login() {
        System.out.println("\n--- Log in ---");
        String phone = prompt("Phone number");
        String pin = readPin("PIN");
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("phoneNumber", phone);
            body.put("pin", pin);
            JsonNode res = api.post("/api/auth/login", body);
            api.setToken(res.get("token").asText());
            System.out.println("Login successful.");
            return true;
        } catch (ApiException e) {
            System.out.println("Login failed: " + e.getMessage());
            return false;
        }
    }

    // ------------------------------------------------------------------ dashboard

    private void dashboard() {
        while (api.hasToken()) {
            try {
                showDashboard();
                System.out.println("1) Transfer money    2) Deposit    3) Withdraw");
                System.out.println("4) Full history      5) Refresh    6) Log out");
                switch (prompt("Choose")) {
                    case "1" -> transfer();
                    case "2" -> cash("deposit");
                    case "3" -> cash("withdraw");
                    case "4" -> history();
                    case "5" -> { /* loop redraws */ }
                    case "6" -> logout();
                    default -> System.out.println("Please enter a number from 1 to 6.");
                }
            } catch (ApiException e) {
                if (e.status() == 401) {
                    System.out.println("\nYour session has expired. Please log in again.");
                    api.setToken(null);
                } else {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
    }

    private void showDashboard() throws ApiException {
        JsonNode acct = api.get("/api/accounts/me");
        JsonNode recent = api.get("/api/transactions/recent");

        System.out.println("\n==================== DASHBOARD ====================");
        System.out.println("Name:           " + acct.get("fullName").asText());
        System.out.println("Phone:          " + acct.get("phoneNumber").asText());
        System.out.println("Account number: " + acct.get("accountNumber").asText());
        System.out.println("Balance:        " + money(acct.get("balance")));
        System.out.println("---------------- Recent activity ------------------");
        printTransactions(recent);
        System.out.println("===================================================");
    }

    private void transfer() throws ApiException {
        System.out.println("\n--- Transfer money ---");
        String phone = prompt("Recipient phone number");
        JsonNode recipient = api.get("/api/transfers/recipient/" + phone);
        System.out.println("Recipient: " + recipient.get("fullName").asText());

        BigDecimal amount = readAmount("Amount to send");
        if (amount == null) return;
        String description = prompt("Description (optional, press Enter to skip)");

        String confirm = prompt("Send " + money(amount) + " to " + recipient.get("fullName").asText() + "? (y/n)");
        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Transfer cancelled.");
            return;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("recipientPhoneNumber", phone);
        body.put("amount", amount);
        body.put("description", description.isBlank() ? null : description);
        JsonNode res = api.post("/api/transfers", body);

        System.out.println("\nTransfer successful!");
        System.out.println("Reference #:  " + res.get("transactionId").asLong());
        System.out.println("Sent to:      " + res.get("recipientName").asText());
        System.out.println("Amount:       " + money(res.get("amount")));
        System.out.println("New balance:  " + money(res.get("newBalance")));
    }

    private void cash(String action) throws ApiException {
        System.out.println("\n--- " + (action.equals("deposit") ? "Cash deposit" : "Cash withdrawal") + " ---");
        BigDecimal amount = readAmount("Amount");
        if (amount == null) return;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("amount", amount);
        JsonNode res = api.post("/api/accounts/" + action, body);
        System.out.println("Done. New balance: " + money(res.get("balance")));
    }

    private void history() throws ApiException {
        int page = 0;
        while (true) {
            JsonNode list = api.get("/api/transactions/history?page=" + page + "&size=10");
            System.out.println("\n--- Transaction history (page " + (page + 1) + ") ---");
            printTransactions(list);
            if (list.size() == 0 && page > 0) {
                page--;
            }
            String cmd = prompt("[n] next page, [p] previous page, [q] back").toLowerCase();
            if (cmd.equals("n")) page++;
            else if (cmd.equals("p")) page = Math.max(0, page - 1);
            else return;
        }
    }

    private void logout() {
        try {
            api.post("/api/auth/logout", Map.of());
        } catch (ApiException ignored) {
            // token is discarded locally either way
        }
        api.setToken(null);
        System.out.println("You have been logged out.");
    }

    // ------------------------------------------------------------------ helpers

    private void printTransactions(JsonNode list) {
        if (list.size() == 0) {
            System.out.println("(no transactions yet)");
            return;
        }
        for (JsonNode t : list) {
            String type = t.get("type").asText();
            boolean incoming = type.equals("MONEY_RECEIVED") || type.equals("CASH_DEPOSIT");
            String status = t.get("status").asText();
            String sign = status.equals("FAILED") ? " " : (incoming ? "+" : "-");

            String other = switch (type) {
                case "MONEY_SENT" -> "to " + t.get("receiverAccount").asText();
                case "MONEY_RECEIVED" -> "from " + t.get("senderAccount").asText();
                default -> "";
            };
            String when = DATE_FMT.format(Instant.parse(t.get("createdAt").asText()));
            String desc = t.hasNonNull("description") && !type.startsWith("CASH") ? "  \"" + t.get("description").asText() + "\"" : "";

            System.out.printf("#%-4d %s  %-15s %s%-14s %-7s %s%s%n",
                    t.get("id").asLong(), when, type, sign, money(t.get("amount")), status, other, desc);
        }
    }

    private static String money(JsonNode amount) {
        return money(amount.decimalValue());
    }

    private static String money(BigDecimal amount) {
        return "₱" + String.format(Locale.US, "%,.2f", amount);
    }

    private BigDecimal readAmount(String label) {
        String raw = prompt(label).replace(",", "");
        try {
            BigDecimal value = new BigDecimal(raw);
            if (value.signum() <= 0) {
                System.out.println("Amount must be greater than zero.");
                return null;
            }
            return value;
        } catch (NumberFormatException e) {
            System.out.println("That is not a valid amount.");
            return null;
        }
    }

    private String prompt(String label) {
        System.out.print(label + ": ");
        System.out.flush();
        if (!in.hasNextLine()) {          // input closed (Ctrl+D): quit cleanly
            System.out.println();
            System.exit(0);
        }
        return in.nextLine().trim();
    }

    /** Hides the PIN while typing when running in a real terminal. */
    private String readPin(String label) {
        Console console = System.console();
        if (console != null && console.isTerminal()) {
            char[] chars = console.readPassword("%s: ", label);
            return chars == null ? "" : new String(chars).trim();
        }
        return prompt(label);
    }
}
