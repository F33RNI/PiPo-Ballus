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

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    public static File settingsFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show home activity
        setContentView(R.layout.activity_home);

        // Remove action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize BottomNavigationView variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigationView);

        // Select home item
        bottomNavigationView.setSelectedItemId(R.id.menuHome);

        // Load and parse settings
        if (!SettingsContainer.settingsLoaded) {
            settingsFile = new File(getBaseContext().getExternalFilesDir(null),
                    "settings.json");
            new SettingsHandler(settingsFile, this).readSettings();
        }

        // Add bottom menu clicks
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menuCamera) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                System.gc();
                finish();
            }
            else if (item.getItemId() == R.id.menuSettings) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                System.gc();
                finish();
            }
            return false;
        });

        // Load home HTML page
        String locale = Locale.getDefault().toLanguageTag().toLowerCase();
        if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES) {
            if (locale.contains("ru"))
                ((WebView) findViewById(R.id.webView)).loadUrl(
                        "file:///android_asset/ru/home_night.html");
            else if (locale.contains("es"))
                ((WebView) findViewById(R.id.webView)).loadUrl(
                        "file:///android_asset/es/home_night.html");
            else
                ((WebView) findViewById(R.id.webView)).loadUrl(
                        "file:///android_asset/en/home_night.html");
        }
        else {
            if (locale.contains("ru"))
                ((WebView) findViewById(R.id.webView)).loadUrl(
                        "file:///android_asset/ru/home.html");
            else if (locale.contains("es"))
                ((WebView) findViewById(R.id.webView)).loadUrl(
                        "file:///android_asset/es/home.html");
            else
                ((WebView) findViewById(R.id.webView)).loadUrl(
                        "file:///android_asset/en/home.html");
        }

    }
}