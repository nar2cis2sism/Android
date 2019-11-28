package engine.android.game.util;

import engine.android.game.Box;

/**
 * Bonus管理器
 * 
 * @author Daimon
 * @since 6/6/2012
 */
public class BonusManager {

    private final Box<Bonus> bonus = new Box<Bonus>();     // 奖励物品清单

    /**
     * 添加Bonus
     */
    public void add(Bonus b) {
        bonus.add(b.setStatus(Bonus.LIVE));
    }

    /**
     * 触发Bonus
     */
    public void trigger(Bonus b) {
        b.setStatus(Bonus.EFFECTIVE);
    }

    /**
     * 移除Bonus
     */
    public void remove(Bonus b) {
        bonus.remove(b.setStatus(Bonus.DEAD));
    }

    /**
     * 奖励物品
     */
    public abstract class Bonus {

        public static final int LIVE        = 0;        // 活动态（可以被触发）
        public static final int EFFECTIVE   = 1;        // 生效态（产生附加作用）
        public static final int DEAD        = 2;        // 死亡态（不可以被触发）

        private int status;                             // 状态

        /**
         * 设置状态
         */
        Bonus setStatus(int status) {
            if (status == EFFECTIVE && this.status == LIVE)
            {
                valid();
            }
            else if (status == DEAD && this.status == EFFECTIVE)
            {
                invalid();
            }

            this.status = status;
            return this;
        }

        public int getStatus() {
            return status;
        }

        /**
         * 奖励生效
         */
        protected abstract void valid();

        /**
         * 奖励失效
         */
        protected abstract void invalid();
    }
}