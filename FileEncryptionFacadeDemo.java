// FileEncryptionFacadeDemo.java

import java.io.*;

/**
 * Demonstrates file encryption using the Facade pattern.
 */
public class FileEncryptionFacadeDemo {

    /**
     * Module 1: Reads the source file into a byte array.
     */
    static class SourceFileReader {
        /**
         * Reads all bytes from the specified file.
         * @param sourcePath path to the source file
         * @return byte array containing file data
         * @throws IOException if an I/O error occurs
         */
        public byte[] read(String sourcePath) throws IOException {
            try (FileInputStream fis = new FileInputStream(sourcePath);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                return baos.toByteArray();
            }
        }
    }

    /**
     * Module 2: Encrypts a byte array using a simple XOR cipher.
     */
    static class FileEncryptor {
        private final byte key;

        public FileEncryptor(byte key) {
            this.key = key;
        }

        /**
         * Applies XOR encryption to each byte.
         * @param data original byte array
         * @return encrypted byte array
         */
        public byte[] encrypt(byte[] data) {
            byte[] result = new byte[data.length];
            for (int i = 0; i < data.length; i++) {
                result[i] = (byte) (data[i] ^ key);
            }
            return result;
        }
    }

    /**
     * Module 3: Writes a byte array to the specified file.
     */
    static class EncryptedFileWriter {
        /**
         * Writes the given data to the destination file.
         * @param data byte array to write
         * @param destPath path to the destination file
         * @throws IOException if an I/O error occurs
         */
        public void write(byte[] data, String destPath) throws IOException {
            try (FileOutputStream fos = new FileOutputStream(destPath)) {
                fos.write(data);
            }
        }
    }

    /**
     * Facade class: Provides a single method to perform read→encrypt→write.
     */
    public static class FileEncryptionFacade {
        private final SourceFileReader reader;
        private final FileEncryptor encryptor;
        private final EncryptedFileWriter writer;

        /**
         * Constructs the facade with the given XOR key.
         * @param key encryption key (0–255)
         */
        public FileEncryptionFacade(byte key) {
            this.reader    = new SourceFileReader();
            this.encryptor = new FileEncryptor(key);
            this.writer    = new EncryptedFileWriter();
        }

        /**
         * Executes the full encryption process:
         * 1) Read input file.
         * 2) Encrypt data.
         * 3) Write encrypted output.
         *
         * @param sourcePath path to the input file
         * @param destPath   path to the encrypted output file
         * @throws IOException if an I/O error occurs
         */
        public void encryptFile(String sourcePath, String destPath) throws IOException {
            byte[] originalData  = reader.read(sourcePath);
            byte[] encryptedData = encryptor.encrypt(originalData);
            writer.write(encryptedData, destPath);
        }
    }

    /**
     * Client code. Usage:
     *   java FileEncryptionFacadeDemo <input-file> <output-file> <key>
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java FileEncryptionFacadeDemo <input-file> <output-file> <key>");
            return;
        }

        String inputFile  = args[0];
        String outputFile = args[1];
        byte key          = (byte) Integer.parseInt(args[2]);

        FileEncryptionFacade facade = new FileEncryptionFacade(key);
        try {
            facade.encryptFile(inputFile, outputFile);
            System.out.printf("Encryption succeeded: %s -> %s%n", inputFile, outputFile);
        } catch (IOException e) {
            System.err.println("Encryption failed: " + e.getMessage());
        }
    }
}
