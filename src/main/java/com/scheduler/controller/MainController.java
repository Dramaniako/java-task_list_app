package com.scheduler.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Optional;

import com.scheduler.controller.MainController.GroupOption;
import com.scheduler.model.Anggota;
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

public class MainController {

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
    private ChoiceBox<GroupOption> kelompok;
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
    private TableColumn<Anggota, Integer> colAnggotaId; // The User ID
    @FXML
    private TableColumn<Anggota, String> colAnggotaNama; // The Name
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

        // 1. Set the Value Factory (Data Binding)
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 2. Set the Cell Factory (Visual Styling)
        colStatus.setCellFactory(column -> new javafx.scene.control.TableCell<Tugas, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle(""); // Reset style for empty cells
                } else {
                    setText(item);

                    // Check the status string and change color
                    if ("BELUM_DIKERJAKAN".equalsIgnoreCase(item)) {
                        // Red text, bold
                        setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-weight: bold;");
                    } else if ("SELESAI".equalsIgnoreCase(item)) {
                        // Green text, bold
                        setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-font-weight: bold;");
                    } else {
                        // Default black for any other status
                        setStyle("-fx-text-fill: black;");
                    }
                }
            }
        });

        colAnggotaId.setCellValueFactory(new PropertyValueFactory<>("idPengguna"));
        colAnggotaNama.setCellValueFactory(new PropertyValueFactory<>("namaPengguna")); // Matches getNamaPengguna()
        colAnggotaStatus.setCellValueFactory(new PropertyValueFactory<>("statusAnggota")); // Matches getStatusAnggota()

        // 1. Load the Groups into ChoiceBox first
        loadKelompokOptions();

        // 2. Add listener: When user changes Group in ChoiceBox, reload the table
        kelompok.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (newVal.getId() == 0) {
                    tfCurrentId.setText("-");
                } else {
                    tfCurrentId.setText(String.valueOf(newVal.getId()));
                }
                loadData();
                loadAnggota();
            }
        });

        // 3. Select the first group by default (if any exist) to trigger loadData
        if (!kelompok.getItems().isEmpty()) {
            kelompok.getSelectionModel().selectFirst();
        }
    }

    private void loadKelompokOptions() {
        // Query to find groups where the current user is a member (Anggota)
        // Adjust table names (Kelompok/Anggota) if yours are different
        String query = "SELECT kelompok.id_Kelompok, kelompok.nama_Kelompok FROM kelompok JOIN anggota ON kelompok.id_Kelompok = anggota.id_Kelompok WHERE anggota.id_Pengguna = ?";

        ObservableList<GroupOption> options = FXCollections.observableArrayList();

        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {

            options.add(new GroupOption(0, "default"));

            pstmt.setInt(1, Session.getUser().getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                options.add(new GroupOption(
                        rs.getInt("id_Kelompok"),
                        rs.getString("nama_Kelompok")
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
        GroupOption selectedGroup = kelompok.getValue();

        if (selectedGroup == null || selectedGroup.getId() == 0) {
            tableAnggota.setItems(listAnggota); // Clear table
            return;
        }

        String query = "SELECT anggota.id_Anggota, anggota.id_Pengguna, anggota.id_Kelompok, pengguna.nama_Pengguna, CASE WHEN anggota.id_Pengguna = kelompok.id_Ketua THEN 'Ketua' ELSE 'Anggota' END AS status_Anggota FROM anggota INNER JOIN pengguna ON anggota.id_Pengguna = pengguna.id_Pengguna JOIN kelompok ON anggota.id_Kelompok = kelompok.id_Kelompok WHERE anggota.id_Kelompok = ?";

        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, String.valueOf(selectedGroup.getId()));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                listAnggota.add(new Anggota(
                        rs.getInt("id_Anggota"),
                        rs.getInt("id_Kelompok"),
                        rs.getInt("id_Pengguna"),
                        rs.getString("nama_Pengguna"), // Fetched from Pengguna table
                        rs.getString("status_Anggota") // Fetched from Anggota table
                ));
            }
            tableAnggota.setItems(listAnggota);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void hapusAnggota() {
        Anggota selectedMember = tableAnggota.getSelectionModel().getSelectedItem();
        GroupOption selectedGroup = kelompok.getValue();

        if (selectedMember == null) {
            showAlert("Peringatan", "Pilih anggota yang ingin dihapus dari tabel.");
            return;
        }
        if (selectedGroup == null || selectedGroup.getId() == 0) {
            showAlert("Error", "Kelompok tidak valid.");
            return;
        }

        String sqlCheckLeader = "SELECT id_Ketua FROM Kelompok WHERE id_Kelompok = ?";
        String sqlDeleteMember = "DELETE FROM anggota WHERE id_Pengguna = ? AND id_Kelompok = ?";

        try (Connection conn = DatabaseHelper.connect()) {

            // --- STEP 1: Verify that YOU are the Leader (Ketua) ---
            int idKetua = -1;

            try (PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheckLeader)) {
                // FIX: Use .getValue().getId(), not .getId()
                pstmtCheck.setInt(1, selectedGroup.getId());

                try (ResultSet rs = pstmtCheck.executeQuery()) {
                    if (rs.next()) {
                        idKetua = rs.getInt("id_Ketua");
                    }
                }
            }

            // Logic Check: Are you the leader?
            if (idKetua != Session.getUser().getId()) {
                showAlert("Akses Ditolak", "Hanya Ketua Kelompok yang berhak mengeluarkan anggota.");
                return;
            }

            // Optional Safety: Don't let the leader delete themselves
            if (selectedMember.getIdPengguna() == idKetua) {
                showAlert("Gagal", "Anda adalah ketua. Anda tidak bisa mengeluarkan diri sendiri.");
                return;
            }

            // --- STEP 2: Execute Delete ---
            try (PreparedStatement pstmtDelete = conn.prepareStatement(sqlDeleteMember)) {
                pstmtDelete.setInt(1, selectedMember.getIdPengguna());
                pstmtDelete.setInt(2, selectedGroup.getId());

                int affectedRows = pstmtDelete.executeUpdate();

                if (affectedRows > 0) {
                    // FIX: Refresh the ANGGOTA table, not just the data/tasks table
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
        GroupOption currentKelompok = kelompok.getValue();

        if (currentKelompok == null) {
            showAlert("Peringatan", "Pilih kelompok yang ingin dihapus");
            return;
        }
        if (currentKelompok.getId() == 0) {
            showAlert("Error", "Kelompok tidak valid.");
            return;
        }

        String sqlCheckLeader = "SELECT id_Ketua FROM Kelompok WHERE id_Kelompok = ?";
        String sqlDeleteTasks = "DELETE FROM Tugas WHERE id_Kelompok = ?";
        String sqlDeleteMembers = "DELETE FROM Anggota WHERE id_Kelompok = ?";
        String sqlDeleteGroup = "DELETE FROM Kelompok WHERE id_Kelompok = ?";

        try (Connection conn = DatabaseHelper.connect()) {
            // Start Transaction (Important!)
            conn.setAutoCommit(false);

            int idKetua = -1;

            try (PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheckLeader)) {
                // FIX: Use .getValue().getId(), not .getId()
                pstmtCheck.setInt(1, currentKelompok.getId());

                try (ResultSet rs = pstmtCheck.executeQuery()) {
                    if (rs.next()) {
                        idKetua = rs.getInt("id_Ketua");
                    }
                }
            }

            // Logic Check: Are you the leader?
            if (idKetua != Session.getUser().getId()) {
                showAlert("Akses Ditolak", "Hanya Ketua Kelompok yang berhak menghapus kelompok.");
                return;
            }

            try {
                // STEP 1: Delete all Tasks for this group
                try (PreparedStatement pstmt1 = conn.prepareStatement(sqlDeleteTasks)) {
                    pstmt1.setInt(1, currentKelompok.getId());
                    pstmt1.executeUpdate();
                }

                // STEP 2: Delete all Members (Anggota) for this group
                try (PreparedStatement pstmt2 = conn.prepareStatement(sqlDeleteMembers)) {
                    pstmt2.setInt(1, currentKelompok.getId());
                    pstmt2.executeUpdate();
                }

                // STEP 3: Delete the Group (Kelompok) itself
                try (PreparedStatement pstmt3 = conn.prepareStatement(sqlDeleteGroup)) {
                    pstmt3.setInt(1, currentKelompok.getId());
                    int affected = pstmt3.executeUpdate();

                    if (affected == 0) {
                        throw new SQLException("Gagal menghapus kelompok (ID tidak ditemukan).");
                    }
                }

                // If we get here, everything worked. Commit changes.
                conn.commit();

                // Refresh UI
                loadKelompokOptions(); // Reload dropdown
                loadData();            // Clear table
                showAlert("Sukses", "Kelompok dan semua datanya berhasil dihapus.");

            } catch (SQLException e) {
                // If any step failed, undo everything
                conn.rollback();
                throw e; // Re-throw to be caught below
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Gagal menghapus kelompok: " + e.getMessage());
        }
    }

    private void loadData() {
        listTugas.clear();
        GroupOption selectedGroup = kelompok.getValue();

        String query;

        boolean isShowAll = (selectedGroup == null || selectedGroup.getId() == 0);

        if (isShowAll) {
            query = "SELECT * FROM Tugas WHERE id_Pengguna = ?";
        } else {
            query = "SELECT * FROM Tugas WHERE id_Kelompok = ?";
        }

        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (isShowAll) {
                // Mapping to: WHERE id_Pengguna = ?
                pstmt.setInt(1, Session.getUser().getId());
            } else {
                // Mapping to: WHERE id_Kelompok = ?
                // WARNING: In your previous code, id_Kelompok was a String. 
                // If your GroupOption.getId() returns an int, verify your DB column type.
                // If DB column is VARCHAR, use setString:
                pstmt.setString(1, String.valueOf(selectedGroup.getId()));
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
    private void handleTambah() {
        GroupOption selectedGroup = kelompok.getValue();
        if (selectedGroup == null || selectedGroup.getId() == 0) {
            showAlert("Peringatan", "Silakan pilih spesifik Kelompok di dropdown atas (bukan 'Semua Tugas') untuk menambahkan tugas ke dalamnya.");
            return;
        }

        String query = "INSERT INTO Tugas (nama_Tugas, deskripsi_Tugas, tenggat, tanggal_Mulai, status, id_Pengguna, id_Kelompok) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseHelper.connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, txtNama.getText());
            pstmt.setString(2, txtDeskripsi.getText());

            if (dpTenggat.getValue() == null) {
                showAlert("Error", "Mohon isi tanggal tenggat.");
                return;
            }

            pstmt.setDate(3, java.sql.Date.valueOf(dpTenggat.getValue()));
            pstmt.setDate(4, java.sql.Date.valueOf(LocalDate.now()));
            pstmt.setString(5, "BELUM_DIKERJAKAN");

            // Set User ID
            pstmt.setInt(6, Session.getUser().getId());

            // Set Group ID from ChoiceBox
            pstmt.setInt(7, selectedGroup.getId());

            pstmt.executeUpdate();
            loadData();
            clearForm();
            showAlert("Sukses", "Tugas berhasil ditambahkan ke " + selectedGroup.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Gagal menambah tugas: " + e.getMessage());
        }
    }

    // Fungsi DELETE: Menghapus tugas [cite: 90]
    @FXML
    private void handleHapus() {
        Tugas selected = tableTugas.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String query = "DELETE FROM Tugas WHERE id_Tugas = ?";
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

            String query = "UPDATE Tugas SET status = ? WHERE id_Tugas = ?";
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

    private void clearForm() {
        txtNama.clear();
        txtDeskripsi.clear();
        dpTenggat.setValue(null);
    }

    public static class GroupOption {

        private final int id;
        private final String name;

        public GroupOption(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return name;
        } // This is what shows in the ChoiceBox
    }

    @FXML
    private void handleKelompok() {
        // 1. Create the Alert
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Select Action");
        alert.setHeaderText("Choose your connection mode");
        alert.setContentText("Do you want to create a new session or join an existing one?");

        // 2. Define Custom Buttons
        ButtonType buttonCreate = new ButtonType("Buat");
        ButtonType buttonJoin = new ButtonType("Join");
        ButtonType buttonCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

        // 3. Set the buttons on the Alert
        alert.getButtonTypes().setAll(buttonCreate, buttonJoin, buttonCancel);

        // 4. Show and Wait for response
        Optional<ButtonType> result = alert.showAndWait();

        // 5. Handle the result
        if (result.get() == buttonCreate) {
            handleBuatKelompok();
        } else if (result.get() == buttonJoin) {
            handleJoin();
        } else {
            System.out.println("User chose Cancel");
            // Close dialog or do nothing
        }
    }

    @FXML
    private void handleHapusKelompok() {
        // 1. Create the Alert
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Select Action");
        alert.setHeaderText("Choose your Action");
        alert.setContentText("Yakin mau menghapus kelompok?");

        // 2. Define Custom Buttons
        ButtonType buttonDelete = new ButtonType("Ya");
        ButtonType buttonCancel = new ButtonType("Tidak", ButtonData.CANCEL_CLOSE);

        // 3. Set the buttons on the Alert
        alert.getButtonTypes().setAll(buttonDelete, buttonCancel);

        // 4. Show and Wait for response
        Optional<ButtonType> result = alert.showAndWait();

        // 5. Handle the result
        if (result.get() == buttonDelete) {
            hapusKelompok();
        } else {
            System.out.println("User chose Cancel");
            // Close dialog or do nothing
        }
    }

    private void handleBuatKelompok() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Buat Kelompok Baru");
        dialog.setHeaderText("Masukkan nama kelompok baru:");
        dialog.setContentText("Nama:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(namaKelompok -> {
            if (namaKelompok.trim().isEmpty()) {
                return;
            }

            String sqlCheckParams = "SELECT 1 FROM Kelompok WHERE nama_Kelompok = ?";

            String sqlInsertGroup = "INSERT INTO Kelompok (nama_Kelompok, id_Ketua) VALUES (?,?)";
            // NEW QUERY: Insert into Anggota table
            String sqlInsertMember = "INSERT INTO Anggota (id_Pengguna, id_Kelompok) VALUES (?, ?)";

            try (Connection conn = DatabaseHelper.connect()) {
                conn.setAutoCommit(false); // Start Transaction

                try (PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheckParams)) {
                    pstmtCheck.setString(1, namaKelompok);
                    try (ResultSet rs = pstmtCheck.executeQuery()) {
                        if (rs.next()) {
                            // If we found a row, the name exists.
                            showAlert("Gagal", "Nama kelompok '" + namaKelompok + "' sudah digunakan.");
                            return; // STOP HERE. Do not insert.
                        }
                    }
                }

                // 1. Create the Group
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

                // 2. Add User to the new Group (in Anggota table)
                if (newGroupId != -1) {
                    try (PreparedStatement pstmtMember = conn.prepareStatement(sqlInsertMember)) {
                        pstmtMember.setInt(1, Session.getUser().getId()); // User ID
                        pstmtMember.setInt(2, newGroupId);                // New Group ID
                        pstmtMember.executeUpdate();
                    }
                    conn.commit(); // Save everything

                    // Refresh UI
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

    private void handleJoin() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Join Kelompok");
        dialog.setHeaderText("Masukkan ID Kelompok:");
        dialog.setContentText("ID:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(id_Kelompok -> {
            if (id_Kelompok.trim().isEmpty()) {
                return;
            }
            String sqlCheckParams = "SELECT 1 FROM Anggota WHERE id_Kelompok = ? AND id_Pengguna = ?";

            String sqlNamaKelompok = "SELECT kelompok.nama_Kelompok FROM kelompok INNER JOIN anggota ON kelompok.id_Kelompok = anggota.id_Kelompok WHERE anggota.id_Pengguna = ? AND anggota.id_Kelompok = ?";

            // NEW QUERY: Insert into Anggota table
            String sqlInsertMember = "INSERT INTO Anggota (id_Pengguna, id_Kelompok) VALUES (?, ?)";

            String namaKelompok = "";

            try (Connection conn = DatabaseHelper.connect()) {
                conn.setAutoCommit(false); // Start Transaction

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
                            // If we found a row, the name exists.
                            showAlert("Gagal", "Anda sudah masuk kelompok '" + namaKelompok + "'");
                            return; // STOP HERE. Do not insert.
                        }
                    }
                }

                try (PreparedStatement pstmtMember = conn.prepareStatement(sqlInsertMember)) {
                    pstmtMember.setInt(1, Session.getUser().getId());
                    pstmtMember.setString(2, id_Kelompok);
                    pstmtMember.executeUpdate();
                }
                conn.commit(); // Save everything

                // Refresh UI
                loadKelompokOptions();

                showAlert("Sukses", "Masuk Ke Kelompok " + namaKelompok + " Berhasil");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Gagal membuat kelompok: " + e.getMessage());
            }
        });
    }

    // Helper method for alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
