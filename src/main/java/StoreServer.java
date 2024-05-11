import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StoreServer{
    private static Map<String, Integer> inventory = new HashMap<>();
    private static Map<String, Customer> customers = new HashMap<>();
    private Customer currentCustomer;

    public static void main(String[] args) {
        inventory.put("shoe1", 5);
        inventory.put("shoe2", 5);
        inventory.put("shoe3", 5);

        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(8080);
            System.out.println("Server started");

            while (true) {

                System.out.println("Waiting for a client ...");
                new ClientHandler(serverSocket.accept()).start();
            }
            //System.out.println("Closing connection");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }



    private boolean isValidId(String id) {
        for (Customer customer : customers.values()) {
            if (customer.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidName(String name) {
        for (Customer customer : customers.values()) {
            if (customer.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidMoney(String moneyStr) {
        try {
            int money = Integer.parseInt(moneyStr);
            return money >= 0; //TODO maybe manfi is valid
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidProductName(String productName) {
        return inventory.containsKey(productName);
    }

    private boolean isValidQuantity(String quantityStr) {
        try {
            int quantity = Integer.parseInt(quantityStr);
            return quantity > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void chargeCustomer(int chargeAmount, DataOutputStream dataOutputStream) throws IOException {
        if (currentCustomer == null) {
            dataOutputStream.writeUTF("You must login first");
            return;
        }

        currentCustomer.setMoney(currentCustomer.getMoney() + chargeAmount);
        dataOutputStream.writeUTF("You have been charged " + chargeAmount + " successfully. Your new balance is " + currentCustomer.getMoney());
    }

    private int getPrice(String productName, DataOutputStream dataOutputStream) throws IOException {

        if (!isValidProductName(productName)) {
            dataOutputStream.writeUTF("Product not found");
            return -1;
        }

        for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
            if (entry.getKey().equals(productName)) {
                return entry.getValue();
            }
        }
        return -1;
    }

    private int getQuantity(String productName, DataOutputStream dataOutputStream) throws IOException {

        if (!isValidProductName(productName)) {
            dataOutputStream.writeUTF("Product not found");
            return -1;
        }

        for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
            if (entry.getKey().equals(productName)) {
                return entry.getValue();
            }
        }
        return -1;
    }

    private void purchaseProduct(String productName, int quantity, DataOutputStream dataOutputStream) throws IOException {
        if (!isValidProductName(productName)) {
            dataOutputStream.writeUTF("Product not found");
            return;
        }

        if (quantity > inventory.get(productName)) {
            dataOutputStream.writeUTF("Not enough stock");
            return;
        }

        if (currentCustomer == null) {
            dataOutputStream.writeUTF("You must login first");
            return;
        }

        int price = getPrice(productName, dataOutputStream);
        if (price == -1) {
            return;
        }

        int totalPrice = price * quantity;
        if (currentCustomer.getMoney() < totalPrice) {
            dataOutputStream.writeUTF("Not enough money");
            return;
        }

        currentCustomer.setMoney(currentCustomer.getMoney() - totalPrice);
        inventory.put(productName, inventory.get(productName) - quantity);
        dataOutputStream.writeUTF("You have purchased " + quantity + " " + productName + " for " + totalPrice + ". Your new balance is " + currentCustomer.getMoney());
    }

    private void getCustomerMoney(DataOutputStream dataOutputStream) throws IOException {
        if (currentCustomer == null) {
            dataOutputStream.writeUTF("You must login first");
            return;
        }

        dataOutputStream.writeUTF("Your balance is " + currentCustomer.getMoney());
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private DataInputStream dataInputStream = null;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            System.out.println("Client accepted");
        }

        public void run() {
            try {
                dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

                String request = "";

                while (!request.equals("exit")) {
                    try {
                        request = dataInputStream.readUTF();
                        System.out.println(request);

                    }
                    catch(IOException i) {
                        System.out.println(i);
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    if (dataInputStream != null)
                        dataInputStream.close();
                    if (socket != null)
                        socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

class Customer {
    private String name;
    private String id;
    private int money;

    public Customer(String name, String id, int money) {
        this.name = name;
        this.id = id;
        this.money = money;
    }


    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

