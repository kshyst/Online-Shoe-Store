import java.io.*;
import java.net.Socket;

public class Client {
    private DataInputStream dataInputStream = null;
    private DataOutputStream dataOutputStream = null;
    private Socket socket = null;
    public void start() {
        try {
            socket = new Socket("127.0.0.1", 8080);
            System.out.println("Connected");
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            String request = "";

            while (!request.equals("exit")) {
                request = bufferedReader.readLine();
                dataOutputStream.writeUTF(request);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (dataInputStream != null)
                    dataInputStream.close();
                if (dataOutputStream != null)
                    dataOutputStream.close();
                if (socket != null)
                    socket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }
}
