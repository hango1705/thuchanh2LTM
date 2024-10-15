package client;

public class Student {
    private int idSV;
    private String maSV;
    private String hoTen;
    private int namSinh;
    private String queQuan;
    private float GPA;
    
    public Student() {}

    public Student(int idSV, String maSV, String hoTen, int namSinh, String queQuan, float GPA) {
        this.idSV = idSV;
        this.maSV = maSV;
        this.hoTen = hoTen;
        this.namSinh = namSinh;
        this.queQuan = queQuan;
        this.GPA = GPA;
    }

    public int getIdSV() { return idSV; }
    public void setIdSV(int idSV) { this.idSV = idSV; }

    public String getMaSV() { return maSV; }
    public void setMaSV(String maSV) { this.maSV = maSV; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public int getNamSinh() { return namSinh; }
    public void setNamSinh(int namSinh) { this.namSinh = namSinh; }

    public String getQueQuan() { return queQuan; }
    public void setQueQuan(String queQuan) { this.queQuan = queQuan; }

    public float getGPA() { return GPA; }
    public void setGPA(float GPA) { this.GPA = GPA; }
}
