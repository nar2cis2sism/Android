package engine.interview.exam;

public class Exam {
    
    public void 两个数据交换(int a, int b)
    {
        a = a ^ b;
        b = a ^ b;
        a = a ^ b;
    }
    
    
//    public class Test { 
//        public static void main(String[] args) {  
//        System.out 
//        .println(test 
//        ());  
//        }  
//         
//        public static String test() {  
//        try {  
//        System.out 
//        .println("try block");  
//        return test1 
//        ();  
//        } finally {  
//        System.out 
//        .println("finally block");  
//        }  
//        }  
//        public static String test1() {  
//        System.out 
//        .println("return statement");  
//        return "after return";  
//        }  
//        }  
//        清单 9 的结果：
//
//        try block
//        return statement
//        finally block
//        after return 
}