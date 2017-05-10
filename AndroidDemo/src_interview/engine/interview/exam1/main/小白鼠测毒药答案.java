package engine.interview.exam1.main;

import engine.interview.exam1.test.药水;

/**
 * 把10只小白鼠看成10个二进制位，每只小白鼠都有0,1两种状态。10只小白鼠所表示的数范围是
 * 0000000000~1111111111 
 * 2的10次方为1024，足够表示1000；
 * 接下来，就是把毒药混合，给小白鼠吃，最后看死了的小白鼠都是哪几个，就能判断出哪瓶药有毒。
 * 药            小白鼠
 * 1       0000000001
 * 2       0000000010
 * 3       0000000011
 * 。。。  。。。。。

 * 假设死的小白鼠的结果是 000000111，则有毒的药是第7瓶
 */

public class 小白鼠测毒药答案 extends 小白鼠测毒药试题 {

    @Override
    public 药水 find() {
        for (int i = 0; i < poison.length; i++)
        {
            int index = 1;
            for (int j = mouse.length - 1; j >= 0; j--, index *= 2)
            {
                if ((i & index) != 0)
                {
                    mouse[j].喝药水(poison[i]);
                }
            }
        }
        
        try {
            Thread.sleep(23 * 60 * 60 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        int pos = 0;
        int index = 1;
        for (int i = mouse.length - 1; i >= 0; i--, index *= 2)
        {
            if (mouse[i].死亡())
            {
                pos |= index;
            }
        }
        
        return poison[pos];
    }
}