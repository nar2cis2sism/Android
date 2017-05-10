package demo.j2se.file;

import java.io.File;

public class TabToSpace extends FileProcess {
    
    public static void main(String[] args) {
        String path = "C:\\Users\\hyan\\Desktop\\New folder";
        new TabToSpace().process(new File(path));
    }
    
    @Override
    protected void processFile(File file) {
        String name = file.getName();
        if (name.endsWith(".java") || name.endsWith(".xml"))
        {
            super.processFile(file);
        }
    }
    
    @Override
    protected String processLine(String s) {
        s = s.replaceAll("\t", "    ");
        while (s.length() > 0 && s.charAt(s.length() - 1) == ' ')
        {
            s = s.substring(0, s.length() - 1);
        }
        
        return s;
    }
}