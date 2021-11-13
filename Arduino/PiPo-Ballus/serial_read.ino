/*
 * Copyright (C) 2021 Fern H. (aka Pavel Neshumov), PiPo-Ballus Table controller
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

void serial_read(void) {
    // Count watchdog cycles
    if (serial_watchdog <= WATCHDOG_LOST_CYCLES)
        serial_watchdog++;

    // Continue loop until all bytes are read
    while (COMMUNICATION_SERIAL.available()) {
        // Read current byte
        serial_buffer[serial_buffer_position] = COMMUNICATION_SERIAL.read();

        if (serial_byte_previous == SERIAL_SUFFIX_1 && serial_buffer[serial_buffer_position] == SERIAL_SUFFIX_2) {
            // If data suffix appears
            // Reset buffer position
            serial_buffer_position = 0;

            // Reset check sum
            serial_check_byte = 0;

            // Calculate check sum
            for (serial_temp_byte = 0; serial_temp_byte <= 12; serial_temp_byte++)
                serial_check_byte ^= serial_buffer[serial_temp_byte];

            // Check if the check sums are equal
            if (serial_check_byte == serial_buffer[13]) {
                // Parse X data
                pid_input_x = (uint16_t)serial_buffer[1] | (uint16_t)serial_buffer[0] << 8;
                delta_x = pid_input_x - serial_x_last;
                serial_x_last = pid_input_x;

                // Parse Y data
                pid_input_y = (uint16_t)serial_buffer[3] | (uint16_t)serial_buffer[2] << 8;
                delta_y = pid_input_y - serial_y_last;
                serial_y_last = pid_input_y;

                // Parse Z data
                pid_input_z = (uint16_t)serial_buffer[5] | (uint16_t)serial_buffer[4] << 8;
                delta_z = pid_input_z - serial_z_last;
                serial_z_last = pid_input_z;

                // Parse setpoints
                pid_x_setpoint = (uint16_t)serial_buffer[7] | (uint16_t)serial_buffer[6] << 8;
                pid_y_setpoint = (uint16_t)serial_buffer[9] | (uint16_t)serial_buffer[8] << 8;
                pid_z_setpoint = (uint16_t)serial_buffer[11] | (uint16_t)serial_buffer[10] << 8;

                // Currently not used
                system_info_byte = serial_buffer[12];

                // Calculate the increment in one cycle
                delta_x /= (float)serial_watchdog;
                delta_y /= (float)serial_watchdog;
                delta_z /= (float)serial_watchdog;

                // Reset watchdog
                serial_watchdog = 0;
            }
        }
        else {
            // Store data bytes
            serial_byte_previous = serial_buffer[serial_buffer_position];
            serial_buffer_position++;

            // Reset buffer on overflow
            if (serial_buffer_position >= 16)
                serial_buffer_position = 0;
        }
    }
}
