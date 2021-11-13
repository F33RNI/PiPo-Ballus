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

#ifndef PID_H
#define PID_H

/*********************************/
/*            X-Y PID            */
/*********************************/
// P term of X-Y PID controller (default = 0.28)
const float PID_XY_P PROGMEM = 0.32;

// I term of X-Y PID controller (default = 0.006)
const float PID_XY_I PROGMEM = 0.0012;

// D term of X-Y PID controller (default = 5.8)
const float PID_XY_D PROGMEM = 46.0;

// Output filter of X-Y PID controller (default = 0.0)
const float PID_XY_FILTER PROGMEM = 0.9;

// Maximum output of the X-Y PID controller (+ / -)
const float PID_XY_MAX PROGMEM = 500;


/*********************************/
/*            Z PID            */
/*********************************/
// P term of Z PID controller (default = 1.0)
const float PID_Z_P PROGMEM = 1.0;

// I term of Z PID controller (default = 0.0)
const float PID_Z_I PROGMEM = 0;

// D term of Z PID controller (default = 0.0)
const float PID_Z_D PROGMEM = 0.0;

// Output filter of Z PID controller (default = 0.0)
const float PID_Z_FILTER PROGMEM = 0.0;

// Maximum output of the Z PID controller (+ / -)
const float PID_Z_MAX PROGMEM = 500;

#endif
