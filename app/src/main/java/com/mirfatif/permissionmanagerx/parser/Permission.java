package com.mirfatif.permissionmanagerx.parser;

import android.text.TextUtils;
import android.text.format.DateUtils;
import com.mirfatif.permissionmanagerx.prefs.MySettings;
import java.util.concurrent.TimeUnit;

public class Permission {

  public static final String PROTECTION_DANGEROUS = "Dangerous";
  public static final String PROTECTION_NORMAL = "Normal";
  public static final String PROTECTION_SIGNATURE = "Signature";
  public static final String GRANTED = "Granted";
  public static final String REVOKED = "Revoked";

  private final int mOrder;
  private final Integer mIconResId;
  private final boolean mIsAppOps;
  private final boolean mIsPerUid;
  private final boolean mIsAppOpsSet;
  private int mAppOpsMode;
  private final long mAppOpsAccessTime;
  private String mAppOpsAccessTimeFormatted;
  private final String mDependsOn;
  private boolean mIsExtraAppOp;
  private final String mPackageName;
  private final String mPermissionName;
  private final boolean mIsGranted;
  private final String mProtectionLevel;
  private final boolean mIsPrivileged;
  private final boolean mIsDevelopment;
  private final boolean mIsManifestPermAppOps;
  private final boolean mIsSystemFixed;
  private final boolean mProviderMissing;
  private final Boolean mIsReferenced;
  private final String mReference;
  private final boolean mIsSystemApp;
  private final CharSequence mPermDesc;

  Permission(
      int order,
      Integer iconResId,
      boolean isAppOps,
      boolean isPerUid,
      boolean isAppOpsSet,
      int appOpsMode,
      long appOpsAccessTime,
      String dependsOn,
      boolean isExtraAppOp,
      String packageName,
      String name,
      boolean isGranted,
      String protectionLevel,
      boolean isPrivileged,
      boolean isDevelopment,
      boolean isManifestPermAppOps,
      boolean isSystemFixed,
      boolean providerMissing,
      Boolean isReferenced,
      String reference,
      boolean isSystemApp,
      CharSequence permDesc) {
    mOrder = order;
    mIconResId = iconResId;
    mIsAppOps = isAppOps;
    mIsPerUid = isPerUid;
    mIsAppOpsSet = isAppOpsSet;
    mAppOpsMode = appOpsMode;
    mAppOpsAccessTime = appOpsAccessTime;
    mDependsOn = dependsOn;
    mIsExtraAppOp = isExtraAppOp;
    mPackageName = packageName;
    mPermissionName = name;
    mIsGranted = isGranted;
    mProtectionLevel = protectionLevel;
    mIsPrivileged = isPrivileged;
    mIsDevelopment = isDevelopment;
    mIsManifestPermAppOps = isManifestPermAppOps;
    mIsSystemFixed = isSystemFixed;
    mProviderMissing = providerMissing;
    mIsReferenced = isReferenced;
    mReference = reference;
    mIsSystemApp = isSystemApp;
    mPermDesc = permDesc;
  }

  public int getOrder() {
    return mOrder;
  }

  public Integer getIconResId() {
    return mIconResId;
  }

  public boolean isAppOps() {
    return mIsAppOps;
  }

  public boolean isPerUid() {
    return mIsPerUid;
  }

  public boolean isAppOpsSet() {
    return mIsAppOpsSet;
  }

  public void setAppOpsMode(int mode) {
    mAppOpsMode = mode;
  }

  public int getAppOpsMode() {
    return mAppOpsMode;
  }

  public String getAppOpsAccessTime() {
    // Do not show time older than a year, including zero epoch time and -1
    if (System.currentTimeMillis() - mAppOpsAccessTime > TimeUnit.DAYS.toMillis(365)) {
      return null;
    }
    mAppOpsAccessTimeFormatted =
        DateUtils.getRelativeTimeSpanString(
                mAppOpsAccessTime, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS)
            .toString();
    return mAppOpsAccessTimeFormatted;
  }

  public String dependsOn() {
    return mDependsOn;
  }

  public void setExtraAppOp() {
    mIsExtraAppOp = true;
  }

  public boolean isExtraAppOp() {
    return mIsExtraAppOp;
  }

  public String getName() {
    return mPermissionName;
  }

  public boolean isGranted() {
    return mIsGranted;
  }

  public String getProtectionLevel() {
    return mProtectionLevel;
  }

  public boolean isPrivileged() {
    return mIsPrivileged;
  }

  public boolean isProviderMissing() {
    return mProviderMissing;
  }

  public Boolean isReferenced() {
    return mIsReferenced;
  }

  public String getReference() {
    return mReference;
  }

  public boolean isChangeable() {
    if (MySettings.getInstance().isCriticalApp(mPackageName)) {
      return false;
    }
    if (mIsAppOps) {
      return mDependsOn == null;
    } else {
      // BasePermission.java#enforceDeclaredUsedAndRuntimeOrDevelopment()
      return (mProtectionLevel.equals(PROTECTION_DANGEROUS) || mIsDevelopment)
          && (!mIsSystemApp || !mIsPrivileged)
          && !mIsSystemFixed;
    }
  }

  public CharSequence getDescription() {
    return mPermDesc;
  }

  public String createPermNameString() {
    String permName = mPermissionName;
    if (mIsAppOps && mDependsOn != null) {
      permName += " (" + mDependsOn + ")";
    }
    return permName;
  }

  public String createProtectLevelString() {
    String protectionLevel = mProtectionLevel;
    if (mIsDevelopment) {
      protectionLevel += ", Development"; // implies "Signature"
    }
    if (mIsManifestPermAppOps) {
      protectionLevel += ", AppOps"; // implies "Signature"
    }
    if (mIsSystemFixed) {
      protectionLevel += ", Fixed";
    }

    if (mIsAppOps) {
      if (mIsPerUid) {
        protectionLevel += ", UID mode";
      }
      if (mIsExtraAppOp) {
        protectionLevel += ", Extra";
      }
    } else {
      if (mIsPrivileged) {
        protectionLevel += ", Privileged";
      }
    }
    return protectionLevel;
  }

  public boolean contains(String queryText) {
    boolean isEmpty = true;
    for (String str : queryText.split("\\|")) {
      if (TextUtils.isEmpty(str)) {
        continue;
      }
      isEmpty = false;
      if (contains_(str)) {
        return true;
      }
    }
    return isEmpty;
  }

  private boolean contains_(String queryText) {
    for (String str : queryText.split("&")) {
      if (TextUtils.isEmpty(str)) {
        continue;
      }
      if (!_contains(str)) {
        return false;
      }
    }
    return true;
  }

  public static final String SEARCH_APP_OPS = ":AppOps";
  public static final String SEARCH_UID = ":UID";
  public static final String SEARCH_PRIVILEGED = ":Privileged";
  public static final String SEARCH_DEV = ":Development";
  public static final String SEARCH_FIXED = ":Fixed";
  public static final String SEARCH_TIME = ":TIME";
  public static final String SEARCH_EXTRA = ":Extra";

  private boolean _contains(String queryText) {
    boolean isCaseSensitive = MySettings.getInstance().isCaseSensitiveSearch();
    if (!isCaseSensitive) {
      queryText = queryText.toUpperCase();
    }

    boolean contains = true;
    if (queryText.startsWith("!")) {
      queryText = queryText.replaceAll("^!", "");
      contains = false;
    }

    for (String field :
        new String[] {
          mPermissionName,
          ":" + mProtectionLevel,
          ((mIsAppOps || mIsManifestPermAppOps) ? SEARCH_APP_OPS : ""),
          ((mIsAppOps && mIsPerUid) ? SEARCH_UID : ""),
          (mIsPrivileged ? SEARCH_PRIVILEGED : ""),
          (mIsDevelopment ? SEARCH_DEV : ""),
          (mIsSystemFixed ? SEARCH_FIXED : ""),
          (mIsReferenced == null
              ? Package.SEARCH_ORANGE
              : (mIsReferenced ? Package.SEARCH_GREEN : Package.SEARCH_RED)),
          getAppOpsAccessTime() != null ? SEARCH_TIME : "",
          (mIsExtraAppOp ? SEARCH_EXTRA : "")
        }) {
      if (!isCaseSensitive) {
        field = field.toUpperCase();
      }
      if (field.contains(queryText)) {
        return contains;
      }
    }
    return !contains;
  }

  // Required for ListAdapter/DiffUtil. Consider which fields can change in a Permission.
  public boolean areContentsTheSame(Permission newPerm) {
    if (isReferenced() != null) {
      if (!isReferenced().equals(newPerm.isReferenced())) {
        return false;
      }
    } else if (newPerm.isReferenced() != null) {
      return false;
    }

    if (!isAppOps()) {
      return isGranted() == newPerm.isGranted();
    }

    if (getAppOpsMode() != newPerm.getAppOpsMode()) {
      return false;
    }

    if (isAppOpsSet() != newPerm.isAppOpsSet()) {
      return false;
    }

    // Compare the new Permission Object's calculated value with previously one's saved value,
    // even if both Objects are the same.
    // If calculated on both, the values are always the same, so UI changes cannot be compared.
    if (mAppOpsAccessTimeFormatted != null) {
      if (!mAppOpsAccessTimeFormatted.equals(newPerm.getAppOpsAccessTime())) {
        return false;
      }
    } else if (newPerm.getAppOpsAccessTime() != null) {
      return false;
    }

    return isExtraAppOp() == newPerm.isExtraAppOp();
  }
}
