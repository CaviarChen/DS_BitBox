package unimelb.bitbox.util.FileSystem;


import unimelb.bitbox.util.FileSystem.FileSystemManager.FileSystemEvent;


public interface FileSystemObserver {
    void processFileSystemEvent(FileSystemEvent fileSystemEvent);
}
