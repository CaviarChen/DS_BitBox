package unimelb.bitbox.util.FileSystem;


import unimelb.bitbox.util.FileSystem.FileSystemManager.FileSystemEvent;


/**
 * The observer for file system events
 *
 * @author Aaron Harwood
 *
 * @author Weizhi Xu (752454)
 * @author Wenqing Xue (813044)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 *
 */
public interface FileSystemObserver {
    void processFileSystemEvent(FileSystemEvent fileSystemEvent);
}
