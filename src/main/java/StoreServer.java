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
    private static ServerSocket serverSocket = null;

    public static void main(String[] args) {
        inventory.put("shoe1", 5);
        inventory.put("shoe2", 5);
        inventory.put("shoe3", 5);

        try {
            serverSocket = new ServerSocket(8080);
            System.out.println("Server started");

            while (true) {
                System.out.println("Waiting for a client ...");
                new ClientHandler(serverSocket.accept()).start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static class ClientHandler extends Thread {
        private Customer currentCustomer;
        private Socket socket;
        private DataInputStream dataInputStream = null;
        private DataOutputStream dataOutputStream = null;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            System.out.println("Client accepted");
        }

        public void run() {
            try {
                dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                String request = "";

                while (!request.equals("exit")) {
                    try {
                        request = dataInputStream.readUTF();
                        System.out.println(request);
                        handleRequests(request , dataOutputStream);
                    }
                    catch(IOException i) {
                        System.out.println(i.getMessage());
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

        private void handleRequests(String request , DataOutputStream dataOutputStream) throws IOException {
            for (Regex regex : Regex.values()) {
                Pattern pattern = Pattern.compile(regex.getRegex());
                Matcher matcher = pattern.matcher(request);
                if (matcher.matches()) {
                    switch (regex) {
                        case REGISTER:
                            register(matcher.group("id"), matcher.group("name"), matcher.group("money") , dataOutputStream);
                            break;
                        case LOGIN:
                            login(matcher.group("id") , dataOutputStream);
                            break;
                        case LOGOUT:
                            logout( dataOutputStream);
                            break;
                        case GETPRICE:
                            getPrice(matcher.group("shoename") , dataOutputStream);
                            break;
                        case GETQUANTITY:
                            getQuantity(matcher.group("shoename") , dataOutputStream);
                            break;
                        case GETMONEY:
                            getCustomerMoney(dataOutputStream);
                            break;
                        case CHARGE:
                            chargeCustomer(matcher.group("money") , dataOutputStream);
                            break;
                        case PURCHASE:
                            purchaseProduct(matcher.group("shoename"), matcher.group("quantity") , dataOutputStream);
                            break;
                    }
                }
            }
        }

        private boolean isValidId(String id) {
            for (Customer customer : customers.values()) {
                if (customer.getId().equals(id)) {
                    return false;
                }
            }
            return true;
        }

        private boolean isValidName(String name) {
            for (Customer customer : customers.values()) {
                if (customer.getName().equals(name)) {
                    return false;
                }
            }
            return true;
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

        private void register(String id, String name, String moneyStr, DataOutputStream dataOutputStream) throws IOException {
            if (!isValidId(id)) {
                dataOutputStream.writeUTF("Id already exists");
                return;
            }

            if (!isValidName(name)) {
                dataOutputStream.writeUTF("Name already exists");
                return;
            }

            if (!isValidMoney(moneyStr)) {
                dataOutputStream.writeUTF("Invalid money");
                return;
            }

            int money = Integer.parseInt(moneyStr);
            Customer customer = new Customer(name, id, money);
            customers.put(id, customer);
            dataOutputStream.writeUTF("You have been registered successfully");
        }

        private void login(String id , DataOutputStream dataOutputStream) throws IOException {
            if (customers.containsKey(id)){
                currentCustomer = customers.get(id);
                dataOutputStream.writeUTF("You have been logged in successfully");
            }
            else {
                dataOutputStream.writeUTF("Customer not found");
            }
        }

        private void logout(DataOutputStream dataOutputStream) throws IOException {
            if (currentCustomer != null) {
                System.out.println(currentCustomer.getName() + " logged out");
                currentCustomer = null;
            }
            else {
                dataOutputStream.writeUTF("You must login first");
            }
        }

        private void chargeCustomer(String chargeAmount, DataOutputStream dataOutputStream) throws IOException {
            if (currentCustomer == null) {
                dataOutputStream.writeUTF("You must login first");
                return;
            }

            if (!isValidMoney(chargeAmount)) {
                dataOutputStream.writeUTF("Invalid money");
                return;
            }

            currentCustomer.setMoney(Integer.parseInt(chargeAmount) + currentCustomer.getMoney());
            dataOutputStream.writeUTF("You have been charged " + chargeAmount + " successfully. Your new balance is " + currentCustomer.getMoney());
        }

        private int getPrice(String productName, DataOutputStream dataOutputStream) throws IOException {

            if (!isValidProductName(productName)) {
                dataOutputStream.writeUTF("Product not found");
                return -1;
            }

            for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
                if (entry.getKey().equals(productName)) {
                    dataOutputStream.writeUTF("The price of " + productName + " is " + entry.getValue());
                    return entry.getValue();
                }
            }
            return -1;
        }

        private void getQuantity(String productName, DataOutputStream dataOutputStream) throws IOException {

            if (!isValidProductName(productName)) {
                dataOutputStream.writeUTF("Product not found");
                return ;
            }

            for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
                if (entry.getKey().equals(productName)) {
                    dataOutputStream.writeUTF("The quantity of " + productName + " is " + entry.getValue());
                    break;
                }
            }
        }

        private void purchaseProduct(String productName, String quantity, DataOutputStream dataOutputStream) throws IOException {
            if (!isValidProductName(productName)) {
                dataOutputStream.writeUTF("Product not found");
                return;
            }

            if (!isValidQuantity(quantity)) {
                dataOutputStream.writeUTF("Invalid quantity");
                return;
            }

            if (Integer.parseInt(quantity) > inventory.get(productName)) {
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

            int totalPrice = price * Integer.parseInt(quantity);
            if (currentCustomer.getMoney() < totalPrice) {
                dataOutputStream.writeUTF("Not enough money");
                return;
            }

            currentCustomer.setMoney(currentCustomer.getMoney() - totalPrice);
            inventory.put(productName, inventory.get(productName) - Integer.parseInt(quantity));
            dataOutputStream.writeUTF("You have purchased " + quantity + " " + productName + " for " + totalPrice + ". Your new balance is " + currentCustomer.getMoney());
        }

        private void getCustomerMoney(DataOutputStream dataOutputStream) throws IOException {
            if (currentCustomer == null) {
                dataOutputStream.writeUTF("You must login first");
                return;
            }

            dataOutputStream.writeUTF("Your balance is " + currentCustomer.getMoney());
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

