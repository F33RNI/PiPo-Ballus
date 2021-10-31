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

#ifndef CONFIG_H
#define CONFIG_H

/**********************************************/
/*            Serial communication            */
/**********************************************/
// communication serial port
#define COMMUNICATION_SERIAL Serial1

// Serial port speed
const uint32_t SERIAL_BAUD_RATE PROGMEM = 57600;

// Serial packet ending
const uint8_t SERIAL_SUFFIX_1 PROGMEM = 0xEE;
const uint8_t SERIAL_SUFFIX_2 PROGMEM = 0xEF;

// If there is no packet within 125loops * 4ms = 500ms, the connection is considered lost
const uint8_t WATCHDOG_LOST_CYCLES PROGMEM = 125;


/**************************************/
/*            Servo motors            */
/**************************************/
// Actual PDM pre servo at minimum value (1000)
const uint16_t SERVO_P_LOWEST PROGMEM = 2000;
const uint16_t SERVO_Q_LOWEST PROGMEM = 2000;
const uint16_t SERVO_R_LOWEST PROGMEM = 2010;

// Actual PDM pre servo at maximum value (1000)
const uint16_t SERVO_P_HIGHEST PROGMEM = 1450;
const uint16_t SERVO_Q_HIGHEST PROGMEM = 1450;
const uint16_t SERVO_R_HIGHEST PROGMEM = 1450;

#endif
