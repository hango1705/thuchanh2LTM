package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import server.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Type;
import java.net.*;
import java.util.List;

public class UDPClientGUI extends JFrame {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 9980;
    private static final int BUFFER_SIZE = 4096;

    private Gson gson;
    private DatagramSocket socket;

    private JTextField txtSearchName;
    private JTextField txtMinGPA;
    private JTextField txtMaxGPA;
    private JButton btnSearchName;
    private JButton btnSearchGPA;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnUpdate;

    public UDPClientGUI() {
        gson = new Gson();
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không thể tạo socket.");
            System.exit(1);
        }

        setTitle("UDP Student Client");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
    }

    private void initComponents() {
        JPanel panelName = new JPanel(new FlowLayout());
        panelName.add(new JLabel("Tìm kiếm theo Họ tên:"));
        txtSearchName = new JTextField(20);
        panelName.add(txtSearchName);
        btnSearchName = new JButton("Tìm kiếm");
        panelName.add(btnSearchName);

        JPanel panelGPA = new JPanel(new FlowLayout());
        panelGPA.add(new JLabel("Tìm kiếm GPA từ:"));
        txtMinGPA = new JTextField(5);
        panelGPA.add(txtMinGPA);
        panelGPA.add(new JLabel("đến:"));
        txtMaxGPA = new JTextField(5);
        panelGPA.add(txtMaxGPA);
        btnSearchGPA = new JButton("Tìm kiếm");
        panelGPA.add(btnSearchGPA);

        String[] columnNames = {"ID", "Mã SV", "Họ Tên", "Năm Sinh", "Quê Quán", "GPA"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        btnUpdate = new JButton("Cập nhật thông tin sinh viên");

        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        topPanel.add(panelName);
        topPanel.add(panelGPA);
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(btnUpdate, BorderLayout.SOUTH);

        btnSearchName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchByName();
            }
        });

        btnSearchGPA.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchByGPA();
            }
        });

        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateStudent();
            }
        });
    }

    private void searchByName() {
        String name = txtSearchName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập họ tên để tìm kiếm.");
            return;
        }

        JsonObject request = new JsonObject();
        request.addProperty("action", "searchByName");
        request.addProperty("name", name);
        String requestJson = gson.toJson(request);

        sendRequest(requestJson);

        String responseJson = receiveResponse();

        if (responseJson == null || responseJson.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không nhận được dữ liệu từ server.");
            return;
        }

        java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<Student>>() {
        }.getType();
        List<Student> students = gson.fromJson(responseJson, listType);

        displayStudents(students);
    }

    private void searchByGPA() {
        String minGPAStr = txtMinGPA.getText().trim();
        String maxGPAStr = txtMaxGPA.getText().trim();

        if (minGPAStr.isEmpty() || maxGPAStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập khoảng GPA để tìm kiếm.");
            return;
        }

        float minGPA, maxGPA;
        try {
            minGPA = Float.parseFloat(minGPAStr);
            maxGPA = Float.parseFloat(maxGPAStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ cho GPA.");
            return;
        }

        if (minGPA > maxGPA) {
            JOptionPane.showMessageDialog(this, "GPA tối thiểu không được lớn hơn GPA tối đa.");
            return;
        }

        JsonObject request = new JsonObject();
        request.addProperty("action", "searchByGPA");
        request.addProperty("minGPA", minGPA);
        request.addProperty("maxGPA", maxGPA);
        String requestJson = gson.toJson(request);

        sendRequest(requestJson);

        String responseJson = receiveResponse();

        if (responseJson == null || responseJson.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không nhận được dữ liệu từ server.");
            return;
        }

        java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<Student>>() {
        }.getType();
        List<Student> students = gson.fromJson(responseJson, listType);

        displayStudents(students);
    }

    private void updateStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một sinh viên để cập nhật.");
            return;
        }

        int idSV = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
        String maSV = tableModel.getValueAt(selectedRow, 1).toString();
        String hoTen = tableModel.getValueAt(selectedRow, 2).toString();
        int namSinh = Integer.parseInt(tableModel.getValueAt(selectedRow, 3).toString());
        String queQuan = tableModel.getValueAt(selectedRow, 4).toString();
        float GPA = Float.parseFloat(tableModel.getValueAt(selectedRow, 5).toString());

        Student student = new Student();
        student.setIdSV(idSV);
        student.setMaSV(maSV);
        student.setHoTen(hoTen);
        student.setNamSinh(namSinh);
        student.setQueQuan(queQuan);
        student.setGPA(GPA);

        StudentUpdateDialog dialog = new StudentUpdateDialog(this, student);
        dialog.setVisible(true);

        if (dialog.isUpdated()) {
            Student updatedStudent = dialog.getStudent();
            tableModel.setValueAt(updatedStudent.getMaSV(), selectedRow, 1);
            tableModel.setValueAt(updatedStudent.getHoTen(), selectedRow, 2);
            tableModel.setValueAt(updatedStudent.getNamSinh(), selectedRow, 3);
            tableModel.setValueAt(updatedStudent.getQueQuan(), selectedRow, 4);
            tableModel.setValueAt(updatedStudent.getGPA(), selectedRow, 5);

            JsonObject request = new JsonObject();
            request.addProperty("action", "updateStudent");
            request.add("student", gson.toJsonTree(updatedStudent));
            String requestJson = gson.toJson(request);

            sendRequest(requestJson);
            String responseJson = receiveResponse();
            if (responseJson == null || responseJson.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không nhận được phản hồi từ server.");
                return;
            }
            boolean updateResult = gson.fromJson(responseJson, Boolean.class);
            if (updateResult) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công.");
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại.");
            }
        }
    }

    private void sendRequest(String requestJson) {
        try {
            byte[] sendData = requestJson.getBytes();
            InetAddress serverAddress = InetAddress.getByName(SERVER_ADDRESS);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
            socket.send(sendPacket);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không thể gửi yêu cầu đến server.");
        }
    }

    private String receiveResponse() {
        byte[] receiveBuffer = new byte[BUFFER_SIZE];
        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        try {
            socket.setSoTimeout(3000); // Đặt thời gian chờ 3 giây
            socket.receive(receivePacket);
            String responseJson = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Received response: " + responseJson);
            return responseJson;
        } catch (SocketTimeoutException e) {
            JOptionPane.showMessageDialog(this, "Timeout: Không nhận được phản hồi từ server.");
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi nhận phản hồi từ server.");
            return "";
        }
    }

    private void displayStudents(List<Student> students) {
        tableModel.setRowCount(0);

        for (Student s : students) {
            Object[] row = {
                s.getIdSV(),
                s.getMaSV(),
                s.getHoTen(),
                s.getNamSinh(),
                s.getQueQuan(),
                s.getGPA()
            };
            tableModel.addRow(row);
        }

        if (students.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy sinh viên nào.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UDPClientGUI client = new UDPClientGUI();
            client.setVisible(true);
        });
    }
}
