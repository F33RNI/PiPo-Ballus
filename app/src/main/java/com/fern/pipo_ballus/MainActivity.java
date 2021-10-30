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

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.util.concurrent.LinkedBlockingQueue;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getName();

    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA
    };
    private static final int PERMISSION_REQUEST_CODE = 1;

    private OpenCVHandler openCVHandler;
    private SerialDevice serialDevice;
    private SerialHandler serialHandler;
    private LinkedBlockingQueue<PositionContainer> positionContainers;

    /**
     * Checks if OpenCV library is loaded and asks for permissions
     */
    private final BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i("OpenCV", "OpenCV loaded successfully");

                // Request permissions
                if (hasPermissions(MainActivity.this, PERMISSIONS)) {
                    Log.i(TAG, "Permissions granted");

                    // Continue initialization
                    initModules();
                } else {
                    // Grant permissions
                    Log.w(TAG, "Not all permissions granted");
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS,
                            PERMISSION_REQUEST_CODE);
                }
            }
            else {
                super.onManagerConnected(status);
                Toast.makeText(MainActivity.this, R.string.opencv_not_loaded,
                        Toast.LENGTH_LONG).show();

                // Exit back to home screen
                switchToHomeActivity();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Open layout
        setContentView(R.layout.activity_main);

        // Remove action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize BottomNavigationView variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigationView);

        // Select home item
        bottomNavigationView.setSelectedItemId(R.id.menuCamera);

        // Add bottom menu clicks
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menuHome) {
                switchToHomeActivity();
            }
            else if (item.getItemId() == R.id.menuSettings) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                System.gc();
                finish();
            }
            return false;
        });

        // Add actions button click
        findViewById(R.id.actionsBtn).setOnClickListener(view -> {
            ActionsDialog actionsDialog = new ActionsDialog(MainActivity.this);
            actionsDialog.show();
        });

        // Initialize UsbManager
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        // Initialize BluetoothManager
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled())
            Toast.makeText(this, R.string.bluetooth_disabled,
                    Toast.LENGTH_SHORT).show();

        // Initialize LinkedBlockingQueue
        positionContainers = new LinkedBlockingQueue<>(30);

        // Initialize OpenCVHandler class
        openCVHandler = new OpenCVHandler(findViewById(R.id.javaCameraView),
                this, positionContainers);

        // Initialize SerialHandler class
        serialDevice = new SerialDevice();
        serialHandler =
                new SerialHandler(usbManager, bluetoothAdapter, serialDevice, positionContainers);

        // Create and start SerialHandler thread
        Thread serialThread = new Thread(serialHandler);
        serialThread.setPriority(Thread.NORM_PRIORITY);
        serialThread.start();

        // Create DevicesDialog
        DevicesDialog devicesDialog = new DevicesDialog(this, usbManager, bluetoothAdapter);
        devicesDialog.setDevicesListener(new DevicesListener() {
            @Override
            public void deviceSelected(SerialDevice serialDevice) {
                // Copy serialDevice from dialog to MainActivity
                MainActivity.this.serialDevice
                        .setUsbSerialDriver(serialDevice.getUsbSerialDriver());
                MainActivity.this.serialDevice
                        .setBluetoothDevice(serialDevice.getBluetoothDevice());

                // Open serial device
                if (serialHandler.openDevice()) {
                    // Clear LinkedBlockingQueue
                    positionContainers.clear();

                    // Start SerialThread
                    if (!serialThread.isAlive())
                        serialThread.start();

                    // Display info message
                    Toast.makeText(getApplicationContext(), getString(R.string.opened_) +
                                    serialDevice.getDeviceName(),
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    // Display error message
                    Toast.makeText(getApplicationContext(), getString(R.string.error_opening) +
                            serialDevice.getDeviceName(), Toast.LENGTH_SHORT).show();

                    // Exit back to home screen
                    switchToHomeActivity();
                }
            }

            @Override
            public void canceled() {
                // Display message about no device
                Toast.makeText(getApplicationContext(), R.string.no_serial_device,
                        Toast.LENGTH_LONG).show();

                // Exit back to home screen
                switchToHomeActivity();
            }
        });

        // Create DeviceLostListener
        serialHandler.setDeviceLostListener(() -> runOnUiThread(() -> {
            if (!devicesDialog.isShowing())
                devicesDialog.show();
        }));

        // Load OpenCV library and init layout
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "Internal OpenCV library not found." +
                    " Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,
                    this, baseLoaderCallback);
        } else {
            Log.i(TAG, "OpenCV library found inside package. Using it!");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Enable OpenCV view
        if (openCVHandler != null && openCVHandler.isInitialized())
            openCVHandler.getCameraBridgeViewBase().enableView();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Disable OpenCV view
        if (openCVHandler != null && openCVHandler.isInitialized())
            openCVHandler.getCameraBridgeViewBase().disableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Disable OpenCV view
        if (openCVHandler != null && openCVHandler.isInitialized())
            openCVHandler.getCameraBridgeViewBase().disableView();

        // Close bluetooth and usb device
        if (serialHandler != null)
            serialHandler.closeDevice();
    }

    /**
     * Checks for permissions
     * Code from: https://stackoverflow.com/a/34343101
     * @param context Activity
     * @param permissions List of permissions
     * @return true if all permissions were granted
     */
    public boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Calls on permission check result
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check permissions
        if (hasPermissions(this, PERMISSIONS)) {
            Log.i(TAG, "Permissions granted");

            // Continue initialization
            initModules();
        }
        else {
            Toast.makeText(this, getString(R.string.permissions_not_granted),
                    Toast.LENGTH_LONG).show();

            // Exit back to home screen
            switchToHomeActivity();
        }
    }
    
    private void switchToHomeActivity() {
        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        System.gc();
        finish();
    }

    /**
     * Initializes OpenCVHandler and data communication
     */
    private void initModules() {
        // Initialize OpenCVHandler class
        openCVHandler.initView();
    }
}