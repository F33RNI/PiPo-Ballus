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

/// <summary>
/// Converts cartesian coordinates (X, Y, Z) to delta (P, Q, R) coordinates
/// </summary>
void servo_mapper(void) {
	// Convert X from cartesian to delta
	servo_p_pulse = map(servo_x_pulse, 1000, 2000, X_MIN_P, X_MAX_P) - 1500;
	servo_q_pulse = map(servo_x_pulse, 1000, 2000, X_MIN_Q, X_MAX_Q) - 1500;
	servo_r_pulse = map(servo_x_pulse, 1000, 2000, X_MIN_R, X_MAX_R) - 1500;

	// Convert Y from cartesian to delta
	servo_p_pulse += map(servo_y_pulse, 1000, 2000, Y_MIN_P, Y_MAX_P) - 1500;
	servo_q_pulse += map(servo_y_pulse, 1000, 2000, Y_MIN_Q, Y_MAX_Q) - 1500;
	servo_r_pulse += map(servo_y_pulse, 1000, 2000, Y_MIN_R, Y_MAX_R) - 1500;

	// Convert Z from cartesian to delta
	servo_p_pulse += servo_z_pulse - 1500;
	servo_q_pulse += servo_z_pulse - 1500;
	servo_r_pulse += servo_z_pulse - 1500;

	// Add 1500 to convert output to servo PDM
	servo_p_pulse += 1500;
	servo_q_pulse += 1500;
	servo_r_pulse += 1500;

	// Convert to actual servo PDM
	servo_p_pulse = map(servo_p_pulse, 1000, 2000, SERVO_P_LOWEST, SERVO_P_HIGHEST);
	servo_q_pulse = map(servo_q_pulse, 1000, 2000, SERVO_Q_LOWEST, SERVO_Q_HIGHEST);
	servo_r_pulse = map(servo_r_pulse, 1000, 2000, SERVO_R_LOWEST, SERVO_R_HIGHEST);

	// Trim data ranges
	if (servo_p_pulse > 2000)
		servo_p_pulse = 2000;
	else if (servo_p_pulse < 1000)
		servo_p_pulse = 1000;
	if (servo_q_pulse > 2000)
		servo_q_pulse = 2000;
	else if (servo_q_pulse < 1000)
		servo_q_pulse = 1000;
	if (servo_r_pulse > 2000)
		servo_r_pulse = 2000;
	else if (servo_r_pulse < 1000)
		servo_r_pulse = 1000;
}
