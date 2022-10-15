package cn.huangxin.iaa.context;

import cn.huangxin.iaa.annotation.ComponentScan;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 黄鑫
 * @version 1.0
 * @date 2022/10/10 10:21
 * @description: TODO
 */
public class MyApplicationContext {

    private Class<?> configClass;

    private ConcurrentHashMap<String, Object> beanMap = new ConcurrentHashMap<>();

    public MyApplicationContext() {
    }

    public MyApplicationContext(Class<?> configClass) {
        this.configClass = configClass;
        ComponentScan componentScan = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
        String scanPath = componentScan.value();
        if (!scanPath.isEmpty()) {
            String replace = scanPath.replace(".", File.separator);
            ClassLoader classLoader = configClass.getClassLoader();
            URL url = classLoader.getResource("");
            String filePath = url.getFile();
            String listPath = filePath + File.separator + replace;
            for (String path : getAllFile(listPath)) {
                path = path.substring(path.indexOf("classes\\")+8).replace(".class", "").replace("\\", ".");
                try {
                    Class<?> aClass = classLoader.loadClass(path);
                    String name = aClass.getSimpleName();
                    Object value = aClass.newInstance();
                    System.out.println("name = " + name);
                    beanMap.put(name, value);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
//            classLoader.loadClass("");
        }
    }

    private List<String> getAllFile(String directoryPath) {
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

    public Object getBean(String beanName) {
        return beanMap.get(beanName);
    }
}
