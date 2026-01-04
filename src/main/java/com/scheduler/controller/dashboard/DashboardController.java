package com.scheduler.controller.dashboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import com.scheduler.model.Anggota;
import com.scheduler.model.Kelompok;
import com.scheduler.model.Session;
import com.scheduler.model.Tugas;
import com.scheduler.util.DatabaseHelper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;

public class DashboardController {

    @FXML
    private TextField txtNama;
    @FXML
    private TextArea txtDeskripsi;
    @FXML
    private DatePicker dpTenggat;
    @FXML
    private TableView<Tugas> tableTugas;
    @FXML
    private TableView<Anggota> tableAnggota;
    @FXML
    private ChoiceBox<Kelompok> kelompok;
    @FXML
    private TableColumn<Tugas, String> colNama;
    @FXML
    private TableColumn<Tugas, Text> colDeskripsi;
    @FXML
    private TableColumn<Tugas, LocalDate> colTenggat;
    @FXML
    private TableColumn<Tugas, String> colStatus;
    @FXML
    private TextField tfCurrentId;
    @FXML
    private TableColumn<Anggota, Integer> colAnggotaId;
    @FXML
    private TableColumn<Anggota, String> colAnggotaNama;
    @FXML
    private TableColumn<Anggota, String> colAnggotaStatus;

    private ObservableList<Tugas> listTugas = FXCollections.observableArrayList();
    private ObservableList<Anggota> listAnggota = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        if (Session.getUser() == null) {
            System.out.println("User belum login!");
            return;
        }

        colNama.setCellValueFactory(new PropertyValueFactory<>("namaTugas"));
        colDeskripsi.setCellValueFactory(new PropertyValueFactory<>("deskripsi"));
        colTenggat.setCellValueFactory(new PropertyValueFactory<>("tenggat"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colStatus.setCellFactory(column -> new javafx.scene.control.TableCell<Tugas, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);

                    if ("BELUM_DIKERJAKAN".equalsIgnoreCase(item)) {
                        setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-weight: bold;");
                    } else if ("SELESAI".equalsIgnoreCase(item)) {
                        setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: black;");
                    }
                }
            }
        });

        colAnggotaId.setCellValueFactory(new PropertyValueFactory<>("idPengguna"));
        colAnggotaNama.setCellValueFactory(new PropertyValueFactory<>("namaPengguna"));
        colAnggotaStatus.setCellValueFactory(new PropertyValueFactory<>("statusAnggota"));

        cekTenggat();

        loadKelompokOptions();

        kelompok.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (newVal.getIdKelompok() == 0) {
                    tfCurrentId.setText("-");
                } else {
                    tfCurrentId.setText(String.valueOf(newVal.getIdKelompok()));
                }
                loadData();
                loadAnggota();
            }
        });

        if (!kelompok.getItems().isEmpty()) {
            kelompok.getSelectionModel().selectFirst();
        }
    }

    private void loadKelompokOptions() {
        String query = "SELECT kelompok.id_Kelompok, kelompok.nama_Kelompok, kelompok.id_Ketua FROM kelompok JOIN anggota ON kelompok.id_Kelompok = anggota.id_Kelompok WHERE anggota.id_Pengguna = ?";

        ObservableList<Kelompok> options = FXCollections.observableArrayList();

        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {

            options.add(new Kelompok(0, "default", 0));

            pstmt.setInt(1, Session.getUser().getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                options.add(new Kelompok(
                        rs.getInt("id_Kelompok"),
                        rs.getString("nama_Kelompok"),
                        rs.getInt("id_Ketua")
                ));
            }
            kelompok.setItems(options);

            if (!options.isEmpty()) {
                kelompok.getSelectionModel().selectFirst();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAnggota() {
        listAnggota.clear();
        Kelompok selectedGroup = kelompok.getValue();

        if (selectedGroup == null || selectedGroup.getIdKelompok() == 0) {
            tableAnggota.setItems(listAnggota);
            return;
        }

        String query = "SELECT anggota.id_Anggota, anggota.id_Pengguna, anggota.id_Kelompok, pengguna.nama_Pengguna, CASE WHEN anggota.id_Pengguna = kelompok.id_Ketua THEN 'Ketua' ELSE 'Anggota' END AS status_Anggota FROM anggota INNER JOIN pengguna ON anggota.id_Pengguna = pengguna.id_Pengguna JOIN kelompok ON anggota.id_Kelompok = kelompok.id_Kelompok WHERE anggota.id_Kelompok = ?";

        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, String.valueOf(selectedGroup.getIdKelompok()));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                listAnggota.add(new Anggota(
                        rs.getInt("id_Anggota"),
                        rs.getInt("id_Kelompok"),
                        rs.getInt("id_Pengguna"),
                        rs.getString("nama_Pengguna"),
                        rs.getString("status_Anggota")
                ));
            }
            tableAnggota.setItems(listAnggota);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        listTugas.clear();
        Kelompok selectedGroup = kelompok.getValue();

        String query;

        boolean isShowAll = (selectedGroup == null || selectedGroup.getIdKelompok() == 0);

        if (isShowAll) {
            query = "SELECT * FROM tugas WHERE id_Pengguna = ?";
        } else {
            query = "SELECT * FROM tugas WHERE id_Kelompok = ?";
        }

        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (isShowAll) {
                pstmt.setInt(1, Session.getUser().getId());
            } else {
                pstmt.setString(1, String.valueOf(selectedGroup.getIdKelompok()));
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                listTugas.add(new Tugas(
                        rs.getInt("id_Tugas"),
                        rs.getString("nama_Tugas"),
                        rs.getString("deskripsi_Tugas"),
                        rs.getDate("tenggat").toLocalDate(),
                        rs.getString("status"),
                        rs.getString("id_Kelompok")
                ));
            }
            tableTugas.setItems(listTugas);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleTambahUpdate() {
        String query = "";
        String alert = "";

        Kelompok selectedGroup = kelompok.getValue();
        if (selectedGroup == null || selectedGroup.getIdKelompok() == 0) {
            showAlert("Peringatan", "Silakan pilih spesifik Kelompok di dropdown atas (bukan 'Semua Tugas') untuk menambahkan tugas ke dalamnya.");
            return;
        }

        Tugas selected = tableTugas.getSelectionModel().getSelectedItem();
        if (selected != null) {
            query = "UPDATE tugas SET nama_Tugas = ?, deskripsi_Tugas = ?, tenggat = ? WHERE id_Tugas = ?";
        } else {
            query = "INSERT INTO tugas (nama_Tugas, deskripsi_Tugas, tenggat, tanggal_Mulai, status, id_Pengguna, id_Kelompok) VALUES (?, ?, ?, ?, ?, ?, ?)";
        }

        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            if (selected != null) {
                alert = "Mengupdate";

                pstmt.setString(1, txtNama.getText());
                pstmt.setString(2, txtDeskripsi.getText());

                if (dpTenggat.getValue() == null) {
                    showAlert("Error", "Mohon isi tanggal tenggat.");
                    return;
                }

                pstmt.setDate(3, java.sql.Date.valueOf(dpTenggat.getValue()));
                pstmt.setInt(4, selected.getId());

                pstmt.executeUpdate();
                loadData();
                clearForm();
                showAlert("Sukses", "Tugas berhasil diupdate");
            } else {
                alert = "Menambah";

                pstmt.setString(1, txtNama.getText());
                pstmt.setString(2, txtDeskripsi.getText());

                if (dpTenggat.getValue() == null) {
                    showAlert("Error", "Mohon isi tanggal tenggat.");
                    return;
                }

                pstmt.setDate(3, java.sql.Date.valueOf(dpTenggat.getValue()));
                pstmt.setDate(4, java.sql.Date.valueOf(LocalDate.now()));
                pstmt.setString(5, "BELUM_DIKERJAKAN");
                pstmt.setInt(6, Session.getUser().getId());
                pstmt.setInt(7, selectedGroup.getIdKelompok());

                pstmt.executeUpdate();
                loadData();
                clearForm();
                showAlert("Sukses", "Tugas berhasil ditambahkan ke " + selectedGroup.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Gagal " + alert + " tugas: " + e.getMessage());
        }
    }

    @FXML
    private void handleHapus() {
        Tugas selected = tableTugas.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String query = "DELETE FROM tugas WHERE id_Tugas = ?";
            try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, selected.getId());
                pstmt.executeUpdate();
                loadData();
                showAlert("Sukses", "Tugas berhasil dihapus!");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Gagal menghapus tugas: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleStatus() {
        Tugas selected = tableTugas.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String newStatus;

            if ("SELESAI".equalsIgnoreCase(selected.getStatus())) {
                newStatus = "BELUM_DIKERJAKAN";
            } else {
                newStatus = "SELESAI";
            }

            String query = "UPDATE tugas SET status = ? WHERE id_Tugas = ?";
            try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, newStatus);
                pstmt.setInt(2, selected.getId());
                pstmt.executeUpdate();
                loadData();
                showAlert("Sukses", "Status tugas diganti");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Gagal mengganti status: " + e.getMessage());
            }

        }
    }

    @FXML
    private void hapusAnggota() {
        Anggota selectedMember = tableAnggota.getSelectionModel().getSelectedItem();
        Kelompok selectedGroup = kelompok.getValue();

        if (selectedMember == null) {
            showAlert("Peringatan", "Pilih anggota yang ingin dihapus dari tabel.");
            return;
        }

        String sqlCheckLeader = "SELECT id_Ketua FROM kelompok WHERE id_Kelompok = ?";
        String sqlDeleteMember = "DELETE FROM anggota WHERE id_Pengguna = ? AND id_Kelompok = ?";

        try (Connection conn = DatabaseHelper.connect()) {

            int idKetua = -1;

            try (PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheckLeader)) {
                pstmtCheck.setInt(1, selectedGroup.getIdKelompok());

                try (ResultSet rs = pstmtCheck.executeQuery()) {
                    if (rs.next()) {
                        idKetua = rs.getInt("id_Ketua");
                    }
                }
            }

            if (idKetua != Session.getUser().getId()) {
                showAlert("Akses Ditolak", "Hanya Ketua Kelompok yang berhak mengeluarkan anggota.");
                return;
            }

            if (selectedMember.getIdAnggota() == idKetua) {
                showAlert("Gagal", "Anda adalah ketua. Anda tidak bisa mengeluarkan diri sendiri.");
                return;
            }

            try (PreparedStatement pstmtDelete = conn.prepareStatement(sqlDeleteMember)) {
                pstmtDelete.setInt(1, selectedMember.getIdPengguna());
                pstmtDelete.setInt(2, selectedGroup.getIdKelompok());

                int affectedRows = pstmtDelete.executeUpdate();

                if (affectedRows > 0) {
                    loadAnggota();
                    showAlert("Sukses", "Anggota berhasil dikeluarkan!");
                } else {
                    showAlert("Error", "Gagal menghapus (Data mungkin tidak ditemukan).");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database error: " + e.getMessage());
        }
    }

    private void hapusKelompok() {
        Kelompok currentKelompok = kelompok.getValue();

        if (currentKelompok == null) {
            showAlert("Peringatan", "Pilih kelompok yang ingin dihapus");
            return;
        }
        if (currentKelompok.getIdKelompok() == 0) {
            showAlert("Error", "Kelompok tidak valid.");
            return;
        }

        String sqlCheckLeader = "SELECT id_Ketua FROM kelompok WHERE id_Kelompok = ?";
        String sqlDeleteTasks = "DELETE FROM tugas WHERE id_Kelompok = ?";
        String sqlDeleteMembers = "DELETE FROM anggota WHERE id_Kelompok = ?";
        String sqlDeleteGroup = "DELETE FROM kelompok WHERE id_Kelompok = ?";

        try (Connection conn = DatabaseHelper.connect()) {
            conn.setAutoCommit(false);

            int idKetua = -1;

            try (PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheckLeader)) {
                pstmtCheck.setInt(1, currentKelompok.getIdKelompok());

                try (ResultSet rs = pstmtCheck.executeQuery()) {
                    if (rs.next()) {
                        idKetua = rs.getInt("id_Ketua");
                    }
                }
            }

            if (idKetua != Session.getUser().getId()) {
                showAlert("Akses Ditolak", "Hanya Ketua Kelompok yang berhak menghapus kelompok.");
                return;
            }

            try {
                try (PreparedStatement pstmt1 = conn.prepareStatement(sqlDeleteTasks)) {
                    pstmt1.setInt(1, currentKelompok.getIdKelompok());
                    pstmt1.executeUpdate();
                }

                try (PreparedStatement pstmt2 = conn.prepareStatement(sqlDeleteMembers)) {
                    pstmt2.setInt(1, currentKelompok.getIdKelompok());
                    pstmt2.executeUpdate();
                }

                try (PreparedStatement pstmt3 = conn.prepareStatement(sqlDeleteGroup)) {
                    pstmt3.setInt(1, currentKelompok.getIdKelompok());
                    int affected = pstmt3.executeUpdate();

                    if (affected == 0) {
                        throw new SQLException("Gagal menghapus kelompok (ID tidak ditemukan).");
                    }
                }

                conn.commit();

                loadKelompokOptions();
                loadData();
                showAlert("Sukses", "Kelompok dan semua datanya berhasil dihapus.");

            } catch (SQLException e) {

                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Gagal menghapus kelompok: " + e.getMessage());
        }
    }

    @FXML
    private void handleKelompok() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Select Action");
        alert.setHeaderText("Choose your connection mode");
        alert.setContentText("Do you want to create a new session or join an existing one?");

        ButtonType buttonCreate = new ButtonType("Buat");
        ButtonType buttonJoin = new ButtonType("Join");
        ButtonType buttonCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonCreate, buttonJoin, buttonCancel);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == buttonCreate) {
            BuatKelompok();
        } else if (result.get() == buttonJoin) {
            JoinKelompok();
        } else {
            System.out.println("User chose Cancel");
        }
    }

    @FXML
    private void handleHapusKelompok() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Select Action");
        alert.setHeaderText("Choose your Action");
        alert.setContentText("Yakin mau menghapus kelompok?");

        ButtonType buttonDelete = new ButtonType("Ya");
        ButtonType buttonCancel = new ButtonType("Tidak", ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonDelete, buttonCancel);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == buttonDelete) {
            hapusKelompok();
        } else {
            System.out.println("User chose Cancel");
        }
    }

    private void BuatKelompok() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Buat Kelompok Baru");
        dialog.setHeaderText("Masukkan nama kelompok baru:");
        dialog.setContentText("Nama:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(namaKelompok -> {
            if (namaKelompok.trim().isEmpty()) {
                return;
            }

            String sqlCheckParams = "SELECT 1 FROM kelompok WHERE nama_Kelompok = ?";

            String sqlInsertGroup = "INSERT INTO kelompok (nama_Kelompok, id_Ketua) VALUES (?,?)";
            String sqlInsertMember = "INSERT INTO anggota (id_Pengguna, id_Kelompok) VALUES (?, ?)";

            try (Connection conn = DatabaseHelper.connect()) {
                conn.setAutoCommit(false);

                try (PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheckParams)) {
                    pstmtCheck.setString(1, namaKelompok);
                    try (ResultSet rs = pstmtCheck.executeQuery()) {
                        if (rs.next()) {
                            showAlert("Gagal", "Nama kelompok '" + namaKelompok + "' sudah digunakan.");
                            return;
                        }
                    }
                }

                int newGroupId = -1;
                try (PreparedStatement pstmtGroup = conn.prepareStatement(sqlInsertGroup, Statement.RETURN_GENERATED_KEYS)) {
                    pstmtGroup.setString(1, namaKelompok);
                    pstmtGroup.setInt(2, Session.getUser().getId());
                    int affectedRows = pstmtGroup.executeUpdate();

                    if (affectedRows > 0) {
                        try (ResultSet generatedKeys = pstmtGroup.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                newGroupId = generatedKeys.getInt(1);
                            }
                        }
                    }
                }

                if (newGroupId != -1) {
                    try (PreparedStatement pstmtMember = conn.prepareStatement(sqlInsertMember)) {
                        pstmtMember.setInt(1, Session.getUser().getId());
                        pstmtMember.setInt(2, newGroupId);
                        pstmtMember.executeUpdate();
                    }
                    conn.commit();

                    loadKelompokOptions();
                    showAlert("Sukses", "Kelompok '" + namaKelompok + "' berhasil dibuat!");
                } else {
                    conn.rollback();
                    showAlert("Error", "Gagal mendapatkan ID kelompok baru.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Gagal membuat kelompok: " + e.getMessage());
            }
        });
    }

    private void JoinKelompok() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Join Kelompok");
        dialog.setHeaderText("Masukkan ID Kelompok:");
        dialog.setContentText("ID:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(id_Kelompok -> {
            if (id_Kelompok.trim().isEmpty()) {
                return;
            }
            String sqlCheckParams = "SELECT 1 FROM anggota WHERE id_Kelompok = ? AND id_Pengguna = ?";

            String sqlNamaKelompok = "SELECT kelompok.nama_Kelompok FROM kelompok INNER JOIN anggota ON kelompok.id_Kelompok = anggota.id_Kelompok WHERE anggota.id_Pengguna = ? AND anggota.id_Kelompok = ?";

            String sqlInsertMember = "INSERT INTO anggota (id_Pengguna, id_Kelompok) VALUES (?, ?)";

            String namaKelompok = "";

            try (Connection conn = DatabaseHelper.connect()) {
                conn.setAutoCommit(false);

                try (PreparedStatement pstmtKelompok = conn.prepareStatement(sqlNamaKelompok)) {
                    pstmtKelompok.setInt(1, Session.getUser().getId());
                    pstmtKelompok.setString(2, id_Kelompok);

                    try (ResultSet rs = pstmtKelompok.executeQuery()) {
                        if (rs.next()) {
                            namaKelompok = rs.getString("nama_Kelompok");
                        }
                    }
                }

                try (PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheckParams)) {
                    pstmtCheck.setString(1, id_Kelompok);
                    pstmtCheck.setInt(2, Session.getUser().getId());
                    try (ResultSet rs = pstmtCheck.executeQuery()) {
                        if (rs.next()) {
                            showAlert("Gagal", "Anda sudah masuk kelompok '" + namaKelompok + "'");
                            return;
                        }
                    }
                }

                try (PreparedStatement pstmtMember = conn.prepareStatement(sqlInsertMember)) {
                    pstmtMember.setInt(1, Session.getUser().getId());
                    pstmtMember.setString(2, id_Kelompok);
                    pstmtMember.executeUpdate();
                }
                conn.commit();

                loadKelompokOptions();

                showAlert("Sukses", "Masuk Ke Kelompok " + namaKelompok + " Berhasil");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Gagal membuat kelompok: " + e.getMessage());
            }
        });
    }

    private void cekTenggat() {
        int tugasUrgent = 0;
        LocalDate today = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String nowTanggal = today.format(formatter);

        String query = "SELECT COUNT(*) AS tugasUrgent FROM tugas WHERE id_Pengguna = ? AND tenggat <= ?";

        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, Session.getUser().getId());
            pstmt.setString(2, nowTanggal);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                tugasUrgent = rs.getInt("tugasUrgent");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (tugasUrgent > 0) {
            showAlert("Alert", tugasUrgent + " Tugas memiliki tenggat dekat atau sudah lewat!");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearForm() {
        txtNama.clear();
        txtDeskripsi.clear();
        dpTenggat.setValue(null);
    }
}
