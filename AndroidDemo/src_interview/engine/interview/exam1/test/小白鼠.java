package engine.interview.exam1.test;

public final class 小白鼠 {
    
    public static final 小白鼠[] mouse = new 小白鼠[10];
    
    static
    {
        for (int i = 0; i < mouse.length; i++)
        {
            mouse[i] = new 小白鼠();
        }
    }
    
    private boolean 死亡;
    
    public void 喝药水(final 药水 poison)
    {
        new Thread(){
            public void run() {
                try {
                    Thread.sleep(20 * 60 * 60 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                if (poison.有毒)
                {
                    死亡 = true;
                }
            };
        }.start();
    }
    
    public boolean 死亡()
    {
        return 死亡;
    }
}