package com.mirfatif.permissionmanagerx.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.mirfatif.permissionmanagerx.R;
import com.mirfatif.permissionmanagerx.Utils;
import com.mirfatif.permissionmanagerx.app.App;
import com.mirfatif.permissionmanagerx.parser.Permission;
import com.mirfatif.permissionmanagerx.prefs.MySettings;
import com.mirfatif.permissionmanagerx.ui.PermissionAdapter.ItemViewHolder;
import java.util.ArrayList;
import java.util.List;

public class PermissionAdapter extends ListAdapter<Permission, ItemViewHolder> {

  private final PermClickListener mSwitchToggleListener;
  private final PermSpinnerSelectListener mSpinnerSelectListener;
  private final PermClickListenerWithLoc mPermClickListener;
  private final PermLongClickListener mPermLongClickListener;
  private ArrayAdapter<String> mArrayAdapter;
  private ArrayAdapter<String> mArrayAdapterBg;

  PermissionAdapter(
      PermClickListener switchToggleListener,
      PermSpinnerSelectListener spinnerSelectListener,
      PermClickListenerWithLoc permClickListener,
      PermLongClickListener permLongClickListener) {
    super(new DiffUtilItemCallBack());
    mSwitchToggleListener = switchToggleListener;
    mSpinnerSelectListener = spinnerSelectListener;
    mPermClickListener = permClickListener;
    mPermLongClickListener = permLongClickListener;
  }

  // Override Adapter method
  // Inflate a View, create and return a ViewHolder
  @NonNull
  @Override
  public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    View itemView = inflater.inflate(R.layout.recycler_view_item_permission, parent, false);
    return new ItemViewHolder(itemView);
  }

  // Override RecyclerView.Adapter method
  // set contents in Views
  @Override
  public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
    holder.bind(position);
  }

  // Store and recycle items as they are scrolled off screen
  class ItemViewHolder extends RecyclerView.ViewHolder
      implements OnClickListener, OnLongClickListener {
    View referenceView;
    ImageView groupIconView;
    TextView permissionNameView;
    TextView protectionLevelView;
    TextView appOpsTimeView;
    TextView appOpsRefStateView;
    SwitchCompat stateSwitch;
    AppCompatSpinner spinner;
    LinearLayout spinnerContainer;
    TextView appOpsDefaultView;

    public ItemViewHolder(@NonNull View itemView) {
      super(itemView);

      // Find Views inside the itemView in XML layout with the given IDs
      referenceView = itemView.findViewById(R.id.reference_indication_view);
      groupIconView = itemView.findViewById(R.id.icon_view);
      permissionNameView = itemView.findViewById(R.id.permission_name_view);
      protectionLevelView = itemView.findViewById(R.id.protection_level_view);
      appOpsTimeView = itemView.findViewById(R.id.app_ops_time_view);
      appOpsRefStateView = itemView.findViewById(R.id.app_ops_ref_state_view);
      stateSwitch = itemView.findViewById(R.id.permission_state_switch);
      spinner = itemView.findViewById(R.id.permission_state_spinner);
      spinnerContainer = itemView.findViewById(R.id.permission_state_spinner_container);
      appOpsDefaultView = itemView.findViewById(R.id.app_ops_default_view);

      itemView.setOnClickListener(this);
      itemView.setOnLongClickListener(this);
    }

    public void bind(int position) {
      Permission permission = getItem(position);

      appOpsRefStateView.setVisibility(View.GONE);
      if (permission.isReferenced() == null) {
        referenceView.setBackgroundColor(PackageAdapter.ORANGE);
      } else if (!permission.isReferenced()) {
        referenceView.setBackgroundColor(Color.RED);
        if (permission.isAppOps()) {
          String refState =
              App.getContext()
                  .getResources()
                  .getString(R.string.should_be, permission.getReference());
          appOpsRefStateView.setText(refState);
          appOpsRefStateView.setVisibility(View.VISIBLE);
        }
      } else {
        referenceView.setBackgroundColor(Color.GREEN);
      }

      if (permission.getIconResId() != null) {
        groupIconView.setImageResource(permission.getIconResId());
      }

      spinnerContainer.setOnClickListener(null);

      if (permission.isAppOps()) {
        if (permission.dependsOn() == null) {
          spinnerContainer.setVisibility(View.VISIBLE);
          spinnerContainer.setOnClickListener(v -> spinner.performClick());
        } else {
          spinnerContainer.setVisibility(View.INVISIBLE);
        }
      }

      permissionNameView.setText(permission.createPermNameString());
      protectionLevelView.setText(permission.createProtectLevelString());

      appOpsTimeView.setVisibility(View.GONE);

      if (permission.isAppOps()) {
        String time = permission.getAppOpsAccessTime();
        if (!time.equals("null")) {
          appOpsTimeView.setText(time);
          appOpsTimeView.setVisibility(View.VISIBLE);
        }

        if (mArrayAdapter == null) {
          List<String> appOpsModes = MySettings.getInstance().getAppOpsModes();
          List<String> appOpsModesEllipsized = new ArrayList<>();
          for (String mode : appOpsModes) {
            appOpsModesEllipsized.add(Utils.ellipsize(mode, 8));
          }

          mArrayAdapter =
              new ArrayAdapter<>(
                  spinner.getContext(),
                  android.R.layout.simple_spinner_item,
                  appOpsModesEllipsized);

          mArrayAdapterBg =
              new ArrayAdapter<String>(
                  spinner.getContext(),
                  android.R.layout.simple_spinner_item,
                  appOpsModesEllipsized) {
                @Override
                public boolean areAllItemsEnabled() {
                  return false;
                }

                @Override
                public boolean isEnabled(int position) {
                  if (appOpsModes.get(position).equals("Foreground")) {
                    return false;
                  }
                  return super.isEnabled(position);
                }

                // Cannot use color selector here for TextView. It only honors states: "normal",
                // "selected" and "focused", not "disabled".
                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                  View view = super.getDropDownView(position, convertView, parent);
                  if (appOpsModes.get(position).equals("Foreground")) {
                    // Find TextView from android.R.layout.simple_spinner_dropdown_item.
                    TextView tView = view.findViewById(android.R.id.text1);
                    // Check null to avoid broken behavior on different Android versions.
                    if (tView != null) {
                      tView.setTextColor(spinner.getContext().getColor(R.color.disabledStateColor));
                    }
                  }
                  return view;
                }
              };

          mArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
          mArrayAdapterBg.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        if (permission.getName().equals("RUN_IN_BACKGROUND")
            || permission.getName().equals("RUN_ANY_IN_BACKGROUND")) {
          spinner.setAdapter(mArrayAdapterBg);
        } else {
          spinner.setAdapter(mArrayAdapter);
        }

        spinner.setSelection(permission.getAppOpsMode());
        spinner.setOnItemSelectedListener(
            new AdapterView.OnItemSelectedListener() {
              @Override
              public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // "position" is the AppOps mode int value here
                if (permission.getAppOpsMode() != position) {
                  permission.setAppOpsMode(position);
                  mSpinnerSelectListener.onSelect(getItem(getBindingAdapterPosition()), position);
                }
              }

              @Override
              public void onNothingSelected(AdapterView<?> parent) {}
            });
        stateSwitch.setVisibility(View.GONE);
        spinner.setEnabled(permission.isChangeable());
        appOpsDefaultView.setVisibility(permission.isAppOpsSet() ? View.GONE : View.VISIBLE);

      } else {
        if (permission.isProviderMissing()) {
          stateSwitch.setVisibility(View.INVISIBLE);
        } else {
          stateSwitch.setChecked(permission.isGranted());
          stateSwitch.setEnabled(permission.isChangeable());
          stateSwitch.setOnClickListener(
              v -> {
                stateSwitch.setChecked(permission.isGranted()); // do not change the state here
                mSwitchToggleListener.onClick(getItem(getBindingAdapterPosition()));
              });
          stateSwitch.setVisibility(View.VISIBLE);
        }
        spinnerContainer.setVisibility(View.GONE);
      }
    }

    @Override
    public void onClick(View v) {
      int[] location = new int[2];
      v.getLocationInWindow(location);
      mPermClickListener.onClick(
          getItem(getBindingAdapterPosition()), location[1] - 2 * v.getHeight());
    }

    @Override
    public boolean onLongClick(View v) {
      mPermLongClickListener.onLongClick(getItem(getBindingAdapterPosition()));
      return true;
    }
  }

  private static class DiffUtilItemCallBack extends DiffUtil.ItemCallback<Permission> {
    @Override
    public boolean areItemsTheSame(@NonNull Permission oldItem, @NonNull Permission newItem) {
      return oldItem.getName().equals(newItem.getName());
    }

    @Override
    public boolean areContentsTheSame(@NonNull Permission oldItem, @NonNull Permission newItem) {
      return oldItem.areContentsTheSame(newItem);
    }
  }

  interface PermClickListener {
    void onClick(Permission permission);
  }

  interface PermClickListenerWithLoc {
    void onClick(Permission permission, int yLocation);
  }

  interface PermSpinnerSelectListener {
    void onSelect(Permission permission, int selectedValue);
  }

  interface PermLongClickListener {
    void onLongClick(Permission permission);
  }
}
