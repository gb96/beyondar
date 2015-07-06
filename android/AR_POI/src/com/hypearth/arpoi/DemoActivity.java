/*
 * Copyright (C) 2014 BeyondAR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hypearth.arpoi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.beyondar.example.CustomWorldHelper;

public class DemoActivity extends Activity implements OnItemClickListener {

	private ListView mLisViewt;
    private String[] values = new String[]{
            "1. AR Marker test",
			"2. GPS Location with SA History Places",
			"3. GPS with SA History Places, Events, Organisations",
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mLisViewt = (ListView) findViewById(R.id.examplesList);

		fillList();
	}

	private void fillList() {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
				values);
		mLisViewt.setAdapter(adapter);
		mLisViewt.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
		switch (pos) {
		case 0:
            openActivity(Iteration1.class);
            break;
		case 1:
			openActivity(Iteration2.class);
			break;
		case 2:
			openActivity(Iteration3.class);
			break;
		case 3:
            // openActivity(CameraWithGoogleMapsActivity.class);
            break;
		case 4:
            // openActivity(CameraWithTouchEventsActivity.class);
            break;
		case 5:
            // openActivity(CameraWithScreenShotActivity.class);
            break;
		case 6:
            // openActivity(ChangeGeoObjectImagesOnTouchActivity.class);
            break;
		case 7:
            // openActivity(AttachViewToGeoObjectActivity.class);
            break;
		case 8:
            // openActivity(StaticViewGeoObjectActivity.class);
            break;
		case 9:
            // openActivity(SimpleCameraWithCustomFilterActivity.class);
            break;
		case 10:
            // openActivity(SimpleCameraWithRadarActivity.class);
            break;
		case 11:
            // openActivity(BeyondarLocationManagerMapActivity.class);
            break;
		default:
			break;
		}
	}

	private void openActivity(Class<? extends Activity> ActivityClass) {
		Intent intent = new Intent(this, ActivityClass);
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		CustomWorldHelper.sharedWorld = null;
	}

}