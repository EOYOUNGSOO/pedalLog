package app.pedalLog.android.data.db.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import app.pedalLog.android.data.db.WaypointsTypeConverter;
import app.pedalLog.android.data.db.entity.RidingTemplateEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class RidingTemplateDao_Impl implements RidingTemplateDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<RidingTemplateEntity> __insertionAdapterOfRidingTemplateEntity;

  private final WaypointsTypeConverter __waypointsTypeConverter = new WaypointsTypeConverter();

  private final EntityDeletionOrUpdateAdapter<RidingTemplateEntity> __deletionAdapterOfRidingTemplateEntity;

  private final EntityDeletionOrUpdateAdapter<RidingTemplateEntity> __updateAdapterOfRidingTemplateEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateFavorite;

  public RidingTemplateDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRidingTemplateEntity = new EntityInsertionAdapter<RidingTemplateEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `riding_templates` (`id`,`courseName`,`departure`,`destination`,`waypoints`,`bikeType`,`defaultNote`,`isFavorite`,`sortOrder`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RidingTemplateEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getCourseName());
        statement.bindString(3, entity.getDeparture());
        statement.bindString(4, entity.getDestination());
        final String _tmp = __waypointsTypeConverter.fromWaypoints(entity.getWaypoints());
        statement.bindString(5, _tmp);
        statement.bindString(6, entity.getBikeType());
        statement.bindString(7, entity.getDefaultNote());
        final int _tmp_1 = entity.isFavorite() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        statement.bindLong(9, entity.getSortOrder());
      }
    };
    this.__deletionAdapterOfRidingTemplateEntity = new EntityDeletionOrUpdateAdapter<RidingTemplateEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `riding_templates` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RidingTemplateEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfRidingTemplateEntity = new EntityDeletionOrUpdateAdapter<RidingTemplateEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `riding_templates` SET `id` = ?,`courseName` = ?,`departure` = ?,`destination` = ?,`waypoints` = ?,`bikeType` = ?,`defaultNote` = ?,`isFavorite` = ?,`sortOrder` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RidingTemplateEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getCourseName());
        statement.bindString(3, entity.getDeparture());
        statement.bindString(4, entity.getDestination());
        final String _tmp = __waypointsTypeConverter.fromWaypoints(entity.getWaypoints());
        statement.bindString(5, _tmp);
        statement.bindString(6, entity.getBikeType());
        statement.bindString(7, entity.getDefaultNote());
        final int _tmp_1 = entity.isFavorite() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        statement.bindLong(9, entity.getSortOrder());
        statement.bindLong(10, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateFavorite = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE riding_templates SET isFavorite = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final RidingTemplateEntity template,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfRidingTemplateEntity.insertAndReturnId(template);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final RidingTemplateEntity template,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfRidingTemplateEntity.handle(template);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final RidingTemplateEntity template,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfRidingTemplateEntity.handle(template);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateFavorite(final long id, final boolean isFavorite,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateFavorite.acquire();
        int _argIndex = 1;
        final int _tmp = isFavorite ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateFavorite.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<RidingTemplateEntity>> observeAll() {
    final String _sql = "SELECT * FROM riding_templates ORDER BY isFavorite DESC, sortOrder ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"riding_templates"}, new Callable<List<RidingTemplateEntity>>() {
      @Override
      @NonNull
      public List<RidingTemplateEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCourseName = CursorUtil.getColumnIndexOrThrow(_cursor, "courseName");
          final int _cursorIndexOfDeparture = CursorUtil.getColumnIndexOrThrow(_cursor, "departure");
          final int _cursorIndexOfDestination = CursorUtil.getColumnIndexOrThrow(_cursor, "destination");
          final int _cursorIndexOfWaypoints = CursorUtil.getColumnIndexOrThrow(_cursor, "waypoints");
          final int _cursorIndexOfBikeType = CursorUtil.getColumnIndexOrThrow(_cursor, "bikeType");
          final int _cursorIndexOfDefaultNote = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultNote");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final List<RidingTemplateEntity> _result = new ArrayList<RidingTemplateEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RidingTemplateEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCourseName;
            _tmpCourseName = _cursor.getString(_cursorIndexOfCourseName);
            final String _tmpDeparture;
            _tmpDeparture = _cursor.getString(_cursorIndexOfDeparture);
            final String _tmpDestination;
            _tmpDestination = _cursor.getString(_cursorIndexOfDestination);
            final List<String> _tmpWaypoints;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfWaypoints);
            _tmpWaypoints = __waypointsTypeConverter.toWaypoints(_tmp);
            final String _tmpBikeType;
            _tmpBikeType = _cursor.getString(_cursorIndexOfBikeType);
            final String _tmpDefaultNote;
            _tmpDefaultNote = _cursor.getString(_cursorIndexOfDefaultNote);
            final boolean _tmpIsFavorite;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp_1 != 0;
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
            _item = new RidingTemplateEntity(_tmpId,_tmpCourseName,_tmpDeparture,_tmpDestination,_tmpWaypoints,_tmpBikeType,_tmpDefaultNote,_tmpIsFavorite,_tmpSortOrder);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAllOrdered(final Continuation<? super List<RidingTemplateEntity>> $completion) {
    final String _sql = "SELECT * FROM riding_templates ORDER BY isFavorite DESC, sortOrder ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RidingTemplateEntity>>() {
      @Override
      @NonNull
      public List<RidingTemplateEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCourseName = CursorUtil.getColumnIndexOrThrow(_cursor, "courseName");
          final int _cursorIndexOfDeparture = CursorUtil.getColumnIndexOrThrow(_cursor, "departure");
          final int _cursorIndexOfDestination = CursorUtil.getColumnIndexOrThrow(_cursor, "destination");
          final int _cursorIndexOfWaypoints = CursorUtil.getColumnIndexOrThrow(_cursor, "waypoints");
          final int _cursorIndexOfBikeType = CursorUtil.getColumnIndexOrThrow(_cursor, "bikeType");
          final int _cursorIndexOfDefaultNote = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultNote");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final List<RidingTemplateEntity> _result = new ArrayList<RidingTemplateEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RidingTemplateEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCourseName;
            _tmpCourseName = _cursor.getString(_cursorIndexOfCourseName);
            final String _tmpDeparture;
            _tmpDeparture = _cursor.getString(_cursorIndexOfDeparture);
            final String _tmpDestination;
            _tmpDestination = _cursor.getString(_cursorIndexOfDestination);
            final List<String> _tmpWaypoints;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfWaypoints);
            _tmpWaypoints = __waypointsTypeConverter.toWaypoints(_tmp);
            final String _tmpBikeType;
            _tmpBikeType = _cursor.getString(_cursorIndexOfBikeType);
            final String _tmpDefaultNote;
            _tmpDefaultNote = _cursor.getString(_cursorIndexOfDefaultNote);
            final boolean _tmpIsFavorite;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp_1 != 0;
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
            _item = new RidingTemplateEntity(_tmpId,_tmpCourseName,_tmpDeparture,_tmpDestination,_tmpWaypoints,_tmpBikeType,_tmpDefaultNote,_tmpIsFavorite,_tmpSortOrder);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getById(final long id,
      final Continuation<? super RidingTemplateEntity> $completion) {
    final String _sql = "SELECT * FROM riding_templates WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<RidingTemplateEntity>() {
      @Override
      @Nullable
      public RidingTemplateEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCourseName = CursorUtil.getColumnIndexOrThrow(_cursor, "courseName");
          final int _cursorIndexOfDeparture = CursorUtil.getColumnIndexOrThrow(_cursor, "departure");
          final int _cursorIndexOfDestination = CursorUtil.getColumnIndexOrThrow(_cursor, "destination");
          final int _cursorIndexOfWaypoints = CursorUtil.getColumnIndexOrThrow(_cursor, "waypoints");
          final int _cursorIndexOfBikeType = CursorUtil.getColumnIndexOrThrow(_cursor, "bikeType");
          final int _cursorIndexOfDefaultNote = CursorUtil.getColumnIndexOrThrow(_cursor, "defaultNote");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final RidingTemplateEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCourseName;
            _tmpCourseName = _cursor.getString(_cursorIndexOfCourseName);
            final String _tmpDeparture;
            _tmpDeparture = _cursor.getString(_cursorIndexOfDeparture);
            final String _tmpDestination;
            _tmpDestination = _cursor.getString(_cursorIndexOfDestination);
            final List<String> _tmpWaypoints;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfWaypoints);
            _tmpWaypoints = __waypointsTypeConverter.toWaypoints(_tmp);
            final String _tmpBikeType;
            _tmpBikeType = _cursor.getString(_cursorIndexOfBikeType);
            final String _tmpDefaultNote;
            _tmpDefaultNote = _cursor.getString(_cursorIndexOfDefaultNote);
            final boolean _tmpIsFavorite;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp_1 != 0;
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
            _result = new RidingTemplateEntity(_tmpId,_tmpCourseName,_tmpDeparture,_tmpDestination,_tmpWaypoints,_tmpBikeType,_tmpDefaultNote,_tmpIsFavorite,_tmpSortOrder);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
