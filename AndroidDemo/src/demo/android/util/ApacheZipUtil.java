package demo.android.util;

import engine.android.util.file.FileManager;
import engine.android.util.io.IOUtil;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

/**
 * 压缩工具<br>
 * 由于java.util.zip包不支持含有中文的文件名压缩，故采用org.apache.tools.zip包，需引入ant.jar<br>
 * 注：编译较麻烦，容易死机
 * 
 * @author Daimon
 * @version 3.0
 * @since 9/26/2012
 */

public final class ApacheZipUtil {

    /**
     * 批量压缩文件（夹）
     * 
     * @param zipFile 生成的压缩文件
     * @param files 需要压缩的文件（夹）列表
     */

    public static void zip(File zipFile, File... files) throws Exception {
        FileManager.createFileIfNecessary(zipFile);
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(zipFile));
            zos.setEncoding("gb2312");
            zos.setFallbackToUTF8(true);
            for (File file : files)
            {
                if (file.exists())
                {
                    zip(file, zos, "");
                }
            }
        } finally {
            if (zos != null)
            {
                zos.close();
            }
        }
    }

    /**
     * 压缩单个文件（夹）
     * 
     * @param file 需要压缩的文件（夹）
     * @param zos 压缩后的目标文件流
     * @param dir 压缩后的文件目录
     */

    private static void zip(File file, ZipOutputStream zos, String dir)
            throws Exception {
        dir += (dir.length() == 0 ? dir : File.separator) + file.getName();
        if (file.isDirectory())
        {
            for (File f : file.listFiles())
            {
                zip(f, zos, dir);
            }
        }
        else
        {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                zos.putNextEntry(new ZipEntry(dir));
                IOUtil.writeStream(fis, zos);
                zos.closeEntry();
            } finally {
                if (fis != null)
                {
                    fis.close();
                }
            }
        }
    }

    /**
     * 解压缩文件
     * 
     * @param zipFile 压缩文件
     * @param dir 解压缩的目标目录
     */

    public static void unzip(File zipFile, String dir) throws Exception {
        ZipFile zf = new ZipFile(zipFile);
        
        InputStream is = null;
        OutputStream os = null;

        try {
            @SuppressWarnings("unchecked")
            Enumeration<? extends ZipEntry> e = zf.getEntries();
            while (e.hasMoreElements())
            {
                ZipEntry entry = e.nextElement();
                if (entry.isDirectory())
                {
                    continue;
                }

                try {
                    is = zf.getInputStream(entry);

                    File file = new File(dir + File.separator + entry.getName());
                    FileManager.createFileIfNecessary(file);

                    os = new FileOutputStream(file);

                    IOUtil.writeStream(is, os);
                } finally {
                    if (is != null)
                    {
                        try {
                            is.close();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        
                        is = null;
                    }
                    
                    if (os != null)
                    {
                        try {
                            os.close();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        
                        os = null;
                    }
                }
            }
        } finally {
            zf.close();
        }
    }
}