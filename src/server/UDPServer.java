package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UDPServer {

    private static final int PORT = 9980;
    private static final int BUFFER_SIZE = 1024;
    private DatagramSocket socket;
    private Gson gson;

    public UDPServer() throws SocketException {
        socket = new DatagramSocket(PORT);
        gson = new Gson();
        System.out.println("UDP Server started on port " + PORT);
    }

    public void start() {
        try (Connection conn = DatabaseUtil.getConnection()) {
            System.out.println("Server đã kết nối thành công đến cơ sở dữ liệu.");
        } catch (SQLException e) {
            System.err.println("Server không thể kết nối đến cơ sở dữ liệu. Dừng server.");
            return;
        }
        byte[] receiveBuffer = new byte[BUFFER_SIZE];
        while (true) {
            try {
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);
                String requestJson = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("Received request: " + requestJson);

                JsonObject request = gson.fromJson(requestJson, JsonObject.class);
                String action = request.get("action").getAsString();

                String responseJson = "";
                switch (action) {
                    case "searchByName":
                        String name = request.get("name").getAsString();
                        List<Student> studentsByName = searchByName(name);
                        responseJson = gson.toJson(studentsByName);
                        break;
                    case "searchByGPA":
                        float minGPA = request.get("minGPA").getAsFloat();
                        float maxGPA = request.get("maxGPA").getAsFloat();
                        List<Student> studentsByGPA = searchByGPA(minGPA, maxGPA);
                        responseJson = gson.toJson(studentsByGPA);
                        break;
                    case "updateStudent":
                        JsonObject studentObj = request.getAsJsonObject("student");
                        Student student = gson.fromJson(studentObj, Student.class);
                        boolean updateResult = updateStudent(student);
                        responseJson = gson.toJson(updateResult);
                        break;
                    default:
                        responseJson = gson.toJson("Invalid action");
                }

                byte[] sendBuffer = responseJson.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(
                        sendBuffer,
                        sendBuffer.length,
                        receivePacket.getAddress(),
                        receivePacket.getPort()
                );
                socket.send(sendPacket);
                System.out.println("Sent response: " + responseJson);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<Student> searchByName(String name) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM student WHERE hoTen LIKE ?";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + name + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Student s = new Student();
                s.setIdSV(rs.getInt("idSV"));
                s.setMaSV(rs.getString("maSV"));
                s.setHoTen(rs.getString("hoTen"));
                s.setNamSinh(rs.getInt("namSinh"));
                s.setQueQuan(rs.getString("queQuan"));
                s.setGPA(rs.getFloat("GPA"));
                students.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    private List<Student> searchByGPA(float minGPA, float maxGPA) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM student WHERE GPA BETWEEN ? AND ?";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setFloat(1, minGPA);
            stmt.setFloat(2, maxGPA);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Student s = new Student();
                s.setIdSV(rs.getInt("idSV"));
                s.setMaSV(rs.getString("maSV"));
                s.setHoTen(rs.getString("hoTen"));
                s.setNamSinh(rs.getInt("namSinh"));
                s.setQueQuan(rs.getString("queQuan"));
                s.setGPA(rs.getFloat("GPA"));
                students.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    private boolean updateStudent(Student student) {
        String sql = "UPDATE student SET maSV = ?, hoTen = ?, namSinh = ?, queQuan = ?, GPA = ? WHERE idSV = ?";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, student.getMaSV());
            stmt.setString(2, student.getHoTen());
            stmt.setInt(3, student.getNamSinh());
            stmt.setString(4, student.getQueQuan());
            stmt.setFloat(5, student.getGPA());
            stmt.setInt(6, student.getIdSV());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        try {
            UDPServer server = new UDPServer();
            server.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
