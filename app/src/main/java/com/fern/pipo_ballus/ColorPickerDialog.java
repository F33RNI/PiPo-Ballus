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

import android.app.Dialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.madrapps.pikolo.ColorPicker;
import com.madrapps.pikolo.listeners.OnColorSelectionListener;

/**
 * This class is a color picker dialog with two buttons (save and cancel)
 * TODO: Optimize dialog size
 */
public class ColorPickerDialog extends Dialog implements OnColorSelectionListener {
    private ImageView imageView;

    private final int startColor;

    private ColorPickerListener colorPickerListener;

    private int color;

    public ColorPickerDialog(@NonNull Context context, int startColor) {
        super(context);

        this.startColor = startColor;
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

        // Initialize color picker and color viewer
        imageView = findViewById(R.id.imageView);
        ColorPicker colorPicker = findViewById(R.id.colorPicker);

        // Paint colorPicker and imageView with startColor
        colorPicker.setColor(startColor);
        imageView.getBackground().setColorFilter(startColor, PorterDuff.Mode.MULTIPLY);

        // Connect this class as ColorSelectionListener
        colorPicker.setColorSelectionListener(this);

        // Connect save button
        findViewById(R.id.colorSaveBtn).setOnClickListener(view -> {
            colorPickerListener.colorSelected(color);
            dismiss();
        });

        // Connect cancel button
        findViewById(R.id.colorCancelBtn).setOnClickListener(view -> {
            colorPickerListener.canceled();
            dismiss();
        });

    }

    /**
     * Colors the center circle and remembers the selected color
     * @param color selected color
     */
    @Override
    public void onColorSelected(int color) {
        imageView.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        this.color = color;
    }


    @Override
    public void onColorSelectionEnd(int i) {

    }

    @Override
    public void onColorSelectionStart(int i) {

    }
}
