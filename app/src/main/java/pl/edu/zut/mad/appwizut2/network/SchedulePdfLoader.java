package pl.edu.zut.mad.appwizut2.network;

import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v4.util.AtomicFile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import pl.edu.zut.mad.appwizut2.utils.Constants;

/**
 * Loader for downloading pdf with schedule
 */
public class SchedulePdfLoader extends BaseDataLoader<Uri> {


    /**
     * Length of chunk used for checking for updates
     */
    private static final int CHECKED_RANGE_LENGTH = 30;

    private static int getCheckedRangeStart(int size) {
        return size - CHECKED_RANGE_LENGTH - 20;
    }

    // Invoked via reflection
    SchedulePdfLoader(DataLoadingManager loadingManager) {
        super(loadingManager);
    }

    private String mForGroup;
    private int mCachedFileSize;
    private byte[] mCachedRangeData = new byte[CHECKED_RANGE_LENGTH];

    private File getSchedulePdfFile() {
        // TODO: Move "provided" directory name and mkdir somewhere
        File providedFilesDirectory = new File(getContext().getFilesDir(), Constants.FILE_PROVIDER_FILE_PATH);
        providedFilesDirectory.mkdirs();
        return new File(providedFilesDirectory, "Plan.pdf");
    }

    @Override
    protected String getCacheName() {
        return "SchedulePdfMetadata";
    }

    /**
     * Download small fragment of schedule PDF and check if it matches our cached part
     */
    private boolean checkIfShouldDownloadFullPdf(String pdfAddress) throws IOException {
        // Download range that interests us

        HttpConnect conn = new HttpConnect(pdfAddress);
        try {
            conn.requestRange(getCheckedRangeStart(mCachedFileSize), CHECKED_RANGE_LENGTH);

            // If this isn't range response
            if (!conn.serverReturnedRangeResponse()) {
                if (conn.serverReturnedRangeIsPastEOFResponse()) {
                    // File size changed, re-download
                    return true;
                }

                // Error during communication with server; don't re-download
                return false;
            }

            // Verify size
            if (conn.getFullContentLength() != mCachedFileSize) {
                // Size changed, re-download
                return true;
            }

            // Read part of file for checking
            byte[] checkedRangeData = new byte[CHECKED_RANGE_LENGTH];
            DataInputStream inputStream = new DataInputStream(conn.getInputStream());
            try {
                inputStream.readFully(checkedRangeData);
            } finally {
                inputStream.close();
            }

            // Compare that part of file
            return !Arrays.equals(checkedRangeData, mCachedRangeData);
        } finally {
            conn.close();
        }
    }

    @Override
    protected boolean doDownload(boolean skipCache) {
        String group = ScheduleLoader.getGroupFromSettings(getContext());
        if (group == null) {
            return false;
        }

        String pdfAddress = String.format(Constants.PDF_SCHEDULE_URL, group);

        try {
            // Check if schedule is up to date
            boolean shouldSkipCacheCheck = // We skip checking part of file if
                    skipCache || // Requested by user
                    mCachedFileSize == 0 || // Don't have cached version
                    !group.equals(mForGroup) || // Cached version is for different group
                    !getSchedulePdfFile().exists(); // Cached file doesn't exist
            if (!shouldSkipCacheCheck) {
                if (!checkIfShouldDownloadFullPdf(pdfAddress)) {
                    return false;
                }
            }

            // Download new schedule
            HttpConnect conn = new HttpConnect(pdfAddress);
            try {
                AtomicFile af = new AtomicFile(getSchedulePdfFile());
                FileOutputStream fileOutputStream = af.startWrite();
                try {
                    // Do download
                    IoUtils.copyStream(conn.getInputStream(), fileOutputStream);
                    af.finishWrite(fileOutputStream);
                } catch (Exception e) {
                    af.failWrite(fileOutputStream);
                    throw e;
                }
            } finally {
                conn.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    @Override
    protected boolean loadFromCache(File cacheFile) {
        DataInputStream input = null;
        try {
            input = new DataInputStream(new FileInputStream(cacheFile));

            mForGroup = input.readUTF();
            mCachedFileSize = input.readInt();
            input.readFully(mCachedRangeData);

        } catch (IOException e) {
            mCachedFileSize = 0;
            e.printStackTrace();
            return false;
        } finally {
            IoUtils.closeQuietly(input);
        }
        return true;
    }

    @Override
    protected boolean saveToCache(File cacheFile) {
        if (mCachedFileSize == 0) {
            return false;
        }
        DataOutputStream output = null;
        try {
            output = new DataOutputStream(new FileOutputStream(cacheFile));

            output.writeUTF(mForGroup);
            output.writeInt(mCachedFileSize);
            output.write(mCachedRangeData);

        } catch (IOException e) {
            mCachedFileSize = 0;
            e.printStackTrace();
            return false;
        } finally {
            IoUtils.closeQuietly(output);
        }
        return false;
    }

    @Override
    protected Uri getData() {
        File schedulePdfFile = getSchedulePdfFile();
        if (schedulePdfFile.exists()) {
            return FileProvider.getUriForFile(getContext(), Constants.FILE_PROVIDER_AUTHORITY, schedulePdfFile);
        }
        else {
            return null;
        }
    }
}
