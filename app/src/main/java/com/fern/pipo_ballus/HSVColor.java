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

/**
 * This class provides HSV color storage as well as its conversion to other formats
 */
public class HSVColor {
    private float hue, saturation, value;

    /**
     * Initializes the current class with black color (0, 0, 0)
     */
    HSVColor() {
        hue = 0;
        saturation = 0;
        value = 0;
    }

    /**
     *  Initializes the current class with provided color
     * @param hue hue component of color (0-360)
     * @param saturation saturation component of color (0-1)
     * @param value value component of color (0-1)
     */
    HSVColor(float hue, float saturation, float value) {
        this.hue = hue;
        this.saturation = saturation;
        this.value = value;
    }

    /**
     * Initializes the current class with provided color
     * @param color color as Integer
     */
    HSVColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];
    }

    /**
     * Initializes the current class with provided color
     * @param hsvColor HSVColor class to copy from
     */
    HSVColor(HSVColor hsvColor) {
        this.hue = hsvColor.getHue();
        this.saturation = hsvColor.getSaturation();
        this.value = hsvColor.getValue();
    }

    /**
     * @return current color hue component as float (0-360)
     */
    public float getHue() {
        return hue;
    }

    /**
     * @return current color saturation component as float (0-1)
     */
    public float getSaturation() {
        return saturation;
    }

    /**
     * @return current color value component as float (0-1)
     */
    public float getValue() {
        return value;
    }

    /**
     * @return current color saturation component as Integer (0-255)
     */
    public int getSaturationInt() {
        return (int) (saturation * 255.);
    }

    /**
     * @return current color value component as Integer (0-255)
     */
    public int getValueInt() {
        return (int) (value * 255.);
    }

    /**
     * Sets hue component of current color
     * @param hue value component of HSV color (0-359)
     */
    public void setHue(float hue) {
        this.hue = hue;
    }

    /**
     * Sets saturation component of current color
     * @param saturation value component of HSV color (0-1)
     */
    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }

    /**
     * Sets value component of current color
     * @param value value component of HSV color (0-1)
     */
    public void setValue(float value) {
        this.value = value;
    }

    /**
     * Sets current color from hsv float array
     * @param hsv color in float[3] format (hue 0-359, saturation 0-1, value 0-1)
     */
    public void setHSVFromFloat(float[] hsv) {
        if (hsv != null && hsv.length >= 3) {
            this.hue = hsv[0];
            this.saturation = hsv[1];
            this.value = hsv[2];
        }
    }

    /**
     * Sets current color from Integer format
     * @param color color as Integer
     */
    public void setHSVFromInt(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];
    }

    /**
     * @return current color as Integer
     */
    public int getIntColor() {
        return Color.HSVToColor( new float[]{ this.hue, this.saturation, this.value });
    }

    /**
     * Static method to calculate contrast color (black or white)
     * @param color color as Integer
     * @return black or white color as Integer
     */
    public static int getContrastColor(int color) {
        double y = (299 * Color.red(color) + 587 * Color.green(color)
                + 114 * Color.blue(color)) / 1000.0;
        return y >= 128 ? Color.BLACK: Color.WHITE;
    }
}
