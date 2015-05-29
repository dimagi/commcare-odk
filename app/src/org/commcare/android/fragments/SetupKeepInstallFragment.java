package org.commcare.android.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.commcare.android.framework.UiElement;
import org.commcare.android.view.SquareButtonWithText;
import org.commcare.dalvik.R;
import org.javarosa.core.services.locale.Localization;

/**
 * Fragment to start, update or cancel an app installation.
 * Created by dancluna on 4/10/15.
 */
public class SetupKeepInstallFragment extends Fragment {
    @UiElement(R.id.btn_start_install)
    SquareButtonWithText btnStartInstall;

    @UiElement(R.id.btn_stop_install)
    SquareButtonWithText btnStopInstall;

    public interface StartStopInstallCommands {
        void onStartInstallClicked();
        void onStopInstallClicked();
    }

    StartStopInstallCommands buttonCommands;

    public void setButtonCommands(final StartStopInstallCommands buttonCommands) {
        this.buttonCommands = buttonCommands;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.setup_keepinstall, container, false);
        btnStartInstall = (SquareButtonWithText) view.findViewById(R.id.btn_start_install);
        btnStartInstall.setText(Localization.get("install.button.start"));
        btnStopInstall = (SquareButtonWithText) view.findViewById(R.id.btn_stop_install);
        btnStopInstall.setText(Localization.get("install.button.startover"));
        final TextView setupMsg = (TextView) view.findViewById(R.id.str_setup_message);
        setupMsg.setText(Localization.get("install.ready"));
        final TextView netWarn = (TextView) view.findViewById(R.id.net_warn);
        netWarn.setText(Localization.get("install.netwarn"));
        btnStartInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if(buttonCommands != null) buttonCommands.onStartInstallClicked();
            }
        });
        btnStopInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if(buttonCommands != null) buttonCommands.onStopInstallClicked();
            }
        });
        return view;
    }
}
