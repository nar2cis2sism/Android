package engine.android.widget.helper;

import engine.android.widget.common.text.LetterBar;
import engine.android.widget.common.text.LetterBar.OnLetterChangedListener;

import android.widget.ListView;

import java.util.HashMap;

/**
 * 字母搜索控件辅助类
 * 
 * @author Daimon
 * @since 6/6/2014
 */
public class LetterBarHelper implements OnLetterChangedListener {

    // 字母索引表
    private final LetterBar letterBar;
    private final HashMap<String, Integer> letterMap;
    private ListView listView;
    
    public LetterBarHelper(LetterBar letterBar) {
        letterMap = new HashMap<String, Integer>((this.letterBar = letterBar).getLetters().length);
    }
    
    public final LetterBar getLetterBar() {
        return letterBar;
    }
    
    /**
     * 重置索引
     */
    public LetterBarHelper resetIndex() {
        letterMap.clear();
        return this;
    }

    /**
     * 注意：此索引直接应用到{@link ListView#setSelection(int)}，需加上{@link ListView#getHeaderViewsCount()}
     */
    public void setIndex(String letter, int index) {
        letterMap.put(letter, index);
    }
    
    public Integer getIndex(String letter) {
        return letterMap.get(letter);
    }
    
    /**
     * 绑定列表
     * 
     * @param listView 会根据索引表滑动到对应的位置
     */
    public void bindListView(ListView listView) {
        this.listView = listView;
        letterBar.setOnLetterChangedListener(this);
    }

    @Override
    public void onLetterChanged(String letter) {
        if (listView == null)
        {
            return;
        }
        
        Integer index = getIndex(letter);
        if (index == null)
        {
            return;
        }
        
        if (index < 0)
        {
            index = 0;
        }
        
        listView.setSelection(index);
    }
}