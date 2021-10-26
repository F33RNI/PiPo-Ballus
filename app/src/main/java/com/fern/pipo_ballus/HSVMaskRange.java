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

import android.graphics.Color;

import org.opencv.core.Scalar;

public class HSVMaskRange {
    private Scalar lower;
    private Scalar upper;
    private boolean inverted;

    public Scalar getLower() {
        return lower;
    }

    public Scalar getUpper() {
        return upper;
    }

    public boolean isInverted() {
        return inverted;
    }

    public void fromIntColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        int hue = (int) (hsv[0] / 2);
        if (hsv[0] / 2 < 10 || hsv[0] / 2 > 170) {
            if (hsv[0] / 2 < 1)
                hue = (int) (hsv[0] / 2 + 90);
            else
                hue = (int) (hsv[0] / 2 - 90);
            this.inverted = true;
        }

        int saturationLow = (int) (hsv[1] * 255) - 50;
        if (saturationLow < 0)
            saturationLow = 0;
        int saturationHigh = (int) (hsv[1] * 255) + 50;
        if (saturationHigh > 255)
            saturationHigh = 255;

        int valueLow = (int) (hsv[2] * 255) - 50;
        if (valueLow < 0)
            valueLow = 0;
        int valueHigh = (int) (hsv[2] * 255) + 50;
        if (valueHigh > 255)
            valueHigh = 255;

        lower = new Scalar(hue - 10, saturationLow, valueLow);
        upper = new Scalar(hue + 10, saturationHigh, valueHigh);
    }
}
