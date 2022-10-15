package cn.huangxin.iaa.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 黄鑫
 * @description FileUtil
 */
public class FileUtil {

    public static List<String> getAllFile(String directoryPath) {
        List<String> list = new ArrayList<String>();
        File baseFile = new File(directoryPath);
        if (baseFile.isFile() || !baseFile.exists()) {
            return list;
        }
        File[] files = baseFile.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                list.addAll(getAllFile(file.getAbsolutePath()));
            } else {
                list.add(file.getAbsolutePath());
            }
        }
        return list;
    }
}
