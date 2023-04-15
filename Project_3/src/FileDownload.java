import java.util.zip.CRC32;

/**
 * instances of this class are passed from p2p as file downloads
 * has checksum
 * has simulation method to randomly add noise to download
 */
public class FileDownload {
    String contents;
    long checksum;

    public FileDownload(String fContent) {
        contents = fContent;
        byte[] bytes = contents.getBytes();
        CRC32 crc = new CRC32();
        crc.update(bytes);  // computes checksum
        checksum = crc.getValue();  // store checksum value
    }

    public String GetContents() {
        return contents;
    }

    /**
     * checks computed checksum against stored checksum
     * false if they don't match
     */
    public boolean Checksum() {
        byte[] bytes = contents.getBytes();
        CRC32 crc = new CRC32();
        crc.update(bytes);  // computes checksum
        return (crc.getValue() == checksum);
    }

}