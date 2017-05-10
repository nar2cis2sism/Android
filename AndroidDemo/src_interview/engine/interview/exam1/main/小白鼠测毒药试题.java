package engine.interview.exam1.main;

import engine.interview.exam1.test.小白鼠;
import engine.interview.exam1.test.药水;

/**
 * 有1000瓶药水和10只小白鼠，药水中只有1瓶毒药。小白鼠喝了毒药20小时后发作，怎么在24小时之内找出那瓶毒药
 */

public abstract class 小白鼠测毒药试题 {
    
    protected final 药水[] poison = 药水.poison;
    protected final 小白鼠[] mouse = 小白鼠.mouse;
    
    /**
     * 请实现此方法
     * @return 有毒的药水
     */
    
    public abstract 药水 find();
}