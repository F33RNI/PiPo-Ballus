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

// Constants, variables and config
#include "config.h"
#include "constants.h"
#include "pid.h"
#include "datatypes.h"

void setup()
{
    // Setup timer 4 for servos
    TIMER4_BASE->CR1 = TIMER_CR1_CEN | TIMER_CR1_ARPE;
    TIMER4_BASE->CR2 = 0;
    TIMER4_BASE->SMCR = 0;
    TIMER4_BASE->DIER = 0;
    TIMER4_BASE->EGR = 0;
    TIMER4_BASE->CCMR1 = (0b110 << 4) | TIMER_CCMR1_OC1PE | (0b110 << 12) | TIMER_CCMR1_OC2PE;
    TIMER4_BASE->CCMR2 = (0b110 << 4) | TIMER_CCMR2_OC3PE | (0b110 << 12) | TIMER_CCMR2_OC4PE;
    TIMER4_BASE->CCER = TIMER_CCER_CC1E | TIMER_CCER_CC2E | TIMER_CCER_CC3E | TIMER_CCER_CC4E;
    TIMER4_BASE->PSC = 71;
    TIMER4_BASE->ARR = 5000;
    TIMER4_BASE->DCR = 0;

    // Start from middle values (1500 PDM)
    TIMER4_BASE->CCR1 = (SERVO_P_LOWEST + SERVO_P_HIGHEST) / 2;
    TIMER4_BASE->CCR2 = (SERVO_Q_LOWEST + SERVO_Q_HIGHEST) / 2;
    TIMER4_BASE->CCR3 = (SERVO_R_LOWEST + SERVO_R_HIGHEST) / 2;

    // Setup pins as PWM output
    pinMode(PB6, PWM);
    pinMode(PB7, PWM);
    pinMode(PB8, PWM);

    // Setup serial connection
    COMMUNICATION_SERIAL.begin(SERIAL_BAUD_RATE);
    delay(200);
    COMMUNICATION_SERIAL.flush();
}

void loop()
{
    // Read new data from serial port
    serial_read();

    // Execute PID controller
    pid();

    // Reset PID controller if serial connection is lost
    if (serial_watchdog >= WATCHDOG_LOST_CYCLES) {
        pid_i_mem_x = 0;
        pid_i_mem_y = 0;
        pid_i_mem_z = 0;
        pid_last_x_d_error = 0;
        pid_last_y_d_error = 0;
        pid_last_z_d_error = 0;
        pid_output_x = 0;
        pid_output_y = 0;
        pid_output_z = 0;
    }

    // Add 1500 to convert pid output to servo PDM
    servo_x_pulse = 1500. + pid_output_x;
    servo_y_pulse = 1500. + pid_output_y;
    servo_z_pulse = 1500. + pid_output_z;

    // Convert from cartesianto delta
    servo_mapper();

    // Write pulses to the servos
    TIMER4_BASE->CCR1 = servo_p_pulse;
    TIMER4_BASE->CCR2 = servo_q_pulse;
    TIMER4_BASE->CCR3 = servo_r_pulse;
    TIMER4_BASE->CNT = 5000;

    // Check loop time
    while (micros() - loop_timer < LOOP_PERIOD);
    loop_timer = micros();
}
