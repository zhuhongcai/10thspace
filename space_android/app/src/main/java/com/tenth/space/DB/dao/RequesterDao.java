package com.tenth.space.DB.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.tenth.space.DB.entity.RequesterEntity;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table RequesterInfo.
*/
public class RequesterDao extends AbstractDao<RequesterEntity, Long> {

    public static final String TABLENAME = "RequesterInfo";

    /**
     * Properties of entity RequesterEntity.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Avatar_url = new Property(1, String.class, "avatar_url", false, "AVATAR_URL");
        public final static Property Created = new Property(2, Long.class, "created", false, "CREATED");
        public final static Property IsRead = new Property(3, Boolean.class, "isRead", false, "IS_READ");
        public final static Property Nick_name = new Property(4, String.class, "nick_name", false, "NICK_NAME");
        public final static Property FromUserId = new Property(5, int.class, "fromUserId", false, "FROM_USER_ID");
        public final static Property Addition_msg = new Property(6, String.class, "addition_msg", false, "ADDITION_MSG");
        public final static Property Agree_states = new Property(7, int.class, "agree_states", false, "AGREE_STATES");
    };


    public RequesterDao(DaoConfig config) {
        super(config);
    }
    
    public RequesterDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'RequesterInfo' (" + //
                "'_id' INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "'AVATAR_URL' TEXT," + // 1: avatar_url
                "'CREATED' INTEGER," + // 2: created
                "'IS_READ' INTEGER," + // 3: isRead
                "'NICK_NAME' TEXT NOT NULL ," + // 4: nick_name
                "'FROM_USER_ID' INTEGER NOT NULL UNIQUE ," + // 5: fromUserId
                "'ADDITION_MSG' TEXT," + // 6: addition_msg
                "'AGREE_STATES' INTEGER NOT NULL );"); // 7: agree_states
        // Add Indexes
        db.execSQL("CREATE INDEX " + constraint + "IDX_RequesterInfo_FROM_USER_ID ON RequesterInfo" +
                " (FROM_USER_ID);");
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'RequesterInfo'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, RequesterEntity entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String avatar_url = entity.getAvatar_url();
        if (avatar_url != null) {
            stmt.bindString(2, avatar_url);
        }
 
        Long created = entity.getCreated();
        if (created != null) {
            stmt.bindLong(3, created);
        }
 
        Boolean isRead = entity.getIsRead();
        if (isRead != null) {
            stmt.bindLong(4, isRead ? 1l: 0l);
        }
        stmt.bindString(5, entity.getNick_name());
        stmt.bindLong(6, entity.getFromUserId());
 
        String addition_msg = entity.getAddition_msg();
        if (addition_msg != null) {
            stmt.bindString(7, addition_msg);
        }
        stmt.bindLong(8, entity.getAgree_states());
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public RequesterEntity readEntity(Cursor cursor, int offset) {
        RequesterEntity entity = new RequesterEntity( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // avatar_url
            cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2), // created
            cursor.isNull(offset + 3) ? null : cursor.getShort(offset + 3) != 0, // isRead
            cursor.getString(offset + 4), // nick_name
            cursor.getInt(offset + 5), // fromUserId
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // addition_msg
            cursor.getInt(offset + 7) // agree_states
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, RequesterEntity entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setAvatar_url(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setCreated(cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2));
        entity.setIsRead(cursor.isNull(offset + 3) ? null : cursor.getShort(offset + 3) != 0);
        entity.setNick_name(cursor.getString(offset + 4));
        entity.setFromUserId(cursor.getInt(offset + 5));
        entity.setAddition_msg(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setAgree_states(cursor.getInt(offset + 7));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(RequesterEntity entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(RequesterEntity entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
