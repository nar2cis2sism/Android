//package com.project.app;
//
//import android.app.Fragment;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.DialogInterface.OnDismissListener;
//import android.content.Intent;
//
//import com.google.gson.reflect.TypeToken;
//import com.project.app.bean.GameData;
//import com.project.network.action.Actions;
//import com.project.network.http.servlet.HttpServlet;
//import com.project.network.socket.servlet.SocketServlet;
//import com.project.storage.db.User;
//import com.project.ui.guide.GuideActivity;
//import com.project.ui.home.game.GameActivity;
//import com.project.ui.home.game.GameActivity.GameParams;
//import com.project.ui.home.game.GameOverDialog;
//import com.project.ui.home.game.GameResultDialog;
//import com.project.ui.home.invite.BeInviteDialog;
//import com.project.ui.home.match.MatchFragment;
//import com.project.ui.home.match.MatchFragment.MatchParams;
//import com.project.ui.home.share.ShareActivity;
//import com.project.ui.home.share.ShareActivity.ShareGold;
//import com.project.ui.home.subject.SubjectActivity;
//import com.project.ui.home.subject.SubjectActivity.BeSubjectParams;
//import com.project.ui.home.subject.SubjectActivity.SubjectParams;
//import com.project.ui.me.info.SelectSchoolFragment;
//import com.tongxuezhan.tongxue.R;
//
//import org.json.JSONObject;
//
//import java.util.List;
//
//import engine.android.core.util.LogFactory.LOG;
//import engine.android.framework.protocol.http.UserData;
//import engine.android.framework.protocol.socket.GameOverData;
//import engine.android.framework.protocol.socket.InviteData;
//import engine.android.framework.protocol.socket.MatchData;
//import engine.android.framework.protocol.socket.QuestionData;
//import engine.android.framework.protocol.socket.RoomMember;
//import engine.android.framework.protocol.socket.SelectSubjectData;
//import engine.android.framework.ui.BaseFragment.ParamsBuilder;
//import engine.android.framework.util.GsonUtil;
//import engine.android.util.StringUtil;
//import engine.android.util.file.FileManager;
//
//public class MyTest implements Actions {
//    
//    public static void init() {
//        LOG.log("单元测试");
//
//        MyApp.global().getConfig().configNetwork().setOffline(true);
//        MyApp.global().getSocketManager().setToken(0, "123");
//        MyApp.global().getSocketManager().setup("123:123");
//        MyApp.global().getConfig().configNetwork().setOffline(false);
//
//        UserData data = GsonUtil.parseJson(readHttpData(GET_ME_INFO), UserData.class);
//        MySession.setUser(new User().fromProtocol(data));
//
//        MySession.setGameData(GsonUtil.parseJson(readHttpData(GET_USER_INFO), GameData.class));
//    }
//    
//    public static String readHttpData(String fileName) {
//        try {
//            String json = new String(FileManager.readFile(HttpServlet.class, fileName));
//            return new JSONObject(json).optString("data");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        
//        return null;
//    }
//    
//    public static String readSocketData(int cmd) {
//        try {
//            return new String(FileManager.readFile(SocketServlet.class, String.valueOf(cmd)));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        
//        return null;
//    }
//
//    public static void startGameActivity(Context context, int vs) {
//        GameParams params = new GameParams();
//        params.data = GsonUtil.parseJson(readSocketData(CMD.MATCH_SUCCESS), MatchData.class);
//        params.question = GsonUtil.parseJson(readSocketData(CMD.QUESTION), QuestionData.class);
//        
//        if (vs > 1)
//        {
//            for (int i = 1; i < vs; i++)
//            {
//                params.data.team1.add(params.data.team1.get(0));
//                params.data.team2.add(params.data.team2.get(0));
//            }
//        }
//        
//        context.startActivity(GameActivity.buildIntent(context, params));
//    }
//
//    public static void startSubjectActivity(Context context, boolean beSubject) {
//        if (beSubject)
//        {
//            SelectSubjectData data = GsonUtil.parseJson(StringUtil.adjustJson(readSocketData(CMD.BE_SELECT_SUBJECT)), SelectSubjectData.class);
//            context.startActivity(new Intent(context, SubjectActivity.class)
//                    .putExtra(SubjectActivity.EXTRA_TYPE, 2)
//                    .putExtras(ParamsBuilder.build(new BeSubjectParams(data))));
//        }
//        else
//        {
//            List<RoomMember> list = GsonUtil.parseJson(readSocketData(CMD.CREATE_ROOM_ACK), new TypeToken<List<RoomMember>>() {}.getType());
//            context.startActivity(new Intent(context, SubjectActivity.class)
//                    .putExtra(SubjectActivity.EXTRA_TYPE, 1)
//                    .putExtras(ParamsBuilder.build(new SubjectParams(list))));
//        }
//    }
//    
//    public static void startGuideActivity(Context context) {
//        context.startActivity(new Intent(context, GuideActivity.class));
//    }
//    
//    public static void showGameOverDialog(final Context context) {
//        GameOverData data = GsonUtil.parseJson(readSocketData(CMD.GAME_OVER), GameOverData.class);
//
//        GameOverDialog dialog = new GameOverDialog(context, data.result);
//        dialog.setOnDismissListener(new OnDismissListener() {
//            
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                showGameResultDialog(context);
//            }
//        });
//        
//        dialog.show();
//    }
//    
//    public static void showGameResultDialog(final Context context) {
//        GameOverData data = GsonUtil.parseJson(readSocketData(CMD.GAME_OVER), GameOverData.class);
//        QuestionData question = GsonUtil.parseJson(readSocketData(CMD.QUESTION), QuestionData.class);
//        
//        data.detail.team1.add(data.detail.team1.get(0));
//        data.detail.team1.add(data.detail.team1.get(0));
//        data.detail.team1.add(data.detail.team1.get(0));
//        data.detail.team1.add(data.detail.team1.get(0));
//        
//        data.detail.team2.add(data.detail.team2.get(0));
//        data.detail.team2.add(data.detail.team2.get(0));
//        data.detail.team2.add(data.detail.team2.get(0));
//        data.detail.team2.add(data.detail.team2.get(0));
//
//        GameResultDialog dialog = new GameResultDialog(context, question, data);
//        dialog.setListener(new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                switch (which) {
//                    case R.id.again:
//                        dialog.dismiss();
//                        break;
//                    case R.id.share:
//                        context.startActivity(ShareActivity.buildIntent(context, new ShareGold()));
//                        break;
//                }
//            }
//        });
//        dialog.show();
//    }
//
//    public static Fragment getMatchFragment() {
//        String json = "{\"memberList\":[{\"curHealth\":139.1,\"damage\":13.91,\"defense\":0.0,\"gender\":1,\"gradeName\":\"一年级\",\"health\":139.1,\"imgPath\":\"http://dailu-adv01.oss-cn-shenzhen.aliyuncs.com/item_1550625197172609?Expires\\u003d1865985197\",\"isOwer\":1,\"islive\":1,\"nickName\":\"噜噜噜噜啦啦啦啦啦啦昊哥\",\"petId\":2,\"petLevel\":\"LV.7\",\"petName\":\"章鱼\",\"picUrl\":\"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx_pet_2\",\"schoolName\":\"新鲜胡同小学\",\"status\":1,\"teamId\":\"7BFAFB5B69484D04AF7499E82CBA659D\",\"title\":\"学残\",\"userId\":10098,\"userLevel\":\"level04\"}],\"teamId\":\"7BFAFB5B69484D04AF7499E82CBA659D\",\"teamList\":[{\"isOwer\":0,\"nickName\":\"噜噜噜噜啦啦啦啦啦啦昊哥\",\"score\":20.0,\"subjectId\":66,\"userId\":10098}]}\n";
//        MatchParams params = GsonUtil.parseJson(json, MatchParams.class);
//
//        MatchFragment fragment = new MatchFragment();
//        fragment.setArguments(ParamsBuilder.build(params));
//        return fragment;
//    }
//
//    public static Fragment getSelectSchoolFragment() {
//        SelectSchoolFragment fragment = new SelectSchoolFragment();
//        fragment.setListener(MySession.getUser(), null);
//        return fragment;
//    }
//
//    public static void showInviteDialog(Context context) {
//        InviteData data = GsonUtil.parseJson(readSocketData(CMD.BE_INVITE), InviteData.class);
//        BeInviteDialog dialog = new BeInviteDialog(context, data);
//        dialog.show();
//    }
//}