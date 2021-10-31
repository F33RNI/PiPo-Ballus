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

#ifndef DATATYPES_H
#define DATATYPES_H

// Common variables
uint32_t loop_timer;

// Serial communication
uint8_t serial_buffer[16];
uint8_t serial_buffer_position, serial_byte_previous, serial_check_byte, serial_temp_byte;
uint8_t system_info_byte;
uint8_t serial_watchdog = UINT8_MAX;

// PID controller variables
int16_t pid_inpit_x, pid_inpit_y, pid_inpit_z;
int16_t pid_x_setpoint, pid_y_setpoint, pid_z_setpoint;
float pid_error_temp;
float pid_output_x, pid_output_y, pid_output_z;
float pid_i_mem_x, pid_last_x_d_error;
float pid_i_mem_y, pid_last_y_d_error;
float pid_i_mem_z, pid_last_z_d_error;

// Servo output
uint16_t servo_x_pulse, servo_y_pulse, servo_z_pulse;
uint16_t servo_p_pulse, servo_q_pulse, servo_r_pulse;
uint16_t servo_p_pulse_serial, servo_q_pulse_serial, servo_r_pulse_serial;

#endif