package com.example.libraryarchitecture;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Uri pickedFolderUri = null;

    private Button btnPickFolder, btnCheck;
    private TextView txtResult;

    private final ActivityResultLauncher<Intent> folderPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            pickedFolderUri = result.getData().getData();
                            getContentResolver().takePersistableUriPermission(
                                    pickedFolderUri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            );
                            txtResult.setText("Picked folder: " + pickedFolderUri);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPickFolder = findViewById(R.id.btnPickFolder);
        btnCheck = findViewById(R.id.btnCheck);
        txtResult = findViewById(R.id.txtResult);

        btnPickFolder.setOnClickListener(v -> openFolderPicker());

        btnCheck.setOnClickListener(v -> {
            if (pickedFolderUri == null) {
                txtResult.setText("Please pick a folder first.");
                return;
            }

            StringBuilder sb = new StringBuilder();
            DocumentFile pickedDir = DocumentFile.fromTreeUri(this, pickedFolderUri);

            if (pickedDir != null && pickedDir.isDirectory()) {
                int count = 0;

                sb.append(String.format("%-30s %-20s\n", "File", "ArchType"));
                sb.append(String.format("%-30s %-20s\n", "====", "======="));

                for (DocumentFile file : pickedDir.listFiles()) {
                    if (file.isFile() && file.getName() != null && file.getName().endsWith(".so")) {
                        String arch = detectAbi(file.getUri());
                        sb.append(String.format("%-30s %-20s\n", file.getName(), arch));
                        count++;
                    }
                }

                // prepend final count
                sb.insert(0, "Total number of libraries: " + count + "\n\n");
            }

            txtResult.setText(sb.toString());
        });
    }

    private void openFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        folderPickerLauncher.launch(intent);
    }

    private String detectAbi(Uri uri) {
        try (InputStream is = getContentResolver().openInputStream(uri)) {
            if (is == null) return "Error";
            byte[] header = new byte[20];
            if (is.read(header) < 20) return "Unknown";

            // ELF magic check
            if (header[0] != 0x7f || header[1] != 'E' || header[2] != 'L' || header[3] != 'F') {
                return "Not ELF";
            }

            // Endianness (EI_DATA at offset 5)
            int eiData = header[5] & 0xFF;

            int e_machine;
            if (eiData == 1) { // little endian
                e_machine = (header[19] & 0xFF) << 8 | (header[18] & 0xFF);
            } else if (eiData == 2) { // big endian
                e_machine = (header[18] & 0xFF) << 8 | (header[19] & 0xFF);
            } else {
                return "Unknown endian";
            }

            return getArchName(e_machine);

        } catch (IOException e) {
            return "Error";
        }
    }
    private static final Map<Integer, String> ELF_ARCH_MAP = new HashMap<>();
    static {
        ELF_ARCH_MAP.put(3, "x86");
        ELF_ARCH_MAP.put(62, "x86-64");
        ELF_ARCH_MAP.put(40, "ARM (32-bit)");
        ELF_ARCH_MAP.put(183, "ARM64 (AArch64)");
        ELF_ARCH_MAP.put(8, "MIPS");
        ELF_ARCH_MAP.put(243, "RISC-V");
        ELF_ARCH_MAP.put(21, "PowerPC");
        ELF_ARCH_MAP.put(22, "PowerPC64");
        ELF_ARCH_MAP.put(50, "IA-64");
        // can extend with more official ELF machine constants
    }

    private String getArchName(int e_machine) {
        return ELF_ARCH_MAP.getOrDefault(e_machine, "Unknown (e_machine=" + e_machine + ")");
    }
}