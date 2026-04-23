package app.pedalLog.android.data.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import app.pedalLog.android.data.db.dao.BikeTypeDao;
import app.pedalLog.android.data.db.dao.BikeTypeDao_Impl;
import app.pedalLog.android.data.db.dao.RidingSessionDao;
import app.pedalLog.android.data.db.dao.RidingSessionDao_Impl;
import app.pedalLog.android.data.db.dao.RidingTemplateDao;
import app.pedalLog.android.data.db.dao.RidingTemplateDao_Impl;
import app.pedalLog.android.data.db.dao.TrackPointDao;
import app.pedalLog.android.data.db.dao.TrackPointDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile RidingSessionDao _ridingSessionDao;

  private volatile TrackPointDao _trackPointDao;

  private volatile RidingTemplateDao _ridingTemplateDao;

  private volatile BikeTypeDao _bikeTypeDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `riding_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `courseName` TEXT NOT NULL, `departure` TEXT NOT NULL, `destination` TEXT NOT NULL, `waypoints` TEXT NOT NULL, `bikeType` TEXT NOT NULL, `note` TEXT NOT NULL, `startedAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `track_points` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sessionId` INTEGER NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `altitude` REAL, `heartRate` INTEGER, `timestamp` INTEGER NOT NULL, FOREIGN KEY(`sessionId`) REFERENCES `riding_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_track_points_sessionId` ON `track_points` (`sessionId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `riding_templates` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `courseName` TEXT NOT NULL, `departure` TEXT NOT NULL, `destination` TEXT NOT NULL, `waypoints` TEXT NOT NULL, `bikeType` TEXT NOT NULL, `defaultNote` TEXT NOT NULL, `isFavorite` INTEGER NOT NULL, `sortOrder` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `bike_types` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `sortOrder` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '6ae5635a5eb4f49e0b5ae9b5353debd1')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `riding_sessions`");
        db.execSQL("DROP TABLE IF EXISTS `track_points`");
        db.execSQL("DROP TABLE IF EXISTS `riding_templates`");
        db.execSQL("DROP TABLE IF EXISTS `bike_types`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsRidingSessions = new HashMap<String, TableInfo.Column>(8);
        _columnsRidingSessions.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRidingSessions.put("courseName", new TableInfo.Column("courseName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRidingSessions.put("departure", new TableInfo.Column("departure", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRidingSessions.put("destination", new TableInfo.Column("destination", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRidingSessions.put("waypoints", new TableInfo.Column("waypoints", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRidingSessions.put("bikeType", new TableInfo.Column("bikeType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRidingSessions.put("note", new TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRidingSessions.put("startedAt", new TableInfo.Column("startedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysRidingSessions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesRidingSessions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoRidingSessions = new TableInfo("riding_sessions", _columnsRidingSessions, _foreignKeysRidingSessions, _indicesRidingSessions);
        final TableInfo _existingRidingSessions = TableInfo.read(db, "riding_sessions");
        if (!_infoRidingSessions.equals(_existingRidingSessions)) {
          return new RoomOpenHelper.ValidationResult(false, "riding_sessions(app.pedalLog.android.data.db.entity.RidingSessionEntity).\n"
                  + " Expected:\n" + _infoRidingSessions + "\n"
                  + " Found:\n" + _existingRidingSessions);
        }
        final HashMap<String, TableInfo.Column> _columnsTrackPoints = new HashMap<String, TableInfo.Column>(7);
        _columnsTrackPoints.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackPoints.put("sessionId", new TableInfo.Column("sessionId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackPoints.put("latitude", new TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackPoints.put("longitude", new TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackPoints.put("altitude", new TableInfo.Column("altitude", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackPoints.put("heartRate", new TableInfo.Column("heartRate", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrackPoints.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTrackPoints = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysTrackPoints.add(new TableInfo.ForeignKey("riding_sessions", "CASCADE", "NO ACTION", Arrays.asList("sessionId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesTrackPoints = new HashSet<TableInfo.Index>(1);
        _indicesTrackPoints.add(new TableInfo.Index("index_track_points_sessionId", false, Arrays.asList("sessionId"), Arrays.asList("ASC")));
        final TableInfo _infoTrackPoints = new TableInfo("track_points", _columnsTrackPoints, _foreignKeysTrackPoints, _indicesTrackPoints);
        final TableInfo _existingTrackPoints = TableInfo.read(db, "track_points");
        if (!_infoTrackPoints.equals(_existingTrackPoints)) {
          return new RoomOpenHelper.ValidationResult(false, "track_points(app.pedalLog.android.data.db.entity.TrackPointEntity).\n"
                  + " Expected:\n" + _infoTrackPoints + "\n"
                  + " Found:\n" + _existingTrackPoints);
        }
        final HashMap<String, TableInfo.Column> _columnsRidingTemplates = new HashMap<String, TableInfo.Column>(9);
        _columnsRidingTemplates.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRidingTemplates.put("courseName", new TableInfo.Column("courseName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRidingTemplates.put("departure", new TableInfo.Column("departure", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRidingTemplates.put("destination", new TableInfo.Column("destination", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRidingTemplates.put("waypoints", new TableInfo.Column("waypoints", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRidingTemplates.put("bikeType", new TableInfo.Column("bikeType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRidingTemplates.put("defaultNote", new TableInfo.Column("defaultNote", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRidingTemplates.put("isFavorite", new TableInfo.Column("isFavorite", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRidingTemplates.put("sortOrder", new TableInfo.Column("sortOrder", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysRidingTemplates = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesRidingTemplates = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoRidingTemplates = new TableInfo("riding_templates", _columnsRidingTemplates, _foreignKeysRidingTemplates, _indicesRidingTemplates);
        final TableInfo _existingRidingTemplates = TableInfo.read(db, "riding_templates");
        if (!_infoRidingTemplates.equals(_existingRidingTemplates)) {
          return new RoomOpenHelper.ValidationResult(false, "riding_templates(app.pedalLog.android.data.db.entity.RidingTemplateEntity).\n"
                  + " Expected:\n" + _infoRidingTemplates + "\n"
                  + " Found:\n" + _existingRidingTemplates);
        }
        final HashMap<String, TableInfo.Column> _columnsBikeTypes = new HashMap<String, TableInfo.Column>(3);
        _columnsBikeTypes.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBikeTypes.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBikeTypes.put("sortOrder", new TableInfo.Column("sortOrder", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBikeTypes = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesBikeTypes = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoBikeTypes = new TableInfo("bike_types", _columnsBikeTypes, _foreignKeysBikeTypes, _indicesBikeTypes);
        final TableInfo _existingBikeTypes = TableInfo.read(db, "bike_types");
        if (!_infoBikeTypes.equals(_existingBikeTypes)) {
          return new RoomOpenHelper.ValidationResult(false, "bike_types(app.pedalLog.android.data.db.entity.BikeTypeEntity).\n"
                  + " Expected:\n" + _infoBikeTypes + "\n"
                  + " Found:\n" + _existingBikeTypes);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "6ae5635a5eb4f49e0b5ae9b5353debd1", "09196681dab9642a860da5c559d7425a");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "riding_sessions","track_points","riding_templates","bike_types");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `riding_sessions`");
      _db.execSQL("DELETE FROM `track_points`");
      _db.execSQL("DELETE FROM `riding_templates`");
      _db.execSQL("DELETE FROM `bike_types`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(RidingSessionDao.class, RidingSessionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TrackPointDao.class, TrackPointDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(RidingTemplateDao.class, RidingTemplateDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(BikeTypeDao.class, BikeTypeDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public RidingSessionDao ridingSessionDao() {
    if (_ridingSessionDao != null) {
      return _ridingSessionDao;
    } else {
      synchronized(this) {
        if(_ridingSessionDao == null) {
          _ridingSessionDao = new RidingSessionDao_Impl(this);
        }
        return _ridingSessionDao;
      }
    }
  }

  @Override
  public TrackPointDao trackPointDao() {
    if (_trackPointDao != null) {
      return _trackPointDao;
    } else {
      synchronized(this) {
        if(_trackPointDao == null) {
          _trackPointDao = new TrackPointDao_Impl(this);
        }
        return _trackPointDao;
      }
    }
  }

  @Override
  public RidingTemplateDao ridingTemplateDao() {
    if (_ridingTemplateDao != null) {
      return _ridingTemplateDao;
    } else {
      synchronized(this) {
        if(_ridingTemplateDao == null) {
          _ridingTemplateDao = new RidingTemplateDao_Impl(this);
        }
        return _ridingTemplateDao;
      }
    }
  }

  @Override
  public BikeTypeDao bikeTypeDao() {
    if (_bikeTypeDao != null) {
      return _bikeTypeDao;
    } else {
      synchronized(this) {
        if(_bikeTypeDao == null) {
          _bikeTypeDao = new BikeTypeDao_Impl(this);
        }
        return _bikeTypeDao;
      }
    }
  }
}
