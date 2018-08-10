package engine.android.framework.ui.extra;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import engine.android.framework.R;
import engine.android.framework.ui.BaseFragment;
import engine.android.util.StringUtil;
import engine.android.util.Util;
import engine.android.widget.component.TitleBar;

/**
 * 文本编辑界面
 * 
 * @author Daimon
 * @version N
 * @since 6/6/2014
 */
public class TextEditFragment extends BaseFragment {
    
    public static final class Params {
        
        public String title;            // 标题
        public int maxEms;              // 文本字数限制（中文计数1个字符，其他计数半个字符）
        public String desc;             // 描述说明
    }
    
    public static Bundle buildParams(Params params) {
        return ParamsBuilder.build(params);
    }
    
    Params params;
    
    TextView save;
    EditText input;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((params = ParamsBuilder.parse(getArguments(), Params.class)) == null)
        {
            finish();
        }
    }
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        // 保存
        save = newTextAction(getString(R.string.Save), new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                notifyDataChanged(input.getText().toString());
                finish();
            }
        });
        save.setEnabled(false);

        titleBar
        .setDisplayUpEnabled(true)
        .setTitle(params.title)
        .addAction(save)
        .show();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.text_edit_fragment, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        final TextView number = (TextView) findViewById(R.id.number);
        input = (EditText) findViewById(R.id.input);
        TextView description = (TextView) findViewById(R.id.description);
        
        if (params.maxEms > 0)
        {
            // 计算剩余可输入字数
            input.addTextChangedListener(new TextWatcher() {
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void afterTextChanged(Editable s) {
                    int maxEms = params.maxEms * 2;
                    for (int i = 0, len = s.length(); i < len; i++)
                    {
                        int count = maxEms - (StringUtil.isDoubleByte(s, i) ? 2 : 1);
                        if (count < 0)
                        {
                            s.delete(i, len);
                            break;
                        
                        }
                        else
                        {
                            maxEms = count;
                        }
                    }
                    
                    number.setText(String.valueOf(Math.round(maxEms / 2.0f)));
                }
            });
        }
        input.setText((CharSequence) getData());
        // 比较新旧文本
        input.addTextChangedListener(new TextWatcher() {
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                save.setEnabled(!s.toString().equals(Util.getString(getData(), "")));
            }
        });
        
        description.setText(params.desc);
    }
    
    /**
     * 监听文本变化
     * 
     * @param text 初始文本
     */
    public void setListener(String text, Listener<String> listener) {
        super.setListener(text, listener);
    }
}