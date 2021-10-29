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

import android.bluetooth.BluetoothDevice;

import com.hoho.android.usbserial.driver.UsbSerialDriver;

/**
 * This class stores data about the selected serial port (Bluetooth or USB)
 */
public class SerialDevice {
    private boolean bluetooth = false, usb = false;
    private UsbSerialDriver usbSerialDriver;
    private BluetoothDevice bluetoothDevice;

    /**
     * Initializes all devices as null
     */
    SerialDevice() {
        this.usbSerialDriver = null;
        this.bluetoothDevice = null;
        this.usb = false;
        this.bluetooth = false;
    }

    /**
     * Initializes UsbSerialDriver only
     * @param usbSerialDriver UsbSerialDriver
     */
    SerialDevice(UsbSerialDriver usbSerialDriver) {
        this.usbSerialDriver = usbSerialDriver;
        this.usb = usbSerialDriver != null;
    }

    /**
     * Initializes BluetoothDevice only
     * @param bluetoothDevice BluetoothDevice
     */
    SerialDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
        this.bluetooth = bluetoothDevice != null;
    }

    /**
     * Sets new UsbSerialDriver
     * @param usbSerialDriver UsbSerialDriver
     */
    public void setUsbSerialDriver(UsbSerialDriver usbSerialDriver) {
        this.usbSerialDriver = usbSerialDriver;
        this.usb = usbSerialDriver != null;
    }

    /**
     * Sets new BluetoothDevice
     * @param bluetoothDevice BluetoothDevice
     */
    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
        this.bluetooth = bluetoothDevice != null;
    }

    /**
     * @return true if previous setUsbSerialDriver was not null
     */
    public boolean isUsb() {
        return usb;
    }

    /**
     * @return true if previous setBluetoothDevice was not null
     */
    public boolean isBluetooth() {
        return bluetooth;
    }

    /**
     * @return current UsbSerialDriver
     */
    public UsbSerialDriver getUsbSerialDriver() {
        return usbSerialDriver;
    }

    /**
     * @return current BluetoothDevice
     */
    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    /**
     * @return DeviceName as String
     */
    public String getDeviceName() {
        if (usb && usbSerialDriver != null)
            return usbSerialDriver.getDevice().getProductName();
        if (bluetooth && bluetoothDevice != null)
            return bluetoothDevice.getName();
        else
            return "";
    }
}
