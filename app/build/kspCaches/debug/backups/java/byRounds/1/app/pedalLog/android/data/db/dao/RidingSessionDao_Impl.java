package app.pedalLog.android.data.db.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import app.pedalLog.android.data.db.WaypointsTypeConverter;
import app.pedalLog.android.data.db.entity.RidingSessionEntity;
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
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class RidingSessionDao_Impl implements RidingSessionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<RidingSessionEntity> __insertionAdapterOfRidingSessionEntity;

  private final WaypointsTypeConverter __waypointsTypeConverter = new WaypointsTypeConverter();

  public RidingSessionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRidingSessionEntity = new EntityInsertionAdapter<RidingSessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `riding_sessions` (`id`,`courseName`,`departure`,`destination`,`waypoints`,`bikeType`,`note`,`startedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RidingSessionEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getCourseName());
        statement.bindString(3, entity.getDeparture());
        statement.bindString(4, entity.getDestination());
        final String _tmp = __waypointsTypeConverter.fromWaypoints(entity.getWaypoints());
        statement.bindString(5, _tmp);
        statement.bindString(6, entity.getBikeType());
        statement.bindString(7, entity.getNote());
        statement.bindLong(8, entity.getStartedAt());
      }
    };
  }

  @Override
  public Object insert(final RidingSessionEntity session,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfRidingSessionEntity.insertAndReturnId(session);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<RidingSessionEntity>> observeRecent() {
    final String _sql = "SELECT * FROM riding_sessions ORDER BY startedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"riding_sessions"}, new Callable<List<RidingSessionEntity>>() {
      @Override
      @NonNull
      public List<RidingSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCourseName = CursorUtil.getColumnIndexOrThrow(_cursor, "courseName");
          final int _cursorIndexOfDeparture = CursorUtil.getColumnIndexOrThrow(_cursor, "departure");
          final int _cursorIndexOfDestination = CursorUtil.getColumnIndexOrThrow(_cursor, "destination");
          final int _cursorIndexOfWaypoints = CursorUtil.getColumnIndexOrThrow(_cursor, "waypoints");
          final int _cursorIndexOfBikeType = CursorUtil.getColumnIndexOrThrow(_cursor, "bikeType");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfStartedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "startedAt");
          final List<RidingSessionEntity> _result = new ArrayList<RidingSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RidingSessionEntity _item;
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
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            final long _tmpStartedAt;
            _tmpStartedAt = _cursor.getLong(_cursorIndexOfStartedAt);
            _item = new RidingSessionEntity(_tmpId,_tmpCourseName,_tmpDeparture,_tmpDestination,_tmpWaypoints,_tmpBikeType,_tmpNote,_tmpStartedAt);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
