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
/// Executes X and Y PID controllers
/// </summary>
void pid(void) {
    // X- PID controller
    // Calculate error
    pid_error_temp = (double) (pid_inpit_x - pid_x_setpoint);

    // Calculate I term
    pid_i_mem_x += PID_XY_I * pid_error_temp;
    if (pid_i_mem_x > PID_XY_MAX)pid_i_mem_x = PID_XY_MAX;
    else if (pid_i_mem_x < PID_XY_MAX * -1)pid_i_mem_x = PID_XY_MAX * -1;

    // Calculate P-D terms
    pid_output_x = PID_XY_P * pid_error_temp + pid_i_mem_x + PID_XY_D * (pid_error_temp - pid_last_x_d_error);
    if (pid_output_x > PID_XY_MAX)pid_output_x = PID_XY_MAX;
    else if (pid_output_x < PID_XY_MAX * -1)pid_output_x = PID_XY_MAX * -1;

    // Store error for next loop
    pid_last_x_d_error = pid_error_temp;


    // Y PID controller
    // Calculate error
    pid_error_temp = (double) (pid_inpit_y - pid_y_setpoint);

    // Calculate I term
    pid_i_mem_y += PID_XY_I * pid_error_temp;
    if (pid_i_mem_y > PID_XY_MAX)pid_i_mem_y = PID_XY_MAX;
    else if (pid_i_mem_y < PID_XY_MAX * -1)pid_i_mem_y = PID_XY_MAX * -1;

    // Calculate P-D terms
    pid_output_y = PID_XY_P * pid_error_temp + pid_i_mem_y + PID_XY_D * (pid_error_temp - pid_last_y_d_error);
    if (pid_output_y > PID_XY_MAX)pid_output_y = PID_XY_MAX;
    else if (pid_output_y < PID_XY_MAX * -1)pid_output_y = PID_XY_MAX * -1;

    // Store error for next loop
    pid_last_y_d_error = pid_error_temp;


    // Z PID controller
    // Calculate error
    pid_error_temp = (double)(pid_inpit_z - pid_z_setpoint);

    // Calculate I term
    pid_i_mem_z += PID_Z_I * pid_error_temp;
    if (pid_i_mem_z > PID_Z_MAX)pid_i_mem_z = PID_Z_MAX;
    else if (pid_i_mem_z < PID_Z_MAX * -1)pid_i_mem_z = PID_Z_MAX * -1;

    // Calculate P-D terms
    pid_output_z = PID_Z_P * pid_error_temp + pid_i_mem_z + PID_Z_D * (pid_error_temp - pid_last_z_d_error);
    if (pid_output_z > PID_Z_MAX)pid_output_z = PID_Z_MAX;
    else if (pid_output_z < PID_Z_MAX * -1)pid_output_z = PID_Z_MAX * -1;

    // Store error for next loop
    pid_last_z_d_error = pid_error_temp;
}
