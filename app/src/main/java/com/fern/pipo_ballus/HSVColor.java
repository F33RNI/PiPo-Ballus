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

public class HSVColor {
    private float hue, saturation, value;

    HSVColor() {
        hue = 0;
        saturation = 0;
        value = 0;
    }

    HSVColor(float hue, float saturation, float value) {
        this.hue = hue;
        this.saturation = saturation;
        this.value = value;
    }

    HSVColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];
    }

    HSVColor(HSVColor hsvColor) {
        this.hue = hsvColor.getHue();
        this.saturation = hsvColor.getSaturation();
        this.value = hsvColor.getValue();
    }

    public float getHue() {
        return hue;
    }

    public float getSaturation() {
        return saturation;
    }

    public float getValue() {
        return value;
    }

    public int getSaturationInt() {
        return (int) (saturation * 255.);
    }

    public int getValueInt() {
        return (int) (value * 255.);
    }

    public void setHue(float hue) {
        this.hue = hue;
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public void setHSVFromFloat(float[] hsv) {
        if (hsv != null && hsv.length >= 3) {
            this.hue = hsv[0];
            this.saturation = hsv[1];
            this.value = hsv[2];
        }
    }

    public void setHSVFromInt(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];
    }

    public int getIntColor() {
        return Color.HSVToColor( new float[]{ this.hue, this.saturation, this.value });
    }

    public static int getContrastColor(int color) {
        double y = (299 * Color.red(color) + 587 * Color.green(color)
                + 114 * Color.blue(color)) / 1000.0;
        return y >= 128 ? Color.BLACK: Color.WHITE;
    }
}
