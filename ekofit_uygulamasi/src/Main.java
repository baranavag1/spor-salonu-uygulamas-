import java.sql.*;
import java.util.Random;
import java.util.Scanner;

public class Main {
    static Scanner scanner = new Scanner(System.in);
    static String[] sporlar = {"Yürüyüş", "Koşu", "Bisiklet", "Yüzme", "Fonksiyonel Antrenman", "Kuvvet Antrenmanı", "HIIT"};
    static final String URL = "jdbc:mysql://localhost:3306/ekofit";
    static final String USER = "root";
    static final String PASSWORD = ""; // Şifren varsa buraya yaz

    public static void main(String[] args) {
        anaMenu();
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Bazı sürümler için gerekebilir
        } catch (ClassNotFoundException e) {
            System.out.println("Driver bulunamadı: " + e.getMessage());
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void anaMenu() {
        while (true) {
            System.out.println("\n=== Ekofit Spor Salonu ===");
            System.out.println("1- Giriş Yap");
            System.out.println("2- Üye Ol");
            System.out.println("0- Çıkış");
            System.out.print("Seçiminiz: ");
            String secim = scanner.nextLine();

            switch (secim) {
                case "1":
                    girisYap();
                    break;
                case "2":
                    uyeOl();
                    break;
                case "0":
                    System.out.println("Çıkış yapılıyor...");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Geçersiz seçim. Lütfen tekrar deneyin.");
            }
        }
    }

    public static void girisYap() {
        System.out.print("Öğrenci numarası: ");
        String ogrNo = scanner.nextLine();
        System.out.print("Şifre: ");
        String sifre = scanner.nextLine();

        try (Connection conn = getConnection()) {
            String sql = "SELECT * FROM uyeler WHERE ogr_no = ? AND sifre = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, ogrNo);
            stmt.setString(2, sifre);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("Giriş başarılı! Hoş geldiniz, " + rs.getString("isim"));
                menu(ogrNo);
            } else {
                System.out.println("Giriş başarısız. Bilgilerinizi kontrol edin.");
            }
        } catch (SQLException e) {
            System.out.println("Giriş sırasında hata: " + e.getMessage());
        }
    }

    public static void uyeOl() {
        System.out.print("İsim: ");
        String isim = scanner.nextLine();
        System.out.print("Öğrenci numarası: ");
        String ogrNo = scanner.nextLine();
        System.out.print("Şifre: ");
        String sifre = scanner.nextLine();

        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO uyeler (isim, ogr_no, sifre) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, isim);
            stmt.setString(2, ogrNo);
            stmt.setString(3, sifre);
            stmt.executeUpdate();
            System.out.println("Üyelik başarıyla oluşturuldu.");
        } catch (SQLException e) {
            System.out.println("Kayıt sırasında hata: " + e.getMessage());
        }
    }

    public static void menu(String ogrNo) {
        while (true) {
            System.out.println("\n=== Kullanıcı Menüsü ===");
            System.out.println("1- Kalori Hesapla");
            System.out.println("2- Spor Önerisi Yap");
            System.out.println("3- Haftalık Spor Programı Öner");
            System.out.println("0- Çıkış");
            System.out.print("Seçiminiz: ");
            String secim = scanner.nextLine();

            switch (secim) {
                case "1":
                    System.out.print("Egzersiz süresi (dakika): ");
                    int dakika = scanner.nextInt();
                    scanner.nextLine(); // buffer temizle
                    int kalori = kaloriHesapla(dakika);
                    System.out.println("Yakılan kalori: " + kalori);
                    guncelleKalori(ogrNo, kalori);
                    break;
                case "2":
                    System.out.print("Yakılan kalori miktarı: ");
                    int girilenKalori = scanner.nextInt();
                    scanner.nextLine(); // buffer temizle
                    sporOnerisiYap(girilenKalori);
                    break;
                case "3":
                    haftalikPlaniGoster();
                    break;
                case "0":
                    System.out.println("Çıkış yapılıyor...");
                    return;
                default:
                    System.out.println("Geçersiz seçim. Tekrar deneyin.");
            }
        }
    }

    public static void haftalikPlaniGoster() {
        String[] gunler = {"Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar"};
        for (String gun : gunler) {
            String spor = rastgeleSporSec();
            System.out.println(gun + " -> " + spor);
        }
    }

    public static void sporOnerisiYap(int kalori) {
        if (kalori < 0) {
            System.out.println("Negatif değer girilemez!");
        } else if (kalori <= 100) {
            System.out.println("Önerilen spor: Yürüyüş");
        } else if (kalori <= 200) {
            System.out.println("Önerilen spor: Koşu");
        } else if (kalori <= 300) {
            System.out.println("Önerilen spor: Bisiklet");
        } else if (kalori <= 400) {
            System.out.println("Önerilen spor: Yüzme");
        } else if (kalori <= 500) {
            System.out.println("Önerilen spor: Fonksiyonel Antrenman");
        } else if (kalori <= 600) {
            System.out.println("Önerilen spor: Kuvvet Antrenmanı");
        } else {
            System.out.println("Önerilen spor: HIIT");
        }
    }

    public static int kaloriHesapla(int dakika) {
        return dakika * 5;
    }

    public static String rastgeleSporSec() {
        Random random = new Random();
        int index = random.nextInt(sporlar.length);
        return sporlar[index];
    }

    public static void guncelleKalori(String ogrNo, int kalori) {
        try (Connection conn = getConnection()) {
            String sql = "UPDATE uyeler SET kalori = ? WHERE ogr_no = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, kalori);
            stmt.setString(2, ogrNo);
            stmt.executeUpdate();
            System.out.println("Kalori bilgisi güncellendi.");
        } catch (SQLException e) {
            System.out.println("Kalori güncelleme hatası: " + e.getMessage());
        }
    }
}