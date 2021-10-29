/*
 * Copyright (C) 2021 Fern H. (aka Pavel Neshumov), PiPo-Ballus Android application
 *
 * Licensed under the GNU Affero General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/agpl-3.0.en.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * IT IS STRICTLY PROHIBITED TO USE THE PROJECT (OR PARTS OF THE PROJECT / CODE)
 * FOR MILITARY PURPOSES. ALSO, IT IS STRICTLY PROHIBITED TO USE THE PROJECT (OR PARTS OF THE PROJECT / CODE)
 * FOR ANY PURPOSE THAT MAY LEAD TO INJURY, HUMAN, ANIMAL OR ENVIRONMENTAL DAMAGE.
 * ALSO, IT IS PROHIBITED TO USE THE PROJECT (OR PARTS OF THE PROJECT / CODE) FOR ANY PURPOSE THAT
 * VIOLATES INTERNATIONAL HUMAN RIGHTS OR HUMAN FREEDOM.
 * BY USING THE PROJECT (OR PART OF THE PROJECT / CODE) YOU AGREE TO ALL OF THE ABOVE RULES.
 */

package com.fern.pipo_ballus;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class opens a dialog allowing you to select a main serial device
 */
public class DevicesDialog extends Dialog {
    private static final String ACTION_USB_PERMISSION = "com.android.usb.USB_PERMISSION";

    private final Context context;

    private ListView devicesList;

    private final UsbManager usbManager;
    private final BluetoothAdapter bluetoothAdapter;

    private final List<String> availableDevices;

    private List<UsbSerialDriver> usbSerialDrivers;
    private final List<String> bluetoothMACs;

    private boolean hasUSB = false;
    private boolean hasBluetooth = false;

    private boolean dialogResultReturned = false;

    private DevicesListener devicesListener;

    public DevicesDialog(@NonNull Context context,
                         UsbManager usbManager,
                         BluetoothAdapter bluetoothAdapter) {
        super(context);

        this.context = context;
        this.usbManager = usbManager;
        this.bluetoothAdapter = bluetoothAdapter;

        this.bluetoothMACs = new ArrayList<>();

        this.availableDevices = new ArrayList<>();
    }

    /**
     * Sets DevicesListener interface
     * @param devicesListener DevicesListener interface
     */
    public void setDevicesListener(DevicesListener devicesListener) {
        this.devicesListener = devicesListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load activity
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_devices);

        // Initialize elements
        devicesList = findViewById(R.id.devicesList);

        // Add onClickListener to ListView
        devicesList.setOnItemClickListener((adapterView, view, i, l) -> selectDevice(i));

        // Refresh device list on click
        findViewById(R.id.devicesRefreshBtn).setOnClickListener(view -> refreshDevices());
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (!dialogResultReturned)
            devicesListener.canceled();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Clear dialogResultReturned flag
        dialogResultReturned = false;

        // Refresh device list
        refreshDevices();
    }

    /**
     * Updates usbSerialDrivers, bluetoothMACs and availableDevices
     */
    private void refreshDevices() {
        // Remove adapter
        devicesList.setAdapter(null);

        // Clear available devices
        availableDevices.clear();

        // Add USB devices
        if (usbManager != null) {
            usbSerialDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
            if (usbSerialDrivers != null && usbSerialDrivers.size() > 0) {
                hasUSB = true;
                for (UsbSerialDriver usbSerialDriver : usbSerialDrivers) {
                    UsbDevice usbDevice = usbSerialDriver.getDevice();
                    availableDevices.add(usbDevice.getProductName()
                            + "\n(" + usbDevice.getDeviceName() + ")");
                }
            }
        }

        // Add Bluetooth devices
        if (bluetoothAdapter != null) {
            Set<BluetoothDevice> bluetoothDevices = bluetoothAdapter.getBondedDevices();
            if (bluetoothDevices != null && bluetoothDevices.size() > 0) {
                hasBluetooth = true;
                bluetoothMACs.clear();
                for (BluetoothDevice bluetoothDevice : bluetoothDevices) {
                    bluetoothMACs.add(bluetoothDevice.getAddress());
                    availableDevices.add(bluetoothDevice.getName()
                            + "\n(" + bluetoothDevice.getAddress() + ")");
                }
            }
        }

        // Set listView adapter
        devicesList.setAdapter(new ArrayAdapter<>(context,
                R.layout.listview_layout, R.id.textView, availableDevices));
    }

    /**
     * Selects current device
     * @param index Integer index in ListView
     */
    private void selectDevice(int index) {
        if (index >= 0) {
            int usbIndex = -1;
            int bluetoothIndex = -1;
            if (hasUSB && usbSerialDrivers != null) {
                if (index < usbSerialDrivers.size())
                    usbIndex = index;
                else if (hasBluetooth)
                    bluetoothIndex = index - usbSerialDrivers.size();
            } else if (hasBluetooth && bluetoothAdapter != null)
                bluetoothIndex = index;

            if (usbIndex >= 0) {
                UsbDevice usbDevice = usbSerialDrivers.get(usbIndex).getDevice();

                if (usbManager.hasPermission(usbDevice)) {
                    // Return USB device
                    devicesListener.deviceSelected(
                            new SerialDevice(usbSerialDrivers.get(usbIndex)));

                    // Exit from DevicesDialog
                    dialogResultReturned = true;
                    dismiss();
                }

                else {
                    // Request USB permissions
                    @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent
                            = PendingIntent.getBroadcast(getContext(),
                            0, new Intent(ACTION_USB_PERMISSION), 0);
                    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                    getContext().registerReceiver(usbReceiver, filter);
                    usbManager.requestPermission(usbDevice, pendingIntent);
                }
            }
            else if (bluetoothIndex >= 0) {
                // Return bluetooth device
                devicesListener.deviceSelected(
                        new SerialDevice(bluetoothAdapter.getRemoteDevice(
                                bluetoothMACs.get(bluetoothIndex))));

                // Exit from DevicesDialog
                dialogResultReturned = true;
                dismiss();
            }
            else {
                // Return canceled signal and exit from DevicesDialog
                dialogResultReturned = true;
                devicesListener.canceled();
                dismiss();
            }

        } else {
            // Return canceled signal and exit from DevicesDialog
            dialogResultReturned = true;
            devicesListener.canceled();
            dismiss();
        }
    }

    /**
     * Handles permission request result
     */
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,
                            false)) {
                        if (device != null) {
                            devicesListener.deviceSelected(
                                    new SerialDevice(usbDeviceToSerialDriver(device)));
                        } else
                            devicesListener.canceled();

                        // Set result returned flag
                        dialogResultReturned = true;

                        // Exit from DevicesDialog
                        dismiss();
                    }
                    else {
                        // Display error message
                        Toast.makeText(getContext(),
                                context.getString(R.string.permissions_not_granted),
                                Toast.LENGTH_LONG).show();

                        // Clear result returned flag
                        dialogResultReturned = false;
                    }
                }
            }
        }
    };

    /**
     * Finds UsbSerialDriver in usbSerialDrivers list by UsbDevice
     * @param device UsbDevice
     * @return UsbSerialDriver from usbSerialDrivers list
     */
    private UsbSerialDriver usbDeviceToSerialDriver(UsbDevice device) {
        for (UsbSerialDriver usbSerialDriver : usbSerialDrivers) {
            if (usbSerialDriver.getDevice().equals(device))
                return  usbSerialDriver;
        }
        return null;
    }
}
