package pl.edu.zut.mad.appwizut2.network;

import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v4.util.AtomicFile;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

import pl.edu.zut.mad.appwizut2.utils.Constants;

/**
 * Loader for downloading pdf with schedule
 */
public class SchedulePdfLoader extends BaseDataLoader<Uri, SchedulePdfLoader.CacheInfo> {


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
    private boolean checkIfShouldDownloadFullPdf(String pdfAddress, CacheInfo cachedData) throws IOException {
        // Download range that interests us

        HttpConnect conn = new HttpConnect(pdfAddress);
        try {
            conn.requestRange(getCheckedRangeStart(cachedData.mCachedFileSize), CHECKED_RANGE_LENGTH);

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
            if (conn.getFullContentLength() != cachedData.mCachedFileSize) {
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
            return !Arrays.equals(checkedRangeData, cachedData.mCachedRangeData);
        } finally {
            conn.close();
        }
    }

    @Override
    protected boolean cacheIsValidForCurrentSettings(CacheInfo cachedData) {
        String group = ScheduleLoader.getGroupFromSettings(getContext());
        if (group == null) {
            return false;
        }
        File localPdfFile = getSchedulePdfFile();
        return group.equals(cachedData.mForGroup) && localPdfFile.exists();
    }

    @Override
    protected CacheInfo doDownload(CacheInfo cachedData) throws IOException {
        String group = ScheduleLoader.getGroupFromSettings(getContext());
        if (group == null) {
            return null;
        }

        String pdfAddress = String.format(Constants.PDF_SCHEDULE_URL, group);
        File localPdfFile = getSchedulePdfFile();

        // Check if schedule is up to date
        if (cachedData != null) {
            if (!checkIfShouldDownloadFullPdf(pdfAddress, cachedData)) {
                // Already up to date
                return cachedData;
            }
        }

        // Download new schedule
        HttpConnect conn = new HttpConnect(pdfAddress);
        int fileSize = conn.getFullContentLength();
        try {
            AtomicFile af = new AtomicFile(localPdfFile);
            FileOutputStream fileOutputStream = af.startWrite();
            try {
                // Do download
                IoUtils.copyStream(conn.getInputStream(), fileOutputStream);
                af.finishWrite(fileOutputStream);
            } catch (Exception e) {
                af.failWrite(fileOutputStream);
                throw e;
            }

            // Prepare data for cache so we can just check if file has been changed
            CacheInfo newCacheInfo = new CacheInfo();
            DataInputStream justWrittenPdf = new DataInputStream(new FileInputStream(localPdfFile));
            justWrittenPdf.skip(getCheckedRangeStart(fileSize));
            justWrittenPdf.readFully(newCacheInfo.mCachedRangeData);
            justWrittenPdf.close();
            newCacheInfo.mCachedFileSize = fileSize;
            newCacheInfo.mForGroup = group;
            return newCacheInfo;
        } finally {
            conn.close();
        }

    }

    @Override
    protected Uri parseData(CacheInfo cacheInfo) {
        File schedulePdfFile = getSchedulePdfFile();
        if (schedulePdfFile.exists()) {
            return FileProvider.getUriForFile(getContext(), Constants.FILE_PROVIDER_AUTHORITY, schedulePdfFile);
        }
        else {
            return null;
        }
    }

    class CacheInfo implements Serializable {
        private String mForGroup;
        private int mCachedFileSize;
        private byte[] mCachedRangeData = new byte[CHECKED_RANGE_LENGTH];
    }
}
