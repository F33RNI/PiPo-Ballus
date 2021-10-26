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

import android.animation.ArgbEvaluator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bikcrum.circularrangeslider.CircularRangeSlider;
import com.google.android.material.slider.RangeSlider;

/**
 * This class is a color picker dialog with two buttons (save and cancel)
 * TODO: Optimize dialog size
 */
public class ColorPickerDialog extends Dialog {
    private ImageView previewLower, previewUpper;
    private RangeSlider colorPickerS, colorPickerV;
    private Button colorSaveBtn;
    private CircularRangeSlider colorPickerH;

    private final ArgbEvaluator argbEvaluator;
    private final HSVColor colorLower, colorUpper;

    private ColorPickerListener colorPickerListener;

    public ColorPickerDialog(@NonNull Context context, HSVColor startColorLower,
                             HSVColor startColorUpper) {
        super(context);

        this.colorLower = startColorLower;
        this.colorUpper = startColorUpper;

        this.argbEvaluator = new ArgbEvaluator();
    }

    public void setColorPickerListener(ColorPickerListener colorPickerListener) {
        this.colorPickerListener = colorPickerListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load activity
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_color_picker);

        // Initialize elements
        colorPickerH = findViewById(R.id.colorPickerH);
        colorPickerS = findViewById(R.id.colorPickerS);
        colorPickerV = findViewById(R.id.colorPickerV);
        previewLower = findViewById(R.id.previewLower);
        previewUpper = findViewById(R.id.previewUpper);
        colorSaveBtn = findViewById(R.id.colorSaveBtn);

        // Set initial colors
        updateView();

        // Connect hue range slider
        colorPickerH.setOnRangeChangeListener(new CircularRangeSlider.OnRangeChangeListener() {
            @Override
            public void onRangePress(int startIndex, int endIndex) {
                // Set new hue and update elements
                colorLower.setHue(endIndex * 2);
                colorUpper.setHue(startIndex * 2);
                updateView();
            }

            @Override
            public void onRangeChange(int startIndex, int endIndex) {
                // Set new hue and update elements
                colorLower.setHue(endIndex * 2);
                colorUpper.setHue(startIndex * 2);
                updateView();
            }

            @Override
            public void onRangeRelease(int startIndex, int endIndex) {
                // Set new hue and update elements
                colorLower.setHue(endIndex * 2);
                colorUpper.setHue(startIndex * 2);
                updateView();
            }
        });

        // Connect saturation range slider
        colorPickerS.addOnSliderTouchListener(new RangeSlider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull RangeSlider slider) {
                // Set new saturation and update elements
                colorLower.setSaturation(slider.getValues().get(0));
                colorUpper.setSaturation(slider.getValues().get(1));
                updateView();
            }

            @Override
            public void onStopTrackingTouch(@NonNull RangeSlider slider) {
                // Set new saturation and update elements
                colorLower.setSaturation(slider.getValues().get(0));
                colorUpper.setSaturation(slider.getValues().get(1));
                updateView();
            }
        });

        // Connect value range slider
        colorPickerV.addOnSliderTouchListener(new RangeSlider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull RangeSlider slider) {
                // Set new value and update elements
                colorLower.setValue(slider.getValues().get(0));
                colorUpper.setValue(slider.getValues().get(1));
                updateView();
            }

            @Override
            public void onStopTrackingTouch(@NonNull RangeSlider slider) {
                // Set new value and update elements
                colorLower.setValue(slider.getValues().get(0));
                colorUpper.setValue(slider.getValues().get(1));
                updateView();
            }
        });

        // Connect save button
        findViewById(R.id.colorSaveBtn).setOnClickListener(view -> {
            colorPickerListener.colorSelected(colorLower, colorUpper);
            dismiss();
        });
    }

    private void updateView() {
        // Get integer color values
        int colorLowerInt = colorLower.getIntColor();
        int colorUpperInt = colorUpper.getIntColor();

        // Get middle color
        int middleColor = (int) argbEvaluator.evaluate(0.5f, colorLowerInt, colorUpperInt);

        // Set colors
        previewLower.getBackground().setColorFilter(colorLowerInt, PorterDuff.Mode.MULTIPLY);
        previewUpper.getBackground().setColorFilter(colorUpperInt, PorterDuff.Mode.MULTIPLY);
        colorSaveBtn.setBackgroundColor(middleColor);
        colorSaveBtn.setTextColor(HSVColor.getContrastColor(middleColor));
        colorPickerS.setValues(colorLower.getSaturation(), colorUpper.getSaturation());
        colorPickerV.setValues(colorLower.getValue(), colorUpper.getValue());
        colorPickerH.setEndIndex((int) (colorLower.getHue() / 2));
        colorPickerH.setStartIndex((int) (colorUpper.getHue() / 2));
    }
}
