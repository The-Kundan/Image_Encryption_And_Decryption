package Image;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class imageOperation {

    private static File selectedFile;
    private static final byte[] SIGNATURE = "ENCRYPTED".getBytes(); // Signature to mark encrypted files

    public static void operate(int key, boolean isEncrypt, JTextField textField, JLabel fileLabel) {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(null, "Please choose a file first.");
            return;
        }

        // Show confirmation dialog
        String operation = isEncrypt ? "encrypt" : "decrypt";
        int confirm = JOptionPane.showConfirmDialog(null, "Do you want to " + operation + " this file?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            // File Input/Output Stream
            try {
                FileInputStream fis = new FileInputStream(selectedFile);
                byte[] imgData = new byte[fis.available()];
                fis.read(imgData);
                fis.close();

                // Check for encryption/decryption signature
                boolean isAlreadyEncrypted = isFileEncrypted(imgData);
                if (isEncrypt && isAlreadyEncrypted) {
                    JOptionPane.showMessageDialog(null, "File is already encrypted.");
                    return;
                }
                if (!isEncrypt && !isAlreadyEncrypted) {
                    JOptionPane.showMessageDialog(null, "File is not encrypted.");
                    return;
                }

                // Perform XOR operation
                int i = 0;
                for (byte b : imgData) {
                    imgData[i] = (byte) (b ^ key);
                    i++;
                }

                // Add/remove encryption signature
                if (isEncrypt) {
                    imgData = addSignature(imgData);
                } else {
                    imgData = removeSignature(imgData);
                }

                FileOutputStream fos = new FileOutputStream(selectedFile);
                fos.write(imgData);
                fos.close();

                JOptionPane.showMessageDialog(null, "Operation completed successfully.");

                // Clear file and key fields
                selectedFile = null;
                fileLabel.setText("No file chosen");
                textField.setText("");

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "An error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Operation canceled.");
        }
    }

    private static boolean isFileEncrypted(byte[] data) {
        if (data.length < SIGNATURE.length) return false;
        for (int i = 0; i < SIGNATURE.length; i++) {
            if (data[data.length - SIGNATURE.length + i] != SIGNATURE[i]) return false;
        }
        return true;
    }

    private static byte[] addSignature(byte[] data) {
        byte[] newData = new byte[data.length + SIGNATURE.length];
        System.arraycopy(data, 0, newData, 0, data.length);
        System.arraycopy(SIGNATURE, 0, newData, data.length, SIGNATURE.length);
        return newData;
    }

    private static byte[] removeSignature(byte[] data) {
        byte[] newData = new byte[data.length - SIGNATURE.length];
        System.arraycopy(data, 0, newData, 0, newData.length);
        return newData;
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Image Operation");
        f.setSize(600, 300);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Custom panel with rounded corners
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(0, 0, Color.CYAN, 0, getHeight(), Color.WHITE);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            }
        };
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        Font font = new Font("Arial", Font.BOLD, 18);
        JButton chooseButton = new JButton("Choose Image");
        chooseButton.setFont(font);
        chooseButton.setIcon(new ImageIcon("icons/choose.png")); // Add custom icon

        JLabel fileLabel = new JLabel("No file chosen");
        fileLabel.setFont(font);

        JTextField textField = new JTextField(10);
        textField.setFont(font);

        JButton encryptButton = new JButton("Encrypt");
        encryptButton.setFont(font);
        encryptButton.setIcon(new ImageIcon("icons/encrypt.png")); // Add custom icon

        JButton decryptButton = new JButton("Decrypt");
        decryptButton.setFont(font);
        decryptButton.setIcon(new ImageIcon("icons/decrypt.png")); // Add custom icon

        // Set component positions
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(chooseButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(fileLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Enter Key:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(textField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(encryptButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(decryptButton, gbc);

        f.add(panel);
        f.setVisible(true);

        // Adding action listeners
        chooseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
                if (selectedFile != null) {
                    fileLabel.setText("File: " + selectedFile.getName());
                }
            }
        });

        encryptButton.addActionListener(e -> {
            String text = textField.getText();
            if (text.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please enter a key.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                int temp = Integer.parseInt(text);
                operate(temp, true, textField, fileLabel);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Please enter a valid number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });

        decryptButton.addActionListener(e -> {
            String text = textField.getText();
            if (text.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please enter a key.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                int temp = Integer.parseInt(text);
                operate(temp, false, textField, fileLabel);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Please enter a valid number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
