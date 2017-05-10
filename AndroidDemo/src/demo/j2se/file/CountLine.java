package demo.j2se.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CountLine extends FileProcess {
    
    private static int count;
    private static int count1;
    private static String name = "";
    private static int count2;
    private static int countBuf;
    
    public static void main(String[] args) {
        List<String> list = new ArrayList<String>();
        list.add("D:\\workspace\\good\\depot\\dev\\android\\common");
        list.add("D:\\workspace\\good\\depot\\dev\\android\\gfe\\db");
        list.add("D:\\workspace\\good\\depot\\dev\\android\\gfe\\otap");
        list.add("D:\\workspace\\good\\depot\\dev\\android\\gfe\\syncengine");
        list.add("D:\\workspace\\good\\depot\\dev\\android\\ui");
        count(list);

        list.clear();
        list.add("D:\\workspace\\eclipse3.7\\androidEngine");
        count(list);
    }
    
    private static void count(List<String> list)
    {
        for (String dir : list)
        {
            File file = new File(dir);
            new CountLine().process(file);
        }
        
        System.out.println("类：" + count + "个");
        System.out.println("代码：" + count1 + "行");
        System.out.println("最长类名：" + name);
        System.out.println("最长类代码：" + count2 + "行");
        count = count1 = count2 = 0;
    }
    
    @Override
    protected void processDirectory(File dir) {
        if (dir.getName().equals("libs"))
        {
            return;
        }
        
        super.processDirectory(dir);
    }
    
    @Override
    protected void processFile(File file) {
        String name = file.getName();
        if (name.endsWith(".java") && !name.equals("R.java"))
        {
            count++;
            countBuf = 0;
            
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String s;
                while ((s = br.readLine()) != null)
                {
                    s = processLine(s);
                }
                
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            if (countBuf > count2)
            {
                count2 = countBuf;
                CountLine.name = name;
            }
        }
    }
    
    @Override
    protected String processLine(String s) {
        count1++;
        countBuf++;
        return super.processLine(s);
    }
}