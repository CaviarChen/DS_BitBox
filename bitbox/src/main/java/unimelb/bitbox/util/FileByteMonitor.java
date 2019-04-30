package unimelb.bitbox.util;


import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import unimelb.bitbox.Connection;
import unimelb.bitbox.Constants;
import unimelb.bitbox.protocol.Protocol;
import unimelb.bitbox.protocol.ProtocolFactory;
import unimelb.bitbox.protocol.ProtocolField;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


public class FileByteMonitor {
    private Logger log = Logger.getLogger(FileByteMonitor.class.getName());

    public static final int RETRY_SEND_REQUEST_INTERVAL = 800;

    private static final int MAX_RETRY_LIMIT = 1;
    private static final int MAX_BLOCK_CNT = 10;
    private static final int CHECK_BATCH_INTERVAL = 1000;
    private static final int MAX_WAITING_TIME = 1000 * 60 * 10;
    private static final long DEFAULT_BLOCK_SIZE = 1048576;

    private ConcurrentHashMap<String, Batch> loadingFiles;
    private long maxBlockSize;

    public FileByteMonitor() {

        try {
            maxBlockSize = Long.parseLong(Configuration.getConfigurationValue(Constants.CONFIG_FIELD_BLOCKSIZE));
        } catch (Exception e) {
            log.warning("Failed to get block size" + e.toString());
            maxBlockSize = DEFAULT_BLOCK_SIZE;
        }

        loadingFiles = new ConcurrentHashMap<>();

    }

    public void batchSendAndWait(ProtocolField.FileDes fileDes, FileSystemManager fileSystemManager, Connection conn) {
        String filePath = fileDes.path;
        long fileSize = fileDes.fileSize;

        // file already started loading
        if (loadingFiles.containsKey(filePath)) {
            return ;
        }

        long position = 0;


        loadingFiles.put(filePath, new Batch());

        while (position < fileSize) {
            int numBlocks = 0;

            boolean isLastBatch = false;

            // send requests
            while (numBlocks < MAX_BLOCK_CNT) {
                long length = maxBlockSize;
                isLastBatch = false;

                if (length + position > fileSize) {
                    length = fileSize - position;
                    isLastBatch = true;
                }

                SendFileByteRequest(fileDes, position, length, conn);
                loadingFiles.get(filePath).batchLength.put(position, length);

                numBlocks++;
                position += length;
                if (isLastBatch) break;
            }

            Batch curBatch = loadingFiles.get(filePath);
            curBatch.blockCnt = numBlocks;

            // check whether all bytes in this batch are marked as written
            while (true) {
                // all responses in the current batch are received
                if (curBatch.isAllReceived()) {

                    if (isLastBatch) {
                        if (!checkIfCompleteLoadingFile(filePath, fileSystemManager)) {
                            // unlikely to happen
                            // as the file has received all the bytes but the content is different
                            // if it happens, cancel the loading procedure and abort.
                            try {
                                fileSystemManager.cancelFileLoader(filePath);
                            } catch (IOException e) {
                            }

                            loadingFiles.remove(filePath);
                        } else {
                            break;
                        }
                    }

                    curBatch.Reset();

                } else if (curBatch.timestamp < System.currentTimeMillis()){
                    // timeout and exceed retry limit
                    if (curBatch.retryCnt < MAX_RETRY_LIMIT) {
                        // TODO: retry

                        curBatch.retryCnt++;
                    } else {
                        try {
                            fileSystemManager.cancelFileLoader(filePath);
                        } catch (IOException e) {
                            log.warning(e.toString());
                        }

                        loadingFiles.remove(filePath);
                        return;
                    }
                }

                try {
                    Thread.sleep(CHECK_BATCH_INTERVAL);
                } catch (InterruptedException e) {
                }
            }
        }

        // finish requesting and get all bytes successfully
        loadingFiles.remove(filePath);
    }

    public boolean isFileLoading(String filePath) {
        return loadingFiles.containsKey(filePath);
    }

    public boolean MarkByteWrote(String filePath, long pos, long len) {
        // Something went wrong. pos in request not equal to the one in the response
        // e.g. someone tried to modify the response,
        if (len != loadingFiles.get(filePath).batchLength.get(pos)) {
            return false;
        }

        loadingFiles.get(filePath).batchCheck.put(pos, true);
        return true;
    }


    private Boolean checkIfCompleteLoadingFile(String path, FileSystemManager fileSystemManager) {
        try {
            fileSystemManager.checkWriteComplete(path);
        } catch (NoSuchAlgorithmException e) {
            log.severe(e.toString());
            return false;
        } catch (IOException e) {
            log.warning(e.toString());
            return false;
        }

        return true;
    }


    private void SendFileByteRequest(ProtocolField.FileDes fd, long pos, long len, Connection conn) {
        Protocol.FileBytesRequest fileBytesRequest = new Protocol.FileBytesRequest();

        fileBytesRequest.fileDes = fd;
        fileBytesRequest.filePos.pos = pos;
        fileBytesRequest.filePos.len = len;

        conn.send(ProtocolFactory.marshalProtocol(fileBytesRequest));
    }

    private class Batch {
        public ConcurrentHashMap<Long, Boolean> batchCheck;   // position -> is received
        public ConcurrentHashMap<Long, Long> batchLength; // position -> length
        public long timestamp;
        public int blockCnt;
        public int retryCnt;

        public Batch() {
            this.batchCheck = new ConcurrentHashMap<>();
            this.batchLength = new ConcurrentHashMap<>();
            this.timestamp = System.currentTimeMillis() + MAX_WAITING_TIME;
        }

        public void Reset() {
            this.batchCheck.clear();
            this.batchLength.clear();
            this.timestamp = System.currentTimeMillis() + MAX_WAITING_TIME;
        }

        public boolean isAllReceived() {
            int cnt = 0;
            for (Boolean flag : batchCheck.values()) {
                if (flag) cnt++;
            }
            return cnt == this.blockCnt;
        }
    }
}
