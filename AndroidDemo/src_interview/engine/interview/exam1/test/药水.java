package engine.interview.exam1.test;

import java.util.Random;

public final class 药水 {
    
    public static final 药水[] poison = new 药水[1000];
    
    static
    {
        for (int i = 0; i < poison.length; i++)
        {
            poison[i] = new 药水();
        }
        
        int rand = Math.abs(new Random().nextInt()) % poison.length;
        poison[rand].有毒 = true;
    }
    
    boolean 有毒;

}