package demo.j2se.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;

public class FileProcess {
    
    public void process(File file)
    {
        if (!file.exists())
        {
            throw new RuntimeException("file is not exist");
        }
        
        if (file.isDirectory())
        {
            processDirectory(file);
        }
        else
        {
            processFile(file);
        }
    }
    
    protected void processDirectory(File dir)
    {
        for (File f : dir.listFiles())
        {
            process(f);
        }
    }
    
    protected void processFile(File file)
    {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            PrintWriter pw = new PrintWriter(file.getAbsolutePath() + ".r");
            String s;
            while ((s = br.readLine()) != null)
            {
                s = processLine(s);
                pw.println(s);
            }
            
            pw.close();
            br.close();
            System.out.println("success:" + file.getName());
        } catch (Exception e) {
            System.out.println("fail:" + file.getName());
        }
    }
    
    protected String processLine(String s)
    {
        return s;
    }
}