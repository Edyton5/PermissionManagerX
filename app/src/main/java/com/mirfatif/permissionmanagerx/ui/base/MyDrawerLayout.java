package com.mirfatif.permissionmanagerx.ui.base;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import com.mirfatif.permissionmanagerx.util.UtilsFlavor;

public class MyDrawerLayout extends DrawerLayout {

  public MyDrawerLayout(@NonNull Context context) {
    super(context);
    UtilsFlavor.onCreateLayout(this);
  }

  public MyDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    UtilsFlavor.onCreateLayout(this);
  }

  public MyDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    UtilsFlavor.onCreateLayout(this);
  }
}
