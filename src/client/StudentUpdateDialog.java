package client;

import server.Student;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class StudentUpdateDialog extends JDialog {
    private JTextField txtMaSV;
    private JTextField txtHoTen;
    private JTextField txtNamSinh;
    private JTextField txtQueQuan;
    private JTextField txtGPA;
    private JButton btnSave;
    private JButton btnCancel;
    private boolean updated = false;
    private Student student;

    public StudentUpdateDialog(Frame parent, Student student) {
        super(parent, "Cập nhật thông tin sinh viên", true);
        this.student = student;
        initComponents();
        setValues();
    }

    private void initComponents() {
        setLayout(new GridLayout(6, 2, 10, 10));

        add(new JLabel("Mã SV:"));
        txtMaSV = new JTextField();
        add(txtMaSV);

        add(new JLabel("Họ Tên:"));
        txtHoTen = new JTextField();
        add(txtHoTen);

        add(new JLabel("Năm Sinh:"));
        txtNamSinh = new JTextField();
        add(txtNamSinh);

        add(new JLabel("Quê Quán:"));
        txtQueQuan = new JTextField();
        add(txtQueQuan);

        add(new JLabel("GPA:"));
        txtGPA = new JTextField();
        add(txtGPA);

        btnSave = new JButton("Lưu");
        btnCancel = new JButton("Hủy");
        add(btnSave);
        add(btnCancel);

        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateInput()) {
                    updateStudent();
                    updated = true;
                    dispose();
                }
            }
        });

        btnCancel.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(getParent());
    }

    private void setValues() {
        txtMaSV.setText(student.getMaSV());
        txtHoTen.setText(student.getHoTen());
        txtNamSinh.setText(String.valueOf(student.getNamSinh()));
        txtQueQuan.setText(student.getQueQuan());
        txtGPA.setText(String.valueOf(student.getGPA()));
    }

    private boolean validateInput() {
        if (txtMaSV.getText().trim().isEmpty() ||
                txtHoTen.getText().trim().isEmpty() ||
                txtNamSinh.getText().trim().isEmpty() ||
                txtGPA.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin.");
            return false;
        }
        try {
            Integer.parseInt(txtNamSinh.getText().trim());
            Float.parseFloat(txtGPA.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Năm sinh và GPA phải là số.");
            return false;
        }
        return true;
    }

    private void updateStudent() {
        student.setMaSV(txtMaSV.getText().trim());
        student.setHoTen(txtHoTen.getText().trim());
        student.setNamSinh(Integer.parseInt(txtNamSinh.getText().trim()));
        student.setQueQuan(txtQueQuan.getText().trim());
        student.setGPA(Float.parseFloat(txtGPA.getText().trim()));
    }

    public boolean isUpdated() {
        return updated;
    }

    public Student getStudent() {
        return student;
    }
}
