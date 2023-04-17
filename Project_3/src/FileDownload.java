import java.util.zip.CRC32;

/**
 * instances of this class are passed from p2p as file downloads
 * has checksum
 * has simulation method to randomly add noise to download
 */
public class FileDownload {
    byte[] contents;
    long checksum;

    public FileDownload(byte[] fContent) {
        contents = fContent;
        CRC32 crc = new CRC32();
        crc.update(contents);  // computes checksum
        checksum = crc.getValue();  // store checksum value
        //addNoise();
    }

    public byte[] GetContents() {
        return contents;
    }

    public long GetChecksum(){
        return checksum;
    }

    /**
     * checks computed checksum against stored checksum
     * false if they don't match
     */
    public boolean Checksum() {
        CRC32 crc = new CRC32();
        crc.update(contents);  // computes checksum
        return (crc.getValue() == checksum);
    }

    private void addNoise() {
        // TODO: randomly change a random bit/byte
    }

}