package unimelb.bitbox.util.FileSystem;


import unimelb.bitbox.util.FileSystem.FileSystemManager.FileSystemEvent;


/**
 * 
 *
 * @author Aaron Harwood
 *
 * @author Wenqing Xue (813044)
 * @author Weizhi Xu (752454)
 * @author Zijie Shen (741404)
 * @author Zijun Chen (813190)
 *
 */
public interface FileSystemObserver {
    void processFileSystemEvent(FileSystemEvent fileSystemEvent);
}
