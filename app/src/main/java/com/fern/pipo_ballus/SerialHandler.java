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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.hoho.android.usbserial.driver.UsbSerialPort;

import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class provides communication over a serial port (Bluetooth or USB)
 */
public class SerialHandler implements Runnable {
    private final String TAG = this.getClass().getName();
    private static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final UsbManager usbManager;
    private final BluetoothAdapter bluetoothAdapter;
    private final SerialDevice serialDevice;
    private final LinkedBlockingQueue<PositionContainer> positionContainers;

    private DeviceLostListener deviceLostListener;

    private UsbSerialPort usbSerialPort;
    private BluetoothSocket bluetoothSocket;

    private final byte[] serialBuffer = new byte[12];

    private volatile boolean handleRunning = false;

    /**
     * Sets DeviceLostListener interface (must be initialized from outside)
     * @param deviceLostListener DeviceLostListener interface to send deviceLost() signal
     */
    public void setDeviceLostListener(DeviceLostListener deviceLostListener) {
        this.deviceLostListener = deviceLostListener;
    }

    SerialHandler(UsbManager usbManager,
                  BluetoothAdapter bluetoothAdapter,
                  @NonNull SerialDevice serialDevice,
                  LinkedBlockingQueue<PositionContainer> positionContainers) {
        this.usbManager = usbManager;
        this.bluetoothAdapter = bluetoothAdapter;
        this.serialDevice = serialDevice;
        this.positionContainers = positionContainers;

        this.serialBuffer[10] = SettingsContainer.suffix1;
        this.serialBuffer[11] = SettingsContainer.suffix2;
    }

    /**
     * Tries to open serial ports
     * @return true if port opened successfully or false if not
     */
    public boolean openDevice() {
        try {
            // Open USB serial device
            if (serialDevice.isUsb()) {
                UsbDeviceConnection connection = usbManager.openDevice(
                        serialDevice.getUsbSerialDriver().getDevice());
                if (connection == null)
                    return false;

                usbSerialPort = serialDevice.getUsbSerialDriver().getPorts().get(0);
                try { usbSerialPort.close(); } catch (Exception ignored) { }
                usbSerialPort.open(connection);
                usbSerialPort.setParameters(SettingsContainer.baudRate, 8,
                        UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                // Check if port is opened
                if (usbSerialPort.isOpen())
                    return true;
            }

            // Open Bluetooth serial device
            else if (serialDevice.isBluetooth()) {
                bluetoothSocket = serialDevice.getBluetoothDevice()
                        .createInsecureRfcommSocketToServiceRecord(BT_UUID);
                bluetoothSocket.connect();

                // Check if socket is opened
                if (bluetoothSocket.isConnected())
                    return true;
            }
        } catch (Exception e) {
            // Show error message
            Log.e(TAG, "Error opening serial device!", e);
        }
        return false;
    }

    /**
     * Tries to close serial ports
     */
    public void closeDevice() {
        try {
            // Close ports
            if (serialDevice.isUsb() && usbSerialPort != null)
                usbSerialPort.close();
            if (serialDevice.isBluetooth() && bluetoothAdapter != null && bluetoothSocket != null)
                bluetoothSocket.close();

            // Stop serial thread
            handleRunning = false;
        } catch (Exception e) {
            Log.e(TAG, "Error closing serial device!", e);
        }
    }

    /**
     * Sends positionContainer from LinkedBlockingQueue to sendPosition() void in a loop
     */
    @Override
    public void run() {
        // Set handleRunning flag
        handleRunning = true;

        // Main loop
        while (handleRunning) {
            try {
                sendPosition(positionContainers.take());
            } catch (InterruptedException e) {
                Log.e(TAG, "Error getting data from LinkedBlockingQueue!", e);
            }
        }
    }

    /**
     * Sends data packet over serial port (bluetooth or USB)
     * @param positionContainer PositionContainer class
     */
    private void sendPosition(@NonNull PositionContainer positionContainer) {
        // Build serial packet
        // ballVSTableX
        serialBuffer[0] = (byte) (((int) positionContainer.ballVSTableX >> 8) & 0xFF);
        serialBuffer[1] = (byte) ((int) positionContainer.ballVSTableX & 0xFF);
        // ballVSTableY
        serialBuffer[2] = (byte) (((int) positionContainer.ballVSTableY >> 8) & 0xFF);
        serialBuffer[3] = (byte) ((int) positionContainer.ballVSTableY & 0xFF);
        // ballSetpointX
        serialBuffer[4] = (byte) (((int) positionContainer.ballSetpointX >> 8) & 0xFF);
        serialBuffer[5] = (byte) ((int) positionContainer.ballSetpointX & 0xFF);
        // ballSetpointY
        serialBuffer[6] = (byte) (((int) positionContainer.ballSetpointY >> 8) & 0xFF);
        serialBuffer[7] = (byte) ((int) positionContainer.ballSetpointY & 0xFF);
        // System info
        serialBuffer[8] = (byte) 0;

        // Calculate check byte
        byte checkByte = 0;
        for (int i = 0; i <= 8; i++)
            checkByte = (byte) (checkByte ^ serialBuffer[i]);
        serialBuffer[9] = checkByte;

        // Create checking flag
        boolean isDataSent = false;

        // Send data over serial
        if (serialDevice.isUsb() && usbSerialPort != null && usbSerialPort.isOpen()) {
            try {
                usbSerialPort.write(serialBuffer, 0);
                isDataSent = true;
            } catch (Exception e) {
                Log.e(TAG, "Error sending data over USB serial!", e);
                usbSerialPort = null;
                isDataSent = false;

            }
        }

        // Send data over bluetooth
        if (serialDevice.isBluetooth()
                && bluetoothSocket != null && bluetoothSocket.isConnected()) {
            try {
                bluetoothSocket.getOutputStream().write(serialBuffer);
                isDataSent = true;
            } catch (Exception e) {
                Log.e(TAG, "Error sending data over bluetooth serial!", e);
                bluetoothSocket = null;
                isDataSent = false;
            }
        }

        if (!isDataSent) {
            // Clear LinkedBlockingQueue
            positionContainers.clear();

            // Send deviceLost signal
            if (deviceLostListener != null)
                deviceLostListener.deviceLost();
        }
    }
}
