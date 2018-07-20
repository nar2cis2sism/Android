package engine.android.framework.ui.extra;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import engine.android.framework.R;
import engine.android.framework.ui.BaseFragment;
import engine.android.widget.component.InputBox;
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
        public String text;             // 编辑文本
        public String desc;             // 描述说明
    }
    
    public static Bundle buildParams(Params params) {
        return ParamsBuilder.build(params);
    }
    
    private Params params;
    
    InputBox text;
    TextView description;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        params = ParamsBuilder.parse(getArguments(), Params.class);
        if (params == null)
        {
            finish();
        }
    }
    
    @Override
    protected void setupTitleBar(TitleBar titleBar) {
        // 保存
//        TextView save = new TextView(getContext());
//        save.setText(R.string.Save);
//        save.setTextColor(Color.parseColor("#387ec0"));
//        save.setTextColor(getResources().getColor(R.color.textColorBlue));
//        save.setOnClickListener(new OnClickListener() {
//            
//            @Override
//            public void onClick(View v) {
//                notifyCallback();
//                finish();
//            }
//        });
    
        titleBar
        .setDisplayUpEnabled(true)
        .setTitle(params.title)
        .addAction(getString(R.string.Save))
        .show();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.text_edit_fragment, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        text = (InputBox) findViewById(R.id.text);
        text.input().setText(params.text);
        description = (TextView) findViewById(R.id.description);
        description.setText(params.desc);
    }
}