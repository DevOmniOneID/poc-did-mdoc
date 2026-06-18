/*
 * Copyright 2026 OmniOne.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.omnione.did.mdoc.reader.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import org.omnione.did.mdoc.reader.R;
import org.omnione.did.mdoc.reader.di.DependencyProvider;
import org.omnione.did.mdoc.reader.settings.PreferencesManager;
import org.omnione.did.mdoc.reader.databinding.FragmentSettingsBinding;
import org.omnione.did.sdk.mdoc.proximity.reader.utility.ProtocolLogger;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private PreferencesManager prefs;

    private DependencyProvider deps() {
        return (DependencyProvider) requireActivity().getApplication();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController navController = NavHostFragment.findNavController(this);
        prefs = deps().preferencesManager();

        binding.tvTitle.setText(R.string.settings_title);
        binding.btnBack.setOnClickListener(v -> navController.popBackStack());

        // Load current settings values
        refreshSettings();

        // Set up toggle listeners
        binding.switchRetainData.setOnClickListener(v -> {
            prefs.toggleBoolean(PreferencesManager.KEY_RETAIN_DATA);
            refreshSettings();
        });
        binding.switchSkipIssuerTrust.setOnClickListener(v -> {
            prefs.toggleBoolean(PreferencesManager.KEY_SKIP_ISSUER_TRUST);
            refreshSettings();
        });
        binding.switchBlePeripheral.setOnClickListener(v -> {
            prefs.toggleBoolean(PreferencesManager.KEY_BLE_PERIPHERAL_SERVER);
            refreshSettings();
        });
        binding.switchCaptureIssuerAuth.setOnClickListener(v -> {
            prefs.toggleBoolean(PreferencesManager.KEY_CAPTURE_ISSUER_AUTH);
            ProtocolLogger.setCaptureMode(prefs.isCaptureIssuerAuth());
            refreshSettings();
        });
        binding.btnTrustRefresh.setOnClickListener(v -> new Thread(() -> {
            String listUrl = deps().configProvider().getTrustedIssuerListUrl();
            String gwUrl = deps().configProvider().getDidDocGatewayUrl();
            int n = 0;
            if (listUrl != null && gwUrl != null) {
                n = new org.omnione.did.mdoc.reader.trust.TrustListRefresher(
                    new org.omnione.did.mdoc.reader.trust.IssuerListClient(listUrl),
                    new org.omnione.did.mdoc.reader.trust.DidDocClient(gwUrl),
                    org.omnione.did.mdoc.reader.MdocReaderApplication.getTrustedIssuerStore()).refresh();
            }
            final int count = n;
            requireActivity().runOnUiThread(() -> {
                android.widget.Toast.makeText(
                    requireContext(), getString(R.string.settings_trust_refresh_done, count),
                    android.widget.Toast.LENGTH_SHORT).show();
                refreshSettings();
            });
        }, "trust-refresh-manual").start());
    }

    private void refreshSettings() {
        boolean retainData = prefs.isRetainData();
        binding.switchRetainData.setChecked(retainData);
        binding.tvRetainDataDesc.setText(retainData ? R.string.settings_retain_data_on : R.string.settings_retain_data_off);

        boolean skipTrust = prefs.isSkipIssuerTrust();
        binding.switchSkipIssuerTrust.setChecked(skipTrust);
        binding.tvSkipIssuerTrustDesc.setText(skipTrust ? R.string.settings_skip_issuer_trust_on : R.string.settings_skip_issuer_trust_off);

        boolean blePeripheral = prefs.isBlePeripheralServer();
        binding.switchBlePeripheral.setChecked(blePeripheral);
        binding.tvBlePeripheralDesc.setText(blePeripheral ? R.string.settings_ble_peripheral_on : R.string.settings_ble_peripheral_off);

        boolean captureIssuerAuth = prefs.isCaptureIssuerAuth();
        binding.switchCaptureIssuerAuth.setChecked(captureIssuerAuth);
        binding.tvCaptureIssuerAuthDesc.setText(captureIssuerAuth ? R.string.settings_capture_issuer_auth_on : R.string.settings_capture_issuer_auth_off);

        java.util.Set<String> cached =
            org.omnione.did.mdoc.reader.MdocReaderApplication.getTrustedIssuerStore().trustedIssuerDids();
        if (cached.isEmpty()) {
            binding.tvTrustedIssuersStatus.setText(R.string.settings_trust_cached_empty);
        } else {
            binding.tvTrustedIssuersStatus.setText(
                getString(R.string.settings_trust_cached_list, cached.size(),
                    android.text.TextUtils.join("\n", cached)));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
