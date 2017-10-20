package com.project.storage.dao;

import com.project.storage.MyDAOManager.BaseDAO;
import com.project.storage.db.User;
import com.project.storage.provider.ProviderContract.UserColumns;

public class UserDAO extends BaseDAO {
    
    /**
     * 根据用户ID获取用户信息
     */
    public static User getUserById(long uid) {
        return findItemByProperty(User.class, UserColumns.USER_ID, uid);
    }
//    
//    public static User addUser(long uid) {
//        User user = new User();
//        user.uid = uid;
//        
//        getDAO().save(user);
//        return user;
//    }
//    
//    public static void updateUser(User user) {
//        DAOExpression whereClause
//        = DAOExpression.create(UserColumns._ID).equal(user.uid);
//        
//        getDAO().edit(
//                DAOSQLBuilder.create(User.class)
//                .setWhereClause(whereClause), 
//                user, (String[]) null);
//    }
//    
//    /**
//     * 获取医疗数据
//     */
//    
//    public static <T> T getData(int dataId, Class<T> dataClass) {
//        if (dataClass == Area.class)
//        {
//            return dataClass.cast(getArea(dataId));
//        }
//        
//        DAOExpression whereClause
//        = DAOExpression.create(BaseColumns._ID).equal(dataId);
//        
//        return getDAO().find(
//                DAOQueryBuilder.create(dataClass)
//                .setWhereClause(whereClause), 
//                dataClass);
//    }
//    
//    private static Area getArea(int areaId) {
//        Area area = null;
//        
//        SQLiteDatabase dao = AreaDataBase.getDAO();
//        Cursor cursor = dao.query(AreaDataBase.TABLE, null, 
//                AreaColumns._ID + "=?", 
//                new String[]{String.valueOf(areaId)}, 
//                null, null, null, "1");
//        if (cursor != null)
//        {
//            if (cursor.moveToFirst())
//            {
//                area = DAOTemplate.convertFromCursor(cursor, Area.class);
//            }
//            
//            cursor.close();
//        }
//        
//        return area;
//    }
}