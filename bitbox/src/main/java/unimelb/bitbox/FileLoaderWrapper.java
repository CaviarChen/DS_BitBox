//package unimelb.bitbox;
//
//import unimelb.bitbox.protocol.Protocol;
//import unimelb.bitbox.protocol.ProtocolField;
//import unimelb.bitbox.util.Configuration;
//import unimelb.bitbox.util.FileSystemManager;
//
//import java.util.LinkedHashMap;
//import java.util.LinkedHashSet;
//import java.util.LinkedList;
//
//public class FileLoaderWrapper {
//
//    private static final int BLOCK_SIZE = Integer.parseInt(Configuration.getConfigurationValue("blockSize"));
//
//    private final LinkedList<ProtocolField.FilePosition> pending = new LinkedList<>();
//    private final LinkedHashSet<ProtocolField.FilePosition> waiting = new LinkedHashSet<>();
//
//    public FileLoaderWrapper(Protocol.FileCreateRequest fileCreateRequest, FileSystemManager fileSystemManager) {
//        ProtocolField.FileDes fileDes = fileCreateRequest.fileDes;
//
//        long base = 0, remaining = fileDes.fileSize;
//        while (remaining > 0) {
//            ProtocolField.FilePosition pos = new ProtocolField.FilePosition();
//            pos.pos = base;
//            pos.len = Math.min(remaining, BLOCK_SIZE);
//            base += pos.len;
//            remaining -= pos.len;
//            pending.addLast(pos);
//        }
//
//
//        send(10);
//    }
//
//    public void received(Protocol.FileBytesResponse fileBytesResponse, FileSystemManager fileSystemManager) {
//        ProtocolField.FilePosition pos = new ProtocolField.FilePosition();
//        pos.len = fileBytesResponse.fileContent.len;
//        pos.pos = fileBytesResponse.fileContent.pos;
//        if (!waiting.remove(pos)) {
//            return;
//        }
//
//        // write to file
////        fileSystemManager.writeFile()
//
//        send(1);
//
//        if (waiting.isEmpty()) {
//            // check complete
//            fileSystemManager.checkWriteComplete(fileBytesResponse.fileDes.path);
//        }
//
//    }
//
//    private void send(int limit) {
//        for (int i=0; i<limit; i++) {
//            if (!pending.isEmpty()) {
//                ProtocolField.FilePosition sendPos = pending.pollFirst();
//                waiting.add(sendPos);
//                // send new one
//
//            }
//        }
//    }
//
//
//    private void cancel() {
//
//    }
//
//    private Connection getActiveConnection() {
//
//    }
//
//    public void addConnection() {
//
//    }
//
//    public void clean() {
//
//    }
//
//
//}
