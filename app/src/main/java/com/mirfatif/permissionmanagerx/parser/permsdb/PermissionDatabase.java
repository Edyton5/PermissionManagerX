package com.mirfatif.permissionmanagerx.parser.permsdb;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = PermissionEntity.class, version = 2, exportSchema = false)
public abstract class PermissionDatabase extends RoomDatabase {

  public abstract PermissionDao permissionDao();
}
